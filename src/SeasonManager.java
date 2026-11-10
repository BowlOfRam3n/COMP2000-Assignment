import java.awt.Color;
import java.util.List;
import java.util.Random;

/**
 * The Context in the Strategy pattern: keeps the timers and cycles through
 * the seasons, delegating each season's behaviour to a SeasonStrategy.
 */
public class SeasonManager {
    private static final int RESET_INDEX = 1; // SPRING, same as the old reset()

    private final SimulationModel simulationModel;
    private final Random random = new Random();

    // Order of this list is the order the seasons cycle in.
    private final List<SeasonStrategy> seasons = List.of(
            new WinterSeason(),
            new SpringSeason(),
            new SummerSeason(),
            new AutumnSeason());

    private int currentIndex;
    private boolean enabled = true;

    private long seasonTimer = 0;
    private long effectTimer = 0;

    private int seasonLengthSeconds;

    public SeasonManager(SimulationModel simulationModel) {
        this.simulationModel = simulationModel;
        currentIndex = random.nextInt(seasons.size());

        // Choose a random season length between 5 and 10 seconds.
        seasonLengthSeconds = random.nextInt(6) + 5;

        updateBackground();
    }

    private SeasonStrategy current() {
        return seasons.get(currentIndex);
    }

    public void update(Population cells, long deltaTime) {
        if (!enabled) {
            resetCellModifiers(cells);
            return;
        }

        seasonTimer += deltaTime;
        effectTimer += deltaTime;

        // Apply temporary speed and size modifiers.
        updateCellModifiers(cells);

        // Apply the season's effect once every second.
        while (effectTimer >= 1000) {
            effectTimer -= 1000;
            current().applyEffect(cells, simulationModel);
        }

        // Change season when the season timer reaches its limit.
        if (seasonTimer >= seasonLengthSeconds * 1000L) {
            changeSeason(cells);
        }
    }

    private void changeSeason(Population cells) {
        currentIndex = (currentIndex + 1) % seasons.size();

        seasonTimer = 0;
        effectTimer = 0;
        seasonLengthSeconds = random.nextInt(6) + 5;

        updateBackground();
        updateCellModifiers(cells);
    }

    private void updateCellModifiers(Population cells) {
        for (Cell cell : cells) {
            // Reset modifiers first, then let the season apply its own.
            cell.setSpeedMultiplier(1.0);
            cell.setSizeMultiplier(1.0);
            current().modifyCell(cell);
        }
    }

    private void resetCellModifiers(Population cells) {
        for (Cell cell : cells) {
            cell.setSpeedMultiplier(1.0);
            cell.setSizeMultiplier(1.0);
        }
    }

    private void updateBackground() {
        // Seasons only change the background when they're enabled.
        simulationModel.setSimulationBackground(
                enabled ? current().getBackground() : Color.LIGHT_GRAY);
    }

    public SeasonStrategy getCurrentSeason() {
        return current();
    }

    public int getSecondsRemaining() {
        long remainingMilliseconds = (seasonLengthSeconds * 1000L) - seasonTimer;
        return (int) Math.ceil(remainingMilliseconds / 1000.0);
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        seasonTimer = 0;
        effectTimer = 0;
        updateBackground();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void reset() {
        currentIndex = RESET_INDEX;
        seasonTimer = 0;
        effectTimer = 0;
        seasonLengthSeconds = random.nextInt(6) + 5;
        updateBackground();
    }
}