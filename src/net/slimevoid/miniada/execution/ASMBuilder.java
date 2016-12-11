package net.slimevoid.miniada.execution;

import java.util.Stack;

public class ASMBuilder {
	
	private static final String[] labels = new String[]{"blue", "red", "green",
			"white", "black", "turtle", "clock", "fire", "water", "storm"};
	
	private static final String[] dataNames = new String[]{"train", "car", 
			"plane", "dice", "mushroom", "tree", "flower"};
	
	public static enum Registers implements ASMOperand {
		RAX, RBX, RCX, RDX, RBP, RSP, RSI, RDI,
		R8, R9, R10, R11, R12, R13, R14, R15;

		@Override
		public void appendToBuilder(StringBuilder buff) {
			buff.append('%').append(this.name().toLowerCase());
		}
	}
	
	private int labelId = 0;
	private int dataId = 0;
	
	private StringBuilder txt;
	private StringBuilder data;
	
	private Stack<ASMRoutine> toBuild = new Stack<>();
	
	public ASMBuilder() {
		txt = new StringBuilder();
		data = new StringBuilder();
	}
	
	public void label(String label) {
		txt.append(label).append(":\n");
	}
	
	public void call(String label) {
		txt.append("\tcall ").append(label).append('\n');
	}
	
	public void jmp(String label) {
		txt.append("\tjmp ").append(label).append('\n');
	}
	

	public void main(String label) {
		txt.append("\t.globl ").append(label).append('\n');
	}
	
	private void binaryInstr(String name, ASMOperand from, ASMOperand to) {
		txt.append('\t').append(name).append(' ');
		from.appendToBuilder(txt);
		txt.append(", ");
		to.appendToBuilder(txt);
		txt.append('\n');
	}
	
	private void unaryInstr(String name, ASMOperand op) {
		txt.append('\t').append(name).append(' ');
		op.appendToBuilder(txt);
		txt.append('\n');
	}
	
	private void arglessInstr(String name) {
		txt.append('\t').append(name).append('\n');
	}
	
	public void mov(ASMOperand from, ASMOperand to) {
		binaryInstr("mov", from, to);
	}
	
	public void push(ASMOperand op) {
		unaryInstr("push", op);
	}
	
	public void pop(ASMOperand op) {
		unaryInstr("pop", op);
	}
	
	public void ret() {
		arglessInstr("ret");
	}
	
	public void registerString(String dataName, String str) {
		data.append(dataName).append(":\n\t.string \"").append(str)
			.append("\"\n'");
	}
	
	public String newLabel() {
		int id = labelId++;
		int n = labels.length;
		if(id >= n) return labels[id % n] + '_' + (id / n);
		else		return labels[id % n];
	}
	
	public String newDataName() {
		int id = dataId++;
		int n = dataNames.length;
		if(id >= n) return dataNames[id % n] + '_' + (id / n);
		else		return dataNames[id % n];
	}
	
	public String builtAsm() {
		return "\t.text\n"+txt.toString()+"\n\t.data\n"+data.toString();
	}
	
	public void planBuild(ASMRoutine rout) {
		if(!rout.isPlanned()) {
			toBuild.push(rout);
			rout.setPlanned();
		}
	}
	
	public void build() {
		while(!toBuild.isEmpty()) {
			toBuild.pop().buildASM(this);
		}
	}
}