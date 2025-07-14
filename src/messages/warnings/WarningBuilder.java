package messages.warnings;

import ast.AST;
import messages.MessageType;

public class WarningBuilder {

    private final Warning warning;

    public WarningBuilder(WarningFactory wf) { this.warning = wf.createWarning(); }

    public WarningBuilder(WarningFactory wf, String file) {
        this.warning = wf.createWarning();
        this.warning.setFileName(file);
    }

    public String warning() { return this.warning.printMessage(); }

    public WarningBuilder addLocation(AST node) {
        this.warning.setLocation(node);
        return this;
    }

    public WarningBuilder addWarningType(MessageType wt) {
        this.warning.setWarningType(wt);
        return this;
    }

    public WarningBuilder addArgs(Object... args) {
        this.warning.setArgs(args);
        return this;
    }
}
