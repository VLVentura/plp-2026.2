package project.plp.functional3.desestruturacao;

import static project.plp.expressions1.util.ToStringProvider.listToString;

import java.util.ArrayList;
import java.util.List;

import project.plp.expressions1.util.Tipo;
import project.plp.expressions2.expression.Id;
import project.plp.expressions2.expression.Valor;
import project.plp.expressions2.memory.AmbienteCompilacao;
import project.plp.expressions2.memory.AmbienteExecucao;
import project.plp.expressions2.memory.VariavelJaDeclaradaException;
import project.plp.functional1.util.TipoPolimorfico;
import project.plp.functional3.expression.ValorTupla;
import project.plp.functional3.util.TipoTupla;

/**
 * Um padrao de tupla, com pelo menos duas posicoes, cada uma outro padrao.
 */
public class PadraoTupla implements Padrao {

	private List<Padrao> subpadroes;

	public PadraoTupla(List<Padrao> subpadroes) {
		this.subpadroes = subpadroes;
	}

	public List<Padrao> getSubpadroes() {
		return subpadroes;
	}

	public void bind(Valor valor, AmbienteExecucao ambiente) throws VariavelJaDeclaradaException {
		if (!(valor instanceof ValorTupla)) {
			throw DesestruturacaoException.estrutura(this, valor);
		}

		List<Valor> componentes = ((ValorTupla) valor).valor();
		if (componentes.size() != subpadroes.size()) {
			throw DesestruturacaoException.aridade(this, valor, subpadroes.size(), componentes.size());
		}

		for (int i = 0; i < subpadroes.size(); i++) {
			subpadroes.get(i).bind(componentes.get(i), ambiente);
		}
	}

	public void bindTipo(Tipo tipo, AmbienteCompilacao ambiente) throws VariavelJaDeclaradaException {
		tipo = tipoConcreto(tipo);
		if (!(tipo instanceof TipoTupla)) {
			throw DesestruturacaoException.estrutura(this, tipo);
		}

		List<Tipo> componentes = ((TipoTupla) tipo).getComponentes();
		if (componentes.size() != subpadroes.size()) {
			throw DesestruturacaoException.aridade(this, tipo, subpadroes.size(), componentes.size());
		}

		for (int i = 0; i < subpadroes.size(); i++) {
			subpadroes.get(i).bindTipo(componentes.get(i), ambiente);
		}
	}

	/**
	 * Segue um TipoPolimorfico ja resolvido ate o tipo concreto que ele
	 * representa. Se ainda estiver livre (por exemplo o tipo de um parametro
	 * de funcao, antes de qualquer uso), cria uma TipoTupla do tamanho deste
	 * padrao e unifica com ela: eIgual grava essa tupla como a instancia do
	 * TipoPolimorfico, o que resolve o tipo tanto aqui quanto em qualquer
	 * outro lugar que compartilhe a mesma variavel de tipo.
	 */
	private Tipo tipoConcreto(Tipo tipo) {
		while (tipo instanceof TipoPolimorfico && ((TipoPolimorfico) tipo).getTipoInstanciado() != null) {
			tipo = ((TipoPolimorfico) tipo).getTipoInstanciado();
		}

		if (tipo instanceof TipoPolimorfico) {
			List<Tipo> componentesLivres = new ArrayList<Tipo>(subpadroes.size());
			for (int i = 0; i < subpadroes.size(); i++) {
				componentesLivres.add(new TipoPolimorfico());
			}

			Tipo tuplaLivre = new TipoTupla(componentesLivres);
			tipo.eIgual(tuplaLivre);

			return tuplaLivre;
		}

		return tipo;
	}

	public Tipo novoTipoEsperado(AmbienteCompilacao ambiente) throws VariavelJaDeclaradaException {
		List<Tipo> tipos = new ArrayList<Tipo>(subpadroes.size());
		for (Padrao subpadrao : subpadroes) {
			tipos.add(subpadrao.novoTipoEsperado(ambiente));
		}
		return new TipoTupla(tipos);
	}

	public List<Id> getIdsLigados() {
		List<Id> ids = new ArrayList<Id>();
		for (Padrao subpadrao : subpadroes) {
			ids.addAll(subpadrao.getIdsLigados());
		}
		return ids;
	}

	public PadraoTupla clone() {
		List<Padrao> clones = new ArrayList<Padrao>(subpadroes.size());
		for (Padrao subpadrao : subpadroes) {
			clones.add(subpadrao.clone());
		}
		return new PadraoTupla(clones);
	}

	@Override
	public String toString() {
		return "(" + listToString(subpadroes, ",") + ")";
	}
}
