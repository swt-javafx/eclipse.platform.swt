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


import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.TypedEvent;
import org.eclipse.swt.widgets.Event;

/**
 * Automated Test Suite for class org.eclipse.swt.events.PaintEvent
 *
 * @see org.eclipse.swt.events.PaintEvent
 */
public class Test_org_eclipse_swt_events_PaintEvent extends Test_org_eclipse_swt_events_TypedEvent {

/* custom */
@Override
protected TypedEvent newTypedEvent(Event event) {
	return new PaintEvent(event);
}
}
