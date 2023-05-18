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
package com.hpe.octane.ideplugins.eclipse.ui.snake;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

import com.hpe.octane.ideplugins.eclipse.util.NullEditorInput;

/**
 * This is only an editor so it can only be opened via the code <br>
 * Intended to be an easter egg
 */
public class SnakeEditor extends EditorPart {

    public static final NullEditorInput snakeEditorInput = new NullEditorInput();

    public static final String ID = "com.hpe.octane.ideplugins.eclipse.ui.snake.SnakeEditor"; //$NON-NLS-1$
    private SnakeGameCanvas snakeGameCanvas;

    @Override
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        setSite(site);
        setInput(input);
        setPartName("Octane Snake");
    }

    /**
     * Create contents of the editor part.
     * 
     * @param parent
     */
    @Override
    public void createPartControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new FillLayout());

        snakeGameCanvas = new SnakeGameCanvas(composite);

        composite.addControlListener(new ControlAdapter() {
            @Override
            public void controlResized(ControlEvent e) {
                if (snakeGameCanvas.getGameState() != SnakeGameCanvas.GameState.RUNNING) {
                    snakeGameCanvas.redraw();
                }
            }
        });
        snakeGameCanvas.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                snakeGameCanvas.pause();
            }
        });
    }

    @Override
    public void setFocus() {
        if (snakeGameCanvas != null) {
            snakeGameCanvas.setFocus();
        }
    }

    @Override
    public void doSave(IProgressMonitor monitor) {
    }

    @Override
    public void doSaveAs() {
    }

    @Override
    public boolean isDirty() {
        return false;
    }

    @Override
    public boolean isSaveAsAllowed() {
        return false;
    }

}
