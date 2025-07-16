// Generated from C:/Users/Rodrigo/Desktop/Tuga/src/Tuga.g4 by ANTLR 4.13.2

    package Tuga;

import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link TugaParser}.
 */
public interface TugaListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link TugaParser#program}.
	 * @param ctx the parse tree
	 */
	void enterProgram(TugaParser.ProgramContext ctx);
	/**
	 * Exit a parse tree produced by {@link TugaParser#program}.
	 * @param ctx the parse tree
	 */
	void exitProgram(TugaParser.ProgramContext ctx);
	/**
	 * Enter a parse tree produced by {@link TugaParser#var_declaration}.
	 * @param ctx the parse tree
	 */
	void enterVar_declaration(TugaParser.Var_declarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link TugaParser#var_declaration}.
	 * @param ctx the parse tree
	 */
	void exitVar_declaration(TugaParser.Var_declarationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code FunctionDeclaration}
	 * labeled alternative in {@link TugaParser#function_declaration}.
	 * @param ctx the parse tree
	 */
	void enterFunctionDeclaration(TugaParser.FunctionDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code FunctionDeclaration}
	 * labeled alternative in {@link TugaParser#function_declaration}.
	 * @param ctx the parse tree
	 */
	void exitFunctionDeclaration(TugaParser.FunctionDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link TugaParser#param_list}.
	 * @param ctx the parse tree
	 */
	void enterParam_list(TugaParser.Param_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link TugaParser#param_list}.
	 * @param ctx the parse tree
	 */
	void exitParam_list(TugaParser.Param_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link TugaParser#param}.
	 * @param ctx the parse tree
	 */
	void enterParam(TugaParser.ParamContext ctx);
	/**
	 * Exit a parse tree produced by {@link TugaParser#param}.
	 * @param ctx the parse tree
	 */
	void exitParam(TugaParser.ParamContext ctx);
	/**
	 * Enter a parse tree produced by {@link TugaParser#tipo}.
	 * @param ctx the parse tree
	 */
	void enterTipo(TugaParser.TipoContext ctx);
	/**
	 * Exit a parse tree produced by {@link TugaParser#tipo}.
	 * @param ctx the parse tree
	 */
	void exitTipo(TugaParser.TipoContext ctx);
	/**
	 * Enter a parse tree produced by {@link TugaParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterStatement(TugaParser.StatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link TugaParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitStatement(TugaParser.StatementContext ctx);
	/**
	 * Enter a parse tree produced by the {@code PrintStatement}
	 * labeled alternative in {@link TugaParser#print}.
	 * @param ctx the parse tree
	 */
	void enterPrintStatement(TugaParser.PrintStatementContext ctx);
	/**
	 * Exit a parse tree produced by the {@code PrintStatement}
	 * labeled alternative in {@link TugaParser#print}.
	 * @param ctx the parse tree
	 */
	void exitPrintStatement(TugaParser.PrintStatementContext ctx);
	/**
	 * Enter a parse tree produced by the {@code AssignmentStatement}
	 * labeled alternative in {@link TugaParser#assignment}.
	 * @param ctx the parse tree
	 */
	void enterAssignmentStatement(TugaParser.AssignmentStatementContext ctx);
	/**
	 * Exit a parse tree produced by the {@code AssignmentStatement}
	 * labeled alternative in {@link TugaParser#assignment}.
	 * @param ctx the parse tree
	 */
	void exitAssignmentStatement(TugaParser.AssignmentStatementContext ctx);
	/**
	 * Enter a parse tree produced by the {@code WhileStatement}
	 * labeled alternative in {@link TugaParser#while}.
	 * @param ctx the parse tree
	 */
	void enterWhileStatement(TugaParser.WhileStatementContext ctx);
	/**
	 * Exit a parse tree produced by the {@code WhileStatement}
	 * labeled alternative in {@link TugaParser#while}.
	 * @param ctx the parse tree
	 */
	void exitWhileStatement(TugaParser.WhileStatementContext ctx);
	/**
	 * Enter a parse tree produced by the {@code IfStatement}
	 * labeled alternative in {@link TugaParser#if}.
	 * @param ctx the parse tree
	 */
	void enterIfStatement(TugaParser.IfStatementContext ctx);
	/**
	 * Exit a parse tree produced by the {@code IfStatement}
	 * labeled alternative in {@link TugaParser#if}.
	 * @param ctx the parse tree
	 */
	void exitIfStatement(TugaParser.IfStatementContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ReturnStatement}
	 * labeled alternative in {@link TugaParser#return}.
	 * @param ctx the parse tree
	 */
	void enterReturnStatement(TugaParser.ReturnStatementContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ReturnStatement}
	 * labeled alternative in {@link TugaParser#return}.
	 * @param ctx the parse tree
	 */
	void exitReturnStatement(TugaParser.ReturnStatementContext ctx);
	/**
	 * Enter a parse tree produced by the {@code FunctionCall}
	 * labeled alternative in {@link TugaParser#function_call}.
	 * @param ctx the parse tree
	 */
	void enterFunctionCall(TugaParser.FunctionCallContext ctx);
	/**
	 * Exit a parse tree produced by the {@code FunctionCall}
	 * labeled alternative in {@link TugaParser#function_call}.
	 * @param ctx the parse tree
	 */
	void exitFunctionCall(TugaParser.FunctionCallContext ctx);
	/**
	 * Enter a parse tree produced by the {@code EmptyStatement}
	 * labeled alternative in {@link TugaParser#empty}.
	 * @param ctx the parse tree
	 */
	void enterEmptyStatement(TugaParser.EmptyStatementContext ctx);
	/**
	 * Exit a parse tree produced by the {@code EmptyStatement}
	 * labeled alternative in {@link TugaParser#empty}.
	 * @param ctx the parse tree
	 */
	void exitEmptyStatement(TugaParser.EmptyStatementContext ctx);
	/**
	 * Enter a parse tree produced by the {@code BlockStatement}
	 * labeled alternative in {@link TugaParser#block}.
	 * @param ctx the parse tree
	 */
	void enterBlockStatement(TugaParser.BlockStatementContext ctx);
	/**
	 * Exit a parse tree produced by the {@code BlockStatement}
	 * labeled alternative in {@link TugaParser#block}.
	 * @param ctx the parse tree
	 */
	void exitBlockStatement(TugaParser.BlockStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link TugaParser#body}.
	 * @param ctx the parse tree
	 */
	void enterBody(TugaParser.BodyContext ctx);
	/**
	 * Exit a parse tree produced by {@link TugaParser#body}.
	 * @param ctx the parse tree
	 */
	void exitBody(TugaParser.BodyContext ctx);
	/**
	 * Enter a parse tree produced by the {@code AndExpr}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterAndExpr(TugaParser.AndExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code AndExpr}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitAndExpr(TugaParser.AndExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code RelationalExpr}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterRelationalExpr(TugaParser.RelationalExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code RelationalExpr}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitRelationalExpr(TugaParser.RelationalExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code UnaryExpr}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterUnaryExpr(TugaParser.UnaryExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code UnaryExpr}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitUnaryExpr(TugaParser.UnaryExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code OrExpr}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterOrExpr(TugaParser.OrExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code OrExpr}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitOrExpr(TugaParser.OrExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code FunctionCallExpr}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterFunctionCallExpr(TugaParser.FunctionCallExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code FunctionCallExpr}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitFunctionCallExpr(TugaParser.FunctionCallExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code EqualityExpr}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterEqualityExpr(TugaParser.EqualityExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code EqualityExpr}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitEqualityExpr(TugaParser.EqualityExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code BoolLiteral}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterBoolLiteral(TugaParser.BoolLiteralContext ctx);
	/**
	 * Exit a parse tree produced by the {@code BoolLiteral}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitBoolLiteral(TugaParser.BoolLiteralContext ctx);
	/**
	 * Enter a parse tree produced by the {@code StringLiteral}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterStringLiteral(TugaParser.StringLiteralContext ctx);
	/**
	 * Exit a parse tree produced by the {@code StringLiteral}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitStringLiteral(TugaParser.StringLiteralContext ctx);
	/**
	 * Enter a parse tree produced by the {@code MulDivModExpr}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterMulDivModExpr(TugaParser.MulDivModExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code MulDivModExpr}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitMulDivModExpr(TugaParser.MulDivModExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code VarExpr}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterVarExpr(TugaParser.VarExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code VarExpr}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitVarExpr(TugaParser.VarExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code IntLiteral}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterIntLiteral(TugaParser.IntLiteralContext ctx);
	/**
	 * Exit a parse tree produced by the {@code IntLiteral}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitIntLiteral(TugaParser.IntLiteralContext ctx);
	/**
	 * Enter a parse tree produced by the {@code DoubleLiteral}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterDoubleLiteral(TugaParser.DoubleLiteralContext ctx);
	/**
	 * Exit a parse tree produced by the {@code DoubleLiteral}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitDoubleLiteral(TugaParser.DoubleLiteralContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ParenExpr}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterParenExpr(TugaParser.ParenExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ParenExpr}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitParenExpr(TugaParser.ParenExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code AddSubExpr}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterAddSubExpr(TugaParser.AddSubExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code AddSubExpr}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitAddSubExpr(TugaParser.AddSubExprContext ctx);
}