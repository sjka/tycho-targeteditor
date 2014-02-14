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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.tycho.targeteditor.model.INexusRepository;
import org.eclipse.tycho.targeteditor.model.INexusRepositoryService;
import org.eclipse.tycho.targeteditor.model.INexusVersionInfo;
import org.eclipse.tycho.targeteditor.model.NexusRepositoryService;
import org.eclipse.tycho.targeteditor.model.LDIModelFactory;
import org.eclipse.ui.WorkbenchException;

public class NexusRepositoryServiceMock1 extends NexusRepositoryService {

    class NexusVerionInfoStub implements INexusVersionInfo {

        private final String[] versions;

        public NexusVerionInfoStub(final String... versions) {
            this.versions = versions;
        }

        @Override
        public String getLatestVersion() {
            return versions[0];
        }

        @Override
        public String getLatestReleaseVersion() {
            for (final String version : versions) {
                if (!version.endsWith("-SNAPSHOT")) {
                    return version;
                }
            }
            return null;
        }

        @Override
        public List<String> getVersions() {
            final ArrayList<String> result = new ArrayList<String>(Arrays.asList(versions));
            Collections.reverse(result);
            return result;
        }

    }

    public static final String LATEST_VERSION = "2.0.0";
    public static final String LATEST_SNAPSHOT_VERSION = "2.0.1-SNAPSHOT";
    private INexusRepositoryService originalNexusRepositoryService;

    @Override
    public INexusVersionInfo getVersionInfo(final INexusRepository repository) throws WorkbenchException, IOException {
        if (repository.getRepositoryName().contains("build.snapshots.unzip")) {
            return new NexusVerionInfoStub(LATEST_SNAPSHOT_VERSION, LATEST_VERSION, "1.1.1", "1.1.1-SNAPSHOT", "1.0.0",
                    "0.2.0", "0.1.0");
        } else if (repository.getRepositoryName().contains("problematic")) {
            throw new RuntimeException("Unable to calculate Version info");
        } else if (repository.getRepositoryName().contains("noReleasedVersion")) {
            return new NexusVerionInfoStub(LATEST_SNAPSHOT_VERSION);
        } else {
            return new NexusVerionInfoStub(LATEST_VERSION, "1.1.1", "1.0.0", "0.2.0", "0.1.0");
        }
    }

    private void setReplacedService(final INexusRepositoryService originalNexusRepositoryService) {
        this.originalNexusRepositoryService = originalNexusRepositoryService;
    }

    public void restoreOriginalService() {
        if (this.originalNexusRepositoryService == null) {
            throw new IllegalStateException();
        }
        LDIModelFactory.setNexusRepositoryService(this.originalNexusRepositoryService);
    }

    public static NexusRepositoryServiceMock1 createAndRegister() {
        final NexusRepositoryServiceMock1 service = new NexusRepositoryServiceMock1();
        final INexusRepositoryService originalNexusRepositoryService = LDIModelFactory
                .setNexusRepositoryService(service);
        service.setReplacedService(originalNexusRepositoryService);
        return service;
    }

}
