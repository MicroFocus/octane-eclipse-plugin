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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import com.hpe.octane.ideplugins.eclipse.ui.util.InfoPopup;

/**
 * Use in a diplay filter to catch the Konami Code over the whole swt
 * application
 * 
 */
public class KonamiCodeListener implements Listener {

    InfoPopup infoPopup = new InfoPopup("KONAMI", "↑↑↓↓←→←→ba");

    private final int[] konamiCode = {
            SWT.ARROW_UP,
            SWT.ARROW_UP,
            SWT.ARROW_DOWN,
            SWT.ARROW_DOWN,
            SWT.ARROW_LEFT,
            SWT.ARROW_RIGHT,
            SWT.ARROW_LEFT,
            SWT.ARROW_RIGHT,
            98, // B
            97, // A
    };

    private List<Integer> lastKeyCodes = new ArrayList<>(11);

    private Runnable runIfKonamiCode;

    public KonamiCodeListener(Runnable runIfKonamiCode) {
        this.runIfKonamiCode = runIfKonamiCode;
    }

    @Override
    public void handleEvent(Event event) {

        // Add to lastKeyCodes

        if (event.keyCode != SWT.SHIFT) {
            lastKeyCodes.add(event.keyCode);
            if (lastKeyCodes.size() > 10) {
                lastKeyCodes.remove(0);
            }
        }

        // Check if the lastKeyCodes are eq to the konami code
        if (areEq()) {
            runIfKonamiCode.run();
            infoPopup.open();
        }
    }

    private boolean areEq() {
        if (lastKeyCodes.size() == 10) {
            for (int i = 0; i < 10; i++) {
                if (konamiCode[i] != lastKeyCodes.get(i)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}
