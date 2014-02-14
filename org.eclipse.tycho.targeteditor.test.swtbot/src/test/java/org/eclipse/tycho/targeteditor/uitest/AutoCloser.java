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
package org.eclipse.tycho.targeteditor.uitest;

import java.io.Closeable;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.junit.rules.ExternalResource;

public class AutoCloser extends ExternalResource {

    private final List<Closeable> items = new LinkedList<Closeable>();

    @Override
    protected void after() {
        for (final Closeable item : items) {
            close(item);
        }
    }

    protected void close(final Closeable item) {
        try {
            item.close();
        } catch (final IOException exception) {
            // ignore
        }
    }

    public <T extends Closeable> T add(final T item) {
        items.add(item);
        return item;
    }
}
