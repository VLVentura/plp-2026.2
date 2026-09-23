package project.plp.project.desestruturacao;

import static project.plp.functional3.ExecutorLF3.parse;
import static project.plp.functional3.ExecutorLF3.resultado;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import project.plp.expressions2.memory.VariavelJaDeclaradaException;
import project.plp.project.desestruturacao.DesestruturacaoException.Motivo;
import project.plp.functional3.parser.ParseException;

public class DesestruturacaoErrosTest {

	@Test
	void aridadeMaiorQueOPadrao() throws Exception {
		DesestruturacaoException erro = assertThrows(DesestruturacaoException.class,
				() -> resultado("let var (x, y) = (1, 2, 3) in x"));
		assertEquals(Motivo.ARIDADE, erro.getMotivo());
	}

	@Test
	void aridadeEmPosicaoAninhada() throws Exception {
		DesestruturacaoException erro = assertThrows(DesestruturacaoException.class,
				() -> resultado("let var (x, (y, z)) = (1, (2, 3, 4)) in x"));
		assertEquals(Motivo.ARIDADE, erro.getMotivo());
	}

	@Test
	void estruturaQuandoOValorNaoETupla() throws Exception {
		DesestruturacaoException erro = assertThrows(DesestruturacaoException.class,
				() -> resultado("let var (x, y) = 5 in x"));
		assertEquals(Motivo.ESTRUTURA, erro.getMotivo());
	}

	@Test
	void estruturaEmPosicaoAninhada() throws Exception {
		DesestruturacaoException erro = assertThrows(DesestruturacaoException.class,
				() -> resultado("let var (x, (y, z)) = (1, 2) in x"));
		assertEquals(Motivo.ESTRUTURA, erro.getMotivo());
	}

	@Test
	void duplicidadeNoMesmoPadrao() throws Exception {
		DesestruturacaoException erro = assertThrows(DesestruturacaoException.class,
				() -> resultado("let var (x, x) = (1, 2) in x"));
		assertEquals(Motivo.DUPLICIDADE, erro.getMotivo());
	}

	@Test
	void duplicidadeEmPosicaoAninhada() throws Exception {
		DesestruturacaoException erro = assertThrows(DesestruturacaoException.class,
				() -> resultado("let var (x, (y, x)) = (1, (2, 3)) in x"));
		assertEquals(Motivo.DUPLICIDADE, erro.getMotivo());
	}

	@Test
	void wildcardNuncaCausaDuplicidade() throws Exception {
		assertEquals("true", resultado("let var (_, _) = (1, 2) in true"));
	}

	@Test
	void curingaNaSegundaPosicao() throws Exception {
		assertEquals("2", resultado("let var (_, x) = (1, 2) in x"));
	}

	@Test
	void duplicidadeEntreDeclaracoesDoMesmoLetJaEraDetectada() throws Exception {
		assertThrows(VariavelJaDeclaradaException.class,
				() -> resultado("let var (a, b) = (1, 2), var a = 3 in a"));
	}

	@Test
	void wildcardForaDePadraoEhErroDeSintaxe() {
		assertThrows(ParseException.class, () -> parse("let var x = _ in x"));
	}
}
