package plp.debug.project;

import java.util.ArrayList;
import java.util.List;

import project.plp.expressions1.util.Tipo;
import project.plp.expressions2.expression.Expressao;
import project.plp.expressions2.expression.Id;
import project.plp.expressions2.memory.AmbienteCompilacao;
import project.plp.expressions2.memory.VariavelJaDeclaradaException;
import project.plp.expressions2.memory.VariavelNaoDeclaradaException;
import project.plp.functional1.util.TipoFuncao;
import project.plp.functional2.expression.ValorFuncao;
import project.plp.project.desestruturacao.Padrao;
import plp.debug.core.InfoEscopo;
import plp.debug.core.ScopeAware;

/**
 * Estende {@link ValorFuncao} publicando o escopo da função (onde os
 * parâmetros formais são vinculados) com a faixa exata de código-fonte
 * capturada pelo parser do WebDebug.
 *
 * {@code checaTipo}/{@code getTipo} são herdados de {@code DefFuncao} e
 * reproduzidos aqui apenas para inserir o registro do escopo logo após o
 * {@code incrementa()}. Project permanece inalterada.
 */
public class ValorFuncaoDebug extends ValorFuncao {

	private final InfoEscopo infoEscopo;

	public ValorFuncaoDebug(List<Padrao> parametros, Expressao exp, InfoEscopo infoEscopo) {
		super(parametros, exp);
		this.infoEscopo = infoEscopo;
	}

	private void registra(AmbienteCompilacao ambiente) {
		if (ambiente instanceof ScopeAware) {
			((ScopeAware) ambiente).registraEscopo(infoEscopo);
		}
	}

	/**
	 * Reregistra os parâmetros formais após a verificação do corpo.
	 *
	 * No momento do {@code map()} o parâmetro ainda é um
	 * {@code TipoPolimorfico} não inferido, cujo nome é "?". Depois de checar
	 * o corpo, o mesmo objeto já conhece o tipo inferido, então basta reler
	 * {@code getNome()} para o debugger exibir o tipo concreto.
	 */
	private void refinaParametros(AmbienteCompilacao ambiente) {
		if (!(ambiente instanceof ScopeAware)) {
			return;
		}
		for (Padrao parametro : parametros) {
			for (Id id : parametro.getIdsLigados()) {
				try {
					Tipo tipo = ambiente.get(id);
					if (tipo != null) {
						((ScopeAware) ambiente).registraBinding(id, tipo, tipo.getNome());
					}
				} catch (VariavelNaoDeclaradaException ignored) {
					// Parâmetro fora do ambiente: mantém o valor já registrado.
				}
			}
		}
	}

	@Override
	public boolean checaTipo(AmbienteCompilacao ambiente)
			throws VariavelNaoDeclaradaException, VariavelJaDeclaradaException {
		checaDuplicidade();
		ambiente.incrementa();
		registra(ambiente);

		for (Padrao parametro : parametros) {
			parametro.novoTipoEsperado(ambiente);
		}

		boolean result = exp.checaTipo(ambiente);

		refinaParametros(ambiente);
		ambiente.restaura();

		return result;
	}

	@Override
	public Tipo getTipo(AmbienteCompilacao ambiente)
			throws VariavelNaoDeclaradaException, VariavelJaDeclaradaException {
		ambiente.incrementa();
		registra(ambiente);

		List<Tipo> params = new ArrayList<Tipo>(getAridade());
		for (Padrao parametro : parametros) {
			params.add(parametro.novoTipoEsperado(ambiente));
		}

		exp.checaTipo(ambiente);

		Tipo result = exp.getTipo(ambiente);

		for (Tipo param : params) {
			inferirTipos(param);
		}
		result = new TipoFuncao(params, result);

		refinaParametros(ambiente);
		ambiente.restaura();

		return result;
	}

	@Override
	public ValorFuncaoDebug clone() {
		List<Padrao> novaLista = new ArrayList<Padrao>(this.parametros.size());
		for (Padrao parametro : this.parametros) {
			novaLista.add(parametro.clone());
		}
		return new ValorFuncaoDebug(novaLista, this.exp.clone(), infoEscopo);
	}
}
