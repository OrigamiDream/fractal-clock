package av.is.fractalclock;

import avis.juikit.Juikit;

import javax.swing.*;
import java.awt.*;
import java.util.AbstractMap;
import java.util.Map;

public class FractalClock {

    // Keys
    public static final int SECONDS = 0;

    // Constants | Configuration
    public static final int MAX_FRACTAL_DEPTH = 15;
    public static final int SECONDS_PER_MINUTE = 6000;
    public static final int CLOCK_CASE_RADIUS = 200;
    public static final int SECOND_HAND_LENGTH = 170;
    public static final int INDICATOR_DISTANCE = 175;
    public static final int DIVISION_DISTANCE = 190;

    private static Map.Entry<Integer, Integer> createHand(int time, int radius) {
        double angle = 2 * Math.PI * time / SECONDS_PER_MINUTE;
        return new AbstractMap.SimpleEntry<>((int) (Math.cos(angle) * radius), (int) (Math.sin(angle) * radius));
    }

    private static void drawFractal(int previousGradient, int seconds, int minutes, int centerX, int centerY, Graphics graphics, int depth, double previousLength) {
        previousLength *= 0.7;

        Map.Entry<Integer, Integer> minuteHand = createHand(previousGradient + minutes - 1500, (int) previousLength);
        Map.Entry<Integer, Integer> secondHand = createHand(previousGradient + seconds - 1500, (int) previousLength);

        if(depth > 0) {
            int gradient = (255 + 255) / MAX_FRACTAL_DEPTH;
            int blueGradient = 255 / MAX_FRACTAL_DEPTH;

            graphics.setColor(new Color(Math.min(255, gradient * depth), Math.max(0, 255 - (gradient * depth)), blueGradient * depth, 100));
        }

        graphics.drawLine(centerX, centerY, centerX + secondHand.getKey(), centerY + secondHand.getValue());
        graphics.drawLine(centerX, centerY, centerX + minuteHand.getKey(), centerY + minuteHand.getValue());

        if(depth + 1 > MAX_FRACTAL_DEPTH) {
            return;
        }

        drawFractal(previousGradient + minutes, seconds, minutes, centerX + minuteHand.getKey(), centerY + minuteHand.getValue(), graphics, depth + 1, previousLength);
        drawFractal(previousGradient + seconds, seconds, minutes, centerX + secondHand.getKey(), centerY + secondHand.getValue(), graphics, depth + 1, previousLength);
    }

    public static void main(String[] args) {
        Juikit jk = Juikit.createFrame()
                .size(1000, 1000)
                .centerAlign()
                .background(Color.BLACK)
                .closeOperation(WindowConstants.EXIT_ON_CLOSE)
                .title("Fractal Clock")

                .data(SECONDS, 0)

                .resizable(true)
                .repaintInterval(10L)
                .painter((juikit, graphics) -> {
                    int centerX = juikit.width() / 2;
                    int centerY = juikit.height() / 2;

                    graphics.setColor(Color.WHITE);

                    int seconds = juikit.data(SECONDS, int.class);

                    drawFractal(0, seconds, seconds / 60, centerX, centerY, graphics, 0, SECOND_HAND_LENGTH);

                    graphics.setColor(Color.WHITE);

                    for(int i = 0; i < 12; i++) {
                        double angle = 2 * Math.PI * (i - 50) / 12;
                        int x = (int) (Math.cos(angle) * INDICATOR_DISTANCE + centerX);
                        int y = (int) (Math.sin(angle) * INDICATOR_DISTANCE + centerY);

                        graphics.drawString(String.valueOf(i + 1), x - 5, y + 5);
                    }

                    for(int i = 0; i < 60; i++) {
                        double angle = 2 * Math.PI * i / 60;
                        double rotX = Math.cos(angle);
                        double rotY = Math.sin(angle);

                        graphics.drawLine((int) (rotX * DIVISION_DISTANCE + centerX), (int) (rotY * DIVISION_DISTANCE + centerY),
                                          (int) (rotX * CLOCK_CASE_RADIUS + centerX), (int) (rotY * CLOCK_CASE_RADIUS + centerY));
                    }
                })
                .visibility(true);

        new Thread(() -> {
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            while(true) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                int seconds = jk.data(SECONDS);
                if(seconds + 1 == SECONDS_PER_MINUTE * SECONDS_PER_MINUTE) {
                    seconds = 0;
                }
                jk.data(SECONDS, seconds + 1);
            }
        }).start();
    }

}
