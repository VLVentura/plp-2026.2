package project.plp.functional1.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import project.plp.expressions1.util.Tipo;
import project.plp.expressions2.expression.Expressao;
import project.plp.expressions2.expression.Id;
import project.plp.expressions2.memory.AmbienteCompilacao;
import project.plp.expressions2.memory.VariavelJaDeclaradaException;
import project.plp.expressions2.memory.VariavelNaoDeclaradaException;
import project.plp.project.desestruturacao.DesestruturacaoException;
import project.plp.project.desestruturacao.Padrao;
import project.plp.project.util.TipoTupla;

public class DefFuncao {

	protected List<Padrao> parametros;

	protected Expressao exp;

	public DefFuncao(List<Padrao> parametros, Expressao exp) {
		this.parametros = parametros;
		this.exp = exp;
	}

	public List<Padrao> getParametros() {
		return parametros;
	}

	public Expressao getExp() {
		return exp;
	}

	/**
	 * Retorna a aridade desta funcao.
	 *
	 * @return a aridade desta funcao.
	 */
	public int getAridade() {
		return parametros.size();
	}

	private void checaDuplicidade() {
		Set<Id> vistos = new HashSet<Id>();
		for (Padrao parametro : parametros) {
			for (Id id : parametro.getIdsLigados()) {
				if (!vistos.add(id)) {
					throw DesestruturacaoException.duplicidade(id);
				}
			}
		}
	}

	/**
	 * Propaga a inferencia de tipos feita durante a checagem do corpo para
	 * dentro da estrutura da tupla esperada, ja que TipoPolimorfico.inferir()
	 * so resolve a si mesmo, nao os componentes de uma TipoTupla.
	 */
	private static void inferirTipos(Tipo tipo) {
		if (tipo instanceof TipoPolimorfico) {
			((TipoPolimorfico) tipo).inferir();
		} else if (tipo instanceof TipoTupla) {
			for (Tipo componente : ((TipoTupla) tipo).getComponentes()) {
				inferirTipos(componente);
			}
		}
	}

	/**
	 * Realiza a verificacao de tipos desta declara��o.
	 * 
	 * @param amb
	 *            o ambiente de compila��o.
	 * @return <code>true</code> se os tipos da expressao sao validos;
	 *         <code>false</code> caso contrario.
	 * @exception VariavelNaoDeclaradaException
	 *                se existir um identificador nao declarado no ambiente.
	 * @exception VariavelNaoDeclaradaException
	 *                se existir um identificador declarado mais de uma vez no
	 *                mesmo bloco do ambiente.
	 */
	public boolean checaTipo(AmbienteCompilacao ambiente)
			throws VariavelNaoDeclaradaException, VariavelJaDeclaradaException {
		checaDuplicidade();
		ambiente.incrementa();

		// Cada par�metro formal espera um tipo qualquer, ou uma tupla de
		// tipos quaisquer se for um padr�o de tupla. Essa estrutura ser�
		// inferida durante o checaTipo de exp.
		for (Padrao parametro : parametros) {
			parametro.novoTipoEsperado(ambiente);
		}

		// Chama o checa tipo da express�o para veririficar se o corpo da
		// fun��o est� correto. Isto ir� inferir o tipo dos par�metros.
		boolean result = exp.checaTipo(ambiente);

		ambiente.restaura();

		return result;
	}

	/**
	 * Retorna os tipos possiveis desta fun��o.
	 * 
	 * @param amb
	 *            o ambiente que contem o mapeamento entre identificadores e
	 *            tipos.
	 * @return os tipos possiveis desta declara��o.
	 * @exception VariavelNaoDeclaradaException
	 *                se houver uma vari&aacute;vel n&atilde;o declarada no
	 *                ambiente.
	 * @exception VariavelJaDeclaradaException
	 *                se houver uma mesma vari&aacute;vel declarada duas vezes
	 *                no mesmo bloco do ambiente.
	 * @precondition exp.checaTipo();
	 */
	public Tipo getTipo(AmbienteCompilacao ambiente)
			throws VariavelNaoDeclaradaException, VariavelJaDeclaradaException {
		ambiente.incrementa();

		List<Tipo> params = new ArrayList<Tipo>(getAridade());
		for (Padrao parametro : parametros) {
			params.add(parametro.novoTipoEsperado(ambiente));
		}

		// Usa o checaTipo apenas para inferir o tipo dos par�metros.
		// Pois o getTipo da express�o pode simplismente retornar o
		// tipo, por exemplo, no caso de uma express�o bin�ria ou un�ria
		// os tipos sempre s�o bem definidos (Booleano, Inteiro ou String).
		exp.checaTipo(ambiente);

		// Comp�e o tipo desta fun��o do resultado para o primeiro par�metro.
		Tipo result = exp.getTipo(ambiente);

		// Os objetos em params ja sao os mesmos usados na checagem do corpo,
		// entao ja estao ligados aos tipos concretos; falta apenas travar
		// cada TipoPolimorfico ainda livre como curinga.
		for (Tipo param : params) {
			inferirTipos(param);
		}

		result = new TipoFuncao(params, result);
		ambiente.restaura();

		return result;
	}

	public DefFuncao clone() {
		List<Padrao> novaLista = new ArrayList<Padrao>(this.parametros.size());

		for (Padrao parametro : this.parametros){
			novaLista.add(parametro.clone());
		}

		return new DefFuncao(novaLista, this.exp.clone());
	}
}
