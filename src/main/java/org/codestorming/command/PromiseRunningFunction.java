/*
 * Copyright (c) 2012-2018 Codestorming.org
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Codestorming - initial API and implementation
 */
package org.codestorming.command;

/**
 * Functional interface defining the function to be executed by a {@link IPromise}.
 *
 * @author Thaedrik [thaedrik@codestorming.org]
 * @since 2.1
 */
@FunctionalInterface
public interface PromiseRunningFunction<T> {

	/**
	 * Function executed by a {@link IPromise promise}.
	 *
	 * @param resolve Called on success with the promise's result.
	 * @param reject Called on failure with the promise's error.
	 */
	void call(Callback<T> resolve, Callback<Throwable> reject);
}
