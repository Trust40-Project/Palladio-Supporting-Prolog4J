package org.prolog4j.problog;

import org.osgi.service.component.annotations.Component;
import org.prolog4j.ConversionPolicy;
import org.prolog4j.IProverFactory;
import org.prolog4j.Prover;
import org.prolog4j.problog.impl.ProblogConversionPolicy;
import org.prolog4j.problog.impl.ProblogProver;


@Component(immediate = true, property = { "id=org.prolog4j.problog.proverfactory", "name=JProblog Interpreter", "needsNativeExecutables=true"})
public class ProblogProverFactory  implements IProverFactory {

	@Override
	public Prover createProver() {
		return new ProblogProver(createConversionPolicy());
	}

	@Override
	public ConversionPolicy createConversionPolicy() {
		return new ProblogConversionPolicy();
	}

}
