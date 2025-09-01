package cminor.interpreter.value;

import cminor.ast.expressions.Literal;
import cminor.ast.types.Type;
import cminor.ast.types.VoidType;
import cminor.interpreter.Interpreter;

import java.math.BigDecimal;

/**
 * A type that represents a value during the runtime execution of the {@link Interpreter}.
 * <p><br>
 *     This class is designed to create values that will be stored on the {@link utilities.RuntimeStack}
 *     during the execution of the {@link Interpreter}. It is mainly designed to help reduce
 *     the amount of type casting and extra variables needed for the interpreter to properly execute.
 * </p>
 * @author Daniel Levy
 */
public class Value {

    /**
     * The object that the value is representing.
     */
    private Object val;

    /**
     * The type of the {@link #val}.
     */
    protected Type type;

    /**
     * Creates a default {@link Value} object.
     */
    public Value() {
        this.val = null;
        this.type = new VoidType();
    }

    /**
     * Creates a {@link Value} object based on a passed object and type.
     * @param val Object that the value is representing
     * @param type {@link Type} of the object
     */
    public Value(Object val, Type type) {
        this.val = val;
        this.type = type;
    }

    /**
     * Creates a {@link Value} object based on a {@link Literal}.
     * @param li Literal
     */
    public Value(Literal li) {
        switch(li.getConstantKind()) {
            case INT:
                val = Integer.parseInt(li.text);
                break;
            case CHAR:
                if(!li.text.isEmpty())
                    val = li.text.charAt(1) == '\\' ? (char) ('\\' + li.text.charAt(2)) : li.text.charAt(1);
                break;
            case BOOL:
                val = Boolean.parseBoolean(li.text);
                break;
            case REAL:
                val = new BigDecimal(li.text);
                break;
            case STR:
                if(!li.text.isEmpty())
                    val = li.text.substring(1,li.text.length()-1);
                break;
            case ENUM:
                if(li.type.isInt())
                    val = Integer.parseInt(li.text);
                else
                    val = li.text.charAt(1) == '\\' ? (char) ('\\' + li.text.charAt(2)) : li.text.charAt(1);
                break;
        }
        this.type = li.type;
    }

    /**
     * Returns the type of the current {@link Value}.
     * @return Type
     */
    public Type getType() { return this.type; }

    /**
     * Checks if the current {@link Value} represents a {@link RuntimeObject}.
     * @return Boolean
     */
    public boolean isObject() { return false; }

    /**
     * Checks if the current {@link Value} represents a {@link RuntimeList}.
     * @return Boolean
     */
    public boolean isList() { return false; }

    /**
     * Returns the current {@link Value} as an int.
     * @return Int
     */
    public int asInt() { return (int) val; }

    /**
     * Returns the current {@link Value} as a char.
     * @return Char
     */
    public char asChar() { return (char) val; }

    /**
     * Returns the current {@link Value} as a bool.
     * @return Boolean
     */
    public boolean asBool() { return (boolean) val; }

    /**
     * Returns the current {@link Value} as a real.
     * @return {@link BigDecimal}
     */
    public BigDecimal asReal() { return (BigDecimal) val; }

    /**
     * Returns the current {@link Value} as a string.
     * @return String
     */
    public String asString() { return (String) val; }

    /**
     * Returns the current {@link Value} as a {@link RuntimeObject}.
     * @return {@link RuntimeObject}
     */
    public RuntimeObject asObject() {
        throw new RuntimeException("The current Value does not represent an object and can not be typecasted.");
    }

    /**
     * Returns the current {@link Value} as a {@link RuntimeList}.
     * @return {@link RuntimeList}
     */
    public RuntimeList asList() {
        throw new RuntimeException("The current Value does not represent a list and can not be typecasted.");
    }

    /**
     * Checks if two instances of {@link Value} are equal.
     * <p>
     *     This method will be called by the {@code ==} and {@code !=}
     *     operators in the {@link Interpreter} to deduce
     *     whether two values are equal. We will return the result
     *     to the {@link Interpreter}, so it can save the value.
     * </p>
     * @param RHS {@link Value} that we are checking equality for
     * @return Boolean
     */
    public boolean equals(Value RHS) {
        if(!this.type.equals(RHS.type))
            return false;
        else if(RHS.type.isInt())
            return this.asInt() == RHS.asInt();
        else if(RHS.type.isChar())
            return this.asChar() == RHS.asChar();
        else if(RHS.type.isBool())
            return this.asBool() == RHS.asBool();
        else if(RHS.type.isReal())
            return this.asReal().compareTo(RHS.asReal()) == 0;
        else if(RHS.type.isString())
            return this.asString().equals(RHS.asString());
        else if(RHS.type.isList()) {
            if(!this.isList() || this.asList().size() != RHS.asList().size())
                return false;

            for(int i = 1; i <= RHS.asList().size(); i++)
                if(!this.asList().get(i).equals(RHS.asList().get(i)))
                    return false;

            return true;
        }
        else
            return false;
    }

    /**
     * {@code toString} method.
     * @return Returns the string representation of the current value
     */
    @Override
    public String toString() { return val.toString(); }
}
