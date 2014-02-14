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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

public class LDITargetEditorPO {
    private static final String LDI_TARGET_EDITOR_ID = "org.eclipse.tycho.targeteditor.LDITargetEditor";

    private final SWTBotEditor ldiEditor;

    public static LDITargetEditorPO open(final IFile targetFile) throws PartInitException {
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                final IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                final IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
                try {
                    IDE.openEditor(activePage, targetFile, LDI_TARGET_EDITOR_ID);
                } catch (final PartInitException e) {
                    Activator
                            .getDefault()
                            .getLog()
                            .log(new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(),
                                    "Unable to open Editor", e));
                }
            }
        });

        final SWTWorkbenchBot bot = new SWTWorkbenchBot();
        final SWTBotEditor ldiEditor = bot.editorByTitle(targetFile.getName());
        return new LDITargetEditorPO(ldiEditor);
    }

    private LDITargetEditorPO(final SWTBotEditor ldiEditor) {
        if (ldiEditor == null) {
            throw new IllegalArgumentException("Editor must not be null");
        }
        this.ldiEditor = ldiEditor;
    }

    public void close() {
        this.ldiEditor.close();
    }

    public RepositoriesEditorPagePO switchToRepositoriesPage() {
        final RepositoriesEditorPagePO repositoriesEditorPagePO = new RepositoriesEditorPagePO(ldiEditor.bot()
                .cTabItem(LDITargetEditor.REPOSITORIES_PAGE_TITLE));
        repositoriesEditorPagePO.activate();
        return repositoriesEditorPagePO;
    }

    public boolean isDirty() {
        return this.ldiEditor.isDirty();
    }

    public void save() {
        this.ldiEditor.save();
    }

    public LDITargetEditor getEclipseEditor() {
        return (LDITargetEditor) ldiEditor.getReference().getEditor(false);
    }

}
