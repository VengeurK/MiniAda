package net.slimevoid.miniada.syntax;

import net.slimevoid.miniada.TokenList;
import net.slimevoid.miniada.typing.Environment;
import net.slimevoid.miniada.typing.Type;
import net.slimevoid.miniada.typing.TypeException;

public class ExpressionUnOperator extends Expression {

	public final Operator op;
	public final Expression e;
	
	private ExpressionUnOperator(Operator op, Expression e) {
		this.op = op;
		this.e = e;
		setFirstToken(op.firstTok);
		setLastToken(e.lastTok);
	}
	
	public static ExpressionUnOperator matchExpressionUnOperator(TokenList toks)
			throws MatchException {
		Operator op = Operator.matchOperator(toks);
		Expression e = Expression.matchExpression(toks);
		return new ExpressionUnOperator(op, e);
	}
	
	@Override
	public String toString() {
		return op+" "+e;
	}

	@Override
	public Type computeType(Environment env) throws TypeException {
		Type eType = e.getType(env);
		Type opType = op.getOperandType();
		if(opType != null && !eType.equals(opType))
			throw new TypeException(e, "Expected type "+opType
					+" while expression has type "+eType);
		return op.getResultingType();
	}

	@Override
	public Object value(Environment env) {
		switch(op.type) {
		case MINUS:
			return -e.valueInt(env);
		case NOT:
			return !e.valueBool(env);
		default:
			return null;
		}
	}
}
