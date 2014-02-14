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

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.tycho.targeteditor.xml.Repository;

public class RecommendedRepositoriesLabelProvider {

    public String getFilterText(final Object element) {
        return getNameLabel(element) + " " + getGavLabel(element);
    }

    public CellLabelProvider getNameColumLabelProvider() {
        return new ColumnLabelProvider() {
            @Override
            public String getText(final Object element) {
                return getNameLabel(element);
            }
        };
    }

    public CellLabelProvider getGavColumLabelProvider() {
        return new ColumnLabelProvider() {
            @Override
            public String getText(final Object element) {
                return getGavLabel(element);
            }
        };
    }

    private String getNameLabel(final Object element) {
        if (element instanceof Repository) {
            if (element == Repository.RESOLUTION_PENDING) {
                return "Reading recommended repositories...";
            }
            return ((Repository) element).getName();
        }
        return null;
    }

    private String getGavLabel(final Object element) {
        if (element instanceof Repository) {
            final Repository repository = (Repository) element;
            if (element == Repository.RESOLUTION_PENDING) {
                return "";
            }
            return repository.getGroupId() + "  /  " + repository.getArtifactId() + "  /  " + repository.getVersion();
        }
        return null;
    }
}
