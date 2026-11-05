package it.unicam.cs.mpgc.rpg125885.persistence.xml;

import it.unicam.cs.mpgc.rpg125885.content.ContentCatalog;
import it.unicam.cs.mpgc.rpg125885.content.VehicleTypeRegistry;
import it.unicam.cs.mpgc.rpg125885.model.shift.GameState;
import it.unicam.cs.mpgc.rpg125885.persistence.GameRepository;
import it.unicam.cs.mpgc.rpg125885.persistence.PersistenceException;
import it.unicam.cs.mpgc.rpg125885.persistence.SaveSlotInfo;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Salvataggi in XML, un file {@code <slot>.xml} per slot in una cartella.
 * Ogni file è validato con {@code savegame.xsd} sia quando si scrive sia quando si legge.
 */
public final class XmlGameRepository implements GameRepository {

    private static final String SCHEMA = "/schema/savegame.xsd";
    private static final String EXTENSION = ".xml";
    private static final String TEMP_EXTENSION = ".tmp";
    private static final String DISALLOW_DOCTYPE = "http://apache.org/xml/features/disallow-doctype-decl";
    private static final Pattern VALID_SLOT = Pattern.compile("[\\p{L}\\p{N}_ -]{1,40}");

    private final Path directory;
    private final Clock clock;
    private final XmlSchemaValidator validator;
    private final GameStateXmlWriter writer = new GameStateXmlWriter();
    private final GameStateXmlReader reader;

    public XmlGameRepository(Path directory, ContentCatalog catalog, VehicleTypeRegistry vehicleTypes)
            throws PersistenceException {
        this(directory, catalog, vehicleTypes, Clock.systemDefaultZone());
    }

    /**
     * @param directory cartella degli slot; si crea al primo salvataggio
     * @param clock     orologio per la data del salvataggio (fisso nei test)
     */
    public XmlGameRepository(Path directory, ContentCatalog catalog, VehicleTypeRegistry vehicleTypes, Clock clock)
            throws PersistenceException {
        this.directory = Objects.requireNonNull(directory, "directory");
        this.clock = Objects.requireNonNull(clock, "clock");
        this.validator = new XmlSchemaValidator(SCHEMA);
        this.reader = new GameStateXmlReader(catalog, vehicleTypes);
    }

    /** Cartella di default: {@code <home utente>/.rpg125885/saves}. */
    public static Path defaultDirectory() {
        return Path.of(System.getProperty("user.home"), ".rpg125885", "saves");
    }

    @Override
    public void save(String slot, GameState state) throws PersistenceException {
        Path target = fileOf(slot);
        byte[] xml = writer.write(state, slot, LocalDateTime.now(clock));
        validator.validate(xml, slot);
        Path temp = directory.resolve(slot + TEMP_EXTENSION);
        try {
            Files.createDirectories(directory);
            Files.write(temp, xml);
            Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new PersistenceException("Impossibile salvare lo slot " + slot + ": " + e.getMessage(), e);
        }
    }

    @Override
    public GameState load(String slot) throws PersistenceException {
        byte[] xml = read(slot);
        validator.validate(xml, slot);
        return reader.read(parse(xml, slot));
    }

    @Override
    public List<SaveSlotInfo> listSlots() throws PersistenceException {
        if (!Files.isDirectory(directory)) {
            return List.of();
        }
        List<SaveSlotInfo> slots = new ArrayList<>();
        try (DirectoryStream<Path> files = Files.newDirectoryStream(directory, "*" + EXTENSION)) {
            for (Path file : files) {
                String name = file.getFileName().toString();
                String slot = name.substring(0, name.length() - EXTENSION.length());
                try {
                    slots.add(summarize(slot, Files.readAllBytes(file)));
                } catch (PersistenceException | IOException damaged) {
                    // Un file danneggiato non deve impedire di vedere gli altri slot
                }
            }
        } catch (IOException e) {
            throw new PersistenceException("Impossibile leggere la cartella dei salvataggi: " + e.getMessage(), e);
        }
        slots.sort(Comparator.comparing(SaveSlotInfo::savedAt).reversed());
        return slots;
    }

    @Override
    public void delete(String slot) throws PersistenceException {
        try {
            if (!Files.deleteIfExists(fileOf(slot))) {
                throw new PersistenceException("Slot inesistente: " + slot);
            }
        } catch (IOException e) {
            throw new PersistenceException("Impossibile eliminare lo slot " + slot + ": " + e.getMessage(), e);
        }
    }

    /** Riepilogo con XPath sui soli attributi necessari, senza ricostruire la partita. */
    private SaveSlotInfo summarize(String slot, byte[] xml) throws PersistenceException {
        Document document = parse(xml, slot);
        XPath xpath = XPathFactory.newInstance().newXPath();
        try {
            return new SaveSlotInfo(
                    slot,
                    xpath.evaluate("/savegame/fleet/@name", document),
                    LocalDateTime.parse(xpath.evaluate("/savegame/@savedAt", document)),
                    Integer.parseInt(xpath.evaluate("/savegame/progress/@phase", document)),
                    Integer.parseInt(xpath.evaluate("/savegame/progress/@crisis", document)),
                    ((Number) xpath.evaluate("count(/savegame/fleet/unit)", document, XPathConstants.NUMBER))
                            .intValue());
        } catch (XPathExpressionException | NumberFormatException | DateTimeParseException e) {
            throw new PersistenceException("Riepilogo non leggibile (" + slot + ")", e);
        }
    }

    private byte[] read(String slot) throws PersistenceException {
        Path file = fileOf(slot);
        try {
            if (!Files.exists(file)) {
                throw new PersistenceException("Slot inesistente: " + slot);
            }
            return Files.readAllBytes(file);
        } catch (IOException e) {
            throw new PersistenceException("Impossibile leggere lo slot " + slot + ": " + e.getMessage(), e);
        }
    }

    private static Document parse(byte[] xml, String slot) throws PersistenceException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature(DISALLOW_DOCTYPE, true);
            return factory.newDocumentBuilder().parse(new ByteArrayInputStream(xml));
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new PersistenceException("File non leggibile (" + slot + "): " + e.getMessage(), e);
        }
    }

    /** Il nome dello slot diventa il nome del file: si accettano solo caratteri sicuri. */
    private Path fileOf(String slot) throws PersistenceException {
        if (slot == null || !VALID_SLOT.matcher(slot).matches()) {
            throw new PersistenceException(
                    "Nome dello slot non valido: usa da 1 a 40 tra lettere, numeri, spazi, trattini e trattini bassi");
        }
        return directory.resolve(slot + EXTENSION);
    }
}
