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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.equinox.p2.metadata.Version;
import org.eclipse.pde.core.target.ITargetDefinition;
import org.eclipse.pde.core.target.ITargetLocation;
import org.eclipse.pde.core.target.TargetBundle;
import org.eclipse.pde.core.target.TargetFeature;
import org.eclipse.pde.internal.core.target.IUBundleContainer;
import org.eclipse.pde.internal.ui.editor.targetdefinition.TargetEditor;
import org.eclipse.tycho.targeteditor.model.INexusRepository;
import org.eclipse.tycho.targeteditor.model.ILDITargetDefintion;
import org.eclipse.tycho.targeteditor.model.NexusRepository;
import org.eclipse.tycho.targeteditor.model.NexusRepositoryNames;
import org.eclipse.tycho.targeteditor.model.LDIModelFactory;
import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("restriction")
public class LDIModelFactoryTest {

    @Test
    public void testFactory() throws CoreException {
        final ILDITargetDefintion targetDefinition = LDIModelFactory.createLDITargetDefinition(LDIModelFactory
                .getTargetPlatformService().newTarget());
        Assert.assertNotNull(targetDefinition);
        Assert.assertTrue(targetDefinition.getNonRepoLocations().isEmpty());
        Assert.assertTrue(targetDefinition.getRepositoryLocations().isEmpty());
    }

    @Test
    public void testIUBundleFieldAccess() throws IOException, CoreException, URISyntaxException {
        final URI uri = new URI("http://dummyUrl");
        final String[] unitIds = new String[] { "id1", "id2" };
        final String[] versions = new String[] { "1.0.0", "2.0.0" };
        final int resolutionFlags = 5;
        final IUBundleContainer iubc = (IUBundleContainer) LDIModelFactory.getTargetPlatformService().newIULocation(
                unitIds, versions, new URI[] { uri }, resolutionFlags);

        final String[] refIds = LDIModelFactory.getIds(iubc);
        Assert.assertEquals(unitIds[0], refIds[0]);
        Assert.assertEquals(unitIds[1], refIds[1]);
        final Version[] refVersions = LDIModelFactory.getVersions(iubc);
        Assert.assertEquals(versions[0], refVersions[0].toString());
        Assert.assertEquals(versions[1], refVersions[1].toString());
        final int refFlags = LDIModelFactory.getResolutionFlags(iubc);
        Assert.assertEquals(resolutionFlags, refFlags);

    }

    @Test
    public void testResolutionJobFamilyAccess() {
        final TargetEditor targetEditor = new TargetEditor();
        final Object resolveJobFamily = LDIModelFactory.getResolveJobFamily(targetEditor);
        Assert.assertNotNull(resolveJobFamily);
    }

    @Test(expected = RuntimeException.class)
    public void testFailingIUBundleFieldAccess() throws IOException, CoreException, URISyntaxException {
        LDIModelFactory.getField(new Object(), "noObjectField");
    }

    @Test
    public void testCreateRelatedSnapshotRepository() throws URISyntaxException {
        final URI repoUri = new URI("http://nexus:8081/nexus/content/repositories/"
                + NexusRepositoryNames.MILESTONE.nexusName() + "/g1/g2/a/v/a-v-assembly.zip-unzip");
        final INexusRepository repo = (INexusRepository) NexusRepository.createRepository(repoUri);
        final INexusRepository relatedSnapshotRepository = LDIModelFactory.createRelatedSnapshotRepository(repo);
        assertEquals(
                repoUri.toString().replace(NexusRepositoryNames.MILESTONE.nexusName(),
                        NexusRepositoryNames.SNAPSHOT.nexusName()), relatedSnapshotRepository.getURI().toString());
    }

    @Test
    public void testCreateRelatedSnapshotRepository_sTos() throws URISyntaxException {
        final INexusRepository initialRepo = NexusRepositoryTest.createNexusRepo(
                NexusRepositoryNames.SNAPSHOT.nexusName(), "1.0.0");
        final INexusRepository relatedSnapshotRepository = LDIModelFactory.createRelatedSnapshotRepository(initialRepo);
        assertNotSame(initialRepo, relatedSnapshotRepository);
        assertEquals(relatedSnapshotRepository.getRepositoryName(), NexusRepositoryNames.SNAPSHOT.nexusName());
        checkSkipRepoName_EqualRemainder(initialRepo, relatedSnapshotRepository);
    }

    @Test
    public void testCreateRelatedSnapshotRepository_mTos() throws URISyntaxException {
        final INexusRepository initialRepo = NexusRepositoryTest.createNexusRepo(
                NexusRepositoryNames.MILESTONE.nexusName(), "1.0.0");
        final INexusRepository relatedSnapshotRepository = LDIModelFactory.createRelatedSnapshotRepository(initialRepo);
        assertEquals(relatedSnapshotRepository.getRepositoryName(), NexusRepositoryNames.SNAPSHOT.nexusName());
        checkSkipRepoName_EqualRemainder(initialRepo, relatedSnapshotRepository);
    }

    @Test
    public void testCreateRelatedSnapshotRepository_rTos() throws URISyntaxException {
        final INexusRepository initialRepo = NexusRepositoryTest.createNexusRepo(
                NexusRepositoryNames.RELEASE.nexusName(), "1.0.0");
        final INexusRepository relatedSnapshotRepository = LDIModelFactory.createRelatedSnapshotRepository(initialRepo);
        assertEquals(relatedSnapshotRepository.getRepositoryName(), NexusRepositoryNames.SNAPSHOT.nexusName());
        checkSkipRepoName_EqualRemainder(initialRepo, relatedSnapshotRepository);
    }

    @Test
    public void testCreateRelatedSnapshotRepository_xTox() throws URISyntaxException {
        final INexusRepository initialRepo = NexusRepositoryTest.createNexusRepo("unknownRepo", "1.0.0");
        final INexusRepository relatedSnapshotRepository = LDIModelFactory.createRelatedSnapshotRepository(initialRepo);
        assertNotSame(initialRepo, relatedSnapshotRepository);
        assertEquals(relatedSnapshotRepository.getRepositoryName(), "unknownRepo");
        checkSkipRepoName_EqualRemainder(initialRepo, relatedSnapshotRepository);
    }

    private void checkSkipRepoName_EqualRemainder(final INexusRepository repo1, final INexusRepository repo2) {
        final String u1 = repo1.getURI().toString().replace(repo1.getRepositoryName(), "repoName");
        final String u2 = repo2.getURI().toString().replace(repo2.getRepositoryName(), "repoName");
        assertEquals(u1, u2);
    }

    @Test
    public void testClearResolutionState_IUBundleContainer() throws URISyntaxException {
        final URI uri = new URI("http://dummyUrl");
        final String[] unitIds = new String[] { "id1", "id2" };
        final String[] versions = new String[] { "1.0.0", "2.0.0" };
        final int resolutionFlags = 5;
        final IUBundleContainer iubc = (IUBundleContainer) LDIModelFactory.getTargetPlatformService().newIULocation(
                unitIds, versions, new URI[] { uri }, resolutionFlags);
        final ITargetDefinition newTarget = LDIModelFactory.getTargetPlatformService().newTarget();
        newTarget.setTargetLocations(new ITargetLocation[] { iubc });
        LDIModelFactory.clearResolutionStatus(iubc);
    }

    @Test
    public void testClearResolutionState_ProfileBundleContainer() throws URISyntaxException {
        final ITargetLocation profileContainer = LDIModelFactory.getTargetPlatformService().newProfileLocation(
                "c:/test/", "configLoc");
        LDIModelFactory.clearResolutionStatus(profileContainer);
    }

    @Test
    public void testClearResolutionState_DirectoryBundleContainer() throws URISyntaxException {
        final ITargetLocation directoryContainer = LDIModelFactory.getTargetPlatformService().newDirectoryLocation(
                "c:/test/");
        LDIModelFactory.clearResolutionStatus(directoryContainer);
    }

    @Test
    public void testClearResolutionState_FeatureBundleContainer() throws URISyntaxException {
        final ITargetLocation featureContainer = LDIModelFactory.getTargetPlatformService().newFeatureLocation(
                "c:/test/", "featureId", "1.0.0");
        LDIModelFactory.clearResolutionStatus(featureContainer);
    }

    @Test
    public void testClearResolutionState_FailNoMethod() throws URISyntaxException {
        Throwable expectedNoSuchMethodException = null;
        try {
            LDIModelFactory.clearResolutionStatus(new SimpleIBundleContainerImplementation());
        } catch (final Exception e) {
            expectedNoSuchMethodException = e.getCause();
        }
        assertNotNull(expectedNoSuchMethodException);
        assertTrue(expectedNoSuchMethodException instanceof NoSuchMethodException);
    }

    @Test
    public void testClearResolutionState_TestMethodCalled() throws URISyntaxException {
        final ClearableIBundleContainerImplementation container = new ClearableIBundleContainerImplementation();
        LDIModelFactory.clearResolutionStatus(container);
        assertTrue(1 == container.callCounter);
    }

    @Test
    public void testCreateNexusRepository() throws URISyntaxException {
        final INexusRepository nexusRepository_withClassifier = LDIModelFactory.createNexusRepository(
                NexusRepositoryNames.MILESTONE, "groupId", "artifactid", "0.0.0", "assembly", "zip");
        final URI assumedUri_withClassifier = new URI(
                "http://nexus:8081/nexus/content/repositories/build.milestones.unzip/groupId/artifactid/0.0.0/artifactid-0.0.0-assembly.zip-unzip");
        assertEquals(assumedUri_withClassifier, nexusRepository_withClassifier.getURI());

        final INexusRepository nexusRepository_withOutClassifier = LDIModelFactory.createNexusRepository(
                NexusRepositoryNames.MILESTONE, "groupId", "artifactid", "0.0.0", "assembly", "zip");
        final URI assumedUri_withOutClassifier = new URI(
                "http://nexus:8081/nexus/content/repositories/build.milestones.unzip/groupId/artifactid/0.0.0/artifactid-0.0.0-assembly.zip-unzip");
        assertEquals(assumedUri_withOutClassifier, nexusRepository_withOutClassifier.getURI());
    }

    @Test
    public void testCreateNexusRepositoryFromUri() throws URISyntaxException {
        assertNotNull(LDIModelFactory
                .createNexusRepository(new URI(
                        "http://nexus:8081/nexus/content/repositories/build.milestones.unzip/groupId/artifactid/0.0.0/artifactid-0.0.0-assembly.zip-unzip")));
        assertNull(LDIModelFactory.createNexusRepository(new URI("http://download.eclipse.org/")));
    }

    private class SimpleIBundleContainerImplementation implements ITargetLocation {
        @Override
        public IStatus resolve(final ITargetDefinition definition, final IProgressMonitor monitor) {
            return null;
        }

        @Override
        public boolean isResolved() {
            return false;
        }

        @Override
        public String[] getVMArguments() {
            return null;
        }

        @Override
        public IStatus getStatus() {
            return null;
        }

        @Override
        public Object getAdapter(Class adapter) {
            return null;
        }

        @Override
        public String getType() {
            return null;
        }

        @Override
        public String getLocation(boolean resolve) throws CoreException {
            return null;
        }

        @Override
        public TargetBundle[] getBundles() {
            return null;
        }

        @Override
        public TargetFeature[] getFeatures() {
            return null;
        }

        @Override
        public String serialize() {
            return null;
        }

    }

    private class ClearableIBundleContainerImplementation extends SimpleIBundleContainerImplementation {
        private int callCounter = 0;

        @SuppressWarnings("unused")
        void clearResolutionStatus() {
            this.callCounter++;
        }
    }
}
