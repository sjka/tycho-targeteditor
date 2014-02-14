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
package org.eclipse.tycho.targeteditor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.net.URISyntaxException;

import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.tycho.targeteditor.model.INexusRepository;
import org.eclipse.tycho.targeteditor.model.NexusRepositoryNames;
import org.eclipse.tycho.targeteditor.model.NexusRepositoryServiceMock1;
import org.eclipse.tycho.targeteditor.model.NexusRepositoryTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class UpdateRepositoryVersionFormTest {

    private UpdateRepositoryVersionForm subject;
    private INexusRepository notCurrentRepo;
    private INexusRepository latestReleaseRepo;
    private INexusRepository latestSnapshotRepo;
    private INexusRepository problemRepo;
    private INexusRepository noReleasedVersionRepo;
    private NexusRepositoryServiceMock1 repositoryServiceMock;

    @Before
    public void setup() throws URISyntaxException {
        repositoryServiceMock = NexusRepositoryServiceMock1.createAndRegister();
        subject = new UpdateRepositoryVersionForm();
        notCurrentRepo = NexusRepositoryTest.createNexusRepo(NexusRepositoryNames.MILESTONE.nexusName(), "1.0.0");
        latestReleaseRepo = NexusRepositoryTest.createNexusRepo(NexusRepositoryNames.MILESTONE.nexusName(),
                NexusRepositoryServiceMock1.LATEST_VERSION);
        latestSnapshotRepo = NexusRepositoryTest.createNexusRepo(NexusRepositoryNames.MILESTONE.nexusName(),
                NexusRepositoryServiceMock1.LATEST_SNAPSHOT_VERSION);
        problemRepo = NexusRepositoryTest.createNexusRepo("problematic",
                NexusRepositoryServiceMock1.LATEST_SNAPSHOT_VERSION);
        noReleasedVersionRepo = NexusRepositoryTest.createNexusRepo("noReleasedVersion",
                NexusRepositoryServiceMock1.LATEST_SNAPSHOT_VERSION);
        waitForVersion(notCurrentRepo);
        waitForVersion(latestReleaseRepo);
        waitForVersion(latestSnapshotRepo);
        waitForVersion(problemRepo);
        waitForVersion(noReleasedVersionRepo);
    }

    @After
    public void cleanup() {
        repositoryServiceMock.restoreOriginalService();
    }

    @Test
    public void testEmptyDisabled() {
        subject.updateCurrentSelection(new StructuredSelection(new Object[0]));
        assertFalse(subject.canUpdateRelease());
    }

    @Test
    public void testSingleProblemRepoDisabled() {
        subject.updateCurrentSelection(new StructuredSelection(problemRepo));
        assertFalse(subject.canUpdateRelease());
    }

    @Test
    public void testMultiProblemRepoEnabled() {
        subject.updateCurrentSelection(new StructuredSelection(new Object[] { problemRepo, notCurrentRepo }));
        assertTrue(subject.canUpdateRelease());
    }

    @Test
    public void testSingleEnabled() {
        subject.updateCurrentSelection(new StructuredSelection(notCurrentRepo));
        assertTrue(subject.canUpdateRelease());
    }

    @Test
    public void testSingleDisabled() {
        subject.updateCurrentSelection(new StructuredSelection(latestReleaseRepo));
        assertFalse(subject.canUpdateRelease());
    }

    @Test
    public void testMultiDisabled() {
        subject.updateCurrentSelection(new StructuredSelection(new Object[] { latestReleaseRepo, latestReleaseRepo }));
        assertFalse(subject.canUpdateRelease());
    }

    @Test
    public void testMultiEnabled() {
        subject.updateCurrentSelection(new StructuredSelection(new Object[] { latestReleaseRepo, notCurrentRepo }));
        assertTrue(subject.canUpdateRelease());
    }

    @Test
    public void testLatestSnapshotDisabled() {
        subject.updateCurrentSelection(new StructuredSelection(new Object[] { latestSnapshotRepo }));
        assertFalse(subject.canUpdateRelease());
    }

    @Test
    public void testRepoWithoutReleasedVersion() {
        subject.updateCurrentSelection(new StructuredSelection(new Object[] { noReleasedVersionRepo }));
        assertFalse(subject.canUpdateRelease());
    }

    @Test
    public void testMultiType() throws URISyntaxException {
//        Unable to create IUBundleContainer in a simple Unit Test. (LDIModelFactory.getTargetPlatformService().newFeatureContainer("c:/dummy", "id", "1.0.0");)
//        Test some arbitrary classes instead. All unknown selections should be ignored
        subject.updateCurrentSelection(new StructuredSelection(new Object[] { latestReleaseRepo, notCurrentRepo,
                "Hi there", new File("c:/dummy") }));
        assertTrue(subject.canUpdateRelease());
        subject.updateCurrentSelection(new StructuredSelection(new Object[] { latestReleaseRepo, "Hi there",
                new File("c:/dummy") }));
        assertFalse(subject.canUpdateRelease());
    }

    @Test
    public void testSingleUpdate() throws URISyntaxException {
        subject.updateCurrentSelection(new StructuredSelection(notCurrentRepo));
        assertTrue(subject.canUpdateRelease());
        subject.updateToRelease();
        assertFalse(subject.canUpdateRelease());
        assertEquals(NexusRepositoryServiceMock1.LATEST_VERSION, notCurrentRepo.getVersion());
    }

    @Test
    public void testMultiUpdate() throws URISyntaxException {
        final INexusRepository secondNotCurrentRepo = NexusRepositoryTest.createNexusRepo(
                NexusRepositoryNames.MILESTONE.nexusName(), "1.0.1");
        waitForVersion(secondNotCurrentRepo);

        subject.updateCurrentSelection(new StructuredSelection(new Object[] { notCurrentRepo, latestReleaseRepo,
                secondNotCurrentRepo }));
        assertTrue(subject.canUpdateRelease());
        subject.updateToRelease();
        assertFalse(subject.canUpdateRelease());
        assertEquals(NexusRepositoryServiceMock1.LATEST_VERSION, notCurrentRepo.getVersion());
        assertEquals(NexusRepositoryServiceMock1.LATEST_VERSION, secondNotCurrentRepo.getVersion());
    }

    @Test
    public void testOldSnapshotUpdate() throws URISyntaxException {
        final INexusRepository oldSnapshotRepo = NexusRepositoryTest.createNexusRepo(
                NexusRepositoryNames.SNAPSHOT.nexusName(), "1.0.0-SNAPSHOT");
        waitForVersion(oldSnapshotRepo);
        subject.updateCurrentSelection(new StructuredSelection(new Object[] { oldSnapshotRepo }));
        assertTrue(subject.canUpdateRelease());
        subject.updateToRelease();
        assertFalse(subject.canUpdateRelease());
        assertEquals(NexusRepositoryServiceMock1.LATEST_VERSION, oldSnapshotRepo.getVersion());
        assertEquals(NexusRepositoryNames.MILESTONE.nexusName(), oldSnapshotRepo.getRepositoryName());
    }

    private void waitForVersion(final INexusRepository repo) {
        do {
            try {
                Thread.sleep(1);
            } catch (final InterruptedException e) {
                fail(e.getMessage());
            }
        } while (RepositoryVersionCache.getInstance().getAllAvailableVersions(repo) == null);
    }

}
