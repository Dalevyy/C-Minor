package errors;

import ast.*;

public abstract class DefaultError {

    private static String mainError = "Error detected.";

    public static void header(AST n) {
        System.out.println(mainError + "\n");
        DefaultError.printProgramLine(n);

    }
    public static void printProgramLine(AST n) {
        int line = n.getLocation().start.line;

        AST master = n;
        while(master.getParent() != null && master.getParent().getLocation().start.line == line) {
            master = master.getParent();
        }

        System.out.println(line + " | " + master.getText());
    }

}
