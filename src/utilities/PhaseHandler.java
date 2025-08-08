package utilities;

import ast.AST;
import messages.CompilationMessage;
import messages.MessageHandler;
import messages.MessageNumber;
import messages.errors.setting.SettingError;

/**
 * A class designed to store and execute all compiler phases.
 * <p>
 *     This class will keep track of all compiler phases that need to be executed.
 *     The {@link compiler.Compiler} and {@link interpreter.VM} will set this object
 *     up, and they will interface with it in order to generate the correct configuration
 *     for each compilation mode.
 * </p>
 * @author Daniel Levy
 */
public class PhaseHandler {

    /**
     * {@link Vector} containing every phase that will be executed.
     */
    private final Vector<Visitor> allPhases;

    /**
     * {@link MessageHandler} that will deal with any compiler-related command errors.
     */
    private final MessageHandler msgHandler;

    /**
     * An optional number that denotes which phase the compilation process should stop at.
     * <p>
     *     This will be used for debugging purposes. I do not anticipate a user would ever
     *     want to siphon off the compilation process unless they are studying how a
     *     compiler works.
     * </p>
     */
    private int finalPhaseToExecute = -1;

    /**
     * Default constructor for {@link PhaseHandler}.
     */
    public PhaseHandler() {
        this.allPhases = new Vector<>();
        this.msgHandler = new MessageHandler();
    }

    /**
     * Adds a {@link Visitor} to execute.
     * @param v {@link Visitor}
     */
    public void addPhase(Visitor v) { allPhases.add(v); }

    /**
     * Executes all {@link Visitor} in {@link #allPhases}.
     * <p>
     *     If an error occurs while executing in interpretation mode, then an exception will
     *     be thrown and caught by the {@link interpreter.VM}.
     * </p>
     * @param node The {@link AST} node that executes each {@link Visitor}.
     */
    public void execute(AST node) {
        if(finalPhaseToExecute != -1 && finalPhaseToExecute != allPhases.size()) {
            executeUntilFinalPhase(node);
            return;
        }

        for(Visitor v : allPhases)
            node.visit(v);
    }

    /**
     * Executes only a specified amount of {@link Visitor} based on the value of {@link #finalPhaseToExecute}.
     * @param node The {@link AST} we would like to execute each {@link Visitor} with.
     */
    private void executeUntilFinalPhase(AST node) {
        for(int i = 0; i < finalPhaseToExecute; i++)
            node.visit(allPhases.get(i));
    }

    /**
     * Adds a {@link Printer} to execute the generated {@link AST} from the parser.
     * <p>
     *     This will be an optional phase that a user can write in order to see how the
     *     C Minor parse tree looks after parsing is completed.
     * </p>
     */
    public void addPrinterPhase() {
        if(!(allPhases.get(0) instanceof Printer))
            allPhases.add(0,new Printer());
        else
            allPhases.remove(0);
    }

    /**
     * Sets the {@link #finalPhaseToExecute} if we would like to only run a portion of the compiler.
     * <p>
     *     This method will also make sure we wrote the correct phase number when using this command.
     * </p>
     * @param phase A String representing the input command used in the
     * {@link interpreter.VM} or {@link compiler.Compiler}.
     */
    public void setFinalPhaseToExecute(String phase) throws CompilationMessage {
        Vector<String> parts = new Vector<>(phase.split(" "));

        // ERROR CHECK #1: To use the "#phase" command, the user needs to write "#phase <phaseNum>"
        if(parts.size() != 2 || !parts.get(0).equals("#phase")) {
            msgHandler.createErrorBuilder(SettingError.class)
                      .addErrorNumber(MessageNumber.SETTING_ERROR_1)
                      .generateError();
        }

        // ERROR CHECK #2: We need to make sure the given phase number can actually be parsed as an integer.
        String givenPhaseNumber = parts.get(1);
        StringBuilder phaseNumber = new StringBuilder();

        for(int i = 0; i < givenPhaseNumber.length(); i++) {
            phaseNumber.append(givenPhaseNumber.charAt(i));

            if(givenPhaseNumber.charAt(i) < '0' || givenPhaseNumber.charAt(i) > '9') {
                // We will ignore negative numbers for now and print a different error message.
                if(givenPhaseNumber.charAt(i) == '-' && i == 0)
                    continue;

                msgHandler.createErrorBuilder(SettingError.class)
                          .addErrorNumber(MessageNumber.SETTING_ERROR_2)
                          .generateError();
            }
        }

        finalPhaseToExecute = Integer.parseInt(phaseNumber.toString());
        // ERROR CHECK #3: The given phase number needs to be in the given range of the visitor vector.
        if(finalPhaseToExecute <= 0 || finalPhaseToExecute > allPhases.size()) {
            finalPhaseToExecute = -1;
            msgHandler.createErrorBuilder(SettingError.class)
                      .addErrorNumber(MessageNumber.SETTING_ERROR_3)
                      .generateError();
        }
    }
}
