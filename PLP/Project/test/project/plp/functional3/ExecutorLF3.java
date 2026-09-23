package project.plp.functional3;

import java.io.StringReader;

import project.plp.expressions1.util.Tipo;
import project.plp.expressions2.expression.Valor;
import project.plp.expressions2.memory.ContextoCompilacao;
import project.plp.functional3.parser.Func3Parser;

/**
 * Helper de testes que roda um programa LF3 a partir de uma string, sem
 * passar por arquivo nem por System.in.
 */
public class ExecutorLF3 {

	private ExecutorLF3() {
	}

	public static Programa parse(String codigo) throws Exception {
		Func3Parser parser = new Func3Parser(new StringReader(codigo));
		return parser.Input();
	}

	public static boolean tipaCorretamente(String codigo) throws Exception {
		return parse(codigo).checaTipo();
	}

	public static Valor executar(String codigo) throws Exception {
		Programa programa = parse(codigo);
		if (!programa.checaTipo()) {
			throw new IllegalStateException("Programa nao passou na checagem de tipos: " + codigo);
		}
		return programa.executar();
	}

	public static String resultado(String codigo) throws Exception {
		return executar(codigo).toString();
	}

	public static Tipo tipo(String codigo) throws Exception {
		Programa programa = parse(codigo);
		programa.checaTipo();
		return programa.getExpressao().getTipo(new ContextoCompilacao());
	}
}
