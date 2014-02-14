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

class Repository extends ModelChangeProvider implements IRepository {

    protected URI uri;

    public Repository(final URI uri) {
        this.uri = uri;
    }

    @Override
    public URI getURI() {
        return uri;
    }

}
