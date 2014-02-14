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

import org.eclipse.pde.core.IModelChangedEvent;
import org.eclipse.pde.core.IModelChangedListener;

public class CollectingModelChangeListener implements IModelChangedListener {
    final private List<IModelChangedEvent> receivedEvents = new ArrayList<IModelChangedEvent>();

    @Override
    public void modelChanged(final IModelChangedEvent event) {
        receivedEvents.add(event);
    }

    public List<IModelChangedEvent> getCollectedEvents() {
        return receivedEvents;
    }

}
