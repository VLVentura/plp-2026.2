package project.plp.functional3.expression;

import project.plp.expressions1.util.Tipo;
import project.plp.expressions1.util.TipoPrimitivo;
import project.plp.expressions2.expression.ExpBinaria;
import project.plp.expressions2.expression.Expressao;
import project.plp.expressions2.expression.Valor;
import project.plp.expressions2.expression.ValorInteiro;
import project.plp.expressions2.memory.AmbienteCompilacao;
import project.plp.expressions2.memory.AmbienteExecucao;
import project.plp.expressions2.memory.VariavelJaDeclaradaException;
import project.plp.expressions2.memory.VariavelNaoDeclaradaException;

public class ExpMult extends ExpBinaria {

	/**
	 * Controi uma Expressao de Multiplica��o com as sub-expressoes
	 * especificadas. Assume-se que estas sub-expressoes resultam em
	 * <code>ValorInteiro</code> quando avaliadas.
	 * 
	 * @param esq
	 *            Expressao da esquerda
	 * @param dir
	 *            Expressao da direita
	 */
	public ExpMult(Expressao esq, Expressao dir) {
		super(esq, dir, "*");
	}

	/**
	 * Retorna o valor da Expressao de Multiplica��o
	 */
	public Valor avaliar(AmbienteExecucao amb)
			throws VariavelNaoDeclaradaException, VariavelJaDeclaradaException {
		return new ValorInteiro(((ValorInteiro) getEsq().avaliar(amb)).valor()
				* ((ValorInteiro) getDir().avaliar(amb)).valor());
	}

	/**
	 * Realiza a verificacao de tipos desta expressao.
	 * 
	 * @param ambiente
	 *            o ambiente de compila��o.
	 * @return <code>true</code> se os tipos da expressao sao validos;
	 *         <code>false</code> caso contrario.
	 * @exception VariavelNaoDeclaradaException
	 *                se existir um identificador nao declarado no ambiente.
	 * @exception VariavelNaoDeclaradaException
	 *                se existir um identificador declarado mais de uma vez no
	 *                mesmo bloco do ambiente.
	 */
	@Override
	protected boolean checaTipoElementoTerminal(AmbienteCompilacao ambiente)
			throws VariavelNaoDeclaradaException, VariavelJaDeclaradaException {
		return getEsq().getTipo(ambiente).eInteiro()
				&& getDir().getTipo(ambiente).eInteiro();
	}

	/**
	 * Retorna os tipos possiveis desta expressao.
	 * 
	 * @param ambiente
	 *            o ambiente de compila��o.
	 * @return os tipos possiveis desta expressao.
	 */
	public Tipo getTipo(AmbienteCompilacao ambiente) {
		return TipoPrimitivo.INTEIRO;
	}
	
	public ExpMult clone() {
		return new ExpMult(this.esq.clone(), this.dir.clone());
	}
}
