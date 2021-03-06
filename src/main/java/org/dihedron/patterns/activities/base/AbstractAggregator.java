/*
 * Copyright (c) 2012-2015, Andrea Funto'. All rights reserved. See LICENSE for details.
 */ 

package org.dihedron.patterns.activities.base;

import org.dihedron.core.License;
import org.dihedron.patterns.activities.ActivityContext;
import org.dihedron.patterns.activities.exceptions.ActivityException;
import org.dihedron.patterns.activities.types.ActivityData;
import org.dihedron.patterns.activities.types.Scalar;
import org.dihedron.patterns.activities.types.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Andrea Funto'
 */
@License
public abstract class AbstractAggregator extends AbstractActivity {
	
	/**
	 * Whether by default the aggregator should be strict in its application and 
	 * only tolerate vector inputs.
	 */
	public static final boolean DEFAULT_STRICT = true;
	
	/**
	 * The logger.
	 */
	private static final Logger logger = LoggerFactory.getLogger(AbstractAggregator.class);
	
	/**
	 * Whether the aggregator should be strict in its behaviour, and only accept
	 * vector inputs; setting this to false will cause the aggregator to tolerate
	 * scalar inputs and leave them as they are, thus leading to an activity that
	 * is more resilient to input mis-conformance.
	 */
	private boolean strict = DEFAULT_STRICT;

	/**
	 * Returns whether the aggregator is strict about input data types.
	 *
	 * @return 
	 *   if the aggregator only accepts Vector inputs.
	 */
	public boolean isStrict() {
		return strict;
	}

	/**
	 * Sets the value of the strictness about input data types.
	 *
	 * @param strict 
	 *   {@code true} to have the aggregator only accept Vectpr inputs, {@code false}
	 *   if the aggregator must also accept scalar input and leave it as is.
	 */
	public void setStrict(boolean strict) {
		this.strict = strict;
	}

	/**
	 * @see org.dihedron.patterns.activities.Activity#execute(org.dihedron.patterns.activities.ActivityContext, java.lang.Object)
	 */	
	@Override
	public ActivityData perform(ActivityContext context, ActivityData data) throws ActivityException {
		if (data instanceof Vector) {
			return aggregate(context, (Vector)data);
		} else if (data instanceof Scalar) {
			if(strict) {
				logger.error("cardinality mismatch: an aggregator should only be invoked on a collection of objects");
				throw new ActivityException("Cardinality mismatch: an aggregator should only be invoked on a collection of objects");
			} else {
				return data;
			}
		}
		logger.error("an aggregator only supports Vector or Scalar");
		throw new ActivityException("An aggregator only supports Vector or Scalar.");
	}
	
	@Override
	protected abstract Scalar aggregate(ActivityContext context, Vector vector) throws ActivityException;
}
