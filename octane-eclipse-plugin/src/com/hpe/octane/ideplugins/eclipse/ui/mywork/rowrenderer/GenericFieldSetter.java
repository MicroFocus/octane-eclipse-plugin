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
package com.hpe.octane.ideplugins.eclipse.ui.mywork.rowrenderer;

import static com.hpe.adm.octane.ideplugins.services.util.Util.getUiDataFromModel;
import static com.hpe.octane.ideplugins.eclipse.util.EntityFieldsConstants.FIELD_AUTHOR;
import static com.hpe.octane.ideplugins.eclipse.util.EntityFieldsConstants.FIELD_FULL_NAME;
import static com.hpe.octane.ideplugins.eclipse.util.EntityFieldsConstants.FIELD_OWNER;

import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.octane.ideplugins.eclipse.ui.entitylist.custom.EntityModelRow;
import com.hpe.octane.ideplugins.eclipse.ui.entitylist.custom.EntityModelRow.DetailsPosition;

/**
 * For setting the row subtitle, use detail position <code>null</code>
 */
class GenericFieldSetter implements RowFieldSetter {

    private String fieldName;
    private String defaultValue = "";
    private String fieldLabel;
    private DetailsPosition position;

    public GenericFieldSetter(String fieldName, String fieldLabel, DetailsPosition position) {
        this.fieldName = fieldName;
        this.fieldLabel = fieldLabel;
        this.position = position;
    }

    public GenericFieldSetter(String fieldName, String defaultValue) {
        this.fieldName = fieldName;
        this.defaultValue = defaultValue;
    }

    @Override
    public void setField(EntityModelRow row, EntityModel entityModel) {
        if (position == null) {
            row.setEntitySubTitle(getUiDataFromModel(entityModel.getValue(fieldName)), defaultValue);
        } else {
            if (FIELD_OWNER.equals(fieldName) || FIELD_AUTHOR.equals(fieldName)) {
                row.addDetails(fieldLabel, getUiDataFromModel(entityModel.getValue(fieldName), FIELD_FULL_NAME), position);
            } else {
                row.addDetails(fieldLabel, getUiDataFromModel(entityModel.getValue(fieldName)), position);
            }
        }
    }

    @Override
    public String[] getFieldNames() {
        return new String[] { fieldName };
    }
}
