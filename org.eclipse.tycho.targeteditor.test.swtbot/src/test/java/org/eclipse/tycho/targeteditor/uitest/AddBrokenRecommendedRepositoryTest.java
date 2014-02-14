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
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URL;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.tycho.targeteditor.Activator;
import org.eclipse.tycho.targeteditor.LDITargetEditorPO;
import org.eclipse.tycho.targeteditor.PreferencePagePO;
import org.eclipse.tycho.targeteditor.RepositoriesEditorPagePO;
import org.eclipse.tycho.targeteditor.dialogs.AddRepositorySelectOptionsPO;
import org.eclipse.tycho.targeteditor.model.NexusRepositoryServiceMock;
import org.eclipse.tycho.targeteditor.model.LDIModelFactoryUtil;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(SWTBotJunit4ClassRunner.class)
public class AddBrokenRecommendedRepositoryTest {

    private static IProject project;
    private AddRepositorySelectOptionsPO addRepoWizard;

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
        AddRepositoryWizardTest.setRecommendedRepositoryFile("resources/notRecommendedRepositories.xml");
        final IFile targetFile = Util.createTestTargetFile(project);
        final LDITargetEditorPO ldiEditor = LDITargetEditorPO.open(targetFile);
        final RepositoriesEditorPagePO reposPage = ldiEditor.switchToRepositoriesPage();
        assertTrue(reposPage.hasAddRepositoryAction());
        addRepoWizard = reposPage.getAddRepositoryWizard();
        addRepoWizard.waitForRepositoriesLoaded();
    }

    @After
    public void tearDown() {
        addRepoWizard.close();
    }

    @Test
    public void testTableIsEmptyIfUrlIsBroken() {
        assertEquals(0, addRepoWizard.getRecommendedRepositoryCount());
    }

    @Test
    public void testFixingUrlLoadsTable() {
        final PreferencePagePO preferencePage = addRepoWizard.openPreferencePage();
        final URL url = FileLocator.find(Activator.getDefault().getBundle(), new Path(
                "resources/recommendedRepositories.xml"), null);
        preferencePage.setUrl(url.toExternalForm());
        preferencePage.saveAndClose();
        addRepoWizard.waitForRepositoriesLoaded();
        assertEquals(2, addRepoWizard.getRecommendedRepositoryCount());
    }
}
