/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.web.mime;
import java.util.Map;
import net.hasor.cobble.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author 赵永春 (zyc@byshell.org)
 * @version : 2013-7-13
 */
class SaxXmlParser extends DefaultHandler {
    private final Map<String, String> dataMap;

    public SaxXmlParser(Map<String, String> dataMap) {
        this.dataMap = dataMap;
    }

    public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
        if (!"mime-mapping".equalsIgnoreCase(localName))
            return;
        String extension = attributes.getValue("extension");
        String mimeType = attributes.getValue("mimeType");
        if (StringUtils.isBlank(extension) || StringUtils.isBlank(mimeType)) {
            return;
        }
        this.dataMap.put(extension.toUpperCase(), mimeType);
    }
}
