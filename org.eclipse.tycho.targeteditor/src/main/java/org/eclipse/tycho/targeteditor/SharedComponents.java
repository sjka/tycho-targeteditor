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

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

public class SharedComponents {

    static Composite createCompositeInSection(final Composite parent, final FormToolkit toolkit, final String title,
            final String description) {

        final TableWrapLayout detailsLayout = new TableWrapLayout();
        parent.setLayout(detailsLayout);

        final Section section = toolkit.createSection(parent, ExpandableComposite.TITLE_BAR | Section.DESCRIPTION);
        section.setLayout(new TableWrapLayout());
        section.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
        section.setText(title);
        section.setDescription(description);

        final Composite clientComposite = toolkit.createComposite(section, SWT.BORDER);
        section.setClient(clientComposite);
        return clientComposite;
    }
}
