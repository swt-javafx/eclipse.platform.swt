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

import static org.junit.Assert.assertArrayEquals;

import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

/**
 * Automated Test Suite for class org.eclipse.swt.widgets.TabFolder
 *
 * @see org.eclipse.swt.widgets.TabFolder
 */
public class Test_org_eclipse_swt_widgets_TabFolder extends Test_org_eclipse_swt_widgets_Composite {

public Test_org_eclipse_swt_widgets_TabFolder(String name) {
	super(name);
}

@Override
protected void setUp() {
	super.setUp();
	makeCleanEnvironment();
}

@Override
public void test_ConstructorLorg_eclipse_swt_widgets_CompositeI() {
	try {
		new TabFolder(null, 0);
		fail("No exception thrown for parent == null");
	}
	catch (IllegalArgumentException e) {
	}
}

@Override
public void test_computeSizeIIZ() {
}

@Override
public void test_computeTrimIIII() {
}

@Override
public void test_getClientArea() {
}

public void test_getItemCount() {
	int number = 10;
	for (int i = 0; i<number ; i++){
		assertTrue(":a:" + i, tabFolder.getItemCount()==i);
	  	new TabItem(tabFolder, 0);
	}
}

public void test_getItemI() {
	int number = 15;
	TabItem[] items = new TabItem[number];
	for (int i = 0; i < number; i++) {
		items[i] = new TabItem(tabFolder, 0);
	}

	for (int i = 0; i < number; i++) {
		assertTrue(":a:" +String.valueOf(i), tabFolder.getItem(i).equals(items[i]));
	}
	try {
		tabFolder.getItem(number);
		fail("No exception thrown for illegal index argument");
	}
	catch (IllegalArgumentException e) {
	}

	try {
		tabFolder.getItem(number+1);
		fail("No exception thrown for illegal index argument");
	}
	catch (IllegalArgumentException e) {
	}

	try {
		tabFolder.getItem(-1);
		fail("No exception thrown for index == -1");
	}
	catch (IllegalArgumentException e) {
	}
}

public void test_getItems() {
	int number = 5;
	TabItem[] items = new TabItem[number];

	assertEquals(0, tabFolder.getItems().length);
	
	for (int i = 0; i<number ; i++){
	  	items[i] = new TabItem(tabFolder, 0);
	}
	assertArrayEquals(items, tabFolder.getItems());
	
	tabFolder.getItems()[0].dispose();
	assertArrayEquals(new TabItem[]{items[1], items[2], items[3], items[4]}, tabFolder.getItems());

	tabFolder.getItems()[3].dispose();
	assertArrayEquals(new TabItem[]{items[1], items[2], items[3]}, tabFolder.getItems());

	tabFolder.getItems()[1].dispose();
	assertArrayEquals(new TabItem[]{items[1], items[3]}, tabFolder.getItems());
}

public void test_getSelection() {
	int number = 10;
	TabItem[] tis = new TabItem[number];
	for (int i = 0; i<number ; i++){
	  	tis[i] = new TabItem(tabFolder, 0);
	}
	assertTrue(":a:", tabFolder.getSelection()[0] == tis[0]);	
	for (int i = 0; i<number ; i++){
		tabFolder.setSelection(i);
		assertTrue(":b:" + i, tabFolder.getSelection()[0]==tis[i]);
	}
}

public void test_getSelectionIndex() {
	int number = 15;
	TabItem[] items = new TabItem[number];
	for (int i = 0; i < number; i++)
		items[i] = new TabItem(tabFolder, 0);
		
	assertTrue(":a:", tabFolder.getSelectionIndex()==0);

	tabFolder.setSelection(new TabItem[]{items[2], items[number-1], items[10]});
	assertTrue(":b:", tabFolder.getSelectionIndex()==2);
	
	tabFolder.setSelection(items);
	assertTrue(":c:", tabFolder.getSelectionIndex()==0);
	
	tabFolder.setSelection(items[2]);
	assertTrue(":d:", tabFolder.getSelectionIndex()==2);
}

public void test_indexOfLorg_eclipse_swt_widgets_TabItem() {
	int number = 10;
	TabItem[] tis = new TabItem[number];
	for (int i = 0; i<number ; i++){
	  	tis[i] = new TabItem(tabFolder, 0);
	}
	for (int i = 0; i<number ; i++){
		assertTrue(":a:" + i, tabFolder.indexOf(tis[i])==i);
	}

	//
	makeCleanEnvironment();
	
	for (int i = 0; i<number ; i++){
	  	tis[i] = new TabItem(tabFolder, 0);
	}
	for (int i = 0; i<number ; i++){
		try {
			tabFolder.indexOf(null);
			fail("No exception thrown for tabItem == null");
		}
		catch (IllegalArgumentException e) {
		}
	}

	//
	makeCleanEnvironment();	
	number = 20;
	TabItem[] items = new TabItem[number];

	for (int i = 0; i < number; i++) {
		items[i] = new TabItem(tabFolder, 0);
		items[i].setText(String.valueOf(i));
	}

	//another tabFolder
	TabFolder tabFolder_2 = new TabFolder(shell, 0);
	TabItem[] items_2 = new TabItem[number];
	for (int i = 0; i < number; i++) {
		items_2[i] = new TabItem(tabFolder_2, 0);
		items_2[i].setText(String.valueOf(i));
	}

	for (int i = 0; i < number; i++) {
		assertTrue(":a:" + String.valueOf(i), tabFolder.indexOf(items_2[i])==-1);
	}

	//
	TabFolder tabFolder2 = new TabFolder(shell, SWT.NULL);
	TabItem tabItem = new TabItem(tabFolder2, SWT.NULL);
	
	assertTrue(":a:", tabFolder.indexOf(tabItem) == -1);
}

public void test_setSelectionEmpty() {
	if (SwtTestUtil.isGTK) {
		//TODO Fix GTK failure.
		if (SwtTestUtil.verbose) {
			System.out.println("Excluded test_setSelectionEmpty(org.eclipse.swt.tests.junit.Test_org_eclipse_swt_widgets_TabFolder)");
		}
		return;
	}
	int number = 10;
	for (int i = 0; i<number ; i++){
	  	new TabItem(tabFolder, 0);
	}
	for (int i = 0; i<number ; i++){
		tabFolder.setSelection(i);
		assertEquals(i, tabFolder.getSelectionIndex());
	}
	
	makeCleanEnvironment();
	tabFolder.setSelection(-1);
	assertEquals(0, tabFolder.getSelection().length);	
}

public void test_setSelectionI() {
	int number = 10;
	for (int i = 0; i<number ; i++){
	  	new TabItem(tabFolder, 0);
	}
	for (int i = 0; i<number ; i++){
		tabFolder.setSelection(i);
		assertEquals(i, tabFolder.getSelectionIndex());
	}

	//
	makeCleanEnvironment();
	
	for (int i = 0; i<number ; i++){
	  	new TabItem(tabFolder, 0);
	  	assertEquals("i=" + i, 0, tabFolder.getSelectionIndex());
	}

	//
	makeCleanEnvironment();
	
	number = 5;
	TabItem[] items = new TabItem[number];
	for (int i = 0; i < number; i++)
		items[i] = new TabItem(tabFolder, 0);
	try {
		tabFolder.setSelection((TabItem) null);
		fail("No exception thrown for selection == null");
	}
	catch (IllegalArgumentException e) {
	}
	finally {
		assertArrayEquals(new TabItem[]{items[0]}, tabFolder.getSelection());
	}
	
	try {
		tabFolder.setSelection((TabItem[]) null);
		fail("No exception thrown for selection == null");
	}
	catch (IllegalArgumentException e) {
	}
	finally {
		assertArrayEquals(new TabItem[]{items[0]}, tabFolder.getSelection());
	}

	//
	makeCleanEnvironment();
	
	items = new TabItem[number];
	for (int i = 0; i < number; i++)
		items[i] = new TabItem(tabFolder, 0);

	tabFolder.setSelection(0);
	assertArrayEquals(new TabItem[]{items[0]}, tabFolder.getSelection());

	tabFolder.setSelection(4);
	assertArrayEquals(new TabItem[]{items[4]}, tabFolder.getSelection());

	tabFolder.setSelection(2);
	assertArrayEquals(new TabItem[]{items[2]}, tabFolder.getSelection());	

	tabFolder.setSelection(1);
	assertArrayEquals(new TabItem[]{items[1]}, tabFolder.getSelection());

	tabFolder.setSelection(number + 1);
	assertArrayEquals(new TabItem[]{items[1]}, tabFolder.getSelection());	

//	tabFolder.setSelection(-1);
//	assertEquals(0, tabFolder.getSelection().length);	
	
	tabFolder.setSelection(3);
	assertArrayEquals(new TabItem[]{items[3]}, tabFolder.getSelection());

//	tabFolder.setSelection(-2);
//	assertEquals(0, tabFolder.getSelection().length);	

	//
	makeCleanEnvironment();
	
	for (int i = 0; i < number; i++)
		items[i] = new TabItem(tabFolder, 0);

	tabFolder.setSelection(items[0]);
	assertArrayEquals(new TabItem[]{items[0]}, tabFolder.getSelection());
	
	tabFolder.setSelection(new TabItem[] {items[0]});
	assertArrayEquals(new TabItem[] {items[0]}, tabFolder.getSelection());

	tabFolder.setSelection(new TabItem[] {items[3]});
	assertArrayEquals(new TabItem[] {items[3]}, tabFolder.getSelection());	

	tabFolder.setSelection(new TabItem[] {items[4]});
	assertArrayEquals(new TabItem[] {items[4]}, tabFolder.getSelection());

	tabFolder.setSelection(new TabItem[] {items[2]});
	assertArrayEquals(new TabItem[] {items[2]}, tabFolder.getSelection());	

	tabFolder.setSelection(new TabItem[] {items[1]});
	assertArrayEquals(new TabItem[] {items[1]}, tabFolder.getSelection());	

	//
	makeCleanEnvironment();
	
	for (int i = 0; i < number; i++)
		items[i] = new TabItem(tabFolder, 0);
	try {
		tabFolder.setSelection( new TabItem[]{items[0], null});
		tabFolder.setSelection( new TabItem[]{null});		
		fail("No exception thrown for selection == null");
	}
	catch (IllegalArgumentException e) {
	}
	finally {
		assertArrayEquals(new TabItem[]{items[0]}, tabFolder.getSelection());
	}
}

/* custom */
protected TabFolder tabFolder;

private void makeCleanEnvironment() {
// this method must be private or protected so the auto-gen tool keeps it
	tabFolder = new TabFolder(shell, 0);
	setWidget(tabFolder);
}

private void createTabFolder(Vector<String> events) {
	makeCleanEnvironment();
	for (int i = 0; i < 3; i++) {
		TabItem item = new TabItem(tabFolder, SWT.NONE);
		item.setText("TabItem &" + i);
		item.setToolTipText("TabItem ToolTip" + i);
		Text itemText = new Text(tabFolder, SWT.MULTI | SWT.BORDER);
		itemText.setText("\nText for TabItem " + i + "\n\n\n");
		item.setControl(itemText);
		hookExpectedEvents(item, getTestName(), events);
	}
	
//	tabFolder.setSelection(new TabItem[] {tabFolder.getItem(0)});
}

public void test_consistency_KeySelection() {
    Vector<String> events = new Vector<String>();
    createTabFolder(events);
    consistencyEvent(0, SWT.ARROW_RIGHT, 0, 0, ConsistencyUtility.KEY_PRESS, events, false);
}

public void test_consistency_MouseSelection() {
    Vector<String> events = new Vector<String>();
    createTabFolder(events);
    consistencyPrePackShell();
    consistencyEvent(tabFolder.getSize().x/2, 5, 1, 0, ConsistencyUtility.MOUSE_CLICK, events);
}

public void test_consistency_PgdwnSelection () {
    Vector<String> events = new Vector<String>();
    createTabFolder(events);
    consistencyEvent(0, SWT.CTRL, 0, SWT.PAGE_DOWN, ConsistencyUtility.DOUBLE_KEY_PRESS, events, false);
}

public void test_consistency_PgupSelection () {
    Vector<String> events = new Vector<String>();
    createTabFolder(events);
    tabFolder.setSelection(2);
    consistencyEvent(0, SWT.CTRL, 0, SWT.PAGE_UP, ConsistencyUtility.DOUBLE_KEY_PRESS, events, false);
}

public void test_consistency_MenuDetect () {
    Vector<String> events = new Vector<String>();
    createTabFolder(events);
    tabFolder.setSelection(1);
    consistencyEvent(50, 5, 3, 0, ConsistencyUtility.MOUSE_CLICK, events);
}

public void test_consistency_DragDetect () {
    Vector<String> events = new Vector<String>();
    createTabFolder(events);
    tabFolder.setSelection(1);
    consistencyEvent(50, 5, 70, 10, ConsistencyUtility.MOUSE_DRAG, events);
}
}
