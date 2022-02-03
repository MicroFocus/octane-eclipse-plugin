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
package com.hpe.octane.ideplugins.eclipse.ui.search;

import static com.hpe.octane.ideplugins.eclipse.util.EntityFieldsConstants.FIELD_DESCRIPTION;
import static com.hpe.octane.ideplugins.eclipse.util.EntityFieldsConstants.FIELD_ID;
import static com.hpe.octane.ideplugins.eclipse.util.EntityFieldsConstants.FIELD_NAME;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.EditorPart;

import com.hpe.adm.octane.ideplugins.services.connection.ConnectionSettingsProvider;
import com.hpe.adm.octane.ideplugins.services.filtering.Entity;
import com.hpe.adm.octane.ideplugins.services.nonentity.OctaneVersionService;
import com.hpe.adm.octane.ideplugins.services.util.OctaneVersion;
import com.hpe.adm.octane.ideplugins.services.util.Util;
import com.hpe.octane.ideplugins.eclipse.Activator;
import com.hpe.octane.ideplugins.eclipse.filter.ArrayEntityListData;
import com.hpe.octane.ideplugins.eclipse.ui.entitylist.EntityListComposite;
import com.hpe.octane.ideplugins.eclipse.ui.entitylist.custom.AbsoluteLayoutEntityListViewer;
import com.hpe.octane.ideplugins.eclipse.ui.util.LoadingComposite;
import com.hpe.octane.ideplugins.eclipse.ui.util.OpenDetailTabEntityMouseListener;
import com.hpe.octane.ideplugins.eclipse.ui.util.StackLayoutComposite;
import com.hpe.octane.ideplugins.eclipse.ui.util.resource.SWTResourceManager;

public class SearchEditor extends EditorPart {

    // private static final ILog logger = Activator.getDefault().getLog();

    public static final String ID = "com.hpe.octane.ideplugins.eclipse.ui.search.SearchEditor";
    private String darkBackgroundColorString = "rgb(52,57,61)"; 
    ConnectionSettingsProvider connectionSettingsProvider = Activator.getInstance(ConnectionSettingsProvider.class);
    
    static final Set<Entity> searchEntityTypes = new LinkedHashSet<>(Arrays.asList(
            Entity.EPIC,
            Entity.FEATURE,
            Entity.USER_STORY,
            Entity.QUALITY_STORY,
            Entity.DEFECT,
            Entity.TASK,
            Entity.TEST_SUITE,
            Entity.MANUAL_TEST,
            Entity.AUTOMATED_TEST,
            Entity.GHERKIN_TEST,
            Entity.REQUIREMENT));

    private static final Set<String> searchEntityFilterFields = new HashSet<>(Arrays.asList(FIELD_ID, FIELD_NAME, FIELD_DESCRIPTION));

    private ArrayEntityListData entityData = new ArrayEntityListData();
    private EntityListComposite entityListComposite;
    private SearchEditorInput searchEditorInput;

    private NoSearchResultsComposite noSearchResultsComposite;
    private LoadingComposite loadingComposite;
    private StackLayoutComposite container;

    private SearchJob searchJob;
    
    private Color backgroundColor = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getColorRegistry().get(JFacePreferences.CONTENT_ASSIST_BACKGROUND_COLOR);
    private Color foregroundColor = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getColorRegistry().get(JFacePreferences.CONTENT_ASSIST_FOREGROUND_COLOR);

    @Override
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        if (!(input instanceof SearchEditorInput)) {
            throw new RuntimeException("Wrong input");
        }

        this.searchEditorInput = (SearchEditorInput) input;
        setSite(site);
        setInput(input);

        String tabTitle = searchEditorInput.getQuery();
        if (tabTitle.length() > 25) {
            tabTitle = Util.ellipsisTruncate(tabTitle, 25);
        }
        tabTitle = "\"" + tabTitle + "\"";
        setPartName(tabTitle);
    }

    @Override
    public void createPartControl(Composite parent) {

        container = new StackLayoutComposite(parent, SWT.NONE);
        container.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
        container.setForeground(foregroundColor);
        
        Set<Entity> searchEntityTypesCopy = new LinkedHashSet<>(searchEntityTypes);
        // add BDD to searchEntityTypes for Octane versions higher or eq than Coldplay P1 ( 15.1.4 - version where BDD was implemented )
        OctaneVersion octaneVersion = OctaneVersionService.getOctaneVersion(connectionSettingsProvider.getConnectionSettings());
        if (octaneVersion.isMoreOrEqThan(OctaneVersion.COLDPLAY_P1) && !searchEntityTypesCopy.contains(Entity.BDD_SCENARIO)) {
            searchEntityTypesCopy.add(Entity.BDD_SCENARIO);
        }

        entityListComposite = new EntityListComposite(
                container,
                SWT.NONE,
                entityData,
                (parentControl) -> {
                    return new AbsoluteLayoutEntityListViewer((Composite) parentControl,
                            SWT.NONE,
                            new SearchResultRowRenderer(),
                            new SearchEntityModelMenuFactory());
                },
                searchEntityTypesCopy,
                searchEntityFilterFields);
        entityListComposite.setBackground(backgroundColor);
        String backgroundColorString = "rgb(" + backgroundColor.getRed() + "," + backgroundColor.getGreen() + "," + backgroundColor.getBlue() + ")" ;
        
        if(backgroundColorString.equals(darkBackgroundColorString)) {
        	entityListComposite.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
        } else {
        	entityListComposite.setBackground(backgroundColor);
        }
        noSearchResultsComposite = new NoSearchResultsComposite(container, SWT.NONE);
        loadingComposite = new LoadingComposite(container, SWT.NONE);

        entityListComposite.addEntityMouseListener(new OpenDetailTabEntityMouseListener());
        entityListComposite.setBackgroundMode(SWT.INHERIT_FORCE);
        searchJob = new SearchJob(
                "Searching Octane for: \"" + searchEditorInput.getQuery() + "\"",
                searchEditorInput.getQuery(),
                entityData,
                searchEntityTypesCopy.toArray(new Entity[] {}));

        container.showControl(loadingComposite);

        searchJob.schedule();
        searchJob.addJobChangeListener(new JobChangeAdapter() {
            @Override
            public void done(IJobChangeEvent event) {
                Display.getDefault().asyncExec(() -> {
                    // The user can close the search tab before the job returned
                    // the result, so we need a disposed check
                    if (!container.isDisposed()) {
                        if (entityData.getOriginalEntityList().size() == 0) {
                            container.showControl(noSearchResultsComposite);
                        } else {
                        	entityListComposite.refreshIcons();
                            container.showControl(entityListComposite);
                        }
                    }
                });
            }
        });
    }

    @Override
    public void setFocus() {
    }

    @Override
    public void doSave(IProgressMonitor monitor) {
    }

    @Override
    public void doSaveAs() {
    }

    @Override
    public boolean isDirty() {
        return false;
    }

    @Override
    public boolean isSaveAsAllowed() {
        return false;
    }

}
