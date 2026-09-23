package project.plp.functional3.desestruturacao;

import java.util.Collections;
import java.util.List;

import project.plp.expressions1.util.Tipo;
import project.plp.expressions2.expression.Id;
import project.plp.expressions2.expression.Valor;
import project.plp.expressions2.memory.AmbienteCompilacao;
import project.plp.expressions2.memory.AmbienteExecucao;
import project.plp.expressions2.memory.VariavelJaDeclaradaException;
import project.plp.functional1.util.TipoPolimorfico;

/** Um padrao com um unico identificador, o caso sem desestruturacao alguma. */
public class PadraoId implements Padrao {

	private Id id;

	public PadraoId(Id id) {
		this.id = id;
	}

	public Id getId() {
		return id;
	}

	public void bind(Valor valor, AmbienteExecucao ambiente) throws VariavelJaDeclaradaException {
		ambiente.map(id, valor);
	}

	public void bindTipo(Tipo tipo, AmbienteCompilacao ambiente) throws VariavelJaDeclaradaException {
		ambiente.map(id, tipo);
	}

	public Tipo novoTipoEsperado(AmbienteCompilacao ambiente) throws VariavelJaDeclaradaException {
		Tipo tipo = new TipoPolimorfico();
		ambiente.map(id, tipo);
		return tipo;
	}

	public List<Id> getIdsLigados() {
		return Collections.singletonList(id);
	}

	public PadraoId clone() {
		return new PadraoId(id.clone());
	}

	@Override
	public String toString() {
		return id.toString();
	}
}
