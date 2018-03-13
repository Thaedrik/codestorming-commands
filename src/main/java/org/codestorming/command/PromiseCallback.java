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
 * Callback for {@link IPromise promises}.
 *
 * @author Thaedrik [thaedrik@codestorming.org]
 * @since 2.1
 */
@FunctionalInterface
public interface PromiseCallback<R, T> {

	IPromise<R> call(T parameter);
}
