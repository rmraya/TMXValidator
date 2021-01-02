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

 const { ipcRenderer } = require('electron');
const { dialog } = require('electron').remote

function browse() {
    ipcRenderer.send('select-tmx-validation');
}

function validate() {
    var tmxfile: string = (document.getElementById('tmxFile') as HTMLInputElement).value;
    if (!tmxfile) {
        dialog.showErrorBox('Attention', 'Select TMX file');
        return;
    }
    ipcRenderer.send('validate', { command: 'validate', file: tmxfile });
}

ipcRenderer.on('add-tmx-validation', (event, arg) => {
    (document.getElementById('tmxFile') as HTMLInputElement).value = arg;
});

ipcRenderer.on('validation-started', (event, arg) => {
    document.getElementById('working').style.display = 'block';
});

ipcRenderer.on('validation-completed', (event, arg) => {
    document.getElementById('working').style.display = 'none';
});

function showAbout() {
    ipcRenderer.send('show-about');
}