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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.zip.ZipException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.pde.core.target.ITargetDefinition;
import org.eclipse.pde.core.target.ITargetLocation;
import org.eclipse.tycho.targeteditor.model.ILDITargetDefintion;
import org.eclipse.tycho.targeteditor.model.LDIModelFactory;
import org.eclipse.tycho.targeteditor.model.Util;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class RefreshAllCacheActionTest implements LDITargetDefinitionProvider {

    private static File updateSiteFolder;
    private static ITargetDefinition targetDefinition;
    private static ILDITargetDefintion ldiTargetDefinition;

    @BeforeClass
    public static void setupClass() throws URISyntaxException, IOException {
        updateSiteFolder = Util.createTempFolder();
        targetDefinition = createTargetDefinition(updateSiteFolder);
        ldiTargetDefinition = LDIModelFactory.createLDITargetDefinition(targetDefinition);
    }

    @AfterClass
    public static void tearDownClass() throws URISyntaxException, IOException {
        Util.deleteRecursive(updateSiteFolder);
    }

    @Test
    public void testRefresh_ReferencedRepoChanged() throws URISyntaxException, IOException {
        fillRepository(Util.getResourceFile("resources/repos/incompleteRepo.zip"));
        targetDefinition.resolve(new NullProgressMonitor());
        final IStatus firstResolutionStatus = targetDefinition.getStatus();
        assertFalse(firstResolutionStatus.isOK());

        fillRepository(Util.getResourceFile("resources/repos/completeRepo.zip"));
        RefreshAllCacheAction.refreshAllCaches(this, new NullProgressMonitor());
        targetDefinition.resolve(new NullProgressMonitor());

        final IStatus secondResolutionStatus = targetDefinition.getStatus();
        System.out.println(secondResolutionStatus);
        assertTrue(secondResolutionStatus.isOK());
    }

    private static ITargetDefinition createTargetDefinition(final File folder) throws URISyntaxException {
        final URI uri = new URI("file:/" + folder.getAbsolutePath().replace("\\", "/"));
        final String[] unitIds = new String[] { "testFeature.feature.group", "testFeature2.feature.group" };
        final String[] versions = new String[] { "1.0.0", "1.0.0" };
        final int resolutionFlags = 5;
        final ITargetLocation iubc = LDIModelFactory.getTargetPlatformService().newIULocation(unitIds, versions,
                new URI[] { uri }, resolutionFlags);
        final ITargetDefinition result = LDIModelFactory.getTargetPlatformService().newTarget();
        result.setTargetLocations(new ITargetLocation[] { iubc });
        return result;
    }

    @Override
    public ILDITargetDefintion getLDITargetDefinition() {
        return ldiTargetDefinition;
    }

    static void fillRepository(final File zipFile) throws ZipException, IOException {
        Util.cleanFolder(updateSiteFolder);
        Util.extractZip(zipFile, updateSiteFolder);
    }

}
