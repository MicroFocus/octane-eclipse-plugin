/*******************************************************************************
 * © 2017 EntIT Software LLC, a Micro Focus company, L.P.
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

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.octane.ideplugins.services.EntityService;
import com.hpe.adm.octane.ideplugins.services.filtering.Entity;
import com.hpe.adm.octane.ideplugins.services.mywork.MyWorkUtil;
import com.hpe.adm.octane.ideplugins.services.util.Util;
import com.hpe.octane.ideplugins.eclipse.Activator;
import com.hpe.octane.ideplugins.eclipse.ui.entitydetail.EntityModelEditor;
import com.hpe.octane.ideplugins.eclipse.ui.entitydetail.EntityModelEditorInput;
import com.hpe.octane.ideplugins.eclipse.ui.entitylist.EntityMouseListener;

public class OpenDetailTabEntityMouseListener implements EntityMouseListener {

    private static final ILog logger = Activator.getDefault().getLog();
    private static EntityService entityService = Activator.getInstance(EntityService.class);

    @Override
    public void mouseClick(EntityModel entityModel, MouseEvent event) {
        // Open detail tab
        if (event.count == 2) {
            IWorkbench wb = PlatformUI.getWorkbench();
            IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
            IWorkbenchPage page = win.getActivePage();

            if (Entity.USER_ITEM == Entity.getEntityType(entityModel)) {
                entityModel = MyWorkUtil.getEntityModelFromUserItem(entityModel);
            }

            if (Entity.COMMENT == Entity.getEntityType(entityModel)) {
                entityModel = (EntityModel) Util.getContainerItemForCommentModel(entityModel).getValue();
            }
            
            if(Entity.FEATURE == Entity.getEntityType(entityModel) || Entity.EPIC == Entity.getEntityType(entityModel) || Entity.AUTOMATED_TEST_RUN == Entity.getEntityType(entityModel)) {
            	entityService.openInBrowser(entityModel);
            } else {
	            Long id = Long.parseLong(entityModel.getValue("id").getValue().toString());
	            EntityModelEditorInput entityModelEditorInput = new EntityModelEditorInput(id, Entity.getEntityType(entityModel));
	            try {
	                logger.log(new Status(Status.INFO, Activator.PLUGIN_ID, Status.OK, entityModelEditorInput.toString(), null));                
	                page.openEditor(entityModelEditorInput, EntityModelEditor.ID);                                      
	            } catch (PartInitException ex) {
	                logger.log(
	                        new Status(Status.ERROR, Activator.PLUGIN_ID, Status.ERROR, "An exception has occured when opening the editor", ex));
	            }
            }
            
        }
    }

}
