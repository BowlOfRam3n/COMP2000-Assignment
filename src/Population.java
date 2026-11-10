import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * The collection of cells in the simulation.
 * Hides the underlying storage and offers different ways to traverse it
 * (Iterator pattern)
 * The whole population, or every unordered pair.
 */
public class Population implements Iterable<Cell> {
    private final List<Cell> cells = new ArrayList<>();

    // Collection API

    public void add(Cell cell) {
        cells.add(cell);
    }

    public void addAll(Collection<Cell> newCells) {
        cells.addAll(newCells);
    }

    public void removeAll(Collection<Cell> dead) {
        cells.removeAll(dead);
    }

    public void clear() {
        cells.clear();
    }

    public int size() {
        return cells.size();
    }

    public boolean isEmpty() {
        return cells.isEmpty();
    }

    // Iteration

    /** Iterates every cell. The iterator supports remove(). */
    @Override
    public Iterator<Cell> iterator() {
        return cells.iterator();
    }

    /** Every unordered pair of cells exactly once: (0,1) (0,2) ... (n-2,n-1). */
    public Iterable<CellPair> collisionPairs() {
        return () -> new PairIterator();
    }

    /** Only the cells currently in the given state. */
    public Iterable<Cell> ofState(CellState state) {
        return () -> new StateIterator(state);
    }

    private class StateIterator implements Iterator<Cell> {
        private final CellState wanted;
        private final Iterator<Cell> source = cells.iterator();
        private Cell nextMatch; // look-ahead: the next cell that matches, or null

        StateIterator(CellState wanted) {
            this.wanted = wanted;
            advance();
        }

        private void advance() {
            nextMatch = null;
            while (source.hasNext()) {
                Cell c = source.next();
                if (c.getState() == wanted) {
                    nextMatch = c;
                    return;
                }
            }
        }

        @Override
        public boolean hasNext() {
            return nextMatch != null;
        }

        @Override
        public Cell next() {
            if (nextMatch == null) {
                throw new NoSuchElementException();
            }
            Cell result = nextMatch;
            advance();
            return result;
        }
    }

    private class PairIterator implements Iterator<CellPair> {
        private final int expectedSize = cells.size(); // fail-fast check
        private int i = 0;
        private int j = 1;

        @Override
        public boolean hasNext() {
            return i < cells.size() - 1;
        }

        @Override
        public CellPair next() {
            if (cells.size() != expectedSize) {
                throw new ConcurrentModificationException("Population changed during pair iteration");
            }
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            CellPair pair = new CellPair(cells.get(i), cells.get(j));

            j++;
            if (j >= cells.size()) { // finished row i, start the next row
                i++;
                j = i + 1;
            }
            return pair;
        }
    }
}