package net.slimevoid.miniada.typing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.slimevoid.miniada.interpert.Value;
import net.slimevoid.miniada.interpert.ValueAccess;
import net.slimevoid.miniada.interpert.ValuePrimitive;
import net.slimevoid.miniada.Compiler;

public abstract class TypePrimitive extends Type {
	
	private static final Map<String, TypePrimitive> prims = new HashMap<>();
	public static final List<TypePrimitive> primitives = new ArrayList<>();

	public static final TypePrimitive INTEGER = new TypePrimitive("Integer"){
		public Value defaultValue() {
			return new ValuePrimitive(0);
		}

		@Override
		public int size() {
			return Compiler.WORD;
		}
	},
									  BOOLEAN = new TypePrimitive("Boolean"){
		public Value defaultValue() {
			return new ValuePrimitive(false);
		}

		@Override
		public int size() {
			return Compiler.WORD;
		}
	},
									CHARACTER = new TypePrimitive("Character") {
		public Value defaultValue() {
			return new ValuePrimitive(' ');
		}

		@Override
		public int size() {
			return Compiler.WORD;
		}
	};
	
	public static final TypePrimitive NULL = new TypePrimitive("@typenull") {
		public boolean canBeCastedInto(Type t) {
			if(t == this) return true;
			if(t instanceof TypeAccess) return true;
			if(t instanceof TypeDefined &&
					((TypeDefined) t).getDefinition() instanceof TypeAccess)
				return true;
			return false;
		}
		
		public Value defaultValue() {
			return new ValueAccess(null);
		}

		@Override
		public int size() {
			return Compiler.WORD;
		};
	};
	
	private TypePrimitive(String name) {
		super(name);
		prims.put(name.toLowerCase(), this);
		primitives.add(this);
	}
	
	public static TypePrimitive getPrimitive(String name) {
		return prims.get(name.toLowerCase());
	}
}
