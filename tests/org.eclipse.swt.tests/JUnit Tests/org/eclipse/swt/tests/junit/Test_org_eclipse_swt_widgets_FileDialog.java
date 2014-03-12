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
package org.eclipse.swt.tests.junit;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;

/**
 * Automated Test Suite for class org.eclipse.swt.widgets.FileDialog
 *
 * @see org.eclipse.swt.widgets.FileDialog
 */
public class Test_org_eclipse_swt_widgets_FileDialog extends Test_org_eclipse_swt_widgets_Dialog {

public Test_org_eclipse_swt_widgets_FileDialog(String name) {
	super(name);
}

@Override
protected void setUp() {
	super.setUp();
	fileDialog = new FileDialog(shell, SWT.NULL);
	setDialog(fileDialog);
}

public void test_ConstructorLorg_eclipse_swt_widgets_Shell() {
	// Test FileDialog(Shell)
	new FileDialog(shell);
	try {
		new FileDialog(null);
		fail("No exception thrown for parent == null");
	}
	catch (IllegalArgumentException e) {
	}
}

public void test_ConstructorLorg_eclipse_swt_widgets_ShellI() {
	// Test FileDialog(Shell, int)
	FileDialog fd;
	fd = new FileDialog(shell, SWT.NULL);
	int style = fd.getStyle();
	style &= ~SWT.LEFT_TO_RIGHT;
	style &= ~SWT.RIGHT_TO_LEFT;
	assertTrue(style==SWT.APPLICATION_MODAL);
	
	fd = new FileDialog(shell, SWT.APPLICATION_MODAL);
	style = fd.getStyle();
	style &= ~SWT.LEFT_TO_RIGHT;
	style &= ~SWT.RIGHT_TO_LEFT;
	assertTrue(style==SWT.APPLICATION_MODAL);
	
	fd = new FileDialog(shell, SWT.PRIMARY_MODAL);
	style = fd.getStyle();
	style &= ~SWT.LEFT_TO_RIGHT;
	style &= ~SWT.RIGHT_TO_LEFT;
	assertTrue(style==SWT.PRIMARY_MODAL);
	
	fd = new FileDialog(shell, SWT.SYSTEM_MODAL);
	style = fd.getStyle();
	style &= ~SWT.LEFT_TO_RIGHT;
	style &= ~SWT.RIGHT_TO_LEFT;
	assertTrue(style==SWT.SYSTEM_MODAL);
}

public void test_getFileNames() {
	String[] names = fileDialog.getFileNames();
	assertTrue(names.length==0);
}

public void test_open() {
	if (SwtTestUtil.fTestDialogOpen)
		fileDialog.open();
}

public void test_setFileNameLjava_lang_String() {
	fileDialog.setFileName("");
	String name = fileDialog.getFileName();
	assertTrue(name.equals(""));
	fileDialog.setFileName(null);
	name = fileDialog.getFileName();
	assertTrue(name==null);
	fileDialog.setFileName("somefile.test");
	name = fileDialog.getFileName();
	assertTrue(name.equals("somefile.test"));
}

public void test_setFilterExtensions$Ljava_lang_String() {
	fileDialog.setFilterExtensions(new String[] {"txt","java"});
	String filters[] = fileDialog.getFilterExtensions();
	assertTrue(filters.length == 2);
	assertTrue(filters[0].equals("txt"));
	assertTrue(filters[1].equals("java"));
	fileDialog.setFilterExtensions(new String[] {""});
	filters = fileDialog.getFilterExtensions();
	assertTrue(filters.length == 1);
	fileDialog.setFilterExtensions(null);
	filters = fileDialog.getFilterExtensions();
	assertTrue(filters==null);
}

public void test_setFilterNames$Ljava_lang_String() {
	fileDialog.setFilterNames(new String[] {"a.txt","b.java"});
	String filters[] = fileDialog.getFilterNames();
	assertTrue(filters.length == 2);
	assertTrue(filters[0].equals("a.txt"));
	assertTrue(filters[1].equals("b.java"));
	fileDialog.setFilterNames(new String[] {""});
	filters = fileDialog.getFilterNames();
	assertTrue(filters.length == 1);
	fileDialog.setFilterNames(null);
	filters = fileDialog.getFilterNames();
	assertTrue(filters==null);
}

public void test_setFilterPathLjava_lang_String() {
	assertTrue(":1:", fileDialog.getFilterPath() == "");
	String testStr = "./*";
	fileDialog.setFilterPath(testStr);
	assertTrue(":2:", fileDialog.getFilterPath().equals(testStr));
	fileDialog.setFilterPath("");
	assertTrue(":3:", fileDialog.getFilterPath().equals(""));
	fileDialog.setFilterPath(null);
	assertTrue(":4:", fileDialog.getFilterPath() == null);
}
/* custom */
FileDialog fileDialog;
}
