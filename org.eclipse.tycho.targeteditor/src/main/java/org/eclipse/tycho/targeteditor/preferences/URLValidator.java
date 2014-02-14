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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.tycho.targeteditor.Activator;

public final class URLValidator {

    private final String value;

    public URLValidator(final String text) {
        this.value = text;
    }

    public IStatus validate() {
        if ("".equals(value)) { //$NON-NLS-1$
            return error("You have to enter a URL");
        }
        try {
            final URI uri = new URI(value);
            if (!uri.isAbsolute() || uri.isOpaque()) {
                return error("The URL you entered is not well-formed");
            }
            uri.toURL();
            if ("file".equals(uri.getScheme())) { //$NON-NLS-1$
                try {
                    new File(uri);
                } catch (final IllegalArgumentException e) {
                    return error("Invalid file URL specified", e);
                }
            }
        } catch (final URISyntaxException e) {
            return error("The URL you entered is not well-formed", e);
        } catch (final MalformedURLException e) {
            return error("The URL you entered is not well-formed", e);
        }
        return Status.OK_STATUS;
    }

    private IStatus error(final String message, final Exception e) {
        return new Status(IStatus.ERROR, Activator.PLUGIN_ID, message, e);
    }

    private IStatus error(final String message) {
        return new Status(IStatus.ERROR, Activator.PLUGIN_ID, message);
    }

    public URL getUrl() {
        try {
            return new URL(value);
        } catch (final MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
