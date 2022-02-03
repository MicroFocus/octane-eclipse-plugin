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

import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.octane.ideplugins.eclipse.ui.entitylist.custom.EntityModelRow;
import com.hpe.octane.ideplugins.eclipse.util.EntityFieldsConstants;

class TitleFieldSetter implements RowFieldSetter {

    private static String fieldName = EntityFieldsConstants.FIELD_NAME;
    private static String fieldId = EntityFieldsConstants.FIELD_ID;

    @Override
    public void setField(EntityModelRow row, EntityModel entityModel) {
        Integer id = Integer.valueOf(getUiDataFromModel(entityModel.getValue(fieldId)));
        String name = getUiDataFromModel(entityModel.getValue(fieldName));
        row.setEntityTitle(id, name);
    }

    @Override
    public String[] getFieldNames() {
        return new String[] { fieldId, fieldName };
    }
}
