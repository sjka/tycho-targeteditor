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

public enum EDynamicVersions {
    RELEASE, SNAPSHOT;

    public static boolean isDynamicVersion(final String version) {
        for (final EDynamicVersions value : values()) {
            if (value.toString().equals(version)) {
                return true;
            }
        }
        return false;
    }
}
