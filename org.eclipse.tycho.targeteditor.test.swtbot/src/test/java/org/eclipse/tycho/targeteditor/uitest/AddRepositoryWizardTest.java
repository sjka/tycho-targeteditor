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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.tycho.targeteditor.Activator;
import org.eclipse.tycho.targeteditor.LDITargetEditorPO;
import org.eclipse.tycho.targeteditor.PreferencePagePO;
import org.eclipse.tycho.targeteditor.RepositoriesEditorPagePO;
import org.eclipse.tycho.targeteditor.dialogs.AddRepositorySelectOptionsPO;
import org.eclipse.tycho.targeteditor.model.INexusRepository;
import org.eclipse.tycho.targeteditor.model.IRepository;
import org.eclipse.tycho.targeteditor.model.NexusRepositoryServiceMock;
import org.eclipse.tycho.targeteditor.model.LDIModelFactoryUtil;
import org.eclipse.tycho.targeteditor.preferences.PreferenceConstants;
import org.eclipse.ui.PartInitException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(SWTBotJunit4ClassRunner.class)
public class AddRepositoryWizardTest {

    private static IProject project;
    private AddRepositorySelectOptionsPO addRepoWizard;
    private RepositoriesEditorPagePO reposPage;

    @BeforeClass
    public static void setupClass() throws CoreException {
        setRecommendedRepositoryFile("resources/recommendedRepositories.xml");
        project = Util.createAndOpenTestProject();

        LDIModelFactoryUtil.setNexusRepositoryService(new NexusRepositoryServiceMock());
        TargetEditorTest.maximizeWindow();
        TargetEditorTest.closeWelcomePage();
    }

    static void setRecommendedRepositoryFile(final String localPath) {
        final URL url = FileLocator.find(Activator.getDefault().getBundle(), new Path(localPath), null);
        Activator.getDefault().getPreferenceStore()
                .setValue(PreferenceConstants.P_RECOMMENDED_REPOSITORIES_URL, url.toExternalForm());
    }

    @AfterClass
    public static void tearDownClass() throws CoreException {
        project.delete(true, true, new NullProgressMonitor());

    }

    @Before
    public void setup() throws IOException, CoreException {
        final IFile targetFile = Util.createTestTargetFile(project);
        setClipboardText(" "); // emtpy string and clipboard.clearContents() doesn't work
        addRepoWizard = createAddRepoWizard(targetFile);
    }

    @After
    public void tearDown() {
        addRepoWizard.close();
    }

    private static void setClipboardText(final String text) {
        Display.getDefault().syncExec(new Runnable() {
            @Override
            public void run() {
                final Clipboard clipboard = new Clipboard(Display.getCurrent());
                final Transfer[] transfers = new Transfer[] { TextTransfer.getInstance() };
                final Object[] data = new Object[] { text };
                clipboard.setContents(data, transfers);
                clipboard.dispose();
            }
        });
    }

    private AddRepositorySelectOptionsPO createAddRepoWizard(final IFile targetFile) throws PartInitException {
        final LDITargetEditorPO ldiEditor = LDITargetEditorPO.open(targetFile);
        reposPage = ldiEditor.switchToRepositoriesPage();
        assertTrue(reposPage.hasAddRepositoryAction());
        final AddRepositorySelectOptionsPO wizard = reposPage.getAddRepositoryWizard();
        // select first option here only because of unknown test failures during hudson build
        wizard.selectRecommendedRepoOption();
        wizard.waitForRepositoriesLoaded();
        return wizard;
    }

    @Test
    public void testInitialState() {
        assertTrue(addRepoWizard.isRecommendedRepoOptionSelected());
        assertEquals(addRepoWizard.getRecommendedRepositoryCount(), 2);

        assertFalse(addRepoWizard.canSetRecommendedRepoDescription());
        assertFalse(addRepoWizard.canFinish());
        assertFalse(addRepoWizard.hasNext());
        assertTrue(addRepoWizard.canCancel());
    }

    @Test
    public void testManualRepoAddOptionSelection() {
        addRepoWizard.selectManuallyOption();

        assertFalse(addRepoWizard.isRecommendedRepoOptionSelected());
        assertTrue(addRepoWizard.isManuallyOptionSelected());

        assertFalse(addRepoWizard.isRecommendedRepoOptionGroupEnabled());

        assertFalse(addRepoWizard.canFinish());
        assertTrue(addRepoWizard.hasNext());
        assertTrue(addRepoWizard.canCancel());
    }

    @Test
    public void testSelectRecommendedRepoEnablesFinish() throws Exception {
        addRepoWizard.selectFirstRecommendedRepository();
        assertTrue(addRepoWizard.canFinish());
        assertTrue(addRepoWizard.hasNext());
    }

    @Test
    public void testSelectManualRepoDisablesFinish() throws Exception {
        addRepoWizard.selectFirstRecommendedRepository();
        addRepoWizard.selectManuallyOption();

        assertFalse(addRepoWizard.canFinish());
        assertTrue(addRepoWizard.hasNext());
    }

    @Test
    public void testConfigureLink() {
        final PreferencePagePO preferencePage = addRepoWizard.openPreferencePage();

        final String url = "http://newUrl";
        preferencePage.setUrl(url);
        assertEquals(url, preferencePage.getUrl(), url);
        assertTrue(preferencePage.canValidate());

        preferencePage.close();
    }

    @Test
    public void testAddRecommendedRepository() {
        final List<IRepository> beforeRepositories = reposPage.getRepositories();
        final int repositoryCount = beforeRepositories.size();
        addRepoWizard.selectRepository("JPaaS SDK");
        addRepoWizard.performFinish();
        reposPage.waitForRepositories(repositoryCount + 1);
        final List<IRepository> afterRepositories = reposPage.getRepositories();
        afterRepositories.removeAll(beforeRepositories);
        final INexusRepository addedRepository = (INexusRepository) afterRepositories.get(0);
        assertEquals("com.sap.core.distro.jpaas", addedRepository.getGroupId());
        assertEquals("com.sap.core.jpaas.sdk", addedRepository.getArtifactId());
    }
}
