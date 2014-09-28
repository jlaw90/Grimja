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
        FontMetrics metrics = graphics.getFontMetrics();

        // y offset for text...
        Dimension size = splash.getSize();
        int y = size.height - metrics.getHeight();

        // Clear previous text and percentage...
        graphics.setColor(Color.BLACK);
        graphics.setClip(0, 0, size.width, size.height);
        graphics.clearRect(0, y - metrics.getHeight(), size.width, size.height - y + metrics.getHeight());

        // Draw progress bar...
        final int border = 3;
        final int border2 = border * 2;
        final int xOff = border;
        final int yOff = y + border;
        final int progWidth = ((int) ((float) size.width * (percentage / 100f))) - border2;
        final int progHeight = size.height - y - border2;
        graphics.setClip(xOff, yOff, progWidth, progHeight);
        graphics.setColor(Color.LIGHT_GRAY);
        if (progWidth > 0) {
            final float bStart = 0.3f;
            final float bEnd = 0.8f;
            final float bDif = (bEnd - bStart) / (float) progHeight;
            for (int y1 = 0; y1 < progHeight; y1++) {
                float b = bStart + (bDif * y1);
                int rgba = (int) (b * 256);
                graphics.setColor(new Color(rgba, rgba, rgba));
                graphics.drawLine(xOff, yOff + y1, xOff+progWidth, yOff + y1);
            }


            final int stripDistance = 20;
            final int stripeWidth = 5;
            int stripeOff = frame % (stripDistance+stripeWidth);
            graphics.setColor(Color.RED);

            for (int x1 = stripeOff; x1 < progWidth; x1 += stripDistance) {
                for (int i = 0; i < stripeWidth; i++, x1++) {
                    int x = x1 + xOff;
                    graphics.drawLine(x, yOff, x + progHeight-1, yOff + progHeight-1);
                }
            }
        }

        graphics.setClip(0, 0, size.width, size.height);
        graphics.setColor(Color.YELLOW);
        graphics.drawString(loadingText, (size.width - metrics.stringWidth(loadingText)) / 2, y);
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