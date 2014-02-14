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

import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.tycho.targeteditor.xml.Repository;

public class RecommendedRepositoriesContentProvider implements IStructuredContentProvider {

    @Override
    public void dispose() {
    }

    @Override
    public void inputChanged(final Viewer arg0, final Object arg1, final Object arg2) {

    }

    @Override
    public Object[] getElements(final Object inputElement) {
        if (!(inputElement instanceof List<?>)) {
            throw new IllegalArgumentException("inputElement not instance of List<?>");
        }
        final List<Repository> recommendedReposList = (List<Repository>) inputElement;
        return recommendedReposList.toArray();
    }
}
