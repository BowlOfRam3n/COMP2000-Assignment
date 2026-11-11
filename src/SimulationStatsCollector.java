import java.util.stream.Collector;

/**
 * A custom Collector that turns a stream of cells into one SimulationStats
 * snapshot in a single pass: counts per state plus the R0 average.
 */
public final class SimulationStatsCollector {

    private SimulationStatsCollector() {
    }

    /**
     * @param deadCount      dead cells so far (not derivable from the live cells)
     * @param duplicateCount duplicated cells so far
     * @param tick           the tick this snapshot is taken at
     */
    public static Collector<Cell, ?, SimulationStats> of(int deadCount, int duplicateCount, int tick) {
        return Collector.of(
                Accumulator::new,                                          // 1. supplier: new empty accumulator
                Accumulator::add,                                          // 2. accumulator: fold one cell in
                Accumulator::merge,                                        // 3. combiner: join two accumulators
                acc -> acc.toStats(deadCount, duplicateCount, tick),       // 4. finisher: build the result
                Collector.Characteristics.UNORDERED);                      // order of cells doesn't matter
    }

    /** The mutable running totals. */
    private static class Accumulator {
        private int neutral;
        private int infected;
        private int antivirus;
        private int r0Cells;        // cells infected during this window
        private long r0Infections;  // infections those cells caused

        void add(Cell c) {
            CellState s = c.getState();
            if (s == NeutralState.INSTANCE) {
                neutral++;
            } else if (s == InfectedState.INSTANCE) {
                infected++;
            } else if (s == AntivirusState.INSTANCE) {
                antivirus++;
            }

            if (c.infectedThisWindow) {
                r0Cells++;
                r0Infections += c.infectionsCaused;
            }
        }

        /** Used by parallel streams: each thread fills its own accumulator, then they are merged. */
        Accumulator merge(Accumulator other) {
            neutral += other.neutral;
            infected += other.infected;
            antivirus += other.antivirus;
            r0Cells += other.r0Cells;
            r0Infections += other.r0Infections;
            return this;
        }

        SimulationStats toStats(int deadCount, int duplicateCount, int tick) {
            float r0 = (r0Cells == 0) ? 0f : (float) r0Infections / r0Cells;
            return new SimulationStats(neutral, infected, antivirus, deadCount, duplicateCount, r0, tick);
        }
    }
}