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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.pde.internal.ui.PDEPluginImages;
import org.eclipse.pde.internal.ui.editor.targetdefinition.TargetEditor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.forms.widgets.ScrolledForm;

@SuppressWarnings("restriction")
public class LDITargetEditor extends TargetEditor {
    static final String OPEN_IN_TEXT_EDITOR_TOOLBAR_ID = "OpenInTextEditor";
    static final String REPOSITORIES_PAGE_TITLE = "Repositories";
    private RepositoriesEditorPage repositoriesEditorPage;
    static final String REFRESH_ALL_ACTION_ID = "RefreshAllCacheAction";
    static final String REFRESH_ALL_ACTION_TOOL_TIP_TEXT = "Refresh all caches";

    /**
     * Creates the pages of the multi-page editor.
     */
    @Override
    protected void addPages() {
        try {
            repositoriesEditorPage = new RepositoriesEditorPage(this, "Repositories.id", "Repositories");
            this.addPage(repositoriesEditorPage);
        } catch (final PartInitException e) {
            ErrorDialog.openError(getSite().getShell(), "Error attaching 'Repositories' page", null, e.getStatus());
        }
        super.addPages();
    }

    @Override
    protected void pageChange(final int newPageIndex) {
        super.pageChange(newPageIndex);
        // Workaround for bug in dirty state handling of
        // org.eclipse.pde.internal.ui.editor.targetdefinition.DefinitionPage ->
        // org.eclipse.pde.internal.ui.editor.targetdefinition.InformationSection
        // The editor will be marked dirty the first time the Definition page will be activated.
        // Switching to any other page afterwards removes dirty flag.
        // Bug only occurs in case the the DefinitionPage is not the first page, but activated later.
        // With introducing our Repository page as first page the problem occured.
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                LDITargetEditor.this.editorDirtyStateChanged();
            }
        });
    }

    @Override
    public void contributeToToolbar(final ScrolledForm form, final String contextID) {
        final IAction openTextEditorAction = createOpenInTextEditorAction();
        form.getToolBarManager().add(openTextEditorAction);

        final IAction refreshAllCaches = createRefreshAllCachesAction();
        form.getToolBarManager().add(refreshAllCaches);

        super.contributeToToolbar(form, contextID);
    }

    private IAction createRefreshAllCachesAction() {
        final IAction refreshAllCaches = new Action("Refresh all caches") {
            @Override
            public void run() {
                new RefreshAllCacheAction().runRefreshCacheJob(repositoriesEditorPage);
            }
        };
        refreshAllCaches.setToolTipText(LDITargetEditor.REFRESH_ALL_ACTION_TOOL_TIP_TEXT);
        refreshAllCaches.setImageDescriptor(PDEPluginImages.DESC_REFRESH);
        refreshAllCaches.setId(LDITargetEditor.REFRESH_ALL_ACTION_ID);
        return refreshAllCaches;
    }

    private IAction createOpenInTextEditorAction() {
        final IAction openTextEditorAction = new Action("Open in text editor") { //$NON-NLS-1$
            @Override
            public void run() {
                try {
                    if (isDirty()) {
                        final boolean doSave = MessageDialog
                                .openConfirm(getEditorSite().getShell(), "Save request",
                                        "The current editor is dirty. You need to save before switching to text editor.\n\n Would you like to save now?");
                        if (doSave) {
                            doSave(new NullProgressMonitor());
                        } else {
                            return;
                        }
                    }
                    getEditorSite().getPage().openEditor(getEditorInput(), EditorsUI.DEFAULT_TEXT_EDITOR_ID, true, 3);

                } catch (final PartInitException e) {
                    Activator.getDefault().getLog()
                            .log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Unable to open text editor", e)); //$NON-NLS-1$
                }
            }
        };
        openTextEditorAction.setToolTipText("Open in text editor");
        openTextEditorAction.setImageDescriptor(PDEPluginImages.DESC_VALIDATE_TOOL);
        openTextEditorAction.setId(OPEN_IN_TEXT_EDITOR_TOOLBAR_ID);
        return openTextEditorAction;
    }

    @Override
    public void doRevert() {
        super.doRevert();
        repositoriesEditorPage.doRevert();
    }

}
