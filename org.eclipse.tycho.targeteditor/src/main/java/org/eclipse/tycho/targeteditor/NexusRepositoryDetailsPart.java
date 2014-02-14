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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.tycho.targeteditor.model.EDynamicVersions;
import org.eclipse.tycho.targeteditor.model.INexusRepository;
import org.eclipse.tycho.targeteditor.model.NexusVersionCalculationResult;
import org.eclipse.tycho.targeteditor.model.LDIModelFactory;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

public class NexusRepositoryDetailsPart extends TargetRepositoryDetailsPart {

    private Text groupId;
    private Text artifactId;
    private Combo version;
    private Button button_problemDetails;

    @Override
    public void createContents(final Composite parent) {
        parentComposite = parent;
        final TableWrapLayout detailsLayout = new TableWrapLayout();
        detailsLayout.numColumns = 1;

        parent.setLayout(detailsLayout);

        final FormToolkit toolkit = getManagedForm().getToolkit();

        final Section detailsSection = toolkit.createSection(parent, ExpandableComposite.TITLE_BAR
                | Section.DESCRIPTION);

        detailsSection.setLayout(new TableWrapLayout());
        detailsSection.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
        detailsSection.setText(RepositoryMainForm.HEADER_TEXT_DETAILS);
        detailsSection.setDescription("This repository is located on Nexus. Additional Information is available.");

        final Composite detailsContainer = toolkit.createComposite(detailsSection, SWT.BORDER);
        final TableWrapLayout layout = new TableWrapLayout();
        layout.numColumns = 3;
        detailsContainer.setLayout(layout);
        detailsSection.setClient(detailsContainer);

        toolkit.createLabel(detailsContainer, "Group ID:");
        groupId = toolkit.createText(detailsContainer, "");
        groupId.setEditable(false);

        groupId.setLayoutData(new TableWrapData(TableWrapData.FILL));
        toolkit.createLabel(detailsContainer, "");

        toolkit.createLabel(detailsContainer, "Artifact ID:");
        artifactId = toolkit.createText(detailsContainer, "");
        artifactId.setEditable(false);
        artifactId.setLayoutData(new TableWrapData(TableWrapData.FILL));
        toolkit.createLabel(detailsContainer, "");

        toolkit.createLabel(detailsContainer, "Version:");

        version = new Combo(detailsContainer, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY);
        toolkit.adapt(version, true, false);
        version.setLayoutData(new TableWrapData(TableWrapData.FILL));
        version.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(final ModifyEvent e) {
                handleVersionComboModified(e);
            }

        });

        button_problemDetails = new Button(detailsContainer, SWT.PUSH);
        button_problemDetails.setImage(Activator.getDefault().getImageFromRegistry(Activator.ERROR_OVERLAY));
        button_problemDetails.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                handleProblemDetailPressed();
            }

            @Override
            public void widgetDefaultSelected(final SelectionEvent e) {
            }
        });

        toolkit.createLabel(detailsContainer, "Repository URL:");
        repoUrl = toolkit.createHyperlink(detailsContainer, "", SWT.WRAP);
        addRepoUrlHyperlinkListener();

        final TableWrapData tableWrapData = new TableWrapData(TableWrapData.LEFT, TableWrapData.MIDDLE, 1, 2);
        repoUrl.setLayoutData(tableWrapData);
    }

    @Override
    public void selectionChanged(final IFormPart part, final ISelection selection) {
        super.selectionChanged(part, selection);
        refresh();
    }

    @Override
    public void refresh() {
        super.refresh();
        final INexusRepository repo = (INexusRepository) selectedRepo;
        if (repo != null) {
            groupId.setText(repo.getGroupId());
            artifactId.setText(repo.getArtifactId());
            refreshVersionCombo(repo);
        } else {
            groupId.setText("");
            artifactId.setText("");
            version.setText("");
            version.setItems(new String[0]);
        }

    }

    private void refreshVersionCombo(final INexusRepository repo) {
        final NexusVersionCalculationResult availableVersions = RepositoryVersionCache.getInstance()
                .getAllAvailableVersions(repo);
        if (availableVersions != null && availableVersions.getStatus().isOK()) {
            final List<String> allVersions = availableVersions.getVersionInfo().getVersions();
            final String latestReleaseVersion = availableVersions.getVersionInfo().getLatestReleaseVersion();
            if (latestReleaseVersion != null && !latestReleaseVersion.isEmpty()) {
                allVersions.add(EDynamicVersions.RELEASE.name());
            }
            final String latestVersion = availableVersions.getVersionInfo().getLatestVersion();
            if (latestVersion != null && !latestVersion.isEmpty()) {
                allVersions.add(EDynamicVersions.SNAPSHOT.name());
            }

            final List<String> currentItems = Arrays.asList(version.getItems());
            // rely on sorted maven-meta.xml from Nexus
            Collections.reverse(allVersions);
            // Read only combo can only set a text contained as an item
            if (!allVersions.contains(repo.getVersion())) {
                allVersions.add(0, repo.getVersion());
            }
            // modifications to combo will reset cursor position. Do only in
            // case content changed.
            if (!allVersions.equals(currentItems)) {
                final String[] availableVersion = allVersions.toArray(new String[allVersions.size()]);
                version.setItems(availableVersion);
                version.setText(repo.getVersion());
            } else if (!version.getText().equals(repo.getVersion())) {
                version.setText(repo.getVersion());
            }
        } else {
            if (!version.getText().equals(repo.getVersion())) {
                version.setItems(new String[] { repo.getVersion() });
                version.setText(repo.getVersion());
            }
        }
        refreshVersionComboMessages(repo);
    }

    private void refreshVersionComboMessages(final INexusRepository repo) {
        final NexusVersionCalculationResult availableVersions = RepositoryVersionCache.getInstance()
                .getAllAvailableVersions(repo);
        if (availableVersions == null) {
            button_problemDetails.setVisible(false);
        } else if (availableVersions.getStatus().isOK()) {
            button_problemDetails.setVisible(false);
        } else {
            button_problemDetails.setVisible(true);
            button_problemDetails.setToolTipText(availableVersions.getStatus().getMessage());
        }
    }

    private void handleVersionComboModified(final ModifyEvent e) {
        final String content = version.getText().trim();
        final INexusRepository repo = (INexusRepository) selectedRepo;
        if (repo != null && !"".equals(content) && !repo.getVersion().equals(content)) {
            repo.setVersion(content);
            LDIModelFactory.adjustRepositoryName(repo);
        }
    }

    private void handleProblemDetailPressed() {
        final INexusRepository repo = (INexusRepository) selectedRepo;
        final IStatus status = RepositoryVersionCache.getInstance().getAllAvailableVersions(repo).getStatus();
        ErrorDialog.openError(button_problemDetails.getShell(), "Error",
                "Unable to display available artifact versions", status);
    }
}
