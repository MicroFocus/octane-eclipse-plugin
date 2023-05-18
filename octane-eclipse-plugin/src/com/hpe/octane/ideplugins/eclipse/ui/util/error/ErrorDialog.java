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

package com.hpe.octane.ideplugins.eclipse.ui.util.error;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISharedImages;

import com.hpe.octane.ideplugins.eclipse.ui.util.resource.PlatformResourcesManager;

public class ErrorDialog extends Dialog {

    private static final int MAX_WIDTH = 1000;
    private static final int MAX_HEIGHT = 600;

    private Shell shell;
    private ErrorComposite errorComposite;

    public ErrorDialog(Shell parent) {
        super(parent);
        shell = new Shell(getParent(), SWT.CLOSE | SWT.RESIZE | SWT.TITLE | SWT.ON_TOP);
        shell.setSize(600, 300);
        shell.setLayout(new FillLayout());
        errorComposite = new ErrorComposite(shell, SWT.NONE);
        shell.setImage(PlatformResourcesManager.getPlatformImage(ISharedImages.IMG_DEC_FIELD_ERROR));

    }

    private static void positionShell(Shell shell) {
        Monitor primary = shell.getMonitor();
        Rectangle bounds = primary.getBounds();
        Rectangle rect = shell.getBounds();
        int x = bounds.x + (bounds.width - rect.width) / 2;
        int y = bounds.y + (bounds.height - rect.height) / 2;
        shell.setLocation(x, y);
    }

    public void displayException(Exception exception, String title) {
        displayException(exception, title, false);
    }

    public void displayException(Exception exception, String title, boolean blockThreadUntilClosed) {
        shell.setText(title);
        errorComposite.displayException(exception);
        resizeShell();
        shell.open();

        if(blockThreadUntilClosed) {
            Display display = getParent().getDisplay();
            while (!shell.isDisposed()) {
                if (!display.readAndDispatch()) {
                    display.sleep();
                }
            }
        }
    }

    private void resizeShell() {
        //pack to child size
        shell.pack();

        //max size 1000x600
        if(shell.getSize().x > MAX_WIDTH) {
            shell.setSize(new Point(MAX_WIDTH, shell.getSize().y));
        }
        if(shell.getSize().y > MAX_HEIGHT) {
            shell.setSize(new Point(shell.getSize().x, MAX_HEIGHT));
        }

        //put in the middle of the current monitor
        positionShell(shell);
    }

    public void addButton(String btnText, Runnable clickedRunnable) {
        errorComposite.addButton(btnText, clickedRunnable);
        resizeShell();
    }

    public void clearButtons() {
        errorComposite.clearButtons();
    }

    public void close() {
        shell.close();
    }
}