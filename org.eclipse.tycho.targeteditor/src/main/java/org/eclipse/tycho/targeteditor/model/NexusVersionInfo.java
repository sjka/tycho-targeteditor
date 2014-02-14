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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.IMemento;
import org.eclipse.ui.WorkbenchException;

class NexusVersionInfo implements INexusVersionInfo {

    private static final String TAG_RELEASE = "release";
    private static final String TAG_LATEST = "latest";
    private static final String TAG_VERSION = "version";
    private static final String TAG_VERSIONS = "versions";
    private static final String TAG_VERSIONING = "versioning";

    private final IMemento memento;

    public NexusVersionInfo(final IMemento memento) throws WorkbenchException {
        this.memento = memento;
        validate();
    }

    private void validate() throws WorkbenchException {
        final String missing_tag_msg = "maven-metadata.xml does not contain tag ";
        if (memento.getChild(TAG_VERSIONING) == null) {
            throw new WorkbenchException(missing_tag_msg + TAG_VERSIONING);
        }
        if (memento.getChild(TAG_VERSIONING).getChild(TAG_VERSIONS) == null) {
            throw new WorkbenchException(missing_tag_msg + TAG_VERSIONS);
        }
        if (memento.getChild(TAG_VERSIONING).getChild(TAG_LATEST) == null) {
            throw new WorkbenchException(missing_tag_msg + TAG_LATEST);
        }
    }

    @Override
    public String getLatestVersion() {
        final String latestVersion = memento.getChild(TAG_VERSIONING).getChild(TAG_LATEST).getTextData();
        return latestVersion;
    }

    @Override
    public String getLatestReleaseVersion() {
        final IMemento releaseTag = memento.getChild(TAG_VERSIONING).getChild(TAG_RELEASE);
        String latestReleaseVersion = null;
        if (releaseTag != null) {
            latestReleaseVersion = releaseTag.getTextData();
        }
        return latestReleaseVersion;
    }

    @Override
    public List<String> getVersions() {
        final IMemento[] versions = memento.getChild(TAG_VERSIONING).getChild(TAG_VERSIONS).getChildren(TAG_VERSION);
        final List<String> result = new ArrayList<String>();
        for (final IMemento version : versions) {
            result.add(version.getTextData());
        }
        return result;
    }

}
