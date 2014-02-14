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

import org.eclipse.equinox.p2.metadata.Version;
import org.eclipse.jface.action.ControlContribution;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.tycho.targeteditor.model.IMutableVersionedId;
import org.eclipse.tycho.targeteditor.model.ILDIRepositoryLocation;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.ImageHyperlink;

class UpdateIUVersionAction {

    private static final String CONTROL_CONTRIBUTION_ID = "UpdateReferencedUnitVersions";
    private static final String TOOL_TIP_TEXT_ACTIVE = "Change all referenced unit versions to 0.0.0";
    private static final String UNSPECIFIED_VERSION = "0.0.0";
    private final LDITargetDefinitionProvider targetDefinitionProvider;

    UpdateIUVersionAction(final LDITargetDefinitionProvider targetDefinitionProvider) {
        this.targetDefinitionProvider = targetDefinitionProvider;
    }

    boolean containsSpecifiedVersion() {
        for (final ILDIRepositoryLocation repoLocation : targetDefinitionProvider.getLDITargetDefinition()
                .getRepositoryLocations()) {
            for (final IMutableVersionedId version : repoLocation.getUnits()) {
                if (!UNSPECIFIED_VERSION.equals(version.getVersion().toString())) {
                    return true;
                }
            }
        }
        return false;
    }

    void updateVersionsToUnspecified() {
        for (final ILDIRepositoryLocation repoLocation : targetDefinitionProvider.getLDITargetDefinition()
                .getRepositoryLocations()) {
            for (final IMutableVersionedId version : repoLocation.getUnits()) {
                if (!UNSPECIFIED_VERSION.equals(version.getVersion().toString())) {
                    version.setVersion(Version.create(UNSPECIFIED_VERSION));
                }
            }
        }
    }

    public void run() {
        updateVersionsToUnspecified();
    }

    static void createAndAssign(final LDITargetDefinitionProvider targetDefinitionProvider,
            final IManagedForm managedForm) {
        final UpdateIUVersionAction updateVersionReferencesAction = new UpdateIUVersionAction(targetDefinitionProvider);

        final ControlContribution updateVersionControlContribution = new ControlContribution(CONTROL_CONTRIBUTION_ID) {
            @Override
            protected Control createControl(final Composite parent) {
                final ImageHyperlink hyperlink = new ImageHyperlink(parent, SWT.NONE);
                hyperlink.setText(UNSPECIFIED_VERSION);
                hyperlink.setToolTipText(TOOL_TIP_TEXT_ACTIVE);
                hyperlink.setUnderlined(true);
                hyperlink.setForeground(managedForm.getToolkit().getHyperlinkGroup().getForeground());
                hyperlink.addHyperlinkListener(new IHyperlinkListener() {
                    @Override
                    public void linkActivated(final HyperlinkEvent e) {
                        updateVersionReferencesAction.run();
                    }

                    @Override
                    public void linkEntered(final HyperlinkEvent e) {
                        hyperlink.setForeground(managedForm.getToolkit().getHyperlinkGroup().getActiveForeground());
                    }

                    @Override
                    public void linkExited(final HyperlinkEvent e) {
                        hyperlink.setForeground(managedForm.getToolkit().getHyperlinkGroup().getForeground());
                    }
                });
                return hyperlink;
            }
        };

        managedForm.addPart(new AbstractFormPart() {
            @Override
            public void refresh() {
                final boolean isEnabled = updateVersionReferencesAction.containsSpecifiedVersion();
                if (isEnabled) {
                    final IContributionItem updateVersionItem = managedForm.getForm().getToolBarManager()
                            .find(CONTROL_CONTRIBUTION_ID);
                    if (updateVersionItem == null) {
                        final IContributionItem[] items = managedForm.getForm().getToolBarManager().getItems();
                        managedForm.getForm().getToolBarManager()
                                .insertBefore(items[0].getId(), updateVersionControlContribution);
                        managedForm.getForm().getToolBarManager().update(true);
                    }
                } else {
                    managedForm.getForm().getToolBarManager().remove(updateVersionControlContribution);
                    managedForm.getForm().getToolBarManager().update(true);
                }
            }

            @Override
            public boolean isStale() {
                return true;
            }
        });

    }
}
