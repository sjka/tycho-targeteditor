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
package org.eclipse.tycho.targeteditor.xml;

public class Repository {

    public static final Repository RESOLUTION_PENDING = new Repository(null, null, null, null, null, null, null);
    private final String name;
    private final String groupId;
    private final String artifactId;
    private final String version;
    private final String classifier;
    private final String description;
    private final String extension;

    public Repository(final String name, final String groupId, final String artifactId, final String version,
            final String classifier, final String description, final String extension) {
        super();
        this.name = name;
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.classifier = classifier;
        this.description = description;
        this.extension = extension;
    }

    public String getName() {
        return name;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getVersion() {
        return version;
    }

    public String getClassifier() {
        return classifier;
    }

    public String getDescription() {
        return description;
    }

    public String getExtension() {
        return extension;
    }

}
