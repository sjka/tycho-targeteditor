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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tycho.targeteditor.Activator;
import org.eclipse.tycho.targeteditor.jobs.RecommendedRepositoriesReaderJob;
import org.eclipse.tycho.targeteditor.model.INexusRepository;
import org.eclipse.tycho.targeteditor.preferences.PreferenceConstants;
import org.eclipse.tycho.targeteditor.xml.Repository;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferenceLinkArea;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.forms.ManagedForm;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;
import org.eclipse.ui.preferences.IWorkingCopyManager;

public final class AddRepositorySelectOptionWizardPage extends WizardPage implements ISelectionChangedListener {

    private static final class RepositoryUrlChangeListener implements IPropertyChangeListener {
        private AddRepositorySelectOptionWizardPage page;

        @Override
        public void propertyChange(final PropertyChangeEvent event) {
            if (PreferenceConstants.P_RECOMMENDED_REPOSITORIES_URL.equals(event.getProperty())) {
                page.setFilterTableInput();
            }
        }

        public void setPage(final AddRepositorySelectOptionWizardPage page) {
            this.page = page;
        }
    }

    private static final String MANUALLY_ADD_REPO = "Specify GAV manually or from Clipboard";
    private static final String RECOMMENDED_REPOSITORY = "Recommended Repository";
    private static final String PAGE_TITLE = "Select method for adding a Repository";
    private static final RepositoryUrlChangeListener URL_LISTENER = new RepositoryUrlChangeListener();

    private Button addRecommendedRepositoryOption;

    private RecommendedReposTable repositoryTable;

    private Button addRepositoryManuallyOption;

    protected AddRepositorySelectOptionWizardPage() {
        super(PAGE_TITLE, PAGE_TITLE, null);
        setMessage("Select one of the options below in order to add a repository.");
    }

    @Override
    public void createControl(final Composite parent) {

        final Composite container = new Composite(parent, SWT.NONE);
        final GridLayout masterLayout = new GridLayout(2, false);
        container.setLayout(masterLayout);

        final GridDataFactory defaultGridData = GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.BEGINNING)
                .grab(true, false);

        addRecommendedRepositoryOption = new Button(container, SWT.RADIO);
        addRecommendedRepositoryOption.setText(RECOMMENDED_REPOSITORY);
        defaultGridData.applyTo(addRecommendedRepositoryOption);
        addRecommendedRepositoryOption.addSelectionListener(getRadioButtonListener());

        createPreferencePageLink(container);

        repositoryTable = new RecommendedReposTable(this, container, new RecommendedRepositoriesContentProvider(),
                new RecommendedRepositoriesLabelProvider());
        final GridData gridData = GridDataFactory.swtDefaults().create();
        gridData.horizontalSpan = 2;
        repositoryTable.setLayoutData(gridData);
        addRepositoryManuallyOption = new Button(container, SWT.RADIO);
        addRepositoryManuallyOption.setText(MANUALLY_ADD_REPO);
        defaultGridData.applyTo(addRepositoryManuallyOption);
        addRepositoryManuallyOption.addSelectionListener(getRadioButtonListener());

        setControl(container);
        setFilterTableInput();
        registerUrlListener();

        calculateNextEnabled();
    }

    private void registerUrlListener() {
        final IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
        preferenceStore.removePropertyChangeListener(URL_LISTENER);
        URL_LISTENER.setPage(this);
        preferenceStore.addPropertyChangeListener(URL_LISTENER);
    }

    private SelectionListener getRadioButtonListener() {
        return new SelectionListener() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                final Button radioButton = (Button) e.getSource();
                if (radioButton.getSelection()) {
                    if (radioButton.getText().equals(RECOMMENDED_REPOSITORY)) {
                        repositoryTable.setEnabled(true);
                        setRepository(repositoryTable.getSelection());
                    } else {
                        repositoryTable.setEnabled(false);
                        setRepository(null);
                    }
                    calculateNextEnabled();
                }
            }

            @Override
            public void widgetDefaultSelected(final SelectionEvent e) {
            }
        };
    }

    private void setFilterTableInput() {
        repositoryTable.setInput(Arrays.asList(Repository.RESOLUTION_PENDING));
        final IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
        final String url = preferenceStore.getString(PreferenceConstants.P_RECOMMENDED_REPOSITORIES_URL);
        final RecommendedRepositoriesReaderJob job = new RecommendedRepositoriesReaderJob(url);
        job.addJobChangeListener(new JobChangeAdapter() {
            @Override
            public void done(final IJobChangeEvent event) {
                if (event.getResult().isOK()) {
                    final IStatus jobStatus = job.getRealStatus();
                    Display.getDefault().asyncExec(new Runnable() {
                        @Override
                        public void run() {
                            if (jobStatus.isOK()) {
                                setErrorMessage(null);
                                final List<Repository> repositories = job.getRepositories();
                                repositoryTable.setInput(repositories);
                            } else {
                                setErrorMessage(jobStatus.getMessage());
                                repositoryTable.setInput(Collections.emptyList());
                            }
                        }
                    });
                }
            }
        });
        job.schedule();
    }

    private void createPreferencePageLink(final Composite container) {
        final GridDataFactory linkGridDataFactory = GridDataFactory.swtDefaults().align(SWT.END, SWT.END)
                .grab(true, false);
        final PreferenceLinkArea pla = new PreferenceLinkArea(container, SWT.NONE,
                "org.eclipse.tycho.targeteditor.preferences.TargetEditorPreferencePage", "<a>Configure ...</a>",
                new IWorkbenchPreferenceContainer() {

                    @Override
                    public void registerUpdateJob(final Job job) {
                    }

                    @Override
                    public boolean openPage(final String preferencePageId, final Object data) {
                        return true;
                    }

                    @Override
                    public IWorkingCopyManager getWorkingCopyManager() {
                        return null;
                    }
                }, null);
        linkGridDataFactory.applyTo(pla.getControl());
        pla.getControl().setToolTipText("Configure recommended list of repositories in the Preferences page");
        pla.getControl().addMouseListener(new MouseListener() {

            @Override
            public void mouseUp(final MouseEvent e) {
            }

            @Override
            public void mouseDown(final MouseEvent e) {
                PreferencesUtil.createPreferenceDialogOn(getShell(),
                        "org.eclipse.tycho.targeteditor.preferences.TargetEditorPreferencePage", null, null).open();
            }

            @Override
            public void mouseDoubleClick(final MouseEvent e) {
            }
        });
    }

    protected ManagedForm createManagedForm(final Composite parent) {
        final ManagedForm managedForm = new ManagedForm(parent);
        return managedForm;
    }

    @Override
    public void performHelp() {
        PlatformUI.getWorkbench().getHelpSystem().displayHelp(AddRepositoryWizard.ADD_REPOSITORY_HELP_ID);
    }

    @Override
    public void selectionChanged(final SelectionChangedEvent event) {
        final IStructuredSelection selection = (IStructuredSelection) event.getSelection();
        final Repository selectedRepository = (Repository) selection.getFirstElement();
        if (selectedRepository == Repository.RESOLUTION_PENDING) {
            return;
        }
        setRepository(selectedRepository);
        if (selectedRepository != null) {
            repositoryTable.setDescriptionText(selectedRepository.getDescription());
        } else {
            repositoryTable.setDescriptionText("");
        }
        calculateNextEnabled();
    }

    private void setRepository(final Repository selectedRepository) {
        ((AddRepositoryWizard) getWizard()).setRepository(selectedRepository);
    }

    void calculateNextEnabled() {
        setPageComplete(addRepositoryManuallyOption.getSelection()
                || (addRecommendedRepositoryOption.getSelection() && repositoryTable.getRepositoryTable()
                        .getSelectionCount() > 0));
    }

    @Override
    public IWizardPage getNextPage() {
        final AddRepositoryManuallyWizardPage nextPage = (AddRepositoryManuallyWizardPage) super.getNextPage();
        if (addRepositoryManuallyOption.getSelection()) {
            presetRepositoryGavFromClipboard();
        }
        setAddRepoManuallyPageContents(nextPage);
        return nextPage;
    }

    private void setAddRepoManuallyPageContents(final AddRepositoryManuallyWizardPage addRepoManuallyPage) {
        final Repository repository = ((AddRepositoryWizard) getWizard()).getRepository();
        if (repository != null) {
            addRepoManuallyPage.setGroupId(repository.getGroupId());
            addRepoManuallyPage.setArtifactIdInput(repository.getArtifactId());
            addRepoManuallyPage.setVersionInput(repository.getVersion());

            final String classifier = repository.getClassifier();
            if (classifier != null) {
                addRepoManuallyPage.setClassifierInput(classifier);
            } else {
                addRepoManuallyPage.setClassifierInput("");
            }
            addRepoManuallyPage.setFileExtension(repository.getExtension());
        } else {
            addRepoManuallyPage.reset();
        }
    }

    void presetRepositoryGavFromClipboard() {
        final ClipboardAccessor clipboardAccessor = new ClipboardAccessor();
        if (clipboardAccessor.hasRepository()) {
            final INexusRepository presetRepo = clipboardAccessor.getRepository();
            setRepository(new Repository("Clipboard", presetRepo.getGroupId(), presetRepo.getArtifactId(),
                    presetRepo.getVersion(), presetRepo.getClassifier(), "", "zip"));
        }
    }

}
