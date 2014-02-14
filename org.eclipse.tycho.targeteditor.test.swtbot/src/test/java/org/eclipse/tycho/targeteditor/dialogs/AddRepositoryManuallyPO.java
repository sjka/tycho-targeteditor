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

import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
import org.eclipse.tycho.targeteditor.dialogs.AddRepositoryManuallyWizardPage;

public class AddRepositoryManuallyPO extends AddRepositoryWizardPO {

    public AddRepositoryManuallyPO(final SWTBot swtBot) {
        super(swtBot);
    }

    public String getGroupIdText() {
        return getGroupIdTextField().getText();
    }

    public void setGroupIdText(final String groupId) {
        getGroupIdTextField().setText(groupId);
    }

    public String getArtifactIdText() {
        return getArtifactIdTextField().getText();
    }

    public void setArtifactIdText(final String artifactId) {
        getArtifactIdTextField().setText(artifactId);
    }

    public String getVersionIdText() {
        return getVersionTextField().getText();
    }

    public void setVersionIdText(final String version) {
        getVersionTextField().setText(version);
    }

    public String getClassifierText() {
        return getClassifierTextField().getText();
    }

    public void setClassifierText(final String classifier) {
        getClassifierTextField().setText(classifier);
    }

    public String getFileExtensionText() {
        return getFileExtensionField().getText();
    }

    public boolean hasMessage(final String invalidVersionErrorMsg) {
        try {
            // Error message in the wizard has a space in front of it
            bot.text(" " + invalidVersionErrorMsg);
            return true;
        } catch (final WidgetNotFoundException e) {
            return false;
        }
    }

    private SWTBotText getClassifierTextField() {

        try {
            return bot.textWithLabel(AddRepositoryManuallyWizardPage.CLASSIFIER_LABEL);
        } catch (final WidgetNotFoundException e) {
            return null;
        }
    }

    private SWTBotText getGroupIdTextField() {
        return bot.textWithLabel(AddRepositoryManuallyWizardPage.GROUP_ID_LABEL);
    }

    private SWTBotText getArtifactIdTextField() {

        try {
            return bot.textWithLabel(AddRepositoryManuallyWizardPage.ARTIFACT_ID_LABEL);
        } catch (final WidgetNotFoundException e) {
            return null;
        }
    }

    private SWTBotText getVersionTextField() {

        try {
            return bot.textWithLabel(AddRepositoryManuallyWizardPage.VERSION_LABEL);
        } catch (final WidgetNotFoundException e) {
            return null;
        }
    }

    private SWTBotText getFileExtensionField() {
        try {
            return bot.textWithLabel(AddRepositoryManuallyWizardPage.FILE_EXTENSION_LABEL);
        } catch (final WidgetNotFoundException e) {
            return null;
        }
    }

}
