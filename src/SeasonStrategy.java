import java.awt.Color;

/**
 * One season;s behaviour. SeasomManager asks the current season to do things
 * and never needs to know which season it is.
 */

public interface SeasonStrategy {
    String getName();
    
    // Called once per second while this season is active
    default void applyEffect(Population cells, SimulationModel model){
        // most seasons have no one-per-second effect

    }

    // Called for every cell to set speed or size modifiers
    default void modifyCell(Cell cell) {
        // most seasons don't change cells directly
    }

    default Color getBackground() {
        return Color.LIGHT_GRAY;
    }
}
