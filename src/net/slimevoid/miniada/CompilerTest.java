package net.slimevoid.miniada;

import java.io.File;
import java.io.IOException;

public class CompilerTest {
	
	public static void main(String[] args) throws IOException, InterruptedException {
		Compiler comp = new Compiler();
		comp.compile(new File("input.adb"), Compiler.PASS_EXE, 2);
//		comp.test("C:\\Users\\Marc\\Documents\\GitHub\\Maison-close\\"); 
	}
}
