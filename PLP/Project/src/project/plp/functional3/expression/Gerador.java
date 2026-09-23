package project.plp.functional3.expression;

import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import project.plp.expressions1.util.Tipo;
import project.plp.expressions1.excecao.ErroTipoException;
import project.plp.expressions2.expression.Expressao;
import project.plp.expressions2.expression.Id;
import project.plp.expressions2.expression.Valor;
import project.plp.expressions2.expression.ValorBooleano;
import project.plp.expressions2.memory.AmbienteCompilacao;
import project.plp.expressions2.memory.AmbienteExecucao;
import project.plp.expressions2.memory.VariavelJaDeclaradaException;
import project.plp.expressions2.memory.VariavelNaoDeclaradaException;
import project.plp.functional1.util.TipoPolimorfico;
import project.plp.functional2.expression.ValorIrredutivel;
import project.plp.functional3.util.TipoLista;
import project.plp.project.desestruturacao.DesestruturacaoException;
import project.plp.project.desestruturacao.Padrao;

public class Gerador {

	private Padrao padrao;
	private Gerador proximo;
	private Expressao expressao;

	public Gerador(Padrao padrao, Expressao expressao) {
		this.padrao = padrao;
		this.expressao = expressao;
	}

	public Gerador getProximoGerador() {
		return this.proximo;
	}

	public void addProximoGerador(Gerador gerador) {
		if (this.proximo == null) {
			this.proximo = gerador;
		} else {
			this.proximo.addProximoGerador(gerador);
		}
	}

	private void verificaDuplicidade() throws DesestruturacaoException {
		// Um nome nao pode aparecer duas vezes no mesmo padrao.
		Set<Id> vistos = new HashSet<Id>();
		for (Id id : this.padrao.getIdsLigados()) {
			if (!vistos.add(id)) {
				throw DesestruturacaoException.duplicidade(id);
			}
		}
	}

	public void gerarValores(AmbienteExecucao amb, ValorLista resultado, Expressao expressao, Expressao filtro)
			throws VariavelNaoDeclaradaException, VariavelJaDeclaradaException {
		verificaDuplicidade();
		Valor fonte = this.expressao.avaliar(amb);

		if (!(fonte instanceof ValorLista)) {
			throw new IllegalArgumentException("gerador espera uma lista, mas recebeu " + fonte);
		}

		ValorLista temp = (ValorLista) fonte;

		// Enquanto houver elementos na lista
		while (temp != null && !temp.isEmpty()) {
			Valor valor = temp.getHead().avaliar(amb);
			amb.incrementa();

			try {
				this.padrao.bind(valor, amb);
				if (this.proximo != null) {
					// Percorre o proximo gerador inteiro para este elemento.
					this.proximo.gerarValores(amb, resultado, expressao, filtro);
				} else if (filtro == null || ((ValorBooleano) filtro.avaliar(amb)).valor()) {
					resultado.cons(expressao.avaliar(amb));
				}
			} finally {
				amb.restaura();
			}
			temp = temp.getTail();
		}
	}

	public boolean temProximoGerador() {
		return this.proximo != null;
	}

	private TipoLista tipoLista(AmbienteCompilacao amb)
			throws VariavelNaoDeclaradaException, VariavelJaDeclaradaException {
		Tipo tipo = this.expressao.getTipo(amb);
		while (tipo instanceof TipoPolimorfico) {
			Tipo instancia = ((TipoPolimorfico) tipo).getTipoInstanciado();
			if (instancia == null) {
				// Exige uma lista, deixando o tipo dos elementos em aberto.
				TipoLista lista = new TipoLista();
				return tipo.eIgual(lista) ? lista : null;
			}
			tipo = instancia;
		}
		return tipo instanceof TipoLista ? (TipoLista) tipo : null;
	}

	public Map<Id, Tipo> checkTypeBindings(AmbienteCompilacao amb) {
		Map<Id, Tipo> tipos = new HashMap<Id, Tipo>();
		try {
			collectTypeBindings(amb, tipos);
		} catch (VariavelNaoDeclaradaException | VariavelJaDeclaradaException e) {
			throw new ErroTipoException();
		}
		return tipos;
	}

	private void collectTypeBindings(AmbienteCompilacao amb, Map<Id, Tipo> tipos)
			throws VariavelNaoDeclaradaException, VariavelJaDeclaradaException {
		verificaDuplicidade();
		TipoLista lista = tipoLista(amb);
		if (lista == null) {
			throw new ErroTipoException();
		}

		amb.incrementa();
		try {
			this.padrao.bindTipo(lista.getSubTipo(), amb);
			for (Id id : this.padrao.getIdsLigados()) {
				tipos.put(id, amb.get(id));
			}
			if (this.proximo != null) {
				this.proximo.collectTypeBindings(amb, tipos);
			}
		} finally {
			amb.restaura();
		}
	}

	public boolean checaTipo(AmbienteCompilacao amb)
			throws VariavelNaoDeclaradaException, VariavelJaDeclaradaException {
		verificaDuplicidade();
		if (!this.expressao.checaTipo(amb)) {
			return false;
		}
		TipoLista lista = tipoLista(amb);
		if (lista == null) {
			return false;
		}

		amb.incrementa();
		try {
			this.padrao.bindTipo(lista.getSubTipo(), amb);
			return this.proximo == null || this.proximo.checaTipo(amb);
		} catch (DesestruturacaoException e) {
			return false;
		} finally {
			amb.restaura();
		}
	}

	public void reduzir(AmbienteExecucao ambiente) {
		this.expressao = this.expressao.reduzir(ambiente);
	
		// Protege os nomes do padrao contra substituicao por valores externos.
		for (Id id : this.padrao.getIdsLigados()) {
			ambiente.map(id, new ValorIrredutivel());
		}
	}
	
	public Gerador clone() {
		Gerador copia = new Gerador(this.padrao.clone(), this.expressao.clone());
		if (this.proximo != null) {
			copia.proximo = this.proximo.clone();
		}
		return copia;
	}

	public String toString() {
		return " for " + this.padrao + " in " + this.expressao;
	}
}
