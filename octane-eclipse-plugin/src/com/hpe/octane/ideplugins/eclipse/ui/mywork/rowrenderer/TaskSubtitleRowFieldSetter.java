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

import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.octane.ideplugins.services.filtering.Entity;
import com.hpe.octane.ideplugins.eclipse.ui.entitylist.custom.EntityModelRow;
import com.hpe.octane.ideplugins.eclipse.util.CommitMessageUtil;

public class TaskSubtitleRowFieldSetter implements RowFieldSetter {

    @Override
    public void setField(EntityModelRow row, EntityModel entityModel) {
        // Add parent details for tasks
        EntityModel storyEntityModel = (EntityModel) entityModel.getValue("story").getValue();

        StringBuilder parentInfoSb = new StringBuilder();
        parentInfoSb.append("Task of ");
        parentInfoSb.append(CommitMessageUtil.getEntityStringFromType(Entity.getEntityType(storyEntityModel)));
        parentInfoSb.append(" " + storyEntityModel.getValue("id").getValue().toString() + ": ");
        parentInfoSb.append(storyEntityModel.getValue("name").getValue().toString());

        row.setEntitySubTitle(parentInfoSb.toString(), "no parent");
    }

    @Override
    public String[] getFieldNames() {
        return new String[] { "story" };
    }

}
