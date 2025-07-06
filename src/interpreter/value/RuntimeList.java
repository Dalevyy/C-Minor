package interpreter.value;

import ast.expressions.Literal;
import ast.types.ArrayType;
import ast.types.ListType;
import utilities.Vector;

/**
 * A type representing a list that the {@link interpreter.Interpreter} can interact with.
 * <p><br>
 *     Since C Minor natively supports continuous memory structures, this class helps
 *     with internally keeping track of an array or list for the interpreter. We will
 *     mimic the behavior of these structures with a row-based {@link Vector}.
 * </p>
 * @author Daniel Levy
 */
public class RuntimeList extends Value {

    /**
     * Array or list we are internally storing.
     */
    private final Vector<Value> arr;

    /**
     * An {@link ast.expressions.ArrayLiteral} or {@link ast.expressions.ListLiteral} storing the list's metadata.
     */
    private final Literal metaData;

    /**
     * Internal offset that will be updated when we need to update a value inside of {@link #arr}
     */
    private int offset;

    /**
     * Creates a default {@link RuntimeList}.
     * @param metaData {@link ast.expressions.ArrayLiteral} or {@link ast.expressions.ListLiteral}
     */
    public RuntimeList(Literal metaData) {
        super(metaData);
        // Start list with one element to make index calculations easier to do.
        this.arr = new Vector<>(new Value());
        this.metaData = metaData;
        this.offset = -1;
        this.type = this.metaData.getConstantKind().equals(Literal.ConstantType.ARR) ? new ArrayType() : new ListType();
    }

    /**
     * Returns the size of the current {@link #arr}.
     * @return Int
     */
    public int size() { return arr.size()-1; }

    /**
     * Adds a value to the list.
     * <p><br>
     *     If the internal {@link #offset} is set, then that means we are inside of
     *     an {@link ast.statements.AssignStmt}, so we want to internally change a
     *     value the list is storing. If it isn't set, then we simply append the value
     *     to the list.
     * </p>
     * @param val {@link Value}
     */
    public void addElement(Value val) {
        if(offset != -1) {
            arr.set(offset,val);
            offset = -1;
        }
        else
            arr.add(val);
    }

    /**
     * Adds a value to a specific position in the list.
     * @param offset Position we want to add a value to.
     * @param val {@link Value}
     */
    public void addElement(int offset, Value val) { arr.add(offset,val); }

    /**
     * Retrieves a value from the list based on the current offset.
     * @param offset List position we want to access
     * @return {@link Value}
     */
    public Value get(int offset) { return arr.get(offset); }

    /**
     * Retrieves either a {@link ast.expressions.ArrayLiteral} or {@link ast.expressions.ListLiteral}.
     * <p><br>
     *     This represents the internal meta data for the current {@link RuntimeList}
     *     which is needed when we are calculating indices for an {@link ast.expressions.ArrayExpr}.
     * </p>
     * @return {@link ast.expressions.ArrayLiteral} or {@link ast.expressions.ListLiteral}
     */
    public Literal getMetaData() { return this.metaData; }

    /**
     * Sets an internal offset value (only applicable for {@link ast.statements.AssignStmt}.
     * @param offset Position we want to access in the list
     */
    public void setOffset(int offset) { this.offset = offset; }

    /**
     * Checks if the current {@link RuntimeList} represents an array.
     * @return Boolean
     */
    public boolean isArray() { return type.isArray(); }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    public boolean isList() { return true; }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    public RuntimeList asList() { return this; }

    /**
     * Builds a string representation of the current {@link RuntimeList}.
     * <p><br>
     *     To avoid users needing to create for loops to view individual
     *     contents of an array or list, C Minor will allow a user to
     *     output the array or list directly in order to see their values.
     * </p>
     * @param lst Current list we are building a string for
     * @param sb String builder
     */
    public static void buildList(RuntimeList lst, StringBuilder sb) {
        sb.append("[");
        for(int i = 1; i <= lst.size(); i++) {
            if(lst.get(i).isList())
                buildList(lst.get(i).asList(),sb);
            else
                sb.append(lst.get(i));
            if(i != lst.size())
                sb.append(", ");
        }
        sb.append("]");
    }
}
