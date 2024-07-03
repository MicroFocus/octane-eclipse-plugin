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
package com.hpe.octane.ideplugins.eclipse.ui.snake;

import java.security.SecureRandom;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;

import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;

import com.hpe.octane.ideplugins.eclipse.Activator;
import com.hpe.octane.ideplugins.eclipse.ui.util.resource.SWTResourceManager;

/**
 * SWT Snake game
 */
public class SnakeGameCanvas extends Canvas {

    private static int verticalPosCount = 10;
    private static int horizontalPosCount = 20;

    private Color backgroundColor = createColor(COLOR_OCTANE_BLUE);

    private static final int COLOR_OCTANE_BLUE = -1;
    private static final int COLOR_OCTANE_GRAY = -2;

    private static final FontData defaultFontData = Display.getCurrent().getSystemFont().getFontData()[0];

    public enum GameState {
        NOT_STARTED, RUNNING, PAUSED, OVER, WON;
    }

    private GameState gameState = GameState.NOT_STARTED;

    // state of the game
    private static class SpritePos {
        public int x; // 0 <= x < verticalPosCount
        public int y; // 0 <= x < horizontalPosCount
        public SpriteDirection dir = SpriteDirection.RIGHT;

        public SpritePos(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public SpritePos(int x, int y, SpriteDirection dir) {
            this.x = x;
            this.y = y;
            this.dir = dir;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + x;
            result = prime * result + y;
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            SpritePos other = (SpritePos) obj;
            if (x != other.x)
                return false;
            if (y != other.y)
                return false;
            return true;
        }
    }

    private enum SpriteDirection {
        UP, DOWN, LEFT, RIGHT;
    }

    private static final int INIT_SIZE = 3;

    // Represents the body of the snake, where the first element is the head
    private List<SpritePos> snakeBody = new ArrayList<>();
    private Deque<SpriteDirection> snakeDirectionQueue = new ArrayDeque<>(4);
    private SpritePos applePos = null;

    private static final int INIT_SPEED = 200;
    private int speed = INIT_SPEED;

    private SecureRandom secureRandom = new SecureRandom();

    private Timer gameLoopTimer = new Timer();
    private TimerTask gameLoopTask;

    private void restartGameLoop() {
        stopGameLoop();
        startGameLoop();
    }

    private void startGameLoop() {
        if (gameLoopTask != null) {
            gameLoopTask.cancel();
        }
        gameLoopTask = new TimerTask() {
            @Override
            public void run() {
                Display.getDefault().syncExec(() -> {
                    if (GameState.RUNNING.equals(gameState)) {
                        gameState = GameState.RUNNING;
                        moveSnake();
                        if (!SnakeGameCanvas.this.isDisposed()) {
                            redraw();
                        }
                    }
                });
            }
        };
        gameLoopTimer.schedule(gameLoopTask, 1, speed);
    }

    private static final Color createColor(int colorCode) {
        switch (colorCode) {
            case -1:
                return new Color(Display.getDefault(), 0, 121, 239);
            case -2:
                return new Color(Display.getDefault(), 133, 142, 132);
            default:
                return SWTResourceManager.getColor(colorCode);
        }
    }

    private void stopGameLoop() {
        if (gameLoopTask != null) {
            gameLoopTask.cancel();
        }
        if (gameLoopTimer != null) {
            gameLoopTimer.purge();
        }
    }

    public SnakeGameCanvas(Composite parent) {
        super(parent, SWT.NO_BACKGROUND | SWT.NO_REDRAW_RESIZE |
                SWT.NO_MERGE_PAINTS);
        // super(parent, SWT.NONE);
        addListener(SWT.Paint, event -> doPainting(event));
        addListener(SWT.KeyDown, event -> onKeyDown(event));

        addListener(SWT.MouseDown, event -> {
            forceFocus();

            // if the game is not started init the snake
            if (GameState.NOT_STARTED.equals(gameState) || GameState.OVER.equals(gameState)) {
                initGame();
                gameState = GameState.RUNNING;
                startGameLoop();
            } else if (GameState.PAUSED.equals(gameState)) {
                gameState = GameState.RUNNING;
                startGameLoop();
                redraw();
            } else if (GameState.RUNNING.equals(gameState)) {
                gameState = GameState.PAUSED;
                stopGameLoop();
                redraw();
            }
        });

    }

    public void pause() {
        if (gameState == GameState.RUNNING) {
            gameState = GameState.PAUSED;
            stopGameLoop();
            redraw();
        }
    }

    public GameState getGameState() {
        return gameState;
    }

    private void initGame() {
        speed = INIT_SPEED;
        snakeBody.clear();

        backgroundColor = createColor(COLOR_OCTANE_BLUE);

        int initX = horizontalPosCount / 2 - INIT_SIZE;
        int initY = verticalPosCount / 2;

        // start snake horizontally at the middle
        for (int i = 0; i < INIT_SIZE; i++) {
            snakeBody.add(new SpritePos(initX - i, initY));
        }

        snakeDirectionQueue.clear();
        snakeDirectionQueue.push(SpriteDirection.RIGHT);
        placeRandomApple();

        startGameLoop();
    }

    /**
     * Move snake in the current direction
     */
    private void moveSnake() {

        SpriteDirection snakeDirection = snakeDirectionQueue.size() == 1 ? snakeDirectionQueue.peek() : snakeDirectionQueue.poll();

        // Move the head in the current direction
        SpritePos head = snakeBody.get(0);
        int newX = head.x;
        int newY = head.y;
        switch (snakeDirection) {
            case UP:
                newY -= 1;
                break;
            case DOWN:
                newY += 1;
                break;
            case LEFT:
                newX -= 1;
                break;
            case RIGHT:
                newX += 1;
                break;
        }

        // don't move if it's off the board
        if (newX < 0) {
            newX = horizontalPosCount - 1;
        } else if (newX >= horizontalPosCount) {
            newX = 0;
        }
        if (newY < 0) {
            newY = verticalPosCount - 1;
        } else if (newY >= verticalPosCount) {
            newY = 0;
        }

        SpritePos newHead = new SpritePos(newX, newY);
        newHead.dir = snakeDirection;
        // Check if head is on the apple
        if (newHead.equals(applePos)) {
            snakeBody.add(0, new SpritePos(newX, newY, snakeDirection));
            placeRandomApple();
            // get new random background color
            setRandomBackgroundColor();
            speed = (speed > 100) ? (speed - 5) : 100;
            restartGameLoop();
        } else {
            // check in new head collides with the snake body
            for (int i = 1; i < snakeBody.size(); i++) {
                SpritePos curr = snakeBody.get(i);
                if (curr.equals(newHead)) {
                    stopGameLoop();
                    gameState = GameState.OVER;
                    backgroundColor = createColor(SWT.COLOR_RED);
                    return;
                }
            }
        }

        // Shift the body onto the top of the head
        for (int i = snakeBody.size() - 1; i > 0; i--) {
            SpritePos curr = snakeBody.get(i);
            SpritePos prev = snakeBody.get(i - 1);
            curr.x = prev.x;
            curr.y = prev.y;
            curr.dir = prev.dir;
            snakeBody.set(i, curr);
        }

        // create a new head in the new direction
        snakeBody.set(0, newHead);
    }

    /**
     * This sets the game state to WON if there is not more space to put an
     * apple
     */
    private void placeRandomApple() {
        Set<SpritePos> possiblePositions = new HashSet<>();
        for (int x = 0; x < horizontalPosCount; x++) {
            for (int y = 0; y < verticalPosCount; y++) {
                SpritePos pos = new SpritePos(x, y);
                if (!snakeBody.contains(pos)) {
                    possiblePositions.add(pos);
                }
            }
        }

        if (possiblePositions.size() <= 1) {
            applePos = null;
            gameState = GameState.WON; // nice
            backgroundColor = createColor(COLOR_OCTANE_BLUE);
            return;
        }

        // Pick a random element of the set
        int index = secureRandom.nextInt(possiblePositions.size());
        Iterator<SpritePos> iter = possiblePositions.iterator();
        for (int i = 0; i < index; i++) {
            iter.next();
        }
        applePos = iter.next();
    }

    private void doPainting(Event e) {
        int width = getBounds().width;
        int height = getBounds().height;
        Image buffer = new Image(Display.getCurrent(), width, height);
        GC g = new GC(buffer);

        Color colorBlack = createColor(SWT.COLOR_BLACK);
        Color colorWhite = createColor(SWT.COLOR_WHITE);

        // Minimum padding
        int padding = 50;

        // Screen size
        int screenWidth = width - padding; // mandatory padding
        int screenHeight = height - padding; // mandatory padding

        // determine sprite size from current window size
        int spriteSizeWidth = screenWidth / horizontalPosCount;
        int spriteSizeHeight = screenHeight / verticalPosCount;
        int spriteSize = spriteSizeHeight > spriteSizeWidth ? spriteSizeWidth : spriteSizeHeight;

        // view start
        int x1 = (screenWidth - (spriteSize * horizontalPosCount)) / 2 + (padding / 2);
        int y1 = (screenHeight - (spriteSize * verticalPosCount)) / 2 + (padding / 2);

        // Background color of the border of the game
        g.setBackground(backgroundColor);
        g.fillRectangle(0, 0, width, height);

        // Draw the bounds of the game, the board on which the snake moves
        g.setForeground(colorBlack);
        g.drawRectangle(x1 - 2, y1 - 2, horizontalPosCount * spriteSize + 2, verticalPosCount * spriteSize + 2);
        g.drawRectangle(x1 - 1, y1 - 1, horizontalPosCount * spriteSize + 2, verticalPosCount * spriteSize + 2);

        // Set the board to a while bg
        g.setBackground(colorWhite);
        g.fillRectangle(x1, y1, horizontalPosCount * spriteSize, verticalPosCount * spriteSize);

        // Now draw something on the board depending on the game state
        if (GameState.NOT_STARTED.equals(gameState)) {
            drawGameString(g, x1, y1, horizontalPosCount * spriteSize, verticalPosCount * spriteSize,
                    createColor(COLOR_OCTANE_BLUE), "OpenText™️ Core Software Delivery Platform SNAKE", "Click to start, space/click to pause", "");

        } else if (GameState.PAUSED.equals(gameState)) {
            drawGameString(g, x1, y1, horizontalPosCount * spriteSize, verticalPosCount * spriteSize,
                    createColor(SWT.COLOR_YELLOW), "PAUSED", "Score: " + (snakeBody.size() - INIT_SIZE), "Click to start, space/click to pause");

        } else if (GameState.RUNNING.equals(gameState)) {

            // Draw the snake according to the game state
            // draw apple
            if (applePos != null) {
                Image sprite = getSprite(SpriteDirection.UP, true);

                g.drawImage(
                        sprite,
                        0,
                        0,
                        sprite.getBounds().width, sprite.getBounds().height,
                        x1 + spriteSize * applePos.x,
                        y1 + spriteSize * applePos.y,
                        spriteSize,
                        spriteSize);

                if (!sprite.isDisposed()) {
                    sprite.dispose();
                }
            }

            // draw the snake at it's current position
            for (int i = 0; i < snakeBody.size(); i++) {
                SpritePos pos = snakeBody.get(i);
                SpritePos prevPos = i > 0 ? snakeBody.get(i - 1) : null;
                Image sprite;
                if (i == 0) {
                    sprite = getSprite(pos.dir, true);
                } else if (prevPos != null && !pos.dir.equals(prevPos.dir)) {
                    sprite = getSprite(null, false);
                } else {
                    sprite = getSprite(pos.dir, false);
                }
                if (!pos.equals(prevPos)) {
                    g.drawImage(
                            sprite,
                            0,
                            0,
                            sprite.getBounds().width, sprite.getBounds().height,
                            x1 + spriteSize * pos.x, y1 + spriteSize * pos.y,
                            spriteSize,
                            spriteSize);
                }
                if (sprite != null && !sprite.isDisposed()) {
                    sprite.dispose();
                }
            }

        } else if (GameState.OVER.equals(gameState)) {
            drawGameString(g, x1, y1, horizontalPosCount * spriteSize, verticalPosCount * spriteSize,
                    createColor(SWT.COLOR_RED), "GAME OVER", "Score: " + (snakeBody.size() - INIT_SIZE),
                    "Click to restart, or maybe get back to work...");

        } else if (GameState.WON.equals(gameState)) {
            drawGameString(g, x1, y1, horizontalPosCount * spriteSize, verticalPosCount * spriteSize,
                    createColor(SWT.COLOR_RED), "GAME WON", "Wow, that's impressive!", "You should really get back to work now...");
        }

        if (!e.gc.isDisposed()) {
            e.gc.drawImage(buffer, 0, 0, width, height, 0, 0, width, height);
            e.gc.dispose();
        }
        g.dispose();
        colorBlack.dispose();
        colorWhite.dispose();
    }

    private void onKeyDown(Event e) {
        int key = e.keyCode;

        if (GameState.RUNNING.equals(gameState)) {
            if (key == SWT.ARROW_UP) {
                changeDirection(SpriteDirection.UP, SpriteDirection.DOWN);
            }
            if (key == SWT.ARROW_LEFT) {
                changeDirection(SpriteDirection.LEFT, SpriteDirection.RIGHT);
            }
            if (key == SWT.ARROW_DOWN) {
                changeDirection(SpriteDirection.DOWN, SpriteDirection.UP);
            }
            if (key == SWT.ARROW_RIGHT) {
                changeDirection(SpriteDirection.RIGHT, SpriteDirection.LEFT);
            }
        }
        if (key == SWT.SPACE) {
            togglePaused();
        }
    }

    private void changeDirection(SpriteDirection direction, SpriteDirection oppositeDir) {
        if (!snakeDirectionQueue.peek().equals(oppositeDir)) {
            snakeDirectionQueue.clear();
            snakeDirectionQueue.add(direction);
            // callGameLoop(); DISABLED
        }
    }

    private void togglePaused() {
        if (GameState.PAUSED.equals(gameState)) {
            gameState = GameState.RUNNING;
            startGameLoop();
        } else if (GameState.RUNNING.equals(gameState)) {
            gameState = GameState.PAUSED;
            stopGameLoop();
        }
        redraw();
    }

    /**
     * Set random background color
     */
    public void setRandomBackgroundColor() {
        if (backgroundColor != null && !backgroundColor.isDisposed()) {
            backgroundColor.dispose();
        }
        int red = secureRandom.nextInt(256);
        int green = secureRandom.nextInt(256);
        int blue = secureRandom.nextInt(256);
        backgroundColor = new Color(Display.getCurrent(), red, green, blue);
    }

    private static final Map<String, ImageData> spriteDataMap = new HashMap<>();

    @SuppressWarnings("deprecation")
	private Image getSprite(SpriteDirection direction, boolean red) {

        String spriteName = "icons/snake/octane";
        if (red) {
            spriteName += "-red";
        }
        if (direction != null) {
            spriteName += "-" + direction.name().toLowerCase();
        } else {
            spriteName += "-empty";
        }
        spriteName += ".png";

        ImageData imageData = null;

        if (!spriteDataMap.containsKey(spriteName)) {
            ImageDescriptor img = Activator.getImageDescriptor(spriteName);
            spriteDataMap.put(spriteName, img.getImageData());
            imageData = img.getImageData();
        } else {
            imageData = spriteDataMap.get(spriteName);
        }

        if (imageData == null) {
            Color color = createColor(COLOR_OCTANE_BLUE);
            Color black = createColor(SWT.COLOR_BLACK);
            Image img = new Image(Display.getDefault(), new Rectangle(0, 0, 30, 30));
            GC gc = new GC(img);
            gc.setBackground(color);
            gc.setForeground(black);
            gc.fillRectangle(0, 0, 30, 30);
            gc.drawRectangle(0, 0, 29, 29);
            gc.dispose();
            color.dispose();
            black.dispose();
            return img;
        }

        return new Image(Display.getCurrent(), imageData);
    }

    private void drawGameString(GC g, int x, int y, int width, int height, Color titleColor, String title, String subTitle, String subsubTitle) {
        int titleFontSize = width * 5 / 100;
        int bottomFontSize = width * 2 / 100;
        titleFontSize = titleFontSize < 0 ? 0 : titleFontSize;
        bottomFontSize = bottomFontSize < 0 ? 0 : bottomFontSize;

        Color colorColorGray = createColor(COLOR_OCTANE_GRAY);

        FontDescriptor decriptor = FontDescriptor.createFrom(defaultFontData);
        decriptor = decriptor.setHeight(titleFontSize);
        decriptor = decriptor.setStyle(SWT.BOLD);
        Font titleFont = decriptor.createFont(Display.getCurrent());

        g.setForeground(titleColor);
        g.setFont(titleFont);
        g.drawString(title, x + (width - g.textExtent(title).x) / 2, y + height / 2 - titleFontSize * 2);

        decriptor = decriptor.setHeight(bottomFontSize);
        Font bottomFont = decriptor.createFont(Display.getCurrent());
        g.setForeground(colorColorGray);
        g.setFont(bottomFont);
        g.drawString(subTitle, x + (width - g.textExtent(subTitle).x) / 2, y + height / 2 + bottomFontSize);
        g.drawString(subsubTitle, x + (width - g.textExtent(subsubTitle).x) / 2, y + height / 2 + bottomFontSize * 3);

        titleColor.dispose();
        colorColorGray.dispose();
        titleFont.dispose();
        bottomFont.dispose();
    }

}
