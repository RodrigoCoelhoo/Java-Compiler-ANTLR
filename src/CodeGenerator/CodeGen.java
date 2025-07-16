package CodeGenerator;

import java.io.*;
import java.util.*;
import Tuga.*;
import VM.OpCode;
import VM.Instruction.*;
import ErrorListener.*;
import org.antlr.v4.runtime.tree.TerminalNode;


public class CodeGen extends TugaBaseVisitor<Void>
{
    private final ArrayList<Instruction> code = new ArrayList<>();
    private final List<Object> constantPool = new ArrayList<>();
    private final TypeChecker typeChecker;
    private int varCount = 0;
    private int globalAllocs = 0;
    private final Map<String, Integer> varIndices = new HashMap<>();

    private final Map<String, TypeChecker.FunctionSymbol> functionTable;
    private TypeChecker.FunctionSymbol currentFunction;

    private final Stack<Map<String, Integer>> localVarScopes = new Stack<>();
    private final Stack<Integer> localOffsetStack = new Stack<>(); // Calcular a posicao das variaveis (+) e args (-), FP (0)
    private final Stack<Integer> localVarCountStack = new Stack<>();

    public CodeGen(TypeChecker typeChecker) {
        this.typeChecker = typeChecker;
        this.functionTable = typeChecker.getFunctionTable();
    }

    private static class CallPlaceholder extends Instruction {
        public final String functionName;

        public CallPlaceholder(String functionName) {
            super(OpCode.call);
            this.functionName = functionName;
        }

        public String getFunctionName() {
            return functionName;
        }

        @Override
        public String toString() {
            return "call <" + functionName + ">";
        }
    }

    /**
     * Trabalho 3
     */

    @Override
    public Void visitProgram(TugaParser.ProgramContext ctx) {
        // Aqui é só as variaveis globais
        for (TugaParser.Var_declarationContext decl : ctx.var_declaration()) {
            int globalCount = 0;
            for (TerminalNode id : decl.ID()) {
                String varName = id.getText();
                if (!varIndices.containsKey(varName)) {
                    varIndices.put(varName, varCount++);
                }
                globalCount++;
            }
            emit(OpCode.galloc, globalCount);
            globalAllocs++;
        }

        // Visitar as funções
        for (TugaParser.Function_declarationContext func : ctx.function_declaration()) {
            visit(func);
        }

        // Trocar os placeholders
        for(int i = 0; i < code.size(); i++)
        {
            if(code.get(i) instanceof CallPlaceholder placeholder)
            {
                int addr = functionTable.get(placeholder.getFunctionName()).getAddress();
                code.set(i, new Instruction1Arg(OpCode.call, addr));
            }
        }

        return null;
    }

    private boolean principalHasAddress() {
        return functionTable.get("principal").getAddress() != -1;
    }

    @Override
    public Void visitFunctionDeclaration(TugaParser.FunctionDeclarationContext ctx) {
        // Iniciar um Scope para a funcao
        localVarScopes.push(new HashMap<>());

        localVarCountStack.push(0);
        localOffsetStack.push(2); // Espaço para FP e endereço de retorno

        String funcName = ctx.ID().getText();
        TypeChecker.FunctionSymbol func = functionTable.get(funcName);

        int addr = principalHasAddress() ? 0 : 2;
        int startIndex = addr + code.size(); // Índice onde a função começa no código
        func.setAddress(startIndex);

        // Emite call + halt no início do programa se for principal
        if (funcName.equals("principal")) {
            code.add(globalAllocs, new Instruction(OpCode.halt));
            code.add(globalAllocs, new Instruction1Arg(OpCode.call, func.getAddress()));
        }

        currentFunction = func;

        List<TypeChecker.Parameter> params = func.getParam();
        for (int i = 0; i < params.size(); i++) {
            String paramName = params.get(i).getName();
            localVarScopes.peek().put(paramName, - (params.size() - i));
        }

        visit(ctx.block());

        // Verifica se há return dentro da função
        int funcStartIndex = func.getAddress();
        boolean hasReturn = false;
        for (int i = funcStartIndex; i < code.size(); i++) {
            Instruction inst = code.get(i);
            if (inst instanceof Instruction1Arg ia && ia.getOpCode() == OpCode.ret) {
                hasReturn = true;
                break;
            }
        }

        if (func.getReturnType() == TypeChecker.Type.VOID && !hasReturn) {
            if (!funcName.equals("principal")) {
                emit(OpCode.ret, params.size());
            }
        }

        // Garante ret 0 no final da principal
        if (funcName.equals("principal")) {
            emit(OpCode.ret, 0);
        }

        // Limpar o Scope
        localVarScopes.pop();
        localVarCountStack.pop();
        localOffsetStack.pop();
        return null;
    }

    @Override
    public Void visitBlockStatement(TugaParser.BlockStatementContext ctx) {
        // Cria um scope para block
        localVarScopes.push(new HashMap<>());
        localVarCountStack.push(0);

        // Obtém o offset do scope pai ou 0 se for o escopo mais externo (Se for o "pai")
        int parentOffset = localOffsetStack.isEmpty() ? 0 : localOffsetStack.peek();
        localOffsetStack.push(parentOffset);

        for (TugaParser.Var_declarationContext varDecl : ctx.var_declaration()) {
            visit(varDecl);
        }

        int blockStartIndex = code.size();

        for (TugaParser.StatementContext stmt : ctx.statement()) {
            visit(stmt);
        }

        localVarScopes.pop();
        int count = localVarCountStack.pop();
        localOffsetStack.pop();


        boolean hasReturn = false;
        for (int i = blockStartIndex; i < code.size(); i++) {
            Instruction inst = code.get(i);
            if (inst instanceof Instruction1Arg ia &&
                    (ia.getOpCode() == OpCode.ret || ia.getOpCode() == OpCode.retval)) {
                hasReturn = true;
                break;
            }
        }

        boolean isFunctionBlock = ctx.getParent() instanceof TugaParser.FunctionDeclarationContext;
        boolean isPrincipal = isFunctionBlock && ((TugaParser.FunctionDeclarationContext) ctx.getParent()).ID().getText().equals("principal");

        if (count > 0 && (!isFunctionBlock || isPrincipal) && !hasReturn) {
            emit(OpCode.pop, count);
        }

        return null;
    }



    @Override
    public Void visitReturnStatement(TugaParser.ReturnStatementContext ctx) {
        if (ctx.expr() != null) {
            visit(ctx.expr());

            TypeChecker.Type returnType = currentFunction.getReturnType();
            TypeChecker.Type exprType = typeChecker.getType(ctx.expr());

            // Conversão caso retorne um int em uma função que retorna double
            if (returnType == TypeChecker.Type.DOUBLE && exprType == TypeChecker.Type.INT) {
                emit(OpCode.itod);
            }
        }

        int numArgs = currentFunction.getParam().size();

        if (currentFunction.getReturnType() == TypeChecker.Type.VOID) {
            emit(OpCode.ret, numArgs);
        } else {
            emit(OpCode.retval, numArgs);
        }

        return null;
    }

    @Override
    public Void visitFunctionCall(TugaParser.FunctionCallContext ctx) {
        String funcName = ctx.ID().getText();
        TypeChecker.FunctionSymbol func = functionTable.get(funcName);

        // Visita os argumentos
        if (ctx.expr() != null) {
            List<TypeChecker.Parameter> params = func.getParam();
            List<TugaParser.ExprContext> args = ctx.expr();

            for (int i = 0; i < args.size(); i++) {
                visit(args.get(i));

                // Verifica se precisa de conversão int para double
                TypeChecker.Type argType = typeChecker.getType(args.get(i));
                TypeChecker.Type paramType = params.get(i).getType();

                if (argType == TypeChecker.Type.INT && paramType == TypeChecker.Type.DOUBLE) {
                    emit(OpCode.itod);
                }
            }
        }

        if (func.getAddress() == -1) {
            code.add(new CallPlaceholder(funcName));
        } else {
            emit(OpCode.call, func.getAddress());
        }

        return null;
    }

    /**
     * Trabalho 2
     *
     *
     */

    @Override
    public Void visitVar_declaration(TugaParser.Var_declarationContext ctx) {
        int count = 0;
        for (TerminalNode id : ctx.ID()) {
            String varName = id.getText();

            int offset = localOffsetStack.pop();
            localVarScopes.peek().put(varName, offset);
            localOffsetStack.push(offset + 1);
            count++;
        }

        emit(OpCode.lalloc, count);

        // Update local variable count
        if (!localVarCountStack.isEmpty()) {
            localVarCountStack.push(localVarCountStack.pop() + count);
        }
        return null;
    }


    @Override
    public Void visitAssignmentStatement(TugaParser.AssignmentStatementContext ctx) {
        visit(ctx.expr());
        String varName = ctx.ID().getText();

        // Primeiro verifica se é uma variável local
        for (int i = localVarScopes.size() - 1; i >= 0; i--) {
            if (localVarScopes.get(i).containsKey(varName)) {
                int offset = localVarScopes.get(i).get(varName);
                emit(OpCode.lstore, offset);
                return null;
            }
        }

        // Se não for local, verifica se é global
        if (varIndices.containsKey(varName)) {
            int varIndex = varIndices.get(varName);
            emit(OpCode.gstore, varIndex);
        }

        return null;
    }

    @Override
    public Void visitVarExpr(TugaParser.VarExprContext ctx) {
        String varName = ctx.ID().getText();
        for (int i = localVarScopes.size() - 1; i >= 0; i--) {
            if (localVarScopes.get(i).containsKey(varName)) {
                int offset = localVarScopes.get(i).get(varName);
                emit(OpCode.lload, offset);
                return null;
            }
        }

        // Se não for local, assume que é global
        if (varIndices.containsKey(varName)) {
            emit(OpCode.gload, varIndices.get(varName));
        }
        return null;
    }

    @Override
    public Void visitWhileStatement(TugaParser.WhileStatementContext ctx) {
        if (ctx.expr() == null) {
            return null;
        }

        int start = code.size(); // Start of loop condition

        visit(ctx.expr());

        int jumpfPos = code.size(); // Placeholder for jump if false
        emit(OpCode.jumpf, 0);

        // Visit loop body
        if (ctx.statement() != null) {
            visit(ctx.statement());
        } else if (ctx.block() != null) {
            visit(ctx.block());
        } else {
            return null;
        }

        // volta para o início do loop para verificar a condicao
        emit(OpCode.jump, start);

        int offset = principalHasAddress() ? 0 : 2;
        offset += globalAllocs;

        // Troca o placeholder para sair do loop se a condição for falsa
        ((Instruction1Arg) code.get(jumpfPos)).setArg(code.size() + offset);

        return null;
    }


    @Override
    public Void visitIfStatement(TugaParser.IfStatementContext ctx) {
        visit(ctx.expr());

        // Placeholder
        int jumpfPos = code.size();
        emit(OpCode.jumpf, 0);

        visit(ctx.thenBody);
        int offset = principalHasAddress() ? 0 : 2;
        offset += globalAllocs;

        if (ctx.elseBody != null) {
            int jumpPos = code.size();
            emit(OpCode.jump, 0);

            // Voltar para o inicio do else
            ((Instruction1Arg) code.get(jumpfPos)).setArg(code.size() + offset);

            visit(ctx.elseBody);

            // Pular o 'else' e continuar após o bloco
            ((Instruction1Arg) code.get(jumpPos)).setArg(code.size() + offset);
        } else {
            // Salta diretamente para após o 'then'
            ((Instruction1Arg) code.get(jumpfPos)).setArg(code.size() + offset);
        }

        return null;
    }


    @Override
    public Void visitIntLiteral(TugaParser.IntLiteralContext ctx)
    {
        emit(OpCode.iconst, Integer.valueOf(ctx.INT().getText()));
        return null;
    }

    @Override
    public Void visitDoubleLiteral(TugaParser.DoubleLiteralContext ctx)
    {
        double value = Double.valueOf(ctx.DOUBLE().getText());
        int poolIndex = addToConstantPool(value);
        emit(OpCode.dconst, poolIndex);
        return null;
    }

    @Override
    public Void visitStringLiteral(TugaParser.StringLiteralContext ctx)
    {
        int poolIndex = addToConstantPool(ctx.STRING().getText());
        emit(OpCode.sconst, poolIndex);
        return null;
    }

    @Override
    public Void visitBoolLiteral(TugaParser.BoolLiteralContext ctx)
    {
        String boolText = ctx.BOOLEAN().getText();
        boolean value = "verdadeiro".equals(boolText);
        emit(value ? OpCode.tconst : OpCode.fconst);
        return null;
    }

    @Override
    public Void visitPrintStatement(TugaParser.PrintStatementContext ctx)
    {
        visit(ctx.expr());
        TypeChecker.Type type = typeChecker.getType(ctx.expr());

        switch (type)
        {
            case INT: emit(OpCode.iprint); break;
            case DOUBLE: emit(OpCode.dprint); break;
            case BOOLEAN: emit(OpCode.bprint); break;
            case STRING: emit(OpCode.sprint); break;
            default: break;
        }
        return null;
    }

    @Override
    public Void visitUnaryExpr(TugaParser.UnaryExprContext ctx) {
        visit(ctx.expr());
        TypeChecker.Type exprType = typeChecker.getType(ctx.expr());

        if (ctx.MINUS() != null) {
            switch(exprType) {
                case INT: emit(OpCode.iuminus); break;
                case DOUBLE: emit(OpCode.duminus); break;
            }
        }
        else if (ctx.NAO() != null) {
            if (exprType == TypeChecker.Type.BOOLEAN)
                emit(OpCode.not);
        }
        return null;
    }

    @Override
    public Void visitMulDivModExpr(TugaParser.MulDivModExprContext ctx)
    {
        TypeChecker.Type leftType = typeChecker.getType(ctx.expr(0));
        TypeChecker.Type rightType = typeChecker.getType(ctx.expr(1));
        String op = ctx.op.getText();

        if (leftType == TypeChecker.Type.DOUBLE || rightType == TypeChecker.Type.DOUBLE)
        {
            visit(ctx.expr(0));
            if (!(leftType == TypeChecker.Type.DOUBLE)) emit(OpCode.itod);
            visit(ctx.expr(1));
            if (!(rightType == TypeChecker.Type.DOUBLE)) emit(OpCode.itod);
        }
        else
        {
            visit(ctx.expr(0));
            visit(ctx.expr(1));
        }

        boolean isDouble = leftType == TypeChecker.Type.DOUBLE || rightType == TypeChecker.Type.DOUBLE;

        switch (op) {
            case "*":
                emit(isDouble ? OpCode.dmult : OpCode.imult);
                break;
            case "/":
                emit(isDouble ? OpCode.ddiv : OpCode.idiv);
                break;
            case "%":
                emit(OpCode.imod);
                break;
        }
        return null;
    }

    @Override
    public Void visitAddSubExpr(TugaParser.AddSubExprContext ctx) {
        TypeChecker.Type leftType = typeChecker.getType(ctx.expr(0));
        TypeChecker.Type rightType = typeChecker.getType(ctx.expr(1));
        String op = ctx.op.getText();

        // Handle string concatenation
        if (op.equals("+") && (leftType == TypeChecker.Type.STRING || rightType == TypeChecker.Type.STRING)) {
            visit(ctx.expr(0));
            if (leftType != TypeChecker.Type.STRING) {
                if (leftType == TypeChecker.Type.DOUBLE)
                    emit(OpCode.dtos);
                else if (leftType == TypeChecker.Type.BOOLEAN)
                    emit(OpCode.btos);
                else
                    emit(OpCode.itos);
            }

            visit(ctx.expr(1));
            if (rightType != TypeChecker.Type.STRING) {
                if (rightType == TypeChecker.Type.DOUBLE)
                    emit(OpCode.dtos);
                else if (rightType == TypeChecker.Type.BOOLEAN)
                    emit(OpCode.btos);
                else
                    emit(OpCode.itos);
            }

            emit(OpCode.sconcat);
            return null;
        }

        if (leftType == TypeChecker.Type.DOUBLE || rightType == TypeChecker.Type.DOUBLE) {
            visit(ctx.expr(0));
            if (!(leftType == TypeChecker.Type.DOUBLE)) emit(OpCode.itod);
            visit(ctx.expr(1));
            if (!(rightType == TypeChecker.Type.DOUBLE)) emit(OpCode.itod);
        }
        else
        {
            visit(ctx.expr(0));
            visit(ctx.expr(1));
        }

        boolean isDouble = leftType == TypeChecker.Type.DOUBLE || rightType == TypeChecker.Type.DOUBLE;

        if (op.equals("+")) {
            emit(isDouble ? OpCode.dadd : OpCode.iadd);
        } else {
            emit(isDouble ? OpCode.dsub : OpCode.isub);
        }
        return null;
    }

    @Override
    public Void visitRelationalExpr(TugaParser.RelationalExprContext ctx) {
        TypeChecker.Type leftType = typeChecker.getType(ctx.expr(0));
        TypeChecker.Type rightType = typeChecker.getType(ctx.expr(1));
        String op = ctx.op.getText();

        boolean isDouble = leftType == TypeChecker.Type.DOUBLE || rightType == TypeChecker.Type.DOUBLE;

        int leftIndex = 0;
        int rightIndex = 1;
        boolean swapOperands = op.equals(">") || op.equals(">=");

        if (swapOperands) {
            leftIndex = 1;
            rightIndex = 0;
        }

        if (isDouble) {
            visit(ctx.expr(leftIndex));
            if (typeChecker.getType(ctx.expr(leftIndex)) == TypeChecker.Type.INT) {
                emit(OpCode.itod);
            }

            visit(ctx.expr(rightIndex));
            if (typeChecker.getType(ctx.expr(rightIndex)) == TypeChecker.Type.INT) {
                emit(OpCode.itod);
            }
        } else {
            visit(ctx.expr(leftIndex));
            visit(ctx.expr(rightIndex));
        }
        switch (op) {
            case "<":
            case ">":
                emit(isDouble ? OpCode.dlt : OpCode.ilt);
                break;
            case "<=":
            case ">=":
                emit(isDouble ? OpCode.dleq : OpCode.ileq);
                break;
        }

        return null;
    }

    @Override
    public Void visitEqualityExpr(TugaParser.EqualityExprContext ctx) {
        TypeChecker.Type leftType = typeChecker.getType(ctx.expr(0));
        TypeChecker.Type rightType = typeChecker.getType(ctx.expr(1));
        String op = ctx.op.getText();

        if (leftType == TypeChecker.Type.STRING && rightType == TypeChecker.Type.STRING)
        {
            visit(ctx.expr(0));
            visit(ctx.expr(1));
            emit(op.equals("igual") ? OpCode.seq : OpCode.sneq);
            return null;
        }
        else if (leftType == TypeChecker.Type.BOOLEAN && rightType == TypeChecker.Type.BOOLEAN)
        {
            visit(ctx.expr(0));
            visit(ctx.expr(1));
            emit(op.equals("igual") ? OpCode.beq : OpCode.bneq);
            return null;
        }

        boolean isDouble = leftType == TypeChecker.Type.DOUBLE || rightType == TypeChecker.Type.DOUBLE;

        if (isDouble)
        {
            visit(ctx.expr(0));
            if (!(leftType == TypeChecker.Type.DOUBLE)) emit(OpCode.itod);
            visit(ctx.expr(1));
            if (!(rightType == TypeChecker.Type.DOUBLE)) emit(OpCode.itod);
        }
        else
        {
            visit(ctx.expr(0));
            visit(ctx.expr(1));
        }

        emit(op.equals("igual") ? (isDouble ? OpCode.deq : OpCode.ieq) : (isDouble ? OpCode.dneq : OpCode.ineq));
        return null;
    }

    @Override
    public Void visitAndExpr(TugaParser.AndExprContext ctx) {
        visit(ctx.expr(0));
        visit(ctx.expr(1));
        emit(OpCode.and);
        return null;
    }

    @Override
    public Void visitOrExpr(TugaParser.OrExprContext ctx) {
        visit(ctx.expr(0));
        visit(ctx.expr(1));
        emit(OpCode.or);
        return null;
    }

    @Override
    public Void visitParenExpr(TugaParser.ParenExprContext ctx) {
        return visit(ctx.expr());
    }

    private int addToConstantPool(Object value) {
        int index = constantPool.indexOf(value);
        if (index == -1) {
            index = constantPool.size();
            constantPool.add(value);
        }
        return index;
    }

    public void emit(OpCode opc) {
        code.add( new Instruction(opc) );
    }

    public void emit(OpCode opc, int val) {
        code.add( new Instruction1Arg(opc, val) );
    }

    private void writeConstantPool(DataOutputStream dout) throws IOException {
        dout.writeInt(constantPool.size());

        // Write each constant
        for (Object constant : constantPool) {
            if (constant instanceof Double) {
                dout.writeByte(1);
                dout.writeDouble((Double)constant);
            }
            else if (constant instanceof String) {
                dout.writeByte(3);
                String str = (String)constant;
                dout.writeInt(str.length());
                for (char c : str.toCharArray()) {
                    dout.writeChar(c);
                }
            }
        }
    }

    public void dumpCode() {
        System.out.println("*** Constant pool ***");
        for(int i = 0; i < constantPool.size(); i++)
            System.out.println(i + ": " + constantPool.get(i));
        System.out.println("*** Instructions ***");
        for (int i=0; i< code.size(); i++)
            System.out.println( i + ": " + code.get(i) );
    }

    public void saveBytecodes(String filename) throws IOException {
        try (DataOutputStream dout = new DataOutputStream(new FileOutputStream(filename))) {
            writeConstantPool(dout);

            for (Instruction inst : code) {
                inst.writeTo(dout);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}
