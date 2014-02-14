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
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.eclipse.pde.core.IModelChangedEvent;
import org.eclipse.pde.core.IModelChangedListener;
import org.eclipse.tycho.targeteditor.model.INexusRepository;
import org.eclipse.tycho.targeteditor.model.INexusRepositoryService;
import org.eclipse.tycho.targeteditor.model.INexusVersionInfoCallback;
import org.eclipse.tycho.targeteditor.model.NexusRepository;
import org.eclipse.tycho.targeteditor.model.NexusRepositoryService;
import org.eclipse.tycho.targeteditor.model.NexusVersionCalculationResult;
import org.eclipse.tycho.targeteditor.model.LDIModelFactory;
import org.eclipse.ui.WorkbenchException;
import org.junit.Assert;
import org.junit.Test;

public class NexusRepositoryServiceTest {

    public class TestRepository extends NexusRepository {

        TestRepository() {
            super("repositoryName", "groupId", "artifactId", "version", "artifactExtension", true);
        }

        TestRepository(final String artifactId) {
            super("repositoryName", "groupId", artifactId, "version", "artifactExtension", true);
        }

        @Override
        public URL getMavenVersionMetaUrl() {
            try {
                return Util.getResourceURI(NexusVersionInfoTest.TEST_MAVENMETADATA_LOCATION).toURL();
            } catch (final MalformedURLException e) {
                throw new RuntimeException(e);
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    public class AsynTestCallback implements INexusVersionInfoCallback {

        private NexusVersionCalculationResult calculationResult = null;

        @Override
        public synchronized void notifyVersionCalculated(final NexusVersionCalculationResult calculationResult) {
            this.calculationResult = calculationResult;
        }

        public NexusVersionCalculationResult getResult(final long timeout) throws TimeoutException,
                InterruptedException {
            final long time = System.currentTimeMillis();
            NexusVersionCalculationResult result = null;
            do {
                synchronized (this) {
                    result = calculationResult;
                }
            } while (result == null && System.currentTimeMillis() < time + timeout);
            if (result == null) {
                throw new TimeoutException();
            }
            return result;
        }

    }

    @Test
    public void testAsyncVersionCalculation() throws TimeoutException, InterruptedException {
        final INexusRepositoryService nexusRepositoryService = LDIModelFactory.getNexusRepositoryService();
        final AsynTestCallback callback = new AsynTestCallback();
        final TestRepository repository = new TestRepository();
        nexusRepositoryService.getVersionInfoAsync(repository, callback);
        final NexusVersionCalculationResult result = callback.getResult(10000);
        Assert.assertTrue(result.getStatus().isOK());
        Assert.assertEquals("0.7.1", result.getVersionInfo().getLatestReleaseVersion());
        Assert.assertEquals(repository, result.getRepository());
    }

    @Test
    public void testAsyncVersionCalculationStress() throws TimeoutException, InterruptedException {
        final INexusRepositoryService nexusRepositoryService = LDIModelFactory.getNexusRepositoryService();
        final List<AsynTestCallback> callbacks = new ArrayList<NexusRepositoryServiceTest.AsynTestCallback>();
        final List<TestRepository> repositories = new ArrayList<NexusRepositoryServiceTest.TestRepository>();
        for (int i = 0; i < 3000; i++) {
            final AsynTestCallback callback = new AsynTestCallback();
            final TestRepository repository = new TestRepository("a" + Integer.toString(i));
            callbacks.add(callback);
            repositories.add(repository);
            nexusRepositoryService.getVersionInfoAsync(repository, callback);
        }

        for (final AsynTestCallback callback : callbacks) {
            final NexusVersionCalculationResult result = callback.getResult(60000);
            Assert.assertTrue(result.getStatus().getMessage(), result.getStatus().isOK());
            Assert.assertEquals("0.7.1", result.getVersionInfo().getLatestReleaseVersion());
        }

        Assert.assertTrue(NexusRepositoryService.VersionCalculationJob.requests.isEmpty());
    }

    @Test(expected = IOException.class)
    public void testFailingGetVersionMetadata() throws URISyntaxException, WorkbenchException, IOException {
        final NexusRepositoryService service = new NexusRepositoryService();
        final URI invalidURI = new URI("http://nexus:8081/nexus/thereisNoSuchFileSoThereShouldBeAnException");
        service.getMemento(invalidURI.toURL());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetVersionInfoWithHostileNexusRepoImpl() throws URISyntaxException, WorkbenchException, IOException {
        final INexusRepositoryService service = new NexusRepositoryService();
        final INexusRepository hostileRepoImpl = new INexusRepository() {

            @Override
            public URI getURI() {
                return null;
            }

            @Override
            public void addModelChangedListener(final IModelChangedListener listener) {
            }

            @Override
            public void fireModelChanged(final IModelChangedEvent event) {
            }

            @Override
            public void fireModelObjectChanged(final Object object, final String property, final Object oldValue,
                    final Object newValue) {
            }

            @Override
            public void removeModelChangedListener(final IModelChangedListener listener) {
            }

            @Override
            public void setVersion(final String version) {
            }

            @Override
            public String getVersion() {
                return null;
            }

            @Override
            public String getGroupId() {
                return null;
            }

            @Override
            public String getArtifactId() {
                return null;
            }

            @Override
            public String getRepositoryName() {
                return null;
            }

            @Override
            public void setRepositoryName(final String string) {
            }

            @Override
            public String getClassifier() {
                return null;
            }

            @Override
            public String getType() {
                return null;
            }
        };

        service.getVersionInfo(hostileRepoImpl);
    }
}
