import java.awt.Color;
import java.util.Iterator;
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
    public void applyEffect(Population cells, SimulationModel model){
        Iterator<Cell> iterator = cells.iterator();

        while (iterator.hasNext()) {
            iterator.next();

            // 3% chance for this cell to die.

            if (random.nextDouble() < 0.03){
                iterator.remove();;
                model.incrementDeadCellCount();
            }
        }
    }
}
