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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

class NexusRepository extends Repository implements INexusRepository {

    private static final String NEXUS_URL_PREFIX = "http://nexus:8081/nexus/content/repositories/";
    private String repositoryName;
    private final String groupId;
    private final String artifactId;
    private String version;
    private final String artifactExtension;
    private final boolean endsWithSlash;

    NexusRepository(final String repositoryName, final String groupId, final String artifactId, final String version,
            final String artifactExtension, final boolean endWithSlash) {
        super(null);
        this.repositoryName = repositoryName;
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.artifactExtension = artifactExtension;
        this.endsWithSlash = endWithSlash;
    }

    @Override
    public URI getURI() {
        return buildUri();
    }

    @Override
    public void setVersion(final String version) {
        final String oldVersion = getVersion();
        this.version = version;
        super.fireModelObjectChanged(this, "version", oldVersion, version);
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public String getGroupId() {
        return groupId;
    }

    @Override
    public String getArtifactId() {
        return artifactId;
    }

    @Override
    public String getClassifier() {
        // artifactExtension = -c.zip-unzip/... or .zip-unzip/...
        final int idx = artifactExtension.indexOf(".zip-unzip");
        if (idx >= 1) {
            // cut of leading minus
            final String s = artifactExtension.substring(1, idx);
            if (!s.isEmpty()) {
                return s;
            }
        }
        return null;
    }

    @Override
    public String getType() {
        return "zip";
    }

    @Override
    public String getRepositoryName() {
        return repositoryName;
    }

    @Override
    public void setRepositoryName(final String repoName) {
        if (repoName == null || repoName.length() == 0) {
            throw new IllegalArgumentException("Argument must not be null");
        }
        final String oldRepoName = this.repositoryName;
        this.repositoryName = repoName;
        super.fireModelObjectChanged(this, "repositoryName", oldRepoName, this.repositoryName);
    }

    URL getMavenVersionMetaUrl() {
        final String metaUrl = NEXUS_URL_PREFIX + repositoryName + "/" + groupId.replace(".", "/") + "/" + artifactId
                + "/" + "maven-metadata.xml";
        URL result = null;
        try {
            result = new URL(metaUrl);
        } catch (final MalformedURLException e) {
            throw new IllegalStateException("Invalid uri: " + metaUrl);
        }
        return result;
    }

    private URI buildUri() {
        final String uriStr = NEXUS_URL_PREFIX + repositoryName + "/" + groupId.replace(".", "/") + "/" + artifactId
                + "/" + version + "/" + artifactId + "-" + version + artifactExtension
                + (this.endsWithSlash ? "/" : "");
        try {
            return new URI(uriStr);
        } catch (final URISyntaxException e) {
            throw new IllegalStateException("Invalid uri: " + uriStr);
        }

    }

    /**
     * Creates an instance of {@link IRepository} from the given URI. If the given URI represents a
     * "Nexus URI" the created instance will be a {@link NexusRepository}, a {@link Repository}
     * otherwise.
     * 
     * @param The
     *            URI pointing to the repository.
     * @return An instance of {@link IRepository}, which is either a {@link NexusRepository} or a
     *         {@link Repository}
     */
    static IRepository createRepository(final URI uri) {
        IRepository result = null;
        final String uriStr = uri.toString();
        if (uriStr.startsWith(NEXUS_URL_PREFIX)) {
            final String[] tokens = uri.toString().substring(NEXUS_URL_PREFIX.length()).split("/");
            if (tokens.length >= 5) {
                final String repositoryName = tokens[0];
                int artifactIndex = tokens.length;
                do {
                    artifactIndex--;
                } while (artifactIndex > 3 && !tokens[artifactIndex].endsWith(".zip-unzip"));
                if (artifactIndex > 3) {
                    final String artifact = tokens[artifactIndex];
                    final String version = tokens[artifactIndex - 1];
                    final String artifactId = tokens[artifactIndex - 2];

                    final String arifactPrefix = artifactId + "-" + version;
                    final boolean endsWithSlash = uriStr.endsWith("/");
                    if (artifact.startsWith(arifactPrefix)) {
                        String artifactExtension = artifact.substring(arifactPrefix.length());
                        for (int i = artifactIndex + 1; i < tokens.length; i++) {
                            artifactExtension = artifactExtension + "/" + tokens[i];
                        }
                        final StringBuilder groupId = new StringBuilder();
                        for (int i = 1; i < artifactIndex - 2; i++) {
                            if (i != 1) {
                                groupId.append(".");
                            }
                            groupId.append(tokens[i]);
                        }
                        result = new NexusRepository(repositoryName, groupId.toString(), artifactId, version,
                                artifactExtension, endsWithSlash);
                    }
                }
            }
        }
        if (result == null) {
            result = new Repository(uri);
        }
        return result;
    }

}
