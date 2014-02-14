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

import org.eclipse.swt.SWT;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.utils.SWTUtils;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotLink;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotRadio;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
import org.eclipse.tycho.targeteditor.PreferencePagePO;
import org.eclipse.tycho.targeteditor.dialogs.RecommendedReposTable;

public class AddRepositorySelectOptionsPO extends AddRepositoryWizardPO {

    private static final String PREFERENCES = "Preferences";

    private static final String RECOMMENDED_REPOSITORY = "Recommended Repository";
    private static final String MANUALLY_ADD_REPO = "Specify GAV manually or from Clipboard";

    public AddRepositorySelectOptionsPO(final SWTBot swtBot) {
        super(swtBot);
    }

    public AddRepositoryManuallyPO moveToAddRepoManuallyPage() throws InterruptedException {
        getAddRepoManuallyOptionRadioButton().click();
        performNext();
        return new AddRepositoryManuallyPO(bot);
    }

    public void selectRecommendedRepoOption() {
        getAddRecommendedRepoOptionRadioButton().click();
    }

    public void selectManuallyOption() {
        getAddRepoManuallyOptionRadioButton().click();
    }

    public boolean isRecommendedRepoOptionSelected() {
        return getAddRecommendedRepoOptionRadioButton().isSelected();
    }

    public boolean isManuallyOptionSelected() {
        return getAddRepoManuallyOptionRadioButton().isSelected();
    }

    public boolean isRecommendedRepoOptionGroupEnabled() {
        final boolean enabled = getFilterTable().isEnabled();
        final boolean enabled2 = getFilterText().isEnabled();
        final boolean enabled3 = getDescriptionField().isEnabled();
        if (enabled == enabled2 && enabled == enabled3) {
            return enabled;
        }
        throw new IllegalStateException("Inhomogenous enable state");
    }

    public PreferencePagePO openPreferencePage() {
        getConfigureLink().click();
        final PreferencePagePO preferencePagePO = new PreferencePagePO(bot.shell(PREFERENCES).activate().bot());
        if (!preferencePagePO.isActive()) {
            throw new IllegalStateException("Preference page not open");
        }
        return preferencePagePO;
    }

    public String getDescriptionText() {
        return bot.textWithLabel(RecommendedReposTable.DESCRIPTION_LABEL).getText();
    }

    public void selectFirstRecommendedRepository() {
        getFilterTable().getTableItem(0).select();
    }

    public void selectRepository(final String text) {
        final SWTBotTable table = getFilterTable();
        for (int i = 0; i < table.rowCount(); i++) {
            if (table.cell(i, 0).equals(text)) {
                table.getTableItem(i).select();
                return;
            }
        }
    }

    public void waitForRepositoriesLoaded() {
        bot.waitUntil(new DefaultCondition() {
            @Override
            public boolean test() throws Exception {
                final SWTBotTable filterTable = getFilterTable();
                if (filterTable.rowCount() != 1) {
                    return true;
                }
                final String text = filterTable.getTableItem(0).getText(0);
                return !text.equals("Reading recommended repositories...");
            }

            @Override
            public String getFailureMessage() {
                return "Filter table did not load in time";
            }
        });
    }

    public int getRecommendedRepositoryCount() {
        return getFilterTable().rowCount();
    }

    public boolean canSetRecommendedRepoDescription() {
        return !SWTUtils.hasStyle(getDescriptionField().widget, SWT.READ_ONLY);
    }

    private SWTBotTable getFilterTable() {
        try {
            return bot.table();
        } catch (final WidgetNotFoundException e) {
            return null;
        }
    }

    private SWTBotLink getConfigureLink() {
        try {
            return bot.link();
        } catch (final WidgetNotFoundException e) {
            return null;
        }
    }

    private SWTBotText getFilterText() {
        try {
            return bot.textWithId(RecommendedReposTable.FILTER_TEXT_ID);
        } catch (final WidgetNotFoundException e) {
            return null;
        }
    }

    private SWTBotRadio getAddRecommendedRepoOptionRadioButton() {
        try {
            return bot.radio(RECOMMENDED_REPOSITORY);
        } catch (final WidgetNotFoundException e) {
            return null;
        }
    }

    private SWTBotRadio getAddRepoManuallyOptionRadioButton() {
        try {
            return bot.radio(MANUALLY_ADD_REPO);
        } catch (final WidgetNotFoundException e) {
            return null;
        }
    }

    private SWTBotText getDescriptionField() {
        try {
            return bot.textWithLabel(RecommendedReposTable.DESCRIPTION_LABEL);
        } catch (final WidgetNotFoundException e) {
            return null;
        }
    }

}
