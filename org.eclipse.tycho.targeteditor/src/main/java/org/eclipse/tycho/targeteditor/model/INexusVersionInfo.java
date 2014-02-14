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

/**
 * A <code>INexusVersionInfo</code> carries the version information for an specific nexus repository
 * ({@link INexusRepository}).
 */
public interface INexusVersionInfo {

    /**
     * @return the latest available version, which might also be a snapshot version.
     */
    public String getLatestVersion();

    /**
     * @return the latest available release version. This doesn't include snapshot versions. May be
     *         <code>null</code>
     */
    public String getLatestReleaseVersion();

    /**
     * @return all available versions, regardsless of type.
     */
    public List<String> getVersions();
}
