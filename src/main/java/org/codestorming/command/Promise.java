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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Default implementation of the {@link IPromise} interface.
 *
 * @author Thaedrik [thaedrik@codestorming.org]
 * @since 2.1
 */
public class Promise<T> implements IPromise<T> {

	protected final PromiseRunningFunction<T> runningFunction;

	protected volatile PromiseCallback<Object, T> successCallback;

	protected volatile Callback<Throwable> errorCallback;

	protected final List<PromiseCallback<Object, Object>> callbacks = Collections.synchronizedList(new ArrayList<>());

	protected volatile IPromise<Object> next;

	protected volatile T result;

	protected volatile Throwable error;

	protected final AtomicBoolean done = new AtomicBoolean(false);

	private final AtomicBoolean started = new AtomicBoolean(false);

	/**
	 * Creates a new {@code Promise} with the defined function which is called immediately.
	 * <p>
	 * Equivalent to {@link #Promise(PromiseRunningFunction, boolean) new Promise(function, TRUE)}.
	 *
	 * @param runningFunction The running function of this promise.
	 */
	public Promise(PromiseRunningFunction<T> runningFunction) {
		this(runningFunction, true);
	}

	/**
	 * Creates a new {@code Promise} with the defined function.
	 *
	 * @param runningFunction The running function of this promise.
	 * @param startNow Indicates if the running function must be called immediatly.
	 */
	public Promise(PromiseRunningFunction<T> runningFunction, boolean startNow) {
		this.runningFunction = runningFunction;
		if (startNow) {
			start();
		}
	}

	@Override
	public <R, P> IPromise<T> then(PromiseCallback<R, P> successCallback) {
		setSuccessCallback(successCallback);
		return this;
	}

	@Override
	public <P> IPromise<T> then(Callback<P> successCallback) {
		return then((PromiseCallback<Void, P>) parameter -> {
			successCallback.call(parameter);
			return null;
		});
	}

	@Override
	public IPromise<T> katch(Callback<Throwable> errorCallback) {
		setErrorCallback(errorCallback);
		return this;
	}

	@Override
	public final void start() {
		if (started.compareAndSet(false, true)) {
			doStart();
		}
	}

	/**
	 * Calls the defined {@link PromiseRunningFunction} and notifies the error callback if necessary.
	 */
	@SuppressWarnings("unchecked")
	protected void doStart() {
		try {
			runningFunction.call(this::doResolve, this::doReject);
		} catch (Throwable t) {
			error = t;
		}

		// Notifies error callback if necessary
		notifyError();
	}

	protected void addPromise(IPromise<Object> promise) {
		if (!isError() && promise != null) {
			if (!callbacks.isEmpty()) {
				promise.then(callbacks.remove(0)).katch(this::doReject);
			}
			Promise<?> current = this;
			while (current.next != null) {
				current = (Promise<?>) current.next;
			}
			current.next = promise;
		}
	}

	/**
	 * Function called by the promise running function on success.
	 *
	 * @param result The result of the promise.
	 */
	protected void doResolve(T result) {
		this.result = result;
		done.set(true);
		notifySucess();
	}

	protected void notifySucess() {
		if (successCallback != null) {
			try {
				// Notifying callback of the obtained result
				addPromise(successCallback.call(result));
			} catch (Throwable ignored) {
				// Suppressed error
			}
		}
	}

	/**
	 * Function called by the promise running function on failure.
	 *
	 * @param error Promise rejection error.
	 */
	protected void doReject(Throwable error) {
		if (error == null) {
			this.error = new Exception(
					"Undefined error happened: This may happen if null is passed to IPromise.reject()");
		} else {
			this.error = error;
		}
	}

	protected boolean isError() {
		return error != null;
	}

	/**
	 * Defines the sucess-callback for this promise if not set or add a chain callback.
	 *
	 * @param successCallback The success callback for this promise or the next one.
	 */
	@SuppressWarnings("unchecked")
	protected void setSuccessCallback(PromiseCallback<?, ?> successCallback) {
		if (this.successCallback == null && successCallback != null) {
			this.successCallback = (PromiseCallback<Object, T>) successCallback;
			if (done.get()) {
				notifySucess();
			}
		} else if (this.successCallback != null && successCallback != null) {
			addChainCallback(successCallback);
		}
	}

	/**
	 * Adds a success-callback for the returned promises.
	 *
	 * @param chainCallback The callback to add.
	 */
	@SuppressWarnings("unchecked")
	protected void addChainCallback(PromiseCallback<?, ?> chainCallback) {
		IPromise<Object> current = next;
		while (current != null) {
			Promise<Object> p = (Promise<Object>) current;
			if (p.successCallback == null) {
				break;
			}
			current = p.next;
		}
		if (current != null) {
			current.then(chainCallback).katch(this::doReject);
		} else {
			callbacks.add((PromiseCallback<Object, Object>) chainCallback);
		}
	}

	/**
	 * Defines the error-callback and calls it if the error is already defined.
	 * <p>
	 * This method does nothing if the error-callback is already set.
	 *
	 * @param errorCallback The error callback to define for this promise.
	 */
	protected void setErrorCallback(Callback<Throwable> errorCallback) {
		if (this.errorCallback == null && errorCallback != null) {
			this.errorCallback = errorCallback;

			// Notifying the callback of the error
			// if the callback is defined after the error happened
			notifyError();
		}
	}

	/**
	 * Notifies the error callback if the error and the error-callback are defined.
	 */
	protected void notifyError() {
		if (isError() && errorCallback != null) {
			try {
				errorCallback.call(error);
			} catch (Throwable ignored) {
				// Suppressed error
			}
		}
	}
}
