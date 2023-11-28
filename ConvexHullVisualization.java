import javax.swing.*;
import java.awt.*;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;

public class ConvexHullVisualization extends JFrame {

    private JPanel convexHullScreen;   //object for panel


    private ArrayList<Point> points = new ArrayList<>(); //all the points on the screen marked in blue
    private ArrayList<Point> convexHullPoints = new ArrayList<>();  //only boundary points included in hull marked with green
//constructor
    public ConvexHullVisualization() {getContentPane().setBackground(Color.BLACK);
        setTitle("Convex Hull Application");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);

        convexHullScreen = createConvexHullScreen();


        convexHullScreen.setBackground(Color.BLACK);

        showConvexHullScreen();

        setVisible(true);

    }
    private JPanel createConvexHullScreen() {
        JPanel mainPanel = new JPanel(new BorderLayout());



        JPanel buttonPanel = new JPanel();

//button setup
        JButton grahamScanButton = new JButton("Graham Scan");
        JButton jarvisMarchButton = new JButton("Jarvis March");
        JButton bruteForceButton = new JButton("Brute Force");
        JButton quickHull = new JButton("Quick Hull");
        JButton KirkPatrick = new JButton("KirkPatrick's Algorithm");
        JButton resetButton = new JButton("Reset Points");
        JButton randomPoints = new JButton("Random Points");
        JLabel executionTime = new JLabel();

        buttonPanel.add(grahamScanButton);
        buttonPanel.add(jarvisMarchButton);
        buttonPanel.add(bruteForceButton);
        buttonPanel.add(quickHull);
        buttonPanel.add(KirkPatrick);
        buttonPanel.add(resetButton);
        buttonPanel.add(randomPoints);

        buttonPanel.add(executionTime);

        DrawingPanel drawingPanel = new DrawingPanel();
        drawingPanel.setBackground(Color.BLACK);

        mainPanel.add(buttonPanel, BorderLayout.NORTH);

        mainPanel.add(drawingPanel, BorderLayout.CENTER);



        randomPoints.addActionListener(e -> {
            int buffer = 20;
            for (int i = 0; i < 100; i++) {
                points.add(new Point((int) (Math.random() * (getWidth() - 2 * buffer) + buffer),
                        (int) (Math.random() * (getHeight() - 2 * buffer) + buffer)));
            }
            drawingPanel.repaint();

        });

        resetButton.addActionListener(e -> {
            points.clear();
            convexHullPoints.clear();
            drawingPanel.repaint();
        });

        quickHull.addActionListener(e -> {
            convexHullPoints.clear();
            if (!(points.size() < 3)) {
                // Convex hull is not possible with less than 3 points


                // Find the points with minimum and maximum x coordinates
                Point minX = Collections.min(points, Comparator.comparing(Point::getX));
                Point maxX = Collections.max(points, Comparator.comparing(Point::getX));

                // Add the points to the convex hull
                convexHullPoints.add(minX);
                convexHullPoints.add(maxX);

                // Recursively find the points on the left and right side of the line formed by minX and maxX
                findHullSet(minX, maxX, points, convexHullPoints, 1);
                findHullSet(minX, maxX, points, convexHullPoints, -1);
            }


        });
        KirkPatrick.addActionListener(e -> {
            points.sort(Comparator.comparing(Point::getX));
            convexHullPoints.clear();

            // Divide the set of points into two halves
            ArrayList<Point> lowerHull = convexHullSubroutine(new ArrayList<>(points.subList(0, points.size() / 2)), false);
            ArrayList<Point> upperHull = convexHullSubroutine(new ArrayList<>(points.subList(points.size() / 2, points.size())), true);

            // Merge the lower and upper hulls
            mergeHulls(lowerHull, upperHull);

            // Redraw the panel
            drawingPanel.repaint();
        });


        grahamScanButton.addActionListener(e -> {
            long startTime = System.nanoTime();
            // Implement Graham Scan algorithm
            // Update convexHullPoints
            convexHullPoints.clear();

            if (!(points.size() < 3)) {
                Point reference = Collections.min(points, Comparator.comparing(Point::getY));
                points.sort((p1, p2) -> {
                    double angle1 = Math.atan2(p1.y - reference.y, p1.x - reference.x);
                    double angle2 = Math.atan2(p2.y - reference.y, p2.x - reference.x);
                    return Double.compare(angle1, angle2);
                });
                Stack<Point> stack = new Stack<>();
                stack.push(points.get(0));
                stack.push(points.get(1));
                for (int i = 2; i < points.size(); i++) {
                    Point top = stack.pop();
                    while (calculateOrientation(stack.peek(), top, points.get(i)) != 2) {
                        top = stack.pop();
                    }
                    stack.push(top);
                    stack.push(points.get(i));
                }

                convexHullPoints.addAll(stack);
                long endTime = System.nanoTime();
                float timeTaken = (float) (endTime - startTime) / 1_000_000;
                executionTime.setText("Time taken by Graham Scan: " + timeTaken + " ms");
                drawingPanel.repaint();


            }

            drawingPanel.repaint();
        });



        bruteForceButton.addActionListener(e -> {
            // correct this
            long startTime = System.nanoTime();
            for (int i = 0; i < points.size(); i++) {
                for (int j = 0; j < points.size(); j++) {
                    if (i != j) {
                        boolean valid = true;
                        for (int k = 0; k < points.size(); k++) {
                            if (k != i && k != j) {
                                if (isLeftTurn(points.get(i), points.get(j), points.get(k))) {
                                    valid = false;
                                    break;
                                }
                            }
                        }
                        if (valid && !convexHullPoints.contains(points.get(i))) {
                            convexHullPoints.add(points.get(i));
                        }
                        if (valid && !convexHullPoints.contains(points.get(j))) {
                            convexHullPoints.add(points.get(j));
                        }
                    }
                }
            }
            long endTime = System.nanoTime();
            float timeTaken = (float) (endTime - startTime) / 1_000_000;
            executionTime.setText("Time taken to compute brute force Hull: " + timeTaken + " ms");
            drawingPanel.repaint();


        });

        jarvisMarchButton.addActionListener(e -> {
            // Implement Jarvis March algorithm
            // Update convexHullPoints
            long startTime = System.nanoTime();
            convexHullPoints.clear();

            if (!(points.size() < 3)) {
                //leftmost point
                int left = 0;
                for (int i = 0; i < points.size(); i++) {
                    if (points.get(i).x < points.get(left).x) {
                        left = i;

                    }
                }
                int p = left, q;
                do {
                    convexHullPoints.add(points.get(p));
                    q = (p + 1) % points.size();
                    for (int i = 0; i < points.size(); i++) {
                        if (calculateOrientation(points.get(p), points.get(i), points.get(q)) == 2) {
                            q = i;
                        }
                    }
                    p = q;
                }


                while (p != left);
                long endTime = System.nanoTime();
                float timeTaken = (float) (endTime - startTime) / 1_000_000;
                executionTime.setText("Time taken to compute Convex Hull: " + timeTaken + " ms");
                drawingPanel.repaint();
            }
            drawingPanel.repaint();
        });

        drawingPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                points.add(new Point(e.getX(), e.getY()));
                drawingPanel.repaint();
            }
        });

        return mainPanel;

    }


    private class DrawingPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) { super.paintComponent(g);

            // Draw the lines connecting the convex hull points
            g.setColor(Color.RED);
            for (int i = 1; i < convexHullPoints.size(); i++) {
                Point start = convexHullPoints.get(i - 1);
                Point end = convexHullPoints.get(i);
                g.drawLine(start.x, start.y, end.x, end.y);
            }
            if (!convexHullPoints.isEmpty()) {
                Point start = convexHullPoints.get(convexHullPoints.size() - 1);
                Point end = convexHullPoints.get(0);
                g.drawLine(start.x, start.y, end.x, end.y);
            }
            // Draw the marked points
            for (Point point : points) {
                // Draw the point
                if (convexHullPoints.contains(point)) {
                    // Draw the point in green
                    g.setColor(Color.GREEN);
                } else {
                    // Draw the point in blue
                    g.setColor(Color.BLUE);
                }
                g.fillOval(point.x - 5, point.y - 5, 10, 10);

                // Display the coordinates
                g.setColor(Color.WHITE);
                g.drawString("(" + point.x + ", " + point.y + ")", point.x + 10, point.y - 10);
            }
        }

    }

    private static boolean isLeftTurn(Point a, Point b, Point c) {
        return (b.x - a.x) * (c.y - a.y) - (b.y - a.y) * (c.x - a.x) > 0;
    }
     static int calculateOrientation(Point p, Point q, Point r) {
        int val = (q.y - p.y) * (r.x - q.x) - (q.x - p.x) * (r.y - q.y);
        if (val == 0) {
            return 0; // Collinear
        }
        return (val > 0) ? 1 : 2; // Clockwise or Counterclockwise
    }

    private static void findHullSet(Point A, Point B, ArrayList<Point> points, ArrayList<Point> hull, int side) {
        int insertPosition = hull.indexOf(B);
        if (points.size() == 0) {
            return;
        }

        int dist = 0;
        int farthestPoint = -1;

        for (int i = 0; i < points.size(); i++) {
            Point p = points.get(i);
            int distance = (int) crossProduct(A, B, p) * side;

            if (distance > dist) {
                dist = distance;
                farthestPoint = i;
            }
        }

        if (farthestPoint == -1) {
            // No points found, this may happen when all remaining points are collinear
            return;
        }

        Point P = points.get(farthestPoint);
        points.remove(farthestPoint);
        hull.add(insertPosition, P);




    }

    private static double crossProduct(Point A, Point B, Point C) {
        return (B.getX() - A.getX()) * (C.getY() - A.getY()) - (B.getY() - A.getY()) * (C.getX() - A.getX());
    }

    private  int orientation(Point p, Point q, Point r, boolean upper) {
        int val = (q.y - p.y) * (r.x - q.x) - (q.x - p.x) * (r.y - q.y);

        if (upper) {
            // For upper hull, reverse the orientation
            return -val;
        } else {
            return val;
        }
    }


    private ArrayList<Point> convexHullSubroutine(ArrayList<Point> points, boolean upper) {
        Stack<Point> hull = new Stack<>();

        for (Point point : points) {
            while (hull.size() >= 2 &&
                    orientation(hull.get(hull.size() - 2), hull.get(hull.size() - 1), point, upper) <= 0) {
                hull.pop();
            }
            hull.push(point);
        }
        convexHullPoints.addAll(hull);


        return points;
    }

    private  void  mergeHulls(ArrayList<Point> lowerHull, ArrayList<Point> upperHull) {


        // Skip the last point of upper hull to avoid duplication
        for (int i = 1; i < upperHull.size(); i++) {
            convexHullPoints.add(upperHull.get(i));
        }


    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ConvexHullVisualization().setVisible(true));
    }


    private void showConvexHullScreen(){
        getContentPane().removeAll();
        getContentPane().add(convexHullScreen);
        revalidate();
        repaint();
    }


}



