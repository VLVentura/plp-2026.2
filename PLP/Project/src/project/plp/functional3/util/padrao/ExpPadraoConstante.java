package project.plp.functional3.util.padrao;

import project.plp.expressions1.util.Tipo;
import project.plp.expressions2.expression.Expressao;
import project.plp.expressions2.expression.Valor;
import project.plp.expressions2.expression.ValorConcreto;
import project.plp.expressions2.memory.AmbienteCompilacao;
import project.plp.expressions2.memory.AmbienteExecucao;

@SuppressWarnings("unchecked")
public class ExpPadraoConstante extends ExpPadrao<ValorConcreto> {
	
	public ExpPadraoConstante(ValorConcreto expressao) {
		super(expressao);
	}
	
	@Override
	public int getAridade() {
		return 0;
	}
	
	@Override
	public boolean match(AmbienteExecucao ambiente, Expressao matchExpressao) {
		Valor valorPadrao = this.getExpressao().avaliar(ambiente);
		Valor valorMatch = matchExpressao.avaliar(ambiente);
		
		return valorPadrao.equals(valorMatch);
	}
	
	@Override
	public boolean checaTipo(AmbienteCompilacao ambiente) {
		return true;
	}
	
	@Override
	public Tipo getTipo(AmbienteCompilacao ambiente) {
		return this.getExpressao().getTipo(ambiente);
	}

	@Override
	public ExpPadrao<ValorConcreto> clone() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
