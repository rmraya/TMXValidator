/*******************************************************************************
 * Copyright (c) 2005-2021 Maxprograms.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-v10.html
 *
 * Contributors:
 *     Maxprograms - initial API and implementation
 *******************************************************************************/

var _b = require('electron');
_b.ipcRenderer.send('get-version');

_b.ipcRenderer.on('set-version', (event, arg) => {
    document.getElementById('version').innerHTML = arg.version;
    document.getElementById('build').innerHTML = arg.build;
});