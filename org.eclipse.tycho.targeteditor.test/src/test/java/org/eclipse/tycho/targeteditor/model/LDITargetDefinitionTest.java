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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.equinox.p2.metadata.IVersionedId;
import org.eclipse.equinox.p2.metadata.Version;
import org.eclipse.equinox.p2.metadata.VersionedId;
import org.eclipse.pde.core.IModelChangedEvent;
import org.eclipse.pde.core.target.ITargetDefinition;
import org.eclipse.pde.internal.core.target.IUBundleContainer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("restriction")
public class LDITargetDefinitionTest {

    private ILDITargetDefintion repoTypesTargetDefinition;
    private ILDITargetDefintion repoRefTargetDefinition;
    private ILDITargetDefintion emptyTargetDefinition;
    private URI repoURI_test_1_1_1;

    @Before
    public void setup() throws IOException, CoreException, URISyntaxException {
        repoURI_test_1_1_1 = new URI(
                "http://nexus:8081/nexus/content/repositories/build.milestones.unzip/org/eclipse/tycho/test/testartifactId/1.1.1/testartifactId-1.1.1-assembly.zip-unzip/");

        final ITargetDefinition repoTypesTarget = Util.loadTargetDefinition(Util.REPOTYPES_TARGET_DEFINITION_PATH);
        repoTypesTargetDefinition = LDIModelFactory.createLDITargetDefinition(repoTypesTarget);

        final ITargetDefinition repoRefTarget = Util.loadTargetDefinition(Util.REPOREF_TARGET_DEFINITION_PATH);
        repoRefTargetDefinition = LDIModelFactory.createLDITargetDefinition(repoRefTarget);

        final ITargetDefinition emptyTarget = LDIModelFactory.getTargetPlatformService().newTarget();
        emptyTargetDefinition = LDIModelFactory.createLDITargetDefinition(emptyTarget);
    }

    @Test
    public void testRepositoryLists() throws Exception {
        Assert.assertNotNull(repoTypesTargetDefinition);
        Assert.assertTrue(repoTypesTargetDefinition.getNonRepoLocations().size() == 3);
        Assert.assertTrue(repoTypesTargetDefinition.getRepositoryLocations().size() == 1);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testUnmodifiableRepositoryList() throws IOException, CoreException {
        final ITargetDefinition target = Util.loadTargetDefinition(Util.REPOREF_TARGET_DEFINITION_PATH);
        final ILDITargetDefintion ldiTargetDefinition = LDIModelFactory.createLDITargetDefinition(target);

        ldiTargetDefinition.getRepositoryLocations().remove(0);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testUnmodifiableNonRepositoryList() throws IOException, CoreException {
        final ITargetDefinition target = Util.loadTargetDefinition(Util.REPOREF_TARGET_DEFINITION_PATH);
        final ILDITargetDefintion ldiTargetDefinition = LDIModelFactory.createLDITargetDefinition(target);

        ldiTargetDefinition.getNonRepoLocations().remove(0);
    }

    @Test
    public void testRepository() throws Exception {
        Assert.assertNotNull(repoTypesTargetDefinition);
        Assert.assertTrue(repoTypesTargetDefinition.getRepositoryLocations().size() == 1);
        final ILDIRepositoryLocation repositoryLocation = repoTypesTargetDefinition.getRepositoryLocations().get(0);
        final List<IRepository> repositories = repositoryLocation.getRepositories();
        Assert.assertTrue(repositories.size() == 1);
        final IRepository repository = repositories.get(0);
        Assert.assertEquals(new URI("http://dummyUrl/"), repository.getURI());
    }

    @Test
    public void testRepositoryChanges() throws Exception {
        final ITargetDefinition target = Util.loadTargetDefinition(Util.REPOREF_TARGET_DEFINITION_PATH);
        final ILDITargetDefintion ldiTargetDefinition = LDIModelFactory.createLDITargetDefinition(target);
        Assert.assertNotNull(ldiTargetDefinition);
        Assert.assertTrue(ldiTargetDefinition.getRepositoryLocations().size() == 2);
        final ILDIRepositoryLocation repositoryLocation = ldiTargetDefinition.getRepositoryLocations().get(0);
        final List<IRepository> repositories = repositoryLocation.getRepositories();
        Assert.assertTrue(repositories.size() == 1);
        final IRepository repository = repositories.get(0);
        Assert.assertEquals(repoURI_test_1_1_1, repository.getURI());

        final String newVersion = "2.2.2";
        if (repository instanceof INexusRepository) {
            final INexusRepository nexusRepository = (INexusRepository) repository;
            nexusRepository.setVersion(newVersion);
            // check new version in model object
            Assert.assertEquals(newVersion, nexusRepository.getVersion());
        }

        // check new version in model uri
        Assert.assertEquals(
                new URI(
                        "http://nexus:8081/nexus/content/repositories/build.milestones.unzip/org/eclipse/tycho/test/testartifactId/2.2.2/testartifactId-2.2.2-assembly.zip-unzip/"),
                repository.getURI());
        // check new version in underlying target definition model
        final URI targetModelURI = ((IUBundleContainer) target.getTargetLocations()[0]).getRepositories()[0];
        Assert.assertEquals(repository.getURI(), targetModelURI);
    }

    @Test
    public void testUnitsChange() throws IOException, CoreException {
        final ITargetDefinition target = Util.loadTargetDefinition(Util.REPOREF_TARGET_DEFINITION_PATH);
        final ILDITargetDefintion ldiTargetDefinition = LDIModelFactory.createLDITargetDefinition(target);
        Assert.assertNotNull(ldiTargetDefinition);
        Assert.assertTrue(ldiTargetDefinition.getRepositoryLocations().size() == 2);

        // first repo is from Nexus
        final ILDIRepositoryLocation repositoryLocation = ldiTargetDefinition.getRepositoryLocations().get(0);
        final List<IMutableVersionedId> units = repositoryLocation.getUnits();
        Assert.assertTrue(units.size() == 2);
        changeUnitVersion(target, units);
        changeUnitId(target, units);
    }

    @Test
    public void testMultiInstanceRepositoryChange() throws IOException, CoreException {
        final ITargetDefinition target = Util.loadTargetDefinition(Util.REPOREF_TARGET_DEFINITION_PATH);
        final ILDITargetDefintion ldiTargetDefinition = LDIModelFactory.createLDITargetDefinition(target);

        // first repo is from Nexus
        final ILDIRepositoryLocation repositoryLocation1 = ldiTargetDefinition.getRepositoryLocations().get(0);
        final ILDIRepositoryLocation repositoryLocation2 = ldiTargetDefinition.getRepositoryLocations().get(0);

        final INexusRepository nexusRepo1 = (INexusRepository) repositoryLocation1.getRepositories().get(0);
        final INexusRepository nexusRepo2 = (INexusRepository) repositoryLocation2.getRepositories().get(0);

        nexusRepo1.setVersion("99.99.81");
        assertEquals("99.99.81", nexusRepo1.getVersion());
        assertEquals("99.99.81", nexusRepo2.getVersion());

        nexusRepo2.setVersion("1.1.1");
        assertEquals("1.1.1", nexusRepo1.getVersion());
        assertEquals("1.1.1", nexusRepo2.getVersion());
    }

    @Test
    public void testMultiInstanceUnitChange() {
        // first repo is from Nexus
        final ILDIRepositoryLocation repositoryLocation1 = repoRefTargetDefinition.getRepositoryLocations().get(0);
        final ILDIRepositoryLocation repositoryLocation2 = repoRefTargetDefinition.getRepositoryLocations().get(0);

        final IMutableVersionedId unit1 = repositoryLocation1.getUnits().get(0);
        final IMutableVersionedId unit2 = repositoryLocation2.getUnits().get(0);

        final Version version1 = Version.create("99.99.81");
        unit1.setVersion(version1);
        assertEquals(version1, unit1.getVersion());
        assertEquals(version1, unit2.getVersion());

        final Version version2 = Version.create("1.1.1");
        unit2.setVersion(version2);
        assertEquals(version2, unit1.getVersion());
        assertEquals(version2, unit2.getVersion());
    }

    @Test
    public void testAddRepository() {
        final CollectingModelChangeListener listener = new CollectingModelChangeListener();
        emptyTargetDefinition.addModelChangedListener(listener);
        final ILDIRepositoryLocation addedLocation = emptyTargetDefinition.addRepository(repoURI_test_1_1_1);
        assertNotNull(addedLocation);
        assertTrue(emptyTargetDefinition.getRepositoryLocations().contains(addedLocation));
        assertEquals(1, addedLocation.getRepositories().size());
        assertEquals(repoURI_test_1_1_1, addedLocation.getRepositories().get(0).getURI());
        final List<IModelChangedEvent> collectedEvents = listener.getCollectedEvents();
        assertEquals(1, collectedEvents.size());
        assertEquals(collectedEvents.get(0).getNewValue(), addedLocation);
    }

    @Test
    public void testRemoveRepository() {
        final CollectingModelChangeListener listener = new CollectingModelChangeListener();
        repoRefTargetDefinition.addModelChangedListener(listener);
        final List<ILDIRepositoryLocation> repositoryLocations = repoRefTargetDefinition.getRepositoryLocations();
        final int startSize = repositoryLocations.size();
        assertTrue(startSize > 0);
        final ILDIRepositoryLocation locationIntendedToBeRemoved = repositoryLocations.get(0);
        final List<IRepository> repositories = locationIntendedToBeRemoved.getRepositories();
        assertTrue(repositories.size() > 0);
        final IRepository repositoryToBeRemoved = repositories.get(0);
        final ILDIRepositoryLocation locationToBeRemoved = repoRefTargetDefinition
                .findLocationOf(repositoryToBeRemoved);
        repoRefTargetDefinition.removeLocation(locationToBeRemoved);
        assertTrue(repoRefTargetDefinition.getRepositoryLocations().size() == (startSize - 1));
        assertSame(locationIntendedToBeRemoved, locationToBeRemoved);
        final List<IModelChangedEvent> collectedEvents = listener.getCollectedEvents();
        assertEquals(1, collectedEvents.size());
        final IModelChangedEvent event = collectedEvents.get(0);
        assertEquals(event.getChangeType(), IModelChangedEvent.REMOVE);
        assertNull(event.getChangedProperty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRepositoryFromWrongTargetDefinition() {
        final CollectingModelChangeListener listener = new CollectingModelChangeListener();
        repoRefTargetDefinition.addModelChangedListener(listener);
        final List<ILDIRepositoryLocation> repositoryLocations = repoRefTargetDefinition.getRepositoryLocations();
        final List<IRepository> repositories = repositoryLocations.get(0).getRepositories();
        final IRepository repositoryToBeRemoved = repositories.get(0);
        final ILDIRepositoryLocation locationToBeRemoved = emptyTargetDefinition.findLocationOf(repositoryToBeRemoved);
        emptyTargetDefinition.removeLocation(locationToBeRemoved);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveNullLocation() {
        repoRefTargetDefinition.removeLocation(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testfindNullRepository() {
        repoRefTargetDefinition.findLocationOf(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddNullRepository() {
        emptyTargetDefinition.addRepository(null);
    }

    @Test
    public void testAddUnits() {
        final ILDIRepositoryLocation location = emptyTargetDefinition.addRepository(repoURI_test_1_1_1);
        final CollectingModelChangeListener listener = new CollectingModelChangeListener();
        emptyTargetDefinition.addModelChangedListener(listener);
        final IVersionedId newUnit1 = new VersionedId("unitId1", "1.0.0");
        final IVersionedId newUnit2 = new VersionedId("unitId2", "2.0.0");
        assertEquals(0, location.getUnits().size());

        final IMutableVersionedId[] addedUnits = location.addUnits(newUnit1, newUnit2);

        assertEquals(2, addedUnits.length);
        assertEquals(newUnit1.getId(), addedUnits[0].getId());
        assertEquals(newUnit1.getVersion(), addedUnits[0].getVersion());
        assertEquals(newUnit2.getId(), addedUnits[1].getId());
        assertEquals(newUnit2.getVersion(), addedUnits[1].getVersion());

        assertTrue(location.getUnits().contains(addedUnits[0]));
        assertTrue(location.getUnits().contains(addedUnits[1]));
        final List<IModelChangedEvent> collectedEvents = listener.getCollectedEvents();
        assertEquals(1, collectedEvents.size());
        final IModelChangedEvent event = collectedEvents.get(0);
        assertEquals(ILDITargetDefintion.EVENT_ADDED_UNITS, event.getChangedProperty());
    }

    @Test
    public void testAddEmptyUnits() {
        final ILDIRepositoryLocation location = emptyTargetDefinition.addRepository(repoURI_test_1_1_1);
        final CollectingModelChangeListener listener = new CollectingModelChangeListener();
        emptyTargetDefinition.addModelChangedListener(listener);

        final IMutableVersionedId[] addedUnits = location.addUnits();

        assertEquals(0, addedUnits.length);
        final List<IModelChangedEvent> collectedEvents = listener.getCollectedEvents();
        assertEquals(0, collectedEvents.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddUnitsWithNullArray() {
        final ILDIRepositoryLocation location = emptyTargetDefinition.addRepository(repoURI_test_1_1_1);
        location.addUnits((IVersionedId) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddUnitsWithNullInArray() {
        final ILDIRepositoryLocation location = emptyTargetDefinition.addRepository(repoURI_test_1_1_1);
        location.addUnits(new VersionedId("unitId1", "1.0.0"), null);
    }

    @Test
    public void testGetResolutionStatus() throws IOException, CoreException {
        final ITargetDefinition targetDefinition = Util.loadTargetDefinition(Util.REPOREF_SIMPLE_RESOLVE_TEST_PATH);
        final ILDITargetDefintion ldiTargetDefinition = LDIModelFactory.createLDITargetDefinition(targetDefinition);
        targetDefinition.resolve(new NullProgressMonitor());
        final IStatus[] targetProblems = targetDefinition.getStatus().getChildren();
        final IStatus[] ldiTargetProblems = ldiTargetDefinition.getResolutionStatus().getChildren();
        Assert.assertTrue(targetProblems.length == ldiTargetProblems.length);
        System.out.println(targetProblems.length);
        for (int i = 0; i < ldiTargetProblems.length; i++) {
            Assert.assertTrue(targetProblems[i] == ldiTargetProblems[i]);
        }
    }

    private void changeUnitVersion(final ITargetDefinition target, final List<IMutableVersionedId> units) {
        final IMutableVersionedId unit1 = units.get(0);
        Assert.assertEquals(new MutableVersionedId("dummy1.feature.id.feature.group", Version.create("1.1.1")), unit1);

        final String newVersion = "2.2.2";
        unit1.setVersion(Version.create(newVersion));
        // check new version in model object
        Assert.assertEquals(Version.create(newVersion), unit1.getVersion());

        // check new version in underlying target definition model
        final Version[] versions = LDIModelFactory.getVersions((IUBundleContainer) target.getTargetLocations()[0]);
        Assert.assertEquals(newVersion, versions[0].toString());
    }

    private void changeUnitId(final ITargetDefinition target, final List<IMutableVersionedId> units) {
        final IMutableVersionedId unit2 = units.get(1);
        final MutableVersionedId expected = new MutableVersionedId("dummy2.feature.id.feature.group",
                Version.create("1.1.2"));
        Assert.assertEquals(expected, unit2);
        Assert.assertEquals(expected.hashCode(), unit2.hashCode());

        final String newId = "dummy2.feature.changed.id.feature.group";
        unit2.setId(newId);
        // check new version in model object
        Assert.assertEquals(newId, unit2.getId());

        // check new version in underlying target definition model
        final String[] ids = LDIModelFactory.getIds((IUBundleContainer) target.getTargetLocations()[0]);
        Assert.assertEquals(newId, ids[1]);
    }

}
