/*******************************************************************************
 * Copyright 2017-2023 Open Text.
 *
 * The only warranties for products and services of Open Text and
 * its affiliates and licensors (“Open Text”) are as may be set forth
 * in the express warranty statements accompanying such products and services.
 * Nothing herein should be construed as constituting an additional warranty.
 * Open Text shall not be liable for technical or editorial errors or
 * omissions contained herein. The information contained herein is subject
 * to change without notice.
 *
 * Except as specifically indicated otherwise, this document contains
 * confidential information and a valid license is required for possession,
 * use or copying. If this work is provided to the U.S. Government,
 * consistent with FAR 12.211 and 12.212, Commercial Computer Software,
 * Computer Software Documentation, and Technical Data for Commercial Items are
 * licensed to the U.S. Government under vendor's standard commercial license.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *   http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.hpe.octane.ideplugins.eclipse.ui.util;

import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PreferencesUtil;

import com.hpe.octane.ideplugins.eclipse.preferences.PluginPreferencePage;
import com.hpe.octane.ideplugins.eclipse.ui.util.resource.PlatformResourcesManager;

public final class InfoPopup extends PopupDialog {

    private int width = 200;
    private int height = 70;

    private Text text;
    private String content = "";
    private boolean autoClose = true;
    private boolean createLink;
    private long timeToClose = 3000;
    private Link openConnectionSettings;

    public InfoPopup(String headerString, String content) {
        this(headerString, null, content);
    }

    public InfoPopup(String headerString, String content, int width, int height) {
        this(headerString, null, content);
        this.width = width;
        this.height = height;
        this.timeToClose = 7000;
    }

    public InfoPopup(String headerString, String footerString, String content) {
        this(headerString, footerString, content, true);
    }

    public InfoPopup(String headerString, String footerString, String content, boolean autoclose) {
        super(PlatformResourcesManager.getActiveShell(),
                PopupDialog.HOVER_SHELLSTYLE,
                false,
                false,
                false,
                true,
                false,
                headerString,
                footerString);

        this.content = content;
        this.autoClose = autoclose;
    }

    public InfoPopup(String headerString, String content, int width, int height, boolean autoClose, boolean createLink) {
        super(PlatformResourcesManager.getActiveShell(),
                PopupDialog.HOVER_SHELLSTYLE,
                false,
                false,
                false,
                true,
                false,
                headerString,
                "");
        this.content = content;
        this.autoClose = autoClose;
        this.createLink = createLink;
    }

    private void scheduleClose() {
        TimerTask timer = new TimerTask() {
            @Override
            public void run() {
                Display.getDefault().asyncExec(() -> {
                    close();
                });
            }
        };
        Timer fireEventTimer = new Timer();
        fireEventTimer.schedule(timer, timeToClose);
    }

    @Override
    public int open() {
        if (autoClose) {
            scheduleClose();
        }
        return super.open();
    }

    @Override
    protected Control createTitleMenuArea(Composite arg0) {
        return super.createTitleMenuArea(arg0);
    }

    @Override
    protected void fillDialogMenu(IMenuManager dialogMenu) {
        dialogMenu.addMenuListener(new IMenuListener() {
            @Override
            public void menuAboutToShow(IMenuManager arg0) {
                handleShellCloseEvent();
            }
        });
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        text = new Text(parent, SWT.MULTI | SWT.READ_ONLY | SWT.WRAP | SWT.NO_FOCUS);
        text.setText(content);
        return text;
    }

    @Override
    protected Control createInfoTextArea(Composite parent) {
        if (createLink) {
            openConnectionSettings = new Link(parent, SWT.NONE);
            openConnectionSettings.setText("<A>" + "Connection Settings" + "</A>");
            openConnectionSettings.addListener(SWT.Selection, new Listener() {
                @Override
                public void handleEvent(Event event) {
                    PreferencesUtil.createPreferenceDialogOn(parent.getShell(),
                            PluginPreferencePage.ID,
                            null,
                            null).open();
                    scheduleClose();
                }
            });
            return openConnectionSettings;
        } else {
            return super.createInfoTextArea(parent);
        }

    }

    /**
     * Place the dialog in the corner of the IDE
     */
    @Override
    protected void adjustBounds() {
        Rectangle shellBounds = PlatformResourcesManager.getActiveShell().getBounds();
        shellBounds.x = shellBounds.x + shellBounds.width - width - 20;
        shellBounds.y = shellBounds.y + shellBounds.height - height - 20;
        shellBounds.width = width;
        shellBounds.height = height;
        getShell().setBounds(shellBounds);
    }

    public void setText(String textContents) {
        this.content = textContents;
    }

}