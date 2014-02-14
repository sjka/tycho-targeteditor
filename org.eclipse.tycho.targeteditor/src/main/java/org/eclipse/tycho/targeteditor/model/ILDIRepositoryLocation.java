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

import java.util.List;

import org.eclipse.equinox.p2.metadata.IVersionedId;
import org.eclipse.pde.core.target.ITargetDefinition;
import org.eclipse.pde.core.target.ITargetLocation;

/**
 * An <code>LDIRepositoryLocation</code> basically represents a 'location' tag inside a target
 * definition file which references an external p2 repository. In opposite to the original
 * {@link ITargetDefinition} model it allows content modification.
 */
public interface ILDIRepositoryLocation {

    /**
     * Return a list of all repository references defined in this location
     * 
     * @return list of repository references
     */
    List<IRepository> getRepositories();

    /**
     * Returns a list of all units defined in this location. Units serve as resolution entry points.
     * 
     * @return list of referenced units as mutable versioned ids.
     */
    List<IMutableVersionedId> getUnits();

    /**
     * Adds new IU references to this location.
     * 
     * @param units
     *            the unit to be added
     * @return the added units as now available in {@link #getUnits()}
     */
    IMutableVersionedId[] addUnits(IVersionedId... units);

    /**
     * Returns true if this ILDIRepositoryLocation is representing the given container.
     */
    boolean isLdiLocationFor(ITargetLocation container);

}
