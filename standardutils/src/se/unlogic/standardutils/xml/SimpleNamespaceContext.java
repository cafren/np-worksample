/*******************************************************************************
 * Copyright (c) 2010 Robert "Unlogic" Olofsson (unlogic@unlogic.se).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-3.0-standalone.html
 ******************************************************************************/
package se.unlogic.standardutils.xml;

import java.util.Collections;
import java.util.Iterator;

import javax.xml.namespace.NamespaceContext;


public class SimpleNamespaceContext implements NamespaceContext {

	private String prefix;
	private String URI;

	public SimpleNamespaceContext(String prefix, String URI) {

		super();
		this.prefix = prefix;
		this.URI = URI;
	}

	@Override
	public String getNamespaceURI(String prefix) {

		return URI;
	}

	@Override
	public String getPrefix(String namespaceURI) {

		return prefix;
	}

	@Override
	public Iterator<String> getPrefixes(String namespaceURI) {

		return Collections.singletonList(prefix).iterator();
	}
}
