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

/**
 * A <code> INexusRepository </code> represents an {@link IRepository} pointing to a repository
 * which was build as a maven artifact. It grands access to the maven GAV information of the
 * referenced repository and allows version modification.
 * 
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface INexusRepository extends IRepository {

    /**
     * The new version of the artifact to be referenced.
     * 
     * @param version
     *            the new version
     */
    void setVersion(String version);

    /**
     * Returns the maven version of the referenced artifact.
     * 
     * @return the maven version of the referenced artifact.
     */
    String getVersion();

    /**
     * Returns the maven group id of referenced artifact.
     * 
     * @return the maven group id of the referenced artifact.
     */
    String getGroupId();

    /**
     * Returns the maven artifact id of the referenced artifact.
     * 
     * @return the maven artifact id of the referenced artifact.
     */
    String getArtifactId();

    /**
     * Returns the maven classifier of the referenced artifact (e.g. assembly).
     * 
     * @return the maven artifact extension of the referenced artifact. Returns <code>null</code>,
     *         if the artifact has no classifier.
     */
    String getClassifier();

    /**
     * Returns the maven type of the referenced artifact (e.g. zip).
     * 
     * @return the maven artifact extension of the referenced artifact.
     */
    String getType();

    /**
     * @return The name of the nexus repository
     */
    String getRepositoryName();

    /**
     * @param repositoryName
     *            the new repository name
     */
    void setRepositoryName(String repositoryName);

}
