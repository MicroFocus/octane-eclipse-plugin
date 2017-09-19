/*******************************************************************************
 * Copyright 2017 Hewlett-Packard Enterprise Development Company, L.P.
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
package com.hpe.octane.ideplugins.eclipse.ui.search;

import static com.hpe.octane.ideplugins.eclipse.util.EntityFieldsConstants.FIELD_DESCRIPTION;

import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.PlatformUI;

import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.octane.ideplugins.services.filtering.Entity;
import com.hpe.adm.octane.ideplugins.services.util.Util;
import com.hpe.octane.ideplugins.eclipse.ui.entitylist.custom.EntityModelRenderer;
import com.hpe.octane.ideplugins.eclipse.ui.entitylist.custom.EntityModelRow;
import com.hpe.octane.ideplugins.eclipse.util.EntityIconFactory;
import com.hpe.octane.ideplugins.eclipse.util.resource.SWTResourceManager;

public class SearchResultRowRenderer implements EntityModelRenderer {
	
	private Color backgroundColor = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getColorRegistry().get(JFacePreferences.CONTENT_ASSIST_BACKGROUND_COLOR);
	private Color foregroundColor = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getColorRegistry().get(JFacePreferences.CONTENT_ASSIST_FOREGROUND_COLOR);
	private static final EntityIconFactory entityIconFactory = new EntityIconFactory(40, 40, 14);

    @Override
    public Control createRow(Composite parent, EntityModel entityModel) {

        int entityId = Integer.valueOf(Util.getUiDataFromModel(entityModel.getValue("id")));
        Entity entityType = Entity.getEntityType(entityModel);

        EntityModelRow row = new EntityModelRow(parent, SWT.NONE);
        row.addPaintListener(new PaintListener() {
    		@Override
    	    public void paintControl(PaintEvent paintEvent) {        
        	    row.setBackground(backgroundColor);        	    
    	    }
        });  
        row.setForeground(foregroundColor);
        
        row.setEntityIcon(entityIconFactory.getImageIcon(entityType));

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
