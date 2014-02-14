/**
 * Copyright (c) 2011, 2014 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SAP AG - initial API and implementation
 */
package org.eclipse.tycho.targeteditor.xml;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class RecommededRepositoriesParser {

    public List<Repository> parse(final InputStream stream) {
        try {
            return parseInternal(stream);
        } catch (final ParserConfigurationException e) {
            throw new RecommededRepositoriesParserException(e);
        } catch (final SAXException e) {
            throw new RecommededRepositoriesParserException(e);
        } catch (final IOException e) {
            throw new RecommededRepositoriesParserException(e);
        }

    }

    private List<Repository> parseInternal(final InputStream stream) throws ParserConfigurationException, SAXException,
            IOException {
        final DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        docBuilderFactory.setNamespaceAware(true);
        final DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        final Document document = docBuilder.parse(stream);
        validate(document);
        final Element rootElement = (Element) document.getElementsByTagName("repositories").item(0);
        final String fileVersion = getAttributeValue(rootElement, "version");
        if (!"1.0".equals(fileVersion)) {
            throw new RecommededRepositoriesParserException("File version " + fileVersion
                    + " is not supported by this parser");
        }
        final NodeList repositories = document.getElementsByTagName("repository");
        final List<Repository> result = new ArrayList<Repository>();
        for (int i = 0; i < repositories.getLength(); i++) {
            final Element repository = (Element) repositories.item(i);
            final String name = getAttributeValue(repository, "name");
            final String groupId = getNestedElementTextValue(repository, "groupId");
            final String artifactId = getNestedElementTextValue(repository, "artifactId");
            final String version = getNestedElementTextValue(repository, "version");
            final String classifier = getNestedElementTextValue(repository, "classifier");
            final String description = getNestedElementTextValue(repository, "description");
            final String extension = getNestedElementTextValue(repository, "extension");
            result.add(new Repository(name, groupId, artifactId, version, classifier, description, extension));
        }
        return result;
    }

    private void validate(final Document document) throws SAXException, IOException {
        final SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        final Source schemaFile = new StreamSource(getClass().getResourceAsStream("repository.xsd"));
        final Schema schema = factory.newSchema(schemaFile);
        final Validator validator = schema.newValidator();
        validator.validate(new DOMSource(document));
    }

    private String getAttributeValue(final Element repository, final String attributeName) {
        return repository.getAttributes().getNamedItem(attributeName).getTextContent();
    }

    private String getNestedElementTextValue(final Element element, final String tag) {
        final NodeList groupIdNodeList = element.getElementsByTagName(tag);
        if (groupIdNodeList.getLength() > 0 && groupIdNodeList.item(0).getFirstChild() != null) {
            return groupIdNodeList.item(0).getFirstChild().getNodeValue();
        } else {
            return null;
        }
    }

}
