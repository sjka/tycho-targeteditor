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
package org.eclipse.tycho.targeteditor.preferences;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.StringButtonFieldEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tycho.targeteditor.Activator;
import org.eclipse.tycho.targeteditor.xml.RecommededRepositoriesParser;
import org.eclipse.tycho.targeteditor.xml.RecommededRepositoriesParserException;

final class URLFieldEditor extends StringButtonFieldEditor {
    private static final String VALIDATION_TITLE = "Validation";

    URLFieldEditor(final String name, final String labelText, final Composite parent) {
        super(name, labelText, parent);
        setChangeButtonText("&Validate");
    }

    @Override
    protected String changePressed() {
        final String text = getTextControl().getText();
        final IStatus status = validateUrl(text);
        if (status.isOK()) {
            MessageDialog.openInformation(getShell(), VALIDATION_TITLE, "The URL \"" + text
                    + "\" references a valid list of recommended repositories.");
        } else {
            MessageDialog.openError(getShell(), VALIDATION_TITLE, status.getMessage());
        }
        return null;
    }

    private IStatus validateUrl(final String text) {
        final URLValidator validator = new URLValidator(text);
        final IStatus status = validator.validate();
        if (!status.isOK()) {
            return status;
        }
        return validateContent(validator.getUrl());
    }

    private IStatus validateContent(final URL url) {
        try {
            final InputStream inputStream = url.openStream();
            try {
                new RecommededRepositoriesParser().parse(inputStream);
            } finally {
                inputStream.close();
            }
        } catch (final IOException e) {
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Failed to open " + url + ": "
                    + e.getClass().getSimpleName() + ": " + e.getLocalizedMessage(), e);
        } catch (final RecommededRepositoriesParserException e) {
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "The content from " + url + " could not be parsed: "
                    + e.getLocalizedMessage(), e);
        }
        return Status.OK_STATUS;
    }
}
