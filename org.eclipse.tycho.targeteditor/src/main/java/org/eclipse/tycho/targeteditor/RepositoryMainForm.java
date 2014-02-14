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

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.pde.core.target.ITargetLocation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tycho.targeteditor.dialogs.AddRepositoryWizard;
import org.eclipse.tycho.targeteditor.dialogs.RemoveRepositoryForm;
import org.eclipse.tycho.targeteditor.model.INexusRepository;
import org.eclipse.tycho.targeteditor.model.IRepository;
import org.eclipse.ui.forms.DetailsPart;
import org.eclipse.ui.forms.IDetailsPage;
import org.eclipse.ui.forms.IDetailsPageProvider;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.MasterDetailsBlock;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

@SuppressWarnings("restriction")
public class RepositoryMainForm extends MasterDetailsBlock implements IDetailsPageProvider {

    public static final String HEADER_TEXT_DETAILS = "Details";
    private FilterTable filterTable;
    private final LDITargetDefinitionProvider targetDefinitionProvider;
    public static final String ADD_REPOSITORY = "Add Repository";

    public RepositoryMainForm(final LDITargetDefinitionProvider targetDefinitionProvider) {
        this.targetDefinitionProvider = targetDefinitionProvider;
    }

    @Override
    protected void registerPages(@SuppressWarnings("hiding") final DetailsPart detailsPart) {
        detailsPart.setPageProvider(this);
    }

    @Override
    protected void createToolBarActions(final IManagedForm managedForm) {
        UpdateIUVersionAction.createAndAssign(targetDefinitionProvider, managedForm);
        managedForm.getForm().updateToolBar();
    }

    @Override
    protected void createMasterPart(final IManagedForm managedForm, final Composite parent) {
        sashForm.setOrientation(SWT.VERTICAL);
        final FormToolkit toolkit = managedForm.getToolkit();
        final Section masterSection = toolkit
                .createSection(parent, ExpandableComposite.TITLE_BAR | Section.DESCRIPTION);
        masterSection.setText("Repositories");
        masterSection.setDescription("Select a repository or location to view its details.");
        masterSection.marginWidth = 6;
        masterSection.marginHeight = 6;

        final Composite masterComposite = toolkit.createComposite(masterSection);

        createFilteredTable(managedForm, masterComposite);

        final Composite buttonComposite = new Composite(masterComposite, SWT.NONE);
        buttonComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

        createUpdateLatestReleaseSnapshotSection(managedForm, buttonComposite);
        createAddRepositorySection(managedForm, buttonComposite);
        createRemoveRepositorySection(managedForm, buttonComposite);

        GridLayoutFactory.fillDefaults().numColumns(1).generateLayout(buttonComposite);

        masterSection.setClient(masterComposite);
        GridLayoutFactory.fillDefaults().numColumns(2).generateLayout(masterComposite);

    }

    private void createAddRepositorySection(final IManagedForm managedForm, final Composite buttonComposite) {
        final Button but_addRepository = new Button(buttonComposite, SWT.PUSH);
        but_addRepository.setText(ADD_REPOSITORY);
        but_addRepository.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                final Wizard wizard = new AddRepositoryWizard(managedForm, targetDefinitionProvider);
                final WizardDialog addRepositoryDialog = new WizardDialog(Display.getCurrent().getActiveShell(), wizard);
                addRepositoryDialog.open();
            }

            @Override
            public void widgetDefaultSelected(final SelectionEvent e) {
            }
        });
        but_addRepository.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, true));
    }

    private void createUpdateLatestReleaseSnapshotSection(final IManagedForm managedForm,
            final Composite buttonComposite) {
        final UpdateRepositoryVersionForm updateForm = new UpdateRepositoryVersionForm();
        managedForm.addPart(updateForm);
        updateForm.createButtonPart(managedForm, buttonComposite);
    }

    private void createRemoveRepositorySection(final IManagedForm managedForm, final Composite buttonComposite) {
        final RemoveRepositoryForm removeForm = new RemoveRepositoryForm(targetDefinitionProvider);
        managedForm.addPart(removeForm);
        removeForm.createButtonPart(buttonComposite);
    }

    private void createFilteredTable(final IManagedForm managedForm, final Composite masterComposite) {
        filterTable = new FilterTable(managedForm, masterComposite, new RepositoryTableContentProvider(),
                new RepositoryTableLabelProvider());
        managedForm.getToolkit().adapt(filterTable);
    }

    void resetInput() {
        filterTable.setInput(targetDefinitionProvider.getLDITargetDefinition());
        filterTable.selectFirst();
    }

    @Override
    public Object getPageKey(final Object object) {
        return object;
    }

    @Override
    public IDetailsPage getPage(final Object key) {
        if (key instanceof INexusRepository) {
            return new NexusRepositoryDetailsPart();
        } else if (key instanceof IRepository) {
            return new TargetRepositoryDetailsPart();
        } else if (key instanceof ITargetLocation) {
            return new NonRepositoryDetailsPart();
        }
        return null;
    }

}
