package it.unicam.cs.mpgc.rpg125885.persistence.xml;

import it.unicam.cs.mpgc.rpg125885.content.ContentCatalog;
import it.unicam.cs.mpgc.rpg125885.content.VehicleTypeRegistry;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Agency;
import it.unicam.cs.mpgc.rpg125885.model.fleet.GaugeKind;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Stats;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Trait;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Unit;
import it.unicam.cs.mpgc.rpg125885.model.fleet.VehicleType;
import it.unicam.cs.mpgc.rpg125885.model.hazard.HazardTemplate;
import it.unicam.cs.mpgc.rpg125885.model.hazard.InterventionType;
import it.unicam.cs.mpgc.rpg125885.model.item.Gear;
import it.unicam.cs.mpgc.rpg125885.model.item.GearSlot;
import it.unicam.cs.mpgc.rpg125885.model.item.Item;
import it.unicam.cs.mpgc.rpg125885.model.item.RecoverySupply;
import it.unicam.cs.mpgc.rpg125885.model.item.ToolSupply;
import it.unicam.cs.mpgc.rpg125885.model.shift.Decision;
import it.unicam.cs.mpgc.rpg125885.model.shift.DecisionOption;
import it.unicam.cs.mpgc.rpg125885.model.shift.Phase;
import it.unicam.cs.mpgc.rpg125885.model.shift.PhaseType;
import it.unicam.cs.mpgc.rpg125885.model.shift.Requirements;
import it.unicam.cs.mpgc.rpg125885.model.shift.Reward;
import it.unicam.cs.mpgc.rpg125885.model.shift.Scenario;
import it.unicam.cs.mpgc.rpg125885.model.shift.Shift;
import it.unicam.cs.mpgc.rpg125885.persistence.PersistenceException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Contenuto di gioco letto con SAX da {@code hazards.xml}, {@code items.xml}, {@code traits.xml}
 * e {@code shift.xml}. Ogni file è validato con {@code content.xsd} prima della lettura; gli oggetti
 * restano in {@link HashMap} indicizzate per id.
 */
public final class XmlContentCatalog implements ContentCatalog {

    private static final String DEFAULT_DIRECTORY = "/content/";
    private static final String SCHEMA = "/schema/content.xsd";
    private static final String DISALLOW_DOCTYPE = "http://apache.org/xml/features/disallow-doctype-decl";
    /** Il turno richiama pericoli, oggetti e tratti: devono essere letti prima. */
    private static final List<String> FILES = List.of("hazards.xml", "items.xml", "traits.xml", "shift.xml");

    private final VehicleTypeRegistry vehicleTypes;
    private final Map<String, HazardTemplate> hazards = new HashMap<>();
    private final Map<String, Item> items = new HashMap<>();
    private final Map<String, Trait> traits = new LinkedHashMap<>();
    private final Map<String, Shift> shifts = new HashMap<>();

    /** Legge il contenuto incluso nel JAR. */
    public XmlContentCatalog(VehicleTypeRegistry vehicleTypes) throws PersistenceException {
        this(vehicleTypes, DEFAULT_DIRECTORY);
    }

    /**
     * @param directory cartella delle risorse nel classpath, con la barra finale
     * @throws PersistenceException se un file manca, non rispetta lo schema o cita id inesistenti
     */
    public XmlContentCatalog(VehicleTypeRegistry vehicleTypes, String directory) throws PersistenceException {
        this.vehicleTypes = Objects.requireNonNull(vehicleTypes, "vehicleTypes");
        XmlSchemaValidator validator = new XmlSchemaValidator(SCHEMA);
        for (String file : FILES) {
            load(validator, directory + file);
        }
    }

    @Override
    public HazardTemplate hazard(String id) {
        return require(hazards, id, "Pericolo");
    }

    @Override
    public Item item(String id) {
        return require(items, id, "Oggetto");
    }

    @Override
    public Trait trait(String id) {
        return require(traits, id, "Tratto");
    }

    @Override
    public List<Trait> traits() {
        return List.copyOf(traits.values());
    }

    @Override
    public Shift shift(String id) {
        return require(shifts, id, "Turno");
    }

    private static <T> T require(Map<String, T> map, String id, String what) {
        T value = map.get(id);
        if (value == null) {
            throw new IllegalArgumentException(what + " sconosciuto: " + id);
        }
        return value;
    }

    private void load(XmlSchemaValidator validator, String resource) throws PersistenceException {
        try (InputStream in = XmlContentCatalog.class.getResourceAsStream(resource)) {
            if (in == null) {
                throw new PersistenceException("Contenuto mancante: " + resource);
            }
            byte[] xml = in.readAllBytes();
            validator.validate(xml, resource);
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature(DISALLOW_DOCTYPE, true);
            factory.newSAXParser().parse(new ByteArrayInputStream(xml), new ContentHandler());
        } catch (IOException | SAXException | ParserConfigurationException | IllegalArgumentException e) {
            throw new PersistenceException("Contenuto non leggibile (" + resource + "): " + e.getMessage(), e);
        }
    }

    private static Stats statBonus(XmlAttributes a) {
        return new Stats(
                a.integer("maxEndurance", 0),
                a.integer("maxResources", 0),
                a.integer("technique", 0),
                a.integer("medicine", 0),
                a.integer("coordination", 0),
                a.integer("composure", 0),
                a.integer("readiness", 0));
    }

    /** Il premio è un oggetto oppure un'unità di rinforzo; la coerenza degli attributi si controlla qui. */
    private Reward reward(XmlAttributes a) {
        if (a.has("item")) {
            return Reward.ofItem(item(a.text("item")), a.integer("quantity", 1));
        }
        VehicleType type = vehicleTypes.byId(a.text("unit"));
        Trait trait = trait(a.text("trait"));
        String callsign = a.text("callsign");
        String crewLeader = a.text("crewLeader");
        return Reward.ofReinforcement("Rinforzo: " + type.displayName() + " " + callsign,
                () -> new Unit(callsign, crewLeader, type, trait));
    }

    /** Chiamata in costruzione: i suoi elementi arrivano uno alla volta dal parser. */
    private static final class ScenarioDraft {
        private final XmlAttributes attributes;
        private String callText = "";
        private final List<HazardTemplate> hazards = new ArrayList<>();
        private final Map<Agency, Integer> minimumByAgency = new EnumMap<>(Agency.class);
        private final Set<String> vehicleTypes = new HashSet<>();
        private String question;
        private final List<DecisionOption> options = new ArrayList<>();
        private XmlAttributes outcome;
        private Reward reward;

        ScenarioDraft(XmlAttributes attributes) {
            this.attributes = attributes;
        }

        Scenario build() {
            Optional<Decision> decision = question == null
                    ? Optional.empty()
                    : Optional.of(new Decision(question, options.get(0), options.get(1)));
            return new Scenario(
                    attributes.text("id"),
                    attributes.text("title"),
                    attributes.text("district"),
                    callText,
                    hazards,
                    attributes.integer("rounds"),
                    new Requirements(minimumByAgency, vehicleTypes),
                    decision,
                    outcome.text("success"),
                    outcome.text("failure"),
                    attributes.integer("ignorePenalty"),
                    attributes.integer("failPenalty"),
                    attributes.integer("busyPhases"),
                    Optional.ofNullable(reward));
        }
    }

    /** Trasforma gli eventi SAX in oggetti del modello e li registra nel catalogo. */
    private final class ContentHandler extends DefaultHandler {

        private final StringBuilder text = new StringBuilder();
        private final Map<InterventionType, Double> modifiers = new EnumMap<>(InterventionType.class);
        private XmlAttributes hazardAttributes;
        private ScenarioDraft scenario;
        private final Map<String, Scenario> scenarios = new LinkedHashMap<>();
        private String shiftId;
        private XmlAttributes phaseAttributes;
        private final List<Scenario> offers = new ArrayList<>();
        private final List<Phase> phases = new ArrayList<>();

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) {
            XmlAttributes a = XmlAttributes.of(attributes);
            switch (qName) {
                case "hazard" -> startHazard(a);
                case "modifier" -> modifiers.put(a.enumeration(InterventionType.class, "type"), a.decimal("value", 1.0));
                case "gear" -> addItem(new Gear(a.text("id"), a.text("name"), a.text("description"),
                        a.enumeration(GearSlot.class, "slot"), statBonus(a)));
                case "toolSupply" -> addItem(new ToolSupply(a.text("id"), a.text("name"), a.text("description"),
                        a.integer("reduction")));
                case "recoverySupply" -> addItem(new RecoverySupply(a.text("id"), a.text("name"), a.text("description"),
                        a.enumeration(GaugeKind.class, "gauge"), a.integer("amount")));
                case "trait" -> traits.put(a.text("id"), new Trait(a.text("id"), a.text("name"), statBonus(a),
                        a.decimal("recoveryMultiplier", 1.0), a.decimal("experienceMultiplier", 1.0)));
                case "shift" -> shiftId = a.text("id");
                case "scenario" -> scenario = new ScenarioDraft(a);
                case "call" -> text.setLength(0);
                case "requires" -> scenario.minimumByAgency.put(a.enumeration(Agency.class, "agency"), a.integer("min"));
                case "requiresVehicle" -> scenario.vehicleTypes.add(vehicleTypes.byId(a.text("type")).id());
                case "decision" -> scenario.question = a.text("question");
                case "option" -> scenario.options.add(new DecisionOption(a.text("text"), a.integer("gravity"),
                        a.integer("rounds"), a.integer("stress")));
                case "outcome" -> scenario.outcome = a;
                case "reward" -> scenario.reward = reward(a);
                case "phase" -> startPhase(a);
                case "offer" -> offers.add(scenarios.get(a.text("ref")));
                default -> { }
            }
        }

        @Override
        public void characters(char[] chars, int start, int length) {
            text.append(chars, start, length);
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            switch (qName) {
                case "hazard" -> endHazard();
                case "call" -> scenario.callText = text.toString().strip().replaceAll("\\s+", " ");
                case "scenario" -> endScenario();
                case "phase" -> phases.add(new Phase(phaseAttributes.text("time"),
                        phaseAttributes.enumeration(PhaseType.class, "type"), offers));
                case "shift" -> shifts.put(shiftId, new Shift(shiftId, phases));
                default -> { }
            }
        }

        /** {@code <hazard>} è una definizione in hazards.xml e un riferimento dentro una chiamata. */
        private void startHazard(XmlAttributes a) {
            if (scenario == null) {
                hazardAttributes = a;
                modifiers.clear();
            } else {
                scenario.hazards.add(hazard(a.text("ref")));
            }
        }

        private void endHazard() {
            if (scenario != null) {
                return;
            }
            XmlAttributes a = hazardAttributes;
            hazards.put(a.text("id"), new HazardTemplate(a.text("id"), a.text("name"), a.integer("gravity"),
                    a.integer("pace"), modifiers, a.flag("elevated", false), a.integer("stress"),
                    a.integer("escalation"), a.text("behavior"), a.integer("xp")));
        }

        private void endScenario() {
            Scenario built = scenario.build();
            scenarios.put(built.id(), built);
            scenario = null;
        }

        private void startPhase(XmlAttributes a) {
            phaseAttributes = a;
            offers.clear();
        }

        private void addItem(Item item) {
            items.put(item.id(), item);
        }
    }
}
