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
 * Command for which a result can be retrieved after its execution.
 *
 * @param <T> The type of the result.
 * @author Thaedrik [thaedrik@codestorming.org]
 */
public interface CommandWithResult<T> extends Runnable {

	/**
	 * Returns the result of this command.
	 * <p>
	 * If this command is executed in an other thread, the result may be not initialized yet. In this case, the result
	 * should be accessed via a callback defined with {@link #setCallback(Runnable)}.
	 *
	 * @return the result of this command.
	 */
	T getResult();

	/**
	 * Define a callback that will be invoked when the command is done.
	 * <p>
	 * <em>Only one callback can be defined, this method is not complementary with {@link #setCallback(Callback)}</em>.
	 *
	 * @param callback The callback to invoke at the end of the command.
	 */
	void setCallback(Runnable callback);

	/**
	 * Define a callback that will be invoked when the command is done and take the result of the command as parameter.
	 * <p>
	 * <em>Only one callback can be defined, this method is not complementary with {@link #setCallback(Runnable)}</em>.
	 *
	 * @param callback The callback to invoke at the end of the command.
	 */
	void setCallback(Callback<T> callback);
}
