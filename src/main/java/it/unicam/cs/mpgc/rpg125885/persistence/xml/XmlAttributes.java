package it.unicam.cs.mpgc.rpg125885.persistence.xml;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.xml.sax.Attributes;

import java.util.HashMap;
import java.util.Map;

/**
 * Copia degli attributi di un elemento, con letture tipizzate.
 * Vale sia per SAX sia per DOM; i valori sono già validati dallo schema.
 */
final class XmlAttributes {

    private final Map<String, String> values;

    private XmlAttributes(Map<String, String> values) {
        this.values = values;
    }

    /** Copia gli attributi: il parser SAX riusa lo stesso oggetto a ogni evento. */
    static XmlAttributes of(Attributes attributes) {
        Map<String, String> copy = new HashMap<>();
        for (int i = 0; i < attributes.getLength(); i++) {
            copy.put(attributes.getQName(i), attributes.getValue(i));
        }
        return new XmlAttributes(copy);
    }

    static XmlAttributes of(Element element) {
        Map<String, String> copy = new HashMap<>();
        NamedNodeMap attributes = element.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            copy.put(attributes.item(i).getNodeName(), attributes.item(i).getNodeValue());
        }
        return new XmlAttributes(copy);
    }

    boolean has(String name) {
        return values.containsKey(name);
    }

    /**
     * @throws IllegalArgumentException se l'attributo manca
     */
    String text(String name) {
        String value = values.get(name);
        if (value == null) {
            throw new IllegalArgumentException("Attributo mancante: " + name);
        }
        return value;
    }

    int integer(String name) {
        return Integer.parseInt(text(name));
    }

    int integer(String name, int fallback) {
        return has(name) ? integer(name) : fallback;
    }

    double decimal(String name, double fallback) {
        return has(name) ? Double.parseDouble(text(name)) : fallback;
    }

    boolean flag(String name) {
        return Boolean.parseBoolean(text(name));
    }

    boolean flag(String name, boolean fallback) {
        return has(name) ? flag(name) : fallback;
    }

    <E extends Enum<E>> E enumeration(Class<E> type, String name) {
        return Enum.valueOf(type, text(name));
    }
}
