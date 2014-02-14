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
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.tycho.targeteditor.model.IRepository;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.IDetailsPage;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

public class TargetRepositoryDetailsPart extends AbstractFormPart implements IDetailsPage {

    protected IRepository selectedRepo;
    protected Label repoName;
    protected Hyperlink repoUrl;
    protected Composite parentComposite;

    @Override
    public void createContents(final Composite parent) {
        final FormToolkit toolkit = getManagedForm().getToolkit();
        parentComposite = parent;
        final Composite detailsContainer = SharedComponents.createCompositeInSection(parent, toolkit,
                RepositoryMainForm.HEADER_TEXT_DETAILS, "This repository is not located on Nexus.");

        final TableWrapLayout layout = new TableWrapLayout();
        layout.numColumns = 3;
        detailsContainer.setLayout(layout);

        toolkit.createLabel(detailsContainer, "Repository URL:");
        repoUrl = toolkit.createHyperlink(detailsContainer, "", SWT.WRAP);
        addRepoUrlHyperlinkListener();
    }

    @Override
    public void selectionChanged(final IFormPart part, final ISelection selection) {
        final IStructuredSelection sel = (IStructuredSelection) selection;
        selectedRepo = (IRepository) sel.getFirstElement();
        refresh();

    }

    @Override
    public void refresh() {
        super.refresh();
        repoUrl.setText(selectedRepo != null ? selectedRepo.getURI().toString() : "");
        parentComposite.layout();
    }

    protected void addRepoUrlHyperlinkListener() {
        repoUrl.addHyperlinkListener(new HyperlinkAdapter() {
            @Override
            public void linkActivated(final HyperlinkEvent e) {
                Program.launch(repoUrl.getText());
            }
        });
    }

}
