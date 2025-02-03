package token;

public class Token {

    private TokenType type;
    private String text;
    private Location location;

    public Token(TokenType type, String text, Location location) {
        this.type = type;
        this.text = text;
        this.location = location;
    }

    public void setText(String s) { this.text = s; }

    public TokenType getTokenType() { return type; }
    public String getText() { return text; }
    public Location getLocation() { return location; }
    public Position getStartPos() { return location.start.copy(); }
    public Position getEndPos() { return location.end.copy(); }

    public Token copy() { return new Token(type,text,location); }

    public void appendText(String txt) { this.text += txt; }
    public void newEndLocation(Position end) { this.location.end = end; }
    
    @Override
    public String toString() {
        return "[ " + getTokenType() + ", " + getText() + " @ " + location.toString() + " ]";
    }
}
