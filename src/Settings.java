import java.awt.Color;
import java.awt.GridLayout;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToggleButton;

public class Settings {
    public static int CELL_SIZE = 35;
    public static double CELL_SPEED = 2.0;
    public static int CELL_COUNT = 50;
    public static int INFECTED_COUNT = 3;
    public static int ANTIVIRUS_COUNT = 6;
    public static boolean hasCollision = true;

    /**
     * Adds a "label | slider | value" row to the panel.
     * The behaviour that differs per slider is passed in as lambdas:
     * @param setter  what to do with the slider's value (a Consumer of int)
     * @param format  how to display the value as text (a Function from int to String)
     */
    private void addSlider(JPanel panel, String name, int min, int max, int initial,
                           IntConsumer setter, IntFunction<String> format) {
        JLabel label = new JLabel(name);
        label.setForeground(Color.WHITE);

        JSlider slider = new JSlider(min, max, initial);
        slider.setBackground(Color.WHITE);

        JLabel valueLabel = new JLabel(format.apply(initial));
        valueLabel.setForeground(Color.WHITE);

        slider.addChangeListener(e -> {
            setter.accept(slider.getValue());
            valueLabel.setText(format.apply(slider.getValue()));
        });

        panel.add(label);
        panel.add(slider);
        panel.add(valueLabel);
    }

    public JPanel settingsPanel() {
        // Create a settings panel with sliders for cell size and speed
        JPanel settingsPanel = new JPanel();
        settingsPanel.setLayout(new GridLayout(6, 3, 5, 5));
        settingsPanel.setBackground(Color.DARK_GRAY);
        settingsPanel.setOpaque(true);

        addSlider(settingsPanel, "Size", 5, 50, Settings.CELL_SIZE,
                v -> Settings.CELL_SIZE = v, String::valueOf);

        // The slider holds tenths, so 20 means a speed of 2.0
        addSlider(settingsPanel, "Speed", 1, 100, (int) (Settings.CELL_SPEED * 10),
                v -> Settings.CELL_SPEED = v / 10.0, v -> String.valueOf(v / 10.0));

        addSlider(settingsPanel, "Count", 10, 200, Settings.CELL_COUNT,
                v -> Settings.CELL_COUNT = v, String::valueOf);

        addSlider(settingsPanel, "InfCells", 1, 20, Settings.INFECTED_COUNT,
                v -> Settings.INFECTED_COUNT = v, String::valueOf);

        addSlider(settingsPanel, "AntiCells", 1, 20, Settings.ANTIVIRUS_COUNT,
                v -> Settings.ANTIVIRUS_COUNT = v, String::valueOf);

        JLabel collisionLabel = new JLabel("Collision");
        collisionLabel.setForeground(Color.WHITE);
        JToggleButton collisionToggle = new JToggleButton(Settings.hasCollision ? "On" : "Off");
        collisionToggle.setBackground(Color.WHITE);
        collisionToggle.addActionListener(e -> {
            Settings.hasCollision = !Settings.hasCollision;
            collisionToggle.setText(Settings.hasCollision ? "On" : "Off");
        });
        settingsPanel.add(collisionLabel);
        settingsPanel.add(collisionToggle);

        settingsPanel.doLayout();

        return settingsPanel;
    }
}