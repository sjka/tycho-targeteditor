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

import org.eclipse.equinox.p2.metadata.IVersionedId;
import org.eclipse.equinox.p2.metadata.Version;
import org.eclipse.pde.core.IModelChangeProvider;

/**
 * An {@link IVersionedId} which allowing content modification.
 */
public interface IMutableVersionedId extends IVersionedId, IModelChangeProvider {

    /**
     * Sets a new version. See {@link IVersionedId#getVersion()}
     * 
     * @param version
     *            the new version
     */
    void setVersion(Version version);

    /**
     * Sets a new id. See {@link IVersionedId#getId()}
     * 
     * @param id
     *            the new id
     */
    void setId(String id);

}
