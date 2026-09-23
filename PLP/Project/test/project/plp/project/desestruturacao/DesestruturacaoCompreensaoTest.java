package project.plp.project.desestruturacao;

import static org.junit.jupiter.api.Assertions.*;
import static project.plp.functional3.ExecutorLF3.*;

import org.junit.jupiter.api.Test;

import project.plp.expressions2.expression.Expressao;
import project.plp.expressions2.expression.Id;
import project.plp.expressions2.memory.ContextoCompilacao;
import project.plp.expressions2.memory.ContextoExecucao;
import project.plp.expressions2.memory.VariavelNaoDeclaradaException;

public class DesestruturacaoCompreensaoTest {

	@Test
	void tuplasPreservamOrdem() throws Exception {
		assertEquals("[3, 7]", resultado("[x + y for (x, y) in [(1, 2), (3, 4)]]"));
	}

	@Test
	void geradoresComFiltro() throws Exception {
		// O segundo gerador usa a lista ys ligada pelo primeiro.
		assertEquals("[12, 23]", resultado("[x + y for (x, ys) in [(10, [1, 2]), (20, [3])] for y in ys if y > 1]"));
	}

	@Test
	void padraoComWildcard() throws Exception {
		assertEquals("[4, 10]", resultado("[x + z for (x, (_, z)) in [(1, (2, 3)), (4, (5, 6))]]"));
	}

	@Test
	void reducaoComVariaveisDoUltimoGerador() throws Exception {
		// O x do padrao deve prevalecer sobre o x externo, que vale 100.
		assertEquals("[3, 7]", resultado("let var x = 100 in let fun f xs = [x + y for (x, y) in xs] in f([(1, 2), (3, 4)])"));
	}

	@Test
	void estruturaIncompativelFalhaNaChecagem() throws Exception {
		assertFalse(tipaCorretamente("[x for (x, y) in [1, 2]]"));
		assertFalse(tipaCorretamente("[x for (x, y) in [(1, 2, 3)]]"));
	}
}
