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

import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.allOf;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.widgetOfType;
import static org.eclipse.swtbot.swt.finder.waits.Conditions.tableHasRows;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.results.ArrayResult;
import org.eclipse.swtbot.swt.finder.utils.MessageFormat;
import org.eclipse.swtbot.swt.finder.utils.SWTUtils;
import org.eclipse.swtbot.swt.finder.widgets.AbstractSWTBotControl;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCTabItem;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarButton;
import org.eclipse.tycho.targeteditor.FilterTable;
import org.eclipse.tycho.targeteditor.LDITargetEditor;
import org.eclipse.tycho.targeteditor.RepositoryMainForm;
import org.eclipse.tycho.targeteditor.ShowResolutionProblemAction;
import org.eclipse.tycho.targeteditor.UpdateRepositoryVersionForm;
import org.eclipse.tycho.targeteditor.dialogs.AddRepositorySelectOptionsPO;
import org.eclipse.tycho.targeteditor.dialogs.RemoveRepositoryForm;
import org.eclipse.tycho.targeteditor.model.IRepository;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.hamcrest.SelfDescribing;

public class RepositoriesEditorPagePO {

    private static final String REMOVE_SHELL_TITLE = "Confirm Remove Repositories";

    /**
     * inspired by org.eclipse.swtbot.swt.finder.widgets.SWTBotButton
     */
    private class SWTBotHyperlink extends AbstractSWTBotControl<Hyperlink> {

        /**
         * Constructs an instance of this object with the given hyperlink
         * 
         * @param hyperlink
         *            the widget.
         * @throws WidgetNotFoundException
         *             if the widget is <code>null</code> or widget has been disposed.
         */
        public SWTBotHyperlink(final Hyperlink hyperlink) {
            this(hyperlink, null);
        }

        /**
         * Constructs an instance of this object with the given hyperlink
         * 
         * @param hyperlink
         *            the widget.
         * @param description
         *            the description of the widget, this will be reported by {@link #toString()}
         * @throws WidgetNotFoundException
         *             if the widget is <code>null</code> or widget has been disposed.
         */
        public SWTBotHyperlink(final Hyperlink hyperlink, final SelfDescribing description) {
            super(hyperlink, description);
        }

        /**
         * Click on the hyperlink.
         */
        @Override
        public SWTBotHyperlink click() {
            log.debug(MessageFormat.format("Clicking on {0}", SWTUtils.getText(widget))); //$NON-NLS-1$
            waitForEnabled();
            notify(SWT.MouseEnter);
            notify(SWT.MouseMove);
            notify(SWT.Activate);
            notify(SWT.FocusIn);
            notify(SWT.MouseDown, createMouseEvent(0, 0, 1, 0, 1));
            notify(SWT.MouseUp, createMouseEvent(0, 0, 1, 0, 1));
            notify(SWT.Selection);
            notify(SWT.MouseHover);
            notify(SWT.MouseMove);
            notify(SWT.MouseExit);
            notify(SWT.Deactivate);
            notify(SWT.FocusOut);
            log.debug(MessageFormat.format("Clicked on {0}", SWTUtils.getText(widget))); //$NON-NLS-1$
            return this;
        }

    }

    private static final String DETAILS_SECTION_LABEL = "Details";
    private final SWTBotCTabItem repositoriesTabItem;

    public RepositoriesEditorPagePO(final SWTBotCTabItem cTabItem) {
        repositoriesTabItem = cTabItem;
    }

    void activate() {
        repositoriesTabItem.activate();
    }

    public List<IRepository> getRepositories() {
        final List<IRepository> ret = new ArrayList<IRepository>();
        for (final Object data : getTableItemData()) {
            if (data instanceof IRepository) {
                ret.add((IRepository) data);
            }
        }
        return ret;
    }

    public void select(final IRepository repository) {
        final List<Object> dataList = getTableItemData();
        for (int i = 0; i < dataList.size(); i++) {
            if (dataList.get(i).equals(repository)) {
                bot().table().select(i);
                break;
            }
        }
    }

    /**
     * @return index of repository in table if found, -1 otherwise
     */
    public int getIndexOfRepositoryWithURIPart(final String partOfTheURI) throws Exception {
        final List<IRepository> repositories = getRepositories();
        for (int i = 0; i < repositories.size(); i++) {
            if (repositories.get(i).getURI().toString().contains(partOfTheURI)) {
                return i;
            }
        }
        return -1;
    }

    public void select(final int... indexes) {
        bot().table().select(indexes);
    }

    public int getNumberOfSelections() {
        return bot().table().selectionCount();
    }

    private List<Object> getTableItemData() {
        final List<Object> ret = new ArrayList<Object>();
        Display.getDefault().syncExec(new Runnable() {
            @Override
            public void run() {
                final TableItem[] items = bot().table().widget.getItems();
                for (int i = 0; i < items.length; i++) {
                    final Object data = items[i].getData();
                    if (data instanceof IRepository) {
                        ret.add(data);
                    }
                }
            }
        });
        return ret;
    }

    private SWTBot bot() {
        return new SWTBot(repositoriesTabItem.widget);
    }

    public DetailsSectionPO getDetailsSection() {
        final Composite[] detailsSectionComposite = new Composite[1];
        Display.getDefault().syncExec(new Runnable() {
            @Override
            public void run() {
                detailsSectionComposite[0] = bot().label(DETAILS_SECTION_LABEL).widget.getParent();
            }
        });

        return new DetailsSectionPO(new SWTBot(detailsSectionComposite[0]));
    }

    public boolean hasUpdateVersionAction() {
        final SWTBotHyperlink updateVersionButton = getUpdateVersionButton();
        return updateVersionButton != null && updateVersionButton.isEnabled();
    }

    public void performUpdateVersionAction() {
        final SWTBotHyperlink updateVersionActionButton = getUpdateVersionButton();
        if (updateVersionActionButton != null) {
            updateVersionActionButton.click();
        }
    }

    public void performRefreshAllCachesAction() {
        final SWTBotToolbarButton refreshAllCachesButton = bot().toolbarButtonWithTooltip(
                LDITargetEditor.REFRESH_ALL_ACTION_TOOL_TIP_TEXT);
        refreshAllCachesButton.click();
    }

    public boolean hasShowProblemDetailsAction() {
        return getShowProblemDetailsAction() != null;
    }

    public void performShowProblemDetailsAction() {
        getShowProblemDetailsAction().click();
    }

    private SWTBotToolbarButton getShowProblemDetailsAction() {
        try {
            return bot().toolbarButtonWithTooltip(ShowResolutionProblemAction.TOOL_TIP_TEXT);
        } catch (final WidgetNotFoundException e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private SWTBotHyperlink getUpdateVersionButton() {
        // assume that in case the one of the two left tool item is a hyperlink it's the update version action
        // Assumed order (ShowResolutionErrorDetails:optional - UpdateVersion:optional - ...)
        final List<ToolItem> findControls = bot().getFinder().findControls(allOf(widgetOfType(ToolItem.class)));
        final Control[] toolItemControl = UIThreadRunnable.syncExec(findControls.get(0).getDisplay(),
                new ArrayResult<Control>() {
                    @Override
                    public Control[] run() {
                        final List<Control> result = new ArrayList<Control>(findControls.size());
                        for (final ToolItem toolItem : findControls) {
                            result.add(toolItem.getControl());
                        }
                        return result.toArray(new Control[result.size()]);
                    }
                });

        if (toolItemControl != null) {
            for (int i = 0; i < 2; i++) {
                if (toolItemControl[i] instanceof Hyperlink) {
                    return new SWTBotHyperlink((Hyperlink) toolItemControl[i]);
                }
            }
        }
        return null;
    }

    public boolean hasUpdateLatestReleaseAction() {
        final SWTBotButton updateVersionButton = getUpdateRepositoryVersionButton();
        return updateVersionButton != null && updateVersionButton.isEnabled();
    }

    public void performUpdateRepositoryVersionAction() {
        final SWTBotButton updateLatestReleaseButton = getUpdateRepositoryVersionButton();
        if (updateLatestReleaseButton != null) {
            updateLatestReleaseButton.click();
        }
    }

    private SWTBotButton getUpdateRepositoryVersionButton() {
        try {
            return bot().button(UpdateRepositoryVersionForm.USE_LATEST_RELEASE_TXT);
        } catch (final WidgetNotFoundException e) {
            return null;
        }
    }

    public boolean hasAddRepositoryAction() {
        final SWTBotButton addRepositoryButton = getAddRepositoryButton();
        return addRepositoryButton != null && addRepositoryButton.isEnabled();
    }

    public boolean hasRemoveRepositoryAction() {
        final SWTBotButton removeRepositoryButton = getRemoveRepositoryButton();
        return removeRepositoryButton != null && removeRepositoryButton.isEnabled();
    }

    private SWTBotButton getAddRepositoryButton() {
        try {
            return bot().button(RepositoryMainForm.ADD_REPOSITORY);
        } catch (final WidgetNotFoundException e) {
            return null;
        }
    }

    private SWTBotButton getRemoveRepositoryButton() {
        try {
            return bot().button(RemoveRepositoryForm.REMOVE_REPOSITORY);
        } catch (final WidgetNotFoundException e) {
            return null;
        }
    }

    public void performRemoveRepositoryAction() {
        getRemoveRepositoryButton().click();
    }

    public AddRepositorySelectOptionsPO getAddRepositoryWizard() {
        getAddRepositoryButton().click();
        return new AddRepositorySelectOptionsPO(bot().activeShell().activate().bot());

    }

    public boolean showsWarningDialog() {
        return bot().activeShell().getText().equals(REMOVE_SHELL_TITLE);
    }

    public void confirmRemoveRepositories() {
        clickButtonInConfirmShell("OK");
    }

    public void cancelRemoveRepositories() {
        clickButtonInConfirmShell("Cancel");
    }

    private void clickButtonInConfirmShell(final String label) {
        final SWTBotShell confirmShell = bot().shell(REMOVE_SHELL_TITLE);
        confirmShell.bot().button(label).click();
    }

    public void waitForRepositories(final int rowCount) {
        bot().waitUntil(tableHasRows(bot().table(), rowCount));
    }

    public SWTBotTable getFilterTable() {
        try {
            return bot().table();
        } catch (final WidgetNotFoundException e) {
            return null;
        }
    }

    public SWTBotText getFilterText() {
        try {
            return bot().textWithId(FilterTable.FILTER_TEXT_ID);
        } catch (final WidgetNotFoundException e) {
            return null;
        }
    }

}
