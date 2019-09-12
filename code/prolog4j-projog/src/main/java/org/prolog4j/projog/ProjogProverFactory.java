/* 
 * Copyright (c) 2010 Miklos Espak
 * All rights reserved.
 * 
 * Permission is hereby granted, free  of charge, to any person obtaining
 * a  copy  of this  software  and  associated  documentation files  (the
 * "Software"), to  deal in  the Software without  restriction, including
 * without limitation  the rights to  use, copy, modify,  merge, publish,
 * distribute,  sublicense, and/or sell  copies of  the Software,  and to
 * permit persons to whom the Software  is furnished to do so, subject to
 * the following conditions:
 * 
 * The  above  copyright  notice  and  this permission  notice  shall  be
 * included in all copies or substantial portions of the Software.
 * 
 * THE  SOFTWARE IS  PROVIDED  "AS  IS", WITHOUT  WARRANTY  OF ANY  KIND,
 * EXPRESS OR  IMPLIED, INCLUDING  BUT NOT LIMITED  TO THE  WARRANTIES OF
 * MERCHANTABILITY,    FITNESS    FOR    A   PARTICULAR    PURPOSE    AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE,  ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.prolog4j.projog;

import org.osgi.service.component.annotations.Component;
import org.prolog4j.ConversionPolicy;
import org.prolog4j.IProverFactory;
import org.prolog4j.Prover;

/**
 * An implementation of {@link IProverFactory} which always returns
 * {@link TuPrologProver} instances.
 */
@Component(immediate = true, property = {"id=org.prolog4j.projog.proverfactory", "name=Projog Interpreter"})
public final class ProjogProverFactory implements IProverFactory {

	@Override
	public Prover createProver() {
		return new ProjogProver(createConversionPolicy());
	}

	@Override
	public ConversionPolicy createConversionPolicy() {
		return new ProjogConversionPolicy();
	}

}
