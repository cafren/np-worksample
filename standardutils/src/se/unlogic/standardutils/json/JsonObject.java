/*******************************************************************************
 * Copyright (c) 2010 Robert "Unlogic" Olofsson (unlogic@unlogic.se).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-3.0-standalone.html
 ******************************************************************************/
package se.unlogic.standardutils.json;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class JsonObject implements JsonNode {

	private static final long serialVersionUID = -4958874287623042122L;

	protected Map<String, JsonNode> fields;

	public JsonObject() {

		this.fields = new LinkedHashMap<String, JsonNode>();
	}

	public JsonObject(int initialCapacity) {

		this.fields = new LinkedHashMap<String, JsonNode>(initialCapacity);
	}

	@Override
	public String toString() {
	
		return toJson();
	}

	@Override
	public String toJson() {

		StringBuilder builder = new StringBuilder();

		toJson(builder);

		return builder.toString();
	}

	@Override
	public void toJson(StringBuilder stringBuilder) {

		stringBuilder.append("{");

		Iterator<Entry<String, JsonNode>> iterator = fields.entrySet().iterator();

		Entry<String, JsonNode> field;
		while (iterator.hasNext()) {
			field = iterator.next();

			stringBuilder.append("\"");
			stringBuilder.append(field.getKey());
			stringBuilder.append("\":");

			field.getValue().toJson(stringBuilder);

			if (iterator.hasNext()) {
				stringBuilder.append(",");
			}
		}

		stringBuilder.append("}");
	}

	public void putField(String key, JsonNode value) {

		if (value == null) {
			this.fields.put(key, new JsonLeaf(value));
		}
		else {
			this.fields.put(key, value);
		}
	}

	public void putField(String key, Object value) {

		this.fields.put(key, new JsonLeaf(value));
	}

	public void putField(String key, Object value, boolean appendQuotes) {

		this.fields.put(key, new JsonLeaf(value, appendQuotes));
	}

	public void removeField(String key) {

		this.fields.remove(key);
	}

	public void clearNodes() {

		this.fields.clear();
	}
	
	public int size() {

		return fields.size();
	}

	@Override
	public int hashCode() {

		final int prime = 31;
		int result = 1;
		result = prime * result + ((fields == null) ? 0 : fields.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {

		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		JsonObject other = (JsonObject) obj;
		if (fields == null) {
			if (other.fields != null) {
				return false;
			}
		} else if (!fields.equals(other.fields)) {
			return false;
		}
		return true;
	}
	
}
