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
package com.hpe.octane.ideplugins.eclipse.ui.entitylist;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.octane.ideplugins.services.filtering.Entity;
import com.hpe.adm.octane.ideplugins.services.mywork.MyWorkUtil;
import com.hpe.octane.ideplugins.eclipse.filter.EntityListData;
import com.hpe.octane.ideplugins.eclipse.ui.util.ControlProvider;
import com.hpe.octane.ideplugins.eclipse.ui.util.DelayedModifyListener;
import com.hpe.octane.ideplugins.eclipse.ui.util.resource.SWTResourceManager;

public class EntityListComposite extends Composite {

    private EntityListData entityListData;
    private Text textFilter;
    private EntityTypeSelectorComposite entityTypeSelectorComposite;
    private Color backgroundColor = SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT);
    private Set<Entity> filterTypes;

    // Currently only fatlines
    private EntityListViewer entityListViewer;
    private ControlProvider<EntityListViewer> controlProvider;

    public EntityListComposite(
            Composite parent,
            int style,
            EntityListData entityListData,
            ControlProvider<EntityListViewer> controlProvider,
            Set<Entity> filterTypes,
            Set<String> clientSideQueryFields) {

        super(parent, style);
        setLayout(new GridLayout(1, false));

        this.entityListData = entityListData;
        this.controlProvider = controlProvider;

        this.filterTypes = filterTypes;

        entityListData.setTypeFilter(filterTypes);
        entityListData.setStringFilterFields(clientSideQueryFields);

        init();
        entityListViewer.setEntityModels(entityListData.getEntityList());
    }

    private void init() {
        setBackground(backgroundColor);
        setBackgroundMode(SWT.INHERIT_FORCE);

        entityTypeSelectorComposite = new EntityTypeSelectorComposite(this, SWT.NONE, filterTypes.toArray(new Entity[] {}));
        entityTypeSelectorComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        entityTypeSelectorComposite.checkAll();
        entityTypeSelectorComposite.setBackground(backgroundColor);
        entityTypeSelectorComposite.addSelectionListener(() -> {
            entityListData.setTypeFilter(entityTypeSelectorComposite.getCheckedEntityTypes());
        });

        textFilter = new Text(this, SWT.BORDER);
        textFilter.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        textFilter.setMessage("Filter");
        textFilter.addModifyListener(new DelayedModifyListener((e) -> {
            String text = textFilter.getText();
            text = text.trim();
            text = text.toLowerCase();
            entityListData.setStringFilter(text);
        }));

        // Just a placeholder for the viewer
        Composite compositeEntityList = new Composite(this, SWT.NONE);
        compositeEntityList.setLayout(new FillLayout(SWT.HORIZONTAL));
        compositeEntityList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        

        entityListViewer = controlProvider.createControl(compositeEntityList);
        entityListData.addDataChangedHandler(entityList -> entityListViewer.setEntityModels(entityList));
        entityListData.addDataChangedHandler(entityList -> {
            entityTypeSelectorComposite
                    .setEntityTypeCount(
                            countEntitiesByType(entityListData.getOriginalEntityList()));
        });
    }

    private Map<Entity, Integer> countEntitiesByType(Collection<EntityModel> entities) {
        Map<Entity, Integer> result = new HashMap<>();

        entities.forEach(entityModel -> {
            Entity entityType = Entity.getEntityType(entityModel);
            if (entityType == Entity.USER_ITEM) {
                entityType = Entity.getEntityType(MyWorkUtil.getEntityModelFromUserItem(entityModel));
            }
            if (!result.containsKey(entityType)) {
                result.put(entityType, 0);
            }
            result.put(entityType, result.get(entityType) + 1);
        });
        return result;
    }

    @Override
    protected void checkSubclass() {
        // Disable the check that prevents subclassing of SWT components
    }

    public void addEntityMouseListener(EntityMouseListener entityMouseListener) {
        entityListViewer.addEntityMouseListener(entityMouseListener);
    }

    public void removeEntityMouseListener(EntityMouseListener entityMouseListener) {
        entityListViewer.removeEntityMouseListener(entityMouseListener);
    }
    
    public void refreshIcons() {
        entityTypeSelectorComposite.refreshIcons();
    }
}
