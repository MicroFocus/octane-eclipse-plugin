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

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Display;

import com.hpe.octane.ideplugins.eclipse.util.DelayedRunnable;

public class DelayedModifyListener implements ModifyListener {

    private ModifyEvent modifyEvent;
    private DelayedRunnable delayedRunnable;

    public DelayedModifyListener(ModifyListener normalModifyListener) {
        this(500, normalModifyListener);
    }

    public DelayedModifyListener(int delay, ModifyListener normalModifyListener) {
        delayedRunnable = new DelayedRunnable(() -> {
            Display.getDefault().asyncExec(() -> {
                normalModifyListener.modifyText(modifyEvent);
            });
        }, delay);
    }

    @Override
    public void modifyText(ModifyEvent e) {
        this.modifyEvent = e;
        delayedRunnable.execute();
    }

}