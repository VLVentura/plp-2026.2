package lf3.plp.functional1.declaration;

import java.util.HashSet;
import java.util.Set;

import lf3.plp.expressions1.util.Tipo;
import lf3.plp.expressions2.expression.Expressao;
import lf3.plp.expressions2.expression.Id;
import lf3.plp.expressions2.memory.AmbienteCompilacao;
import lf3.plp.expressions2.memory.AmbienteExecucao;
import lf3.plp.expressions2.memory.VariavelJaDeclaradaException;
import lf3.plp.expressions2.memory.VariavelNaoDeclaradaException;
import lf3.plp.functional3.desestruturacao.DesestruturacaoException;
import lf3.plp.functional3.desestruturacao.Padrao;
import lf3.plp.functional3.desestruturacao.PadraoId;

public class DecVariavel implements DeclaracaoFuncional {
	private Padrao padrao;
	private Expressao expressao;

	public DecVariavel(Padrao padraoArg, Expressao expressaoArg) {
		padrao = padraoArg;
		expressao = expressaoArg;
	}

	public DecVariavel(Id idArg, Expressao expressaoArg) {
		this(new PadraoId(idArg), expressaoArg);
	}

	/**
	 * Retorna uma representacao String desta expressao. Util para depuracao.
	 *
	 * @return uma representacao String desta expressao.
	 */
	@Override
	public String toString() {
		return String.format("var %s = %s", padrao, expressao);
	}

	public Expressao getExpressao() {
		return expressao;
	}

	public Padrao getPadrao() {
		return padrao;
	}

	public Id getId() {
		return ((PadraoId) padrao).getId();
	}

	/**
	 * Retorna os tipos possiveis desta declara��o.
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
	 * @precondition this.checaTipo(amb);
	 */
	public Tipo getTipo(AmbienteCompilacao amb)
			throws VariavelNaoDeclaradaException, VariavelJaDeclaradaException {
		return expressao.getTipo(amb);
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
		return expressao.checaTipo(ambiente);
	}

	private void checaDuplicidade() {
		Set<Id> vistos = new HashSet<Id>();
		for (Id id : padrao.getIdsLigados()) {
			if (!vistos.add(id)) {
				throw DesestruturacaoException.duplicidade(id);
			}
		}
	}

	public void elabora(AmbienteExecucao amb, AmbienteExecucao aux) throws VariavelJaDeclaradaException {
		padrao.bind(getExpressao().avaliar(amb), aux);
	}

	public void elabora(AmbienteCompilacao amb, AmbienteCompilacao aux) throws VariavelJaDeclaradaException {
		padrao.bindTipo(getTipo(amb), aux);
	}

	public void incluir(AmbienteExecucao amb, AmbienteExecucao aux) throws VariavelJaDeclaradaException {
		for (Id id : padrao.getIdsLigados()) {
			amb.map(id, aux.get(id));
		}
	}

	public void incluir(AmbienteCompilacao amb, AmbienteCompilacao aux, boolean incluirCuringa)
			throws VariavelJaDeclaradaException {
		for (Id id : padrao.getIdsLigados()) {
			amb.map(id, aux.get(id));
		}
	}

	public void reduzir(AmbienteExecucao amb) {
		for (Id id : padrao.getIdsLigados()) {
			amb.map(id, null);
		}
	}

	public DecVariavel clone() {
		return new DecVariavel(this.padrao.clone(), this.expressao.clone());
	}
}
