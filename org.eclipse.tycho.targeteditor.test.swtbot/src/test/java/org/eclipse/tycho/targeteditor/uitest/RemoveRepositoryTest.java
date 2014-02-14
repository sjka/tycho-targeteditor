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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.tycho.targeteditor.LDITargetEditorPO;
import org.eclipse.tycho.targeteditor.RepositoriesEditorPagePO;
import org.eclipse.tycho.targeteditor.model.NexusRepositoryServiceMock;
import org.eclipse.tycho.targeteditor.model.LDIModelFactoryUtil;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(SWTBotJunit4ClassRunner.class)
public class RemoveRepositoryTest {

    private static final String IDENTIFIER_OF_ONLY_REPO_IN_LOCATION = "onlyRepoInLocation";
    private static final String IDENTIFIER_OF_SECOND_OF_TWO_REPOS_IN_LOCATION = "secondOfTwoReposInOneLocation";
    private static final String IDENTIFIER_OF_FIRST_OF_TWO_REPOS_IN_LOCATION = "firstOfTwoReposInOneLocation";
    private static IFile targetFile;
    private static IProject project;
    private LDITargetEditorPO ldiEditor;
    private RepositoriesEditorPagePO repositoriesPage;
    private int initialNumberOfRepositories;

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
        ldiEditor = LDITargetEditorPO.open(targetFile);
        repositoriesPage = ldiEditor.switchToRepositoriesPage();
        initialNumberOfRepositories = repositoriesPage.getRepositories().size();
    }

    @Test
    public void testRemoveRepoActionState() throws Exception {
        assertTrue(repositoriesPage.hasRemoveRepositoryAction());
        repositoriesPage.performRemoveRepositoryAction();
        assertFalse(repositoriesPage.hasRemoveRepositoryAction());
        repositoriesPage.select(0);
        assertTrue(repositoriesPage.hasRemoveRepositoryAction());
    }

    @Test
    public void testRemoveSingleRepositoryInLocation() throws Exception {
        final int repoToBeDeleted = repositoriesPage
                .getIndexOfRepositoryWithURIPart(IDENTIFIER_OF_ONLY_REPO_IN_LOCATION);

        repositoriesPage.select(repoToBeDeleted);
        repositoriesPage.performRemoveRepositoryAction();

        assertFalse(repositoriesPage.showsWarningDialog());
        assertEquals(-1, repositoriesPage.getIndexOfRepositoryWithURIPart(IDENTIFIER_OF_ONLY_REPO_IN_LOCATION));
        assertEquals(initialNumberOfRepositories - 1, repositoriesPage.getRepositories().size());
    }

    @Test
    public void testRemoveIsPersistent() throws Exception {
        final int repoToBeDeleted = repositoriesPage
                .getIndexOfRepositoryWithURIPart(IDENTIFIER_OF_ONLY_REPO_IN_LOCATION);

        repositoriesPage.select(repoToBeDeleted);
        repositoriesPage.performRemoveRepositoryAction();
        assertTrue(ldiEditor.isDirty());
        ldiEditor.save();
        assertFalse(ldiEditor.isDirty());

        final LDITargetEditorPO ldiEditor2 = LDITargetEditorPO.open(targetFile);
        final RepositoriesEditorPagePO reposPage2 = ldiEditor2.switchToRepositoriesPage();
        assertEquals(initialNumberOfRepositories - 1, reposPage2.getRepositories().size());
    }

    @Test
    public void testThatSelectAndRemoveOneRepositoryWillRemoveAllRepositoriesOfTheLocation() throws Exception {
        final int repoToBeDeleted = repositoriesPage
                .getIndexOfRepositoryWithURIPart(IDENTIFIER_OF_FIRST_OF_TWO_REPOS_IN_LOCATION);
        assertTrue(repoToBeDeleted != -1);

        repositoriesPage.select(repoToBeDeleted);
        repositoriesPage.performRemoveRepositoryAction();

        assertTrue(repositoriesPage.showsWarningDialog());

        repositoriesPage.confirmRemoveRepositories();

        assertEquals(-1, repositoriesPage.getIndexOfRepositoryWithURIPart(IDENTIFIER_OF_FIRST_OF_TWO_REPOS_IN_LOCATION));
        assertEquals(-1,
                repositoriesPage.getIndexOfRepositoryWithURIPart(IDENTIFIER_OF_SECOND_OF_TWO_REPOS_IN_LOCATION));
        assertEquals(initialNumberOfRepositories - 2, repositoriesPage.getRepositories().size());
    }

    @Test
    public void testCancelRemoveWillNotRemoveAnything() throws Exception {
        final int repoToBeDeleted = repositoriesPage
                .getIndexOfRepositoryWithURIPart(IDENTIFIER_OF_FIRST_OF_TWO_REPOS_IN_LOCATION);
        repositoriesPage.select(repoToBeDeleted);
        repositoriesPage.performRemoveRepositoryAction();
        repositoriesPage.cancelRemoveRepositories();

        assertEquals(initialNumberOfRepositories, repositoriesPage.getRepositories().size());
    }

    @Test
    public void testSelectingAllReposOfALocationForRemove() throws Exception {
        final int[] multiSelection = new int[] {
                repositoriesPage.getIndexOfRepositoryWithURIPart(IDENTIFIER_OF_FIRST_OF_TWO_REPOS_IN_LOCATION),
                repositoriesPage.getIndexOfRepositoryWithURIPart(IDENTIFIER_OF_SECOND_OF_TWO_REPOS_IN_LOCATION) };

        repositoriesPage.select(multiSelection);
        assertEquals(2, repositoriesPage.getNumberOfSelections());

        repositoriesPage.performRemoveRepositoryAction();

        // no warning expected as all repositories in the location are explicitely to be deleted
        assertFalse(repositoriesPage.showsWarningDialog());

        assertEquals(-1, repositoriesPage.getIndexOfRepositoryWithURIPart(IDENTIFIER_OF_FIRST_OF_TWO_REPOS_IN_LOCATION));
        assertEquals(-1,
                repositoriesPage.getIndexOfRepositoryWithURIPart(IDENTIFIER_OF_SECOND_OF_TWO_REPOS_IN_LOCATION));
        assertEquals(initialNumberOfRepositories - 2, repositoriesPage.getRepositories().size());
    }
}
