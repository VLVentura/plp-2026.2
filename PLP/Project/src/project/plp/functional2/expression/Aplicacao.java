package project.plp.functional2.expression;

import static java.util.Arrays.asList;
import static project.plp.expressions1.util.ToStringProvider.listToString;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import project.plp.expressions1.util.Tipo;
import project.plp.expressions2.expression.Expressao;
import project.plp.expressions2.expression.Id;
import project.plp.expressions2.expression.Valor;
import project.plp.expressions2.memory.AmbienteCompilacao;
import project.plp.expressions2.memory.AmbienteExecucao;
import project.plp.expressions2.memory.VariavelJaDeclaradaException;
import project.plp.expressions2.memory.VariavelNaoDeclaradaException;
import project.plp.functional1.util.TipoFuncao;
import project.plp.functional1.util.TipoPolimorfico;
import project.plp.project.desestruturacao.Padrao;

public class Aplicacao implements Expressao {

	private Expressao func;
	private List<? extends Expressao> argsExpressao;

	public Aplicacao(Expressao f, Expressao... expressoes) {
		this(f, asList(expressoes));
	}

	public Aplicacao(Expressao f, List<? extends Expressao> expressoes) {
		func = f;
		argsExpressao = expressoes;
	}

	public Valor avaliar(AmbienteExecucao ambiente)
			throws VariavelNaoDeclaradaException, VariavelJaDeclaradaException {

		ValorFuncao funcao = (ValorFuncao) func.avaliar(ambiente);

		List<Valor> valoresReais = avaliaArgumentos(ambiente);
		ambiente.incrementa();
		bindParametros(ambiente, funcao, valoresReais);

		if(funcao.getId() != null){
			ambiente.map(funcao.getId(), funcao.clone());
		}
		Expressao exp = funcao.getExp().clone();

		exp.reduzir(ambiente);
		
		Valor vresult = exp.avaliar(ambiente);
		
		ambiente.restaura();
		
		return vresult;
	}

	/**
	 * Realiza a verificacao de tipos desta expressao.
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
		Tipo tipo = getFuncType(ambiente);

		boolean result;

		TipoFuncao tipoFuncao = (TipoFuncao) tipo;
		result = tipoFuncao.checaTipo(ambiente, argsExpressao);

		return result;
	}

	private Tipo getFuncType(AmbienteCompilacao ambiente) {
		Tipo tipoFuncao = null;
		if (func instanceof Id) {
			tipoFuncao = ambiente.get((Id) func);
		} else if (func instanceof ValorFuncao) {
			tipoFuncao = ((ValorFuncao) func).getTipo(ambiente);
		}

		if (tipoFuncao == null || tipoFuncao instanceof TipoPolimorfico) {
			ArrayList<Tipo> params = new ArrayList<Tipo>();
			for (Expressao valorReal : argsExpressao) {
				params.add(valorReal.getTipo(ambiente));
			}
			tipoFuncao = new TipoFuncao(params, new TipoPolimorfico());
		}
		return tipoFuncao;
	}

	/**
	 * Returns the args.
	 * 
	 * @return ListaExpressao
	 */
	public List<? extends Expressao> getArgsExpressao() {
		return argsExpressao;
	}

	/**
	 * Returns the func.
	 * 
	 * @return Id
	 */
	public Expressao getFunc() {
		return func;
	}

	/**
	 * Retorna os tipos possiveis desta expressao.
	 * 
	 * @param amb
	 *            o ambiente de compila��o.
	 * @return os tipos possiveis desta expressao.
	 * @exception VariavelNaoDeclaradaException
	 *                se existir um identificador nao declarado no ambiente.
	 * @exception VariavelNaoDeclaradaException
	 *                se existir um identificador declarado mais de uma vez no
	 *                mesmo bloco do ambiente.
	 * @precondition this.checaTipo();
	 */
	public Tipo getTipo(AmbienteCompilacao ambiente)
			throws VariavelNaoDeclaradaException, VariavelJaDeclaradaException {

		Tipo tipo = getFuncType(ambiente);

		TipoFuncao tipoFuncao = (TipoFuncao) tipo;

		return tipoFuncao.getTipo(ambiente, argsExpressao);
	}

	private List<Valor> avaliaArgumentos(AmbienteExecucao ambiente)
			throws VariavelNaoDeclaradaException, VariavelJaDeclaradaException {
		List<Valor> valores = new ArrayList<Valor>(argsExpressao.size());
		for (Expressao exp : argsExpressao) {
			valores.add(exp.avaliar(ambiente));
		}
		return valores;
	}

	private void bindParametros(AmbienteExecucao ambiente, ValorFuncao funcao,
			List<Valor> valoresReais) throws VariavelJaDeclaradaException {
		Iterator<Valor> iterValores = valoresReais.iterator();
		for (Padrao parametro : funcao.getParametros()) {
			parametro.bind(iterValores.next(), ambiente);
		}
	}

	/**
	 * Retorna uma representacao String desta expressao. Util para depuracao.
	 * 
	 * @return uma representacao String desta expressao.
	 */
	@Override
	public String toString() {
		return String.format("%s(%s)", func, listToString(argsExpressao, ","));
	}

	public Expressao reduzir(AmbienteExecucao ambiente) {
		this.func = this.func.reduzir(ambiente);
		
		ArrayList<Expressao> novosArgs =
			new ArrayList<Expressao>(this.argsExpressao.size());
		
		for(Expressao arg : this.argsExpressao) {
			novosArgs.add(arg.reduzir(ambiente));
		}
		this.argsExpressao = novosArgs;
		
		return this;
	}
	
	public Aplicacao clone() {
		Aplicacao retorno;
		ArrayList<Expressao> novaLista = new ArrayList<Expressao>(this.argsExpressao.size());

		Iterator<? extends Expressao> iterator = argsExpressao.iterator();
		while (iterator.hasNext()){
			Expressao exp = iterator.next();
			novaLista.add(exp.clone());			
		}
		
		retorno = new Aplicacao(this.func.clone(), novaLista);
		
		return retorno;
	}
}
