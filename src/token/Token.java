package token;

public class Token {

    private final TokenType type;
    private String text;
    private final Location location;

    public Token() { this(TokenType.ID,"",new Location()); }
    public Token(String txt) { this(TokenType.ID,txt,new Location()); }
    public Token(String txt, Location location) { this(TokenType.ID,txt,location);}
    public Token(TokenType type, String text, Location location) {
        this.type = type;
        this.text = text;
        this.location = location;
    }

    public TokenType getTokenType() { return type; }

    public void setText(String s) { this.text = s; }
    public void appendText(String s) { this.text += s; }
    public String getText() { return text; }

    public Location getLocation() { return location; }
    public Position getStartPos() { return location.start.copy(); }
    public Position getEndPos() { return location.end.copy(); }

    public Token copy() { return new Token(type,text,location); }

    public void setStartLocation(Position start) { this.location.start = start; }
    public void setEndLocation(Position end) { this.location.end = end; }

    public boolean equals(String lexeme) { return this.text.equals(lexeme); }
    
    @Override
    public String toString() {
        return "[ " + getTokenType() + ", '" + getText() + "' @ " + location.toString() + " ]";
    }
}
