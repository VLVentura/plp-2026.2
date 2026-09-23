package project.plp.project.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import project.plp.expressions1.util.Tipo;
import project.plp.expressions1.util.TipoPrimitivo;
import project.plp.functional1.util.TipoPolimorfico;
import project.plp.functional3.util.TipoLista;

public class TipoTuplaTest {

	private TipoTupla tuplaIntString() {
		return new TipoTupla(Arrays.<Tipo>asList(TipoPrimitivo.INTEIRO, TipoPrimitivo.STRING));
	}

	@Test
	void getNomeMostraCadaComponente() {
		assertEquals("(INTEIRO,STRING)", tuplaIntString().getNome().replace(" ", ""));
	}

	@Test
	void duasTuplasIguaisSaoIguais() {
		assertTrue(tuplaIntString().eIgual(tuplaIntString()));
	}

	@Test
	void tuplasDeAridadesDiferentesNaoSaoIguais() {
		TipoTupla tripla = new TipoTupla(
				Arrays.<Tipo>asList(TipoPrimitivo.INTEIRO, TipoPrimitivo.STRING, TipoPrimitivo.BOOLEANO));
		assertFalse(tuplaIntString().eIgual(tripla));
	}

	@Test
	void tuplasComComponenteDiferenteNaoSaoIguais() {
		TipoTupla outra = new TipoTupla(Arrays.<Tipo>asList(TipoPrimitivo.INTEIRO, TipoPrimitivo.BOOLEANO));
		assertFalse(tuplaIntString().eIgual(outra));
	}

	@Test
	void tuplaNuncaEIgualAUmTipoPrimitivo() {
		assertFalse(tuplaIntString().eIgual(TipoPrimitivo.INTEIRO));
		assertFalse(TipoPrimitivo.INTEIRO.eIgual(tuplaIntString()));
	}

	@Test
	void tuplaNuncaEIgualAUmaLista() {
		TipoLista lista = new TipoLista(TipoPrimitivo.INTEIRO);
		assertFalse(tuplaIntString().eIgual(lista));
		assertFalse(lista.eIgual(tuplaIntString()));
	}

	@Test
	void tuplaComVariavelDeTipoUnificaComTuplaConcreta() {
		TipoPolimorfico variavel = new TipoPolimorfico();
		TipoTupla comVariavel = new TipoTupla(Arrays.<Tipo>asList(variavel, TipoPrimitivo.INTEIRO));
		TipoTupla concreta = new TipoTupla(Arrays.<Tipo>asList(TipoPrimitivo.STRING, TipoPrimitivo.INTEIRO));

		assertTrue(comVariavel.eIgual(concreta));
		assertTrue(variavel.getTipoInstanciado().eIgual(TipoPrimitivo.STRING));
	}

	@Test
	void variavelDeTipoUnificaComTuplaInteira() {
		TipoPolimorfico variavel = new TipoPolimorfico();
		assertTrue(variavel.eIgual(tuplaIntString()));
		assertTrue(variavel.getTipoInstanciado().eIgual(tuplaIntString()));
	}

	@Test
	void tuplaAninhadaComparaRecursivamente() {
		TipoTupla interna = new TipoTupla(Arrays.<Tipo>asList(TipoPrimitivo.INTEIRO, TipoPrimitivo.BOOLEANO));
		TipoTupla externa = new TipoTupla(Arrays.<Tipo>asList(TipoPrimitivo.STRING, interna));

		TipoTupla internaIgual = new TipoTupla(Arrays.<Tipo>asList(TipoPrimitivo.INTEIRO, TipoPrimitivo.BOOLEANO));
		TipoTupla externaIgual = new TipoTupla(Arrays.<Tipo>asList(TipoPrimitivo.STRING, internaIgual));

		assertTrue(externa.eIgual(externaIgual));
	}

	@Test
	void intersecaoDeTuplasIguaisDevolveUmaTupla() {
		Tipo resultado = tuplaIntString().intersecao(tuplaIntString());
		assertTrue(resultado.eIgual(tuplaIntString()));
	}

	@Test
	void intersecaoDeAridadesDiferentesEhNula() {
		TipoTupla tripla = new TipoTupla(
				Arrays.<Tipo>asList(TipoPrimitivo.INTEIRO, TipoPrimitivo.STRING, TipoPrimitivo.BOOLEANO));
		assertNull(tuplaIntString().intersecao(tripla));
	}

	@Test
	void intersecaoComComponenteIncompativelEhNula() {
		TipoTupla outra = new TipoTupla(Arrays.<Tipo>asList(TipoPrimitivo.BOOLEANO, TipoPrimitivo.STRING));
		assertNull(tuplaIntString().intersecao(outra));
	}

	@Test
	void intersecaoComListaEhNulaSemEstourarPilha() {
		TipoLista lista = new TipoLista(TipoPrimitivo.INTEIRO);
		assertNull(tuplaIntString().intersecao(lista));
		assertNull(lista.intersecao(tuplaIntString()));
	}

	@Test
	void intersecaoComVariavelDeTipoDevolveATupla() {
		Tipo resultado = tuplaIntString().intersecao(new TipoPolimorfico());
		assertTrue(resultado.eIgual(tuplaIntString()));
	}

	@Test
	void tuplaComVariavelNaoResolvidaNaoEhValida() {
		List<Tipo> componentes = Arrays.<Tipo>asList(new TipoPolimorfico(), TipoPrimitivo.INTEIRO);
		assertFalse(new TipoTupla(componentes).eValido());
	}
}
