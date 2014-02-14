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

import org.eclipse.pde.core.target.ITargetLocation;
import org.eclipse.pde.internal.core.target.AbstractBundleContainer;
import org.eclipse.pde.internal.core.target.DirectoryBundleContainer;
import org.eclipse.pde.internal.core.target.ProfileBundleContainer;
import org.eclipse.pde.internal.ui.PDEPlugin;
import org.eclipse.pde.internal.ui.PDEPluginImages;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.tycho.targeteditor.Activator;
import org.eclipse.tycho.targeteditor.RepositoryTableLabelProvider;
import org.eclipse.tycho.targeteditor.model.IRepository;
import org.eclipse.tycho.targeteditor.model.LDIModelFactory;
import org.eclipse.tycho.targeteditor.model.LDIModelFactoryUtil;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@SuppressWarnings("restriction")
@RunWith(SWTBotJunit4ClassRunner.class)
public class RepositoryTableLabelProviderTest {

    @Test
    public void testGetTextNull() throws Exception {
        final RepositoryTableLabelProvider lp = new RepositoryTableLabelProvider();
        lp.getText(null);
    }

    @Test
    public void testGetTextNoRepo() throws Exception {
        final RepositoryTableLabelProvider lp = new RepositoryTableLabelProvider();
        lp.getText(new Object());
    }

    @Test
    public void testGetTextNexusRepo() throws Exception {
        final RepositoryTableLabelProvider lp = new RepositoryTableLabelProvider();
        final IRepository sampleNexusRepo = LDIModelFactoryUtil
                .createRepository("http://nexus:8081/nexus/content/repositories/build.milestones.unzip/org/eclipse/tycho/test/testartifactId/1.1.1/testartifactId-1.1.1-assembly.zip-unzip/");
        final String labelText = lp.getText(sampleNexusRepo);
        Assert.assertNotNull(labelText);
        Assert.assertTrue(labelText.indexOf("org.eclipse.tycho.test") >= 0);
        Assert.assertTrue(labelText.indexOf("testartifactId") >= 0);
        Assert.assertTrue(labelText.indexOf("1.1.1") >= 0);
    }

    @Test
    public void testGetTextNonNexusRepo() throws Exception {
        final RepositoryTableLabelProvider lp = new RepositoryTableLabelProvider();
        final IRepository sampleNonNexusRepo = LDIModelFactoryUtil.createRepository("http://dummy/repo");
        final String labelText2 = lp.getText(sampleNonNexusRepo);
        Assert.assertEquals("http://dummy/repo", labelText2);
    }

    @Test
    public void testGetImageNull() throws Exception {
        final RepositoryTableLabelProvider lp = new RepositoryTableLabelProvider();
        Assert.assertNull(lp.getImage(null));
    }

    @Test
    public void testGetImageNoRepo() throws Exception {
        final RepositoryTableLabelProvider lp = new RepositoryTableLabelProvider();
        Assert.assertNull(lp.getImage(new Object()));
    }

    @Test
    public void testGetImageForRepositories() throws Exception {
        final RepositoryTableLabelProvider lp = new RepositoryTableLabelProvider();
        final IRepository sampleNexusRepo = LDIModelFactoryUtil
                .createRepository("http://nexus:8081/nexus/content/repositories/build.milestones.unzip/org/eclipse/tycho/test/testartifactId/1.1.1/testartifactId-1.1.1-assembly.zip-unzip/");
        final Image image = lp.getImage(sampleNexusRepo);
        Assert.assertEquals(Activator.getDefault().getImageFromRegistry(Activator.NEXUS_IMG), image);

        final IRepository sampleNonNexusRepo = LDIModelFactoryUtil.createRepository("http://dummy/repo");
        final Image image2 = lp.getImage(sampleNonNexusRepo);
        Assert.assertEquals(PDEPlugin.getDefault().getLabelProvider().get(PDEPluginImages.DESC_REPOSITORY_OBJ, 0),
                image2);
    }

    @Test
    public void testGetImageForNonRepositories() throws Exception {
        final RepositoryTableLabelProvider lp = new RepositoryTableLabelProvider();
        final DirectoryBundleContainer directoryBundleContainer = new DirectoryBundleContainer("C:/test");
        final Image directoryImage = lp.getImage(directoryBundleContainer);
        Assert.assertEquals(
                PDEPlugin
                        .getDefault()
                        .getLabelProvider()
                        .get(PlatformUI.getWorkbench().getSharedImages()
                                .getImageDescriptor(ISharedImages.IMG_OBJ_FOLDER), 0), directoryImage);

        final AbstractBundleContainer profileBundleContainer = new ProfileBundleContainer("home", "confLocation");
        Assert.assertTrue(profileBundleContainer instanceof ProfileBundleContainer);
        final Image profileImage = lp.getImage(profileBundleContainer);
        Assert.assertEquals(PDEPlugin.getDefault().getLabelProvider().get(PDEPluginImages.DESC_PRODUCT_DEFINITION, 0),
                profileImage);

        final ITargetLocation newFeatureContainer = LDIModelFactory.getTargetPlatformService().newFeatureLocation(
                "home", "featureId", "1.0.0");
        final Image featureImage = lp.getImage(newFeatureContainer);
        Assert.assertEquals(PDEPlugin.getDefault().getLabelProvider().get(PDEPluginImages.DESC_FEATURE_OBJ, 0),
                featureImage);

    }
}
