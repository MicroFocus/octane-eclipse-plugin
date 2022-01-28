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
package com.hpe.octane.ideplugins.eclipse.ui.util.resource;

import java.io.File;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import com.hpe.octane.ideplugins.eclipse.Activator;

/**
 * Can be used as constants <br>
 * Fetch images for the plugin or for the test SWT shells used for debugging
 * Enum values are generated, please use main method of this class, don't modify by hand
 */
public enum ImageResources {
    //generated, please use main method of this class, don't modify by hand
    
    //@formatter:off
    ACTIVEITEM("activeitem.png"),
    ADD("add.png"),
    BROWSER_16X16("browser-16x16.png"),
    COMMENTS_16X16("comments-16x16.png"),
    DISMISS("dismiss.gif"),
    DOWNLOAD("download.png"),
    DROP_DOWN("drop-down.png"),
    FAVICON("favicon.png"),
    FIELDS_OFF("fields-off.png"),
    FIELDS_ON("fields-on.png"),
    INFO("info.png"),
    MICROFOCUS_LOGO("microfocus-logo.png"),
    MYWORK("mywork.png"),
    OCTANE_ICON_13X13("octane-icon-13x13.png"),
    OCTANE_ICON_RED_13X13("octane-icon-red-13x13.png"),
    OCTANE_LOGO("octane-logo.png"),
    OCTANE_PRELOADER("octane_preloader.gif"),
    OCTANE_REMOVE("octane_remove.png"),
    PLACEHOLDER("placeholder.png"),
    REFRESH_16X16("refresh-16x16.png"),
    S_NO_ITEMS_TO_DISPLAY("s-no-items-to-display.png"),
    S_ROCKET("s-rocket.png"),
    SEARCH("search.png"),
    START_TIMER_16X16("startTimer-16x16.png"),
    STOP_TIMER_16X16("stopTimer-16x16.png");
    //@formatter:on

    private static final String PATH_PREFIX = "icons/";
    private String imgName;

    private ImageResources(String imgName) {
        this.imgName = imgName;
    }

    public Image getImage() {
        // For the Eclipse plugin
        Image img = ResourceManager.getPluginImage(Activator.PLUGIN_ID, PATH_PREFIX + imgName);
        if (img != null) {
            return img;
        }

        // For SWT Debugging
        String path = System.getProperty("user.dir") + "/" + PATH_PREFIX + imgName;
        try {
            img = new Image(Display.getCurrent(), path);
        } catch (Exception ignored) {
        }
        if (img != null) {
            return img;
        }

        // Placeholder, for window builder
        return createPlaceholderImage();
    }

    public String getPluginPath() {
        return PATH_PREFIX + imgName;
    }

    // Poor mans generator
    // Run this if you're lazy
    public static void main(String[] args) {
        String imgPath = System.getProperty("user.dir") + "/icons";
        File dir = new File(imgPath);
        File[] filesList = dir.listFiles();

        String enumValuesString = Arrays.stream(filesList)
                .filter(file -> file.isFile())
                .map(file -> fileNameToEnumName(file.getName()))
                .collect(Collectors.joining("," + System.getProperty("line.separator")));

        System.out.println("//@formatter:off");
        System.out.println(enumValuesString + ";");
        System.out.println("//@formatter:on");
    }

    private static String fileNameToEnumName(String fileName) {
        String enumName = fileName;
        enumName = enumName.replaceAll("-", "_");
        if (enumName.indexOf(".") > 0) {
            enumName = enumName.substring(0, enumName.lastIndexOf("."));
        }
        enumName = camelCaseToUnderscore(enumName);
        enumName = enumName.toUpperCase();
        return enumName + "(\"" + fileName + "\")";
    }

    private static String camelCaseToUnderscore(String str) {
        String regex = "([a-z])([A-Z]+)";
        String replacement = "$1_$2";
        return (str.replaceAll(regex, replacement).toLowerCase());
    }

    private static Image createPlaceholderImage() {
        Image image = new Image(Display.getDefault(), 16, 16);
        GC gc = new GC(image);
        gc.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_YELLOW));
        gc.fillOval(0, 0, 16, 16);
        gc.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_DARK_GREEN));
        gc.drawLine(0, 0, 16, 16);
        gc.drawLine(16, 0, 0, 16);
        gc.dispose();
        return image;
    }

}
