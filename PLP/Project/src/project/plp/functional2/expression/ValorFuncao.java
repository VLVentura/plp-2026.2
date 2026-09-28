package project.plp.functional2.expression;

import static project.plp.expressions1.util.ToStringProvider.listToString;

import java.util.ArrayList;
import java.util.List;

import project.plp.expressions2.expression.Expressao;
import project.plp.expressions2.expression.Id;
import project.plp.expressions2.expression.Valor;
import project.plp.expressions2.memory.AmbienteExecucao;
import project.plp.functional1.util.DefFuncao;
import project.plp.project.desestruturacao.Padrao;

/**
 * @author S�rgio
 */
public class ValorFuncao extends DefFuncao implements ValorAbstrato {

	private Id id;

	public ValorFuncao(List<Padrao> parametros, Expressao exp) {
		super(parametros, exp);
	}

	public Valor avaliar(AmbienteExecucao ambiente) {
		this.reduzir(ambiente);
		return this;
	}

	@Override
	public String toString() {

		return String.format("fn %s . %s", listToString(getParametros(), " "),
				getExp());
	}

	public Id getId() {
		return this.id;
	}

	public void setId (Id id){
		this.id = id;
	}

	public Expressao reduzir(AmbienteExecucao ambiente) {
		ambiente.incrementa();

		if(this.id != null){
			ambiente.map(this.id, new ValorIrredutivel());
		}

		for (Padrao parametro : this.parametros) {
			for (Id id : parametro.getIdsLigados()) {
				ambiente.map(id, new ValorIrredutivel());
			}
		}
 
		this.exp = exp.reduzir(ambiente);
		ambiente.restaura();

		return this;
	}

	public ValorFuncao clone() {
		ValorFuncao retorno;
		List<Padrao> novaLista = new ArrayList<Padrao>(this.parametros.size());

		for (Padrao parametro : this.parametros) {
			novaLista.add(parametro.clone());
		}

		retorno = new ValorFuncao(novaLista, this.exp.clone());

		if (this.id != null)
			retorno.setId(this.id.clone());

		return retorno;
	}
}
