/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.swt.tests.junit;

import junit.framework.TestCase;

import org.eclipse.swt.accessibility.AccessibleControlEvent;
import org.eclipse.swt.widgets.Shell;

/**
 * Automated Test Suite for class org.eclipse.swt.accessibility.AccessibleControlEvent
 *
 * @see org.eclipse.swt.accessibility.AccessibleControlEvent
 */
public class Test_org_eclipse_swt_accessibility_AccessibleControlEvent extends TestCase {

@Override
protected void setUp() {
	shell = new Shell();
}

@Override
protected void tearDown() {
	shell.dispose();
}

public void test_ConstructorLjava_lang_Object() {
	// Object will typically be a widget.
	AccessibleControlEvent event = new AccessibleControlEvent(shell);
	assertNotNull(event);
	
	// Test with some other object also.
	event = new AccessibleControlEvent(new Integer(5));
	assertNotNull(event);
}

public void test_toString() {
	AccessibleControlEvent event = new AccessibleControlEvent(shell);
	assertNotNull(event.toString());
	assertTrue(event.toString().length() > 0);
}



/* custom */
public Shell shell;
}
