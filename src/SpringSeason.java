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
    public void applyEffect(Population cells, SimulationModel model){
        List<Cell> newCells = new ArrayList<>();

        // Only neutral cells can reproduce.
        for (Cell cell : cells.ofState(NeutralState.INSTANCE)) {

            // 10% chance for this cell to reproduce.
            if (random.nextDouble() < 0.10){
                double newX = cell.getX() + (random.nextDouble() * 10 - 5);
                double newY = cell.getY() + (random.nextDouble() * 10 - 5);

                newCells.add(new Cell(newX, newY, cells.size() + newCells.size(), NeutralState.INSTANCE));
                model.incrementDuplicateCellCount();
                
            }
        }

        // Add all new cells after the loop has finished.
        cells.addAll(newCells);
    }
}
