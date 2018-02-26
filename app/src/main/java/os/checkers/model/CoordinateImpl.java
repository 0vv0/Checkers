package os.checkers.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CoordinateImpl implements Coordinate {
    private static Map<Integer, CoordinateImpl> list = new HashMap<>();

    @Override
    public int getRow() {
        return row;
    }

    @Override
    public int getColumn() {
        return column;
    }

    @Override
    public int getMaxValue() {
        return MAX_LENGTH;
    }

    @Override
    public List<Coordinate> getDiagonals() {
        List<Coordinate> coordinates = new ArrayList<>();
        Coordinate c = this;
        while ((c = c.getDownLeftNeighbor()) !=null){
            coordinates.add(c);
        }
        c = this;
        while ((c = c.getDownRightNeighbor()) !=null){
            coordinates.add(c);
        }
        c = this;
        while ((c = c.getUpLeftNeighbor()) !=null){
            coordinates.add(c);
        }
        c = this;
        while ((c = c.getUpRightNeighbor()) !=null){
            coordinates.add(c);
        }
        return coordinates;
    }

    private final int row;
    private final int column;

    private CoordinateImpl(int row, int column) {
        this.row = row;
        this.column = column;
        list.put(this.hashCode(), this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Coordinate)) return false;

        Coordinate that = (Coordinate) o;

        return row == that.getRow() && column == that.getColumn();
    }

    @Override
    public int hashCode() {
        return hash(row, column);
    }

    private static int hash(int i, int j) {
        int result = i;
        result = 31 * result + j;
        return result;
    }

    public static CoordinateImpl get(int row, int column) {
        if (!list.containsKey(hash(row, column))) {
            CoordinateImpl coordinate = new CoordinateImpl(row, column);
            list.put(coordinate.hashCode(), coordinate);
        }
        return list.get(hash(row, column));
    }

    public static CoordinateImpl get(int hash) {
        return get(hash / 31, hash % 31);
    }

    public static void clean() {
        list.clear();
    }

    @Override
    public Coordinate getNextLookingTo(Coordinate coordinate) {
        if (this.row == coordinate.getRow() || this.column == coordinate.getColumn()) {
            return null;
        }
        int rowSign = this.row < coordinate.getRow() ? 1 : -1;
        int columnSign = this.column < coordinate.getColumn() ? 1 : -1;
        return CoordinateImpl.get(this.row + rowSign, this.column + columnSign);
    }

    @Override
    public Coordinate getUpLeftNeighbor() {
        if (this.row == 0 || this.column == 0) {
            return null;
        }
        return CoordinateImpl.get(this.row - 1, this.column - 1);
    }

    @Override
    public Coordinate getUpRightNeighbor() {
        if (this.row == 0 || this.column == MAX_LENGTH - 1) {
            return null;
        }
        return CoordinateImpl.get(this.row - 1, this.column + 1);
    }

    @Override
    public Coordinate getDownLeftNeighbor() {
        if (this.row == MAX_LENGTH - 1 || this.column == 0) {
            return null;
        }
        return CoordinateImpl.get(this.row + 1, this.column - 1);
    }

    @Override
    public Coordinate getDownRightNeighbor() {
        if (this.row == MAX_LENGTH - 1 || this.column == MAX_LENGTH - 1) {
            return null;
        }
        return CoordinateImpl.get(this.row + 1, this.column + 1);
    }

    @Override
    public String toString() {
        return "Coordinate{" +
                "row=" + row +
                ", column=" + column +
                '}';
    }
}
