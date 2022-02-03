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
package com.hpe.octane.ideplugins.eclipse.ui.entitydetail.field;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;

import com.hpe.adm.nga.sdk.model.DateFieldModel;
import com.hpe.adm.nga.sdk.model.FieldModel;
import com.hpe.adm.nga.sdk.model.ReferenceFieldModel;
import com.hpe.adm.octane.ideplugins.services.model.EntityModelWrapper;
import com.hpe.octane.ideplugins.eclipse.ui.util.resource.ImageResources;

public class DateTimeFieldEditor extends Composite implements FieldEditor {

    protected EntityModelWrapper entityModelWrapper;
    protected String fieldName;

    private DateTime dtDate;
    private DateTime dtTime;
    private Label btnSetNull;
    private Link linkSetDate;

    public DateTimeFieldEditor(Composite parent, int style) {
        super(parent, style);
        GridLayout gridLayout = new GridLayout(5, false);
        gridLayout.marginWidth = 0;
        setLayout(gridLayout);

        dtDate = new DateTime(this, SWT.DATE);
        dtDate.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

        dtTime = new DateTime(this, SWT.TIME);
        dtTime.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

        btnSetNull = new Label(this, SWT.NONE);
        btnSetNull.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        btnSetNull.setImage(ImageResources.OCTANE_REMOVE.getImage());
        btnSetNull.setCursor(Display.getCurrent().getSystemCursor(SWT.CURSOR_HAND));

        linkSetDate = new Link(this, SWT.NONE);
        linkSetDate.setText("<a>set date</a>");
        linkSetDate.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        // Init
        setDateTimeVisible(false);

        // Nullify
        btnSetNull.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(MouseEvent e) {
                setDateTimeVisible(false);
                entityModelWrapper.setValue(new ReferenceFieldModel(fieldName, null));
            }
        });

        // De-nullify
        linkSetDate.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(MouseEvent e) {
                setDateTimeVisible(true);
                ZonedDateTime now = ZonedDateTime.now();
                setZonedDateTime(now);
                entityModelWrapper.setValue(new DateFieldModel(fieldName, now));
            }
        });

        SelectionAdapter selectionListener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                    entityModelWrapper.setValue(new DateFieldModel(fieldName, getZonedDateTime()));
                } catch (DateTimeException ignored) {
                    // sometimes you can input an invalid month if you hover
                    // over the date field and type a number
                }
            }
        };
        dtDate.addSelectionListener(selectionListener);
        dtTime.addSelectionListener(selectionListener);
    }

    private void setDateTimeVisible(boolean isDateTimeVisible) {
        dtDate.setVisible(isDateTimeVisible);
        ((GridData) dtDate.getLayoutData()).exclude = !isDateTimeVisible;

        dtTime.setVisible(isDateTimeVisible);
        ((GridData) dtTime.getLayoutData()).exclude = !isDateTimeVisible;

        btnSetNull.setVisible(isDateTimeVisible);
        ((GridData) btnSetNull.getLayoutData()).exclude = !isDateTimeVisible;

        linkSetDate.setVisible(!isDateTimeVisible);
        ((GridData) linkSetDate.getLayoutData()).exclude = isDateTimeVisible;

        dtDate.setSize(dtDate.computeSize(SWT.DEFAULT, SWT.DEFAULT));
        dtTime.setSize(dtTime.computeSize(SWT.DEFAULT, SWT.DEFAULT));
        layout(true);
        getParent().layout();
    }

    private boolean isDateTimeVisible() {
        return dtDate.isVisible() && dtTime.isVisible() && btnSetNull.isVisible();
    }

    private void setZonedDateTime(ZonedDateTime zonedDateTime) {
        
        if (zonedDateTime != null) {
            // Convert to local time for UI
            Instant timeStamp = zonedDateTime.toInstant();
            zonedDateTime = timeStamp.atZone(ZoneId.systemDefault());
            
            dtDate.setYear(zonedDateTime.getYear());
            dtDate.setMonth(zonedDateTime.getMonthValue() - 1); // "- 1" because we convert from ZonedDateTime (1-12 months) to DateTime (0-11 months)
            dtDate.setDay(zonedDateTime.getDayOfMonth());
            dtTime.setHours(zonedDateTime.getHour());
            dtTime.setMinutes(zonedDateTime.getMinute());
            dtTime.setSeconds(zonedDateTime.getSecond());
            setDateTimeVisible(true);
        } else {
            setDateTimeVisible(false);
        }
    }

    private ZonedDateTime getZonedDateTime() {
        if (!isDateTimeVisible()) {
            return null;
        } else {
            // Converting to UTC is not necessary, the SDK will do it for you
            return ZonedDateTime.of(
                    dtDate.getYear(),
                    dtDate.getMonth() + 1, // "+ 1" because we convert from DateTime (0-11 months) to ZonedDateTime (1-12 months)
                    dtDate.getDay(),
                    dtTime.getHours(),
                    dtTime.getMinutes(),
                    dtTime.getSeconds(),
                    0,
                    ZoneId.systemDefault());
        }
    }

    @Override
    public void setField(EntityModelWrapper entityModel, String fieldName) {
        this.entityModelWrapper = entityModel;
        this.fieldName = fieldName;

        @SuppressWarnings("rawtypes")
        FieldModel fieldModel = entityModel.getValue(fieldName);

        if (fieldModel != null && fieldModel.getValue() != null && fieldModel instanceof DateFieldModel) {
            setZonedDateTime((ZonedDateTime) fieldModel.getValue());
        } else {
            setZonedDateTime(null);
        }
    }

}