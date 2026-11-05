package it.unicam.cs.mpgc.rpg125885.persistence.xml;

import it.unicam.cs.mpgc.rpg125885.model.fleet.Fleet;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Unit;
import it.unicam.cs.mpgc.rpg125885.model.item.GearSlot;
import it.unicam.cs.mpgc.rpg125885.model.shift.CallRecord;
import it.unicam.cs.mpgc.rpg125885.model.shift.GameState;
import it.unicam.cs.mpgc.rpg125885.persistence.PersistenceException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Costruisce con DOM il documento di un salvataggio e lo serializza.
 * Scrive solo ciò che non si può ricalcolare: manovre e statistiche effettive si ricavano da tipo, livello, tratto e dotazioni.
 */
public final class GameStateXmlWriter {

    private static final String FORMAT_VERSION = "1";
    private static final String INDENT_PROPERTY = "{http://xml.apache.org/xslt}indent-amount";
    private static final String INDENT_WIDTH = "2";

    /**
     * @param fleetName nome mostrato nel riepilogo dello slot
     * @return il documento serializzato in UTF-8
     */
    public byte[] write(GameState state, String fleetName, LocalDateTime savedAt) throws PersistenceException {
        try {
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Element root = element(document, "savegame",
                    "version", FORMAT_VERSION,
                    "savedAt", DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(savedAt.truncatedTo(ChronoUnit.SECONDS)));
            document.appendChild(root);
            root.appendChild(fleet(document, state.fleet(), fleetName));
            root.appendChild(progress(document, state));
            root.appendChild(log(document, state.callLog()));
            return serialize(document);
        } catch (ParserConfigurationException | TransformerException e) {
            throw new PersistenceException("Impossibile scrivere il salvataggio: " + e.getMessage(), e);
        }
    }

    private Element fleet(Document document, Fleet fleet, String name) {
        Element element = element(document, "fleet", "name", name);
        fleet.units().forEach(unit -> element.appendChild(unit(document, unit)));
        Element inventory = element(document, "inventory");
        fleet.inventory().stream().forEach(entry -> inventory.appendChild(
                element(document, "entry", "item", entry.getKey().id(), "qty", entry.getValue())));
        element.appendChild(inventory);
        return element;
    }

    private Element unit(Document document, Unit unit) {
        Element element = element(document, "unit",
                "callsign", unit.name(),
                "crewLeader", unit.crewLeader(),
                "type", unit.type().id(),
                "trait", unit.trait().id(),
                "level", unit.level(),
                "experience", unit.experience(),
                "endurance", unit.endurance().current(),
                "resources", unit.resources().current(),
                "busyUntil", unit.busyUntil());
        for (GearSlot slot : GearSlot.values()) {
            unit.gearIn(slot).ifPresent(gear -> element.appendChild(
                    element(document, "gear", "slot", slot, "item", gear.id())));
        }
        return element;
    }

    private Element progress(Document document, GameState state) {
        return element(document, "progress",
                "shift", state.shift().id(),
                "phase", state.phaseIndex(),
                "crisis", state.crisisIndex().value(),
                "airAmbulanceUsed", state.isAirAmbulanceUsed());
    }

    private Element log(Document document, List<CallRecord> records) {
        Element log = element(document, "log");
        for (CallRecord record : records) {
            Element element = element(document, "record",
                    "scenario", record.scenarioId(), "outcome", record.outcome());
            record.unitNames().forEach(name -> element.appendChild(element(document, "unit", "name", name)));
            log.appendChild(element);
        }
        return log;
    }

    /** Crea un elemento; gli attributi sono coppie nome, valore. */
    private static Element element(Document document, String name, Object... attributes) {
        Element element = document.createElement(name);
        for (int i = 0; i < attributes.length; i += 2) {
            element.setAttribute((String) attributes[i], String.valueOf(attributes[i + 1]));
        }
        return element;
    }

    private static byte[] serialize(Document document) throws TransformerException {
        TransformerFactory factory = TransformerFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        Transformer transformer = factory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(INDENT_PROPERTY, INDENT_WIDTH);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        transformer.transform(new DOMSource(document), new StreamResult(out));
        return out.toByteArray();
    }
}
