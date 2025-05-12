public class Generate extends AbstractGenerate {

    // Traks the current indentation level for printing the parse tree
    private int indentLevel = 0;

    // Prints a message indented by current indent level
    private void printIndented(String message) {
        for (int i = 0; i < indentLevel; i++) {
            System.out.print("   "); // 3 spaces per indentation level
        }
        System.out.println(message);
    }

    // Inserts and prints information about successfully matched token
    @Override
    public void insertTerminal(Token token) {
        printIndented("Terminal: " + token.text + " (line " + token.lineNumber + ")");
    }

    // indicates the parser is entering a non-terminal grammar
    @Override
    public void commenceNonterminal(String nonTerminalName) {
        printIndented("Entering " + nonTerminalName);
        indentLevel++; // Increase indentation for nested structures
    }

    // indicates the parser has finished parsing non-terminal grammar
    @Override
    public void finishNonterminal(String nonTerminalName) {
        indentLevel--; // Decrease indentation when exiting a non-terminal.
        printIndented("Exiting " + nonTerminalName);
    }

    // Reports when entire file has been successfully parsed
    @Override
    public void reportSuccess() {
        printIndented("Parsing completed successfully.");
    }

    // Reports Syntax error, printing explanatory message and throwing an exception
    @Override
    public void reportError(Token token, String explanatoryMessage) throws CompilationException {
        String errorMsg = "Error at line " + token.lineNumber + " (token: " + token.text + "): " + explanatoryMessage;
        System.err.println(errorMsg);
        throw new CompilationException(errorMsg);
    }
}
