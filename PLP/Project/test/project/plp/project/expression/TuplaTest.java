package project.plp.project.expression;

import static project.plp.functional3.ExecutorLF3.parse;
import static project.plp.functional3.ExecutorLF3.resultado;
import static project.plp.functional3.ExecutorLF3.tipaCorretamente;
import static project.plp.functional3.ExecutorLF3.tipo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class TuplaTest {

	@Test
	void tuplaSimples() throws Exception {
		assertEquals("(1, 2)", resultado("(1, 2)"));
	}

	@Test
	void tuplaAninhada() throws Exception {
		assertEquals("(1, (2, 3))", resultado("(1, (2, 3))"));
	}

	@Test
	void tuplaHeterogenea() throws Exception {
		assertEquals("(1, \"texto\", true)", resultado("(1, \"texto\", true)"));
	}

	@Test
	void tuplaComListaDentro() throws Exception {
		assertEquals("(1, [1, 2, 3])", resultado("(1, [1, 2, 3])"));
	}

	@Test
	void tuplaComFuncaoDentroTipaCorretamente() throws Exception {
		assertTrue(tipaCorretamente("let fun dobro a = a + a in (1, dobro)"));
	}

	@Test
	void parentesesComUmaExpressaoContinuamSendoAgrupamento() throws Exception {
		assertEquals("6", resultado("let var x = (5) in x + 1"));
	}

	@Test
	void parentesesVaziosNaoSaoValidos() {
		assertThrows(Exception.class, () -> parse("()"));
	}

	@Test
	void aplicacaoComDoisArgumentosContinuaFuncionando() throws Exception {
		assertEquals("3", resultado("let fun soma a b = a + b in soma(1, 2)"));
	}

	@Test
	void aplicacaoComUmaTuplaNaoEhOMesmoQueDoisArgumentos() throws Exception {
		// soma espera dois parametros; soma((1, 2)) passa um unico argumento,
		// que e uma tupla, entao a aridade nao bate.
		assertFalse(tipaCorretamente("let fun soma a b = a + b in soma((1, 2))"));
	}

	@Test
	void listaDeTuplasHomogeneaTipaCorretamente() throws Exception {
		assertTrue(tipaCorretamente("[(1, 2), (3, 4)]"));
		assertEquals("[(INTEIRO, INTEIRO)]", tipo("[(1, 2), (3, 4)]").getNome());
	}

	@Test
	void listaDeTuplasHeterogeneaNaoTipaCorretamente() throws Exception {
		assertFalse(tipaCorretamente("[(1, 2), (3, \"a\")]"));
	}

	@Test
	void ifComTuplaNosDoisRamosTipaCorretamente() throws Exception {
		assertEquals("(1, 2)", resultado("if true then (1, 2) else (3, 4)"));
	}

	@Test
	void ifComTuplasDeTiposDiferentesNosRamosNaoTipa() throws Exception {
		assertFalse(tipaCorretamente("if true then (1, 2) else (3, \"a\")"));
	}
}
