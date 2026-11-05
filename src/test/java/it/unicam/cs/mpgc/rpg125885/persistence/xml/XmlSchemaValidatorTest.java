package it.unicam.cs.mpgc.rpg125885.persistence.xml;

import it.unicam.cs.mpgc.rpg125885.persistence.PersistenceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class XmlSchemaValidatorTest {

    private XmlSchemaValidator validator;

    @BeforeEach
    void setUp() throws PersistenceException {
        validator = new XmlSchemaValidator("/schema/content.xsd");
    }

    @Test
    void acceptsADocumentThatFollowsTheSchema() {
        assertDoesNotThrow(() -> validate("<traits><trait id=\"calm\" name=\"Calmo\" composure=\"2\"/></traits>"));
    }

    @Test
    void rejectsAMissingRequiredAttribute() {
        PersistenceException error = assertThrows(PersistenceException.class,
                () -> validate("<traits><trait id=\"calm\"/></traits>"));

        assertTrue(error.getMessage().contains("File non valido"));
    }

    @Test
    void rejectsAValueOutsideTheEnumeration() {
        String hazard = "<hazards><hazard id=\"h\" name=\"H\" gravity=\"10\" pace=\"1\" stress=\"1\" escalation=\"0\""
                + " behavior=\"steady\" xp=\"1\"><modifier type=\"MAGIC\" value=\"1.0\"/></hazard></hazards>";

        assertThrows(PersistenceException.class, () -> validate(hazard));
    }

    @Test
    void rejectsDuplicateIds() {
        String traits = "<traits><trait id=\"a\" name=\"A\"/><trait id=\"a\" name=\"B\"/></traits>";

        assertThrows(PersistenceException.class, () -> validate(traits));
    }

    @Test
    void rejectsADocumentThatIsNotWellFormed() {
        assertThrows(PersistenceException.class, () -> validate("<traits><trait"));
    }

    @Test
    void reportsAMissingSchema() {
        assertThrows(PersistenceException.class, () -> new XmlSchemaValidator("/schema/inesistente.xsd"));
    }

    private void validate(String xml) throws PersistenceException {
        validator.validate(xml.getBytes(StandardCharsets.UTF_8), "test");
    }
}
