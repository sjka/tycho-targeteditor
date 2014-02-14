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
package org.eclipse.tycho.targeteditor;

import org.eclipse.tycho.targeteditor.ShowResolutionProblemAction;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;

public class WorkbenchPO {

    public static WorkbenchPO getWorkbench() {
        return new WorkbenchPO();
    }

    public MessageConsole getTargetDefinitionProblemConsole() {
        final IConsoleManager consoleManager = ConsolePlugin.getDefault().getConsoleManager();
        final IConsole[] currentConsoles = consoleManager.getConsoles();
        for (int i = 0; i < currentConsoles.length; i++) {
            if (ShowResolutionProblemAction.CONSOLE_NAME.equals(currentConsoles[i].getName())) {
                return (MessageConsole) currentConsoles[i];
            }
        }
        return null;
    }
}
