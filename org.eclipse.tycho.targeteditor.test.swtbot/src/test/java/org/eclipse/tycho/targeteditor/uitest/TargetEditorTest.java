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
package org.eclipse.tycho.targeteditor.uitest;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IRegion;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
import org.eclipse.tycho.targeteditor.Activator;
import org.eclipse.tycho.targeteditor.HintBox;
import org.eclipse.tycho.targeteditor.LDITargetEditor;
import org.eclipse.tycho.targeteditor.LDITargetEditorPO;
import org.eclipse.tycho.targeteditor.RepositoriesEditorPagePO;
import org.eclipse.tycho.targeteditor.WorkbenchPO;
import org.eclipse.tycho.targeteditor.model.EDynamicVersions;
import org.eclipse.tycho.targeteditor.model.INexusRepository;
import org.eclipse.tycho.targeteditor.model.IRepository;
import org.eclipse.tycho.targeteditor.model.NexusRepositoryNames;
import org.eclipse.tycho.targeteditor.model.NexusRepositoryServiceMock;
import org.eclipse.tycho.targeteditor.model.LDIModelFactory;
import org.eclipse.tycho.targeteditor.model.LDIModelFactoryUtil;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.console.MessageConsole;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(SWTBotJunit4ClassRunner.class)
public class TargetEditorTest {

    private static final String SNAPSHOTS_REPO = NexusRepositoryNames.SNAPSHOT.nexusName();
    private static final String MILESTONES_REPO = NexusRepositoryNames.MILESTONE.nexusName();

    private static final String SNAPSHOT = EDynamicVersions.SNAPSHOT.name();
    private static final String RELEASE = EDynamicVersions.RELEASE.name();

    private static final String WELCOME_PAGE_TITLE = "Welcome";

    private static IFile targetFile;
    private static IProject project;
    private static NullProgressMonitor nullMonitor = new NullProgressMonitor();

    @BeforeClass
    public static void setupClass() throws CoreException {
        project = Util.createAndOpenTestProject();

        LDIModelFactoryUtil.setNexusRepositoryService(new NexusRepositoryServiceMock());
        maximizeWindow();
        closeWelcomePage();
    }

    @AfterClass
    public static void tearDownClass() throws CoreException {
        project.delete(true, true, nullMonitor);
    }

    @Before
    public void setup() throws IOException, CoreException {
        targetFile = Util.createTestTargetFile(project);
    }

    @Test
    public void testRepositoryTypes() throws WorkbenchException {
        final LDITargetEditorPO ldiEditor = LDITargetEditorPO.open(targetFile);
        final RepositoriesEditorPagePO repositoriesPage = ldiEditor.switchToRepositoriesPage();
        final List<IRepository> repositories = repositoriesPage.getRepositories();
        repositoriesPage.select(repositories.get(3));
        assertTrue(isDetailsPartIsSimple(repositoriesPage));
        repositoriesPage.select(repositories.get(1));
        assertTrue(isDetailsPageIsNexusRepository(repositoriesPage));
        ldiEditor.close();
    }

    @Test
    public void testVersionChange() throws WorkbenchException {
        LDITargetEditorPO ldiEditor = LDITargetEditorPO.open(targetFile);
        RepositoriesEditorPagePO repositoriesPage = ldiEditor.switchToRepositoriesPage();
        final List<IRepository> repositories = repositoriesPage.getRepositories();
        final INexusRepository notLatestVersionRepository = (INexusRepository) repositories.get(0);
        assertThat(notLatestVersionRepository.getVersion(), is(not(NexusRepositoryServiceMock.LATEST_VERSION)));
        repositoriesPage.select(repositories.get(0));
        assertFalse(ldiEditor.isDirty());
        repositoriesPage.getDetailsSection().selectLatestReleasedVersion();
        assertTrue(ldiEditor.isDirty());
        ldiEditor.save();
        assertFalse(ldiEditor.isDirty());
        ldiEditor.close();

        ldiEditor = LDITargetEditorPO.open(targetFile);
        repositoriesPage = ldiEditor.switchToRepositoriesPage();
        final List<IRepository> newRepositories = repositoriesPage.getRepositories();
        final INexusRepository changedRepository = (INexusRepository) newRepositories.get(0);
        assertThat(changedRepository.getVersion(), is(NexusRepositoryServiceMock.LATEST_VERSION));
    }

    @Test
    public void testRepositoryConversion() throws WorkbenchException {
        final LDITargetEditorPO ldiEditor = LDITargetEditorPO.open(targetFile);
        final RepositoriesEditorPagePO repositoriesPage = ldiEditor.switchToRepositoriesPage();
        final List<IRepository> repositories = repositoriesPage.getRepositories();
        final INexusRepository firstRepository = (INexusRepository) repositories.get(0);
        final INexusRepository notLatestVersionRepository = firstRepository;
        assertThat(notLatestVersionRepository.getVersion(), is(not(NexusRepositoryServiceMock.LATEST_SNAPSHOT_VERSION)));
        assertFalse(firstRepository.getRepositoryName().equals(SNAPSHOTS_REPO));
        repositoriesPage.select(firstRepository);
        final String[] availableVersions = repositoriesPage.getDetailsSection().getAvailableVersions();
        String firstNonDynamicVersionInCombo = "";
        for (final String availableVersion : availableVersions) {
            if (!EDynamicVersions.isDynamicVersion(availableVersion)) {
                firstNonDynamicVersionInCombo = availableVersion;
                break;
            }
        }
        assertTrue("No SNAPSHOT version first in combo (after dynamic versions)",
                firstNonDynamicVersionInCombo.endsWith("-SNAPSHOT"));
        repositoriesPage.getDetailsSection().selectLatestVersion();
        assertThat(notLatestVersionRepository.getVersion(), is(NexusRepositoryServiceMock.LATEST_SNAPSHOT_VERSION));
        assertTrue(firstRepository.getRepositoryName().equals(SNAPSHOTS_REPO));

        repositoriesPage.getDetailsSection().selectLatestReleasedVersion();
        assertTrue(firstRepository.getRepositoryName().equals(MILESTONES_REPO));

        ldiEditor.save();
    }

    @Test
    public void testSNAPSHOTAndRELEASEVersionAvailable() throws Exception {
        final LDITargetEditorPO ldiEditor = LDITargetEditorPO.open(targetFile);
        final RepositoriesEditorPagePO repositoriesPage = ldiEditor.switchToRepositoriesPage();
        final List<IRepository> repositories = repositoriesPage.getRepositories();
        final INexusRepository firstRepository = (INexusRepository) repositories.get(0);
        repositoriesPage.select(firstRepository);
        final String[] availableVersions = repositoriesPage.getDetailsSection().getAvailableVersions();
        assertFalse(firstRepository.getRepositoryName().equals(SNAPSHOTS_REPO));
        assertTrue(Arrays.asList(availableVersions).contains(SNAPSHOT));
        assertTrue(Arrays.asList(availableVersions).contains(RELEASE));
        repositoriesPage.getDetailsSection().selectVersion(SNAPSHOT);
        assertTrue(firstRepository.getRepositoryName().equals(SNAPSHOTS_REPO));

        repositoriesPage.getDetailsSection().selectVersion(RELEASE);
        assertTrue(firstRepository.getRepositoryName().equals(SNAPSHOTS_REPO));

    }

    @Test
    public void testSNAPSHOTRepositoryUrlChange() throws Exception {
        final LDITargetEditorPO ldiEditor = LDITargetEditorPO.open(targetFile);
        final RepositoriesEditorPagePO repositoriesPage = ldiEditor.switchToRepositoriesPage();
        final List<IRepository> repositories = repositoriesPage.getRepositories();
        final INexusRepository firstRepository = (INexusRepository) repositories.get(0);
        repositoriesPage.select(firstRepository);
        final String oldVersion = firstRepository.getVersion();
        repositoriesPage.getDetailsSection().selectVersion("SNAPSHOT");
        final String repositoryURL = repositoriesPage.getDetailsSection().getRepositoryUrl();
        assertNotNull(repositoryURL);
        assertFalse(repositoryURL.contains(oldVersion));
        assertTrue(repositoryURL.contains("/SNAPSHOT/"));
        assertTrue(repositoryURL.contains("SNAPSHOT-assembly.zip-unzip"));
    }

    @Test
    public void testRELEASENotAvailable() throws Exception {
        final LDITargetEditorPO ldiEditor = LDITargetEditorPO.open(targetFile);
        final RepositoriesEditorPagePO repositoriesPage = ldiEditor.switchToRepositoriesPage();
        final List<IRepository> repositories = repositoriesPage.getRepositories();
        final INexusRepository notReleasedRepo = (INexusRepository) selectNotReleasedRepo(repositoriesPage,
                repositories);
        assertNotNull("Test target file doesn't contain an artifact 'testartifactNotReleased'" + notReleasedRepo);
        final String[] availableVersions = repositoriesPage.getDetailsSection().getAvailableVersions();

        assertTrue(notReleasedRepo.getRepositoryName().equals(SNAPSHOTS_REPO));
        assertTrue(Arrays.asList(availableVersions).contains(SNAPSHOT));
        assertFalse(Arrays.asList(availableVersions).contains(RELEASE));
        repositoriesPage.getDetailsSection().selectVersion(SNAPSHOT);
        assertTrue(notReleasedRepo.getRepositoryName().equals(SNAPSHOTS_REPO));
    }

    @Test
    public void testVersionUpdate() throws WorkbenchException {
        final LDITargetEditorPO ldiEditor = LDITargetEditorPO.open(targetFile);
        final RepositoriesEditorPagePO repositoriesPage = ldiEditor.switchToRepositoriesPage();
        assertTrue(repositoriesPage.hasUpdateVersionAction());
        repositoriesPage.performUpdateVersionAction();
        assertFalse(repositoriesPage.hasUpdateVersionAction());
        assertTrue(ldiEditor.isDirty());
        ldiEditor.save();
    }

    @Test
    public void testUpdateReposToLatestRelease() throws Exception {
        final LDITargetEditorPO ldiEditor = LDITargetEditorPO.open(targetFile);
        final RepositoriesEditorPagePO reposPage = ldiEditor.switchToRepositoriesPage();
        assertTrue(reposPage.hasUpdateLatestReleaseAction());
        reposPage.performUpdateRepositoryVersionAction();
        assertTrue(ldiEditor.isDirty());
        ldiEditor.save();
        assertFalse(reposPage.hasUpdateLatestReleaseAction());
    }

    @Test
    public void testRevert() throws Exception {
        final LDITargetEditorPO ldiEditor = LDITargetEditorPO.open(targetFile);
        final RepositoriesEditorPagePO reposPage = ldiEditor.switchToRepositoriesPage();
        final INexusRepository repository = (INexusRepository) reposPage.getRepositories().get(0);
        final String initialVersion = repository.getVersion();
        repository.setVersion("99.99.99");
        assertTrue(ldiEditor.isDirty());
        performRevertAction();
        assertFalse(ldiEditor.isDirty());
        assertEquals(initialVersion, ((INexusRepository) reposPage.getRepositories().get(0)).getVersion());
    }

    @Test
    public void testRefreshActionAvailable() throws Exception {
        final LDITargetEditorPO ldiEditor = LDITargetEditorPO.open(targetFile);
        final RepositoriesEditorPagePO reposPage = ldiEditor.switchToRepositoriesPage();
        reposPage.performRefreshAllCachesAction();
    }

    @Test
    public void testShowProblemDetailsAction() throws Exception {
        final int UI_STATUS_POLL_DELAY = 600;// ShowResolutionProblemAction.ResolutionProblemActionLifecyclePart.RESOLUTION_STATUS_POLL_TIMER_PERIOD +100
        // open and wait for resolution
        final LDITargetEditorPO ldiEditor = LDITargetEditorPO.open(targetFile);
        final RepositoriesEditorPagePO reposPage = ldiEditor.switchToRepositoriesPage();
        final LDITargetEditor eclipseLdiEditor = ldiEditor.getEclipseEditor();
        waitForResolveJob(eclipseLdiEditor);
        Thread.sleep(UI_STATUS_POLL_DELAY);

        // perform show details action
        assertTrue(reposPage.hasShowProblemDetailsAction());
        reposPage.performShowProblemDetailsAction();

        // check that output console was created and contains problem
        checkConsoleOutput();

        // replace file content
        final URL emptyTargetTemplateUrl = FileLocator.find(Activator.getDefault().getBundle(), new Path(
                "resources/emptyTarget.target"), null);
        setFileContent(targetFile, emptyTargetTemplateUrl.openStream());
        Thread.sleep(300); // wait for resource change listeners to trigger resolve job
        waitForResolveJob(eclipseLdiEditor);
        Thread.sleep(UI_STATUS_POLL_DELAY);

        // the empty target has no problem
        assertFalse(reposPage.hasShowProblemDetailsAction());
    }

    @Test
    public void testFilterTextHint() throws Exception {
        final LDITargetEditorPO ldiEditor = LDITargetEditorPO.open(targetFile);
        final RepositoriesEditorPagePO reposPage = ldiEditor.switchToRepositoriesPage();

        final SWTBotText filterText = reposPage.getFilterText();
        assertEquals(HintBox.HINT_TEXT, filterText.getText());

        filterText.setFocus();
        assertEquals("", filterText.getText());

        final String filterString = "filterString";
        filterText.setText(filterString);
        assertEquals(filterString, filterText.getText());

        // lose focus in filterText
        reposPage.getFilterTable().setFocus();
        assertEquals(filterString, filterText.getText());

        filterText.setFocus();
        assertEquals(filterString, filterText.getText());

        filterText.setText("");
        reposPage.getFilterTable().setFocus();
        assertEquals(HintBox.HINT_TEXT, filterText.getText());

    }

    @Test
    public void testFiltering() throws Exception {
        final LDITargetEditorPO ldiEditor = LDITargetEditorPO.open(targetFile);
        final RepositoriesEditorPagePO reposPage = ldiEditor.switchToRepositoriesPage();
        final SWTBotTable filterTable = reposPage.getFilterTable();

        assertEquals(6, filterTable.rowCount());

        final SWTBotText filterText = reposPage.getFilterText();
        filterText.setFocus();
        filterText.setText("1.1.1");

        // expect 2 repositories with version starting with 1.1.1
        assertEquals(2, filterTable.rowCount());
        assertEquals(" org.eclipse.tycho.test  /  testartifactId  /  1.1.1  /  (build.milestones.unzip)", filterTable
                .getTableItem(0).getText());

    }

    private void checkConsoleOutput() throws BadLocationException {
        final MessageConsole targetDefinitionProblemConsole = WorkbenchPO.getWorkbench()
                .getTargetDefinitionProblemConsole();
        assertNotNull(targetDefinitionProblemConsole);
        final FindReplaceDocumentAdapter problemDocumentAdapter = new FindReplaceDocumentAdapter(
                targetDefinitionProblemConsole.getDocument());

        // problem formatter is tested as unit test. Just check that content was written to console.
        // Problem text might need to be adjusted in case the used target file or resolution problem creation changes
        final IRegion region = problemDocumentAdapter.find(0, "Problems loading repositories", true, true, true, false);
        assertTrue(region.getOffset() > 0);
    }

    private IRepository selectNotReleasedRepo(final RepositoriesEditorPagePO repositoriesPage,
            final List<IRepository> repositories) {
        for (final IRepository repository : repositories) {
            if (repository instanceof INexusRepository) {
                if ("testartifactNotReleased".equals(((INexusRepository) repository).getArtifactId())) {
                    repositoriesPage.select(repository);
                    return repository;
                }
            }
        }
        return null;
    }

    private static void setFileContent(final IFile file, final InputStream inputStream) throws CoreException,
            IOException {
        try {
            if (file.exists()) {
                file.setContents(inputStream, IResource.FORCE, nullMonitor);
            } else {
                file.create(inputStream, true, nullMonitor);
            }
        } finally {
            inputStream.close();
        }
    }

    private void waitForResolveJob(final LDITargetEditor eclipseLdiEditor) throws InterruptedException {
        final Object resolveJobFamily = LDIModelFactory.getResolveJobFamily(eclipseLdiEditor);
        final Job[] resolveJobs = Job.getJobManager().find(resolveJobFamily);
        for (final Job job : resolveJobs) {
            if (job.getState() == Job.RUNNING || job.getState() == Job.WAITING) {
                job.join();
            }
        }
    }

    private boolean isDetailsPageIsNexusRepository(final RepositoriesEditorPagePO repositoriesPage) {
        return repositoriesPage.getDetailsSection().hasVersionCombo();
    }

    private boolean isDetailsPartIsSimple(final RepositoriesEditorPagePO repositoriesPage) {
        return !repositoriesPage.getDetailsSection().hasVersionCombo();
    }

    static void maximizeWindow() {
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().setMaximized(true);
            }
        });

    }

    private void performRevertAction() {
        final SWTWorkbenchBot bot = new SWTWorkbenchBot();
        bot.menu("File").click();
        bot.menu("Revert").click();
    }

    static void closeWelcomePage() {
        final SWTWorkbenchBot bot = new SWTWorkbenchBot();
        final SWTBotView activeView = bot.activeView();
        if (activeView.getTitle().equals(WELCOME_PAGE_TITLE)) {
            activeView.close();
        }
    }

}
