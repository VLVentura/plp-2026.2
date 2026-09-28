package project.plp.project.desestruturacao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static project.plp.functional3.ExecutorLF3.resultado;
import static project.plp.functional3.ExecutorLF3.tipaCorretamente;
import static project.plp.functional3.ExecutorLF3.tipo;

import org.junit.jupiter.api.Test;

public class DesestruturacaoPolimorfismoTest {

	private static final String TROCA = "let fun troca (a, b) = (b, a) in "
			+ "(troca((1, true)), troca((\"x\", 2)))";

	@Test
	void funcaoComTuplaAplicadaComTiposDiferentes() throws Exception {
		assertEquals("((true, 1), (2, \"x\"))", resultado(TROCA));
	}

	@Test
	void tipoDaAplicacaoComTiposDiferentesNaoSeContamina() throws Exception {
		assertEquals("((BOOLEANO,INTEIRO),(INTEIRO,STRING))", tipo(TROCA).getNome().replace(" ", ""));
	}

	private static final String TROCA_NO_CORPO = "let fun troca p = let var (a, b) = p in (b, a) in "
			+ "(troca((1, true)), troca((\"x\", 2)))";

	@Test
	void desestruturacaoNoCorpoAplicadaComTiposDiferentes() throws Exception {
		assertEquals("((true, 1), (2, \"x\"))", resultado(TROCA_NO_CORPO));
		assertEquals("((BOOLEANO,INTEIRO),(INTEIRO,STRING))", tipo(TROCA_NO_CORPO).getNome().replace(" ", ""));
	}

	@Test
	void desestruturacaoNoCorpoNaoHerdaTipoDaPrimeiraChamada() throws Exception {
		String fst = "let fun fst p = let var (a, b) = p in a in ";
		assertFalse(tipaCorretamente(fst + "fst((1, true)) + fst((\"s\", 2))"));
		assertTrue(tipaCorretamente(fst + "(fst((1, true)), not fst((false, 2)))"));
		assertFalse(tipaCorretamente(fst + "fst(3)"));
	}

	@Test
	void tuplaAninhadaAplicadaDuasVezesComTiposDiferentes() throws Exception {
		assertEquals("(3, (2, 1))", resultado("let fun f (a, (b, c)) = (c, (b, a)) in f((1, (2, 3)))"));
		assertEquals("(4, (\"y\", true))", resultado("let fun f (a, (b, c)) = (c, (b, a)) in f((true, (\"y\", 4)))"));
	}
}
