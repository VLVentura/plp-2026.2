package project.plp.functional3.expression;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import project.plp.expressions1.util.Tipo;
import project.plp.expressions2.expression.Expressao;
import project.plp.expressions2.expression.Id;
import project.plp.expressions2.expression.Valor;
import project.plp.expressions2.memory.AmbienteCompilacao;
import project.plp.expressions2.memory.AmbienteExecucao;
import project.plp.expressions2.memory.VariavelJaDeclaradaException;
import project.plp.expressions2.memory.VariavelNaoDeclaradaException;
import project.plp.functional3.util.TipoLista;

public class ExpCompreensaoLista implements Expressao {

	private Expressao expressao;
	private Expressao filtro;
	private Gerador gerador;

	public ExpCompreensaoLista(Expressao expressao) {
		this.expressao = expressao;
	}

	public void setFiltro(Expressao filtro) {
		this.filtro = filtro;
	}

	public void add(Gerador gerador) {
		if (this.gerador == null) {
			this.gerador = gerador;
		} else {
			this.gerador.addProximoGerador(gerador);
		}
	}

	public void setGeradores(List<Gerador> geradores) {
		for (Gerador geradorTemp : geradores) {
			this.add(geradorTemp);
		}
	}

	public Valor avaliar(AmbienteExecucao amb)
			throws VariavelNaoDeclaradaException, VariavelJaDeclaradaException {
		ValorLista result = ValorLista.getInstancia(null, null);

		gerador.gerarValores(amb, result, this.expressao, this.filtro);

		return result.inverter();
	}

	public boolean checaTipo(AmbienteCompilacao amb)
			throws VariavelNaoDeclaradaException, VariavelJaDeclaradaException {
		if (!temGerador())
			return false;

		amb.incrementa();
		try {
			if (!gerador.checaTipo(amb)) {
				return false;
			}
			mapTypeBindings(amb);
			return expressao.checaTipo(amb) && filtroChecaTipo(amb);
		} finally {
			amb.restaura();
		}
	}

	private void mapTypeBindings(AmbienteCompilacao amb) throws VariavelJaDeclaradaException {
		Map<Id, Tipo> typeBindings = gerador.checkTypeBindings(amb);
		Set<Entry<Id, Tipo>> entrySet = typeBindings.entrySet();
		for (Entry<Id, Tipo> entry : entrySet) {
			amb.map(entry.getKey(), entry.getValue());
		}
	}

	private boolean temGerador() {
		return gerador != null;
	}

	private boolean filtroChecaTipo(AmbienteCompilacao amb) {
		return filtro == null || filtro.checaTipo(amb)
				&& filtro.getTipo(amb).eBooleano();
	}

	public Tipo getTipo(AmbienteCompilacao amb)
			throws VariavelNaoDeclaradaException, VariavelJaDeclaradaException {

		amb.incrementa();
		try {
			mapTypeBindings(amb);
			return new TipoLista(expressao.getTipo(amb));
		} finally {
			amb.restaura();
		}
	}
	
	public ExpCompreensaoLista clone() {
		ExpCompreensaoLista retorno = new ExpCompreensaoLista(this.expressao.clone());
		if (this.filtro != null) {
			retorno.setFiltro(this.filtro.clone());
		} 

		if (this.gerador != null) {
			retorno.gerador = this.gerador.clone();
		}
		
		return retorno;
	}

	public Expressao reduzir(AmbienteExecucao ambiente) {
		reduzirGeradores(this.gerador, ambiente);
		return this;
	}

	private void reduzirGeradores(Gerador gerador, AmbienteExecucao ambiente) {
		if (gerador == null) {
			// Todos os padroes ainda estao em escopo ao reduzir o resultado.
			this.expressao = this.expressao.reduzir(ambiente);
			if (this.filtro != null) {
				this.filtro = this.filtro.reduzir(ambiente);
			}
			return;
		}

		ambiente.incrementa();
		try {
			gerador.reduzir(ambiente);
			reduzirGeradores(gerador.getProximoGerador(), ambiente);
		} finally {
			ambiente.restaura();
		}
	}
	
	public String toString() {
		String aux = this.expressao.toString();
		
		Gerador ger = this.gerador;
		aux += ger.toString();		
		
		while(ger.temProximoGerador()){
			ger = ger.getProximoGerador();
			
			aux += "," + ger.toString();			
		}
		
		if (this.filtro != null) {
			aux += " if "+this.filtro;
		}
		
		return aux;
	}
}
