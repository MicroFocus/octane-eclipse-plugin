package com.hpe.octane.ideplugins.eclipse.ui.util.icon;


import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class CircularImage { 

public static void main(String [] args) { 
final Display display = new Display(); 
final Shell shell = new Shell(display); 
// Set the background of the shell to red so you can see your icon 
shell.setBackground(display.getSystemColor(SWT.COLOR_RED)); 

// Create a 100 x 100 image data with a 2-color (i.e. depth 1) palette 
Color black = display.getSystemColor(SWT.COLOR_BLACK); 
Color white = display.getSystemColor(SWT.COLOR_WHITE); 
PaletteData palette = new PaletteData(new RGB[] { 
black.getRGB(), // pixel 0 = black 
white.getRGB(), // pixel 1 = white 
}); 
ImageData imageData = new ImageData(100, 100, 1, palette); 
imageData.transparentPixel = 1; // set the transparent color to white 

// Create an image from the image data, fill it with white, and draw a 
//black circle on it 
Image image = new Image(display, imageData); 
GC gc = new GC(image); 
gc.setBackground(white); 
gc.setAntialias(SWT.ON);
gc.fillRectangle(0, 0, 100, 100); // fill the whole image with white 
gc.setBackground(black); 
gc.fillOval(0, 0, 100, 100); // draw filled black circle on the image 
gc.dispose(); 

// Get the image data for the drawn image, and use it to create an icon 
imageData = image.getImageData(); 
image.dispose(); 
final Image icon = new Image(display, imageData, 
imageData.getTransparencyMask()); 

// Draw the "circular" image on the shell in its paint event 
shell.addPaintListener(new PaintListener() { 
public void paintControl(PaintEvent event) { 
event.gc.drawImage(icon, 10, 10); 
} 
}); 
shell.setBounds(10, 10, 200, 200); 
shell.open (); 
while (!shell.isDisposed()) { 
if (!display.readAndDispatch()) display.sleep(); 
} 
icon.dispose(); 
display.dispose(); 
} 
} 
