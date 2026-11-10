import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * The view / controller. This draws the model and wires up the buttons.
 * All simulation rules live in SimulationModel now.
 */
public class SimulationPanel extends JPanel implements ActionListener {
    private final SimulationModel model;
    private final Timer timer;

    private JCheckBox seasonsCheckBox;
    private JButton resetButton;
    private JPanel settingsPanel;
    private boolean buttonCreated = false;
    private boolean settingsCreated = false;

    public int simScreenX;
    public int simScreenY;
    public int offset;
    public int statScreenX = 200;

    public SimulationPanel() {
        this.setPreferredSize(new Dimension(1200, 1200));
        this.setLayout(null);
        this.setBackground(Color.BLACK);
        offset = 10;
        model = new SimulationModel(offset);

        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateDimensions();
            }
        });

        timer = new Timer(16, this);
        timer.start();
    }

    private void createResetButton() {
        resetButton = new JButton("Reset Simulation");
        resetButton.addActionListener((ActionEvent e) -> {
            resetSimulation();
        });
        this.add(resetButton);
    }

    public void resetSimulation() {
        model.reset();
        updateDimensions(); // recreates the cells once the size is known
        repaint();
    }

    private void createSeasonsCheckBox() {
        seasonsCheckBox = new JCheckBox("Enable Seasons", true);
        seasonsCheckBox.setForeground(Color.white);

        seasonsCheckBox.addActionListener(e -> {
            model.setSeasonsEnabled(seasonsCheckBox.isSelected());
            repaint();
        });

        this.add(seasonsCheckBox);
    }

    public void updateDimensions() {
        simScreenX = this.getWidth() - statScreenX - offset;
        simScreenY = this.getHeight() - offset;

        model.setWorldSize(simScreenX, simScreenY);

        if (!buttonCreated && simScreenX > 0 && simScreenY > 0) {
            createResetButton();
            buttonCreated = true;
        }

        if (!settingsCreated && simScreenX > 0 && simScreenY > 0) {
            settingsPanel = new Settings().settingsPanel();
            settingsPanel.setBounds(simScreenX + offset, offset * 29, 180, 120);
            this.add(settingsPanel);
            settingsPanel.revalidate();
            settingsPanel.repaint();
            settingsCreated = true;
        }

        // Create Seasonal Checkbox once dimensions are available
        if (seasonsCheckBox == null && simScreenX > 0 && simScreenY > 0) {
            createSeasonsCheckBox();
        }

        // Reposition components based on window size
        if (resetButton != null) {
            resetButton.setBounds(simScreenX + offset, offset * 43, 180, 50);
        }

        if (settingsPanel != null) {
            settingsPanel.setBounds(simScreenX + offset, offset * 29, 180, 120);
            settingsPanel.revalidate();
            settingsPanel.repaint();
        }

        if (seasonsCheckBox != null) {
            seasonsCheckBox.setBounds(simScreenX + offset, offset * 39, 170, 25);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        drawSimBorder(g);
        drawStats(g);

        for (Cell cell : model.getCells()) {
            cell.draw(g);
        }
    }

    public void drawSimBorder(Graphics g) {
        Color background = model.getBackground();

        // Sim screen background
        g.setColor(background);
        g.fillRect(offset, offset, simScreenX - offset, simScreenY - offset);
        // Sim screen borders
        g.setColor(Color.BLACK);
        g.drawLine(simScreenX, offset, simScreenX, simScreenY);
        g.drawLine(offset, offset, offset, simScreenY);
        g.drawLine(offset, offset, simScreenX, offset);
        g.drawLine(offset, simScreenY, simScreenX, simScreenY);
    }

    public void drawStats(Graphics g) {
        int[] counts = model.countByType();
        int neutralCount = counts[0];
        int infectedCount = counts[1];
        int antivirusCount = counts[2];

        g.setFont(new Font("Times New Roman", Font.PLAIN, 30));
        g.setColor(Color.BLACK);
        g.drawString("Season: " + model.getCurrentSeason(), 10, simScreenY - 35);
        g.setColor(Color.WHITE);
        g.drawString("Next Season: " + model.getSecondsRemaining() + "s", 10, simScreenY - 10);
        g.drawString("Pandemic", simScreenX + offset, offset * 3);
        g.drawString("Simulator", simScreenX + offset, offset * 6);
        g.setFont(new Font("Times New Roman", Font.PLAIN, 20));
        g.drawString("Total Cells: " + model.getTotalCellCount(), simScreenX + offset, offset * 10);

        g.setColor(NeutralState.INSTANCE.getCellColor());
        g.drawString("Neutral Cells: " + neutralCount, simScreenX + offset, offset * 13);
        g.setColor(InfectedState.INSTANCE.getCellColor());
        g.drawString("Infected Cells: " + infectedCount, simScreenX + offset, offset * 16);
        g.setColor(AntivirusState.INSTANCE.getCellColor());
        g.drawString("Antivirus Cells: " + antivirusCount, simScreenX + offset, offset * 19);
        g.setColor(Color.DARK_GRAY);
        g.drawString("Dead Cells: " + model.getDeadCellCount(), simScreenX + offset, offset * 22);
        g.setColor(Color.yellow);
        g.drawString("Duplicate Cells: " + model.getDuplicateCellCount(), simScreenX + offset, offset * 25);
        g.setColor(Color.magenta);
        g.drawString("R0 Value: " + model.getR0(), simScreenX + offset, offset * 28);
        g.setColor(Color.ORANGE);
        
        g.drawString("Peak Infected: " + model.getPeakInfected(), simScreenX + offset, offset * 50);
        boolean active = model.isOutbreakActive();
        g.setColor(active ? Color.RED : Color.GREEN);
        g.drawString("Outbreak: " + (active ? "Active" : "Contained"), simScreenX + offset, offset * 53);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        model.tick();
        repaint();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        javax.swing.SwingUtilities.invokeLater(this::updateDimensions);
    }
}