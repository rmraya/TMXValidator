/*******************************************************************************
 * Copyright (c) 2003-2020 Maxprograms.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-v10.html
 *
 * Contributors:
 *     Maxprograms - initial API and implementation
 *******************************************************************************/

import { app, ipcMain, BrowserWindow, dialog } from "electron";
import { execFileSync, spawn } from "child_process";
import { ClientRequest, request } from "http";

var mainWindow: BrowserWindow;
var javapath: string = app.getAppPath() + '/bin/java';
var killed: boolean = false;
var currentStatus: any = {};

if (!app.requestSingleInstanceLock()) {
    app.quit()
} else {
    if (mainWindow) {
        // Someone tried to run a second instance, we should focus our window.
        if (mainWindow.isMinimized()) {
            mainWindow.restore()
        }
        mainWindow.focus()
    }
}

if (process.platform == 'win32') {
    javapath = app.getAppPath() + '\\bin\\java.exe';
}

const ls = spawn(javapath, ['--module-path', 'lib', '-m', 'tmxvalidator/com.maxprograms.tmxvalidation.ValidationServer'], { cwd: app.getAppPath() });

ls.stdout.on('data', (data) => {
    console.log(`stdout: ${data}`);
});

ls.stderr.on('data', (data) => {
    console.error(`stderr: ${data}`);
});

ls.on('close', (code) => {
    console.log(`child process exited with code ${code}`);
});

var ck: Buffer = execFileSync('bin/java', ['--module-path', 'lib', '-m', 'openxliff/com.maxprograms.server.CheckURL', 'http://localhost:8010/ValidationServer'], { cwd: app.getAppPath() });
console.log(ck.toString());

function stopServer() {
    if (!killed) {
        ls.kill();
        killed = true;
    }
}

app.on('ready', () => {
    createWindows();
    mainWindow.show();
    // mainWindow.webContents.openDevTools();
});

app.on('quit', () => {
    stopServer();
})

app.on('window-all-closed', function () {
    stopServer();
    app.quit()
})

function createWindows() {
    mainWindow = new BrowserWindow({
        width: 560,
        height: 170,
        show: false,
        maximizable: false,
        icon: './img/tmxvalidator.png',
        backgroundColor: '#2d2d2e',
        webPreferences: {
            nodeIntegration: true
        }
    });
    mainWindow.setMenu(null);
    mainWindow.loadURL('file://' + app.getAppPath() + '/html/main.html');
}

ipcMain.on('select-tmx-validation', (event, arg) => {
    dialog.showOpenDialog({
        properties: ['openFile'],
        filters: [
            { name: 'TMX File', extensions: ['tmx'] }
        ]
    }).then((value) => {
        if (!value.canceled) {
            event.sender.send('add-tmx-validation', value.filePaths[0]);
        }
    }).catch((reason) => {
        dialog.showErrorBox('Error', reason);
    });
});


ipcMain.on('show-about', (event, arg) => {
    var about = new BrowserWindow({
        parent: mainWindow,
        width: 280,
        height: 280,
        minimizable: false,
        maximizable: false,
        resizable: false,
        show: false,
        icon: '../img/tmxvalidator.png',
        backgroundColor: '#2d2d2e',
        webPreferences: {
            nodeIntegration: true
        }
    });
    about.loadURL('file://' + app.getAppPath() + '/html/about.html');
    about.setMenu(null);
    about.show();
});

function sendRequest(json: any, success: any, error: any) {
    var postData: string = JSON.stringify(json);
    var options = {
        hostname: '127.0.0.1',
        port: 8010,
        path: '/ValidationServer',
        headers: {
            'Content-Type': 'application/json',
            'Content-Length': Buffer.byteLength(postData)
        }
    };
    // Make a request
    var req: ClientRequest = request(options);
    req.on('response',
        function (res: any) {
            res.setEncoding('utf-8');
            if (res.statusCode != 200) {
                error('sendRequest() error: ' + res.statusMessage);
            }
            var rawData: string = '';
            res.on('data', function (chunk: string) {
                rawData += chunk;
            });
            res.on('end', function () {
                try {
                    success(JSON.parse(rawData));
                } catch (e) {
                    error(e.message);
                }
            });
        }
    );
    req.write(postData);
    req.end();
}

function getStatus(processId: string) {
    sendRequest({ command: 'status', process: processId },
        function success(data: any) {
            currentStatus = data;
        },
        function error(reason: string) {
            dialog.showErrorBox('Error', reason);
        }
    );
}

ipcMain.on('validate', (event, arg) => {
    event.sender.send('validation-started');
    sendRequest(arg,
        function success(data: any) {
            currentStatus = data;
            var intervalObject = setInterval(function () {
                if (currentStatus.status === 'Success') {
                    // ignore status from validation request
                } else if (currentStatus.status === 'Completed') {
                    clearInterval(intervalObject);
                    event.sender.send('validation-completed');
                    getValidationStatus(data.process, event);
                    return;
                } else if (currentStatus.status === 'Running') {
                    // keep waiting
                } else {
                    clearInterval(intervalObject);
                    event.sender.send('validation-completed');
                    dialog.showErrorBox('Error', currentStatus.reason);
                    return;
                }
                getStatus(data.process);
            }, 500);
        },
        function error(reason: string) {
            dialog.showErrorBox('Error', reason);
        }
    );
});

function getValidationStatus(processId: string, event: any) {
    sendRequest({ command: 'validationResult', process: processId },
        function success(data: any) {
            if (data.valid) {
                dialog.showMessageBox({ type: 'info', message: data.comment });
            } else {
                dialog.showMessageBox({ type: 'error', message: data.reason });
            }
        },
        function error(reason: string) {
            dialog.showErrorBox('Error', reason);
        }
    );
}

ipcMain.on('get-version', (event) => {
    sendRequest({ command: 'version' },
        function success(data: any) {
            event.sender.send('set-version', data);
        },
        function error(reason: string) {
            dialog.showErrorBox('Error', reason);
        }
    );
});

