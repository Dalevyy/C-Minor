package interpreter.value;

import ast.AST;
import ast.types.ClassType;
import ast.types.Type;
import java.util.HashMap;

/**
 * A type representing an object the {@link interpreter.Interpreter} can interact with.
 * <p><br>
 *     Since C Minor is an object-oriented language, we need a way to represent objects
 *     during the runtime execution of a program. This class will help to store and
 *     access the internal data for an object via the {@link interpreter.Interpreter}.
 * </p>
 * @author Daniel Levy
 */
public class RuntimeObject extends Value {

    /**
     * Internal hash map that serves as the representation for an object.
     */
    private final HashMap<String,Value> obj;

    /**
     * Creates a {@link RuntimeObject} based on a passed type.
     * @param objType {@link Type}
     */
    public RuntimeObject(Type objType) {
        super(null,objType);
        this.obj = new HashMap<>();
    }

    /**
     * Adds either a new field or updates an existing field's value.
     * @param fieldName Field we want to add/update
     * @param val {@link Value} the field will store
     */
    public void setField(String fieldName, Value val) { obj.put(fieldName,val); }

    /**
     * See {@link #setField(String,Value)}.
     */
    public void setField(AST node, Value val) { setField(node.toString(),val); }

    /**
     * Retrieves a field value from the current object.
     * @param fieldName Field we want to access
     * @return {@link Value}
     */
    public Value getField(String fieldName) { return obj.get(fieldName); }

    /**
     * See {@link #getField(String)}.
     */
    public Value getField(AST node) { return this.getField(node.toString()); }

    /**
     * Checks if the current object has a field.
     * @param fieldName Field we want to find
     * @return Boolean
     */
    public boolean hasField(String fieldName) { return obj.get(fieldName) != null; }

    /**
     * See {@link #hasField(String)}.
     */
    public boolean hasField(AST node) { return this.hasField(node.toString()); }

    /**
     * Retrieves the current type of the object.
     * @return {@link ClassType}
     */
    public ClassType getCurrentType() {
        return type.isMultiType() ? type.asMultiType().getRuntimeType() : type.asClassType();
    }

    /**
     * Sets the runtime type of the object.
     * @param ct {@link ClassType}
     */
    public void setType(ClassType ct) { this.type = ct; }

    /**
     * {@inheritDoc}
     * @return Boolean
     */
    public boolean isObject() { return true; }

    /**
     * {@inheritDoc}
     * @return {@link RuntimeObject}
     */
    public RuntimeObject asObject() { return this; }
}
