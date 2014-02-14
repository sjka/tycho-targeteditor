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
import static org.junit.Assert.assertSame;

import java.net.URISyntaxException;

import org.eclipse.tycho.targeteditor.model.EDynamicVersions;
import org.eclipse.tycho.targeteditor.model.INexusRepository;
import org.eclipse.tycho.targeteditor.model.NexusRepositoryNames;
import org.eclipse.tycho.targeteditor.model.LDIModelFactory;
import org.junit.Test;

public class AdjustRepositoryNameTest {

    @Test
    public void testAdjustRepositoryName_mTom() throws URISyntaxException {
        final INexusRepository repo = NexusRepositoryTest.createNexusRepo(NexusRepositoryNames.MILESTONE.nexusName(),
                "1.0.0");
        LDIModelFactory.adjustRepositoryName(repo);
        assertSame(NexusRepositoryNames.MILESTONE, NexusRepositoryNames.byNexusName(repo.getRepositoryName()));
    }

    @Test
    public void testAdjustRepositoryName_sTos() throws URISyntaxException {
        final INexusRepository repo = NexusRepositoryTest.createNexusRepo(NexusRepositoryNames.SNAPSHOT.nexusName(),
                "1.0.0-SNAPSHOT");
        LDIModelFactory.adjustRepositoryName(repo);
        assertSame(NexusRepositoryNames.SNAPSHOT, NexusRepositoryNames.byNexusName(repo.getRepositoryName()));
    }

    @Test
    public void testAdjustRepositoryName_sTom() throws URISyntaxException {
        final INexusRepository repo = NexusRepositoryTest.createNexusRepo(NexusRepositoryNames.SNAPSHOT.nexusName(),
                "1.0.0");
        LDIModelFactory.adjustRepositoryName(repo);
        assertSame(NexusRepositoryNames.MILESTONE, NexusRepositoryNames.byNexusName(repo.getRepositoryName()));
    }

    @Test
    public void testAdjustRepositoryName_mTos() throws URISyntaxException {
        final INexusRepository repo = NexusRepositoryTest.createNexusRepo(NexusRepositoryNames.MILESTONE.nexusName(),
                "1.0.0-SNAPSHOT");
        LDIModelFactory.adjustRepositoryName(repo);
        assertSame(NexusRepositoryNames.SNAPSHOT, NexusRepositoryNames.byNexusName(repo.getRepositoryName()));
    }

    @Test
    public void testAdjustRepositoryName_rTos() throws URISyntaxException {
        final INexusRepository repo = NexusRepositoryTest.createNexusRepo(NexusRepositoryNames.RELEASE.nexusName(),
                "1.0.0-SNAPSHOT");
        LDIModelFactory.adjustRepositoryName(repo);
        assertSame(NexusRepositoryNames.SNAPSHOT, NexusRepositoryNames.byNexusName(repo.getRepositoryName()));
    }

    @Test
    public void testAdjustRepositoryName_xTox() throws URISyntaxException {
        final INexusRepository repo = NexusRepositoryTest.createNexusRepo("x", "1.0.0-SNAPSHOT");
        LDIModelFactory.adjustRepositoryName(repo);
        assertEquals("x", repo.getRepositoryName());
        repo.setVersion("1.0.0");
        assertEquals("x", repo.getRepositoryName());
    }

    @Test
    public void testAdjustRepositoryName_mWithDynamicVersionSnapshotTos() throws URISyntaxException {
        final INexusRepository repo = NexusRepositoryTest.createNexusRepo(NexusRepositoryNames.MILESTONE.nexusName(),
                EDynamicVersions.SNAPSHOT.name());
        LDIModelFactory.adjustRepositoryName(repo);
        assertSame(NexusRepositoryNames.SNAPSHOT, NexusRepositoryNames.byNexusName(repo.getRepositoryName()));
    }

    @Test
    public void testAdjustRepositoryName_rWithDynamicVersionReleaseTos() throws URISyntaxException {
        final INexusRepository repo = NexusRepositoryTest.createNexusRepo(NexusRepositoryNames.RELEASE.nexusName(),
                EDynamicVersions.RELEASE.name());
        LDIModelFactory.adjustRepositoryName(repo);
        assertSame(NexusRepositoryNames.SNAPSHOT, NexusRepositoryNames.byNexusName(repo.getRepositoryName()));
    }

    @Test
    public void testAdjustRepositoryName_sWithDynamicVersionReleaseTos() throws URISyntaxException {
        final INexusRepository repo = NexusRepositoryTest.createNexusRepo(NexusRepositoryNames.SNAPSHOT.nexusName(),
                EDynamicVersions.RELEASE.name());
        LDIModelFactory.adjustRepositoryName(repo);
        assertSame(NexusRepositoryNames.SNAPSHOT, NexusRepositoryNames.byNexusName(repo.getRepositoryName()));
    }
}
