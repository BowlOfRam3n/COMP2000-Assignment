import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SpringSeason implements SeasonStrategy {
    private final Random random = new Random();

    @Override
    public String getName() {
        return "SPRING";
    }

    @Override
    public void applyEffect(Population cells, SimulationModel model, double winterRecoveryMemory) {
        List<Cell> newCells = new ArrayList<>();
        int neutralCount = model.countByType()[0];
        double targetPopulation = Math.max(1, Settings.CELL_COUNT);
        double neutralShortage = clamp(1.0 - neutralCount / targetPopulation, 0.0, 1.0);

        // Spring reproduction rises when neutral cells are scarce, with a
        // bounded bonus carried over from the population loss in the last winter.
        double reproductionChance = clamp(
                0.10 + (0.10 * neutralShortage) + winterRecoveryMemory,
                0.0,
                0.30);

        // Population.stream() may be parallel for large populations. This action
        // uses randomness and shared state, so force this traversal to be sequential.
        cells.stream().sequential()
                .filter(cell -> cell.getState() == NeutralState.INSTANCE)
                .forEach(cell -> {
                    if (random.nextDouble() < reproductionChance) {
                        double newX = cell.getX() + (random.nextDouble() * 10 - 5);
                        double newY = cell.getY() + (random.nextDouble() * 10 - 5);

                        newCells.add(new Cell(
                                newX,
                                newY,
                                cells.size() + newCells.size(),
                                NeutralState.INSTANCE));
                        model.incrementDuplicateCellCount();
                    }
                });

        // Add offspring after the stream has finished traversing the population.
        cells.addAll(newCells);
    }

    private static double clamp(double value, double minimum, double maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }
}
