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
package org.eclipse.tycho.targeteditor.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.pde.core.IModelChangedEvent;
import org.eclipse.tycho.targeteditor.model.INexusRepository;
import org.eclipse.tycho.targeteditor.model.IRepository;
import org.eclipse.tycho.targeteditor.model.NexusRepository;
import org.eclipse.tycho.targeteditor.model.NexusRepositoryNames;
import org.junit.Test;

public class NexusRepositoryTest {

    String nexusURLWithTrailingSlash = "http://nexus:8081/nexus/content/repositories/build.snapshots.unzip/g1of3/g2of3/g3of3/a/v/a-v-c.zip-unzip/";
    String nexusURLWithOutTrailingSlash = "http://nexus:8081/nexus/content/repositories/build.snapshots.unzip/g1of3/g2of3/g3of3/a/v/a-v-c.zip-unzip";
    String nexusURLWithoutClassifier = "http://nexus:8081/nexus/content/repositories/build.snapshots.unzip/g1of3/g2of3/g3of3/a/v/a-v.zip-unzip/";

    String groupId = "g1of3.g2of3.g3of3";
    String artifactId = "a";
    String version = "v";
    String classifier = "c";
    String type = "zip";

    String baseUrl = "http://nexus:8081/nexus/content/repositories/";

    @Test
    public void testGetMavenVersionMetaUrl() throws URISyntaxException {
        final NexusRepository nexusRepo = (NexusRepository) createRepository(nexusURLWithTrailingSlash);
        assertEquals(
                "http://nexus:8081/nexus/content/repositories/build.snapshots.unzip/g1of3/g2of3/g3of3/a/maven-metadata.xml",
                nexusRepo.getMavenVersionMetaUrl().toString());
    }

    @Test
    public void testCreateRepository() throws URISyntaxException {
        final IRepository repo = createRepository(nexusURLWithTrailingSlash);
        assertTrue(repo instanceof INexusRepository);
        final INexusRepository nexusRepo = (INexusRepository) repo;
        assertEquals(groupId, nexusRepo.getGroupId());
        assertEquals(artifactId, nexusRepo.getArtifactId());
        assertEquals(version, nexusRepo.getVersion());
        assertEquals(classifier, nexusRepo.getClassifier());
        assertEquals(type, nexusRepo.getType());
    }

    @Test
    public void testCreateRepositoryWithoutSlash() throws URISyntaxException {
        final IRepository repo = createRepository(nexusURLWithOutTrailingSlash);
        assertTrue(repo instanceof INexusRepository);
        final INexusRepository nexusRepo = (INexusRepository) repo;
        assertEquals(groupId, nexusRepo.getGroupId());
        assertEquals(artifactId, nexusRepo.getArtifactId());
        assertEquals(version, nexusRepo.getVersion());
        assertEquals(classifier, nexusRepo.getClassifier());
        assertEquals(type, nexusRepo.getType());
    }

    @Test
    public void testCreateRepositoryWithoutClassifier() throws URISyntaxException {
        final IRepository repo = createRepository(nexusURLWithoutClassifier);
        assertTrue(repo instanceof INexusRepository);
        final INexusRepository nexusRepo = (INexusRepository) repo;
        assertEquals(groupId, nexusRepo.getGroupId());
        assertEquals(artifactId, nexusRepo.getArtifactId());
        assertEquals(version, nexusRepo.getVersion());
        assertNull(nexusRepo.getClassifier());
        assertEquals(type, nexusRepo.getType());
    }

    @Test
    public void testCreateNonNexusRepositoryReasonHost() throws URISyntaxException {
        final String repoUrl = "http://notnexus:8081/nexus/content/repositories/build.snapshots.unzip/g1of3/g2of3/g3of3/a/v/a-v-c.zip-unzip/";
        final IRepository repo = createRepository(repoUrl);
        assertFalse("\"notnexus\" is a not allowed host name for a nexus repo.", repo instanceof INexusRepository);
    }

    @Test
    public void testCreateNonNexusRepositoryReasonBaseURL() throws URISyntaxException {
        final String repoUrl = "http://nexus:8081/nexus/content/shadows/build.snapshots.unzip/g1of3/g2of3/g3of3/a/v/a-v-c.zip-unzip/";
        final IRepository repo = createRepository(repoUrl);
        assertFalse("Shadows is not allowed for a nexus repo.", repo instanceof INexusRepository);
    }

    @Test
    public void testCreateNonNexusRepositoryReasonTooFewSegments() throws URISyntaxException {
        final String repoUrl = "http://nexus:8081/nexus/content/repositories/build.snapshots.unzip/a/v/a-v-c.zip-unzip/";
        final IRepository repo = createRepository(repoUrl);
        assertFalse("Too few segments for a nexus repo. BaseUrl + five segments needed.",
                repo instanceof INexusRepository);
    }

    @Test
    public void testRepositoryWithDeepContent() throws URISyntaxException {
        final String repoUrl = "http://nexus:8081/nexus/content/repositories/build.milestones.unzip/g1/g2/a/v/a-v-assembly.zip-unzip/deep/";
        final IRepository repo = createRepository(repoUrl);
        assertTrue("Deep Url not detected", repo instanceof INexusRepository);
        final INexusRepository nexusRepo = (INexusRepository) repo;
        assertEquals("Deep Url resolved incorrect", "a", nexusRepo.getArtifactId());
        assertEquals("Deep Url resolved incorrect", "g1.g2", nexusRepo.getGroupId());
        assertEquals("Deep Url resolved incorrect", "v", nexusRepo.getVersion());
        assertEquals("Deep Url resolved incorrect", "assembly", nexusRepo.getClassifier());
        assertEquals("Deep Url resolved incorrect", "zip", nexusRepo.getType());
        assertEquals("Deep Url resolved incorrect", repoUrl, nexusRepo.getURI().toString());
    }

    @Test
    public void testSetRepositoryName() throws URISyntaxException {
        final INexusRepository repo = getSampleRepository();
        repo.setRepositoryName(NexusRepositoryNames.SNAPSHOT.nexusName());
        assertSame(NexusRepositoryNames.SNAPSHOT, NexusRepositoryNames.byNexusName(repo.getRepositoryName()));
    }

    private INexusRepository getSampleRepository() throws URISyntaxException {
        final String repoUrl = "http://nexus:8081/nexus/content/repositories/build.milestones.unzip/g1/g2/a/v/a-v-assembly.zip-unzip";
        final INexusRepository repo = (INexusRepository) createRepository(repoUrl);
        return repo;
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetRepositoryNameFailure() throws URISyntaxException {
        final INexusRepository repo = getSampleRepository();
        repo.setRepositoryName(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetRepositoryNameFailureEmpty() throws URISyntaxException {
        final INexusRepository repo = getSampleRepository();
        repo.setRepositoryName("");
    }

    @Test
    public void testSetRepositoryNameNotification() throws URISyntaxException {
        final INexusRepository repo = getSampleRepository();
        final CollectingModelChangeListener listener = new CollectingModelChangeListener();
        repo.addModelChangedListener(listener);
        repo.setRepositoryName(NexusRepositoryNames.SNAPSHOT.nexusName());
        final IModelChangedEvent receivedEvent = listener.getCollectedEvents().get(0);
        assertNotNull(receivedEvent);
        assertEquals(NexusRepositoryNames.SNAPSHOT.nexusName(), receivedEvent.getNewValue());
        assertEquals(NexusRepositoryNames.MILESTONE.nexusName(), receivedEvent.getOldValue());
        assertEquals("repositoryName", receivedEvent.getChangedProperty());
        assertEquals(repo, receivedEvent.getChangedObjects()[0]);
    }

    @Test
    public void testGetURI() throws URISyntaxException {
        INexusRepository repo = (INexusRepository) createRepository(nexusURLWithTrailingSlash);
        assertEquals(nexusURLWithTrailingSlash, repo.getURI().toString());
        repo.setVersion("v2");
        assertEquals(
                "http://nexus:8081/nexus/content/repositories/build.snapshots.unzip/g1of3/g2of3/g3of3/a/v2/a-v2-c.zip-unzip/",
                repo.getURI().toString());

        repo = (INexusRepository) NexusRepository.createRepository(new URI(nexusURLWithOutTrailingSlash));
        assertEquals(nexusURLWithOutTrailingSlash, repo.getURI().toString());
    }

    public static INexusRepository createNexusRepo(final String repoName, final String version)
            throws URISyntaxException {
        final String repoUrl = "http://nexus:8081/nexus/content/repositories/repoName/gId/aId/version/aId-version.zip-unzip/";
        final INexusRepository repo = (INexusRepository) createRepository(repoUrl);
        repo.setVersion(version);
        repo.setRepositoryName(repoName);
        return repo;
    }

    public static IRepository createRepository(final String url) throws URISyntaxException {
        return NexusRepository.createRepository(new URI(url));
    }
}
