/*
 * Universidade Federal de Pernambuco - UFPE
 * Centro de Inform�tica - CIn
 * 
 * Paradigmas de Linguagem de Programa��o - PLP
 * 
 * Tipo: TipoFuncao
 */
package project.plp.functional1.util;

import static project.plp.expressions1.util.ToStringProvider.listToString;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import project.plp.expressions1.util.Tipo;
import project.plp.expressions2.expression.Expressao;
import project.plp.expressions2.memory.AmbienteCompilacao;
import project.plp.expressions2.memory.VariavelJaDeclaradaException;
import project.plp.expressions2.memory.VariavelNaoDeclaradaException;
import project.plp.project.desestruturacao.DesestruturacaoException;
import project.plp.project.util.TipoTupla;

/**
 * Esta classe representa o tipo de uma fun��o.
 * 
 * Caso o tipo do dom�nio da fun��o tamb�m seja um objeto dessa classe a fun��o
 * representada por esse objeto � uma fun��o que recebe uma fun��o como
 * par�metro, logo, trata-se de um caso de suporte a fun��es de alta ordem.
 * 
 * Se o tipo da imagem da fun��o tamb�m for um objeto da classe TipoFuncao
 * trata-se de uma fun��o com m�ltiplos par�metros. Assim, o tipo do retorno da
 * fun��o ser� sempre o tipo da imagem do �ltimo objeto dessa classe.
 * 
 * @author Joabe Jesus (jbjj@cin.ufpe.br)
 */
public class TipoFuncao implements Tipo {

	/**
	 * O tipo do dom�nio da fun��o.
	 */
	private List<Tipo> dominio;

	/**
	 * O tipo da imagem (o tipo de retorno) da fun��o.
	 */
	private Tipo imagem;

	/**
	 * Construtor da classe que representa um tipo fun��o (T1 x ... x Tn -> T).
	 * 
	 * @param dominio
	 *            A lista dos tipos do dom�nio da fun��o (T1 x ... x Tn).
	 * @param imagem
	 *            O tipo da imagem da fun��o (T).
	 */
	public TipoFuncao(List<Tipo> dominio, Tipo imagem) {
		this.dominio = dominio;
		this.imagem = imagem;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see project.plp.expressions1.util.Tipo#getNome()
	 */
	public String getNome() {
		return String.format("(%s) -> %s", listToString(dominio, " x"), imagem);
	}

	public List<Tipo> getDominio() {
		return dominio;
	}

	public Tipo getImagem() {
		return imagem;
	}

	public boolean eBooleano() {
		return imagem.eBooleano();
	}

	public boolean eInteiro() {
		return imagem.eInteiro();
	}

	public boolean eString() {
		return imagem.eString();
	}

	public boolean eValido() {
		boolean ret = dominio != null;
		for (Tipo t : this.dominio) {
			ret &= t.eValido();
		}
		ret &= imagem != null && imagem.eValido();
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see project.plp.expressions1.util.Tipo#eIgual(project.plp.expressions1.util.Tipo)
	 */
	public boolean eIgual(Tipo tipo) {
		boolean ret = true;
		if (tipo instanceof TipoPolimorfico)
			return tipo.eIgual(this);

		if (tipo instanceof TipoFuncao) {
			TipoFuncao tipoFuncao = (TipoFuncao) tipo;
			if (this.dominio.size() != tipoFuncao.dominio.size())
				return false;
			Iterator<Tipo> it = this.dominio.iterator();
			for (Tipo t : tipoFuncao.dominio) {
				ret &= t.eIgual(it.next());
			}
			return ret && this.imagem.eIgual(tipoFuncao.imagem);
		}

		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see project.plp.expressions1.util.Tipo#intersecao(project.plp.expressions1.util.Tipo)
	 */
	public Tipo intersecao(Tipo outroTipo) {
		if (outroTipo.eIgual(this))
			return this;
		else
			return null;
	}

	@Override
	public String toString() {
		return getNome();
	}

	/**
	 * Este m�todo � usado para limpar os tipos curingas, pois ap�s a aplica��o
	 * os mesmos podem estar instanciados e isto pode influenciar um erro de
	 * tipos na pr�xima aplica��o.
	 */
	private void limparTiposCuringas() {
		for (Tipo tDom : getDominio()) {
			limparTipoCuringa(tDom);
		}
		limparTipoCuringa(getImagem());
	}

	/**
	 * Desce por dentro de tuplas para limpar cada TipoPolimorfico folha, ja
	 * que limpar() so afeta o proprio objeto, nao os componentes de uma
	 * TipoTupla. Um TipoPolimorfico ja inferido como tupla nao e um curinga.
	 */
	private void limparTipoCuringa(Tipo tipo) {
		if (tipo instanceof TipoPolimorfico) {
			Tipo inferido = ((TipoPolimorfico) tipo).getTipoInferido();
			if (inferido instanceof TipoTupla) {
				limparTipoCuringa(inferido);
			} else {
				((TipoPolimorfico) tipo).limpar();
			}
		} else if (tipo instanceof TipoTupla) {
			for (Tipo componente : ((TipoTupla) tipo).getComponentes()) {
				limparTipoCuringa(componente);
			}
		}
	}

	/**
	 * Copia um tipo substituindo cada TipoPolimorfico folha pelo tipo
	 * instanciado nesta chamada, para que o resultado sobreviva a
	 * limparTiposCuringas() feita logo em seguida.
	 */
	private Tipo materializar(Tipo tipo) {
		if (tipo instanceof TipoPolimorfico) {
			Tipo instanciado = ((TipoPolimorfico) tipo).getTipoInstanciado();
			return instanciado == null ? tipo : materializar(instanciado);
		}

		if (tipo instanceof TipoTupla) {
			List<Tipo> componentes = new ArrayList<Tipo>();
			for (Tipo componente : ((TipoTupla) tipo).getComponentes()) {
				componentes.add(materializar(componente));
			}
			return new TipoTupla(componentes);
		}

		return tipo;
	}

	private boolean checkArgumentListSize(
			List<? extends Expressao> parametrosFormais) {
		return getDominio().size() == parametrosFormais.size();
	}

	private boolean checkArgumentTypes(AmbienteCompilacao ambiente,
			List<? extends Expressao> parametrosFormais)
			throws VariavelNaoDeclaradaException, VariavelJaDeclaradaException {
		boolean result = true;

		Iterator<Tipo> it = getDominio().iterator();
		Tipo tipoArg;
		for (Expressao valorReal : parametrosFormais) {

			result &= valorReal.checaTipo(ambiente);

			tipoArg = valorReal.getTipo(ambiente);
			Tipo tipoDom = it.next();

			checaEstruturaTupla(tipoDom, tipoArg);
			result &= tipoArg.eIgual(tipoDom);
		}
		return result;
	}

	/**
	 * Quando o parametro espera uma tupla, reporta um argumento que nao e
	 * tupla, ou que tem outra aridade, como erro de desestruturacao, em vez
	 * de deixar eIgual apenas devolver false sem dizer o motivo.
	 */
	private static void checaEstruturaTupla(Tipo esperado, Tipo recebido) {
		esperado = seguirTipoPolimorfico(esperado);
		if (!(esperado instanceof TipoTupla)) {
			return;
		}

		recebido = seguirTipoPolimorfico(recebido);
		if (recebido instanceof TipoPolimorfico) {
			// Ainda livre: a unificacao decide.
			return;
		}
		if (!(recebido instanceof TipoTupla)) {
			throw DesestruturacaoException.estruturaArgumento(esperado, recebido);
		}

		List<Tipo> componentesEsperados = ((TipoTupla) esperado).getComponentes();
		List<Tipo> componentesRecebidos = ((TipoTupla) recebido).getComponentes();
		if (componentesEsperados.size() != componentesRecebidos.size()) {
			throw DesestruturacaoException.aridadeArgumento(esperado, recebido,
					componentesEsperados.size(), componentesRecebidos.size());
		}

		for (int i = 0; i < componentesEsperados.size(); i++) {
			checaEstruturaTupla(componentesEsperados.get(i), componentesRecebidos.get(i));
		}
	}

	/**
	 * Segue um TipoPolimorfico ate o tipo que ele representa, pela instancia
	 * desta chamada ou pela inferencia, parando em uma variavel ainda livre.
	 */
	private static Tipo seguirTipoPolimorfico(Tipo tipo) {
		while (tipo instanceof TipoPolimorfico) {
			TipoPolimorfico polimorfico = (TipoPolimorfico) tipo;
			Tipo proximo = polimorfico.getTipoInstanciado() != null ? polimorfico.getTipoInstanciado()
					: polimorfico.getTipoInferido();
			if (proximo == null || proximo == TipoPolimorfico.CURINGA) {
				break;
			}
			tipo = proximo;
		}
		return tipo;
	}

	public boolean checaTipo(AmbienteCompilacao ambiente,
			List<? extends Expressao> parametrosFormais) {
		boolean result = checkArgumentListSize(parametrosFormais)
				&& checkArgumentTypes(ambiente, parametrosFormais);
		limparTiposCuringas();
		return result;
	}

	public Tipo getTipo(AmbienteCompilacao ambiente,
			List<? extends Expressao> parametrosFormais) {
		// Infere os par�metros
		Iterator<Tipo> it = getDominio().iterator();
		Tipo tipoArg;
		for (Expressao valorReal : parametrosFormais) {
			tipoArg = valorReal.getTipo(ambiente);
			tipoArg.eIgual(it.next());
		}

		// Obtem o resultado, ja materializado com a instancia��o desta
		// chamada, pois limparTiposCuringas() vai apagar essa informa��o dos
		// objetos originais logo em seguida.
		Tipo ret = materializar(getImagem());

		limparTiposCuringas();
		return ret;
	}

}
