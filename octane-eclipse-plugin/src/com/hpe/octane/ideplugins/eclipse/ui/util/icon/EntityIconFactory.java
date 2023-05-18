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
package com.hpe.octane.ideplugins.eclipse.ui.util.icon;


import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;

import com.hpe.adm.octane.ideplugins.services.EntityLabelService;
import com.hpe.adm.octane.ideplugins.services.connection.ConnectionSettingsProvider;
import com.hpe.adm.octane.ideplugins.services.filtering.Entity;
import com.hpe.octane.ideplugins.eclipse.Activator;
import com.hpe.octane.ideplugins.eclipse.ui.util.resource.ImageResources;

public class EntityIconFactory {
    
    private EntityLabelService entityLabelService;
    
    // cache images by size font and entity
    private final Map<Integer, Map<Integer, Map<Entity, ImageData>>> imageDataCache = new HashMap<>();
    // cache for editor part images - the rectangle images
    private final Map<Integer, Map<Integer, Map<Entity, Image>>> imageCacheForEditorPart = new HashMap<>();
    
    private Color fontColor = new Color(Display.getCurrent(), 255, 255, 255);
    private static Image activeImg = ImageResources.ACTIVEITEM.getImage();
    
    private static EntityIconFactory instance;

    private static final Map<Entity, Color> entityColorMap = new HashMap<>();
    
    static {
        entityColorMap.put(Entity.USER_STORY, new Color(Display.getCurrent(), 255, 176, 0));
        entityColorMap.put(Entity.QUALITY_STORY, new Color(Display.getCurrent(), 51, 193, 128));
        entityColorMap.put(Entity.DEFECT, new Color(Display.getCurrent(), 178, 22, 70));
        entityColorMap.put(Entity.EPIC, new Color(Display.getCurrent(), 116, 37, 173));
        entityColorMap.put(Entity.FEATURE, new Color(Display.getCurrent(), 229, 120, 40));
        entityColorMap.put(Entity.TASK, new Color(Display.getCurrent(), 22, 104, 193));
        entityColorMap.put(Entity.MANUAL_TEST, new Color(Display.getCurrent(), 0, 171, 243));
        entityColorMap.put(Entity.GHERKIN_TEST, new Color(Display.getCurrent(), 0, 169, 137));
        entityColorMap.put(Entity.TEST_SUITE, new Color(Display.getCurrent(), 39, 23, 130));
        entityColorMap.put(Entity.MANUAL_TEST_RUN, new Color(Display.getCurrent(), 0, 171, 243));
        entityColorMap.put(Entity.TEST_SUITE_RUN, new Color(Display.getCurrent(), 0, 171, 243));
        entityColorMap.put(Entity.AUTOMATED_TEST, new Color(Display.getCurrent(), 186, 71, 226));
        entityColorMap.put(Entity.COMMENT, new Color(Display.getCurrent(), 253, 225, 89));
        entityColorMap.put(Entity.REQUIREMENT, new Color(Display.getCurrent(), 11, 142, 172));
        entityColorMap.put(Entity.BDD_SCENARIO, new Color(Display.getCurrent(),117,218, 77));
    }

    private EntityIconFactory() {
        this.entityLabelService = Activator.getInstance(EntityLabelService.class);
        ConnectionSettingsProvider connectionSettingsProvider = Activator.getInstance(ConnectionSettingsProvider.class);
        
        connectionSettingsProvider.addChangeHandler(() -> {
            //imageDataCache.clear();
        });
    }
    
    public static EntityIconFactory getInstance() {
        if(instance == null) {
            instance = new EntityIconFactory();
        }    
        return instance;
    }

    private ImageData loadImageData(Entity entity, int iconSize, int fontSize) {     

        Color color = entityColorMap.get(entity);
        String initials = entityLabelService.getEntityInitials(entity);
        
        Display display = Display.getDefault();
        Image img = new Image(display, iconSize, iconSize);
        setUpGraphics(img, display, initials, color, iconSize, fontSize);
        ImageData imgData = img.getImageData();
        img.dispose();
        return imgData;
    }
    
    private void setUpGraphics(Image image, Display display, String initials, Color color, int iconSize, int fontSize) {
        GC gc = new GC(image);
        gc.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
        gc.fillRectangle(0, 0, iconSize, iconSize); // fill the whole image with white
        gc.setAntialias(SWT.ON);
        gc.setBackground(color);
        gc.fillOval(0, 0, iconSize, iconSize); // fill an oval in the middle of the picture with the background color
           
        gc.setForeground(fontColor);
        gc.setFont(new Font(display, "Arial", fontSize, SWT.BOLD));
        
        Point p = gc.stringExtent(initials);
        int fontX = (iconSize  - p.x + 1) / 2;
        int fontY = (iconSize - p.y) / 2;
        
        gc.drawString(initials, fontX, fontY, true);
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

    private Image loadImageForEditorPart(Entity entity, int iconSize, int fontSize) {
        
        Color color = entityColorMap.get(entity);
        String initials = entityLabelService.getEntityInitials(entity);
        
        Display display = Display.getDefault();   
        Image image = new Image( display, iconSize, iconSize);
        setUpGraphicsForEditorPart(image, display, initials, color, iconSize, fontSize);
        return image;
    }
    
    private void setUpGraphicsForEditorPart(Image image, Display display, String initials, Color color, int iconSize, int fontSize) {
        GC gc = new GC(image);
        gc.setBackground(color);
        gc.fillRectangle(0, 0, iconSize, iconSize);
        gc.setAntialias(SWT.ON);
        
        gc.setForeground(fontColor);
        gc.setFont(new Font(display, "Arial", fontSize, SWT.BOLD));
        
        Point p = gc.stringExtent(initials);
        int fontX = (iconSize  - p.x + 1) / 2;
        int fontY = (iconSize - p.y) / 2;
        
        gc.drawString(initials, fontX, fontY, true);
        gc.dispose();
    }
    
    public void clearImageDataCache() {
    	imageCacheForEditorPart.clear();
    	imageDataCache.clear();
    }
    
    public Image getImageForEditorPart(Entity entity, int iconSize, int fontSize) {
        
        if (!imageCacheForEditorPart.containsKey(iconSize)) {
            
            //create a map for this size
            Map<Entity, Image> imageMap = new HashMap<>();
            imageMap.put(entity, loadImageForEditorPart(entity, iconSize, fontSize));
            
            Map<Integer, Map<Entity, Image>> fontSizeMap = new HashMap<>();
            fontSizeMap.put(fontSize, imageMap);
            
            imageCacheForEditorPart.put(iconSize, fontSizeMap);
            
        } else if (!imageCacheForEditorPart.get(iconSize).containsKey(fontSize)) {
            
            //size map exists but fontSize map does not
            Map<Entity, Image> imageMap = new HashMap<>();
            imageMap.put(entity, loadImageForEditorPart(entity, iconSize, fontSize));
            
            imageCacheForEditorPart.get(iconSize).put(fontSize, imageMap);
        
        } else if (!imageCacheForEditorPart.get(iconSize).get(fontSize).containsKey(entity)) {
        
            //size map exists, font map exists but entity icon does not
            imageCacheForEditorPart.get(iconSize).get(fontSize).put(entity, loadImageForEditorPart(entity, iconSize, fontSize));
        }

        return imageCacheForEditorPart.get(iconSize).get(fontSize).get(entity);    
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