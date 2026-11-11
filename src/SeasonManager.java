import java.awt.Color;
import java.util.Random;

/** Coordinates the active season and delegates seasonal behaviour to strategies. */
public class SeasonManager {
    private final SimulationModel model;
    private final SeasonStrategy[] seasons = {
        new WinterSeason(),
        new SpringSeason(),
        new SummerSeason(),
        new AutumnSeason()
    };
    private final Random random = new Random();

    private int currentSeasonIndex;
    private boolean enabled = true;
    private long seasonTimer;
    private long effectTimer;
    private int seasonLengthSeconds;

    public SeasonManager(SimulationModel model) {
        this.model = model;
        currentSeasonIndex = random.nextInt(seasons.length);
        chooseSeasonLength();
        updateBackground();
    }

    public void update(Population cells, long deltaTime) {
        if (!enabled) {
            resetCellModifiers(cells);
            return;
        }

        seasonTimer += deltaTime;
        effectTimer += deltaTime;
        updateCellModifiers(cells);

        while (effectTimer >= 1000) {
            effectTimer -= 1000;
            currentSeason().applyEffect(cells, model);
        }

        if (seasonTimer >= seasonLengthSeconds * 1000L) {
            changeSeason(cells);
        }
    }

    private void changeSeason(Population cells) {
        currentSeasonIndex = (currentSeasonIndex + 1) % seasons.length;
        seasonTimer = 0;
        effectTimer = 0;
        chooseSeasonLength();
        updateBackground();
        updateCellModifiers(cells);
    }

    private void chooseSeasonLength() {
        // Choose a random duration from 5 through 10 seconds.
        seasonLengthSeconds = random.nextInt(6) + 5;
    }

    private void updateCellModifiers(Population cells) {
        for (Cell cell : cells) {
            cell.setSpeedMultiplier(1.0);
            cell.setSizeMultiplier(1.0);
            currentSeason().modifyCell(cell);
        }
    }

    private void resetCellModifiers(Population cells) {
        for (Cell cell : cells) {
            cell.setSpeedMultiplier(1.0);
            cell.setSizeMultiplier(1.0);
        }
    }

    private void updateBackground() {
        Color background = enabled ? currentSeason().getBackground() : Color.LIGHT_GRAY;
        model.setSimulationBackground(background);
    }

    private SeasonStrategy currentSeason() {
        return seasons[currentSeasonIndex];
    }

    public SeasonStrategy getCurrentSeason() {
        return currentSeason();
    }

    public int getSecondsRemaining() {
        long remainingMilliseconds = seasonLengthSeconds * 1000L - seasonTimer;
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
        currentSeasonIndex = 1; // Start a reset simulation in spring.
        seasonTimer = 0;
        effectTimer = 0;
        chooseSeasonLength();

        updateBackground();
    }
}


