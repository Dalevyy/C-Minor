package messages.warnings;

import ast.AST;
import messages.MessageType;

public class WarningBuilder {

    private final Warning warning;

    public WarningBuilder(WarningFactory wf, boolean mode) {
        this.warning = wf.createWarning();
        this.warning.setInterpretMode(mode);
    }

    public WarningBuilder(WarningFactory wf, String file, boolean mode) {
        this(wf,mode);
        this.warning.setFileName(file);
    }

    public String warning() { return this.warning.printMessage(); }

    public Warning create() { return this.warning; }

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
