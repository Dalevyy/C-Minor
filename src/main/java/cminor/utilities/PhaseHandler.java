package cminor.utilities;

import cminor.ast.AST;
import cminor.ast.topleveldecls.ClassDecl;
import cminor.ast.types.ClassType;
import cminor.messages.CompilationMessage;
import cminor.messages.MessageHandler;
import cminor.messages.MessageNumber;
import cminor.messages.errors.setting.SettingError;
import cminor.micropasses.FieldRewriter;
import cminor.micropasses.PropertyGenerator;
import cminor.micropasses.SemanticAnalyzer;
import cminor.micropasses.TypeValidator;
import cminor.namechecker.NameChecker;
import cminor.typechecker.TypeChecker;

/**
 * A class designed to store and execute all compiler phases.
 * <p>
 *     This class will keep track of all compiler phases that need to be executed.
 *     The {@link cminor.compiler.Compiler} and {@link cminor.interpreter.VM} will set this object
 *     up, and they will interface with it in order to generate the correct configuration
 *     for each compilation mode.
 * </p>
 * @author Daniel Levy
 */
public class PhaseHandler {

    /**
     * {@link Vector} containing every phase that will be executed.
     */
    private Vector<Visitor> phases;

    /**
     * {@link MessageHandler} that will deal with any compiler-related command errors.
     */
    private final MessageHandler msgHandler;

    private SymbolTable globalScope;

    /**
     * An optional number that denotes which phase the compilation process should stop at.
     * <p>
     *     This will be used for debugging purposes. I do not anticipate a user would ever
     *     want to siphon off the compilation process unless they are studying how a
     *     compiler works.
     * </p>
     */
    private PhaseNumber finalPhase;

    /**
     * Default constructor for {@link PhaseHandler}.
     */
    public PhaseHandler() {
        this.phases = new Vector<>();
        this.msgHandler = new MessageHandler();
        this.finalPhase = null;
        this.globalScope = null;
    }

    public PhaseHandler(SymbolTable globalScope) {
        this();
        this.globalScope = globalScope;
        setup();
    }

    /**
     * Executes all {@link Visitor} in {@link #phases}.
     * <p>
     *     If an error occurs while executing in interpretation mode, then an exception will
     *     be thrown and caught by the {@link cminor.interpreter.VM}.
     * </p>
     * @param node The {@link AST} node that executes each {@link Visitor}.
     */
    public void execute(AST node) {
        if(finalPhase != null)
            executeUntilFinalPhase(node);
        else
            for(Visitor v : phases)
                node.visit(v);
    }

//    private void execute(ClassDecl cd) {
//        phases.add(PhaseNumber.FIELD_REWRITER.ordinal(), new FieldRewriter());
//
//        if(finalPhase != null)
//            executeUntilFinalPhase(cd);
//        else
//            for(Visitor v : phases)
//                cd.visit(v);
//
//        phases.remove(PhaseNumber.FIELD_REWRITER.ordinal());
//    }

    /**
     * Executes only a specified amount of {@link Visitor} based on the value of {@link #finalPhase}.
     * @param node The {@link AST} we would like to execute each {@link Visitor} with.
     */
    private void executeUntilFinalPhase(AST node) {
        for(int i = 0; i <= finalPhase.ordinal(); i++)
            node.visit(phases.get(i));
    }

    /**
     * Adds a {@link Printer} to execute the generated {@link AST} from the parser.
     * <p>
     *     This will be an optional phase that a user can write in order to see how the
     *     C Minor parse tree looks after parsing is completed.
     * </p>
     */
    public void addPrinterPhase() {
        if(!(phases.getFirst() instanceof Printer))
            phases.add(0,new Printer());
        else
            phases.removeFirst();
    }

    /**
     * Sets the {@link #finalPhase} if we would like to only run a portion of the compiler.
     * <p>
     *     This method will also make sure we wrote the correct phase number when using this command.
     * </p>
     * @param phase A String representing the input command used in the
     * {@link cminor.interpreter.VM} or {@link cminor.compiler.Compiler}.
     */
    public void setFinalPhase(String phase) throws CompilationMessage {
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

        int finalPhaseToExecute = Integer.parseInt(phaseNumber.toString());
        // ERROR CHECK #3: The given phase number needs to be in the given range of the visitor vector.
        if(finalPhaseToExecute <= 0 || finalPhaseToExecute > phases.size()) {
            finalPhaseToExecute = -1;
            msgHandler.createErrorBuilder(SettingError.class)
                      .addErrorNumber(MessageNumber.SETTING_ERROR_3)
                      .generateError();
        }

        finalPhase = PhaseNumber.values()[finalPhaseToExecute];
    }

    /**
     * Sets the {@link #finalPhase} to execute. This method should only be called by the unit tests.
     * @param phase The final {@link PhaseNumber} we wish to execute before terminating the compiler.
     */
    public void setFinalPhase(PhaseNumber phase) { finalPhase = phase; }

    private void reset() {
        phases = new Vector<>();
        setup();
    }

    private void setup() {
        phases.add(new SemanticAnalyzer());
        phases.add(new PropertyGenerator());
        phases.add(new NameChecker(globalScope));
        phases.add(new FieldRewriter());
        phases.add(new TypeValidator(globalScope));
        phases.add(new TypeChecker(globalScope));
    }
}
