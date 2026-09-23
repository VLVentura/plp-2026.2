package project.plp.functional3;

import static project.plp.functional3.ExecutorLF3.resultado;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Regressao da LF3 original (sem tuplas), para garantir que a extensao nao
 * quebra o que ja funcionava.
 */
public class LinguagemBaseTest {

	@Test
	void mapAplicaFuncaoATodosOsElementos() throws Exception {
		String programa = "let fun soma a b = a + b, fun map op xxs = "
				+ "if (xxs==[]) then [] else (let var x = head xxs, var xs = tail xxs in op(x,x) : map(op, xs)) in "
				+ "map(soma,[3,2,5,3,4,5])";
		assertEquals("[6, 4, 10, 6, 8, 10]", resultado(programa));
	}

	@Test
	void filterMantemElementosPositivos() throws Exception {
		String programa = "let fun filter p xxs = if xxs == [] then [] "
				+ "else let var x = head xxs, var xs = tail xxs in (if p(x) then x : filter(p, xs) else filter(p,xs)) in "
				+ "let fun positivo x = x > 0 in filter(positivo, [1,-1, 0,-3,2,3,4])";
		assertEquals("[1, 2, 3, 4]", resultado(programa));
	}
}
