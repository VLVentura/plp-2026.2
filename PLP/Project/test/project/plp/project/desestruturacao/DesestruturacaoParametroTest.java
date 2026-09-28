package project.plp.project.desestruturacao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static project.plp.functional3.ExecutorLF3.resultado;
import static project.plp.functional3.ExecutorLF3.tipaCorretamente;

import org.junit.jupiter.api.Test;

public class DesestruturacaoParametroTest {

	@Test
	void parametroSimples() throws Exception {
		assertEquals("30", resultado("let fun somaPar (x, y) = x + y in somaPar((10, 20))"));
	}

	@Test
	void parametroAninhado() throws Exception {
		assertEquals("6", resultado("let fun f (x, (y, z)) = x + y + z in f((1, (2, 3)))"));
	}

	@Test
	void funcaoAnonima() throws Exception {
		assertEquals("3", resultado("let var soma = fn (x, y) . x + y in soma((1, 2))"));
	}

	@Test
	void aridadeIncompativelEhErroEstrutural() throws Exception {
		assertFalse(tipaCorretamente("let fun f (x, y) = x + y in f(3)"));
	}

	@Test
	void numeroDeArgumentosErrado() throws Exception {
		assertFalse(tipaCorretamente("let fun f (x, y) = x + y in f(1, 2)"));
	}

	@Test
	void duplicidadeEntreParametros() throws Exception {
		DesestruturacaoException erro = assertThrows(DesestruturacaoException.class,
				() -> resultado("let fun f (x, y) x = x in f((1, 2), 3)"));
		assertEquals(DesestruturacaoException.Motivo.DUPLICIDADE, erro.getMotivo());
	}

	@Test
	void capturaComFuncaoNomeada() throws Exception {
		String programa = "let var x = 10 in "
				+ "let fun g p = let fun f (x, y) = x + y in f(p) in "
				+ "g((1, 2))";
		assertEquals("3", resultado(programa));
	}

	@Test
	void capturaComFuncaoAnonima() throws Exception {
		assertEquals("3", resultado("let var x = 10 in (fn (x, y) . x + y)((1, 2))"));
	}
}
