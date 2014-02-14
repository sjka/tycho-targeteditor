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
package org.eclipse.tycho.targeteditor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.tycho.targeteditor.model.ILDIRepositoryLocation;
import org.eclipse.tycho.targeteditor.model.ILDITargetDefintion;

public class RepositoryTableContentProvider implements IStructuredContentProvider {

    @Override
    public void dispose() {
    }

    @Override
    public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
        if (newInput != null && !(newInput instanceof ILDITargetDefintion)) {
            throw new IllegalArgumentException("inputElement not instanceof ILDITargetDefintion");
        }
    }

    @Override
    public Object[] getElements(final Object inputElement) {
        if (!(inputElement instanceof ILDITargetDefintion)) {
            throw new IllegalArgumentException("inputElement not instanceof ILDITargetDefintion");
        }
        final ILDITargetDefintion targetDefinition = (ILDITargetDefintion) inputElement;

        final List<Object> locations = new ArrayList<Object>();
        for (final ILDIRepositoryLocation location : targetDefinition.getRepositoryLocations()) {
            locations.addAll(location.getRepositories());
        }
        locations.addAll(targetDefinition.getNonRepoLocations());
        return locations.toArray(new Object[locations.size()]);
    }
}
