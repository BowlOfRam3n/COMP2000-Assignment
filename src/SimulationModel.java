import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.DoubleSupplier;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The simulation's data and rules.
 */
public class SimulationModel {
    private final Population population = new Population();
    private final List<SimulationStats> statsHistory = new ArrayList<>();
    private final SeasonManager seasonManager;

    private final int offset;
    private int worldWidth;
    private int worldHeight;

    private Color background = Color.LIGHT_GRAY;
    private BerserkState berserk = new BerserkState();

    private int cellCount = Settings.CELL_COUNT;
    private boolean cellsCreated = false;
    private int deadCellCount = 0;
    private int duplicateCellCount = 0;
    private int simTick = 0;
    private float r0 = 0;
    private long lastUpdateTime;

    public SimulationModel(int offset) {
        this.offset = offset;
        this.lastUpdateTime = System.currentTimeMillis();
        this.seasonManager = new SeasonManager(this);
    }

    /** Called by the view whenever the drawable area changes. */
    public void setWorldSize(int width, int height) {
        this.worldWidth = width;
        this.worldHeight = height;

        if (!cellsCreated && width > 0 && height > 0) {
            createCells();
            cellsCreated = true;
        }
    }

    public void reset() {
        cellCount = Settings.CELL_COUNT;
        population.clear();
        statsHistory.clear();
        cellsCreated = false;
        deadCellCount = 0;
        duplicateCellCount = 0;
        simTick = 0;
        r0 = 0;
        seasonManager.reset();
        lastUpdateTime = System.currentTimeMillis();
        berserk = new BerserkState(); // fresh berserk chances after every reset
    }

    private void createCells() {
        try {
            validateSettings();
        } catch (InvalidSettingsException e) {
            Settings.INFECTED_COUNT = 3;
            Settings.ANTIVIRUS_COUNT = 3;
        }

        // Suppliers: each call produces a new random coordinate
        DoubleSupplier randomX = () -> offset + Math.random() * (worldWidth - 2 * offset - 20);
        DoubleSupplier randomY = () -> offset + Math.random() * (worldHeight - 2 * offset - 20);

        for (int i = 0; i < cellCount; i++) {
            double x = randomX.getAsDouble();
            double y = randomY.getAsDouble();

            if (i < Settings.INFECTED_COUNT) {
                population.add(new Cell(x, y, i, InfectedState.INSTANCE));
            } else if (i < Settings.ANTIVIRUS_COUNT + Settings.INFECTED_COUNT) {
                population.add(new Cell(x, y, i, AntivirusState.INSTANCE));
            } else {
                population.add(new Cell(x, y, i, NeutralState.INSTANCE));
            }
        }
    }


    /** Advances the simulation by one step. */
    public void tick() {
        simTick++;
        long currentTime = System.currentTimeMillis();
        long deltaTime = currentTime - lastUpdateTime;
        lastUpdateTime = currentTime;

        // Move each cell.
        for (Cell cell : population) {
            cell.move(worldWidth, worldHeight, offset, offset);
        }

        // Check cell collisions (every unordered pair once).
        for (CellPair pair : population.collisionPairs()) {
            if (pair.a().collidesWith(pair.b())) {
                pair.a().onCollision(pair.b());
            }
        }

        // Update the season system.
        seasonManager.update(population, deltaTime);

        // Random deaths (each cell has a tiny chance per tick).
        incrementDeadCellCount(population.removeIf(c -> Math.random() < 0.0001));

        // Random duplication of infected and antivirus cells.
        List<Cell> toAdd = new ArrayList<>();
        population.forEach(c -> {
            if ((c.getState() == InfectedState.INSTANCE || c.getState() == AntivirusState.INSTANCE)
                    && Math.random() < 0.0002) {
                toAdd.add(new Cell(c.getX(), c.getY(), population.size(), c.getState(), Color.ORANGE, true));
            }
        });
        population.addAll(toAdd);
        duplicateCellCount += toAdd.size();

        // Record stats every 125 ticks.
        if (simTick % 125 == 0) {
            r0 = calculateR0();

            int[] counts = countByType();
            statsHistory.add(new SimulationStats(
                    counts[0], counts[1], counts[2],
                    deadCellCount, duplicateCellCount, r0, simTick));

            population.forEach(Cell::resetInfectionWindow);
        }
            updateBerserk();
    }

    private void updateBerserk() {
        if (!cellsCreated || population.isEmpty()) {
            return;
        }

        int numOfInfected = countByType()[1];

        if (numOfInfected <= 5 && berserk.getBerserkChanceLeft() > 0 && !berserk.isBerserk()) {
            try {
                berserk.EnterBerserk(); // Throws if berserk can't start

                population.ofState(InfectedState.INSTANCE).forEach(c -> c.isBerserk = true);
            } catch (IllegalBerserkException e) {
                System.out.println("Failed to enter Berserk: " + e.getMessage());
            }
        } else if (!berserk.isBerserk()) {
            population.forEach(c -> c.isBerserk = false);
        }
    }

    /** @return {neutral, infected, antivirus} */
    public int[] countByType() {
        // One pass: group the cells by state and count each group.
        Map<CellState, Long> counts = population.stream()
                .collect(Collectors.groupingBy(Cell::getState, Collectors.counting()));

        return new int[] {
                counts.getOrDefault(NeutralState.INSTANCE, 0L).intValue(),
                counts.getOrDefault(InfectedState.INSTANCE, 0L).intValue(),
                counts.getOrDefault(AntivirusState.INSTANCE, 0L).intValue()
        };
    }

    public float calculateR0() {
        // Average number of infections caused by the cells that were infected this window.
        return (float) population.stream()
                .filter(c -> c.infectedThisWindow)
                .mapToInt(c -> c.infectionsCaused)
                .average()
                .orElse(0);
    }

    /** Read-only view for the renderer. Replaced by an Iterator in step 2. */
    public Iterable<Cell> getCells() {
        return population;
    }

    public int getTotalCellCount() { 
        return population.size(); 
    }

    public List<SimulationStats> getStatsHistory() {
        return Collections.unmodifiableList(statsHistory);
    }

    public int getDeadCellCount() { return deadCellCount; }
    public int getDuplicateCellCount() { return duplicateCellCount; }
    public float getR0() { return r0; }
    public Color getBackground() { return background; }

    public String getCurrentSeason() { return seasonManager.getCurrentSeason().getName(); }
    public int getSecondsRemaining() { return seasonManager.getSecondsRemaining(); }

    // Commands used by SeasonManager

    public void setSeasonsEnabled(boolean enabled) {
        seasonManager.setEnabled(enabled);
    }

    public void setSimulationBackground(Color background) {
        this.background = background;
    }

    public void incrementDeadCellCount() {
        deadCellCount++;
    }

    public void incrementDeadCellCount(int amount) {
        deadCellCount += amount;
    }

    public void incrementDuplicateCellCount() {
        duplicateCellCount++;
    }

    // validation

    public void validateSettings() {
        if (Settings.INFECTED_COUNT + Settings.ANTIVIRUS_COUNT > Settings.CELL_COUNT) {
            throw new InvalidSettingsException("Infected + antivirus exceeds total cell count");
        }
    }

    public static class InvalidSettingsException extends RuntimeException {
        public InvalidSettingsException(String message) {
            super(message);
        }
    }
}