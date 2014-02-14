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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.equinox.p2.core.ProvisionException;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.metadata.Version;
import org.eclipse.equinox.p2.query.IQuery;
import org.eclipse.equinox.p2.query.IQueryResult;
import org.eclipse.equinox.p2.query.QueryUtil;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepository;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.pde.internal.core.target.P2TargetUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.tycho.targeteditor.LDITargetDefinitionProvider;
import org.eclipse.tycho.targeteditor.model.ILDIRepositoryLocation;
import org.eclipse.tycho.targeteditor.model.IMutableVersionedId;
import org.eclipse.tycho.targeteditor.model.INexusRepository;
import org.eclipse.tycho.targeteditor.model.INexusRepositoryService;
import org.eclipse.tycho.targeteditor.model.INexusVersionInfo;
import org.eclipse.tycho.targeteditor.model.IRepository;
import org.eclipse.tycho.targeteditor.model.ILDITargetDefintion;
import org.eclipse.tycho.targeteditor.model.NexusRepositoryNames;
import org.eclipse.tycho.targeteditor.model.LDIModelFactory;
import org.eclipse.tycho.targeteditor.xml.Repository;
import org.eclipse.ui.PlatformUI;

@SuppressWarnings("restriction")
public final class AddRepositoryManuallyWizardPage extends WizardPage {

    static final String GROUP_ID_LABEL = "Group ID:";
    static final String ARTIFACT_ID_LABEL = "Artifact ID:";
    static final String VERSION_LABEL = "Version:";
    static final String CLASSIFIER_LABEL = "Classifier:";
    static final String FILE_EXTENSION_LABEL = "File Extension:";
    private static final String PAGE_TITLE = "Add a Repository";
    private static final String SNAPSHOT_IDENTIFIER = "-SNAPSHOT";

    public static final String INFO_MSG = "On pressing finish the related repository will be added to the target file if it is accessible on Nexus.";
    public static final String ERROR_MSG_INVALID_REPO = "No valid p2 repository with the specified parameters could be found.";
    public static final String ERROR_MSG_INVALID_VERSION = "Specified version of the requested repository could not be found on Nexus.";
    public static final String ERROR_MSG_DUPLICATE_REPO = "Repository with the given group id and artifact id already exists in the target file";
    public static final String ERROR_MSG_MANDATORY_FIELDS = "Group ID, Artifact ID, Version and File Extension are mandatory fields that need to be specified.";
    private static final String ERROR_MSG_INVALID_P2_REPO = "Given location is either not a valid p2 repository or does not contain any Installable Units";

    private final ModifyListener modifyListener;
    private final LDITargetDefinitionProvider targetDefinitionProvider;
    private Text groupIdInput;
    private Text artifactIdInput;
    private Text versionInput;
    private Text classifierInput;
    private Text fileExtensionInput;

    protected AddRepositoryManuallyWizardPage(final LDITargetDefinitionProvider targetDefinitionProvider) {
        super(PAGE_TITLE, PAGE_TITLE, null);
        this.targetDefinitionProvider = targetDefinitionProvider;
        modifyListener = new ModifyListener() {
            @Override
            public void modifyText(final ModifyEvent e) {
                if (areMandatoryFieldsSet()) {
                    setMessage(INFO_MSG, INFORMATION);
                    setRepository(new Repository("Manually", groupIdInput.getText().trim(), artifactIdInput.getText()
                            .trim(), versionInput.getText().trim(), classifierInput.getText().trim(), "",
                            fileExtensionInput.getText().trim()));
                    setPageComplete(true);
                } else {
                    if (isPageComplete()) {
                        // only if the input is invalid again
                        setMessage(ERROR_MSG_MANDATORY_FIELDS, ERROR);
                    }
                    setRepository(null);
                    setPageComplete(false);
                }
            }
        };
    }

    void setGroupId(final String groupIdInput) {
        this.groupIdInput.setText(groupIdInput);
    }

    void setArtifactIdInput(final String artifactIdInput) {
        this.artifactIdInput.setText(artifactIdInput);
    }

    void setVersionInput(final String versionInput) {
        this.versionInput.setText(versionInput);
    }

    void setClassifierInput(final String classifierInput) {
        this.classifierInput.setText(classifierInput);
    }

    void setFileExtension(final String fileExtensionInput) {
        this.fileExtensionInput.setText(fileExtensionInput);
    }

    private void setRepository(final Repository repository) {
        ((AddRepositoryWizard) getWizard()).setRepository(repository);
    }

    private Repository getRepository() {
        return ((AddRepositoryWizard) getWizard()).getRepository();
    }

    void reset() {
        setPageComplete(false);
        setMessage("Group ID, Artifact ID and Version of a repository are mandatory fields.");
        setGroupId("");
        setArtifactIdInput("");
        setVersionInput("");
        setClassifierInput("");
    }

    private boolean areMandatoryFieldsSet() {
        boolean result = groupIdInput.getText().trim().length() != 0;
        result &= artifactIdInput.getText().trim().length() != 0;
        result &= versionInput.getText().trim().length() != 0;
        return result;
    }

    @Override
    public void createControl(final Composite parent) {
        final Composite container = new Composite(parent, SWT.NONE);
        final GridLayout masterLayout = new GridLayout(2, false);
        masterLayout.horizontalSpacing = 10;
        masterLayout.verticalSpacing = 8;
        container.setLayout(masterLayout);

        final GridDataFactory labelGridDataFactory = GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER)
                .grab(false, false);
        final GridDataFactory textFieldGridDataFactory = GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER)
                .grab(true, false);

        createLabel(container, labelGridDataFactory, GROUP_ID_LABEL);
        groupIdInput = createText(container, textFieldGridDataFactory);

        createLabel(container, labelGridDataFactory, ARTIFACT_ID_LABEL);
        artifactIdInput = createText(container, textFieldGridDataFactory);

        createLabel(container, labelGridDataFactory, VERSION_LABEL);
        versionInput = createText(container, textFieldGridDataFactory);

        createLabel(container, labelGridDataFactory, CLASSIFIER_LABEL);
        classifierInput = createText(container, textFieldGridDataFactory);

        createLabel(container, labelGridDataFactory, FILE_EXTENSION_LABEL);
        fileExtensionInput = createText(container, textFieldGridDataFactory);
        fileExtensionInput.setText("zip");
        fileExtensionInput.setEnabled(false);
        setControl(container);
        reset();
    }

    private void createLabel(final Composite composite, final GridDataFactory gridDataFactory, final String text) {
        final Label label = new Label(composite, SWT.WRAP);
        gridDataFactory.applyTo(label);
        label.setText(text);
    }

    private Text createText(final Composite parent, final GridDataFactory gridDataFactory) {
        final Text text = new Text(parent, SWT.BORDER | SWT.SINGLE | Window.getDefaultOrientation());
        gridDataFactory.applyTo(text);
        text.setText("");
        text.addModifyListener(modifyListener);
        return text;
    }

    boolean performFinish() {
        final boolean[] result = { true };
        final String[] errorMessage = new String[1];
        try {
            getContainer().run(true, false, new IRunnableWithProgress() {

                @Override
                public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    monitor.beginTask("Adding repository to target definition", IProgressMonitor.UNKNOWN);
                    try {
                        final Repository repository = getRepository();
                        addRepository(targetDefinitionProvider, repository);
                    } catch (final ProvisionException e) {
                        errorMessage[0] = ERROR_MSG_INVALID_REPO;
                        result[0] = false;
                    } catch (final IOException e) {
                        errorMessage[0] = ERROR_MSG_INVALID_REPO;
                        result[0] = false;
                    } catch (final Exception e) {
                        errorMessage[0] = e.getMessage();
                        result[0] = false;
                    }
                    monitor.done();
                }
            });
        } catch (final InvocationTargetException e) {
            errorMessage[0] = e.getMessage();
            result[0] = false;
        } catch (final InterruptedException e) {
            errorMessage[0] = e.getMessage();
            result[0] = false;
        }
        if (!result[0]) {
            setRepository(null);
            setMessage(errorMessage[0], ERROR);
            if (getContainer().getCurrentPage() != this) {
                this.getContainer().showPage(this);
            }
            this.getContainer().updateButtons();
        }
        return result[0];

    }

    void addRepository(final LDITargetDefinitionProvider tdProvider, final Repository repositoryData)
            throws ProvisionException, OperationCanceledException, CoreException, IOException, Exception {
        final ILDITargetDefintion ldiTargetDefinition = tdProvider.getLDITargetDefinition();

        NexusRepositoryNames nexusRepoName = null;
        if (repositoryData.getVersion().endsWith(SNAPSHOT_IDENTIFIER)) {
            nexusRepoName = NexusRepositoryNames.SNAPSHOT;
        } else {
            nexusRepoName = NexusRepositoryNames.MILESTONE;
        }
        final INexusRepository repository = LDIModelFactory.createNexusRepository(nexusRepoName,
                repositoryData.getGroupId(), repositoryData.getArtifactId(), repositoryData.getVersion(),
                repositoryData.getClassifier(), repositoryData.getExtension());
        final INexusRepositoryService service = LDIModelFactory.getNexusRepositoryService();

        validateRepository(service, repository, ldiTargetDefinition);
        // add a repository and all its IUs
        final ILDIRepositoryLocation newRepoLocation = ldiTargetDefinition.addRepository(repository.getURI());
        addAllUnits(newRepoLocation, repository.getURI());

    }

    private void validateRepository(final INexusRepositoryService service, final INexusRepository repository,
            final ILDITargetDefintion ldiTargetDefinition) throws IOException, ProvisionException,
            OperationCanceledException, CoreException, Exception {

        // Throws an IOException in case the repo can not be found on Nexus
        final INexusVersionInfo versionInfo = service.getVersionInfo(repository);
        final List<String> versions = versionInfo.getVersions();
        if (versions == null || !versions.contains(repository.getVersion())) {
            throw new IllegalStateException(ERROR_MSG_INVALID_VERSION);
        }

        if (isDuplicateRepository(ldiTargetDefinition, repository)) {
            throw new IllegalStateException(ERROR_MSG_DUPLICATE_REPO);
        }

        if (!validateP2Repo(repository)) {
            throw new IllegalStateException(ERROR_MSG_INVALID_P2_REPO);
        }
    }

    static boolean validateP2Repo(final IRepository repository) throws CoreException {
        final IInstallableUnit[] p2RepoIUs = getP2RepoIUs(repository.getURI());
        if (p2RepoIUs != null && p2RepoIUs.length == 0) {
            return false;
        }
        return true;
    }

    private boolean isDuplicateRepository(final ILDITargetDefintion ldiTargetDefinition, final INexusRepository repoToAdd) {
        final List<ILDIRepositoryLocation> repositoryLocations = ldiTargetDefinition.getRepositoryLocations();
        INexusRepository nexusRepo = null;
        for (final ILDIRepositoryLocation repoLoc : repositoryLocations) {
            final List<IRepository> repos = repoLoc.getRepositories();
            for (final IRepository iRepository : repos) {
                if (iRepository instanceof INexusRepository) {
                    nexusRepo = ((INexusRepository) iRepository);
                    if (nexusRepo.getGroupId().equals(repoToAdd.getGroupId())
                            && nexusRepo.getArtifactId().equals(repoToAdd.getArtifactId())) {
                        return true;
                    }
                }
            }

        }
        return false;
    }

    private void addAllUnits(final ILDIRepositoryLocation newRepoLocation, final URI uri) throws ProvisionException,
            OperationCanceledException, CoreException {
        final IInstallableUnit[] foundUnits = getP2RepoIUs(uri);
        final IMutableVersionedId[] addedUnits = newRepoLocation.addUnits(foundUnits);
        for (final IMutableVersionedId mutableVersionedId : addedUnits) {
            mutableVersionedId.setVersion(Version.create("0.0.0"));
        }
    }

    private static IInstallableUnit[] getP2RepoIUs(final URI uri) throws ProvisionException, CoreException {
        final IMetadataRepository repo = P2TargetUtils.getRepoManager().loadRepository(uri, new NullProgressMonitor());
        final IQuery<IInstallableUnit> groupQuery = QueryUtil.createIUGroupQuery();
        final IQuery<IInstallableUnit> latestGroupQuery = QueryUtil.createLatestQuery(groupQuery);

        final IQueryResult<IInstallableUnit> queryResult = repo.query(latestGroupQuery, new NullProgressMonitor());
        final IInstallableUnit[] foundUnits = queryResult.toArray(IInstallableUnit.class);
        return foundUnits;
    }

    @Override
    public void performHelp() {
        PlatformUI.getWorkbench().getHelpSystem().displayHelp(AddRepositoryWizard.ADD_REPOSITORY_HELP_ID);
    }

}
