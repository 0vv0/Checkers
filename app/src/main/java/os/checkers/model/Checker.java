package os.checkers.model;

public class Checker {


    public boolean isKing() {
        return type==Type.King;
    }

    public enum Type {
        Checker, King;
    }

    private Color color;
    private Type type = Type.Checker;

    public Checker(Color color) {
        this.color = color;
    }

    public void upgrade() {
        if (type == Type.Checker) {
            type = Type.King;
        } else {

        }
    }

    public Color getColor() {
        return color;
    }

    public Type getType() {
        return type;
    }

    public boolean isWhite() {
        return color == Color.White;
    }

    public boolean isBlack() {
        return color == Color.Black;
    }

}
