import java.awt.Color;
import java.util.Random;

public class WinterSeason implements SeasonStrategy {
    private final Random random = new Random();

    @Override
    public String getName() {
        return "WINTER";
    }

    @Override
    public Color getBackground() {
        return new Color(210, 230, 245);
    }

    @Override
    public void applyEffect(Population cells, SimulationModel model) {
        // 3% chance for each cell to die; the rule is a Predicate.
        int died = cells.removeIf(c -> random.nextDouble() < 0.03);
        model.incrementDeadCellCount(died);
    }
}