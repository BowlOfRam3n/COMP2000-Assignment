import java.awt.Color;
import java.util.Random;

/** Coordinates season timing and delegates seasonal rules to SeasonStrategy objects. */
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
    private int seasonStartPopulation = -1;

    // A winter population loss boosts the next spring. Spring growth boosts
    // the next winter. Both are bounded by the receiving season's strategy.
    private double winterRecoveryMemory;
    private double springGrowthMemory;

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

        if (seasonStartPopulation < 0) {
            seasonStartPopulation = cells.size();
        }

        seasonTimer += deltaTime;
        effectTimer += deltaTime;
        updateCellModifiers(cells);

        while (effectTimer >= 1000) {
            effectTimer -= 1000;
            currentSeason().applyEffect(cells, model, memoryForCurrentSeason());
        }

        if (seasonTimer >= seasonLengthSeconds * 1000L) {
            changeSeason(cells);
        }
    }

    private void changeSeason(Population cells) {
        recordSeasonOutcome(cells.size());
        currentSeasonIndex = (currentSeasonIndex + 1) % seasons.length;
        seasonTimer = 0;
        effectTimer = 0;
        seasonStartPopulation = cells.size();
        chooseSeasonLength();
        updateBackground();
        updateCellModifiers(cells);
    }

    private void recordSeasonOutcome(int endingPopulation) {
        int startingPopulation = Math.max(1, seasonStartPopulation);
        double change = endingPopulation - (double) seasonStartPopulation;
        String endingSeason = currentSeason().getName();

        if ("WINTER".equals(endingSeason)) {
            // Remember up to a 10 percentage-point recovery bonus after a net loss.
            winterRecoveryMemory = clamp((-change / startingPopulation) * 0.5, 0.0, 0.10);
        } else if ("SPRING".equals(endingSeason)) {
            // Remember up to a 5 percentage-point crowding response after net growth.
            springGrowthMemory = clamp((change / startingPopulation) * 0.25, 0.0, 0.05);
        }
    }

    private double memoryForCurrentSeason() {
        switch (currentSeason().getName()) {
            case "SPRING":
                return winterRecoveryMemory;
            case "WINTER":
                return springGrowthMemory;
            default:
                return 0.0;
        }
    }

    private static double clamp(double value, double minimum, double maximum) {
        return Math.max(minimum, Math.min(maximum, value));
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
        seasonStartPopulation = -1;
        updateBackground();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void reset() {
        currentSeasonIndex = 1; // Start a reset simulation in spring.
        seasonTimer = 0;
        effectTimer = 0;
        seasonStartPopulation = -1;
        winterRecoveryMemory = 0.0;
        springGrowthMemory = 0.0;
        chooseSeasonLength();
        updateBackground();
    }
}
