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
package org.eclipse.tycho.targeteditor.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.pde.core.IModelChangeProvider;
import org.eclipse.pde.core.IModelChangedEvent;
import org.eclipse.pde.core.IModelChangedListener;
import org.eclipse.pde.core.ModelChangedEvent;

public class ModelChangeProvider implements IModelChangeProvider {
    List<IModelChangedListener> listeners = new ArrayList<IModelChangedListener>();

    @Override
    public void addModelChangedListener(final IModelChangedListener listener) {
        synchronized (listeners) {
            if (!listeners.contains(listener)) {
                listeners.add(listener);
            }
        }
    }

    @Override
    public void removeModelChangedListener(final IModelChangedListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    @Override
    public void fireModelChanged(final IModelChangedEvent event) {
        final List<IModelChangedListener> recievers;
        synchronized (listeners) {
            recievers = new ArrayList<IModelChangedListener>(listeners);
        }
        for (final IModelChangedListener listener : recievers) {
            listener.modelChanged(event);
        }
    }

    @Override
    public void fireModelObjectChanged(final Object object, final String property, final Object oldValue,
            final Object newValue) {
        final IModelChangedEvent event = new ModelChangedEvent(this, object, property, oldValue, newValue);
        fireModelChanged(event);
    }

}
