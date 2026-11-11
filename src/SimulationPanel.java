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
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/** Swing view for the simulation; rules and state live in SimulationModel. */
public class SimulationPanel extends JPanel implements ActionListener {
    private final int offset = 10;
    private final int statScreenX = 200;
    private final SimulationModel model = new SimulationModel(offset);
    private final Timer timer;

    private int simScreenX;
    private int simScreenY;
    private JButton resetButton;
    private JPanel settingsPanel;
    private JCheckBox seasonsCheckBox;

    public SimulationPanel() {
        setPreferredSize(new Dimension(1200, 800));
        setLayout(null);
        setBackground(Color.BLACK);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateDimensions();
            }
        });

        timer = new Timer(16, this);
        timer.start();
    }

    private void updateDimensions() {
        simScreenX = getWidth() - statScreenX - offset;
        simScreenY = getHeight() - offset;
        if (simScreenX <= 0 || simScreenY <= 0) {
            return;
        }

        model.setWorldSize(simScreenX, simScreenY);

        if (resetButton == null) {
            resetButton = new JButton("Reset Simulation");
            resetButton.addActionListener(e -> resetSimulation());
            add(resetButton);
        }
        resetButton.setBounds(simScreenX + offset, offset * 43, 180, 50);

        if (settingsPanel == null) {
            settingsPanel = new Settings().settingsPanel();
            add(settingsPanel);
        }
        settingsPanel.setBounds(simScreenX + offset, offset * 29, 180, 120);

        if (seasonsCheckBox == null) {
            seasonsCheckBox = new JCheckBox("Enable Seasons", true);
            seasonsCheckBox.setForeground(Color.WHITE);
            seasonsCheckBox.addActionListener(e -> {
                model.setSeasonsEnabled(seasonsCheckBox.isSelected());
                repaint();
            });
            add(seasonsCheckBox);
        }
        seasonsCheckBox.setBounds(simScreenX + offset, offset * 39, 170, 25);
    }

    private void resetSimulation() {
        model.reset();
        model.setWorldSize(simScreenX, simScreenY);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(model.getBackground());
        g.fillRect(offset, offset, simScreenX - offset, simScreenY - offset);
        g.setColor(Color.BLACK);
        g.drawRect(offset, offset, simScreenX - offset, simScreenY - offset);
        drawStats(g);

        for (Cell cell : model.getCells()) {
            cell.draw(g);
        }
    }

    private void drawStats(Graphics g) {
        int[] counts = model.countByType();
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
        g.drawString("Neutral Cells: " + counts[0], simScreenX + offset, offset * 13);
        g.setColor(InfectedState.INSTANCE.getCellColor());
        g.drawString("Infected Cells: " + counts[1], simScreenX + offset, offset * 16);
        g.setColor(AntivirusState.INSTANCE.getCellColor());
        g.drawString("Antivirus Cells: " + counts[2], simScreenX + offset, offset * 19);
        g.setColor(Color.DARK_GRAY);
        g.drawString("Dead Cells: " + model.getDeadCellCount(), simScreenX + offset, offset * 22);
        g.setColor(Color.YELLOW);
        g.drawString("Duplicate Cells: " + model.getDuplicateCellCount(), simScreenX + offset, offset * 25);
        g.setColor(Color.MAGENTA);
        g.drawString("R0 Value: " + model.getR0(), simScreenX + offset, offset * 28);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        model.tick();
        repaint();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        SwingUtilities.invokeLater(this::updateDimensions);
    }
}
