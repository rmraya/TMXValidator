/*******************************************************************************
 * Copyright (c) 2005-2023 Maxprograms.
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
import { ChildProcessWithoutNullStreams, execFileSync, spawn } from "child_process";
import { ClientRequest, request } from "http";
import { IpcMainEvent } from "electron/main";

class TMXValidator {

    static mainWindow: BrowserWindow;
    javapath: string = app.getAppPath() + '/bin/java';
    static ls: ChildProcessWithoutNullStreams;
    static killed: boolean = false;
    static currentStatus: any = {};

    constructor() {
        if (!app.requestSingleInstanceLock()) {
            app.quit()
        } else {
            if (TMXValidator.mainWindow) {
                // Someone tried to run a second instance, we should focus our window.
                if (TMXValidator.mainWindow.isMinimized()) {
                    TMXValidator.mainWindow.restore()
                }
                TMXValidator.mainWindow.focus();
            }
        }
        if (process.platform == 'win32') {
            this.javapath = app.getAppPath() + '\\bin\\java.exe';
        }
        TMXValidator.ls = spawn(this.javapath, ['--module-path', 'lib', '-m', 'tmxvalidator/com.maxprograms.tmxvalidation.ValidationServer'], { cwd: app.getAppPath() });
        TMXValidator.ls.stdout.on('data', (data) => {
            console.log(`stdout: ${data}`);
        });
        TMXValidator.ls.stderr.on('data', (data) => {
            console.error(`stderr: ${data}`);
        });
        execFileSync('bin/java', ['--module-path', 'lib', '-m', 'tmxvalidator/com.maxprograms.server.CheckURL', 'http://localhost:8010/ValidationServer'], { cwd: app.getAppPath() });
        app.on('ready', () => {
            TMXValidator.createWindows();
            TMXValidator.mainWindow.show();
        });
        app.on('quit', () => {
            TMXValidator.stopServer();
        });
        app.on('window-all-closed', function () {
            TMXValidator.stopServer();
            app.quit()
        });
        ipcMain.on('select-file', () => {
            dialog.showErrorBox('Attention', 'Select TMX file');
        });
        ipcMain.on('select-tmx-validation', () => {
            this.selectFile();
        });
        ipcMain.on('show-about', () => {
            this.showAbout();
        });
        ipcMain.on('validate', (event: IpcMainEvent, arg: any) => {
            TMXValidator.validate(event, arg);
        });
        ipcMain.on('get-version', (event: IpcMainEvent) => {
            TMXValidator.sendRequest({ command: 'version' },
                (data: any) => {
                    event.sender.send('set-version', data);
                },
                (reason: string) => {
                    dialog.showErrorBox('Error', reason);
                }
            );
        });
    }

    static stopServer(): void {
        if (!this.killed) {
            TMXValidator.ls.kill();
            TMXValidator.killed = true;
        }
    }

    selectFile(): void {
        dialog.showOpenDialog({
            properties: ['openFile'],
            filters: [
                { name: 'TMX File', extensions: ['tmx'] }
            ]
        }).then((value) => {
            if (!value.canceled) {
                TMXValidator.mainWindow.webContents.send('add-tmx-validation', value.filePaths[0]);
            }
        }).catch((reason) => {
            dialog.showErrorBox('Error', reason);
        });
    }

    static validate(event: IpcMainEvent, arg: any): void {
        event.sender.send('validation-started');
        TMXValidator.sendRequest(arg,
            function success(data: any) {
                TMXValidator.currentStatus = data;
                let intervalObject = setInterval(function () {
                    if (TMXValidator.currentStatus.status === 'Success') {
                        // ignore status from validation request
                    } else if (TMXValidator.currentStatus.status === 'Completed') {
                        clearInterval(intervalObject);
                        event.sender.send('validation-completed');
                        TMXValidator.getValidationStatus(data.process, event);
                        return;
                    } else if (TMXValidator.currentStatus.status === 'Running') {
                        // keep waiting
                    } else {
                        clearInterval(intervalObject);
                        event.sender.send('validation-completed');
                        dialog.showErrorBox('Error', TMXValidator.currentStatus.reason);
                        return;
                    }
                    TMXValidator.getStatus(data.process);
                }, 500);
            },
            function error(reason: string) {
                dialog.showErrorBox('Error', reason);
            }
        );
    }

    showAbout(): void {
        let about = new BrowserWindow({
            parent: TMXValidator.mainWindow,
            width: 280,
            height: 290,
            minimizable: false,
            maximizable: false,
            resizable: false,
            show: false,
            icon: 'img/tmxvalidator.png',
            backgroundColor: '#2d2d2e',
            webPreferences: {
                nodeIntegration: true,
                contextIsolation: false
            }
        });
        about.loadURL('file://' + app.getAppPath() + '/html/about.html');
        about.setMenu(null);
        about.show();
    }

    static createWindows() {
        TMXValidator.mainWindow = new BrowserWindow({
            width: 560,
            height: 180,
            show: false,
            maximizable: false,
            icon: 'img/tmxvalidator.png',
            backgroundColor: '#2d2d2e',
            darkTheme: true,
            webPreferences: {
                nodeIntegration: true,
                contextIsolation: false
            }
        });
        TMXValidator.mainWindow.setMenu(null);
        TMXValidator.mainWindow.loadURL('file://' + app.getAppPath() + '/html/main.html');
    }

    static sendRequest(json: any, success: any, error: any): void {
        let postData: string = JSON.stringify(json);
        let options = {
            hostname: '127.0.0.1',
            port: 8010,
            path: '/ValidationServer',
            headers: {
                'Content-Type': 'application/json',
                'Content-Length': Buffer.byteLength(postData)
            }
        };
        // Make a request
        let req: ClientRequest = request(options);
        req.on('response',
            function (res: any) {
                res.setEncoding('utf-8');
                if (res.statusCode != 200) {
                    error('sendRequest() error: ' + res.statusMessage);
                }
                let rawData: string = '';
                res.on('data', (chunk: string) => {
                    rawData += chunk;
                });
                res.on('end', () => {
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

    static getStatus(processId: string): void {
        TMXValidator.sendRequest({ command: 'status', process: processId },
            (data: any) => {
                TMXValidator.currentStatus = data;
            },
            (reason: string) => {
                dialog.showErrorBox('Error', reason);
            }
        );
    }

    static getValidationStatus(processId: string, event: any): void {
        TMXValidator.sendRequest({ command: 'validationResult', process: processId },
            (data: any) => {
                if (data.valid) {
                    dialog.showMessageBox({ type: 'info', message: data.comment });
                } else {
                    dialog.showMessageBox({ type: 'error', message: data.reason });
                }
            },
            (reason: string) => {
                dialog.showErrorBox('Error', reason);
            }
        );
    }

}
try {
    new TMXValidator();
} catch (e) {
    console.error(e);
}