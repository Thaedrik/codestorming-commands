/*
 * Copyright (c) 2012-2017 Codestorming.org
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
 * Abstract implementation of the {@link CommandWithResult} interface.
 *
 * @param <T> The type of the result.
 * @author Thaedrik [thaedrik@codestorming.org]
 */
public abstract class ACommandWithResult<T> implements CommandWithResult<T> {

	private T result;

	private Runnable callback;

	private Callback<T> callback2;

	@Override
	public T getResult() {
		return result;
	}

	@Override
	public final void run() {
		execute();
		if (callback != null) {
			callback.run();
		} else if (callback2 != null) {
			callback2.call(getResult());
		}
	}

	/**
	 * Called when the {@link #run()} method is invoked.
	 */
	protected abstract void execute();

	@Override
	public final void setCallback(Runnable callback) {
		this.callback = callback;
		this.callback2 = null;
	}

	@Override
	public final void setCallback(Callback<T> callback) {
		this.callback2 = callback;
		this.callback = null;
	}

	/**
	 * Set the result of this command.
	 *
	 * @param result The result to set.
	 */
	protected final void setResult(T result) {
		this.result = result;
	}
}
