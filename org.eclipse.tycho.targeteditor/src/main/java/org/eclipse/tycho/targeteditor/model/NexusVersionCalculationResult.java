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

import org.eclipse.core.runtime.IStatus;

/**
 * Result of a nexus version calculation.
 */
public class NexusVersionCalculationResult {

    private final INexusRepository repository;
    private final INexusVersionInfo versionInfo;
    private final IStatus status;

    public NexusVersionCalculationResult(final INexusRepository repository, final INexusVersionInfo versionInfo,
            final IStatus status) {
        this.repository = repository;
        this.versionInfo = versionInfo;
        this.status = status;
    }

    /**
     * @return The repository of which the available version were calculated.
     */
    public INexusRepository getRepository() {
        return repository;
    }

    /**
     * @return The calculated version info or <code>null</code> if calculation failed
     *         {@link #getStatus()}
     */
    public INexusVersionInfo getVersionInfo() {
        return versionInfo;
    }

    /**
     * @return Status OK, if version was calculated successfully. Failure status otherwise.
     */
    public IStatus getStatus() {
        return status;
    }
}
