package project.plp.project.util;

import static project.plp.expressions1.util.ToStringProvider.listToString;

import java.util.ArrayList;
import java.util.List;

import project.plp.expressions1.util.Tipo;
import project.plp.functional1.util.TipoPolimorfico;

/**
 * Tipo de uma tupla, com a lista dos tipos de cada posicao. Aridade minima
 * de duas posicoes, espelhando a sintaxe de ExpTupla.
 */
public class TipoTupla implements Tipo {

	private List<Tipo> componentes;

	public TipoTupla(List<Tipo> componentes) {
		this.componentes = componentes;
	}

	public List<Tipo> getComponentes() {
		return componentes;
	}

	public int getAridade() {
		return componentes.size();
	}

	public boolean eBooleano() {
		return false;
	}

	public boolean eInteiro() {
		return false;
	}

	public boolean eString() {
		return false;
	}

	public boolean eValido() {
		boolean valido = true;
		for (Tipo componente : componentes) {
			valido &= componente.eValido();
		}
		return valido;
	}

	public String getNome() {
		return "(" + listToString(componentes, ",") + ")";
	}

	@Override
	public String toString() {
		return getNome();
	}

	public boolean eIgual(Tipo tipo) {
		if (tipo instanceof TipoPolimorfico) {
			return tipo.eIgual(this);
		}

		if (tipo instanceof TipoTupla) {
			TipoTupla outra = (TipoTupla) tipo;
			if (this.getAridade() != outra.getAridade()) {
				return false;
			}

			boolean igual = true;
			for (int i = 0; i < componentes.size(); i++) {
				igual &= componentes.get(i).eIgual(outra.componentes.get(i));
			}

			return igual;
		}

		// Uma tupla nunca e igual a um tipo de outra natureza (primitivo,
		// lista ou funcao). Delegar para tipo.eIgual(this) aqui entraria em
		// loop com tipos que tambem delegam para o que nao reconhecem, como
		// TipoLista.
		return false;
	}

	public Tipo intersecao(Tipo outroTipo) {
		if (outroTipo instanceof TipoPolimorfico) {
			return outroTipo.intersecao(this);
		}

		if (outroTipo instanceof TipoTupla) {
			TipoTupla outra = (TipoTupla) outroTipo;
			if (this.getAridade() != outra.getAridade()) {
				return null;
			}

			List<Tipo> resultado = new ArrayList<Tipo>(componentes.size());
			for (int i = 0; i < componentes.size(); i++) {
				Tipo intersecaoComponente = componentes.get(i).intersecao(outra.componentes.get(i));
				if (intersecaoComponente == null) {
					return null;
				}
				resultado.add(intersecaoComponente);
			}

			return new TipoTupla(resultado);
		}
 
		return null;
	}
}
