package project.plp.functional3.desestruturacao;

import static project.plp.functional3.ExecutorLF3.resultado;
import static project.plp.functional3.ExecutorLF3.tipo;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Desestruturacao de tuplas em "let var Padrao = Expressao", o caminho feliz:
 * a estrutura do padrao sempre bate com a do valor.
 */
public class DesestruturacaoLetTest {

	@Test
	void exemploCentralDoEscopo() throws Exception {
		String programa = "let var pessoa = (\"Pessoa\", (25, true)) in "
				+ "let var (nome, (idade, _)) = pessoa in (nome, idade)";
		assertEquals("(\"Pessoa\", 25)", resultado(programa));
		assertEquals("(STRING, INTEIRO)", tipo(programa).getNome());
	}

	@Test
	void padraoSimples() throws Exception {
		assertEquals("3", resultado("let var (x, y) = (1, 2) in x + y"));
	}

	@Test
	void padraoHeterogeneo() throws Exception {
		assertEquals("20", resultado("let var (nome, idade) = (\"Ana\", 20) in idade"));
	}

	@Test
	void padraoComAridadeMaiorQueDois() throws Exception {
		String programa = "let var (a, b, c, d) = (1, \"b\", true, [4]) in (a, b, c, d)";
		assertEquals("(1, \"b\", true, [4])", resultado(programa));
	}

	@Test
	void padraoAninhado() throws Exception {
		assertEquals("6", resultado("let var (x, (y, z)) = (1, (2, 3)) in x + y + z"));
	}

	@Test
	void padraoComWildcard() throws Exception {
		assertEquals("1", resultado("let var (x, _) = (1, 2) in x"));
	}

	@Test
	void padraoComDoisWildcards() throws Exception {
		assertEquals("true", resultado("let var (_, _) = (1, 2) in true"));
	}
}
