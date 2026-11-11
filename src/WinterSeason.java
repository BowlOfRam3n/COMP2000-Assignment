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
    public void applyEffect(Population cells, SimulationModel model, double springGrowthMemory) {
        double targetPopulation = Math.max(1, Settings.CELL_COUNT);
        double excessPopulation = Math.max(0.0, (cells.size() - targetPopulation) / targetPopulation);

        // Winter culling responds to overcrowding and to growth remembered
        // from the previous spring, while keeping the death chance bounded.
        double deathChance = Math.min(
                0.13,
                0.03 + Math.min(0.05, excessPopulation * 0.05) + springGrowthMemory);
        int died = cells.removeIf(cell -> random.nextDouble() < deathChance);
        model.incrementDeadCellCount(died);
    }
}
