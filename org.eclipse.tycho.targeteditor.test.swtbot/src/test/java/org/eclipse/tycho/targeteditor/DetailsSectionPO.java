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

import org.eclipse.swtbot.forms.finder.SWTFormsBot;
import org.eclipse.swtbot.forms.finder.widgets.SWTBotHyperlink;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCombo;
import org.eclipse.tycho.targeteditor.model.EDynamicVersions;

public class DetailsSectionPO {

    private final SWTBot swtBot;

    public DetailsSectionPO(final SWTBot swtBot) {
        this.swtBot = swtBot;
    }

    public boolean hasVersionCombo() {
        return getVersionCombo() != null;
    }

    public void selectLatestVersion() {
        final SWTBotCombo versionCombo = getVersionCombo();
        final String[] availableVersions = getAvailableVersions();
        for (final String availableVersion : availableVersions) {
            if (!EDynamicVersions.isDynamicVersion(availableVersion)) {
                versionCombo.setSelection(availableVersion);
                break;
            }
        }
    }

    public void selectLatestReleasedVersion() {
        final String[] availableVersions = getAvailableVersions();
        for (int i = 0; i < availableVersions.length; i++) {
            if (!availableVersions[i].endsWith("-SNAPSHOT") && !EDynamicVersions.isDynamicVersion(availableVersions[i])) {
                getVersionCombo().setSelection(i);
                break;
            }
        }
    }

    public void selectVersion(final String version) {
        getVersionCombo().setSelection(version);
    }

    public String[] getAvailableVersions() {
        final SWTBotCombo versionCombo = getVersionCombo();
        if (versionCombo != null) {
            return versionCombo.items();
        }
        return null;
    }

    private SWTBotCombo getVersionCombo() {
        try {
            return swtBot.comboBox();
        } catch (final WidgetNotFoundException e) {
            return null;
        }
    }

    public String getRepositoryUrl() {
        final SWTBotHyperlink hyperlink = new SWTFormsBot(swtBot.getFinder()).hyperlink();
        return hyperlink.getText();
    }
}
