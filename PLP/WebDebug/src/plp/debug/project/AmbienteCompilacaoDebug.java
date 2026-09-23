package plp.debug.project;

import project.plp.expressions2.expression.Id;
import project.plp.expressions1.util.Tipo;
import project.plp.expressions2.memory.AmbienteCompilacao;
import project.plp.expressions2.memory.VariavelJaDeclaradaException;
import project.plp.expressions2.memory.VariavelNaoDeclaradaException;
import plp.debug.core.InfoEscopo;
import plp.debug.core.ScopeAware;
import plp.debug.core.SnapshotRecorder;

/**
 * Implementa {@link AmbienteCompilacao} delegando todas as chamadas a um
 * ambiente real, sem alterar Project. Observa incrementa()/restaura()/
 * map() para alimentar um {@link SnapshotRecorder}.
 */
public class AmbienteCompilacaoDebug implements AmbienteCompilacao, ScopeAware {

	private final AmbienteCompilacao target;
	private final SnapshotRecorder recorder = new SnapshotRecorder();

	public AmbienteCompilacaoDebug(AmbienteCompilacao target) {
		this.target = target;
	}

	public SnapshotRecorder getRecorder() {
		return recorder;
	}

	@Override
	public void registraEscopo(InfoEscopo info) {
		recorder.registraEscopo(info);
	}

	@Override
	public void registraBinding(Object idArg, Object valorArg, String display) {
		recorder.recordBinding(idArg, valorArg, display);
	}

	@Override
	public void incrementa() {
		target.incrementa();
		recorder.pushFrame();
	}

	@Override
	public void restaura() {
		target.restaura();
		recorder.popFrame();
	}

	@Override
	public void map(Id idArg, Tipo tipoId) throws VariavelJaDeclaradaException {
		target.map(idArg, tipoId);
		recorder.recordBinding(idArg, tipoId, tipoId == null ? null : tipoId.getNome());
	}

	@Override
	public Tipo get(Id idArg) throws VariavelNaoDeclaradaException {
		return target.get(idArg);
	}
}
