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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.net.URISyntaxException;
import java.util.concurrent.TimeoutException;

import org.eclipse.pde.core.IModelChangedEvent;
import org.eclipse.pde.core.IModelChangedListener;
import org.eclipse.tycho.targeteditor.RepositoryVersionCache;
import org.eclipse.tycho.targeteditor.model.INexusRepository;
import org.eclipse.tycho.targeteditor.model.NexusRepositoryNames;
import org.eclipse.tycho.targeteditor.model.NexusRepositoryServiceMock1;
import org.eclipse.tycho.targeteditor.model.NexusRepositoryTest;
import org.eclipse.tycho.targeteditor.model.NexusVersionCalculationResult;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class RepositoryVersionCacheTest {

    public class RepositoryVersionChangeListener implements IModelChangedListener {

        private IModelChangedEvent latestEvent;

        @Override
        public void modelChanged(final IModelChangedEvent event) {
            synchronized (this) {
                this.latestEvent = event;
                this.notifyAll();
            }

        }

        public synchronized IModelChangedEvent getLatestEvent() {
            return latestEvent;
        }

        public void waitFor(final INexusRepository repo, final long timeout) throws InterruptedException,
                TimeoutException {
            final long entranceTime = System.currentTimeMillis();
            do {
                synchronized (this) {
                    if (latestEvent != null) {
                        final Object latestNewValue = latestEvent.getNewValue();
                        if (latestNewValue instanceof NexusVersionCalculationResult) {
                            final NexusVersionCalculationResult calcResult = (NexusVersionCalculationResult) latestNewValue;
                            if (calcResult.getRepository().getURI().equals(repo.getURI())) {
                                this.latestEvent = null;
                                return;
                            }
                        }
                    }
                    this.wait(timeout);
                }
            } while ((System.currentTimeMillis() - entranceTime) < timeout);
            throw new TimeoutException();
        }
    }

    private NexusRepositoryServiceMock1 repositoryServiceMock;
    private INexusRepository sampleRepo;

    @Before
    public void setup() throws URISyntaxException {
        repositoryServiceMock = NexusRepositoryServiceMock1.createAndRegister();
        sampleRepo = NexusRepositoryTest.createNexusRepo(NexusRepositoryNames.SNAPSHOT.nexusName(), "1.0.0");
        RepositoryVersionCache.getInstance().clean();
    }

    @After
    public void cleanup() {
        repositoryServiceMock.restoreOriginalService();
    }

    @Test
    public void testAccess() throws InterruptedException, TimeoutException {
        final RepositoryVersionChangeListener versionChangeListener = new RepositoryVersionChangeListener();
        RepositoryVersionCache.getInstance().addModelChangedListener(versionChangeListener);
        final NexusVersionCalculationResult firstAccessVersions = RepositoryVersionCache.getInstance()
                .getAllAvailableVersions(sampleRepo);
        versionChangeListener.waitFor(sampleRepo, 3000);
        RepositoryVersionCache.getInstance().removeModelChangedListener(versionChangeListener);

        assertNull(firstAccessVersions);
        assertNotNull(RepositoryVersionCache.getInstance().getAllAvailableVersions(sampleRepo));

    }

    @Test
    public void testReload() throws InterruptedException, TimeoutException {
        final RepositoryVersionChangeListener initialAccessListener = new RepositoryVersionChangeListener();
        RepositoryVersionCache.getInstance().addModelChangedListener(initialAccessListener);
        RepositoryVersionCache.getInstance().getAllAvailableVersions(sampleRepo);
        initialAccessListener.waitFor(sampleRepo, 3000);
        assertNotNull(RepositoryVersionCache.getInstance().getAllAvailableVersions(sampleRepo));
        RepositoryVersionCache.getInstance().removeModelChangedListener(initialAccessListener);

        final RepositoryVersionChangeListener reloadListener = new RepositoryVersionChangeListener();
        RepositoryVersionCache.getInstance().addModelChangedListener(reloadListener);
        RepositoryVersionCache.getInstance().reload();
        reloadListener.waitFor(sampleRepo, 3000);
        RepositoryVersionCache.getInstance().removeModelChangedListener(reloadListener);
    }

    @Test
    public void testClean() throws InterruptedException, TimeoutException {
        final RepositoryVersionChangeListener initialAccessListener = new RepositoryVersionChangeListener();
        RepositoryVersionCache.getInstance().addModelChangedListener(initialAccessListener);
        RepositoryVersionCache.getInstance().getAllAvailableVersions(sampleRepo);
        initialAccessListener.waitFor(sampleRepo, 3000);
        assertNotNull(RepositoryVersionCache.getInstance().getAllAvailableVersions(sampleRepo));
        RepositoryVersionCache.getInstance().removeModelChangedListener(initialAccessListener);

        final RepositoryVersionChangeListener cleanListener = new RepositoryVersionChangeListener();
        RepositoryVersionCache.getInstance().addModelChangedListener(cleanListener);
        RepositoryVersionCache.getInstance().clean();
        final IModelChangedEvent cleanEvent = cleanListener.getLatestEvent();
        assertNotNull(cleanEvent);
        assertEquals(RepositoryVersionCache.EVENT_PROPERTY_CACHE_CLEAN, cleanEvent.getChangedProperty());
        assertNull(RepositoryVersionCache.getInstance().getAllAvailableVersions(sampleRepo));
        cleanListener.waitFor(sampleRepo, 3000);
        RepositoryVersionCache.getInstance().removeModelChangedListener(cleanListener);
        assertNotNull(RepositoryVersionCache.getInstance().getAllAvailableVersions(sampleRepo));
    }

}
