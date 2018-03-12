package os.checkers.model;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;

public class Field extends Observable implements Serializable {
    private Square[][] field = new Square[Coordinate.MAX_LENGTH][Coordinate.MAX_LENGTH];
    private Color player = Color.White;

    private static Gson getGson(){
        return new GsonBuilder()
                .registerTypeAdapter(Coordinate.class, new InterfaceAdapter<Coordinate>())
                .setExclusionStrategies(new ExclusionStrategy() {
                    @Override
                    public boolean shouldSkipField(FieldAttributes f) {
                        return f.getDeclaringClass() == Observable.class;
                    }

                    @Override
                    public boolean shouldSkipClass(Class<?> clazz) {
                        return false;
                    }
                })
                .create();
    }

    public String getJson() {
        return getGson()
                .toJson(this);
    }

    public static Field fromJson(String serializedToString) {
        return getGson().fromJson(serializedToString, Field.class);
    }

    public Field() {
//        this.field = ;
//        player = Color.White;
        initFieldWithSquares();
        initFieldWithCheckers(0, 2, Color.White);
        initFieldWithCheckers(5, 7, Color.Black);
    }

    public Field(Field field){
        for (int i = 0; i <field.size() ; i++) {
            for (int j= 0; j<field.size();j++){
                this.field[i][j] = field.get(CoordinateImpl.get(i, j));
                this.player = field.getPlayer();
            }
        }
    }

    public Square get(Coordinate coordinate) {
        return field[coordinate.getRow()][coordinate.getColumn()];
    }

    private void initFieldWithSquares() {
        Color squareColorStartedCurrent = player;
        for (int i = 0; i < size(); i++) {
            squareColorStartedCurrent = squareColorStartedCurrent.getNext();
            for (int j = 0; j < size(); j++) {
                Color upgradeToKingThisColor;
                if (i == 0) {
//                    upgrade to King on this line for #player.getNext() color
                    upgradeToKingThisColor = player.getNext();
                } else if (i == size() - 1) {
//                    upgrade to King on this line for #player color
                    upgradeToKingThisColor = player;
                } else {
                    upgradeToKingThisColor = null;
                }

                field[i][j] = new Square(CoordinateImpl.get(i, j), squareColorStartedCurrent, upgradeToKingThisColor);
                squareColorStartedCurrent = squareColorStartedCurrent.getNext();

            }
        }
    }

    /**
     * rows from #startRow up to #endRow - with #color checkers
     **/
    private void initFieldWithCheckers(int startRow, int endRow, Color color) {
//        swap startRow and endRow if required
        if (startRow > endRow) {
            int i = startRow;
            startRow = endRow;
            endRow = i;
        }
        for (int i = startRow; i <= endRow; i++) {
            for (int j = 0; j < size(); j++) {
                if (i % 2 == j % 2) {
                    field[i][j].setChecker(new Checker(color));
                }
            }
        }
    }

    public int size() {
        return field.length;
    }

    public Color getPlayer() {
        return player;
    }

    //    @NotSafe - all checkes should be done before
    private void makeRealMove(Coordinate from, Coordinate to) {
        Checker checker = this.get(from).getChecker();//take checker
        clearInBetween(from, to);//clear all line
        this.get(to).setChecker(checker);//put checker onto onto target square
    }

    public boolean move(Coordinate from, Coordinate... to) {
        return new Steps(from, to).build();
    }

    public boolean move(List<Coordinate> coordinates) {
        boolean flag = new Steps(coordinates).build();
        if(flag) {notifyObservers();}
        return flag;
    }

    private void clearInBetween(final Coordinate from, final Coordinate to) {
        Coordinate iterator = from;
        while (iterator != null) {
            this.get(iterator).clear();
            iterator = iterator.getNextLookingTo(to);
        }
    }

    public boolean isAllowed(Coordinate from, Coordinate to) {
        return isAllowed(from, to, field[from.getRow()][from.getColumn()].getChecker());
    }

    public boolean isAllowed(Coordinate from, Coordinate to, Checker checker) {
        RuleSet rules = new RuleSet(from, to, checker);
        return rules.check();
    }

    public List<Coordinate> getAllowedMovesFor(final Coordinate from) {
        List<Coordinate> coordinates = from.getDiagonals();
//        coordinates.removeIf(coord->isAllowed(from, coord));
        for(Iterator<Coordinate> iter = coordinates.iterator(); iter.hasNext();){
            if(!isAllowed(from, iter.next())){iter.remove();}
        }
        return coordinates;
    }

    public boolean isSelectable(final Coordinate coordinate){
        return getAllowedMovesFor(coordinate).size()>0;
    }

    public boolean hasAllowedMoves(Coordinate coordinate, Checker checker) {

        return checker!=null && getAllowedMovesFor(coordinate, checker).size()>0;
    }

    private List<Coordinate> getAllowedMovesFor(Coordinate from, Checker checker) {
        List<Coordinate> coordinates = from.getDiagonals();
//        coordinates.removeIf(coord->isAllowed(from, coord));
        for(Iterator<Coordinate> iter = coordinates.iterator(); iter.hasNext();){
            if(!isAllowed(from, iter.next(), checker)){iter.remove();}
        }
        return coordinates;
    }

    private class RuleSet {
        private final int fromRow;
        private final int fromColumn;
        private final int toRow;
        private final int toColumn;
        private final Square fSquare;
        private final Square tSquare;
        private final Checker fChecker;

        RuleSet(Coordinate from, Coordinate to, Checker checker) {
            this.fromRow = from.getRow();
            this.fromColumn = from.getColumn();
            this.toRow = to.getRow();
            this.toColumn = to.getColumn();
//            System.out.println(from + "\n" + to);
            fSquare = field[fromRow][fromColumn];
            tSquare = field[toRow][toColumn];

            if (checker == null) {
                fChecker = fSquare.getChecker();//if Checker not set than use one from From cell. Can be null
            } else {
                if(fSquare.getChecker() == null || checker.equals(fSquare.getChecker())){//Checker given and From cell is emty or has given Checker
                    fChecker = checker;
                } else {
                    fChecker = null;
                }
            }
        }

        boolean check() {
            if (generalCheck()) {
                if (fChecker.isKing()) {
                    return notMoreThanOneForShot();
                } else if (distanceIs(1)) {
                    return (fChecker.equals(fSquare.getChecker())) && movingForvard();
                } else
                    return distanceIs(2) && distanceIs2();
            } else {
                return false;
            }
        }

        boolean notMoreThanOneForShot() {
            Coordinate nextNeigbour = fSquare.getCoord().getNextLookingTo(tSquare.getCoord());
            int count = 0;
            while (nextNeigbour != null) {
//                System.out.println(nextNeigbour);
                if (field[nextNeigbour.getRow()][nextNeigbour.getColumn()].isNotEmpty()) {
                    if (field[nextNeigbour.getRow()][nextNeigbour.getColumn()].getChecker().getColor() == fChecker.getColor().getNext()) {
                        if (++count > 1) {
                            return false;
                        }
                    } else {
                        return false;
                    }
                }
                nextNeigbour = nextNeigbour.getNextLookingTo(tSquare.getCoord());
            }
            return count < 2;
        }

        boolean distanceIs2() {
            Checker checkerInBetween = field[(fromRow + toRow) / 2][(fromColumn + toColumn) / 2].getChecker();
            //                field[(fromRow + toRow) / 2][(fromColumn + toColumn) / 2].clear();
            return distanceIs(2) && checkerInBetween != null && checkerInBetween.getColor() != fChecker.getColor();
        }

        boolean generalCheck() {
            return fChecker != null &&
                    player == fChecker.getColor() &&//our turn now
                    tSquare.getChecker() == null &&
                    theSameDiagonal();
        }

        boolean theSameDiagonal() {
            int i = Math.abs(fromRow - toRow);
            int j = Math.abs(fromColumn - toColumn);
            return i == j;
        }

        boolean distanceIs(int distance) {
            int i = Math.abs(fromRow - toRow);
            return i == distance;
        }

        boolean movingForvard() {
            if (fChecker.getColor() == Color.White) {
                return toRow > fromRow;
            } else {
                return toRow < fromRow;
            }
        }

    }

    private class Steps {
        private List<Coordinate> path = new ArrayList<>();

        Steps(List<Coordinate> coordinates) {
            if (coordinates == null || coordinates.size() < 2) {
                throw new IllegalArgumentException("Two or more coordinates required!");
            }
            path.addAll(coordinates);
        }

        Steps(Coordinate from, Coordinate... coordinates) {
            if (from == null) {
                throw new IllegalArgumentException("Start coordinate is null");
            } else if (coordinates == null) {
                throw new IllegalArgumentException("Coordinates are null");
            } else if (coordinates.length == 0) {
                throw new IllegalArgumentException("Coordinate is empty");
            }

            path.add(from);
            for (Coordinate coordinate : coordinates) {
                if (coordinate != null) {
                    path.add(coordinate);
                } else {
                    throw new IllegalArgumentException("Coordinate is null");
                }
            }
        }

        boolean build() {
            boolean allStepAreCorrect = true;
            for (int i = 0; i < path.size() - 1; i++) {
                if (!isAllowed(path.get(i), path.get(i + 1), field[path.get(0).getRow()][path.get(0).getColumn()].getChecker())) {
                    allStepAreCorrect = false;
                    break;
                }
            }
            if (allStepAreCorrect) {
                for (int i = 0; i < path.size() - 1; i++) {
                    makeRealMove(path.get(i), path.get(i + 1));
                }
                player = player.getNext();//rememmber next player turn
            }
            return allStepAreCorrect;
        }
    }

}
