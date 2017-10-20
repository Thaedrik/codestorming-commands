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

import java.util.concurrent.Callable;

/**
 * A {@code Command} is a runnable object with success, error and complete callbacks.
 * <p>
 * Each callback is called at a specific state of the command.
 * <ul>
 *     <li>onSuccess  : Called when the command ends without exception, with the result as parameter</li>
 *     <li>onError    : Called if the command throws an exception</li>
 *     <li>onComplete : Called when the command ends and is not canceled</li>
 * </ul>
 * <p>
 * <strong>ATTENTION: A command can be executed only once.</strong>
 *
 * @param <R> The type of the result value returned by the command.
 * @author Thaedrik [thaedrik@codestorming.org]
 * @since 2.0
 */
public abstract class Command<R> implements Runnable {

	protected Callback<R> onSuccess;

	protected Callback<Throwable> onError;

	protected Callback<Command<R>> onComplete;

	protected Command<?> successCommand;

	protected Command<?> errorCommand;

	protected Command<?> completeCommand;

	protected volatile boolean canceling;

	protected volatile boolean executed;

	/**
	 * Creates a command that will execute the specified callable.
	 *
	 * @param callable The operation to execute in the command.
	 * @param <T> The type of the result of the command.
	 * @return a command that will execute the specified callable.
	 */
	public static <T> Command<T> create(Callable<T> callable) {
		return new Command<T>() {
			@Override
			protected T execute() throws Exception {
				return callable.call();
			}
		};
	}

	@Override
	public final void run() {
		if (executed || canceling) {
			return;
		} // else

		executed = true;

		try {
			final R result = execute();
			onSuccessCall(result);
			runSuccessCommand();
		} catch (Throwable t) {
			onErrorCall(t);
			runErrorCommand();
		} finally {
			onCompleteCall();
			runCompleteCommand();
		}
	}

	private void onCompleteCall() {
		if (onComplete != null) {
			try {
				onComplete.call(this);
			} catch (Throwable ignored) {}
		}
	}

	private void onErrorCall(Throwable t) {
		if (onError != null) {
			try {
				onError.call(t);
			} catch (Throwable ignored) {}
		}
	}

	private void onSuccessCall(R result) {
		if (onSuccess != null) {
			try {
				onSuccess.call(result);
			} catch (Throwable ignored) {}
		}
	}

	/**
	 * Operation that will return this command's result.
	 *
	 * @return The result of this command.
	 * @throws Exception if an error occurs while running this command.
	 */
	protected abstract R execute() throws Exception;

	/**
	 * Cancels this command if it has not yet been executed.
	 */
	public final void cancel() {
		canceling = true;
		doCancel();
	}

	/**
	 * Implementors may override this method to add behavior to the cancel operation.
	 */
	protected void doCancel() {}

	/**
	 * Runs the success command if any.
	 * <p>
	 * All exceptions are ignored.
	 */
	protected void runSuccessCommand() {
		if (successCommand != null) {
			try {
				successCommand.run();
			} catch (Throwable ignored) {}
		}
	}

	/**
	 * Runs the error command if any.
	 * <p>
	 * All exceptions are ignored.
	 */
	protected void runErrorCommand() {
		if (errorCommand != null) {
			try {
				errorCommand.run();
			} catch (Throwable ignored) {}
		}
	}

	/**
	 * Runs the complete command if any.
	 * <p>
	 * All exceptions are ignored.
	 */
	protected void runCompleteCommand() {
		if (completeCommand != null) {
			try {
				completeCommand.run();
			} catch (Throwable ignored) {}
		}
	}

	/**
	 * Defines the on-success callback.
	 * <p>
	 * <em>NOTE: exceptions thrown by the on-success callback are <strong>ignored</strong></em>
	 *
	 * @param callback Callback called when this command succeeds
	 * @return this command.
	 */
	public Command<R> onSuccess(Callback<R> callback) {
		return onSuccess(callback, null);
	}

	/**
	 * Defines the on-success callback.
	 * <p>
	 * <em>NOTE: exceptions thrown by the on-success callback are <strong>ignored</strong></em>
	 *
	 * @param callback Callback called when this command succeeds
	 * @param command Command to execute after this command succeeded (nullable)
	 * @return this command.
	 */
	public Command<R> onSuccess(Callback<R> callback, Command<?> command) {
		this.onSuccess = callback;
		this.successCommand = command;
		return this;
	}

	/**
	 * Defines the on-error callback.
	 * <p>
	 * <em>NOTE: exceptions thrown by the on-error callback are <strong>ignored</strong></em>
	 *
	 * @param callback Callback called when this command fails
	 * @return this command.
	 */
	public Command<R> onError(Callback<Throwable> callback) {
		return onError(callback, null);
	}

	/**
	 * Defines the on-error callback.
	 * <p>
	 * <em>NOTE: exceptions thrown by the on-error callback are <strong>ignored</strong></em>
	 *
	 * @param callback Callback called when this command fails
	 * @param command Command to execute after this command failed (nullable)
	 * @return this command.
	 */
	public Command<R> onError(Callback<Throwable> callback, Command<?> command) {
		this.onError = callback;
		this.errorCommand = command;
		return this;
	}

	/**
	 * Defines the on-complete callback.
	 * <p>
	 * <em>NOTE: exceptions thrown by the on-complete callback are <strong>ignored</strong></em>
	 *
	 * @param callback Callback called when this command is complete and not canceled.
	 * @return this command.
	 */
	public Command<R> onComplete(Callback<Command<R>> callback) {
		return onComplete(callback, null);
	}

	/**
	 * Defines the on-complete callback.
	 * <p>
	 * <em>NOTE: exceptions thrown by the on-complete callback are <strong>ignored</strong></em>
	 *
	 * @param callback Callback called when this command is complete and not canceled.
	 * @param command Command to execute after this command is complete (nullable)
	 * @return this command.
	 */
	public Command<R> onComplete(Callback<Command<R>> callback, Command<?> command) {
		this.onComplete = callback;
		this.completeCommand = command;
		return this;
	}
}
