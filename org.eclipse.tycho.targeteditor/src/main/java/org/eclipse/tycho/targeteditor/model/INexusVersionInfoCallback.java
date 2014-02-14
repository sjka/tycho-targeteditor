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

public interface INexusVersionInfoCallback {

    /**
     * Notification about asynchronous version calculation.
     * 
     * @param repository
     *            The repository of which the available version were calculated.
     * @param versionInfo
     *            The calculated version info or <code>null</code>. If calculation failed, see
     *            status.
     * @param status
     *            Status OK, if version was calculated successfully. Failure status otherwise.
     */
    void notifyVersionCalculated(NexusVersionCalculationResult calculationResult);

}
