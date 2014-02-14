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
package org.eclipse.tycho.targeteditor.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.tycho.targeteditor.Activator;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class TargetEditorPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    public TargetEditorPreferencePage() {
        super(GRID);
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
        setDescription("Location of the configuration file which contains the "
                + "recommended repositories for the Tycho Target Editor");
    }

    @Override
    public void createFieldEditors() {
        addField(new URLFieldEditor(PreferenceConstants.P_RECOMMENDED_REPOSITORIES_URL, "&URL:", getFieldEditorParent()));
    }

    @Override
    public void init(final IWorkbench workbench) {
    }

}
