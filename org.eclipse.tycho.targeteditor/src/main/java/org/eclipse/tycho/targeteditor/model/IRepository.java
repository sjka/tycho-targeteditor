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

import org.eclipse.pde.core.IModelChangeProvider;

/**
 * Represents a reference to a p2 repository.
 * 
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IRepository extends IModelChangeProvider {

    /**
     * Returns the p2 repository reference as an URI.
     * 
     * @return the repository reference
     */
    URI getURI();

}
