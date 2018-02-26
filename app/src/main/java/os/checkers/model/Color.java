package os.checkers.model;

public enum Color {
    White, Black;
    public Color getNext(){
        if(this==White){return Black;} else {return White;}
    }
}
