/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.swt.examples.controlexample;

import java.util.concurrent.CountDownLatch;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;


public class CustomControlExample extends ControlExample {

	/**
	 * Creates an instance of a CustomControlExample embedded
	 * inside the supplied parent Composite.
	 * 
	 * @param parent the container of the example
	 */
	public CustomControlExample(Composite parent) {
		super (parent);
	}
	
	/**
	 * Answers the set of example Tabs
	 */
	@Override
	Tab[] createTabs() {
		return new Tab [] {
			new CComboTab (this),
			new CLabelTab (this),
			new CTabFolderTab (this),
			new SashFormTab (this),
			new StyledTextTab (this),
		};
	}
	
	/**
	 * Invokes as a standalone program.
	 */
	public static void main(String[] args) {
		final Display display = new Display();
		final CustomControlExample[] instance = new CustomControlExample[1];
		final CountDownLatch latch = new CountDownLatch(1);
		display.syncExec(new Runnable() {
			@Override
			public void run() {
				Shell shell = new Shell(display);
				shell.setLayout(new FillLayout());
				instance[0] = new CustomControlExample(shell);
				shell.setText(getResourceString("custom.window.title"));
				setShellSize(instance[0], shell);
				shell.addDisposeListener(new DisposeListener() {
					@Override
					public void widgetDisposed(DisposeEvent e) {
						latch.countDown();
					}
				});
				shell.open();
			}
		});
		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		instance[0].dispose();
		display.dispose();
	}
}
