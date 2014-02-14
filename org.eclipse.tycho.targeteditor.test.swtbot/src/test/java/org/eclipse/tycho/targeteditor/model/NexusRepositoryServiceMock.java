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
import org.eclipse.tycho.targeteditor.model.INexusVersionInfo;
import org.eclipse.tycho.targeteditor.model.NexusRepositoryService;
import org.eclipse.ui.WorkbenchException;

public class NexusRepositoryServiceMock extends NexusRepositoryService {

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

    @Override
    public INexusVersionInfo getVersionInfo(final INexusRepository repository) throws WorkbenchException, IOException {
        if (repository.getArtifactId().equals("testartifactNotReleased")) {
            return new NexusVerionInfoStub(LATEST_SNAPSHOT_VERSION, "1.1.1-SNAPSHOT", "0.1.0-SNAPSHOT");
        } else if (repository.getRepositoryName().contains("build.snapshots.unzip")) {
            return new NexusVerionInfoStub(LATEST_SNAPSHOT_VERSION, LATEST_VERSION, "1.1.1", "1.1.1-SNAPSHOT", "1.0.0",
                    "0.2.0", "0.1.18", "0.1.0");
        } else {
            return new NexusVerionInfoStub(LATEST_VERSION, "1.1.1", "1.0.0", "0.2.0", "0.1.18", "0.1.0");
        }
    }
}
