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

import java.io.Closeable;

import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
import org.eclipse.ui.PartInitException;

public class PreferencePagePO implements Closeable {
    private final SWTBot bot;

    /**
     * see {@link #close()}
     */
    private SWTBotButton cancelButton;

    public PreferencePagePO(final SWTBot dialogBot) {
        this.bot = dialogBot;
    }

    public static PreferencePagePO open() throws PartInitException {
        final SWTBot bot = new SWTBot();
        bot.menu("Window").menu("Preferences").click();
        final SWTBotShell preferencesDialogShell = bot.activeShell();
        final SWTBot dialogBot = preferencesDialogShell.bot();
        dialogBot.tree().select("Tycho Target Editor");
        final PreferencePagePO preferencePagePO = new PreferencePagePO(dialogBot);
        if (!preferencePagePO.isActive()) {
            throw new IllegalStateException("Preference page not open");
        }
        return preferencePagePO;
    }

    public boolean isActive() {
        return bot.shell("Preferences").isActive();
    }

    public void setUrl(final String url) {
        getUrlTextField().setText(url);
    }

    public String getUrl() {
        return getUrlTextField().getText();
    }

    public boolean canValidate() {
        try {
            bot.button("Validate");
            return true;
        } catch (final WidgetNotFoundException e) {
            return false;
        }
    }

    public String validationResult() {
        bot.button("Validate").click();
        final SWTBot dialogBot = bot.shell("Validation").bot();
        final String text = dialogBot.label(1).getText();
        dialogBot.button("OK").click();
        return text;
    }

    public void saveAndClose() {
        // comment in getCancelButton()
        getCancelButton();
        bot.button("OK").click();
    }

    /*
     * Close kindly. Does nothing in case the page is already closed.
     */
    @Override
    public void close() {
        final SWTBotButton button = getCancelButton();
        if (!button.widget.isDisposed()) {
            button.click();
        }
    }

    /**
     * caching cancel button for performance tuning. {@link #close()} can be called by cleanup tasks
     * even in case the page is already closed. In case the page is already closed (probably via
     * {@link PreferencePagePO#saveAndClose()}) the method will block SWT timeout.
     * 
     * @return lazy cached Canel button
     */
    private SWTBotButton getCancelButton() {
        if (cancelButton == null) {
            cancelButton = bot.button("Cancel");
        }
        return cancelButton;
    }

    private SWTBotText getUrlTextField() {
        try {
            final SWTBotText urlTextField = bot.textWithLabel("URL:");
            return urlTextField;
        } catch (final WidgetNotFoundException e) {
            return null;
        }
    }

}
