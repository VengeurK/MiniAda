package net.slimevoid.miniada.syntax;

import net.slimevoid.miniada.Compiler;
import net.slimevoid.miniada.TokenList;
import net.slimevoid.miniada.execution.ASMBuilder;
import net.slimevoid.miniada.execution.ASMBuilder.Register;
import net.slimevoid.miniada.execution.ASMMem;
import net.slimevoid.miniada.execution.ASMVar;
import net.slimevoid.miniada.token.Identifier;
import net.slimevoid.miniada.token.Symbol.SymbolType;
import net.slimevoid.miniada.token.Yytoken;
import net.slimevoid.miniada.typing.Environment;
import net.slimevoid.miniada.typing.Type;
import net.slimevoid.miniada.typing.TypeAccess;
import net.slimevoid.miniada.typing.TypeDefined;
import net.slimevoid.miniada.typing.TypeException;
import net.slimevoid.miniada.typing.TypeRecord;
import net.slimevoid.miniada.typing.Typeable;

public class Access extends SyntaxNode implements Typeable {
	
	public final Identifier id;
	public final Expression from;
	
	public DeclarationFunction func;
	
	public boolean alterable;
	public int offset;
	
	private Access(Identifier id, Yytoken fst, Yytoken lst) {
		this(id, null, fst, lst);
	}

	private Access(Identifier id, Expression from, Yytoken fst, Yytoken lst) {
		this.id = id;
		this.from = from;
		setFirstToken(fst);
		setLastToken(lst);
	}

	public static Access matchAccess(TokenList toks) 
			throws MatchException {
		if(toks.gotoLastOcc(SymbolType.DOT)) {
			Compiler.matchSymbol(toks, SymbolType.DOT);
			Identifier id = Compiler.matchIdent(toks);
			toks.checkConsumed();
			toks.prev(); toks.prev(); toks.prev(); toks.setBound();
			toks.revert();
			Expression from = Expression.matchExpression(toks);
			return new Access(id, from, from.firstTok, id);
		}
		Identifier id = Compiler.matchIdent(toks);
		toks.checkConsumed();
		return new Access(id, id, id);
	}
	
	@Override
	public String toString() {
		if(from == null) return id.name;
		else return from+"."+id.name;
	}

	@Override
	public Type getType(Environment env) throws TypeException {
		alterable = false;
		if(from == null) {
			try {
				func = env.getFunction(id);
				return func.retType;
			} catch(TypeException e) {}
			alterable = env.isAlterable(id);
			return env.getVarType(id);
		}
		Type t = from.getType(env);
		String typeName = t.getName();
		while(true)
			if(t instanceof TypeDefined)
				t = ((TypeDefined) t).getDefinition();
			else if(t instanceof TypeAccess) {
				t = ((TypeAccess) t).type;
				alterable = true;
			}
			else break;
		if(!(t instanceof TypeRecord))
			throw new TypeException(from, 
					"Expected record or access to record");
		TypeRecord rec = (TypeRecord) t;
		alterable = from.isAlterable() || alterable;
		if(!rec.hasMember(id.name))
			throw new TypeException(id, typeName+" has no field "+id);
		offset = rec.getMemberOffset(id.name);
		return rec.getMemberType(id.name);
	}
	
	public ASMMem getAsmOperand(ASMBuilder asm, Environment env) {
		assert(alterable);
		if(from != null) {
			if(from.getComputedType().isAccess()) {
				Register r = asm.getTmpReg();
				from.buildAsm(asm, env);
				asm.pop(r);
				return new ASMMem(-offset, r);
			}
			assert(from instanceof ExpressionAccess);
			Access a = ((ExpressionAccess) from).access;
			ASMMem mem = a.getAsmOperand(asm, env);
			mem.offset(offset);
			return mem;
		}
		assert(func == null);
		return new ASMVar(id, env);
	}
}
