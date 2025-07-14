package messages.errors;

import messages.errors.mod.ModError;
import messages.errors.runtime.RuntimeError;
import messages.errors.scope.ScopeError;
import messages.errors.semantic.SemanticError;
import messages.errors.syntax.SyntaxError;
import messages.errors.type.TypeError;
import messages.Message;
import messages.MessageType;
import utilities.PrettyPrint;

public abstract class Error extends Message {

    protected MessageType error;
    protected MessageType suggest;

    public abstract String header();

    protected String errorNumber() {
        return error.toString().substring(error.toString().lastIndexOf("_")+1);
    }

    public String createMessage() {
        StringBuilder sb = new StringBuilder();

        sb.append(header());
        if(location != null)
            sb.append(location.header());

        sb.append(buildError());
        if(suggest != null)
            sb.append("\n").append(buildSuggestion());

        return sb.toString();
    }

    public String printMessage() {
        if(interpretMode) {
            System.out.println(createMessage());
            throw new RuntimeException("Compile Error Found.");
        }
        else
            return createMessage();
    }

    private String buildError() {
        String errorMsg = PrettyPrint.RED + this.error.getMessage() + PrettyPrint.RESET;
        if(this.args != null) {
            for(int i = 0; i < this.args.length; i++) {
                String arg = "<arg" + i + ">";
                errorMsg = errorMsg.replace(arg,this.args[i].toString());
            }
        }
        return errorMsg;
    }

    private String buildSuggestion() {
        String suggestMsg = "\nSuggestion:\n" + PrettyPrint.RED + this.suggest.getMessage() + PrettyPrint.RESET;
        if(this.argsForSuggestions != null) {
            for(int i = 0; i < this.argsForSuggestions.length; i++) {
                String arg = "<arg" + i + ">";
                suggestMsg = suggestMsg.replace(arg,this.argsForSuggestions[i].toString());
            }
        }
        return suggestMsg;
    }

    public void setErrorType(MessageType et) { error = et; }
    public MessageType errorType() { return error; }

    public void setSuggestType(MessageType et) { suggest = et; }
    public MessageType suggestType() { return suggest; }

    // Helper Methods
    public boolean isModifierError() { return false; }
    public ModError asModifierError() { throw new RuntimeException("The following error does not represent a modifier error"); }

    public boolean isRuntimeError() { return false; }
    public RuntimeError asRuntimeError() { throw new RuntimeException("The following error does not represent a runtime error"); }

    public boolean isScopeError() { return false; }
    public ScopeError asScopeError() { throw new RuntimeException("The following error does not represent a scope error"); }

    public boolean isSemanticError() { return false; }
    public SemanticError asSemanticError() { throw new RuntimeException("The following error does not represent a semantic error"); }

    public boolean isSyntaxError() { return false; }
    public SyntaxError asSyntaxError() { throw new RuntimeException("The following error does not represent a syntax error"); }

    public boolean isTypeError() { return false; }
    public TypeError asTypeError() { throw new RuntimeException("The following error does not represent a type error"); }
}
