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

import java.util.concurrent.ExecutorService;

/**
 * A {@code PromiseEnvironment} allows to create {@link IPromise promises} that will
 * be executed in the defined {@link ExecutorService}.
 *
 * @author Thaedrik [thaedrik@codestorming.org]
 * @since 2.1
 */
public class PromiseEnvironment {

	protected final ExecutorService executorService;

	/**
	 * Creates a {@code PromiseEnvironment} with the specified {@link ExecutorService}.
	 *
	 * @param executorService The executor service to use to run the {@link IPromise promises}.
	 */
	public PromiseEnvironment(ExecutorService executorService) {
		this.executorService = executorService;
	}

	/**
	 * Creates a new {@link Promise} that will execute the running function into the defined {@link ExecutorService}.
	 * <p>
	 * The function is immediately scheduled to run.
	 *
	 * @param runningFunction The promise's running function.
	 * @param <T> Type of the promise's result.
	 * @return a new {@link Promise} that will execute the running function.
	 */
	public <T> IPromise<T> newPromise(PromiseRunningFunction<T> runningFunction) {
		return newPromise(runningFunction, true);
	}

	/**
	 * Creates a new {@link Promise} that will execute the running function into the defined {@link ExecutorService}.
	 *
	 * @param runningFunction The promise's running function.
	 * @param startNow Indicates if the running function is to be scheduled immediately.
	 * @param <T> Type of the promise's result.
	 * @return a new {@link Promise} that will execute the running function.
	 */
	public <T> IPromise<T> newPromise(PromiseRunningFunction<T> runningFunction, boolean startNow) {
		final Promise<T> promise = new InternalPromise<>(runningFunction);
		if (startNow) {
			promise.start();
		}
		return promise;
	}

	protected class InternalPromise<T> extends Promise<T> {

		public InternalPromise(PromiseRunningFunction<T> runningFunction) {
			super(runningFunction, false);
		}

		@Override
		protected void doStart() {
			executorService.submit(super::doStart);
		}
	}
}
