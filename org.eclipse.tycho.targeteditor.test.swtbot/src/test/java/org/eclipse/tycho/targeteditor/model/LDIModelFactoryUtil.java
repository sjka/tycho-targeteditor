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
import java.net.URISyntaxException;

import org.eclipse.tycho.targeteditor.model.INexusRepositoryService;
import org.eclipse.tycho.targeteditor.model.IRepository;
import org.eclipse.tycho.targeteditor.model.NexusRepository;
import org.eclipse.tycho.targeteditor.model.LDIModelFactory;

public class LDIModelFactoryUtil {

    public static void setNexusRepositoryService(final INexusRepositoryService nexusRepositoryService) {
        LDIModelFactory.setNexusRepositoryService(nexusRepositoryService);
    }

    public static IRepository createRepository(final String url) throws URISyntaxException {
        return NexusRepository.createRepository(new URI(url));
    }

}
