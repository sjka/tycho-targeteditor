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

public enum NexusRepositoryNames {
    SNAPSHOT("build.snapshots.unzip"), MILESTONE("build.milestones.unzip"), RELEASE("build.releases.unzip");

    public static NexusRepositoryNames byNexusName(final String nexusName) {
        NexusRepositoryNames result = null;
        final NexusRepositoryNames[] values = NexusRepositoryNames.values();
        for (int i = 0; i < values.length; i++) {
            if (values[i].nexusName().equals(nexusName)) {
                result = values[i];
                break;
            }
        }
        return result;
    }

    private String nexusName;

    private NexusRepositoryNames(final String techName) {
        this.nexusName = techName;
    }

    public String nexusName() {
        return this.nexusName;
    }

}
