/*******************************************************************************
 * Copyright (c) 2010 Robert "Unlogic" Olofsson (unlogic@unlogic.se).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-3.0-standalone.html
 ******************************************************************************/
package se.unlogic.standardutils.json;

import java.util.Collection;

import se.unlogic.standardutils.validation.ValidationError;

public class JsonUtils {

	private static final String CONTENT_TYPE = "application/json";

	public static JsonNode encode(Collection<ValidationError> validationErrors) {

		JsonArray jsonArray = new JsonArray();
		JsonObject jsonObject;
		for (ValidationError error : validationErrors) {
			jsonObject = new JsonObject();

			if (error.getFieldName() != null) {
				jsonObject.putField("field", new JsonLeaf(error.getFieldName()));
			}
			if (error.getValidationErrorType() != null) {
				jsonObject.putField("errorType", new JsonLeaf(error.getValidationErrorType().toString()));
			}

			if (error.getMessageKey() != null) {
				jsonObject.putField("messageKey", new JsonLeaf(error.getMessageKey()));
			}

			if (error.getDisplayName() != null) {
				jsonObject.putField("displayName", new JsonLeaf(error.getDisplayName()));
			}

			jsonArray.addNode(jsonObject);
		}
		return jsonArray;
	}

	public static String getContentType() {

		return CONTENT_TYPE;
	}
}
