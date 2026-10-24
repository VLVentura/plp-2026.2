package project.plp.project.expression;

import java.util.List;

import project.plp.expressions1.util.Tipo;
import project.plp.expressions2.expression.Expressao;
import project.plp.expressions2.expression.Valor;
import project.plp.expressions2.memory.AmbienteCompilacao;
import project.plp.expressions2.memory.AmbienteExecucao;
import project.plp.expressions2.memory.VariavelJaDeclaradaException;
import project.plp.expressions2.memory.VariavelNaoDeclaradaException;
import project.plp.functional1.util.TipoPolimorfico;
import project.plp.project.util.TipoTupla;

/**
 * Acesso direto a uma posicao de tupla: e.k, com k literal e a primeira
 * posicao sendo .1. Como k e estatico, a checagem de tipos sabe exatamente
 * qual componente e devolvido, mas para isso precisa conhecer a aridade da
 * tupla: sobre uma variavel de tipo ainda livre a projecao e rejeitada, e o
 * caminho e desestruturar.
 */
public class ExpProjecao implements Expressao {

	private Expressao tupla;

	private int indice;

	public ExpProjecao(Expressao tupla, int indice) {
		this.tupla = tupla;
		this.indice = indice;
	}

	public Expressao getTupla() {
		return tupla;
	}

	public int getIndice() {
		return indice;
	}

	public Valor avaliar(AmbienteExecucao amb) throws VariavelNaoDeclaradaException, VariavelJaDeclaradaException {
		Valor valor = this.tupla.avaliar(amb);
		if (!(valor instanceof ValorTupla)) {
			throw ProjecaoException.estrutura(this.indice, valor);
		}

		List<Valor> componentes = ((ValorTupla) valor).valor();
		checaIndice(valor, componentes.size());
		return componentes.get(this.indice - 1);
	}

	public boolean checaTipo(AmbienteCompilacao amb)
			throws VariavelNaoDeclaradaException, VariavelJaDeclaradaException {
		if (!this.tupla.checaTipo(amb)) {
			return false;
		}
		tipoDaTupla(amb);
		return true;
	}

	public Tipo getTipo(AmbienteCompilacao amb) throws VariavelNaoDeclaradaException, VariavelJaDeclaradaException {
		return tipoDaTupla(amb).getComponentes().get(this.indice - 1);
	}

	/**
	 * Tipo da expressao projetada, ja validado como uma tupla que tem a
	 * posicao pedida.
	 */
	private TipoTupla tipoDaTupla(AmbienteCompilacao amb)
			throws VariavelNaoDeclaradaException, VariavelJaDeclaradaException {
		Tipo tipo = TipoPolimorfico.seguir(this.tupla.getTipo(amb));
		if (tipo instanceof TipoPolimorfico) {
			throw ProjecaoException.tipoLivre(this.indice, this.tupla);
		}
		if (!(tipo instanceof TipoTupla)) {
			throw ProjecaoException.estrutura(this.indice, tipo);
		}

		TipoTupla tipoTupla = (TipoTupla) tipo;
		checaIndice(tipoTupla, tipoTupla.getAridade());
		return tipoTupla;
	}

	private void checaIndice(Object tupla, int aridade) {
		if (this.indice < 1 || this.indice > aridade) {
			throw ProjecaoException.indice(this.indice, tupla, aridade);
		}
	}

	public Expressao reduzir(AmbienteExecucao ambiente) {
		this.tupla = this.tupla.reduzir(ambiente);
		return this;
	}

	public ExpProjecao clone() {
		return new ExpProjecao(this.tupla.clone(), this.indice);
	}

	@Override
	public String toString() {
		return this.tupla + "." + this.indice;
	}
}
