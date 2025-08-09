package interpreter;

import ast.AST;
import ast.misc.CompilationUnit;
import lexer.Lexer;
import messages.CompilationMessage;
import messages.MessageHandler;
import messages.MessageNumber;
import messages.errors.setting.SettingError;
import micropasses.*;
import modifierchecker.ModifierChecker;
import namechecker.NameChecker;
import parser.Parser;
import typechecker.TypeChecker;
import utilities.PhaseHandler;
import utilities.Vector;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * A class that initializes and sets up the C Minor virtual environment.
 * <p>
 *     When a user enters interpretation mode, we will set up a virtual environment
 *     for the user to write and execute commands using a REPL.
 * </p>
 * @author Daniel Levy
 */
public class VM {

    /**
     * The global {@link CompilationUnit} for the {@link VM}.
     */
    private final CompilationUnit globalUnit;

    /**
     * {@link PhaseHandler} that will execute all compilation phases when we are in the {@link VM}.
     */
    private final PhaseHandler phaseHandler;

    /**
     * {@link MessageHandler} that will print out errors related to compiler settings.
     */
    private final MessageHandler msgHandler;

    /**
     * Default constructor for {@link VM}.
     */
    public VM() {
        this.globalUnit = new CompilationUnit();
        this.phaseHandler = new PhaseHandler();
        this.msgHandler = new MessageHandler();

        setupVM();
    }

    /**
     * Reads in user input from the {@link VM} and processes it before executing the compiler.
     * @throws IOException An exception thrown in case there was an error reading in user input with InputStreamReader.
     * (Not sure if an exception is ever thrown).
     */
    public void readUserInput() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        System.out.println("C Minor Interpreter\n");
        while(true) {
            StringBuilder program = new StringBuilder();
            String input;

            System.out.print(">>> ");
            input = reader.readLine();

            switch(input) {
                case "#clear":
                    ImportHandler.clear();
                    globalUnit.reset();
                    continue;
                case "#print-tokens":
                    Parser.setPrintTokens();
                    continue;
                case "#print-tree":
                    phaseHandler.addPrinterPhase();
                    continue;
                case "#print-table":
                    continue;
                case "#debug":
                    CompilationMessage.setDebugMode();
                    continue;
                default:
                    try {
                        if(input.startsWith("#")) {
                            if(input.startsWith("#phase"))
                                phaseHandler.setFinalPhaseToExecute(input);
                            else {
                                msgHandler.createErrorBuilder(SettingError.class)
                                          .addErrorNumber(MessageNumber.SETTING_ERROR_4)
                                          .generateError();
                            }
                        }
                    }
                    catch(CompilationMessage msg) {
                        msg.printMessage();
                        continue;
                    }

                    if(input.isEmpty())
                        continue;
            }

            int tabs = 0;
            program.append(input);
            tabs += input.length() - input.replace("{","").length();
            tabs -= input.length() - input.replace("}", "").length();
            while(tabs > 0) {
                System.out.print("... ");
                input = reader.readLine();
                tabs += input.length() - input.replace("{","").length();
                tabs -= input.length() - input.replace("}", "").length();
                program.append(input).append("\n");
            }

            runInterpreter(program.toString());
        }
    }

    /**
     * Executes the C Minor interpreter.
     * @param program String representation of the user program that will be parsed and analyzed by the compiler.
     */
    private void runInterpreter(String program) {
        Parser parser = new Parser(new Lexer(program));
        Vector<? extends AST> nodes;

        try {
            nodes = parser.nextNode();
            for(AST node : nodes)
                phaseHandler.execute(node);
        }
        catch(CompilationMessage msg) {
            msg.printMessage();
            msg.updateGlobalScope(globalUnit.getScope());
        }
    }

    /**
     * Initializes the VM by adding the phases that will be executed to the {@link #phaseHandler}.
     */
    private void setupVM() {
        MessageHandler.setInterpretationMode();

        phaseHandler.addPhase(new PropertyGenerator());
        phaseHandler.addPhase(new NameChecker(globalUnit.getScope()));
        phaseHandler.addPhase(new VariableInitialization());
        phaseHandler.addPhase(new FieldRewrite());
        phaseHandler.addPhase(new OperatorOverloadCheck());
        phaseHandler.addPhase(new LoopKeywordCheck());
        phaseHandler.addPhase(new TypeValidityPass(globalUnit.getScope()));
        phaseHandler.addPhase(new TypeChecker(globalUnit.getScope()));
        phaseHandler.addPhase(new ConstructorGeneration());
        phaseHandler.addPhase(new ModifierChecker(globalUnit.getScope()));
        phaseHandler.addPhase(new PureKeywordPass());
        phaseHandler.addPhase(new Interpreter(globalUnit.getScope()));
    }
}
