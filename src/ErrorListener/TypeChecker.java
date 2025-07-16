package ErrorListener;

import ErrorListener.SymbolTable.Scope;
import Tuga.*;
import org.antlr.v4.runtime.tree.*;
import org.antlr.v4.runtime.Token;
import java.util.*;
import ErrorListener.SymbolTable.Symbol;

public class TypeChecker extends TugaBaseVisitor<TypeChecker.Type>
{
    private final ParseTreeProperty<Type> types = new ParseTreeProperty<>();
    private final List<String> errors = new ArrayList<>();
    private final Map<String, FunctionSymbol> functionTable = new HashMap<>();
    private Scope currentScope = new Scope(null);

    public enum Type {
        INT, DOUBLE, BOOLEAN, STRING, VOID, ERROR
    }

    public List<String> getErrors() {
        return errors;
    }

    private void setType(ParseTree node, Type type) {
        types.put(node, type);
    }

    public Type getType(ParseTree node) {
        return types.get(node);
    }

    public Map<String, FunctionSymbol> getFunctionTable() { return this.functionTable; }


    /**
     * Guarda as informações das funções
     */
    public static class FunctionSymbol {
        private String name;
        private Type returnType;
        private List<Parameter> param;
        private int address;

        public FunctionSymbol (String name, Type returnType, List<Parameter> param, int address) {
            this.name = name;
            this.returnType = returnType;
            this.param = param;
            this.address = address;
        }

        public Type getReturnType () {
            return this.returnType;
        }

        public int getAddress () {
            return this.address;
        }

        public List<Parameter> getParam() {
            return this.param;
        }

        public void setAddress(int addr) {
            this.address = addr;
        }

        public String getName() {
            return name;
        }
    }

    public static class Parameter {
        private String name;
        private Type type;

        public Parameter(String name, Type type) {
            this.name = name;
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public Type getType() {
            return type;
        }
    }

    private void enterScope(String name) {
        currentScope = new Scope(currentScope, name);
    }

    private void exitScope() {
        currentScope = currentScope.getEnclosingScope();
    }



    /**
     * Trabalho 3
     *
     */

    @Override
    public Type visitProgram(TugaParser.ProgramContext ctx) {
        for (TugaParser.Var_declarationContext varDecl : ctx.var_declaration()) {
            visit(varDecl);
        }

        Set<String> declaredFunctions = new HashSet<>();
        for (TugaParser.Function_declarationContext funcDecl : ctx.function_declaration()) {
            TugaParser.FunctionDeclarationContext funcCtx = (TugaParser.FunctionDeclarationContext)funcDecl;
            String funcName = funcCtx.ID().getText();

            if (declaredFunctions.contains(funcName)) {
                errors.add("erro na linha " + funcCtx.start.getLine() + ": '" + funcName + "' ja foi declarado");
            } else {
                declaredFunctions.add(funcName);
                Type returnType = funcCtx.tipo() != null ? visit(funcCtx.tipo()) : Type.VOID;
                List<Parameter> param = new ArrayList<>();

                if (funcCtx.param_list() != null) {
                    for (TugaParser.ParamContext paramCtx : funcCtx.param_list().param()) {
                        Parameter parameter = new Parameter(paramCtx.ID().getText(), visit(paramCtx.tipo()));
                        param.add(parameter);
                    }
                }

                functionTable.put(funcName, new FunctionSymbol(funcName,returnType, param, -1));
            }
        }


        for (TugaParser.Function_declarationContext funcDecl : ctx.function_declaration()) {
            visit(funcDecl);
        }

        if (!functionTable.containsKey("principal")) {
            int lastLine = ctx.stop.getLine();
            errors.add("erro na linha " + lastLine + ": falta funcao principal()");
        }

        return null;
    }

    @Override
    public Type visitFunctionDeclaration(TugaParser.FunctionDeclarationContext ctx) {
        String funcName = ctx.ID().getText();

        Symbol existing = currentScope.resolve(funcName);
        if (existing != null) {
            errors.add("erro na linha " + ctx.start.getLine() + ": '" + funcName + "' ja foi declarado");
            return Type.ERROR;
        }

        enterScope(funcName);

        if (ctx.param_list() != null) {
            for (TugaParser.ParamContext paramCtx : ctx.param_list().param()) {
                Type paramType = visit(paramCtx.tipo());
                Token token = paramCtx.ID().getSymbol();
                currentScope.define(new Symbol(token, paramType));
            }
        }

        visit(ctx.block());
        exitScope();
        return null;
    }

    @Override
    public Type visitBlockStatement(TugaParser.BlockStatementContext ctx) {
        enterScope("bloco");

        if (ctx.var_declaration() != null) {
            for (TugaParser.Var_declarationContext varDecl : ctx.var_declaration()) {
                visit(varDecl);
            }
        }

        if (ctx.statement() != null) {
            for (TugaParser.StatementContext stmt : ctx.statement()) {
                visit(stmt);
            }
        }

        exitScope();
        return null;
    }

    @Override
    public Type visitReturnStatement(TugaParser.ReturnStatementContext ctx) {
        if (ctx.expr() != null) {
            Type exprType = visit(ctx.expr());
            setType(ctx, exprType);
            return exprType;
        } else { // return ;
            setType(ctx, Type.VOID);
            return Type.VOID;
        }
    }

    @Override
    public Type visitFunctionCall(TugaParser.FunctionCallContext ctx) {
        String funcName = ctx.ID().getText();
        FunctionSymbol funcSymbol = functionTable.get(funcName);

        if (funcSymbol == null) {
            errors.add("erro na linha " + ctx.start.getLine() + ": '" + funcName + "' nao foi declarado");
            return Type.ERROR;
        }

        List<Parameter> param = funcSymbol.getParam();
        int expectedArgs = param.size();
        int actualArgs = ctx.expr() != null ? ctx.expr().size() : 0;

        if (expectedArgs != actualArgs) {
            errors.add("erro na linha " + ctx.start.getLine() + ": '" + funcName + "' requer " + expectedArgs + " argumentos");
            return Type.ERROR;
        }

        // Verificar se os tipos dos args são compativeis
        if (ctx.expr() != null) {
            for (int i = 0; i < actualArgs; i++) {
                Type argType = visit(ctx.expr(i));
                Type paramType = param.get(i).getType();

                if (argType != paramType && !(paramType == Type.DOUBLE && argType == Type.INT)) {
                    errors.add("erro na linha " + ctx.start.getLine() + ": '" + ctx.expr(i).getText() + "' devia ser do tipo " + typeToString(param.get(i).getType()) );
                    return Type.ERROR;
                }
            }
        }

        // Verifica se alguma variavel/funcao esta a receber o resultado
        if (ctx.parent instanceof TugaParser.StatementContext && funcSymbol.getReturnType() != Type.VOID) {
            errors.add("erro na linha " + ctx.start.getLine() + ": valor de '" + funcName + "' tem de ser atribuido a uma variavel");
            return Type.ERROR;
        }

        return funcSymbol.getReturnType();
    }

    @Override
    public Type visitFunctionCallExpr(TugaParser.FunctionCallExprContext ctx) {
        Type callType = visit(ctx.function_call());
        setType(ctx, callType);
        return callType;
    }

    /**
     * Trabalho 2
     *
     */

    private String typeToString(Type type) {
        switch(type) {
            case INT: return "inteiro";
            case DOUBLE: return "real";
            case BOOLEAN: return "booleano";
            case STRING: return "string";
            case VOID: return "vazio";
            case ERROR: return "erro";
            default: return type.name().toLowerCase();
        }
    }

    @Override
    public Type visitVar_declaration(TugaParser.Var_declarationContext ctx) {
        Type varType = visit(ctx.tipo());

        if (varType == null) {
            varType = Type.ERROR;
        }

        for (TerminalNode id : ctx.ID()) {
            String varName = id.getText();

            // Verificar se já existe uma função com esse nome
            if (functionTable.containsKey(varName)) {
                errors.add("erro na linha " + ctx.start.getLine() + ": '" + varName + "' ja foi declarado");
                return Type.ERROR; // Importante: retornar para evitar declarar
            }

            // Verificar se a variável já foi declarada neste Scope
            Symbol existing = currentScope.resolve(varName);
            if (existing != null) {
                errors.add("erro na linha " + ctx.start.getLine() + ": '" + varName + "' ja foi declarado");
                return Type.ERROR;
            }

            // Declara a variavel
            Token token = id.getSymbol();
            Symbol sym = new Symbol(token, varType);
            currentScope.define(sym);
            setType(id, varType);
        }
        return null;
    }


    @Override
    public Type visitAssignmentStatement(TugaParser.AssignmentStatementContext ctx) {
        String varName = ctx.ID().getText();

        // Verificar se uma funcao esta ser usada como variavel (Esquerda) (Receber um valor) "funcao <- valor"
        if (functionTable.containsKey(varName)) {
            errors.add("erro na linha " + ctx.start.getLine() + ": '" + varName + "' nao eh variavel");
            return Type.ERROR;
        }

        // Verificar se uma funcao está a ser usada como variavel (Direita) "variavel <- funcao"
        if (ctx.expr() instanceof TugaParser.VarExprContext && functionTable.containsKey(ctx.expr().getText())) {
            errors.add("erro na linha " + ctx.start.getLine() + ": '" + ctx.expr().getText() + "' nao eh variavel");
            return Type.ERROR;
        }

        Symbol sym = currentScope.resolve(varName);
        Type varType = (sym != null) ? sym.type() : null;
        Type exprType = visit(ctx.expr());

        if (exprType == Type.ERROR) {
            return Type.ERROR;
        }

        if (varType == null) {
            errors.add("erro na linha " + ctx.start.getLine() + ": variavel '" + varName + "' nao foi declarada");
            return Type.ERROR;
        }

        if (varType == Type.DOUBLE && exprType == Type.INT) {
            return varType;
        }

        if (exprType != varType) {
            errors.add("erro na linha " + ctx.start.getLine() + ": operador '<-' eh invalido entre " + typeToString(varType) + " e " + typeToString(exprType));
            return Type.ERROR;
        }

        return varType;
    }

    @Override
    public Type visitVarExpr(TugaParser.VarExprContext ctx) {
        String varName = ctx.ID().getText();
        Symbol sym = currentScope.resolve(varName);
        Type varType = (sym != null) ? sym.type() : null;


        if (varType == null) {
            errors.add("erro na linha " + ctx.start.getLine() + ": variavel '" + varName + "' nao foi declarada");
            return Type.ERROR;
        }

        setType(ctx, varType);
        return varType;
    }

    @Override
    public Type visitWhileStatement(TugaParser.WhileStatementContext ctx) {
        if (ctx.expr() == null) {
            errors.add("erro na linha " + ctx.start.getLine() + ": condiçao do 'enquanto' faltando");
            return Type.ERROR;
        }

        Type exprType = visit(ctx.expr());
        if (exprType != Type.BOOLEAN) {
            errors.add("erro na linha " + ctx.start.getLine() + ": expressao de 'enquanto' nao eh do tipo booleano");
        }

        if (ctx.statement() != null) {
            visit(ctx.statement());
        } else if (ctx.block() != null) {
            visit(ctx.block());
        } else {
            errors.add("erro na linha " + ctx.start.getLine() + ": corpo do 'enquanto' faltando");
        }

        return null;
    }

    @Override
    public Type visitIfStatement(TugaParser.IfStatementContext ctx) {
        Type exprType = visit(ctx.expr());
        if (exprType != Type.BOOLEAN) {
            errors.add("erro na linha " + ctx.start.getLine() + ": expressao de 'se' nao eh do tipo booleano");
        }

        visit(ctx.thenBody);

        if (ctx.elseBody != null) {
            visit(ctx.elseBody);
        }

        return null;
    }

    @Override
    public Type visitTipo(TugaParser.TipoContext ctx) {
        if (ctx.getText().equals("inteiro")) return Type.INT;
        if (ctx.getText().equals("real")) return Type.DOUBLE;
        if (ctx.getText().equals("booleano")) return Type.BOOLEAN;
        if (ctx.getText().equals("string")) return Type.STRING;
        return Type.ERROR;
    }

    /**
     * Trabalho 1
     *
     */

    @Override
    public Type visitIntLiteral(TugaParser.IntLiteralContext ctx) {
        setType(ctx, Type.INT);
        return Type.INT;
    }

    @Override
    public Type visitDoubleLiteral(TugaParser.DoubleLiteralContext ctx) {
        setType(ctx, Type.DOUBLE);
        return Type.DOUBLE;
    }

    @Override
    public Type visitBoolLiteral(TugaParser.BoolLiteralContext ctx) {
        setType(ctx, Type.BOOLEAN);
        return Type.BOOLEAN;
    }

    @Override
    public Type visitStringLiteral(TugaParser.StringLiteralContext ctx) {
        setType(ctx, Type.STRING);
        return Type.STRING;
    }

    @Override
    public Type visitUnaryExpr(TugaParser.UnaryExprContext ctx)
    {
        Type exprType = visit(ctx.expr());

        if (ctx.MINUS() != null)
        {
            if (exprType == Type.INT || exprType == Type.DOUBLE) {
                setType(ctx, exprType);
                return exprType;
            }
            else
            {
                errors.add("Line " + ctx.start.getLine() + ":" + ctx.start.getCharPositionInLine() + " error: Unary '-' cannot be applied to " + exprType);
                setType(ctx, Type.ERROR);
                return Type.ERROR;
            }
        }
        else if (ctx.NAO() != null)
        {
            if (exprType == Type.BOOLEAN)
            {
                setType(ctx, Type.BOOLEAN);
                return Type.BOOLEAN;
            }
            else
            {
                errors.add("Line " + ctx.start.getLine() + ":" + ctx.start.getCharPositionInLine() + " error: Unary 'nao' cannot be applied to " + exprType);
                setType(ctx, Type.ERROR);
                return Type.ERROR;
            }
        }

        setType(ctx, Type.ERROR);
        return Type.ERROR;
    }

    @Override
    public Type visitMulDivModExpr(TugaParser.MulDivModExprContext ctx)
    {
        Type leftType = visit(ctx.expr(0));
        Type rightType = visit(ctx.expr(1));
        String op = ctx.op.getText();

        if (op.equals("*") || op.equals("/"))
        {
            if ((leftType == Type.INT || leftType == Type.DOUBLE) && (rightType == Type.INT || rightType == Type.DOUBLE))
            {
                Type resultType = (leftType == Type.DOUBLE || rightType == Type.DOUBLE) ? Type.DOUBLE : Type.INT;
                setType(ctx, resultType);
                return resultType;
            }
            else
            {
                errors.add("Line " + ctx.start.getLine() + ":" + ctx.start.getCharPositionInLine() + " error: Operator '" + op + "' cannot be applied to " + leftType + ", " + rightType);
                setType(ctx, Type.ERROR);
                return Type.ERROR;
            }
        }
        else if (op.equals("%"))
        {
            if (leftType == Type.INT && rightType == Type.INT)
            {
                setType(ctx, Type.INT);
                return Type.INT;
            }
            else
            {
                errors.add("Line " + ctx.start.getLine() + ":" + ctx.start.getCharPositionInLine() + " error: Operator '%' requires both operands to be integers");
                setType(ctx, Type.ERROR);
                return Type.ERROR;
            }
        }

        setType(ctx, Type.ERROR);
        return Type.ERROR;
    }

    @Override
    public Type visitAddSubExpr(TugaParser.AddSubExprContext ctx) {
        Type leftType = visit(ctx.expr(0));
        Type rightType = visit(ctx.expr(1));
        String op = ctx.op.getText();

        if (op.equals("+")) {
            if (leftType == Type.STRING || rightType == Type.STRING) {
                setType(ctx, Type.STRING);
                return Type.STRING;
            }
            else if ((leftType == Type.INT || leftType == Type.DOUBLE) && (rightType == Type.INT || rightType == Type.DOUBLE)) {
                Type resultType = (leftType == Type.DOUBLE || rightType == Type.DOUBLE) ? Type.DOUBLE : Type.INT;
                setType(ctx, resultType);
                return resultType;
            }
            else {
                errors.add("erro na linha " + ctx.start.getLine() + ": operador '+' eh invalido entre " + typeToString(leftType) + " e " + typeToString(rightType));
                setType(ctx, Type.ERROR);
                return Type.ERROR;
            }
        }
        else if (op.equals("-")) {
            if ((leftType == Type.INT || leftType == Type.DOUBLE) && (rightType == Type.INT || rightType == Type.DOUBLE)) {
                Type resultType = (leftType == Type.DOUBLE || rightType == Type.DOUBLE) ? Type.DOUBLE : Type.INT;
                setType(ctx, resultType);
                return resultType;
            }
            else {
                errors.add("erro na linha " + ctx.start.getLine() + ": operador '-' eh invalido entre " + typeToString(leftType) + " e " + typeToString(rightType));
                setType(ctx, Type.ERROR);
                return Type.ERROR;
            }
        }

        setType(ctx, Type.ERROR);
        return Type.ERROR;
    }

    @Override
    public Type visitRelationalExpr(TugaParser.RelationalExprContext ctx)
    {
        Type leftType = visit(ctx.expr(0));
        Type rightType = visit(ctx.expr(1));

        if ((leftType == Type.INT || leftType == Type.DOUBLE) && (rightType == Type.INT || rightType == Type.DOUBLE))
        {
            setType(ctx, Type.BOOLEAN);
            return Type.BOOLEAN;
        }
        else
        {
            errors.add("Line " + ctx.start.getLine() + ":" + ctx.start.getCharPositionInLine() + " error: Relational operator cannot be applied to " + leftType + ", " + rightType);
            setType(ctx, Type.ERROR);
            return Type.ERROR;
        }
    }

    @Override
    public Type visitEqualityExpr(TugaParser.EqualityExprContext ctx)
    {
        Type leftType = visit(ctx.expr(0));
        Type rightType = visit(ctx.expr(1));
        String op = ctx.op.getText();

        if (leftType == Type.STRING && rightType == Type.STRING)
        {
            setType(ctx, Type.BOOLEAN);
            return Type.BOOLEAN;
        }
        else if (leftType == Type.BOOLEAN && rightType == Type.BOOLEAN)
        {
            setType(ctx, Type.BOOLEAN);
            return Type.BOOLEAN;
        }
        else if ((leftType == Type.INT || leftType == Type.DOUBLE) && (rightType == Type.INT || rightType == Type.DOUBLE))
        {
            setType(ctx, Type.BOOLEAN);
            return Type.BOOLEAN;
        }
        else
        {
            errors.add("Line " + ctx.start.getLine() + ":" + ctx.start.getCharPositionInLine() + " error: Equality operator '" + op + "' cannot be applied to " + leftType + ", " + rightType);
            setType(ctx, Type.ERROR);
            return Type.ERROR;
        }
    }

    @Override
    public Type visitAndExpr(TugaParser.AndExprContext ctx)
    {
        Type leftType = visit(ctx.expr(0));
        Type rightType = visit(ctx.expr(1));

        if (leftType == Type.BOOLEAN && rightType == Type.BOOLEAN)
        {
            setType(ctx, Type.BOOLEAN);
            return Type.BOOLEAN;
        }
        else
        {
            errors.add("Line " + ctx.start.getLine() + ":" + ctx.start.getCharPositionInLine() + " error: Logical 'e' requires both operands to be boolean");
            setType(ctx, Type.ERROR);
            return Type.ERROR;
        }
    }

    @Override
    public Type visitOrExpr(TugaParser.OrExprContext ctx)
    {
        Type leftType = visit(ctx.expr(0));
        Type rightType = visit(ctx.expr(1));

        if (leftType == Type.BOOLEAN && rightType == Type.BOOLEAN)
        {
            setType(ctx, Type.BOOLEAN);
            return Type.BOOLEAN;
        }
        else
        {
            errors.add("Line " + ctx.start.getLine() + ":" + ctx.start.getCharPositionInLine() + " error: Logical 'ou' requires both operands to be boolean");
            setType(ctx, Type.ERROR);
            return Type.ERROR;
        }
    }

    @Override
    public Type visitParenExpr(TugaParser.ParenExprContext ctx) {
        Type exprType = visit(ctx.expr());
        setType(ctx, exprType);
        return exprType;
    }

    @Override
    public Type visitPrintStatement(TugaParser.PrintStatementContext ctx) {
        Type exprType = visit(ctx.expr());
        setType(ctx, exprType);
        return exprType;
    }
}