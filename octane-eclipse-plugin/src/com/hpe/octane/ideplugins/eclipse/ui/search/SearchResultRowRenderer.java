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
package com.hpe.octane.ideplugins.eclipse.ui.search;

import static com.hpe.octane.ideplugins.eclipse.util.EntityFieldsConstants.FIELD_DESCRIPTION;

import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.PlatformUI;

import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.octane.ideplugins.services.filtering.Entity;
import com.hpe.adm.octane.ideplugins.services.util.Util;
import com.hpe.octane.ideplugins.eclipse.ui.entitylist.custom.EntityModelRenderer;
import com.hpe.octane.ideplugins.eclipse.ui.entitylist.custom.EntityModelRow;
import com.hpe.octane.ideplugins.eclipse.ui.util.icon.EntityIconFactory;
import com.hpe.octane.ideplugins.eclipse.ui.util.resource.SWTResourceManager;

public class SearchResultRowRenderer implements EntityModelRenderer {

    private Color foregroundColor = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getColorRegistry()
            .get(JFacePreferences.CONTENT_ASSIST_FOREGROUND_COLOR);

    @Override
    public Control createRow(Composite parent, EntityModel entityModel) {

        int entityId = Integer.valueOf(Util.getUiDataFromModel(entityModel.getValue("id")));
        Entity entityType = Entity.getEntityType(entityModel);

        EntityModelRow row = new EntityModelRow(parent, SWT.NONE);
        row.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
        row.setForeground(foregroundColor);

        row.setEntityIcon(EntityIconFactory.getInstance().getImageIcon(entityType, 40, 14));

        String name = Util.getUiDataFromModel(entityModel.getValue("name"));
        name = Util.stripHtml(name);
        row.setEntityTitle(entityId, name);

        String description = Util.getUiDataFromModel(entityModel.getValue(FIELD_DESCRIPTION));
        description = Util.stripHtml(description);
        description = Util.ellipsisTruncate(description, 100); // magic!
        row.setEntitySubTitle(description);

        return row;
    }

}
