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

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;

public class AddRepositoryWizardPO {

    final SWTBot bot;

    public AddRepositoryWizardPO(final SWTBot swtBot) {
        this.bot = swtBot;
    }

    public Boolean canFinish() {
        return getFinishButton().isEnabled();
    }

    public Boolean canCancel() {
        return getCancelButton().isEnabled();
    }

    public Boolean hasNext() {
        return getNextButton().isEnabled();
    }

    /**
     * The finish operation is assumed to be successful in case the wizard is closed after running
     * finish. In case an error occurs the wizard is kept open.
     * 
     * @return true if the wizard was closed after performing the finish operation.
     */
    public boolean performFinish() {
        final SWTBotButton cancelButton = getCancelButton();
        getFinishButton().click();
        bot.waitUntil(Conditions.widgetIsEnabled(cancelButton), 90000);
        // SWTBotButton isEnabled() method returns true in case the button widget is disposed.
        // so to check whether the dialog was closed we check the button widget dispose state now.
        if (cancelButton.widget.isDisposed()) {
            return true;
        } else {
            return false;
        }
    }

    public void performCancel() {
        getCancelButton().click();
    }

    public void close() {
        final SWTBotButton cancelButton = getCancelButton();
        if (cancelButton != null && !cancelButton.widget.isDisposed()) {
            cancelButton.click();
        }
    }

    void performNext() {
        getNextButton().click();
    }

    private SWTBotButton getFinishButton() {
        try {
            return bot.button(IDialogConstants.FINISH_LABEL);
        } catch (final WidgetNotFoundException e) {
            return null;
        }
    }

    private SWTBotButton getCancelButton() {
        try {
            return bot.button(IDialogConstants.CANCEL_LABEL);
        } catch (final WidgetNotFoundException e) {
            return null;
        }
    }

    private SWTBotButton getNextButton() {
        try {
            return bot.button(IDialogConstants.NEXT_LABEL);
        } catch (final WidgetNotFoundException e) {
            return null;
        }
    }
}
