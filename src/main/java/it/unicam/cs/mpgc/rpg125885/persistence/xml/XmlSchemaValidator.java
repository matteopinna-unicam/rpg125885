package it.unicam.cs.mpgc.rpg125885.persistence.xml;

import it.unicam.cs.mpgc.rpg125885.persistence.PersistenceException;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;

/** Controlla un documento XML contro uno schema XSD caricato dalle risorse. */
public final class XmlSchemaValidator {

    private final Schema schema;

    /**
     * @param schemaResource percorso dello schema nel classpath, ad esempio {@code /schema/content.xsd}
     * @throws PersistenceException se lo schema manca o non è valido
     */
    public XmlSchemaValidator(String schemaResource) throws PersistenceException {
        URL url = XmlSchemaValidator.class.getResource(schemaResource);
        if (url == null) {
            throw new PersistenceException("Schema mancante: " + schemaResource);
        }
        try {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            this.schema = factory.newSchema(url);
        } catch (SAXException e) {
            throw new PersistenceException("Schema non valido: " + schemaResource, e);
        }
    }

    /**
     * @param xml         contenuto del documento
     * @param description nome mostrato nel messaggio d'errore (file o slot)
     * @throws PersistenceException se il documento non è ben formato o viola lo schema
     */
    public void validate(byte[] xml, String description) throws PersistenceException {
        try {
            Validator validator = schema.newValidator();
            validator.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            validator.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            validator.validate(new StreamSource(new ByteArrayInputStream(xml)));
        } catch (SAXException | IOException e) {
            throw new PersistenceException("File non valido (" + description + "): " + e.getMessage(), e);
        }
    }
}
