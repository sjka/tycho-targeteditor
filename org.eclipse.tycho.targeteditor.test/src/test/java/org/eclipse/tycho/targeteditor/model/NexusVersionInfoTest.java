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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import junit.framework.Assert;

import org.eclipse.tycho.targeteditor.model.INexusVersionInfo;
import org.eclipse.tycho.targeteditor.model.NexusRepositoryService;
import org.eclipse.tycho.targeteditor.model.NexusVersionInfo;
import org.eclipse.ui.WorkbenchException;
import org.junit.Test;

public class NexusVersionInfoTest {

    final static String TEST_MAVENMETADATA_LOCATION = "resources/maven-metadata.xml";

    @Test
    public void testGetVersions() throws URISyntaxException, MalformedURLException, IOException, WorkbenchException {
        final NexusRepositoryService service = new NexusRepositoryService();
        final INexusVersionInfo nexusVersionInfo = new NexusVersionInfo(service.getMemento(Util.getResourceURI(
                TEST_MAVENMETADATA_LOCATION).toURL()));
        Assert.assertEquals("0.8.0-SNAPSHOT", nexusVersionInfo.getLatestVersion());
        Assert.assertEquals("0.7.1", nexusVersionInfo.getLatestReleaseVersion());
        Assert.assertEquals(9, nexusVersionInfo.getVersions().size());
    }

    @Test
    public void testGetMissionLastRelease() throws URISyntaxException, MalformedURLException, IOException,
            WorkbenchException {
        final NexusRepositoryService service = new NexusRepositoryService();
        final INexusVersionInfo nexusVersionInfo = new NexusVersionInfo(service.getMemento(Util.getResourceURI(
                "resources/maven-metadata_1.xml").toURL()));
        Assert.assertEquals("0.8.0-SNAPSHOT", nexusVersionInfo.getLatestVersion());
        Assert.assertNull(nexusVersionInfo.getLatestReleaseVersion());
    }

    @Test(expected = WorkbenchException.class)
    public void testNotParsable() throws Exception {
        final NexusRepositoryService service = new NexusRepositoryService();
        new NexusVersionInfo(service.getMemento(Util.getResourceURI("resources/maven-metadata_invalid.xml").toURL()));
    }

    @Test
    public void testMissingTagVersions() throws Exception {
        final NexusRepositoryService service = new NexusRepositoryService();
        try {
            new NexusVersionInfo(service.getMemento(Util.getResourceURI("resources/maven-metadata_invalid1.xml")
                    .toURL()));
            Assert.fail();
        } catch (final WorkbenchException e) {
            Assert.assertTrue(e.getMessage().contains("versions"));
        }
    }

    @Test
    public void testMissingTagVersioning() throws Exception {
        final NexusRepositoryService service = new NexusRepositoryService();
        try {
            new NexusVersionInfo(service.getMemento(Util.getResourceURI("resources/maven-metadata_invalid2.xml")
                    .toURL()));
            Assert.fail();
        } catch (final WorkbenchException e) {
            Assert.assertTrue(e.getMessage().contains("versioning"));
        }
    }

    @Test
    public void testMissingTagLatest() throws URISyntaxException, MalformedURLException, IOException {
        final NexusRepositoryService service = new NexusRepositoryService();
        try {
            new NexusVersionInfo(service.getMemento(Util.getResourceURI("resources/maven-metadata_invalid3.xml")
                    .toURL()));
            Assert.fail();
        } catch (final WorkbenchException e) {
            Assert.assertTrue(e.getMessage().contains("latest"));
        }
    }

}
