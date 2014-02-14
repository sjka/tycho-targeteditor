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
package org.eclipse.tycho.targeteditor.dialogs;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.eclipse.tycho.targeteditor.model.IRepository;
import org.eclipse.tycho.targeteditor.model.NexusRepositoryTest;
import org.eclipse.tycho.targeteditor.model.Util;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AddRepositoryManuallyTest {

    private IRepository sampleRepo;

    private static File updateSiteFolder;

    @Before
    public void setup() throws URISyntaxException, IOException {
        updateSiteFolder = Util.createTempFolder();
    }

    @After
    public void tearDown() throws URISyntaxException, IOException {
        Util.deleteRecursive(updateSiteFolder);
    }

    @Test
    public void testUpdateSiteRepoWithIUs() throws Exception {
        Util.cleanFolder(updateSiteFolder);
        Util.extractZip(Util.getResourceFile("resources/repos/updatesiteWithIU.zip"), updateSiteFolder);
        final String uri = updateSiteFolder.toURI().toString();
        sampleRepo = NexusRepositoryTest.createRepository(uri);
        assertTrue(AddRepositoryManuallyWizardPage.validateP2Repo(sampleRepo));
    }

    @Test
    public void testValidP2Repo() throws Exception {
        Util.cleanFolder(updateSiteFolder);
        Util.extractZip(Util.getResourceFile("resources/repos/completeRepo.zip"), updateSiteFolder);
        final String uri = updateSiteFolder.toURI().toString();
        sampleRepo = NexusRepositoryTest.createRepository(uri);
        assertTrue(AddRepositoryManuallyWizardPage.validateP2Repo(sampleRepo));
    }

    @Test
    public void testUpdateSiteRepoWithoutIUs() throws Exception {
        Util.cleanFolder(updateSiteFolder);
        Util.extractZip(Util.getResourceFile("resources/repos/updatesiteWithoutIU.zip"), updateSiteFolder);
        final String uri = updateSiteFolder.toURI().toString();
        sampleRepo = NexusRepositoryTest.createRepository(uri);
        assertFalse(AddRepositoryManuallyWizardPage.validateP2Repo(sampleRepo));
    }

}
