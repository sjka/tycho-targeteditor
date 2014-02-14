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

import java.io.IOException;

import org.eclipse.ui.WorkbenchException;

public interface INexusRepositoryService {

    /**
     * Retrieves the version information for the given repository synchronously. This might be a
     * costly task and may take some time.
     * 
     * @param The
     *            repository the version information shall be retrieved for.
     * @return The version information
     * @throws WorkbenchException
     *             in case the metadata file couldn't be parsed.
     * @throws IOException
     *             In case the server was unavailable or the corresponding GAV doesn't exist.
     */
    INexusVersionInfo getVersionInfo(INexusRepository repository) throws WorkbenchException, IOException;

    /**
     * Retrieves the version information asynchronously. After the version is retrieved the given
     * callback is called.
     * 
     * @param The
     *            repository the version information shall be retrieved for.
     * @param The
     *            callback.
     */
    void getVersionInfoAsync(INexusRepository repository, INexusVersionInfoCallback callback);

}
