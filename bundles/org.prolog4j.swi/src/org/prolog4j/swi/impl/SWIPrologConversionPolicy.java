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
package org.prolog4j.swi.impl;

import java.lang.reflect.Array;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.prolog4j.Compound;
import org.prolog4j.ConversionPolicy;
import org.prolog4j.Converter;

import org.jpl7.Atom;
import org.jpl7.Term;
import org.jpl7.Util;

/**
 * SWI-Prolog implementation of the conversion policy.
 */
public class SWIPrologConversionPolicy extends ConversionPolicy {

	/** Converts an Integer object to a term. */
	private static final Converter<Integer> INTEGER_CONVERTER = new Converter<Integer>() {
		@Override
		public Object convert(Integer i) {
			return new org.jpl7.Integer(i);
		}
	};
	/** Converts a Long object to a term. */
	private static final Converter<Long> LONG_CONVERTER = new Converter<Long>() {
		@Override
		public Object convert(Long value) {
			return new org.jpl7.Integer(value);
		}
	};
	/** Converts a Float object to a term. */
	private static final Converter<Float> FLOAT_CONVERTER = new Converter<Float>() {
		@Override
		public Object convert(Float value) {
			return new org.jpl7.Float(value);
		}
	};
	/** Converts a Double object to a term. */
	private static final Converter<Double> DOUBLE_CONVERTER = new Converter<Double>() {
		@Override
		public Object convert(Double value) {
			return new org.jpl7.Float(value);
		}
	};
	/** Converts a String object to a term. */
	private static final Converter<String> STRING_CONVERTER = new Converter<String>() {
		@Override
		public Object convert(String value) {
			return new Atom(value);
		}
	};
	/** Converts a jpl.Integer term to an Integer object. */
	private static final Converter<org.jpl7.Integer> Long_TERM_CONVERTER = new Converter<org.jpl7.Integer>() {
		@Override
		public Object convert(org.jpl7.Integer value) {
			return value.longValue();
		}
	};
	/** Converts a jpl.Float term to a Double object. */
	private static final Converter<org.jpl7.Float> DOUBLE_TERM_CONVERTER = 
		new Converter<org.jpl7.Float>() {
		@Override
		public Object convert(org.jpl7.Float value) {
			return value.doubleValue();
		}
	};

	/**
	 * Constructs a conversion policy for SWI-Prolog.
	 */
	public SWIPrologConversionPolicy() {
		super();
		addObjectConverter(Long.class, LONG_CONVERTER);
		addObjectConverter(Float.class, FLOAT_CONVERTER);
		addObjectConverter(Double.class, DOUBLE_CONVERTER);
		addObjectConverter(Integer.class, INTEGER_CONVERTER);
		addObjectConverter(String.class, STRING_CONVERTER);
		addObjectConverter(Object[].class, new Converter<Object[]>() {
			@Override
			public Object convert(Object[] array) {
				Term pList = new Atom("[]", "reserved_symbol");
				for (int i = array.length - 1; i >= 0; --i) {
					pList = new org.jpl7.Compound("[|]", 
							new Term[] { (Term) convertObject(array[i]), pList});
				}
				return pList;
			}
		});
		addListConverter(List.class, new Converter<List<?>>() {
			@Override
			public Object convert(List<?> list) {
				Term pList = new Atom("[]", "reserved_symbol");
				ListIterator<?> it = list.listIterator(list.size());
				while (it.hasPrevious()) {
					pList = new org.jpl7.Compound("[|]",
							new Term[] {(Term) convertObject(it.previous()), pList});
				}
				return pList;
			}
		});
		addObjectConverter(Compound.class, new Converter<Compound>() {
			@Override
			public Object convert(Compound value) {
				String functor = value.getFunctor();
				Object[] args = value.getArgs();
				Term[] tArgs = new Term[value.getArity()];
				for (int i = 0; i < args.length; ++i) {
					tArgs[i] = (Term) convertObject(args[i]);
				}
				return new org.jpl7.Compound(functor, tArgs);
			}
		});
		addObjectConverter(Term.class, new Converter<Term>() {
			@Override
			public Object convert(Term value) {
				return value;
			}
		});
		addTermConverter(org.jpl7.Integer.class, Long_TERM_CONVERTER);
		addTermConverter(org.jpl7.Float.class, DOUBLE_TERM_CONVERTER);
		addTermConverter(Atom.class, new Converter<Atom>() {
			@Override
			public Object convert(Atom value) {
			    if ("[]".equals(value.name()) && "reserved_symbol".equals(value.atomType())) {
			        // we got an empty list
			        return Collections.emptyList();
			    }
				return value.name();
			}
		});
		addTermConverter(org.jpl7.Compound.class, new Converter<org.jpl7.Compound>() {
			@Override
			public Object convert(org.jpl7.Compound value) {
				if (Util.listToLength(value) != -1) {
					Term term = value;
					List<Object> list = new LinkedList<Object>();
					while (term.hasFunctor("[|]", 2)) {
						list.add(convertTerm(term.arg(1)));
						term = term.arg(2);
					}
					return list;
				}
				int arity = value.arity();
				Object[] args = new Object[arity];
				for (int i = 0; i < arity; ++i) {
					args[i] = convertTerm(value.arg(i + 1));
				}
				return new Compound(value.name(), args);
			}

			@SuppressWarnings("unchecked")
			@Override
			public <R> R convert(org.jpl7.Compound value, java.lang.Class<R> to) {
				int length = Util.listToLength(value);
				if (length != -1 && Object[].class.isAssignableFrom(to)) {
					R[] array = (R[]) Array.newInstance(to.getComponentType(), length);
					Term term = value;
					for (int i = 0; i < length; ++i) {
						array[i] = (R) convertTerm(term.arg(1));
						term = term.arg(2);
					}
					return to.cast(array);
				}
				return null;
			}
		});
	}

	@Override
	public boolean match(Object term1, Object term2) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object term(int value) {
		return new org.jpl7.Integer(value);
	}

	@Override
	public Object term(double value) {
		return new org.jpl7.Float(value);
	}

	@Override
	public Object term(String name) {
		return Util.textToTerm(name);
	}

	@Override
	public Object term(String name, Object... args) {
		Term[] args2 = new Term[args.length];
		for (int i = 0; i < args2.length; ++i) {
			args2[i] = (Term) convertObject(args[i]);
		}
		return Util.textToTerm(name).putParams(args2);
	}

	@Override
	public int intValue(Object term) {
		return ((Term) term).intValue();
	}

	@Override
	public double doubleValue(Object term) {
		return ((Term) term).doubleValue();
	}

	@Override
	protected String getName(Object compound) {
		return ((Term) compound).name();
	}

	@Override
	public int getArity(Object compound) {
		return ((Term) compound).arity();
	}


	@Override
	public Object getArg(Object compound, int index) {
		return convertTerm(((Term) compound).arg(index + 1));
	}

	@Override
	public boolean isAtom(Object term) {
		return ((Term) term).isAtom();
	}

	@Override
	public boolean isCompound(Object term) {
		return term instanceof org.jpl7.Compound;
	}

	@Override
	public boolean isDouble(Object term) {
		return term instanceof org.jpl7.Float;
	}

	@Override
	public boolean isInteger(Object term) {
		return term instanceof org.jpl7.Integer;
	}

}
