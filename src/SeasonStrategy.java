import java.awt.Color;

/** One season's behaviour, selected by SeasonManager. */
public interface SeasonStrategy {
    String getName();

    // Called once per second while this season is active.
    default void applyEffect(Population cells, SimulationModel model, double memoryBonus) {
        // Most seasons have no once-per-second effect.
    }

    // Called for each cell to apply season-specific speed or size changes.
    default void modifyCell(Cell cell) {
        // Most seasons don't change cells directly.
    }

    default Color getBackground() {
        return Color.LIGHT_GRAY;
    }
}
