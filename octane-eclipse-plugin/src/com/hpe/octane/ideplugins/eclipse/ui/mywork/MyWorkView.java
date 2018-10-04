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
package com.hpe.octane.ideplugins.eclipse.ui.mywork;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;

import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.octane.ideplugins.services.mywork.MyWorkService;
import com.hpe.adm.octane.ideplugins.services.mywork.MyWorkUtil;
import com.hpe.adm.octane.ideplugins.services.util.EntityUtil;
import com.hpe.octane.ideplugins.eclipse.Activator;
import com.hpe.octane.ideplugins.eclipse.filter.UserItemArrayEntityListData;
import com.hpe.octane.ideplugins.eclipse.preferences.PluginPreferencePage;
import com.hpe.octane.ideplugins.eclipse.preferences.PluginPreferenceStorage;
import com.hpe.octane.ideplugins.eclipse.preferences.PluginPreferenceStorage.PrefereceChangeHandler;
import com.hpe.octane.ideplugins.eclipse.ui.OctaneViewPart;
import com.hpe.octane.ideplugins.eclipse.ui.entitydetail.EntityModelEditorInput;
import com.hpe.octane.ideplugins.eclipse.ui.entitylist.EntityListComposite;
import com.hpe.octane.ideplugins.eclipse.ui.entitylist.custom.AbsoluteLayoutEntityListViewer;
import com.hpe.octane.ideplugins.eclipse.ui.mywork.rowrenderer.MyWorkEntityModelRowRenderer;
import com.hpe.octane.ideplugins.eclipse.ui.search.SearchEditor;
import com.hpe.octane.ideplugins.eclipse.ui.search.SearchEditorInput;
import com.hpe.octane.ideplugins.eclipse.ui.snake.SnakeEditor;
import com.hpe.octane.ideplugins.eclipse.ui.util.InfoPopup;
import com.hpe.octane.ideplugins.eclipse.ui.util.OpenDetailTabEntityMouseListener;
import com.hpe.octane.ideplugins.eclipse.ui.util.SeparatorControlContribution;
import com.hpe.octane.ideplugins.eclipse.ui.util.TextContributionItem;
import com.hpe.octane.ideplugins.eclipse.ui.util.error.ErrorComposite;
import com.hpe.octane.ideplugins.eclipse.ui.util.resource.SWTResourceManager;
import com.hpe.octane.ideplugins.eclipse.util.CommitMessageUtil;
import com.hpe.octane.ideplugins.eclipse.util.EntityFieldsConstants;

public class MyWorkView extends OctaneViewPart {
   
    private static final ILog logger = Activator.getDefault().getLog();

    public static final String ID = "com.hpe.octane.ideplugins.eclipse.ui.mywork.MyWorkView";
    
    private static final String LOADING_MESSAGE = "Loading \"My Work\"";
    private static final String darkBackgroundColorString = "rgb(52,57,61)";

    private Color backgroundColor = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getColorRegistry()
            .get(JFacePreferences.CONTENT_ASSIST_BACKGROUND_COLOR);
    
    private MyWorkService myWorkService = Activator.getInstance(MyWorkService.class);
    private UserItemArrayEntityListData entityData = new UserItemArrayEntityListData();
    private EntityListComposite entityListComposite;

    private Action refreshAction = new Action() {
        private Job refreshJob = new Job(LOADING_MESSAGE) {
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                showLoading();
                monitor.beginTask(LOADING_MESSAGE, IProgressMonitor.UNKNOWN);
                Collection<EntityModel> entities;
                try {
                    entities = myWorkService.getMyWork(MyWorkEntityModelRowRenderer.getRequiredFields());
                    // Remove active item if it's no longer in "My Work"

                    EntityModelEditorInput activeItem = PluginPreferenceStorage.getActiveItem();
                    if (activeItem != null && !userItemsContainsActiveItem(entities)) {
                        Display.getDefault().asyncExec(() -> {
                            PluginPreferenceStorage.setActiveItem(null);
                            new InfoPopup(
                                    "Active item cleared, no longer part of \"My Work\"",
                                    "Active item: \""
                                            + CommitMessageUtil.getEntityStringFromType(activeItem.getEntityType())
                                            + " " + activeItem.getId() + ": "
                                            + " " + activeItem.getTitle()
                                            + "\" has been removed, it is no longer part of \"My Work\"",
                                    400,
                                    100).open();
                        });
                    }
                    Display.getDefault().asyncExec(() -> {
                        entityData.setEntityList(entities);
                        entityListComposite.refreshIcons();
                        if (entities.size() == 0) {
                            showControl(noWorkComposite);
                        } else {
                            showContent();
                        }
                    });
                } catch (Exception e) {
                    Display.getDefault().asyncExec(() -> {
                        showControl(errorAndSettingsComposite);
                        errorComposite.displayException(e);
                        entityData.setEntityList(Collections.emptyList());

                        Display.getDefault().asyncExec(() -> {
                            PluginPreferenceStorage.setActiveItem(null);
                            new InfoPopup(
                                    "Your Previously saved connection settings do not seem to work",
                                    "Please go to settings and test your connection to Octane",
                                    400,
                                    100,
                                    false,
                                    true).open();
                        });
                    });
                }
                monitor.done();
                return Status.OK_STATUS;
            }
        };

        @Override
        public void run() {
            refreshJob.schedule();
        }
    };

    /**
     * Shown when my work service returns an empty list
     */
    private NoWorkComposite noWorkComposite;
    private ErrorComposite errorComposite;
    private TextContributionItem textContributionItem;
    private Composite errorAndSettingsComposite;

    @Override
    public Control createOctanePartControl(Composite parent) {

        entityListComposite = new EntityListComposite(
                parent,
                SWT.NONE,
                entityData,
                (viewerParent) -> {
                    AbsoluteLayoutEntityListViewer viewer = new AbsoluteLayoutEntityListViewer((Composite) viewerParent,
                            SWT.NONE,
                            new MyWorkEntityModelRowRenderer(),
                            new MyWorkEntityModelMenuFactory(entityData));
                    
                    // nasty workaround, will force the view to refresh all the
                    // rows, drawing the green thingy on the icons
                    PrefereceChangeHandler prefereceChangeHandler = () -> {
                        if(!viewer.isDisposed()) {
                            viewer.forceRedrawRows();
                        }
                    };
        
                    PluginPreferenceStorage.addPrefenceChangeHandler(PluginPreferenceStorage.PreferenceConstants.ACTIVE_ITEM_ID, prefereceChangeHandler);
                    
                    viewer.addDisposeListener(e -> 
                        PluginPreferenceStorage.removePrefenceChangeHandler(
                                    PluginPreferenceStorage.PreferenceConstants.ACTIVE_ITEM_ID, 
                                    prefereceChangeHandler)
                        );
                    
                    return viewer;
                },
                MyWorkEntityModelRowRenderer.getRequiredFields().keySet(),
                MyWorkEntityModelRowRenderer.getRequiredFields()
                        .values()
                        .stream()
                        .flatMap(col -> col.stream())
                        .filter(field -> !field.equals(EntityFieldsConstants.FIELD_TYPE) && !field.equals(EntityFieldsConstants.FIELD_SUBTYPE))
                        .collect(Collectors.toSet()));

        String backgroundColorString = "rgb(" + backgroundColor.getRed() + "," + backgroundColor.getGreen() + "," + backgroundColor.getBlue() + ")";

        if (backgroundColorString.equals(darkBackgroundColorString)) {
            entityListComposite.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
        } else {
            entityListComposite.setBackground(backgroundColor);
        }
        noWorkComposite = new NoWorkComposite(parent, SWT.NONE, new Runnable() {
            @Override
            public void run() {
                // Unfortunately the game explodes on mac os, causing the ide to
                // not respond, don't have time to fix now
                String os = System.getProperty("os.name").toLowerCase();
                if (os != null && os.indexOf("win") >= 0) {
                    IWorkbenchPage currentPage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                    try {
                        currentPage.openEditor(SnakeEditor.snakeEditorInput, SnakeEditor.ID);
                    } catch (PartInitException ignored) {
                    }
                }
            }
        });
        errorAndSettingsComposite = new Composite(parent, SWT.BORDER);
        errorAndSettingsComposite.setLayout(new GridLayout(1, false));
        errorAndSettingsComposite.setBackground(SWTResourceManager.getColor(SWT.COLOR_LIST_BACKGROUND));
        errorAndSettingsComposite.setBackgroundMode(SWT.INHERIT_FORCE);
        errorAndSettingsComposite.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true, 1, 1));

        errorComposite = new ErrorComposite(errorAndSettingsComposite, SWT.NONE);
        errorComposite.setLayoutData(new GridData(SWT.CENTER, SWT.BOTTOM, true, true, 1, 1));
        
        Link link = new Link(errorAndSettingsComposite, SWT.NONE);
        link.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1));
        link.setText("<A>" + "Something went wrong! Click here to go to Settings." + "</A>");
        link.setFont(new Font(null, "Arial", 12, SWT.NONE));
        link.setForeground(new Color(Display.getCurrent(), 204, 0, 0));

        link.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                PreferencesUtil.createPreferenceDialogOn(parent.getShell(),
                        PluginPreferencePage.ID,
                        null,
                        null).open();
            }
        });

        IActionBars viewToolbar = getViewSite().getActionBars();

        // Add search action to view toolbar
        textContributionItem = new TextContributionItem(ID + ".searchtext");
        textContributionItem.setControlCreatedRunnable(
                () -> {
                    textContributionItem.setMessage("Global search");
                    textContributionItem.addTraverseListener(new TraverseListener() {
                        @Override
                        public void keyTraversed(TraverseEvent e) {
                            if (e.detail == SWT.TRAVERSE_RETURN) {
                                // Open search editor
                                SearchEditorInput searchEditorInput = new SearchEditorInput(textContributionItem.getText());
                                try {
                                    logger.log(new Status(
                                            Status.INFO,
                                            Activator.PLUGIN_ID,
                                            Status.OK,
                                            searchEditorInput.toString(),
                                            null));

                                    MyWorkView.this.getSite().getPage()
                                            .openEditor(searchEditorInput, SearchEditor.ID);

                                } catch (PartInitException ex) {
                                    logger.log(new Status(
                                            Status.ERROR,
                                            Activator.PLUGIN_ID,
                                            Status.ERROR,
                                            "An exception has occured when opening the editor",
                                            ex));
                                }
                            }
                        }
                    });
                });
        viewToolbar.getToolBarManager().add(textContributionItem);

        viewToolbar.getToolBarManager().add(new SeparatorControlContribution(ID + ".separator"));

        // Add refresh action to view toolbar
        refreshAction.setText("Refresh");
        refreshAction.setToolTipText("Refresh \"My Work\"");
        refreshAction.setImageDescriptor(Activator.getImageDescriptor("icons/refresh-16x16.png"));
        ActionContributionItem refreshActionItem = new ActionContributionItem(refreshAction);
        refreshActionItem.setMode(ActionContributionItem.MODE_FORCE_TEXT);
        viewToolbar.getToolBarManager().add(refreshActionItem);

        // Mouse handlers
        entityListComposite.addEntityMouseListener(new OpenDetailTabEntityMouseListener());

        // Init
        Runnable initRunnable = () -> {
            if (!Activator.getConnectionSettings().isEmpty()) {
                refreshAction.setEnabled(true);
                textContributionItem.setEnabled(true);
                refreshAction.run();
            } else {
                showWelcome();
                refreshAction.setEnabled(false);
                textContributionItem.setEnabled(false);
            }
        };

        Activator.addConnectionSettingsChangeHandler(initRunnable);
        initRunnable.run();

        // Return root
        return entityListComposite;
    }

    public void refresh() {
        refreshAction.run();
    }

    @Override
    public void setFocus() {
        // TODO Auto-generated method stub
    }

    /**
     * Check if the provided list of Entity.USER_ITEM contains the current
     * active item
     * 
     * @param entityModels
     */
    public static boolean userItemsContainsActiveItem(Collection<EntityModel> entityModels) {
        Collection<EntityModel> entities = MyWorkUtil.getEntityModelsFromUserItems(entityModels);
        EntityModelEditorInput activeItem = PluginPreferenceStorage.getActiveItem();

        if (activeItem == null) {
            return false;
        }

        return entities
                .stream()
                .anyMatch(entity -> EntityUtil.areEqual(entity, activeItem.toEntityModel()));
    }

}
