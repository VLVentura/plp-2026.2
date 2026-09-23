package project.plp.functional3.expression;

import static project.plp.expressions1.util.ToStringProvider.listToString;

import java.util.ArrayList;
import java.util.List;

import project.plp.expressions1.util.Tipo;
import project.plp.expressions2.expression.Expressao;
import project.plp.expressions2.expression.Valor;
import project.plp.expressions2.memory.AmbienteCompilacao;
import project.plp.expressions2.memory.AmbienteExecucao;
import project.plp.expressions2.memory.VariavelJaDeclaradaException;
import project.plp.expressions2.memory.VariavelNaoDeclaradaException;
import project.plp.functional3.util.TipoTupla;

/**
 * Uma tupla com pelo menos duas posicoes: (e1, e2, ..., en). Os parenteses
 * com uma unica expressao continuam sendo agrupamento, nao tupla.
 */
public class ExpTupla implements Expressao {

	private List<Expressao> componentes;

	public ExpTupla(List<Expressao> componentes) {
		this.componentes = componentes;
	}

	public List<Expressao> getComponentes() {
		return componentes;
	}

	public Valor avaliar(AmbienteExecucao amb) throws VariavelNaoDeclaradaException, VariavelJaDeclaradaException {
		List<Valor> valores = new ArrayList<Valor>(componentes.size());
		for (Expressao componente : componentes) {
			valores.add(componente.avaliar(amb));
		}
		return new ValorTupla(valores);
	}

	public boolean checaTipo(AmbienteCompilacao amb)
			throws VariavelNaoDeclaradaException, VariavelJaDeclaradaException {
		boolean valido = true;
		for (Expressao componente : componentes) {
			valido &= componente.checaTipo(amb);
		}
		return valido;
	}

	public Tipo getTipo(AmbienteCompilacao amb) throws VariavelNaoDeclaradaException, VariavelJaDeclaradaException {
		List<Tipo> tipos = new ArrayList<Tipo>(componentes.size());
		for (Expressao componente : componentes) {
			tipos.add(componente.getTipo(amb));
		}
		return new TipoTupla(tipos);
	}

	public Expressao reduzir(AmbienteExecucao ambiente) {
		List<Expressao> reduzidos = new ArrayList<Expressao>(componentes.size());
		for (Expressao componente : componentes) {
			reduzidos.add(componente.reduzir(ambiente));
		}
		this.componentes = reduzidos;
		return this;
	}

	public ExpTupla clone() {
		List<Expressao> clones = new ArrayList<Expressao>(componentes.size());
		for (Expressao componente : componentes) {
			clones.add(componente.clone());
		}
		return new ExpTupla(clones);
	}

	@Override
	public String toString() {
		return "(" + listToString(componentes, ",") + ")";
	}
}
