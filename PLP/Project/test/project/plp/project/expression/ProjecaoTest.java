package project.plp.project.expression;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static project.plp.functional3.ExecutorLF3.resultado;
import static project.plp.functional3.ExecutorLF3.tipaCorretamente;
import static project.plp.functional3.ExecutorLF3.tipo;

import org.junit.jupiter.api.Test;

public class ProjecaoTest {

	@Test
	void primeiraPosicaoEPontoUm() throws Exception {
		assertEquals("10", resultado("(10, 20).1"));
		assertEquals("20", resultado("(10, 20).2"));
	}

	@Test
	void tipoDaPosicaoProjetada() throws Exception {
		assertEquals("STRING", tipo("(\"Ana\", 20).1").getNome());
		assertEquals("INTEIRO", tipo("(\"Ana\", 20).2").getNome());
	}

	@Test
	void projecaoEncadeada() throws Exception {
		assertEquals("2", resultado("(1, (2, 3)).2.1"));
	}

	@Test
	void projecaoLigaMaisForteQueOperadores() throws Exception {
		assertEquals("4", resultado("1 + (2, 3).2"));
		assertEquals("false", resultado("not (true, 1).1"));
	}

	@Test
	void projecaoDeVariavel() throws Exception {
		assertEquals("21", resultado("let var p = (\"Ana\", 20) in p.2 + 1"));
	}

	@Test
	void projecaoDoRetornoDeFuncaoRecursiva() throws Exception {
		String divide = "let fun divide a b = "
				+ "if a < b then (0, a) "
				+ "else let var (q, r) = divide(a - b, b) in (q + 1, r) "
				+ "in (divide(17, 5).1, divide(17, 5).2)";
		assertEquals("(3, 2)", resultado(divide));
	}

	@Test
	void projecaoDeFuncaoPolimorfica() throws Exception {
		String programa = "let fun troca (a, b) = (b, a) in (troca((1, true)).1, troca((\"x\", 2)).1)";
		assertEquals("(true, 2)", resultado(programa));
		assertEquals("(BOOLEANO,INTEIRO)", tipo(programa).getNome().replace(" ", ""));
	}

	@Test
	void projecaoDeParametroJaDesestruturado() throws Exception {
		assertEquals("2", resultado("let fun f p = let var (a, b) = p in p.2 in f((1, 2))"));
	}

	@Test
	void indiceForaDaTupla() throws Exception {
		ProjecaoException erro = assertThrows(ProjecaoException.class, () -> tipaCorretamente("(1, 2).3"));
		assertEquals(ProjecaoException.Motivo.INDICE, erro.getMotivo());
	}

	@Test
	void indiceZeroNaoExiste() throws Exception {
		ProjecaoException erro = assertThrows(ProjecaoException.class, () -> tipaCorretamente("(1, 2).0"));
		assertEquals(ProjecaoException.Motivo.INDICE, erro.getMotivo());
	}

	@Test
	void projecaoDeInteiroEErroEstrutural() throws Exception {
		ProjecaoException erro = assertThrows(ProjecaoException.class, () -> tipaCorretamente("5.1"));
		assertEquals(ProjecaoException.Motivo.ESTRUTURA, erro.getMotivo());
	}

	@Test
	void projecaoDeListaEErroEstrutural() throws Exception {
		ProjecaoException erro = assertThrows(ProjecaoException.class, () -> tipaCorretamente("[1, 2].1"));
		assertEquals(ProjecaoException.Motivo.ESTRUTURA, erro.getMotivo());
	}

	@Test
	void projecaoDeTipoLivreERejeitada() throws Exception {
		ProjecaoException erro = assertThrows(ProjecaoException.class,
				() -> tipaCorretamente("let fun f p = p.1 in f((1, 2))"));
		assertEquals(ProjecaoException.Motivo.TIPO_LIVRE, erro.getMotivo());
	}

	@Test
	void projecaoNaoECapturadaNaReducao() throws Exception {
		String programa = "let var x = (10, 20) in "
				+ "let fun g p = let var (x, y) = p in (x, y).1 in "
				+ "g((1, 2))";
		assertEquals("1", resultado(programa));
	}
}
