package messages.errors;

public enum ErrorType {
    REDECL,                 // Redeclaring variable in the same scope
    SELF_ASSIGN,            // Declaring and assigning a variable to itself
    NO_DECL,                // Missing declaration
    NO_CLASS_DECL,          // Class has not been declared
    MISSING_FIELD,          // Field was not defined in calss
    FIELD_VAL_GIVEN,        // Field was already given a value in a NewExpr
    INHERIT_SELF,           // Class tries to inherit itself

    // Type Checking Errors
    LOCAL_DECL_TYPE_DOES_NOT_MATCH_INIT_EXPR,
    GLOBAL_DECL_TYPE_DOES_NOT_MATCH_INIT_EXPR,
    ASSIGN_STMT_TYPE_DOES_NOT_MATCH,
    ASSIGN_STMT_INVALID_TYPES_USED,

    BIN_EXPR_NOT_ASSIGNCOMP,
    BIN_EXPR_NOT_NUMERIC,
    BIN_EXPR_SLEFT_SRIGHT_NOT_INT,
    BIN_EXPR_BITWISE_NOT_DISCRETE,
    BIN_EXPR_LOGICAL_NOT_BOOL,
    BIN_EXPR_OBJ_OPS_MISSING_OBJ,

    WHILE_CONDITION_NOT_BOOLEAN
}
