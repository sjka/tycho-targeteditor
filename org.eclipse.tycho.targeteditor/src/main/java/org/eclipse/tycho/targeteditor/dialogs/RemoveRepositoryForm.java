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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tycho.targeteditor.LDITargetDefinitionProvider;
import org.eclipse.tycho.targeteditor.model.ILDIRepositoryLocation;
import org.eclipse.tycho.targeteditor.model.IRepository;
import org.eclipse.tycho.targeteditor.model.ILDITargetDefintion;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.IPartSelectionListener;

public class RemoveRepositoryForm extends AbstractFormPart implements IPartSelectionListener {
    private Button removeButton;
    private final List<IRepository> selectedRepositories = new ArrayList<IRepository>();
    private boolean unknownItemsSelected;
    public static final String REMOVE_REPOSITORY = "Remove Repository";
    private final LDITargetDefinitionProvider targetDefinitionProvider;

    public RemoveRepositoryForm(final LDITargetDefinitionProvider targetDefinitionProvider) {
        this.targetDefinitionProvider = targetDefinitionProvider;
    }

    public void createButtonPart(final Composite buttonComposite) {
        removeButton = new Button(buttonComposite, SWT.PUSH);
        removeButton.setText(RemoveRepositoryForm.REMOVE_REPOSITORY);
        removeButton.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                removeRepository();
            }

            @Override
            public void widgetDefaultSelected(final SelectionEvent e) {
            }
        });
        removeButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, true));
    }

    @Override
    public void refresh() {
        updateButtonState();
    }

    @Override
    public boolean isStale() {
        return true;
    }

    @Override
    public void selectionChanged(final IFormPart part, final ISelection selection) {
        updateCurrentSelection(selection);
        updateButtonState();
    }

    private void updateButtonState() {
        removeButton.setEnabled(canRemove());
    }

    void updateCurrentSelection(final ISelection selection) {
        selectedRepositories.clear();
        unknownItemsSelected = false;
        if (selection instanceof IStructuredSelection) {
            final IStructuredSelection structuredSelection = (IStructuredSelection) selection;
            for (final Object selectedElement : structuredSelection.toArray()) {
                if (selectedElement instanceof IRepository) {
                    selectedRepositories.add((IRepository) selectedElement);
                } else {
                    unknownItemsSelected = true;
                }
            }
        }
    }

    boolean canRemove() {
        return !unknownItemsSelected && selectedRepositories.size() > 0;
    }

    void removeRepository() {
        final ILDITargetDefintion ldiTargetDefinition = targetDefinitionProvider.getLDITargetDefinition();
        final List<ILDIRepositoryLocation> involvedLocations = new ArrayList<ILDIRepositoryLocation>();
        final List<IRepository> touchedRepositories = new ArrayList<IRepository>();
        for (final IRepository repositoryToBeRemoved : selectedRepositories) {
            final ILDIRepositoryLocation location = ldiTargetDefinition.findLocationOf(repositoryToBeRemoved);
            involvedLocations.add(location);

            for (final IRepository repository : location.getRepositories()) {
                touchedRepositories.add(repository);
            }

        }
        touchedRepositories.removeAll(selectedRepositories);
        if (touchedRepositories.isEmpty() || confirmDeleteRepositories(touchedRepositories)) {
            for (final ILDIRepositoryLocation locationToBeRemoved : involvedLocations) {
                ldiTargetDefinition.removeLocation(locationToBeRemoved);
            }
        }
    }

    private boolean confirmDeleteRepositories(final List<IRepository> touchedRepositories) {
        final RepositoryDeletionConfirmDialog dialog = new RepositoryDeletionConfirmDialog(selectedRepositories,
                touchedRepositories);
        return dialog.openConfirm();
    }
}
