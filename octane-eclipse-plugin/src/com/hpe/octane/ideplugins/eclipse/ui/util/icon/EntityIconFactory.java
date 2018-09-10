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
package com.hpe.octane.ideplugins.eclipse.ui.util.icon;


import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

import com.hpe.adm.octane.ideplugins.services.connection.ConnectionSettingsProvider;
import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.octane.ideplugins.services.EntityLabelService;
import com.hpe.adm.octane.ideplugins.services.filtering.Entity;
import com.hpe.octane.ideplugins.eclipse.Activator;
import com.hpe.octane.ideplugins.eclipse.ui.util.resource.ImageResources;

public class EntityIconFactory {

    private static final IconDetail undefinedIconDetail = new IconDetail(0, 0, 0, "N/A");
    private static String INITIALS = "initials";
    
    private EntityLabelService entityLabelService;
    
    // map to color and short text
    private final Map<Entity, IconDetail> iconDetailMap = new HashMap<>();
    // cache images by size font and entity
    private final Map<Integer, Map<Integer, Map<Entity, ImageData>>> imageDataCache = new HashMap<>();

    private Color fontColor = new Color(Display.getCurrent(), 255, 255, 255);
    
    private static Image activeImg = ImageResources.ACTIVEITEM.getImage();
    
    private static EntityIconFactory instance;
    
    private EntityIconFactory() {
        this.entityLabelService = Activator.getInstance(EntityLabelService.class);
        ConnectionSettingsProvider connectionSettingsProvider = Activator.getInstance(ConnectionSettingsProvider.class);
        
        connectionSettingsProvider.addChangeHandler(() -> {
            iconDetailMap.clear();
            imageDataCache.clear();
            init();
        });
        
        init();
    }
    
    public static EntityIconFactory getInstance() {
        if(instance == null) {
            instance = new EntityIconFactory();
        }    
        return instance;
    }
    
    
    private void init() {
        Map<String, EntityModel> entityLabels = entityLabelService.getEntityLabelDetails();
        
        iconDetailMap.put(Entity.USER_STORY, new IconDetail(255, 176, 0, 
                entityLabels.get(Entity.USER_STORY.getSubtypeName()).getValue(INITIALS).getValue().toString()));
        iconDetailMap.put(Entity.QUALITY_STORY, new IconDetail(51, 193, 128, 
                entityLabels.get(Entity.QUALITY_STORY.getEntityName()).getValue(INITIALS).getValue().toString()));
        iconDetailMap.put(Entity.DEFECT, new IconDetail(178, 22, 70, 
                entityLabels.get(Entity.DEFECT.getEntityName()).getValue(INITIALS).getValue().toString()));
        iconDetailMap.put(Entity.EPIC, new IconDetail(116, 37, 173, 
                entityLabels.get(Entity.EPIC.getEntityName()).getValue(INITIALS).getValue().toString()));
        iconDetailMap.put(Entity.FEATURE, new IconDetail(229, 120, 40, 
                entityLabels.get(Entity.FEATURE.getEntityName()).getValue(INITIALS).getValue().toString()));
        iconDetailMap.put(Entity.REQUIREMENT, new IconDetail(11, 142, 172, 
                entityLabels.get(Entity.REQUIREMENT.getTypeName()).getValue(INITIALS).getValue().toString()));

        iconDetailMap.put(Entity.TASK, new IconDetail(22, 104, 193, 
                entityLabels.get(Entity.TASK.getEntityName()).getValue(INITIALS).getValue().toString())); 

        iconDetailMap.put(Entity.TEST_SUITE, new IconDetail(39, 23, 130, 
                entityLabels.get(Entity.TEST_SUITE.getEntityName()).getValue(INITIALS).getValue().toString()));
        iconDetailMap.put(Entity.MANUAL_TEST, new IconDetail(0, 171, 243, 
                entityLabels.get(Entity.MANUAL_TEST.getEntityName()).getValue(INITIALS).getValue().toString()));
        iconDetailMap.put(Entity.GHERKIN_TEST, new IconDetail(0, 169, 137, 
                entityLabels.get(Entity.GHERKIN_TEST.getEntityName()).getValue(INITIALS).getValue().toString()));
        iconDetailMap.put(Entity.AUTOMATED_TEST, new IconDetail(186, 71, 226, 
                entityLabels.get(Entity.AUTOMATED_TEST.getEntityName()).getValue(INITIALS).getValue().toString()));

        iconDetailMap.put(Entity.MANUAL_TEST_RUN, new IconDetail(0, 171, 243, 
                entityLabels.get(Entity.MANUAL_TEST_RUN.getEntityName()).getValue(INITIALS).getValue().toString()));
        iconDetailMap.put(Entity.TEST_SUITE_RUN, new IconDetail(0, 171, 243, 
                entityLabels.get(Entity.TEST_SUITE_RUN.getEntityName()).getValue(INITIALS).getValue().toString()));

        iconDetailMap.put(Entity.COMMENT, new IconDetail(253, 225, 89, 
                entityLabels.get(Entity.COMMENT.getEntityName()).getValue(INITIALS).getValue().toString()));
    }

    private ImageData loadImageData(Entity entity, int iconSize, int fontSize) {
        IconDetail iconDetail = iconDetailMap.containsKey(entity) ? iconDetailMap.get(entity) : undefinedIconDetail;
        Display display = Display.getDefault();
        Image img = new Image(display, iconSize, iconSize);
        setUpGraphics(img, display, iconDetail, iconSize, fontSize);
        ImageData imgData = img.getImageData();
        img.dispose();
        return imgData;
    }
    
    public Image getImageForEditorPart(Entity entity, int iconSize, int fontSize) {
        IconDetail iconDetail = iconDetailMap.containsKey(entity) ? iconDetailMap.get(entity) : undefinedIconDetail;
        Display display = Display.getDefault();
        Color background = iconDetail.getColor();
        PaletteData palette = new PaletteData(new RGB[] {
                background.getRGB(), // pixel 0 = black
                display.getSystemColor(SWT.COLOR_WHITE).getRGB(), // pixel 1 = white
        });
        ImageData imageData = new ImageData(iconSize, iconSize, 1, palette);
        imageData.transparentPixel = 1; // set the transparent color to white
        Image image = new Image( display, imageData);
        setUpGraphics(image, display, iconDetail, iconSize, fontSize);
        imageData = image.getImageData();
        return image;
    }
    
    private void setUpGraphics(Image image, Display display, IconDetail iconDetail, int iconSize, int fontSize) {
        GC gc = new GC(image);
        gc.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
        gc.fillRectangle(0, 0, iconSize, iconSize); // fill the whole image with white
        gc.setAntialias(SWT.ON);
        gc.setBackground(iconDetail.getColor());
        gc.fillOval(0, 0, iconSize, iconSize); // fill an oval in the middle of the picture with the background color
           
        gc.setForeground(fontColor);
        gc.setFont(new Font(display, "Arial", fontSize, SWT.BOLD));
        
        Point p = gc.stringExtent(iconDetail.getDisplayLabelText());
        int fontX = (iconSize  - p.x + 1) / 2;
        int fontY = (iconSize - p.y) / 2;
        
        gc.drawString(iconDetail.getDisplayLabelText(), fontX, fontY, true);
        gc.dispose();
    }
    
    private ImageData overlayActiveImage(ImageData imgData, int iconSize) {
        Image img = new Image(Display.getDefault(), imgData);
        GC gc = new GC(img);

        int xpercent = 60 * iconSize / 100;
        int ypercent = 60 * iconSize / 100;

        gc.drawImage(activeImg,
                0, 0, activeImg.getBounds().width, activeImg.getBounds().height,
                xpercent, ypercent, iconSize - xpercent, iconSize - ypercent);

        return img.getImageData();
    }

    public Image getImageIcon(Entity entity, int iconSize, int fontSize) {
        return getImageIcon(entity, iconSize, fontSize, false);
    }

    public Image getImageIcon(Entity entity, int iconSize, int fontSize, boolean isActive) {
        if (!imageDataCache.containsKey(iconSize)) {
            //create a map for this size
            Map<Entity, ImageData> imageMap = new HashMap<>();
            imageMap.put(entity, loadImageData(entity, iconSize, fontSize));
            Map<Integer, Map<Entity, ImageData>> fontSizeMap = new HashMap<>();
            fontSizeMap.put(fontSize, imageMap);
            imageDataCache.put(iconSize, fontSizeMap);
        } else if (!imageDataCache.get(iconSize).containsKey(fontSize)) {
            //size map exists but fontSize map does not
            Map<Entity, ImageData> imageMap = new HashMap<>();
            imageMap.put(entity, loadImageData(entity, iconSize, fontSize));
            imageDataCache.get(iconSize).put(fontSize, imageMap);
        } else if (!imageDataCache.get(iconSize).get(fontSize).containsKey(entity)) {
            //size map exists, font map exists but entity icon does not
            imageDataCache.get(iconSize).get(fontSize).put(entity, loadImageData(entity, iconSize, fontSize));
        }

        ImageData imageData = imageDataCache.get(iconSize).get(fontSize).get(entity);

        if (isActive) {
            imageData = overlayActiveImage(imageData, iconSize);
        }
          
        return new Image(Display.getDefault(), imageData, imageData.getTransparencyMask());
    }

}
