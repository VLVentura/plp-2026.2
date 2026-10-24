package project.plp.project.expression;

/**
 * Erro de projecao de tupla (e.k): a posicao nao existe na tupla, o valor ou
 * tipo projetado nao e uma tupla, ou o tipo ainda nao e conhecido.
 */
public class ProjecaoException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public enum Motivo {
		INDICE, ESTRUTURA, TIPO_LIVRE
	}

	private final Motivo motivo;

	private ProjecaoException(Motivo motivo, String mensagem) {
		super(mensagem);
		this.motivo = motivo;
	}

	public Motivo getMotivo() {
		return this.motivo;
	}

	public static ProjecaoException indice(int indice, Object tupla, int aridade) {
		return new ProjecaoException(Motivo.INDICE, String.format(
				"projecao .%d fora da tupla %s, que tem %d posicoes (a primeira e .1)", indice, tupla, aridade));
	}

	public static ProjecaoException estrutura(int indice, Object recebido) {
		return new ProjecaoException(Motivo.ESTRUTURA,
				String.format("projecao .%d espera uma tupla, mas recebeu %s", indice, recebido));
	}

	public static ProjecaoException tipoLivre(int indice, Object expressao) {
		return new ProjecaoException(Motivo.TIPO_LIVRE, String.format(
				"projecao .%d precisa de uma tupla de tipo conhecido, mas o tipo de %s ainda nao foi inferido;"
						+ " use desestruturacao (let var (a, b) = ...)",
				indice, expressao));
	}
}
