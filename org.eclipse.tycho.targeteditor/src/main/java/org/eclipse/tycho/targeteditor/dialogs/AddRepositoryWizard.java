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

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.tycho.targeteditor.LDITargetDefinitionProvider;
import org.eclipse.tycho.targeteditor.xml.Repository;
import org.eclipse.ui.forms.IManagedForm;

public class AddRepositoryWizard extends Wizard {
    protected static final String ADD_REPOSITORY_HELP_ID = "org.eclipse.tycho.targeteditor.addRepository";
    private final AddRepositoryManuallyWizardPage addRepoManuallyWizardPage;
    private final AddRepositorySelectOptionWizardPage addRepoOptionsPage;
    private Repository repository;
    public static final String WIZARD_TITLE = "Add a Repository";

    public AddRepositoryWizard(final IManagedForm managedForm,
            final LDITargetDefinitionProvider targetDefinitionProvider) {
        super();
        addRepoManuallyWizardPage = new AddRepositoryManuallyWizardPage(targetDefinitionProvider);
        addRepoOptionsPage = new AddRepositorySelectOptionWizardPage();
    }

    @Override
    public void addPages() {
        setWindowTitle(WIZARD_TITLE);
        setNeedsProgressMonitor(true);
        addPage(addRepoOptionsPage);
        addPage(addRepoManuallyWizardPage);
    }

    @Override
    public boolean performFinish() {
        if (canFinish()) {
            return addRepoManuallyWizardPage.performFinish();
        }
        return false;
    }

    @Override
    public boolean canFinish() {
        return getRepository() != null;
    }

    public Repository getRepository() {
        return repository;
    }

    public void setRepository(final Repository repository) {
        this.repository = repository;
    }
}
