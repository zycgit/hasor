/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.web.mime;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.ServletContext;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import net.hasor.cobble.ResourcesUtils;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.io.IOUtils;
import net.hasor.cobble.logging.Logger;
import net.hasor.cobble.logging.LoggerFactory;
import net.hasor.web.MimeType;
import org.xml.sax.InputSource;

/**
 * {@link MimeType} 接口实现。
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2015年2月11日
 */
public class MimeTypeSupplier extends ConcurrentHashMap<String, String> implements MimeType {
    private static final Logger         logger = LoggerFactory.getLogger(MimeTypeSupplier.class);
    private final        ServletContext content;

    public MimeTypeSupplier(ServletContext content) {
        this.content = content;
    }

    public ServletContext getContent() {
        return this.content;
    }

    /** 根据扩展名获取meta类型。 */
    public String getMimeType(String suffix) {
        String mimeType = this.get(suffix.toUpperCase());
        if (StringUtils.isBlank(mimeType)) {
            return this.getContent().getMimeType(suffix);
        }
        return mimeType;
    }

    public void addMimeType(String type, String mimeType) {
        if (StringUtils.isNotBlank(type) && StringUtils.isNotBlank(mimeType)) {
            put(type.toUpperCase(), mimeType);
        }
    }

    /** 装载数据。 */
    public void loadResource(String resourceName) throws IOException {
        ClassLoader classLoader = this.content.getClassLoader();
        List<InputStream> inStreamList = null;
        if (classLoader == null) {
            inStreamList = ResourcesUtils.getResourceAsStreamList(resourceName);
        } else {
            inStreamList = ResourcesUtils.getResourceAsStreamList(this.content.getClassLoader(), resourceName);
        }
        for (InputStream inStream : inStreamList) {
            this.loadStream(inStream);
        }
    }

    public void loadReader(Reader reader) throws IOException {
        logger.debug("parsingReader...");
        prossParser(reader, saxParser -> {
            saxParser.parse(new InputSource(reader), new SaxXmlParser(MimeTypeSupplier.this));
        });
    }

    public void loadStream(InputStream inStream) throws IOException {
        logger.debug("parsingStream...");
        prossParser(inStream, saxParser -> {
            saxParser.parse(inStream, new SaxXmlParser(MimeTypeSupplier.this));
        });
    }

    private void prossParser(Closeable closeable, Call call) throws IOException {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
            factory.setFeature("http://xml.org/sax/features/namespaces", true);
            call.parser(factory.newSAXParser());
        } catch (Exception e) {
            throw new IOException(e);
        } finally {
            IOUtils.closeQuietly(closeable);
        }
    }

    private interface Call {
        void parser(SAXParser saxParser) throws Exception;
    }
}
