package project.plp.functional3.desestruturacao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import project.plp.expressions1.util.Tipo;
import project.plp.expressions1.util.TipoPrimitivo;
import project.plp.expressions2.expression.Id;
import project.plp.expressions2.memory.ContextoCompilacao;
import project.plp.functional3.util.TipoTupla;

public class PadraoTest {

	@Test
	void padraoIdLigaUmUnicoIdentificador() {
		Id x = new Id("x");
		Padrao padrao = new PadraoId(x);
		assertEquals(Arrays.asList(x), padrao.getIdsLigados());
	}

	@Test
	void padraoWildcardNaoLigaNenhumIdentificador() {
		assertTrue(new PadraoWildcard().getIdsLigados().isEmpty());
	}

	@Test
	void padraoTuplaLigaIdentificadoresNaOrdem() {
		Id x = new Id("x");
		Id y = new Id("y");
		Id z = new Id("z");
		Padrao padrao = new PadraoTupla(Arrays.<Padrao>asList(new PadraoId(x),
				new PadraoTupla(Arrays.<Padrao>asList(new PadraoWildcard(), new PadraoId(y))), new PadraoId(z)));

		assertEquals(Arrays.asList(x, y, z), padrao.getIdsLigados());
	}

	@Test
	void novoTipoEsperadoDePadraoIdMapeiaUmaVariavelDeTipoFresca() {
		Id x = new Id("x");
		Padrao padrao = new PadraoId(x);
		ContextoCompilacao ambiente = new ContextoCompilacao();
		ambiente.incrementa();

		Tipo tipo = padrao.novoTipoEsperado(ambiente);

		assertSame(tipo, ambiente.get(x));
	}

	@Test
	void novoTipoEsperadoDePadraoTuplaConstroiArvoreComAsMesmasVariaveis() {
		Id x = new Id("x");
		Id y = new Id("y");
		Padrao padrao = new PadraoTupla(Arrays.<Padrao>asList(new PadraoId(x), new PadraoId(y)));
		ContextoCompilacao ambiente = new ContextoCompilacao();
		ambiente.incrementa();

		Tipo tipo = padrao.novoTipoEsperado(ambiente);

		List<Tipo> componentes = ((TipoTupla) tipo).getComponentes();
		assertSame(componentes.get(0), ambiente.get(x));
		assertSame(componentes.get(1), ambiente.get(y));
	}

	@Test
	void bindTipoDePadraoTuplaDesceRecursivamentePeloTipoConhecido() {
		Id x = new Id("x");
		Id y = new Id("y");
		Padrao padrao = new PadraoTupla(Arrays.<Padrao>asList(new PadraoId(x),
				new PadraoTupla(Arrays.<Padrao>asList(new PadraoWildcard(), new PadraoId(y)))));

		TipoTupla tipoInterno = new TipoTupla(Arrays.<Tipo>asList(TipoPrimitivo.BOOLEANO, TipoPrimitivo.INTEIRO));
		TipoTupla tipo = new TipoTupla(Arrays.<Tipo>asList(TipoPrimitivo.STRING, tipoInterno));

		ContextoCompilacao ambiente = new ContextoCompilacao();
		ambiente.incrementa();
		padrao.bindTipo(tipo, ambiente);

		assertTrue(ambiente.get(x).eIgual(TipoPrimitivo.STRING));
		assertTrue(ambiente.get(y).eIgual(TipoPrimitivo.INTEIRO));
	}
}
