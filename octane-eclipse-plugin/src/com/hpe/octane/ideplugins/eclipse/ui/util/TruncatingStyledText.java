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

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Caret;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ToolTip;

public class TruncatingStyledText extends StyledText {

    private String originalText = "";
    private GC gc;
    
    public TruncatingStyledText(Composite parent, int style) {
        this(parent, style, new ToolTip(parent.getShell(), SWT.NONE));
    }

    public TruncatingStyledText(Composite parent, int style, ToolTip tip) {
        super(parent, style);
        gc = new GC(TruncatingStyledText.this);

        addMouseTrackListener(new MouseTrackAdapter() {

            @Override
            public void mouseHover(MouseEvent e) {
                int containerWidth = TruncatingStyledText.this.getSize().x;
                int stringWidth = getTextWidth();

                if (stringWidth > containerWidth) {
                    Point cursorLocation = Display.getCurrent().getCursorLocation();
                    cursorLocation.x += 5;
                    cursorLocation.y += 5;
                    tip.setLocation(cursorLocation);
                    tip.setMessage(originalText);
                    tip.setVisible(true);
                }
            }

            @Override
            public void mouseExit(MouseEvent e) {
                tip.setVisible(false);
            }
        });

        addControlListener(new ControlAdapter() {
            @Override
            public void controlResized(ControlEvent e) {
                int containerWidth = TruncatingStyledText.this.getSize().x;
                int stringWidth = getTextWidth();

                if (stringWidth > containerWidth) {
                    setInternalText(getTrunctated(originalText, containerWidth));
                } else {
                    setInternalText(originalText);
                }
            }
        });

        //deselect on focus lost
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                setSelection(0, 0);
            }
        });
    }

    private int getTextWidth() {
        return gc.stringExtent(originalText).x;
    }

    private int getTextWidth(String text) {
        return gc.stringExtent(text).x;
    }

    private String getTrunctated(String text, int width) {

        int stringWidth = getTextWidth(addEllipsis(text));

        while (stringWidth > width && text.length() > 0) {
            text = text.substring(0, text.length() - 1);
            stringWidth = getTextWidth(addEllipsis(text));
        }

        return addEllipsis(text);
    }

    private String addEllipsis(String text) {
        return text + "...";
    }

    private void setInternalText(String text) {
        super.setText(text);
    }

    @Override
    public void setText(String text) {
        this.originalText = text;
        super.setText(text);
    }

    @Override
    public void setCaret(Caret caret) {
    }

    @Override
    public void setFont(Font font) {
        super.setFont(font);
        gc.setFont(font);
    }
    
}
