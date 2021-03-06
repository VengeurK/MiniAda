package net.slimevoid.miniada;

import java.util.Stack;

import net.slimevoid.miniada.syntax.MatchException;
import net.slimevoid.miniada.token.EOF;
import net.slimevoid.miniada.token.Keyword;
import net.slimevoid.miniada.token.Keyword.KeywordType;
import net.slimevoid.miniada.token.Symbol;
import net.slimevoid.miniada.token.Symbol.SymbolType;
import net.slimevoid.miniada.token.Yytoken;

public class TokenList {
	
	private int cur;
	private int bound;
	private Stack<Integer> savedPos;
	private Yytoken[] tokens;
	
	public TokenList(Yytoken[] tokens) {
		this.tokens = tokens;
		for(int i = 0; i < tokens.length; i ++) {
			tokens[i].posInList = i;
		}
		cur = 0;
		resetBound();
		savedPos = new Stack<>();
	}
	
	public void savePos() {
		savedPos.push(cur);
	}
	
	public void revert() {
		if(savedPos.isEmpty()) 
			throw new IllegalStateException("No position saved");
		cur = savedPos.pop();
	}
	
	public void dropSave() {
		savedPos.pop();
	}
	
	public Yytoken cur() {
		return tokens[cur];
	}
	
	public Yytoken next() throws OutOfBoundsException {
		if(outOfBounds()) {
			throw new OutOfBoundsException( 
					"Internal error (out of bounds)");
		}
		return nextBypassBound();
	}
	
	public Yytoken nextBoundChecked() {
		assert(!outOfBounds());
		return nextBypassBound();
	}
	
	public Yytoken nextBypassBound() {
		return tokens[cur++];
	}
	
	public void prev() {
		cur --;
	}
	
	public void setBound() {
		bound = cur;
	}
	
	public void resetBound() {
		bound = tokens.length-1;
	}

	public boolean nextIsOcc(SymbolType...types) {
		return nextIsOcc(null, types);
	}
	
	public boolean nextIsOcc(KeywordType kt, SymbolType...types) {
		Yytoken tok;
		try {
			tok = next();
		} catch (OutOfBoundsException e) {
			return false;
		}
		prev();
		if(tok instanceof Symbol)
			for(SymbolType t : types)
				if(((Symbol) tok).type == t)
					return true;
		if(tok instanceof Keyword && ((Keyword) tok).type == kt)
			return true;
		return false;
	}
	
	/**
	 * If return is true, the previous position is saved
	 * Otherwise, position is unchanged
	 */
	public boolean gotoLastOcc(SymbolType...types) {
		return gotoLastOcc(null, types);
	}
	
	/**
	 * If return is true, the previous position is saved
	 * Otherwise, position is unchanged
	 */
	public boolean gotoLastOcc(KeywordType keytype, SymbolType...types) {
		return gotoLastOcc(keytype, null, types);
	}
	
	/**
	 * If return is true, the previous position is saved
	 * Otherwise, position is unchanged
	 */
	public boolean gotoLastOcc(KeywordType keytypeA, KeywordType keytypeB,
			SymbolType...types) {
		Yytoken tok;
		savePos();
		int parDepth = 0;
		boolean found = false;
		try {
			while(!((tok = next()) instanceof EOF)) {
				if(tok instanceof Symbol) {
					for(SymbolType type : types) {
						if(((Symbol) tok).type == type && parDepth == 0) {
							if(found) dropSave();
							found = true;
							prev(); savePos(); next();
						}
					}
					if(((Symbol) tok).type == SymbolType.LPAR) parDepth++;
					if(((Symbol) tok).type == SymbolType.RPAR) parDepth--;
				}
				if(tok instanceof Keyword)
					if((((Keyword)tok).type == keytypeA || 
					   ((Keyword)tok).type == keytypeB) && parDepth == 0) {
						if(found) dropSave();
						found = true;
						prev(); savePos(); next();
					}
			}
		} catch (OutOfBoundsException e) {
		}
		revert();
		return found;
 	}
	
	/**
	 * If return is true, the previous position is saved
	 * Otherwise, position is unchanged
	 */
	public boolean gotoFirstOcc(SymbolType...types) {
		return gotoFirstOcc(null, types);
	}
	
	/**
	 * If return is true, the previous position is saved
	 * Otherwise, position is unchanged
	 */
	public boolean gotoFirstOcc(KeywordType keytype, SymbolType...types) {
		return gotoFirstOcc(keytype, null, types);
	}
	
	/**
	 * If return is true, the previous position is saved
	 * Otherwise, position is unchanged
	 */
	public boolean gotoFirstOcc(KeywordType keytypeA, KeywordType keytypeB,
			SymbolType...types) {
		Yytoken tok;
		savePos();
		int parDepth = 0;
		try {
			while(!((tok = next()) instanceof EOF)) {
				if(tok instanceof Symbol)
					for(SymbolType type : types) {
						if(((Symbol) tok).type == type && parDepth == 0) {
							prev();
							return true;
						}
						if(((Symbol) tok).type == SymbolType.LPAR) parDepth++;
						if(((Symbol) tok).type == SymbolType.RPAR) parDepth--;
					}
				if(tok instanceof Keyword)
					if((((Keyword)tok).type == keytypeA || 
					((Keyword)tok).type == keytypeB) && parDepth == 0) {
						prev();
						return true;
					}
			}
		} catch (OutOfBoundsException e) {
		}
		revert();
		return false;
	}
	
	public void goToBound() {
		cur = bound;
	}
	
	public void goTo(Yytoken tok) {
		cur = tok.posInList;
	}
	
	public void checkStackSize() {
		assert(savedPos.size() == 0);
	}
	
	public void checkConsumed() throws MatchException {
		try {
			Yytoken tok = next(); prev();
			throw new MatchException(tok, "Unexpected token");
		} catch (OutOfBoundsException e) {} 
	}
	
	public void printState() {
		System.out.println(cur+" - "+bound);
	}

	public boolean outOfBounds() {
		return cur > bound;
	}
	
	public boolean isBounded() {
		return bound < tokens.length-1;
	}
	
	public static class OutOfBoundsException extends Exception {

		private static final long serialVersionUID = 1L;
		
		private String msg;
		
		public OutOfBoundsException(String msg) {
			this.msg = msg;
		}
		
		@Override
		public String getMessage() {
			return msg;
		}
	}
}
