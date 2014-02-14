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

import java.net.URI;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.pde.core.IModelChangeProvider;
import org.eclipse.pde.core.target.ITargetDefinition;
import org.eclipse.pde.internal.core.target.AbstractBundleContainer;

/**
 * LDITargetEditor specific wrapper of {@link ITargetDefinition} model. It allows modification of
 * repository reference URIs and related referenced installable units. The structure of the provided
 * model maps basically to the persistence xml model. References are specified as "locations"
 * (technically {@link IBundleContainer} instances) with different type. Only one of such location
 * types represents http reference to a p2 repository.
 */
@SuppressWarnings("restriction")
public interface ILDITargetDefintion extends IModelChangeProvider {

    static final String EVENT_PROPERTY_RESET_CONTAINER_RESOLUTION_STATE = "ResetContainerResolutionState";
    static final String EVENT_ADDED_LOCATION = "AddedLocation";
    static final String EVENT_ADDED_UNITS = "AddedUnits";

    /**
     * Returns a list of all locations with software site references. See class comment.
     * 
     * @return list of all locations referencing external p2 repositories
     */
    List<ILDIRepositoryLocation> getRepositoryLocations();

    /**
     * All locations defined in the underlying {@link ITargetDefinition} model which do not
     * represent a p2 repository reference.
     * 
     * @return list of locations not referencing p2 repositories
     */
    List<AbstractBundleContainer> getNonRepoLocations();

    /**
     * Clear the resolution state of bundle containers in underlying {@link ITargetDefinition}. As a
     * result subsequent resolving of underlying {@link ITargetDefinition} will recalculate
     * references instead of using cached values.
     */
    void clearResolutionStatus();

    /**
     * Adds a new Software Site repository reference to the target definition. The new repository
     * reference will be added to a newly created location. The new location will be configured with
     * default attributes: </br> <code>
     * includeAllPlatforms="false" includeMode="planner" includeSource="true" type="InstallableUnit"
     * </code>
     * 
     * @param uri
     *            the pointing repository to be added
     * @return a newly created repository location containing exactly one {@link IRepository}
     *         representing the provided uri reference
     */
    ILDIRepositoryLocation addRepository(URI uri);

    /**
     * Returns the location that contains the given repository.
     * 
     * @param repository
     *            a repository that is contained in the requested location
     * @return location that contains the given repository
     */
    ILDIRepositoryLocation findLocationOf(IRepository repository);

    /**
     * Removes the the given location.
     * 
     * @param locationToBeRemoved
     *            the location that should be removed
     */
    void removeLocation(ILDIRepositoryLocation locationToBeRemoved);

    /**
     * Retrieves the overall resolution status for this target definition. See
     * {@link ITargetDefinition#getBundleStatus()}
     * 
     * @return the bundle status of the underlying ITargetDefinition
     */
    IStatus getResolutionStatus();

}
