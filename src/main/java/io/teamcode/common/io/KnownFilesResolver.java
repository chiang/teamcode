package io.teamcode.common.io;

import io.teamcode.common.converter.KnownFile;
import org.springframework.util.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by chiang on 2017. 5. 24..
 */
public class KnownFilesResolver extends DefaultHandler {

    Map<String, KnownFile> knownFilesMap = new HashMap<>();

    private String fileName;

    private String isExtensionOnlyStr;

    public boolean match(String source) {
        if (!StringUtils.hasText(source)) {
            return false;
        }

        boolean matched = false;
        for (KnownFile kf: knownFilesMap.values()) {
            if (kf.isExtensionOnly())
                matched = source.toLowerCase().endsWith(kf.getName());
            else {
                matched = source.equals(kf.getName());
            }

            if (matched)
                break;
        }

        return matched;
    }

    public Map<String, KnownFile> getKnownFilesMap() {
        return this.knownFilesMap;
    }

    public void read(InputStream stream) throws IOException, KnownFilesResolveException {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(false);
            factory.setFeature(
                    XMLConstants.FEATURE_SECURE_PROCESSING, true);
            SAXParser parser = factory.newSAXParser();
            parser.parse(stream, this);
        } catch (ParserConfigurationException e) {
            throw new KnownFilesResolveException("Unable to create an XML parser", e);
        } catch (SAXException e) {
            throw new KnownFilesResolveException("Invalid type configuration", e);
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (qName.equals("known-file-name")) {
            fileName = attributes.getValue("name");

            isExtensionOnlyStr = attributes.getValue("extension-only");
            boolean isExtensionOnly = false;
            if (StringUtils.hasText(isExtensionOnlyStr)) {
                isExtensionOnly = Boolean.valueOf(isExtensionOnlyStr);
            }

            //FIXME toLowerCase 로 하는 것이 타당한지 검토 필요
            knownFilesMap.put(fileName, KnownFile.builder().name(fileName.toLowerCase()).extensionOnly(isExtensionOnly).build());//TODO 조금 더 세밀하게.
        }
    }
}
