

/*
 * Copyright (C) 2014  James Lawrence.
 *
 *     This file is part of GrimEdi.
 *
 *     GrimEdi is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.sqrt4.grimedi.ui;

import java.awt.*;

public class SplashScreenController {
    private static String loadingText = "Loading...";
    private static float percentage = 0f;
    private static long lastUpdate = System.currentTimeMillis();
    private static int frame;


    public static void setPercentage(float perc) {
        percentage = perc;
    }

    public static void setText(String text) {
        loadingText = text;
    }

    private static void repaint() {
        SplashScreen splash = SplashScreen.getSplashScreen();
        if (splash == null || !splash.isVisible())
            return;

        // Time correction
        long time = System.currentTimeMillis();
        long delta = time - lastUpdate;
        final int fps = 25;
        final int mspf = 1000/fps;
        int framesElapsed = (int) (delta / mspf);
        long calcTime = framesElapsed * mspf;
        lastUpdate += calcTime;
        frame += framesElapsed;

        Graphics graphics = splash.createGraphics();
        graphics.setFont(Font.decode("Helvetica 12 bold"));
        FontMetrics metrics = graphics.getFontMetrics();

        // y offset for text...
        Dimension size = splash.getSize();
        final Insets insets = new Insets(4, 0, 6, 0);
        int y = size.height - metrics.getHeight();

        // Clear previous text and percentage...
        graphics.setColor(Color.BLACK);
        graphics.setClip(0, y - metrics.getHeight(), size.width, size.height);
        graphics.clearRect(0, 0, size.width, size.height);

        // Draw loading text...
        graphics.setColor(Color.WHITE);
        graphics.drawString(loadingText, (size.width - metrics.stringWidth(loadingText)) / 2, y);
        y += insets.top;

        // Draw progress bar...
        graphics.setClip(insets.left, y + insets.top, size.width - insets.right - insets.left, size.height - insets.bottom - insets.top);
        final int xOff = insets.left;
        final int yOff = y;
        final int progWidth = ((int) ((float) size.width * (percentage / 100f))) - (insets.left+insets.right);
        final int progHeight = size.height - y - insets.bottom;
        graphics.setClip(xOff, yOff, progWidth, progHeight);
        graphics.setColor(Color.LIGHT_GRAY);
        if (progWidth > 0) {
            graphics.setColor(Color.WHITE);
            graphics.fillRect(xOff, yOff, progWidth, progHeight);


            final int stripDistance = 20;
            final int stripeWidth = 8;
            int stripeOff = frame % (stripDistance+stripeWidth);
            graphics.setColor(Color.BLACK);

            for (int x1 = -stripeOff; x1 < progWidth; x1 += stripDistance) {
                for (int i = 0; i < stripeWidth; i++, x1++) {
                    int x = x1 + xOff;
                    graphics.drawLine(x, yOff, x + progHeight-1, yOff + progHeight-1);
                }
            }
        }
        splash.update();
    }

    static {
        SplashScreen splash = SplashScreen.getSplashScreen();
        if (splash != null) {
            Thread repaintThread = new Thread() {
                public void run() {
                    SplashScreen splash = SplashScreen.getSplashScreen();
                    while (splash != null && splash.isVisible()) {
                        try {
                            repaint();

                            Thread.sleep(20);
                            splash = SplashScreen.getSplashScreen();
                        } catch (Exception ignore) {
                        }
                    }
                }
            };
            repaintThread.setPriority(1);
            repaintThread.setDaemon(true);
            repaintThread.start();
        }
    }

    public static boolean supported() {
        return SplashScreen.getSplashScreen() != null;
    }
}