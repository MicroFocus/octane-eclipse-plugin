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

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Display;

import com.hpe.adm.octane.ideplugins.services.filtering.Entity;
import com.hpe.octane.ideplugins.eclipse.ui.util.resource.ImageResources;

public class EntityIconFactory {

    private static final IconDetail undefinedIconDetail = new IconDetail(0, 0, 0, "N/A");

    // map to color and short text
    private final Map<Entity, IconDetail> iconDetailMap = new HashMap<>();

    // cache
    private final Map<Entity, ImageData> imageDataCache = new HashMap<>();

    private int iconHeight = 16;
    private int iconWidth = 16;
    private Color fontColor = new Color(Display.getCurrent(), 255, 255, 255);
    private int fontSize = 11;

    private static Image activeImg = ImageResources.ACTIVEITEM.getImage();

    public EntityIconFactory() {
        init();
    }

    public EntityIconFactory(int iconHeight, int iconWidth, int fontSize, Color fontColor) {
        this.iconHeight = iconHeight;
        this.iconWidth = iconWidth;
        this.fontColor = fontColor;
        this.fontSize = fontSize;
        init();
    }

    public EntityIconFactory(int iconHeight, int iconWidth, int fontSize) {
        this.iconHeight = iconHeight;
        this.iconWidth = iconWidth;
        this.fontSize = fontSize;
        init();
    }

    private void init() {
        iconDetailMap.put(Entity.USER_STORY, new IconDetail(255, 176, 0, "US"));
        iconDetailMap.put(Entity.QUALITY_STORY, new IconDetail(51, 193, 128, "QS"));
        iconDetailMap.put(Entity.DEFECT, new IconDetail(178, 22, 70, "D"));
        iconDetailMap.put(Entity.EPIC, new IconDetail(116, 37, 173, "E"));
        iconDetailMap.put(Entity.FEATURE, new IconDetail(229, 120, 40, "F"));
        iconDetailMap.put(Entity.REQUIREMENT, new IconDetail(11, 142, 172, "R"));

        iconDetailMap.put(Entity.TASK, new IconDetail(22, 104, 193, "T")); 

        iconDetailMap.put(Entity.TEST_SUITE, new IconDetail(39, 23, 130, "TS"));
        iconDetailMap.put(Entity.MANUAL_TEST, new IconDetail(0, 171, 243, "MT"));
        iconDetailMap.put(Entity.GHERKIN_TEST, new IconDetail(0, 169, 137, "GT"));
        iconDetailMap.put(Entity.AUTOMATED_TEST, new IconDetail(186, 71, 226, "AT"));

        iconDetailMap.put(Entity.MANUAL_TEST_RUN, new IconDetail(0, 171, 243, "MR"));
        iconDetailMap.put(Entity.TEST_SUITE_RUN, new IconDetail(0, 171, 243, "SR"));

        iconDetailMap.put(Entity.COMMENT, new IconDetail(253, 225, 89, "C"));
    }

    private void loadImageData(Entity entity) {
        IconDetail iconDetail = iconDetailMap.containsKey(entity) ? iconDetailMap.get(entity) : undefinedIconDetail;
           
        Display display = Display.getDefault();
        Color white = display.getSystemColor(SWT.COLOR_WHITE);
        Image img = new Image(display, iconWidth, iconHeight);
        img.getImageData().transparentPixel = img.getImageData().getPixel(0, 0);
        GC gc = new GC(img);
 
        gc.setBackground(white);
        gc.fillRectangle(0, 0, iconWidth, iconHeight); // fill the whole image with white
        gc.setAntialias(SWT.ON);
        gc.setBackground(iconDetail.getColor());
        gc.fillOval(0, 0, iconWidth, iconHeight);
            
        gc.setForeground(fontColor);
        gc.setFont(new Font(display, "Arial", fontSize, SWT.BOLD));

        int fontX = (iconHeight - gc.textExtent(iconDetail.getDisplayLabelText()).y) / 2;
        int fontY = (iconWidth - gc.textExtent(iconDetail.getDisplayLabelText()).x) / 2;

        gc.drawText(iconDetail.getDisplayLabelText(), fontY, fontX);
        
        imageDataCache.put(entity, img.getImageData());

        gc.dispose();
        img.dispose();
    }
    
    public Image getImageForEditorPart(Entity entity) {
        IconDetail iconDetail = iconDetailMap.containsKey(entity) ? iconDetailMap.get(entity) : undefinedIconDetail;
        
        ImageIcon imageIcon = new ImageIcon();
//        imageIcon.setImage(new Image());
        
        Display display = Display.getDefault();
        Image img = new Image(display, iconWidth, iconHeight);
        GC gc = new GC(img);

//        Color white = display.getSystemColor(SWT.COLOR_WHITE); 
//        gc.setBackground(white);
//        gc.fillRectangle(0, 0, iconWidth, iconHeight); // fill the whole image with white
        gc.setAntialias(SWT.ON);
        gc.setBackground(iconDetail.getColor());
        gc.fillOval(0, 0, iconWidth, iconHeight);
            
        gc.setForeground(fontColor);
        gc.setFont(new Font(display, "Arial", fontSize, SWT.BOLD));

        int fontX = (iconHeight - gc.textExtent(iconDetail.getDisplayLabelText()).y) / 2;
        int fontY = (iconWidth - gc.textExtent(iconDetail.getDisplayLabelText()).x) / 2;

        gc.drawText(iconDetail.getDisplayLabelText(), fontY, fontX, true);
        
        
        return ImageResources.ACTIVEITEM.getImage();
        
//        BufferedImage bImg = new BufferedImage(60, 60, BufferedImage.TYPE_INT_RGB);
//        Graphics2D graphics = bImg.createGraphics();
//        Color color = iconDetail.getColor();
//        graphics.setBackground(new java.awt.Color(color.getRed(), color.getGreen(),
//                color.getBlue()));
//        graphics.fillOval(0, 0, bImg.getWidth(), bImg.getHeight());
//
//        ImageIcon imageIcon = new ImageIcon(bImg);
//        
//        return imageIcon.getImage();
    }

    private ImageData overlayActiveImage(ImageData imgData) {
        Image img = new Image(Display.getDefault(), imgData);
        GC gc = new GC(img);

        int xpercent = 60 * iconWidth / 100;
        int ypercent = 60 * iconWidth / 100;

        gc.drawImage(activeImg,
                0, 0, activeImg.getBounds().width, activeImg.getBounds().height,
                xpercent, ypercent, iconWidth - xpercent, iconWidth - ypercent);

        return img.getImageData();
    }

    public Image getImageIcon(Entity entity) {
        return getImageIcon(entity, false);
    }

    public Image getImageIcon(Entity entity, boolean isActive) {
        if (!imageDataCache.containsKey(entity)) {
            loadImageData(entity);
        }

        ImageData imageData = imageDataCache.get(entity);

        if (isActive) {
            imageData = overlayActiveImage(imageData);
        }
          
        return new Image(Display.getDefault(), imageData, imageData.getTransparencyMask());
    }

}
