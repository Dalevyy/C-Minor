package token;

// Keeps track of a starting and ending position for a token
public class Location {

    public Position start;
    public Position end;

    public Location() {
        this.start = new Position(1,1);
        this.end = new Position(1,1);
    }

    public Location(Position start, Position end) {
        this.start = start;
        this.end = end;
    }

    public void addLine()   { end.addToLine(); }
    public void addCol()    { end.addToCol(); }
    public void removeCol() { end.column -= 1; }

    public void resetStart() {
        start.line = end.line;
        start.column = end.column;
    }

    public Location copy() {
        Location loc = new Location(start.copy(), end.copy());
        this.start.column = this.end.column;
        return loc;
    }

    @Override
    public String toString() {
        return start.toString() + " to " + end.toString();
    }
}
