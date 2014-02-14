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

import org.eclipse.equinox.p2.metadata.Version;
import org.eclipse.equinox.p2.metadata.VersionedId;

class MutableVersionedId extends ModelChangeProvider implements IMutableVersionedId {
    private VersionedId versionedId;

    MutableVersionedId(final String id, final Version version) {
        versionedId = new VersionedId(id, version);
    }

    @Override
    public String getId() {
        return versionedId.getId();
    }

    @Override
    public Version getVersion() {
        return versionedId.getVersion();
    }

    @Override
    public void setVersion(final Version version) {
        final Version oldVersion = getVersion();
        versionedId = new VersionedId(getId(), version);
        super.fireModelObjectChanged(this, "version", oldVersion, version);
    }

    @Override
    public void setId(final String id) {
        final String oldId = getId();
        versionedId = new VersionedId(id, getVersion());
        super.fireModelObjectChanged(this, "Id", oldId, id);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((versionedId == null) ? 0 : versionedId.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MutableVersionedId other = (MutableVersionedId) obj;
        if (versionedId == null) {
            if (other.versionedId != null) {
                return false;
            }
        } else if (!versionedId.equals(other.versionedId)) {
            return false;
        }
        return true;
    }

}
