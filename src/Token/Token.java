package Token;

public class Token {

    private TokenType type;
    private String text;
    private Location location;

    public Token(TokenType type, String text, Location location) {
        this.type = type;
        this.text = text;
        this.location = location;
    }

    public TokenType getTokenType() { return type; }
    public String getText() { return text; }
    public Location getLocation() { return location; }

    public void appendText(String txt) { this.text += txt; }
    public void newEndLocation(Location end) { this.location.end = end.end; }
    
    @Override
    public String toString() {
        return "[ " + getTokenType() + ", " + getText() + " @ " + location.toString() + " ]";
    }
}
