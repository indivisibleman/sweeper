package topsom.sweeper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import javax.swing.Timer;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Queue;
import java.util.*;

public class Monolith extends JFrame {
    public static final Logger log = LogManager.getLogger(Monolith.class);
    private final JPanel statusPanel = new JPanel();
    private final JButton faceButton = new JButton();
    private final JPanel minePanel = new JPanel();
    private static final String TIMER_LABEL_TEXT = "Timer: ";
    private final JLabel timerLabel = new JLabel(TIMER_LABEL_TEXT + "00:00");
    private final JLabel minesLabel = new JLabel(MINE_LABEL_TEXT + INTERMEDIATE_MINE_COUNT);
    private static final int BEGINNER_ROW_COUNT = 9;
    private static final int BEGINNER_COLUMN_COUNT = 9;
    private static final int INTERMEDIATE_ROW_COUNT = 16;
    private static final int INTERMEDIATE_COLUMN_COUNT = 16;
    private static final int EXPERT_ROW_COUNT = 16;
    private static final int EXPERT_COLUMN_COUNT = 30;
    private static final int CELL_WIDTH = 20;
    private static final int CELL_HEIGHT = 20;
    private static final int HORIZONTAL_GAP = 0;
    private static final int VERTICAL_GAP = 0;
    private final Map<JButton, Point> buttonToPoint = new HashMap<>();
    private final Map<Point, JButton> pointToButton = new HashMap<>();
    private static final String MINE_LABEL_TEXT = "Mines: ";
    private static final int BEGINNER_MINE_COUNT = 10;
    private static final int INTERMEDIATE_MINE_COUNT = 40;
    private static final int EXPERT_MINE_COUNT = 99;
    private int mineCount = INTERMEDIATE_MINE_COUNT;
    private final Set<Point> mines = new HashSet<>(mineCount);
    private final Set<Point> flags = new HashSet<>();
    private static final int STATUS_FIRST_CLICK = 0;
    private static final int STATUS_PLAYING = 1;
    private static final int STATUS_GAME_OVER = 2;
    private int status = 0;
    private ImageIcon blankIcon;
    private ImageIcon oneIcon;
    private ImageIcon twoIcon;
    private ImageIcon threeIcon;
    private ImageIcon fourIcon;
    private ImageIcon fiveIcon;
    private ImageIcon sixIcon;
    private ImageIcon sevenIcon;
    private ImageIcon eightIcon;
    private ImageIcon flagIcon;
    private ImageIcon mineIcon;
    private ImageIcon facePlayIcon;
    private ImageIcon faceWinIcon;
    private ImageIcon faceFailIcon;
    private final Timer timer = new Timer(50, this::timerActionListener);
    private long startTime = System.currentTimeMillis();
    private final JMenuBar jMenuBar = new JMenuBar();
    private final JMenu gameMenu = new JMenu("Game");
    private final JMenuItem newMenuItem = new JMenuItem("New", KeyEvent.VK_F2);
    private final JMenuItem beginnerMenuItem = new JMenuItem("Beginner");
    private final JMenuItem intermediateMenuItem = new JMenuItem("Intermediate");
    private final JMenuItem expertMenuItem = new JMenuItem("Expert");
    private final JMenuItem customMenuItem = new JMenuItem("Custom");
    //private final JMenuItem bestTimesMenuItem = new JMenuItem("Best times");
    private final JMenuItem exitMenuItem = new JMenuItem("Exit");

    private void timerActionListener(ActionEvent actionEvent) {
        long time = System.currentTimeMillis() - startTime;

        var duration = Duration.ofMillis(time);
        long hours = duration.getSeconds() / 3600;
        long minutes = (duration.getSeconds() % 3600) / 60;
        long seconds = duration.getSeconds() % 60;

        if (hours > 0) {
            timerLabel.setText(TIMER_LABEL_TEXT + String.format("%02d:%02d:%02d", hours, minutes, seconds));
        } else {
            timerLabel.setText(TIMER_LABEL_TEXT + String.format("%02d:%02d", minutes, seconds));
        }
    }

    private Monolith() {
        initialiseUserInterface();
    }

    private void initialiseUserInterface() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        setTitle("Minesweeper");
        setResizable(false);
        setLayout(new BorderLayout());

        statusPanel.setLayout(new GridBagLayout());
        statusPanel.setBorder(new EmptyBorder(2, 5, 0, 5));

        blankIcon = loadIcon("/blank.png");
        oneIcon = loadIcon("/1.png");
        twoIcon = loadIcon("/2.png");
        threeIcon = loadIcon("/3.png");
        fourIcon = loadIcon("/4.png");
        fiveIcon = loadIcon("/5.png");
        sixIcon = loadIcon("/6.png");
        sevenIcon = loadIcon("/7.png");
        eightIcon = loadIcon("/8.png");
        flagIcon = loadIcon("/flag.png");
        mineIcon = loadIcon("/mine.png");
        facePlayIcon = loadIcon("/face-play.png");
        faceWinIcon = loadIcon("/face-win.png");
        faceFailIcon = loadIcon("/face-fail.png");

        var statusPanelConstraints = new GridBagConstraints();
        statusPanelConstraints.weightx = 1.0;
        statusPanelConstraints.gridx = 0;
        statusPanelConstraints.anchor = GridBagConstraints.LINE_START;
        statusPanel.add(timerLabel, statusPanelConstraints);
        statusPanelConstraints.gridx = 1;
        statusPanelConstraints.anchor = GridBagConstraints.CENTER;
        faceButton.setPreferredSize(new Dimension(32, 32));
        faceButton.setIcon(facePlayIcon);
        faceButton.addActionListener(this::faceButton);
        statusPanel.add(faceButton, statusPanelConstraints);
        statusPanelConstraints.gridx = 2;
        statusPanelConstraints.anchor = GridBagConstraints.LINE_END;
        statusPanel.add(minesLabel, statusPanelConstraints);
        add(statusPanel, BorderLayout.NORTH);

        minePanel.setBorder(new EmptyBorder(HORIZONTAL_GAP, VERTICAL_GAP, HORIZONTAL_GAP, VERTICAL_GAP));
        populateMinePanelButtons(INTERMEDIATE_ROW_COUNT, INTERMEDIATE_COLUMN_COUNT);

        add(minePanel, BorderLayout.CENTER);

        setIconImage(mineIcon.getImage());

        newMenuItem.addActionListener(this::faceButton);
        gameMenu.add(newMenuItem);
        gameMenu.addSeparator();
        beginnerMenuItem.addActionListener(this::switchToBeginnerMode);
        gameMenu.add(beginnerMenuItem);
        intermediateMenuItem.addActionListener(this::switchToIntermediateMode);
        gameMenu.add(intermediateMenuItem);
        expertMenuItem.addActionListener(this::switchToExpertMode);
        gameMenu.add(expertMenuItem);
        customMenuItem.addActionListener(this::switchToCustomMode);
        gameMenu.add(customMenuItem);
        gameMenu.addSeparator();
        //gameMenu.add(bestTimesMenuItem);
        //gameMenu.addSeparator();
        exitMenuItem.addActionListener(this::exit);
        gameMenu.add(exitMenuItem);

        jMenuBar.add(gameMenu);

        setJMenuBar(jMenuBar);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void exit(ActionEvent actionEvent) {
        System.exit(0);
    }

    private void switchToBeginnerMode(ActionEvent actionEvent) {
        mineCount = BEGINNER_MINE_COUNT;
        populateMinePanelButtons(BEGINNER_ROW_COUNT, BEGINNER_COLUMN_COUNT);
        faceButton(actionEvent);
        pack();
    }

    private void switchToIntermediateMode(ActionEvent actionEvent) {
        mineCount = INTERMEDIATE_MINE_COUNT;
        populateMinePanelButtons(INTERMEDIATE_ROW_COUNT, INTERMEDIATE_COLUMN_COUNT);
        faceButton(actionEvent);
        pack();
    }

    private void switchToExpertMode(ActionEvent actionEvent) {
        mineCount = EXPERT_MINE_COUNT;
        populateMinePanelButtons(EXPERT_ROW_COUNT, EXPERT_COLUMN_COUNT);
        faceButton(actionEvent);
        pack();
    }

    private void switchToCustomMode(ActionEvent actionEvent) {
        var dialog = new JDialog(this, "Custom board values", Dialog.ModalityType.DOCUMENT_MODAL);
        dialog.setLayout(new GridBagLayout());
        dialog.getRootPane().setBorder(new EmptyBorder(2, 5, 0, 5));
        var constraints = new GridBagConstraints();
        constraints.insets = new Insets(1, 2, 1, 2);
        constraints.anchor = GridBagConstraints.LINE_END;
        var rowsSpinner = new JSpinner();
        rowsSpinner.setModel(new SpinnerNumberModel(INTERMEDIATE_ROW_COUNT, 3, 100, 1));
        var rowsLabel = new JLabel("Rows");
        rowsLabel.setLabelFor(rowsSpinner);
        constraints.gridx = 0;
        constraints.gridy = 0;
        dialog.add(rowsLabel, constraints);
        constraints.gridx = 1;
        constraints.gridy = 0;
        dialog.add(rowsSpinner, constraints);
        var columnsSpinner = new JSpinner();
        columnsSpinner.setModel(new SpinnerNumberModel(INTERMEDIATE_COLUMN_COUNT, 3, 100, 1));
        var columnsLabel = new JLabel("Columns");
        columnsLabel.setLabelFor(columnsSpinner);
        constraints.gridx = 0;
        constraints.gridy = 1;
        dialog.add(columnsLabel, constraints);
        constraints.gridx = 1;
        constraints.gridy = 1;
        dialog.add(columnsSpinner, constraints);
        var minesSpinner = new JSpinner();
        minesSpinner.setModel(new SpinnerNumberModel(INTERMEDIATE_MINE_COUNT, 1, 100, 1));
        var minesLabel = new JLabel("Mines");
        minesLabel.setLabelFor(minesSpinner);
        constraints.gridx = 0;
        constraints.gridy = 2;
        dialog.add(minesLabel, constraints);
        constraints.gridx = 1;
        constraints.gridy = 2;
        dialog.add(minesSpinner, constraints);
        var confirmButton = new JButton("Confirm");
        constraints.gridx = 0;
        constraints.gridwidth = 2;
        constraints.gridy = 3;
        confirmButton.addActionListener(event -> switchToCustomMode(event, (Integer) minesSpinner.getValue(), (Integer) rowsSpinner.getValue(), (Integer) columnsSpinner.getValue()));
        rowsSpinner.addChangeListener(e -> {
            int cellCount = (Integer) rowsSpinner.getValue() * (Integer) columnsSpinner.getValue();
            if ((Integer) minesSpinner.getValue() > cellCount - 3) {
                minesSpinner.setValue(cellCount - 3);
            }
        });
        columnsSpinner.addChangeListener(e -> {
            int cellCount = (Integer) rowsSpinner.getValue() * (Integer) columnsSpinner.getValue();
            if ((Integer) minesSpinner.getValue() > cellCount - 3) {
                minesSpinner.setValue(cellCount - 3);
            }
        });
        minesSpinner.addChangeListener(e -> {
            int cellCount = (Integer) rowsSpinner.getValue() * (Integer) columnsSpinner.getValue();
            if ((Integer) minesSpinner.getValue() > cellCount - 3) {
                minesSpinner.setValue(cellCount - 3);
            }
        });
        dialog.add(confirmButton, constraints);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void switchToCustomMode(ActionEvent actionEvent, int mineCount, int rowCount, int columnCount) {
        this.mineCount = mineCount;
        populateMinePanelButtons(rowCount, columnCount);
        faceButton(actionEvent);
        pack();
    }

    private void populateMinePanelButtons(int rowCount, int columnCount) {
        minePanel.setBounds(0, 0, rowCount * CELL_WIDTH, columnCount * CELL_HEIGHT);
        minePanel.setLayout(new GridLayout(rowCount, columnCount, HORIZONTAL_GAP, VERTICAL_GAP));
        minePanel.removeAll();
        buttonToPoint.clear();
        pointToButton.clear();

        minesLabel.setText(MINE_LABEL_TEXT + (mineCount - flags.size()));

        for (int row = 0; row < rowCount; row++) {
            for (int column = 0; column < columnCount; column++) {
                var button = new JButton();
                button.setPreferredSize(new Dimension(CELL_WIDTH, CELL_HEIGHT));
                button.setIcon(blankIcon);
                button.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        var point = buttonToPoint.get(button);
                        if (SwingUtilities.isRightMouseButton(e)
                                && STATUS_PLAYING == status) {
                            flag(button, point);
                        } else if (SwingUtilities.isLeftMouseButton(e)
                                && e.isControlDown()
                                && STATUS_PLAYING == status
                                && (button.isEnabled() || (!button.isEnabled() && flags.contains(point)))) {
                            flag(button, point);
                        } else if (SwingUtilities.isLeftMouseButton(e)
                                && e.getClickCount() == 2
                                && STATUS_PLAYING == status
                                && !button.isEnabled()) {
                            for (var surroundingPoint : getSurroundingPoints(point)) {
                                if (!flags.contains(surroundingPoint) && pointToButton.containsKey(surroundingPoint)) {
                                    clickPoint(surroundingPoint);
                                }
                            }
                        } else if (SwingUtilities.isLeftMouseButton(e)) {
                            buttonClick(button);
                        }
                    }
                });
                var point = new Point(row, column);
                buttonToPoint.putIfAbsent(button, point);
                pointToButton.putIfAbsent(point, button);
                minePanel.add(button);
            }
        }
    }

    private void faceButton(ActionEvent actionEvent) {
        mines.clear();
        flags.clear();
        status = STATUS_FIRST_CLICK;
        timer.stop();
        timerLabel.setText(TIMER_LABEL_TEXT + "00:00");
        minesLabel.setText(MINE_LABEL_TEXT + mineCount);
        faceButton.setIcon(facePlayIcon);
        for (var button : buttonToPoint.keySet()) {
            button.setEnabled(true);
            button.setIcon(blankIcon);
            button.setBackground(null);
        }
    }

    private void buttonClick(JButton button) {
        if (!buttonToPoint.containsKey(button)) {
            return;
        }

        var point = buttonToPoint.get(button);

        switch (status) {
            case STATUS_FIRST_CLICK -> {
                status = STATUS_PLAYING;
                startTime = System.currentTimeMillis();
                timer.start();
                List<Point> possibleMines = new ArrayList<>();
                for (var possibleMine : pointToButton.keySet()) {
                    if (possibleMine.equals(point)) {
                        continue;
                    }
                    possibleMines.add(possibleMine);
                }
                Collections.shuffle(possibleMines);
                for (int pointIndex = 0; pointIndex < mineCount; pointIndex++) {
                    mines.add(possibleMines.get(pointIndex));
                }
                clickPoint(point);
            }
            case STATUS_PLAYING -> clickPoint(point);
            case STATUS_GAME_OVER -> {
                // Do nothing
            }
            default -> log.warn("Default button click!?");
        }
    }

    private void flag(JButton button, Point point) {
        if (flags.contains(point)) {
            button.setEnabled(true);
            flags.remove(point);
        } else if (button.isEnabled()) {
            button.setEnabled(false);
            button.setDisabledIcon(flagIcon);
            flags.add(point);
        }
        minesLabel.setText(MINE_LABEL_TEXT + (mineCount - flags.size()));
    }

    private void clickPoint(Point clicked) {
        if (mines.contains(clicked)) {
            status = STATUS_GAME_OVER;
            timer.stop();
            for (var mine : mines) {
                pointToButton.get(mine).setIcon(mineIcon);
            }
            faceButton.setIcon(faceFailIcon);
            pointToButton.get(clicked).setBackground(Color.red);
            return;
        }

        Set<Point> checkedPoints = new HashSet<>();
        Queue<Point> pointsToCheck = new ArrayDeque<>();

        pointsToCheck.add(clicked);

        while (!pointsToCheck.isEmpty()) {
            var pointToCheck = pointsToCheck.poll();
            var button = pointToButton.get(pointToCheck);
            var surroundingPoints = getSurroundingPoints(pointToCheck);
            int surroundingMineCount = surroundingMineCount(surroundingPoints);
            switch (surroundingMineCount) {
                case 0 -> {
                    button.setDisabledIcon(blankIcon);
                    button.setEnabled(false);
                    for (var surroundingPoint : surroundingPoints) {
                        if (!pointToButton.containsKey(surroundingPoint)
                                || checkedPoints.contains(surroundingPoint)
                                || pointsToCheck.contains(surroundingPoint)
                                || flags.contains(surroundingPoint)) {
                            continue;
                        }

                        pointsToCheck.add(surroundingPoint);
                    }
                }
                case 1 -> {
                    button.setDisabledIcon(oneIcon);
                    button.setEnabled(false);
                }
                case 2 -> {
                    button.setDisabledIcon(twoIcon);
                    button.setEnabled(false);
                }
                case 3 -> {
                    button.setDisabledIcon(threeIcon);
                    button.setEnabled(false);
                }
                case 4 -> {
                    button.setDisabledIcon(fourIcon);
                    button.setEnabled(false);
                }
                case 5 -> {
                    button.setDisabledIcon(fiveIcon);
                    button.setEnabled(false);
                }
                case 6 -> {
                    button.setDisabledIcon(sixIcon);
                    button.setEnabled(false);
                }
                case 7 -> {
                    button.setDisabledIcon(sevenIcon);
                    button.setEnabled(false);
                }
                case 8 -> {
                    button.setDisabledIcon(eightIcon);
                    button.setEnabled(false);
                }
                default -> log.warn("Weird surrounding mine count: {}", surroundingMineCount);
            }

            checkedPoints.add(pointToCheck);
        }

        int unflaggedButtons = flags.size();

        for (var button : buttonToPoint.keySet()) {
            if (button.isEnabled()) {
                unflaggedButtons++;
            }
        }

        if (mines.size() == unflaggedButtons) {
            status = STATUS_GAME_OVER;
            faceButton.setIcon(faceWinIcon);
            timer.stop();
        }
    }

    private int surroundingMineCount(Point[] surroundingPoints) {
        int count = 0;

        for (var surroundingPoint : surroundingPoints) {
            if (mines.contains(surroundingPoint)) {
                count++;
            }
        }

        return count;
    }

    private Point[] getSurroundingPoints(Point point) {
        var surroundingPoints = new Point[8];

        surroundingPoints[0] = new Point(point.x + 1, point.y + 1);
        surroundingPoints[1] = new Point(point.x + 1, point.y);
        surroundingPoints[2] = new Point(point.x + 1, point.y - 1);
        surroundingPoints[3] = new Point(point.x, point.y + 1);
        surroundingPoints[4] = new Point(point.x, point.y - 1);
        surroundingPoints[5] = new Point(point.x - 1, point.y + 1);
        surroundingPoints[6] = new Point(point.x - 1, point.y);
        surroundingPoints[7] = new Point(point.x - 1, point.y - 1);

        return surroundingPoints;
    }

    private ImageIcon loadIcon(String iconName) {
        BufferedImage icon = null;

        try {
            icon = ImageIO.read(Objects.requireNonNull(getClass().getResource(iconName)));
        } catch (IOException | NullPointerException exception) {
            log.error("Failed to load icon: {} because of: {}%n", iconName, exception);
        }

        if (icon == null) {
            return null;
        }

        return new ImageIcon(icon);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Monolith::new);
    }
}
