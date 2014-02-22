/*******************************************************************************
 * Copyright (c) 2010, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.swt.browser;

import javafx.scene.web.WebView;

import org.eclipse.swt.widgets.Composite;


class BrowserFactory {

	WebBrowser createWebBrowser(int style) {
		return new WebBrowser() {
			WebView webView;
			
			@Override
			public boolean back() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public void create(Composite parent, int style) {
				webView = new WebView();
				parent.controlContainer.getChildren().add(webView);
			}

			@Override
			public boolean execute(String script) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean forward() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public String getBrowserType() {
				return "javafx";
			}

			@Override
			public String getText() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public String getUrl() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public boolean isBackEnabled() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean isForwardEnabled() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public void refresh() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public boolean setText(String html, boolean trusted) {
				webView.getEngine().loadContent(html);
				return true;
			}

			@Override
			public boolean setUrl(String url, String postData, String[] headers) {
				webView.getEngine().load(url);
				return true;
			}

			@Override
			public void stop() {
				// TODO Auto-generated method stub
				
			}
			
		};
	}

}
