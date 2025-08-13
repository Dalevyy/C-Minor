package cminor.utilities;

/**
 * An enum that keeps of all semantic passes that the compiler executes after syntax analysis.
 * <p>
 *     This enum serves more as a helper for the {@link PhaseHandler}, so we can assign a
 *     number to each pass. This just future proofs the {@link PhaseHandler} if we decide
 *     to add or remove passes.
 * </p>
 * @author Daniel Levy
 */
public enum PhaseNumber {

    PRINTER,
    SEMANTIC_ANALYZER,
    PROPERTY_GENERATOR,
    NAME_CHECKER;
}
