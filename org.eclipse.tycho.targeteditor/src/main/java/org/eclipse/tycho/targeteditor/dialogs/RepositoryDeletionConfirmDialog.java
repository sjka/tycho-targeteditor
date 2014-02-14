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

import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.tycho.targeteditor.RepositoryTableLabelProvider;
import org.eclipse.tycho.targeteditor.model.IRepository;

final class RepositoryDeletionConfirmDialog extends MessageDialog {
    private static final String DIALOG_TITLE = "Confirm Remove Repositories";
    private static final String DIALOG_MESSAGE = "This operation requires additional repositories to be deleted.";
    private static final String[] DIALOG_BUTTON_LABELS = new String[] { IDialogConstants.OK_LABEL,
            IDialogConstants.CANCEL_LABEL };
    private static final RepositoryTableLabelProvider LABEL_PROVIDER = new RepositoryTableLabelProvider();

    private final String repositoriesText;

    RepositoryDeletionConfirmDialog(final List<IRepository> selectedRepositories,
            final List<IRepository> extraDeletedRepositories) {
        super(null, DIALOG_TITLE, null, DIALOG_MESSAGE, MessageDialog.WARNING, DIALOG_BUTTON_LABELS, 0);
        final StringBuffer textBuffer = new StringBuffer();
        textBuffer.append("Repositories selected for deletion:");
        appendRepositories(textBuffer, selectedRepositories);
        textBuffer.append("\n");
        textBuffer.append("Repositories in the same location that also will be deleted:");
        appendRepositories(textBuffer, extraDeletedRepositories);
        // TODO publish additional help page
//        textBuffer.append("\n");
//        textBuffer.append("More information: ...wiki.../Remove+Repository+from+a+Location+with+many+Repositories");

        repositoriesText = textBuffer.toString();
    }

    @Override
    protected Control createCustomArea(final Composite comp) {
        setShellStyle(getShellStyle() | SWT.RESIZE);
        final Composite parent = new Composite(comp, 0);
        final GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.marginLeft = 2;
        layout.numColumns = 1;
        layout.verticalSpacing = 9;
        parent.setLayout(layout);
        parent.setLayoutData(new GridData(GridData.FILL_BOTH));

        final Text text = new Text(parent, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
        text.setText(repositoriesText);
        final GridData data = new GridData(640, 300);
        text.setLayoutData(data);
        return parent;
    }

    boolean openConfirm() {
        return open() == 0;
    }

    private void appendRepositories(final StringBuffer textBuffer, final List<IRepository> repositories) {
        textBuffer.append("\n");
        for (final IRepository repository : repositories) {
            appendRepository(textBuffer, repository);
        }
    }

    private void appendRepository(final StringBuffer text, final IRepository repository) {
        text.append(LABEL_PROVIDER.getText(repository));
        text.append("     ");
        text.append(repository.getURI());
        text.append("\n");
    }

}
