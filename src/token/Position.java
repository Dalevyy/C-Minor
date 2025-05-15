package token;

// Keeps track of Position for a token
public class Position implements Comparable<Position> {

    public int line;
    public int column;

    public Position() { line = column = 1; }

    public Position(int line, int column) {
        this.line = line;
        this.column = column;
    }

    public void addToLine() {
        line += 1;
        column = 1;
    }

    public void addToCol() { column += 1; }

    public Position copy() { return new Position(line, column); }

    @Override
    public String toString() { return this.line + "." + this.column; }

    @Override
    public int compareTo(Position other) {
        return this.equals(other) ? 1 : 0;
    }

    public boolean equals(Position other) {
        return this.line == other.line && this.column == other.column;
    }

}
