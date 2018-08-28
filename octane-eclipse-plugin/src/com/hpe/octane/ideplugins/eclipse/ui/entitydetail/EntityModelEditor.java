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
package com.hpe.octane.ideplugins.eclipse.ui.entitydetail;


import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.EditorPart;

import com.hpe.adm.nga.sdk.exception.OctaneException;
import com.hpe.adm.nga.sdk.model.FieldModel;
import com.hpe.adm.octane.ideplugins.services.EntityService;
import com.hpe.adm.octane.ideplugins.services.model.EntityModelWrapper;
import com.hpe.adm.octane.ideplugins.services.model.EntityModelWrapper.FieldModelChangedHandler;
import com.hpe.octane.ideplugins.eclipse.Activator;
import com.hpe.octane.ideplugins.eclipse.ui.entitydetail.job.GetEntityModelJob;
import com.hpe.octane.ideplugins.eclipse.ui.entitydetail.job.UpdateEntityJob;
import com.hpe.octane.ideplugins.eclipse.ui.util.LoadingComposite;
import com.hpe.octane.ideplugins.eclipse.ui.util.StackLayoutComposite;
import com.hpe.octane.ideplugins.eclipse.ui.util.error.ErrorDialog;
import com.hpe.octane.ideplugins.eclipse.ui.util.icon.EntityIconFactory;
import com.hpe.octane.ideplugins.eclipse.ui.util.resource.PlatformResourcesManager;
import com.hpe.octane.ideplugins.eclipse.ui.util.resource.SWTResourceManager;

public class EntityModelEditor extends EditorPart {

    public static final String ID = "com.hpe.octane.ideplugins.eclipse.ui.entitydetail.EntityModelEditor"; //$NON-NLS-1$
    private static final EntityIconFactory entityIconFactoryForTabInfo = new EntityIconFactory(20, 20, 8);
    private static final String SAVE_FAILED_DIALOG_TITLE = "Saving entity failed";

    private static EntityService entityService = Activator.getInstance(EntityService.class);
    
    public EntityModelEditorInput input;
    private EntityModelWrapper entityModelWrapper;
    private EntityComposite entityComposite;
    private StackLayoutComposite rootComposite;
    private LoadingComposite loadingComposite;
    private boolean isDirty = false;
    
    private Image img;
    
    @Override
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {

        if (!(input instanceof EntityModelEditorInput)) {
            throw new RuntimeException("Wrong input");
        }
        this.input = (EntityModelEditorInput) input;

        setSite(site);
        setInput(input);
        
        setPartName(String.valueOf(this.input.getId()));
        img = entityIconFactoryForTabInfo.getImageForEditorPart(this.input.getEntityType());
//        Display display = Display.getCurrent();
//        Color blue = display.getSystemColor(SWT.COLOR_BLUE);
//        img.setBackground(blue);
      
        setTitleImage(img);
        
    }
    
   

    @Override
    public void createPartControl(Composite parent) {
        rootComposite = new StackLayoutComposite(parent, SWT.NONE);
        rootComposite.setBackgroundMode(SWT.INHERIT_FORCE);
        rootComposite.setForeground(SWTResourceManager.getColor(SWT.COLOR_LIST_SELECTION));
        rootComposite.setBackground(PlatformResourcesManager.getPlatformBackgroundColor());

        loadingComposite = new LoadingComposite(rootComposite, SWT.NONE);
        rootComposite.showControl(loadingComposite);

        entityComposite = new EntityComposite(rootComposite, SWT.NONE);
        entityComposite.addRefreshSelectionListener(event -> loadEntity());

        entityComposite.addSaveSelectionListener(new Listener() {
            @Override
            public void handleEvent(Event event) {
                doSave(null);
            }
        });
        loadEntity();
    }

    private void loadEntity() {
        GetEntityModelJob getEntityDetailsJob = new GetEntityModelJob("Retrieving entity details", input.getEntityType(), input.getId());

        getEntityDetailsJob.addJobChangeListener(new JobChangeAdapter() {

            @Override
            public void scheduled(IJobChangeEvent event) {
                Display.getDefault().asyncExec(() -> {
                    rootComposite.showControl(loadingComposite);
                });
            }

            @Override
            public void done(IJobChangeEvent event) {
                if (getEntityDetailsJob.wasEntityRetrived()) {

                    EntityModelEditor.this.entityModelWrapper = new EntityModelWrapper(getEntityDetailsJob.getEntiyData());
                    initIsDirtyListener();

                    Display.getDefault().asyncExec(() -> {
                        entityComposite.setEntityModel(entityModelWrapper);
                        rootComposite.showControl(entityComposite);
                    });
                } else {
                    Display.getDefault().asyncExec(() -> {
                        ErrorDialog errorDialog = new ErrorDialog(rootComposite.getShell());
                        errorDialog.addButton("Try again", () -> {
                            loadEntity();
                            errorDialog.close();
                        });
                        errorDialog.addButton("Close", () -> {
                            getSite().getPage().closeEditor(EntityModelEditor.this, false);
                            errorDialog.close();
                        });
                        errorDialog.displayException(getEntityDetailsJob.getException(), "Failed to load backlog item");
                    });
                }
            }
        });

        getEntityDetailsJob.schedule();
    }

    private void initIsDirtyListener() {
        setIsDirty(false);
        entityModelWrapper.addFieldModelChangedHandler(new FieldModelChangedHandler() {
            @Override
            public void fieldModelChanged(@SuppressWarnings("rawtypes") FieldModel fieldModel) {
                setIsDirty(true);
            }
        });
    }

    private void setIsDirty(boolean isDirty) {
        Display.getDefault().syncExec(() -> {
            EntityModelEditor.this.isDirty = isDirty;
            firePropertyChange(IEditorPart.PROP_DIRTY);
        });
    }

    @Override
    public void setFocus() {}

    @Override
    public void doSave(IProgressMonitor monitor) {
        UpdateEntityJob updateEntityJob = new UpdateEntityJob("Saving " + entityModelWrapper.getEntityType(), entityModelWrapper.getEntityModel());
        updateEntityJob.schedule();
        
        try {
            updateEntityJob.join(1000 * 10, monitor);
            OctaneException octaneException = updateEntityJob.getOctaneException();
            if (octaneException != null) {
                throw octaneException;
            } else {
                loadEntity(); //reload entity from server if save was successful
            }
        } catch (OperationCanceledException | InterruptedException | OctaneException ex) {
            ErrorDialog errorDialog = new ErrorDialog(rootComposite.getShell());
            errorDialog.addButton("Back", () -> errorDialog.close());
            errorDialog.addButton("Refresh", () -> {
                loadEntity();
                errorDialog.close();
            });
            errorDialog.addButton("Open in browser", () -> {
                entityService.openInBrowser(entityModelWrapper.getReadOnlyEntityModel());
                errorDialog.close();
            });
            errorDialog.displayException(ex, SAVE_FAILED_DIALOG_TITLE);
            
            // This would stop the editor from closing, if the editor was being closed before the save
            // The monitor can be null because of doSaveAs()
            // The monitor being null will not affect the editor b4 close, 
            // because when the platform saves b4 close, it will always use the doSave method, i. e. the monitor won't be null
            if(monitor != null) {
                monitor.setCanceled(true);
            }
        }
    }

    @Override
    public void doSaveAs() {
        doSave(null);
    }

    @Override
    public boolean isDirty() {
        return isDirty;
    }

    @Override
    public boolean isSaveAsAllowed() {
        return entityModelWrapper != null && isDirty;
    }
}