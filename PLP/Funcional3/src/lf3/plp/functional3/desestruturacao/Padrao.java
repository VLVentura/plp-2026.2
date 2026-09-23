package lf3.plp.functional3.desestruturacao;

import java.util.List;

import lf3.plp.expressions1.util.Tipo;
import lf3.plp.expressions2.expression.Id;
import lf3.plp.expressions2.expression.Valor;
import lf3.plp.expressions2.memory.AmbienteCompilacao;
import lf3.plp.expressions2.memory.AmbienteExecucao;
import lf3.plp.expressions2.memory.VariavelJaDeclaradaException;

/**
 * Um padrao decide quais identificadores um valor, ou um tipo, desestruturado
 * passa a ligar. E irrefutavel: a validade depende so da estrutura, nunca do
 * valor em si.
 */
public interface Padrao {

	void bind(Valor valor, AmbienteExecucao ambiente) throws VariavelJaDeclaradaException;

	void bindTipo(Tipo tipo, AmbienteCompilacao ambiente) throws VariavelJaDeclaradaException;

	Tipo novoTipoEsperado(AmbienteCompilacao ambiente) throws VariavelJaDeclaradaException;

	List<Id> getIdsLigados();

	Padrao clone();
}
