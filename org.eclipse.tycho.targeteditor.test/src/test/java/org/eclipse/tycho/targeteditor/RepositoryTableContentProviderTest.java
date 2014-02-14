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

import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.pde.core.target.ITargetDefinition;
import org.eclipse.pde.internal.core.target.DirectoryBundleContainer;
import org.eclipse.pde.internal.core.target.FeatureBundleContainer;
import org.eclipse.pde.internal.core.target.ProfileBundleContainer;
import org.eclipse.swt.SWT;
import org.eclipse.tycho.targeteditor.model.ILDITargetDefintion;
import org.eclipse.tycho.targeteditor.model.INexusRepository;
import org.eclipse.tycho.targeteditor.model.IRepository;
import org.eclipse.tycho.targeteditor.model.LDIModelFactory;
import org.eclipse.tycho.targeteditor.model.NexusRepositoryNames;
import org.eclipse.tycho.targeteditor.model.Util;
import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("restriction")
public class RepositoryTableContentProviderTest {

    @Test(expected = IllegalArgumentException.class)
    public void testGetElementsNull() {
        final RepositoryTableContentProvider rtcp = new RepositoryTableContentProvider();
        rtcp.getElements(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetElementsIllegalInput() throws Exception {
        final RepositoryTableContentProvider rtcp = new RepositoryTableContentProvider();
        rtcp.getElements(new Object());
    }

    @Test
    public void testGetElements() throws Exception {
        final RepositoryTableContentProvider rtcp = new RepositoryTableContentProvider();
        final ITargetDefinition targetDefinition = Util.loadTargetDefinition(Util.REPOREF_TARGET_DEFINITION_PATH);
        final ILDITargetDefintion ldiTargetDefinition = LDIModelFactory.createLDITargetDefinition(targetDefinition);
        final Object[] elements = rtcp.getElements(ldiTargetDefinition);
        Assert.assertEquals(2, elements.length);

        // the order of elements is as defined in the target definition file
        Assert.assertTrue(elements[0] instanceof INexusRepository);
        Assert.assertTrue(elements[0] instanceof IRepository);

        Assert.assertTrue(elements[1] instanceof IRepository);
        Assert.assertFalse(elements[1] instanceof INexusRepository);
    }

    @Test
    public void testGetElementsForRepositoriesOnly() throws Exception {
        final RepositoryTableContentProvider rtcp = new RepositoryTableContentProvider();
        final ITargetDefinition targetDefinition = Util.loadTargetDefinition(Util.REPOTYPES_TARGET_DEFINITION_PATH);
        final ILDITargetDefintion ldiTargetDefinition = LDIModelFactory.createLDITargetDefinition(targetDefinition);
        final Object[] elements = rtcp.getElements(ldiTargetDefinition);
        Assert.assertEquals(4, elements.length);

        // the list shows the repositories first and other location types last
        Assert.assertFalse(elements[0] instanceof INexusRepository);
        Assert.assertTrue(elements[0] instanceof IRepository);

        Assert.assertFalse(elements[1] instanceof IRepository);
        Assert.assertTrue(elements[1] instanceof DirectoryBundleContainer);

        Assert.assertFalse(elements[2] instanceof IRepository);
        Assert.assertTrue(elements[2] instanceof ProfileBundleContainer);

        Assert.assertFalse(elements[3] instanceof IRepository);
        Assert.assertTrue(elements[3] instanceof FeatureBundleContainer);
    }

    @Test
    public void testSnapshotRepositoryDecoration() throws URISyntaxException {
        final INexusRepository snapshotRepository = LDIModelFactory.createNexusRepository(
                NexusRepositoryNames.SNAPSHOT, "dummy.group", "dummyArtifact", "1.0.0", null, "zip");
        final INexusRepository milestoneRepository = LDIModelFactory.createNexusRepository(
                NexusRepositoryNames.MILESTONE, "dummy.group", "dummyArtifact", "1.0.0", null, "zip");
        final INexusRepository releaseRepository = LDIModelFactory.createNexusRepository(NexusRepositoryNames.RELEASE,
                "dummy.group", "dummyArtifact", "1.0.0", null, "zip");
        final IRepository nonNexusRepository = Util.createRepository(new URI("someUri"));
        assertTrue(RepositoryTableLabelDecorator.getDecorationColor(snapshotRepository) == SWT.COLOR_DARK_YELLOW);
        assertTrue(RepositoryTableLabelDecorator.getDecorationColor(milestoneRepository) == -1);
        assertTrue(RepositoryTableLabelDecorator.getDecorationColor(releaseRepository) == -1);
        assertTrue(RepositoryTableLabelDecorator.getDecorationColor(nonNexusRepository) == -1);
        assertTrue(RepositoryTableLabelDecorator.getDecorationColor(null) == -1);
    }
}
