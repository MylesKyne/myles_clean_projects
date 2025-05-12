import java.io.*;

// Syntax Analyser implementation for SCC312 CW
// Implements a recursive descent parser with respect to the grammar specification
public class SyntaxAnalyser extends AbstractSyntaxAnalyser {

    // this constructor initialises the lexical analyser with the source file
    public SyntaxAnalyser(String filename) throws IOException {
        lex = new LexicalAnalyser(filename);
    }

    // advances the next token and updates the nextToken attirbute
    private void nextToken() throws IOException {
        nextToken = lex.getNextToken();
    }

    // ensures that three parts appear in this order: 1. begin, 2. A valid <StatementList> 3. end
    // Trhows CompilationException if tokens don't appear in this expected order
    @Override
    public void _statementPart_() throws IOException, CompilationException {
        myGenerate.commenceNonterminal("<StatementPart>");
        acceptTerminal(Token.beginSymbol);
        statementList();
        acceptTerminal(Token.endSymbol);
        myGenerate.finishNonterminal("<StatementPart>");
    }

    // parses the <StatementList> non terminal ensuring parts appear like 1. A single <Statement> 
    // 2. Zero or more occurrences of ; followed by another statement
    private void statementList() throws IOException, CompilationException {
        myGenerate.commenceNonterminal("<StatementList>");
        statement();
        while (nextToken.symbol == Token.semicolonSymbol) {
            acceptTerminal(Token.semicolonSymbol);
            statement();
        }
        myGenerate.finishNonterminal("<StatementList>");
    }

    //Parses a single <statement> which is one of the following
    // 1. <AssigmentStatement> which is if the next token is an identifier
    // 2. <IfStatement> makes use of 'if'
    // 3. <WhileStatement> While loop
    // 4. <ProcedureStatement> procedure/function call
    // 5. <UntilStatement> do until loop
    // 6. <ForStatement> For loops
    private void statement() throws IOException, CompilationException {
        myGenerate.commenceNonterminal("<Statement>");
        switch (nextToken.symbol) {
            case Token.identifier:
                assignmentStatement();
                break;
            case Token.ifSymbol:
                ifStatement();
                break;
            case Token.whileSymbol:
                whileStatement();
                break;
            case Token.callSymbol:
                procedureStatement();
                break;
            case Token.doSymbol:
                untilStatement();
                break;
            case Token.forSymbol:
                forStatement();
                break;
            default:
                myGenerate.reportError(nextToken, "Unexpected token in <Statement>");
        }
        myGenerate.finishNonterminal("<Statement>");
    }

      // Parses an <AssignmentStatement> strutured as
      // 1. an Identifier, 2. ':= symbol', 3. An <Expression> string constant
      private void assignmentStatement() throws IOException, CompilationException {
        myGenerate.commenceNonterminal("<AssignmentStatement>");
        acceptTerminal(Token.identifier);
        acceptTerminal(Token.becomesSymbol); // ":=" 
        if (nextToken.symbol == Token.stringConstant) {
            acceptTerminal(Token.stringConstant);
        } else {
            expression();
        }
        myGenerate.finishNonterminal("<AssignmentStatement>");
    }

    // Parses an <IfStatement> parts are 1. if keyword, 2. <condition> 3. 'then' keyword, 4. <StatementList>
    // 5. optional, an else keyword followed by another <StatementList> 6. finally an 'end if'
    private void ifStatement() throws IOException, CompilationException {
        myGenerate.commenceNonterminal("<IfStatement>");
        acceptTerminal(Token.ifSymbol);
        condition();
        acceptTerminal(Token.thenSymbol);
        statementList();
        if (nextToken.symbol == Token.elseSymbol) {
            acceptTerminal(Token.elseSymbol);
            statementList();
        }
        // For "end if", endSymbol is followed by an ifSymbol.
        acceptTerminal(Token.endSymbol);
        acceptTerminal(Token.ifSymbol);
        myGenerate.finishNonterminal("<IfStatement>");
    }

    // Parses <WhileStatement> parts are, 1. 'while' keyword, 2. <condition> to evaluate loop 3. 'loop' keyword
    // 4. A <StatementList> which represents the loop body 5. finally an 'end loop'
    private void whileStatement() throws IOException, CompilationException {
        myGenerate.commenceNonterminal("<WhileStatement>");
        acceptTerminal(Token.whileSymbol);
        condition();
        acceptTerminal(Token.loopSymbol);
        statementList();
        acceptTerminal(Token.endSymbol);
        acceptTerminal(Token.loopSymbol);
        myGenerate.finishNonterminal("<WhileStatement>");
    }

    // Parses a <Proedurecstatement> structured as: 1. 'call' keyword, 2. Procedure identifier, 3. Opening bracket
    // 4. valid <ArgumentList> 5. closing bracket
    private void procedureStatement() throws IOException, CompilationException {
        myGenerate.commenceNonterminal("<ProcedureStatement>");
        acceptTerminal(Token.callSymbol);
        acceptTerminal(Token.identifier);
        acceptTerminal(Token.leftParenthesis);
        argumentList();
        acceptTerminal(Token.rightParenthesis);
        myGenerate.finishNonterminal("<ProcedureStatement>");
    }

    // parses <UntilStatement> 1. 'do' keyword, 2. <statementList> exeuted withcin the loop 3. 'until' followed by condition
    private void untilStatement() throws IOException, CompilationException {
        myGenerate.commenceNonterminal("<UntilStatement>");
        acceptTerminal(Token.doSymbol);
        statementList();
        acceptTerminal(Token.untilSymbol);
        condition();
        myGenerate.finishNonterminal("<UntilStatement>");
    }

    // parses a <ForStatement> parts are 1. 'for' keyword following by opening braket, 2. assignment statement
    // 3. ; expected, 4. loop condition 5. another ; 6. increment/decrement 7. end bracket followed by do 8. <statementList> representing loop body
    // 9. 'end loop'
    private void forStatement() throws IOException, CompilationException {
        myGenerate.commenceNonterminal("<ForStatement>");
        acceptTerminal(Token.forSymbol);
        acceptTerminal(Token.leftParenthesis);
        assignmentStatement();
        acceptTerminal(Token.semicolonSymbol);
        condition();
        acceptTerminal(Token.semicolonSymbol);
        assignmentStatement();
        acceptTerminal(Token.rightParenthesis);
        acceptTerminal(Token.doSymbol);
        statementList();
        acceptTerminal(Token.endSymbol);
        acceptTerminal(Token.loopSymbol);
        myGenerate.finishNonterminal("<ForStatement>");
    }

    // <ArgumentList> parsed, structured as, 1. one identifier, 2. potentially other identifiers seperated by commas
    private void argumentList() throws IOException, CompilationException {
        myGenerate.commenceNonterminal("<ArgumentList>");
        acceptTerminal(Token.identifier);
        while (nextToken.symbol == Token.commaSymbol) {
            acceptTerminal(Token.commaSymbol);
            acceptTerminal(Token.identifier);
        }
        myGenerate.finishNonterminal("<ArgumentList>");
    }

    //Parses a <condition> 1. An identifier, 2. conditional operator like <= or =, 3. an identifier, number, string etc
    private void condition() throws IOException, CompilationException {
        myGenerate.commenceNonterminal("<Condition>");
        acceptTerminal(Token.identifier);
        if (isConditionalOperator(nextToken.symbol)) {
            acceptTerminal(nextToken.symbol);
        } else {
            myGenerate.reportError(nextToken, "Expected a conditional operator in <Condition>");
        }
        if (nextToken.symbol == Token.identifier ||
            nextToken.symbol == Token.numberConstant ||
            nextToken.symbol == Token.stringConstant) {
            acceptTerminal(nextToken.symbol);
        } else {
            myGenerate.reportError(nextToken, "Expected identifier, number constant, or string constant in <Condition>");
        }
        myGenerate.finishNonterminal("<Condition>");
    }

    // checks if a symbol is conditional, returns true if matches, false otherwise
    private boolean isConditionalOperator(int symbol) {
        return symbol == Token.greaterEqualSymbol ||
               symbol == Token.greaterThanSymbol ||
               symbol == Token.equalSymbol ||
               symbol == Token.notEqualSymbol ||
               symbol == Token.lessThanSymbol ||
               symbol == Token.lessEqualSymbol;
    }

    // Parses an arithmetic <Expression> parts are: 1. An initial <Term> 2. optionally additional terms combined with 
    // + or - operators
    private void expression() throws IOException, CompilationException {
        myGenerate.commenceNonterminal("<Expression>");
        term();
        while (nextToken.symbol == Token.plusSymbol ||
               nextToken.symbol == Token.minusSymbol) {
            acceptTerminal(nextToken.symbol);
            term();
        }
        myGenerate.finishNonterminal("<Expression>");
    }

    // Parses a <Term> whih is a building block of arithemetic expressions like x * 2 or x * y / z
    // parts are 1. an initial <Factor> 2. optionally additional factors 
    private void term() throws IOException, CompilationException {
        myGenerate.commenceNonterminal("<Term>");
        factor();
        while (nextToken.symbol == Token.timesSymbol ||
               nextToken.symbol == Token.divideSymbol ||
               nextToken.symbol == Token.modSymbol) {
            acceptTerminal(nextToken.symbol);
            factor();
        }
        myGenerate.finishNonterminal("<Term>");
    }

    // Parses a factor which is 1. An identifier or number constant, 2. an expression in brackets 3. reports an error if none found
    private void factor() throws IOException, CompilationException {
        myGenerate.commenceNonterminal("<Factor>");
        if (nextToken.symbol == Token.identifier || nextToken.symbol == Token.numberConstant) {
            acceptTerminal(nextToken.symbol);
        } else if (nextToken.symbol == Token.leftParenthesis) {
            acceptTerminal(Token.leftParenthesis);
            expression();
            acceptTerminal(Token.rightParenthesis);
        } else {
            myGenerate.reportError(nextToken, "Expected identifier, number constant, or '(' in <Factor>");
        }
        myGenerate.finishNonterminal("<Factor>");
    }

    // checks whether a current token matches the expected terminal symbol
    // 1. if matched, inserts token and increments to next one, if no match, syntax error indicating expected token
    @Override
    public void acceptTerminal(int symbol) throws IOException, CompilationException {
        if (nextToken.symbol == symbol) {
            myGenerate.insertTerminal(nextToken);
            nextToken();
        } else {
            myGenerate.reportError(nextToken, "Expected " + Token.getName(symbol));
        }
    }
}
