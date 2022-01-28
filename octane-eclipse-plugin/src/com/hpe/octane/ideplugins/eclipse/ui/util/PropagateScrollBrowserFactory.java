/*******************************************************************************
 * © Copyright 2017-2022 Micro Focus or one of its affiliates.
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

import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Browser that can propagate mouse wheel scroll to first parent
 * {@link ScrolledComposite}. <br>
 * This is a factory because you can't subclass {@link Browser}. <br>
 * Why must life be so difficult?!
 */
public class PropagateScrollBrowserFactory {

    private ScrolledComposite parentScrollComposite;
    private Browser browser;

    public Browser createBrowser(Composite parent, int style) {
        browser = new Browser(parent, style);
        addListener(browser);
        return browser;
    }

    public Browser createBrowser(Composite parent, int style, ScrolledComposite parentScrollComposite) {
        browser = new Browser(parent, style);
        addListener(browser);
        this.parentScrollComposite = parentScrollComposite;
        return browser;
    }

    private void addListener(Browser browser) {
        browser.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseScrolled(MouseEvent e) {
                if (hasVerticalScroll(browser)) {
                    return; // no double scrolling
                }

                ScrolledComposite toScroll = parentScrollComposite != null ? parentScrollComposite : findScrolledCompositeParent();

                if (toScroll != null && toScroll.getVerticalBar() != null) {
                    Point currentOrigin = toScroll.getOrigin();
                    if (e.count < 0) {
                        currentOrigin.y += toScroll.getVerticalBar().getIncrement();
                    } else {
                        currentOrigin.y -= toScroll.getVerticalBar().getIncrement();
                    }
                    toScroll.setOrigin(currentOrigin);
                }
            }
        });
    }

    private boolean hasVerticalScroll(Browser browser) {
        try {
            return (boolean) browser.evaluate("return document.body.scrollHeight > document.body.clientHeight;");
        } catch (Exception ignored) {
            // Assume that it does
            return true;
        }
    }

    private ScrolledComposite findScrolledCompositeParent() {
        // Find first scrollable parent
        Control currentControl = browser;
        while (currentControl != null) {
            currentControl = currentControl.getParent();
            if (currentControl != null && currentControl instanceof ScrolledComposite) {
                return (ScrolledComposite) currentControl;
            }
        }
        return null;
    }

}
