package parser;

import lexer.Lexer;
import token.Token;
import token.TokenType;
import java.util.Arrays;
import java.util.ArrayList;

public class PEG {
    private final Lexer input;
    private int currPos;
    private final ArrayList<Integer> positions;
    private final ArrayList<Token> lookaheads;

    public PEG(Lexer input) {
        this.input = input;
        this.currPos = 0;
        this.positions = new ArrayList<Integer>();
        this.lookaheads = new ArrayList<Token>();
    }

    // This method adds more tokens to the lookaheads array. This
    // will occur if the index we are trying to check for is crea

    void populateLA(int index) {
        if(currPos + index > lookaheads.size()) {
            int countLA = currPos+index - lookaheads.size();
            for(int i = 0; i < countLA; i++)
                lookaheads.add(input.nextToken());
        }
    }

    void consume() {
        currPos++;
        if(currPos == lookaheads.size()) {  // && !expects()
            currPos = 0;
            lookaheads.clear();
        }
        populateLA(0);
    }

    private Token LA() {
        populateLA(0);
        return lookaheads.get(currPos);
    }

    // mark
    // release
    // seek
    // expects

    private Token LA(int index) {
        populateLA(index);
        return lookaheads.get(currPos+index);
    }

    /*
        The 'check' methods determine whether or not the current lookahead
        matches the token we are expecting to see.
    */
    private boolean check(TokenType expectedType) { return LA().getTokenType() == expectedType; }
    private boolean check(TokenType... expectedTypes) {
        for(TokenType currType : expectedTypes) {
            if(check(currType)) return true;
        }
        return false;
    }

    /*
        The 'match' methods will operate the same way as the 'check' methods, but
        if the current lookahead matches the expected token, we will consume the
        lookahead.
    */
    private boolean match(TokenType expectedType) {
        if(check(expectedType)) {
            consume();
            return true;
        }
        return false;
    }

    private boolean match(TokenType... expectedTypes) {
        if(check(expectedTypes)) {
            consume();
            return true;
        }
        return false;
    }

    // 1. compilation ::= file_merge* enum_type* global_variable* class_type* function* main
    public void compilation() {
        
    }

}
