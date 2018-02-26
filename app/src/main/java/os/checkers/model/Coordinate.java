package os.checkers.model;

import java.util.List;

public interface Coordinate {
    int MAX_LENGTH = 8;

    Coordinate getNextLookingTo(Coordinate coordinate);

    Coordinate getUpLeftNeighbor();

    Coordinate getUpRightNeighbor();

    Coordinate getDownLeftNeighbor();

    Coordinate getDownRightNeighbor();

    int getRow();
    int getColumn();

    int getMaxValue();

    List<Coordinate> getDiagonals();
}
