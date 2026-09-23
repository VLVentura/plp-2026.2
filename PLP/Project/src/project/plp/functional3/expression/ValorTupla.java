package project.plp.functional3.expression;

import static project.plp.expressions1.util.ToStringProvider.listToString;

import java.util.ArrayList;
import java.util.List;

import project.plp.expressions1.util.Tipo;
import project.plp.expressions2.expression.Valor;
import project.plp.expressions2.expression.ValorConcreto;
import project.plp.expressions2.memory.AmbienteCompilacao;
import project.plp.expressions2.memory.VariavelJaDeclaradaException;
import project.plp.expressions2.memory.VariavelNaoDeclaradaException;
import project.plp.functional3.util.TipoTupla;

/**
 * Valor de uma tupla ja avaliada, imutavel, com pelo menos duas posicoes.
 * Heterogenea: cada posicao pode ter um tipo diferente.
 */
public class ValorTupla extends ValorConcreto<List<Valor>> {

	public ValorTupla(List<Valor> valores) {
		super(valores);
	}

	@Override
	public Tipo getTipo(AmbienteCompilacao amb) throws VariavelNaoDeclaradaException, VariavelJaDeclaradaException {
		List<Tipo> tipos = new ArrayList<Tipo>(valor().size());
		for (Valor componente : valor()) {
			tipos.add(componente.getTipo(amb));
		}
		return new TipoTupla(tipos);
	}

	@Override
	public String toString() {
		return "(" + listToString(valor(), ",") + ")";
	}

	@Override
	public ValorTupla clone() {
		List<Valor> clones = new ArrayList<Valor>(valor().size());
		for (Valor componente : valor()) {
			clones.add((Valor) componente.clone());
		}
		return new ValorTupla(clones);
	}
}
