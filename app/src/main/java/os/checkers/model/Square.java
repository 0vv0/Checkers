package os.checkers.model;


public class Square {
    private Color color;
    private Checker checker = null;
    private Coordinate coord;
    private Color upgradeToKing;

    public boolean isWhite() {
        return color == Color.White;
    }

    public boolean isBlack() {
        return color == Color.Black;
    }

    public boolean isEmpty() {
        return checker == null;
    }


    /*
    ** #coordinate for square on field, #color of square
     */
    public Square(Coordinate coordinate, Color color) {
        this.color = color;
        this.coord = coordinate;
    }

    /*
    * #coordinate for square on field, #color of square
    * #upgradeToKing - checkers of this color will be upgraded to King
    */
    public Square(Coordinate coordinate, Color color, Color upgradeToKing) {
        this.color = color;
        this.coord = coordinate;
        this.upgradeToKing = upgradeToKing;
    }

    public Color getColor() {
        return color;
    }

    public Checker getChecker() {
        return checker;
    }

    public boolean setChecker(Checker checker) {
        if (isEmpty()&&checker!=null) {
            if(upgradeToKing!=null&&checker.getColor().equals(upgradeToKing)){checker.upgrade();}
            this.checker = checker;
            return true;
        }
        return false;
    }

    public Checker clear() {
        Checker temp = checker;
        checker = null;
        return temp;
    }

    public boolean isNotEmpty() {
        return !isEmpty();
    }

    public Coordinate getCoord() {
        return coord;
    }
}
