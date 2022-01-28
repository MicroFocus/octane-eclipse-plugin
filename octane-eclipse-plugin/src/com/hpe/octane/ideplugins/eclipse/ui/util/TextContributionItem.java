/*******************************************************************************
 * © Copyright 2017-2022 Micro Focus or one of its affiliates.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.hpe.octane.ideplugins.eclipse.ui.util;

import org.eclipse.jface.action.ControlContribution;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

public class TextContributionItem extends ControlContribution {

    private Text text;
    private Runnable controlCreated;
    private boolean isEnabled;

    public TextContributionItem(String id) {
        super(id);
    }

    @Override
    protected Control createControl(Composite parent) {
        ToolBar toolbar = (ToolBar) parent;

        // Force height
        ToolItem ti = new ToolItem(toolbar, SWT.PUSH);
        ti.setImage(createForceHeightImageData());

        text = new Text(parent, SWT.BORDER);
        text.setEnabled(isEnabled);
        if (controlCreated != null) {
            controlCreated.run();
        }

        return text;
    }

    @Override
    public int computeWidth(Control control) {
        return 150;
    }

    public Text getTextControl() {
        return text;
    }

    private static Image createForceHeightImageData() {
        Image src = new Image(Display.getCurrent(), 16, 16);
        return src;
    }

    public String getText() {
        return text.getText().trim();
    }

    public void setText(String string) {
        text.setText(string);
    }

    public void addModifyListener(ModifyListener listener) {
        text.addModifyListener(listener);
    }

    public void addTraverseListener(TraverseListener listener) {
        text.addTraverseListener(listener);
    }

    public void setMessage(String message) {
        text.setMessage(message);
    }

    public void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
        if (text != null) {
            text.setEnabled(isEnabled);
        }
    }

    /**
     * Called once the swt control was created by framework
     * 
     * @param controlCreated
     */
    public void setControlCreatedRunnable(Runnable controlCreated) {
        this.controlCreated = controlCreated;
    }

}
