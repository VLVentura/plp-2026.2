package project.plp.project.desestruturacao;

import java.util.Collections;
import java.util.List;

import project.plp.expressions1.util.Tipo;
import project.plp.expressions2.expression.Id;
import project.plp.expressions2.expression.Valor;
import project.plp.expressions2.memory.AmbienteCompilacao;
import project.plp.expressions2.memory.AmbienteExecucao;
import project.plp.functional1.util.TipoPolimorfico;

/** O curinga "_": a posicao e validada, mas nenhum identificador e ligado. */
public class PadraoWildcard implements Padrao {

	public void bind(Valor valor, AmbienteExecucao ambiente) {
	}

	public void bindTipo(Tipo tipo, AmbienteCompilacao ambiente) {
	}

	public Tipo novoTipoEsperado(AmbienteCompilacao ambiente) {
		return new TipoPolimorfico();
	}

	public List<Id> getIdsLigados() {
		return Collections.emptyList();
	}

	public PadraoWildcard clone() {
		return new PadraoWildcard();
	}

	@Override
	public String toString() {
		return "_";
	}
}
