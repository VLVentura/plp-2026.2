package project.plp.functional3.desestruturacao;

import static project.plp.functional3.ExecutorLF3.resultado;
import static project.plp.functional3.ExecutorLF3.tipo;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class DesestruturacaoTipoLivreTest {

	@Test
	void parametroDesestruturadoNoCorpoDaFuncao() throws Exception {
		String programa = "let fun soma p = let var (a, b) = p in a + b in soma((1, 2))";
		assertEquals("3", resultado(programa));
	}

	@Test
	void retornoMultiploComRecursao() throws Exception {
		String programa = "let fun divide a b = "
				+ "if a < b then (0, a) else let var (q, r) = divide(a - b, b) in (q + 1, r) in "
				+ "let var (q, r) = divide(17, 5) in (r, q + r)";
		assertEquals("(2, 5)", resultado(programa));
		assertEquals("(INTEIRO, INTEIRO)", tipo(programa).getNome());
	}
}
