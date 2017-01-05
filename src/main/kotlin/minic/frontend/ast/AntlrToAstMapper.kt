package minic.frontend.ast

import minic.frontend.antlr.MiniCLexer
import minic.frontend.antlr.MiniCParser.*
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.Token

// ANTLR parse tree mapping to AST

fun Token.startPoint() = Point(line, charPositionInLine)
fun Token.endPoint() = Point(line, charPositionInLine + text.length)

class AntlrToAstMapper(val setPosition: Boolean = true) {

    fun map(antlrProgramContext: ProgramContext) : Program {
        return antlrProgramContext.toAst()
    }

    fun ParserRuleContext.position() : Position? {
        return if (setPosition) Position(start.startPoint(), stop.endPoint()) else null
    }

    fun ProgramContext.toAst() : Program = Program(this.statement().map { it.toAst() }, position())

    fun StatementContext.toAst(): Statement = when (this) {
        is VariableDeclarationStatementContext -> VariableDeclaration(declaration().type().toAst(), declaration().Identifier().text, declaration().expression().toAst(), position())
        is AssignmentStatementContext -> Assignment(assignment().Identifier().text, assignment().expression().toAst(), position())
        else -> throw UnsupportedOperationException(this.javaClass.canonicalName)
    }

    fun ExpressionContext.toAst(): Expression = when (this) {
        is LiteralExpressionContext -> literal().toAst()
        is BinaryOperationContext -> toAst()
        is UnaryOperationContext -> toAst()
        is ParenthesesExpressionContext -> parExpression().expression().toAst()
        is VariableReferenceContext -> VariableReference(text, position())
        else -> throw UnsupportedOperationException(this.javaClass.canonicalName)
    }

    fun LiteralContext.toAst(): Expression = when (this) {
        is IntContext -> IntLiteral(text.toInt(), position())
        is FloatContext -> FloatLiteral(text.toDouble(), position())
        is BooleanContext -> BooleanLiteral(text.toBoolean(), position())
        is StringContext -> StringLiteral(text.substring(1, text.lastIndex), position())
        else -> throw UnsupportedOperationException(this.javaClass.canonicalName)
    }

    fun UnaryOperationContext.toAst(): Expression = when (op.type) {
        MiniCLexer.NOT -> NotExpression(expression().toAst(), position())
        MiniCLexer.MINUS -> UnaryMinusExpression(expression().toAst(), position())
        else -> throw UnsupportedOperationException(this.javaClass.canonicalName)
    }

    fun BinaryOperationContext.toAst(): BinaryExpression = when (op.type) {
        MiniCLexer.PLUS -> AdditionExpression(left.toAst(), right.toAst(), position())
        MiniCLexer.MINUS -> SubtractionExpression(left.toAst(), right.toAst(), position())
        MiniCLexer.MUL -> MultiplicationExpression(left.toAst(), right.toAst(), position())
        MiniCLexer.DIV -> DivisionExpression(left.toAst(), right.toAst(), position())
        MiniCLexer.MOD -> ModExpression(left.toAst(), right.toAst(), position())

        MiniCLexer.AND -> AndExpression(left.toAst(), right.toAst(), position())
        MiniCLexer.OR -> OrExpression(left.toAst(), right.toAst(), position())

        else -> throw UnsupportedOperationException(this.javaClass.canonicalName)
    }

    fun TypeContext.toAst(): Type = when (this) {
        is IntTypeContext -> IntType(position())
        is DoubleTypeContext -> DoubleType(position())
        is BooleanTypeContext -> BooleanType(position())
        is StringTypeContext -> StringType(position())
        else -> throw UnsupportedOperationException(this.javaClass.canonicalName)
    }
}