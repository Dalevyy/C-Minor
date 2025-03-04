package ast;

// TODO: Error out when there's a double final, pure, or recurs (needed for methods)

/*
    -------------------------------------------------------------------------
                                    Modifiers
    -------------------------------------------------------------------------

    This is a helper class that is not part of a C Minor AST. Modifiers will
    take a single Modifier and/or Modifiers and set the appropriate boolean
    flag to be true. This will aid us when we are performing modifier checking
    alongside code generation.
*/
public class Modifiers {

    private boolean nodeIsPublic;
    private boolean nodeIsProtected;
    private boolean nodeIsProperty;
    private boolean nodeIsFinal;
    private boolean nodeIsAbstract;
    private boolean nodeIsPure;
    private boolean nodeIsRecurs;
    private boolean nodeIsIn;
    private boolean nodeIsOut;
    private boolean nodeIsInOut;
    private boolean nodeIsRef;

    public Modifiers(Modifier mod) { if(mod != null) setModifier(mod); }

    public Modifiers(Vector<Modifier> mods) {
        for(int i = 0; i < mods.size(); i++)
            setModifier(mods.get(i));
    }

    public void setModifier(Modifier m) {
        switch(m.toString()) {
            case "Public":
                nodeIsPublic = true;
                break;
            case "Protected":
                nodeIsProtected = true;
                break;
            case "Property":
                nodeIsProperty = true;
                break;
            case "Final":
                nodeIsFinal = true;
                break;
            case "Abstract":
                nodeIsAbstract = true;
                break;
            case "Pure":
                nodeIsPure = true;
                break;
            case "Recursive":
                nodeIsRecurs = true;
                break;
            case "In":
                nodeIsIn = true;
                break;
            case "Out":
                nodeIsOut = true;
                break;
            case "Inout":
                nodeIsInOut = true;
                break;
            case "Ref":
                nodeIsRef = true;
                break;
        }
    }

    public boolean isPublic() { return nodeIsPublic; }
    public boolean isProtected() { return nodeIsProtected; }
    public boolean isProperty() { return nodeIsProperty; }
    public boolean isFinal() { return nodeIsFinal; }
    public boolean isAbstract() { return nodeIsAbstract; }
    public boolean isPure() { return nodeIsPure; }
    public boolean isRecurs() { return nodeIsRecurs; }
    public boolean isIn() { return nodeIsIn; }
    public boolean isOut() { return nodeIsOut; }
    public boolean isInOut() { return nodeIsInOut; }
    public boolean isRef() { return nodeIsRef; }
}
