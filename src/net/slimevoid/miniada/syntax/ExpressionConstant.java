package net.slimevoid.miniada.syntax;

import net.slimevoid.miniada.TokenList;
import net.slimevoid.miniada.TokenList.OutOfBoundsException;
import net.slimevoid.miniada.execution.ASMBuilder;
import net.slimevoid.miniada.execution.ASMConst;
import net.slimevoid.miniada.interpert.Scope;
import net.slimevoid.miniada.interpert.Value;
import net.slimevoid.miniada.interpert.ValueAccess;
import net.slimevoid.miniada.token.ConstChar;
import net.slimevoid.miniada.token.ConstInt;
import net.slimevoid.miniada.token.Keyword;
import net.slimevoid.miniada.token.Keyword.KeywordType;
import net.slimevoid.miniada.token.Yytoken;
import net.slimevoid.miniada.typing.Environment;
import net.slimevoid.miniada.typing.Type;
import net.slimevoid.miniada.typing.TypeException;
import net.slimevoid.miniada.typing.TypePrimitive;

public class ExpressionConstant extends Expression {
	
	public final Object value;
	public final TypePrimitive type;

	private ExpressionConstant(Object value) {
		this.value = value;
		if(value == null) type = TypePrimitive.NULL;
		else if(value instanceof Integer) type = TypePrimitive.INTEGER;
		else if(value instanceof Character) type = TypePrimitive.CHARACTER;
		else if(value instanceof Boolean) type = TypePrimitive.BOOLEAN;
		else throw new RuntimeException(value+" isn't a valid constant");
	}
	
	public static ExpressionConstant matchExpressionConstant(TokenList toks) 
			throws MatchException {
		Yytoken tok;
		try {
			tok = toks.next();
		} catch (OutOfBoundsException e) {
			throw new MatchException(toks.cur(), "Expected expression");
		}
		ExpressionConstant cst = null; 
		if(tok instanceof ConstInt)
			cst = new ExpressionConstant(((ConstInt) tok).value);
		if(tok instanceof ConstChar)
			cst = new ExpressionConstant(((ConstChar) tok).value);
		if(tok instanceof Keyword) {
			if(((Keyword) tok).type == KeywordType.TRUE)
				cst = new ExpressionConstant(true);
			if(((Keyword) tok).type == KeywordType.FALSE)
				cst = new ExpressionConstant(false);
			if(((Keyword) tok).type == KeywordType.NULL)
				cst = new ExpressionConstant(null);
		}
		if(cst == null) throw new MatchException(tok, "Expected expression");
		cst.setFirstToken(tok);
		cst.setLastToken(tok);
		return cst;
	}
	
	@Override
	public String toString() {
		return ""+value;
	}

	@Override
	public Type computeType(Environment env) throws TypeException {
		return type;
	}
	
	@Override
	public Value value(Scope s) {
		if(type != TypePrimitive.NULL) return super.value(s);
		else return new ValueAccess(null);
	}

	@Override
	public Object valuePrim(Scope s) {
		if(type != TypePrimitive.NULL) return value;
		else return super.valuePrim(s);
	}

	@Override
	public void buildAsm(ASMBuilder asm, Environment env) {
		int cst;
		if(value instanceof Integer)
			cst = (int) value;
		else if(value instanceof Character)
			cst = (int) (char) value;
		else if(value instanceof Boolean)
			cst = ((boolean)value) ? 1 : 0;
		else
			cst = 0;
		asm.push(new ASMConst(cst));
	}
}
