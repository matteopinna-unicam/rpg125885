package it.unicam.cs.mpgc.rpg125885.persistence.xml;

import it.unicam.cs.mpgc.rpg125885.content.ContentCatalog;
import it.unicam.cs.mpgc.rpg125885.content.VehicleTypeRegistry;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Fleet;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Gauge;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Unit;
import it.unicam.cs.mpgc.rpg125885.model.item.Gear;
import it.unicam.cs.mpgc.rpg125885.model.item.GearSlot;
import it.unicam.cs.mpgc.rpg125885.model.item.Item;
import it.unicam.cs.mpgc.rpg125885.model.shift.CallOutcome;
import it.unicam.cs.mpgc.rpg125885.model.shift.CallRecord;
import it.unicam.cs.mpgc.rpg125885.model.shift.CrisisIndex;
import it.unicam.cs.mpgc.rpg125885.model.shift.GameState;
import it.unicam.cs.mpgc.rpg125885.persistence.PersistenceException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Ricostruisce una {@link GameState} da un documento DOM già validato, risolvendo gli id
 * di tipi di mezzo, tratti, oggetti e turno con i cataloghi.
 */
public final class GameStateXmlReader {

    private static final int FIRST_LEVEL = 1;

    private final ContentCatalog catalog;
    private final VehicleTypeRegistry vehicleTypes;

    public GameStateXmlReader(ContentCatalog catalog, VehicleTypeRegistry vehicleTypes) {
        this.catalog = Objects.requireNonNull(catalog, "catalog");
        this.vehicleTypes = Objects.requireNonNull(vehicleTypes, "vehicleTypes");
    }

    /**
     * @throws PersistenceException se il salvataggio cita id sconosciuti o valori che il modello rifiuta
     */
    public GameState read(Document document) throws PersistenceException {
        try {
            Element root = document.getDocumentElement();
            XmlAttributes progress = XmlAttributes.of(first(root, "progress"));
            return new GameState(
                    readFleet(first(root, "fleet")),
                    catalog.shift(progress.text("shift")),
                    progress.integer("phase"),
                    new CrisisIndex(progress.integer("crisis")),
                    readLog(first(root, "log")),
                    progress.flag("airAmbulanceUsed"));
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw new PersistenceException("Salvataggio incoerente con il contenuto di gioco: " + e.getMessage(), e);
        }
    }

    private Fleet readFleet(Element element) {
        Fleet fleet = new Fleet(children(element, "unit").stream().map(this::readUnit).toList());
        for (Element entry : children(first(element, "inventory"), "entry")) {
            XmlAttributes a = XmlAttributes.of(entry);
            fleet.inventory().add(catalog.item(a.text("item")), a.integer("qty"));
        }
        return fleet;
    }

    /** Riporta l'unità allo stato salvato usando solo la sua interfaccia pubblica. */
    private Unit readUnit(Element element) {
        XmlAttributes a = XmlAttributes.of(element);
        Unit unit = new Unit(a.text("callsign"), a.text("crewLeader"),
                vehicleTypes.byId(a.text("type")), catalog.trait(a.text("trait")));
        for (int level = FIRST_LEVEL; level < a.integer("level"); level++) {
            unit.levelUp();
        }
        unit.addExperience(a.integer("experience"));
        children(element, "gear").forEach(gear -> unit.equip(readGear(gear)));
        restore(unit.endurance(), a.integer("endurance"), "Resistenza");
        restore(unit.resources(), a.integer("resources"), "Risorse");
        unit.occupyUntil(a.integer("busyUntil"));
        return unit;
    }

    private Gear readGear(Element element) {
        XmlAttributes a = XmlAttributes.of(element);
        Item item = catalog.item(a.text("item"));
        if (!(item instanceof Gear gear) || gear.slot() != a.enumeration(GearSlot.class, "slot")) {
            throw new IllegalArgumentException("Dotazione non valida: " + a.text("item"));
        }
        return gear;
    }

    private static void restore(Gauge gauge, int value, String name) {
        if (value > gauge.max()) {
            throw new IllegalArgumentException(name + " oltre il massimo: " + value);
        }
        gauge.decrease(gauge.current());
        gauge.increase(value);
    }

    private static List<CallRecord> readLog(Element log) {
        List<CallRecord> records = new ArrayList<>();
        for (Element record : children(log, "record")) {
            XmlAttributes a = XmlAttributes.of(record);
            List<String> unitNames = children(record, "unit").stream()
                    .map(unit -> XmlAttributes.of(unit).text("name"))
                    .toList();
            records.add(new CallRecord(a.text("scenario"), a.enumeration(CallOutcome.class, "outcome"), unitNames));
        }
        return records;
    }

    private static List<Element> children(Element parent, String name) {
        List<Element> result = new ArrayList<>();
        for (Node node = parent.getFirstChild(); node != null; node = node.getNextSibling()) {
            if (node instanceof Element element && element.getTagName().equals(name)) {
                result.add(element);
            }
        }
        return result;
    }

    private static Element first(Element parent, String name) {
        return children(parent, name).getFirst();
    }
}
