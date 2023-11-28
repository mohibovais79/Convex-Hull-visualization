import javax.swing.*;
import java.awt.*;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class DrawTwoLinesWithPoints extends JFrame {

    private DrawingPanel drawingPanel;
    private Line[] lines;
    private int lineCount;
    private JLabel intersectionLabel;

    public DrawTwoLinesWithPoints() {
        setTitle("Line Intersection");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 400);

        lines = new Line[2];
        drawingPanel = new DrawingPanel();
        drawingPanel.setBackground(Color.cyan);

        intersectionLabel = new JLabel();
        intersectionLabel.setForeground(Color.RED);

        JPanel buttonPanel = new JPanel();
        JButton slopeButton = new JButton("Slope");
        JButton ccwButton = new JButton("CCW");

        slopeButton.addActionListener(e -> calculateSlope());

        ccwButton.addActionListener(e -> checkCCW());

        buttonPanel.add(slopeButton);
        buttonPanel.add(ccwButton);

        drawingPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (lineCount < 2) {
                    lines[lineCount] = new Line(e.getX(), e.getY(), e.getX(), e.getY());
                    drawingPanel.repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (lineCount < 2) {
                    Line currentLine = lines[lineCount];
                    currentLine.setEndX(e.getX());
                    currentLine.setEndY(e.getY());
                    lineCount++;
                    drawingPanel.repaint();
                }
            }
        });

        drawingPanel.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (lineCount > 0 && lineCount <= 2) {
                    Line currentLine = lines[lineCount - 1];
                    currentLine.setEndX(e.getX());
                    currentLine.setEndY(e.getY());
                    drawingPanel.repaint();
                }
            }
        });

        add(buttonPanel, BorderLayout.NORTH);
        add(drawingPanel, BorderLayout.CENTER);
        add(intersectionLabel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private class DrawingPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            for (int i = 0; i < lineCount; i++) {
                Line line = lines[i];
                g.drawLine(line.getStartX(), line.getStartY(), line.getEndX(), line.getEndY());

                // Display starting and ending points
                g.setColor(Color.BLUE);
                g.drawString("(" + line.getStartX() + ", " + line.getStartY() + ")", line.getStartX(), line.getStartY());
                g.drawString("(" + line.getEndX() + ", " + line.getEndY() + ")", line.getEndX(), line.getEndY());
            }
        }
    }

    private static class Line {
        private int startX, startY, endX, endY;

        public Line(int startX, int startY, int endX, int endY) {
            this.startX = startX;
            this.startY = startY;
            this.endX = endX;
            this.endY = endY;
        }

        public int getStartX() {
            return startX;
        }

        public int getStartY() {
            return startY;
        }

        public int getEndX() {
            return endX;
        }

        public int getEndY() {
            return endY;
        }

        public void setEndX(int endX) {
            this.endX = endX;
        }

        public void setEndY(int endY) {
            this.endY = endY;
        }

        public double calculateSlope() {
            return (endY - startY) / (double)(endX - startX);
        }
    }

    private void calculateSlope() {
        if (lineCount == 2) {
            Line line1 = lines[0];
            Line line2 = lines[1];

            double slope1 = line1.calculateSlope();
            double slope2 = line2.calculateSlope();

            String intersectionResult = "Lines are ";
            if (slope1 == slope2) {
                intersectionResult += "parallel no intersection";
            } else {
                intersectionResult += "not parallel they intersect";
            }

            intersectionLabel.setText(intersectionResult);
        } else {
            intersectionLabel.setText("Draw two lines first.");
        }
    }

    private void checkCCW() {
        if (lineCount == 2) {
            Line line1 = lines[0];
            Line line2 = lines[1];

            double crossProduct = (line2.getEndY() - line2.getStartY()) * (line1.getEndX() - line1.getStartX())
                    - (line2.getEndX() - line2.getStartX()) * (line1.getEndY() - line1.getStartY());

            String ccwResult = "Lines are ";
            if (crossProduct > 0) {
                ccwResult += "in a counter-clockwise direction.";
            } else if (crossProduct < 0) {
                ccwResult += "in a clockwise direction.";
            } else {
                ccwResult += "collinear.";
            }

            intersectionLabel.setText(ccwResult);
        } else {
            intersectionLabel.setText("Draw two lines first.");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(DrawTwoLinesWithPoints::new);
    }
}
///end
