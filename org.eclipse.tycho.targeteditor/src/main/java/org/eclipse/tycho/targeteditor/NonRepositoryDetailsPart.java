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

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.IDetailsPage;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.widgets.FormToolkit;

public class NonRepositoryDetailsPart extends AbstractFormPart implements IDetailsPage {

    @Override
    public void createContents(final Composite parent) {
        final FormToolkit toolkit = getManagedForm().getToolkit();
        final Composite composite = SharedComponents.createCompositeInSection(parent, toolkit,
                RepositoryMainForm.HEADER_TEXT_DETAILS, "This location type is not supported by this editor.");
        composite.setVisible(false);

    }

    @Override
    public void selectionChanged(final IFormPart part, final ISelection selection) {
        // nothing to show
    }
}
