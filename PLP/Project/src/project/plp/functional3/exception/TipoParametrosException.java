package project.plp.functional3.exception;

import project.plp.expressions2.expression.Id;

public class TipoParametrosException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;
	private static String formatStr = "Tipo de par�metros incorretos para a fun��o %s";
	
	public TipoParametrosException(Id id) {
		super(String.format(formatStr, id.getIdName()));
	}
	
}
