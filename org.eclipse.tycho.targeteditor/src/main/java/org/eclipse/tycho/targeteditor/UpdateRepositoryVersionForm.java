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

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tycho.targeteditor.model.INexusRepository;
import org.eclipse.tycho.targeteditor.model.NexusVersionCalculationResult;
import org.eclipse.tycho.targeteditor.model.LDIModelFactory;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.IPartSelectionListener;

public class UpdateRepositoryVersionForm extends AbstractFormPart implements IPartSelectionListener {
    static final String USE_LATEST_RELEASE_TXT = "Use Latest Release";
    private Button but_UseLatestRelease;
    private final List<INexusRepository> currentSelectedNexusRepos = new ArrayList<INexusRepository>();

    public void createButtonPart(final IManagedForm managedForm, final Composite buttonComposite) {
        but_UseLatestRelease = new Button(buttonComposite, SWT.PUSH);
        but_UseLatestRelease.setText(USE_LATEST_RELEASE_TXT);
        but_UseLatestRelease.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                updateToRelease();
            }

            @Override
            public void widgetDefaultSelected(final SelectionEvent e) {
            }
        });
        but_UseLatestRelease.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, true));
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
        but_UseLatestRelease.setEnabled(canUpdateRelease());
    }

    void updateCurrentSelection(final ISelection selection) {
        currentSelectedNexusRepos.clear();
        if (selection instanceof IStructuredSelection) {
            final IStructuredSelection sSel = (IStructuredSelection) selection;
            for (final Object selectedElement : sSel.toArray()) {
                if (selectedElement instanceof INexusRepository) {
                    currentSelectedNexusRepos.add((INexusRepository) selectedElement);
                }
            }
        }
    }

    boolean canUpdateRelease() {
        return getReleaseUpdatable().size() > 0;
    }

    void updateToRelease() {
        for (final INexusRepository repository : getReleaseUpdatable()) {
            final NexusVersionCalculationResult allAvailableVersions = RepositoryVersionCache.getInstance()
                    .getAllAvailableVersions(repository);
            if (allAvailableVersions != null && allAvailableVersions.getStatus().isOK()) {
                repository.setVersion(allAvailableVersions.getVersionInfo().getLatestReleaseVersion());
                LDIModelFactory.adjustRepositoryName(repository);
            }
        }
    }

    private List<INexusRepository> getReleaseUpdatable() {
        final List<INexusRepository> result = new ArrayList<INexusRepository>();
        for (final INexusRepository repository : currentSelectedNexusRepos) {
            final NexusVersionCalculationResult allAvailableVersions = RepositoryVersionCache.getInstance()
                    .getAllAvailableVersions(repository);
            if (allAvailableVersions != null && allAvailableVersions.getStatus().isOK()) {
                final String latestReleaseVersion = allAvailableVersions.getVersionInfo().getLatestReleaseVersion();
                final boolean usesLatestReleasedVersion = latestReleaseVersion == null
                        || latestReleaseVersion.equals(repository.getVersion());
                final boolean usesLatestVersion = allAvailableVersions.getVersionInfo().getLatestVersion()
                        .equals(repository.getVersion());
                if (!(usesLatestReleasedVersion || usesLatestVersion)) {
                    result.add(repository);
                }
            }
        }
        return result;
    }
}
