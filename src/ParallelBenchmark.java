import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

/** Times the stats collector on sequential vs parallel streams at growing sizes. */
public class ParallelBenchmark {

    public static void main(String[] args) {
        int[] sizes = { 200, 2_000, 20_000, 200_000 };
        long sink = 0; // using the results stops the JIT from optimising the work away

        System.out.println("cells      sequential (us)   parallel (us)");
        for (int n : sizes) {
            List<Cell> cells = makeCells(n);

            // Warm-up: let the JIT compile the code before we time it.
            for (int i = 0; i < 30; i++) {
                sink += run(cells, false);
                sink += run(cells, true);
            }

            int runs = 50;
            long start = System.nanoTime();
            for (int i = 0; i < runs; i++) sink += run(cells, false);
            double seqMicros = (System.nanoTime() - start) / runs / 1_000.0;

            start = System.nanoTime();
            for (int i = 0; i < runs; i++) sink += run(cells, true);
            double parMicros = (System.nanoTime() - start) / runs / 1_000.0;

            System.out.printf("%-10d %-17.1f %.1f%n", n, seqMicros, parMicros);
        }
        System.out.println("(ignore) " + sink);
    }

    private static int run(List<Cell> cells, boolean parallel) {
        Stream<Cell> stream = parallel ? cells.parallelStream() : cells.stream();
        return stream.collect(SimulationStatsCollector.of(0, 0, 0)).getInfectedCount();
    }

    private static List<Cell> makeCells(int n) {
        CellState[] states = { NeutralState.INSTANCE, InfectedState.INSTANCE, AntivirusState.INSTANCE };
        Random random = new Random(1);
        List<Cell> cells = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            Cell c = new Cell(0, 0, i, states[random.nextInt(3)]);
            c.infectedThisWindow = random.nextBoolean();
            c.infectionsCaused = random.nextInt(5);
            cells.add(c);
        }
        return cells;
    }
}