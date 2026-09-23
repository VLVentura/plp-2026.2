package project.plp.functional3.desestruturacao;

import project.plp.expressions2.expression.Id;

/**
 * Erro de desestruturacao: o padrao nao bate em aridade ou em estrutura com
 * o valor ou o tipo recebido, ou o mesmo identificador aparece mais de uma
 * vez no padrao.
 */
public class DesestruturacaoException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public enum Motivo {
		ARIDADE, ESTRUTURA, DUPLICIDADE
	}

	private final Motivo motivo;

	private DesestruturacaoException(Motivo motivo, String mensagem) {
		super(mensagem);
		this.motivo = motivo;
	}

	public Motivo getMotivo() {
		return motivo;
	}

	public static DesestruturacaoException aridade(Padrao padrao, Object recebido, int aridadeEsperada,
			int aridadeRecebida) {
		return new DesestruturacaoException(Motivo.ARIDADE, String.format(
				"padrao %s espera %d posicoes, mas %s tem %d", padrao, aridadeEsperada, recebido, aridadeRecebida));
	}

	public static DesestruturacaoException estrutura(Padrao padrao, Object recebido) {
		return new DesestruturacaoException(Motivo.ESTRUTURA,
				String.format("padrao %s espera uma tupla, mas recebeu %s", padrao, recebido));
	}

	public static DesestruturacaoException duplicidade(Id id) {
		return new DesestruturacaoException(Motivo.DUPLICIDADE,
				String.format("identificador %s aparece mais de uma vez no padrao", id));
	}
}
