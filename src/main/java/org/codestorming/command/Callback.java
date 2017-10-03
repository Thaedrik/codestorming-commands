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
 * Defines a callback with a parameter.
 *
 * @param <T> The type of the parameter that will be passed to the call method.
 * @author Thaedrik [thaedrik@codestorming.org]
 */
public interface Callback<T> {

	void call(T parameter);
}
