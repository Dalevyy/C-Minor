package utilities;

import ast.AST;
import interpreter.value.Value;
import java.util.HashMap;

/**
 * A class that mimics the behavior of a system stack.
 * <p><br>
 *     Since C Minor has an interpretation mode, we need to mimic the
 *     behavior of a real system stack when users execute programs through
 *     the {@link interpreter.VM}. This class is similar to the {@link SymbolTable}
 *     class, but it is designed to work with the {@link interpreter.Interpreter} in
 *     order to make it easier to access and save values during runtime.
 * </p>
 * @author Daniel Levy
 */
public class RuntimeStack {

    /**
     * Internal hash map that will act as our stack.
     */
    private final HashMap<String, Value> stack;

    /**
     * Previous call frame that the current call frame originates from.
     */
    private RuntimeStack parent;

    /**
     * Creates a default {@link RuntimeStack} object.
     */
    public RuntimeStack() {
        this.stack = new HashMap<>();
        this.parent = null;
    }

    /**
     * Creates a {@link RuntimeStack} object that acts as a new call frame.
     * @param parent {@link RuntimeStack} that the call frame points to
     */
    public RuntimeStack(RuntimeStack parent) {
        this();
        this.parent = parent;
    }

    /**
     * Checks if the stack is storing a variable.
     * @param name Variable name
     * @return Boolean
     */
    private boolean hasValue(String name) {
        if(stack.containsKey(name))
            return true;
        else if(parent != null)
            return parent.hasValue(name);
        else
            return false;
    }

    /**
     * Adds a new value to the stack based on a variable name.
     * @param name Variable name
     * @param val {@link Value}
     */
    public void addValue(String name, Value val) { stack.put(name,val); }

    /**
     * See {@link #addValue(String,Value)}.
     */
    public void addValue(AST node, Value val) { addValue(node.toString(),val); }

    /**
     * Sets the value for a variable on the stack.
     * @param name Variable name we want to set a new value for
     * @param value {@link Value} to save
     */
    public void setValue(String name, Value value) {
        if(stack.containsKey(name))
            stack.put(name,value);
        else if(parent != null)
            parent.setValue(name,value);
    }

    /**
     * See {@link #setValue(String, Value)}.
     */
    public void setValue(AST node, Value value) { setValue(node.toString(),value); }

    /**
     * Retrieves a value from the stack.
     * @param name String representing the name we want to get a value from
     * @return {@link Value}
     */
    public Value getValue(String name) {
        if(stack.containsKey(name))
            return stack.get(name);
        else if(parent != null)
            return parent.getValue(name);
        else
            return null;
    }

    /**
     * See {@link #getValue(String)}.
     */
    public Value getValue(AST node) { return this.getValue(node.toString()); }

    /**
     * Creates a new stack call frame.
     * @return {@link RuntimeStack}
     */
    public RuntimeStack createCallFrame() { return new RuntimeStack(this);}

    /**
     * Destroys the current stack call frame.
     * @return {@link RuntimeStack} representing the parent call frame
     */
    public RuntimeStack destroyCallFrame() { return this.parent; }
}
