package project.plp.functional3.util.padrao;

import project.plp.expressions1.util.Tipo;
import project.plp.expressions2.expression.Expressao;
import project.plp.expressions2.expression.Id;
import project.plp.expressions2.memory.AmbienteCompilacao;
import project.plp.expressions2.memory.AmbienteExecucao;
import project.plp.functional1.util.TipoPolimorfico;

public class ExpPadraoId extends ExpPadrao<Id> {
	
	public ExpPadraoId(Id expressao) {
		super(expressao);
	}
	
	@Override
	public int getAridade() {
		return 1;
	}
	
	@Override
	public boolean match(AmbienteExecucao ambiente, Expressao matchExpressao) {
		return true;
	}
	
	@Override
	public boolean checaTipo(AmbienteCompilacao ambiente) {
		ambiente.map(this.getExpressao(), new TipoPolimorfico());
		return true;
	}
	
	@Override
	public Tipo getTipo(AmbienteCompilacao ambiente) {
		this.checaTipo(ambiente);
		return this.getExpressao().getTipo(ambiente);
	}

	@Override
	public ExpPadrao<Id> clone() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
