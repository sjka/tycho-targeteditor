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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.tycho.targeteditor.LDITargetEditorPO;
import org.eclipse.tycho.targeteditor.RepositoriesEditorPagePO;
import org.eclipse.tycho.targeteditor.dialogs.AddRepositoryManuallyPO;
import org.eclipse.tycho.targeteditor.dialogs.AddRepositoryManuallyWizardPage;
import org.eclipse.tycho.targeteditor.model.NexusRepositoryServiceMock;
import org.eclipse.tycho.targeteditor.model.LDIModelFactoryUtil;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(SWTBotJunit4ClassRunner.class)
public class AddRepositoryManuallyTest {

    private static IFile targetFile;
    private static IProject project;

    @BeforeClass
    public static void setupClass() throws CoreException {
        project = Util.createAndOpenTestProject();

        LDIModelFactoryUtil.setNexusRepositoryService(new NexusRepositoryServiceMock());
        TargetEditorTest.maximizeWindow();
        TargetEditorTest.closeWelcomePage();
    }

    @AfterClass
    public static void tearDownClass() throws CoreException {
        project.delete(true, true, new NullProgressMonitor());
    }

    @Before
    public void setup() throws IOException, CoreException {
        targetFile = Util.createTestTargetFile(project);
        setClipboardText(" "); // emtpy string and clipboard.clearContents() doesn't work
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

    @Test
    public void testAddRepoActionIsAvailable() throws Exception {
        final LDITargetEditorPO ldiEditor = LDITargetEditorPO.open(targetFile);
        final RepositoriesEditorPagePO reposPage = ldiEditor.switchToRepositoriesPage();
        assertTrue(reposPage.hasAddRepositoryAction());
    }

    @Test
    public void testInitialState() throws Exception {
        final LDITargetEditorPO ldiEditor = LDITargetEditorPO.open(targetFile);
        final RepositoriesEditorPagePO reposPage = ldiEditor.switchToRepositoriesPage();
        assertTrue(reposPage.hasAddRepositoryAction());
        final AddRepositoryManuallyPO addRepoWizard = reposPage.getAddRepositoryWizard().moveToAddRepoManuallyPage();

        assertNotNull(addRepoWizard);
        assertThat(addRepoWizard.canFinish(), is(false));
        assertThat(addRepoWizard.canCancel(), is(true));

        addRepoWizard.performCancel();
        assertFalse(ldiEditor.isDirty());
    }

    @Test
    public void testInitialStatePrefilled() throws Exception {
        setClipboardText("http://nexus:8081/nexus/content/repositories/build.snapshots.unzip/org/eclipse/tycho/demo/helloworld/org.eclipse.tycho.demo.helloworld.updatesite/0.1.1-SNAPSHOT/org.eclipse.tycho.demo.helloworld.updatesite-0.1.1-SNAPSHOT-assembly.zip-unzip");

        final LDITargetEditorPO ldiEditor = LDITargetEditorPO.open(targetFile);
        final RepositoriesEditorPagePO reposPage = ldiEditor.switchToRepositoriesPage();
        assertTrue(reposPage.hasAddRepositoryAction());
        final AddRepositoryManuallyPO addRepoWizard = reposPage.getAddRepositoryWizard().moveToAddRepoManuallyPage();

        assertNotNull(addRepoWizard);

        assertThat(addRepoWizard.canFinish(), is(true));
        assertThat(addRepoWizard.canCancel(), is(true));

        assertEquals("org.eclipse.tycho.demo.helloworld", addRepoWizard.getGroupIdText());
        assertEquals("org.eclipse.tycho.demo.helloworld.updatesite", addRepoWizard.getArtifactIdText());
        assertEquals("0.1.1-SNAPSHOT", addRepoWizard.getVersionIdText());
        assertEquals("assembly", addRepoWizard.getClassifierText());
        assertEquals("zip", addRepoWizard.getFileExtensionText());

        addRepoWizard.performCancel();
        assertFalse(ldiEditor.isDirty());
    }

    @Test
    public void testFinishButtonState() throws Exception {
        final LDITargetEditorPO ldiEditor = LDITargetEditorPO.open(targetFile);
        final RepositoriesEditorPagePO reposPage = ldiEditor.switchToRepositoriesPage();
        assertTrue(reposPage.hasAddRepositoryAction());
        final AddRepositoryManuallyPO addRepoWizard = reposPage.getAddRepositoryWizard().moveToAddRepoManuallyPage();
        assertNotNull(addRepoWizard);
        assertThat(addRepoWizard.canFinish(), is(false));

        addRepoWizard.setGroupIdText("group_id");
        addRepoWizard.setArtifactIdText("artifact_id");
        addRepoWizard.setVersionIdText("version");
        assertThat(addRepoWizard.canFinish(), is(true));
        assertTrue(addRepoWizard.hasMessage(AddRepositoryManuallyWizardPage.INFO_MSG));

        addRepoWizard.setClassifierText("classifier");
        assertThat(addRepoWizard.canFinish(), is(true));
        assertTrue(addRepoWizard.hasMessage(AddRepositoryManuallyWizardPage.INFO_MSG));

        addRepoWizard.setClassifierText("");
        assertThat(addRepoWizard.canFinish(), is(true));
        assertTrue(addRepoWizard.hasMessage(AddRepositoryManuallyWizardPage.INFO_MSG));

        addRepoWizard.setClassifierText("classifier");
        addRepoWizard.setVersionIdText("");
        assertTrue(addRepoWizard.hasMessage(AddRepositoryManuallyWizardPage.ERROR_MSG_MANDATORY_FIELDS));
        assertThat(addRepoWizard.canFinish(), is(false));

        addRepoWizard.setClassifierText("");
        assertTrue(addRepoWizard.hasMessage(AddRepositoryManuallyWizardPage.ERROR_MSG_MANDATORY_FIELDS));
        assertThat(addRepoWizard.canFinish(), is(false));

        addRepoWizard.performCancel();
        assertFalse(ldiEditor.isDirty());
    }

    @Test
    public void testInvalidVersionInput() throws Exception {
        final LDITargetEditorPO ldiEditor = LDITargetEditorPO.open(targetFile);
        final RepositoriesEditorPagePO reposPage = ldiEditor.switchToRepositoriesPage();
        assertTrue(reposPage.hasAddRepositoryAction());
        final AddRepositoryManuallyPO addRepoWizard = reposPage.getAddRepositoryWizard().moveToAddRepoManuallyPage();
        assertNotNull(addRepoWizard);

        addRepoWizard.setGroupIdText("dummy.group");
        addRepoWizard.setArtifactIdText("dummy.artifact.id");
        addRepoWizard.setVersionIdText("10.10.10");

        assertTrue(addRepoWizard.hasMessage(AddRepositoryManuallyWizardPage.INFO_MSG));

        assertFalse(addRepoWizard.performFinish());

        assertTrue(addRepoWizard.hasMessage(AddRepositoryManuallyWizardPage.ERROR_MSG_INVALID_VERSION));

        addRepoWizard.performCancel();
        assertFalse(ldiEditor.isDirty());

    }

    @Test
    public void testDuplicateRepository() throws Exception {
        final LDITargetEditorPO ldiEditor = LDITargetEditorPO.open(targetFile);
        final RepositoriesEditorPagePO reposPage = ldiEditor.switchToRepositoriesPage();
        assertTrue(reposPage.hasAddRepositoryAction());
        final AddRepositoryManuallyPO addRepoWizard = reposPage.getAddRepositoryWizard().moveToAddRepoManuallyPage();
        assertNotNull(addRepoWizard);

        addRepoWizard.setGroupIdText("org.eclipse.tycho.test");
        addRepoWizard.setArtifactIdText("testartifactId1");
        addRepoWizard.setVersionIdText("2.0.0");

        assertFalse(addRepoWizard.performFinish());

        assertTrue(addRepoWizard.hasMessage(AddRepositoryManuallyWizardPage.ERROR_MSG_DUPLICATE_REPO));

        addRepoWizard.performCancel();
        assertFalse(ldiEditor.isDirty());

    }

    @Test
    public void testNonExistingRepository() throws Exception {
        final LDITargetEditorPO ldiEditor = LDITargetEditorPO.open(targetFile);
        final RepositoriesEditorPagePO reposPage = ldiEditor.switchToRepositoriesPage();
        assertTrue(reposPage.hasAddRepositoryAction());
        final AddRepositoryManuallyPO addRepoWizard = reposPage.getAddRepositoryWizard().moveToAddRepoManuallyPage();
        assertNotNull(addRepoWizard);

        addRepoWizard.setGroupIdText("invalid.groupid");
        addRepoWizard.setArtifactIdText("invalid.artifactId");
        addRepoWizard.setVersionIdText("2.0.0");

        assertFalse(addRepoWizard.performFinish());

        assertTrue(addRepoWizard.hasMessage(AddRepositoryManuallyWizardPage.ERROR_MSG_INVALID_REPO));

        addRepoWizard.performCancel();
        assertFalse(ldiEditor.isDirty());
    }

}
