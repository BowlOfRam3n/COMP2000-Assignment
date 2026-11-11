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
    public void applyEffect(Population cells, SimulationModel model) {
        List<Cell> newCells = new ArrayList<>();

        // Population.stream() may be parallel for large populations. Keep this
        // side-effecting operation sequential: it uses randomness and shared state.
        cells.stream().sequential()
                .filter(cell -> cell.getState() == NeutralState.INSTANCE)
                .forEach(cell -> {
                    if (random.nextDouble() < 0.10) {
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
}
