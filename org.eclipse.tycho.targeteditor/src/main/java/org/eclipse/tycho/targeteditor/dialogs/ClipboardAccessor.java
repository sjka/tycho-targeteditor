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
package org.eclipse.tycho.targeteditor.dialogs;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tycho.targeteditor.model.INexusRepository;
import org.eclipse.tycho.targeteditor.model.NexusRepositoryNames;
import org.eclipse.tycho.targeteditor.model.LDIModelFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class ClipboardAccessor {

    private final INexusRepository repository;

    private static String extractCliboardContents() {
        final Clipboard clipboard = new Clipboard(Display.getCurrent());
        try {
            return (String) clipboard.getContents(TextTransfer.getInstance());
        } finally {
            clipboard.dispose();
        }
    }

    ClipboardAccessor() {
        this(extractCliboardContents());
    }

    ClipboardAccessor(final String clipboardText) {
        if (clipboardText == null) {
            repository = null;
        } else {
            final String text = clipboardText.trim();
            if (getRepositoryByUrl(text) != null) {
                repository = getRepositoryByUrl(text);
            } else {
                repository = getRepositoryByGavXml(text);
            }
        }
    }

    INexusRepository getRepository() {
        return repository;
    }

    boolean hasRepository() {
        return repository != null;
    }

    private INexusRepository getRepositoryByUrl(final String text) {
        try {
            final URI uri = new URI(text);
            return LDIModelFactory.createNexusRepository(uri);
        } catch (final URISyntaxException e) {
            // ignore, presetValue is not an URI
        }
        return null;
    }

    private INexusRepository getRepositoryByGavXml(final String text) {
        try {
            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            final Document document = factory.newDocumentBuilder().parse(new InputSource(new StringReader(text)));
            final XPathFactory xpf = XPathFactory.newInstance();
            final XPath xp = xpf.newXPath();

            final Element root = document.getDocumentElement();
            final String groupId = xp.evaluate("/dependency/groupId", root);
            final String artifactId = xp.evaluate("/dependency/artifactId", root);
            final String version = xp.evaluate("/dependency/version", root);
            final String classifier = xp.evaluate("/dependency/classifier", root);
            final String type = xp.evaluate("/dependency/type", root);

            if (!groupId.isEmpty() && !artifactId.isEmpty() && !version.isEmpty() && "zip".equals(type)) {
                return LDIModelFactory.createNexusRepository(NexusRepositoryNames.SNAPSHOT, groupId, artifactId,
                        version, classifier, type);
            }
        } catch (final SAXException e) {
            // ignore, presetValue is not a GAV xml
        } catch (final IOException e) {
            // ignore, presetValue is not a GAV xml
        } catch (final ParserConfigurationException e) {
            // ignore, presetValue is not a GAV xml
        } catch (final XPathExpressionException e) {
            // ignore, presetValue is not a GAV xml
        }
        return null;
    }

}
