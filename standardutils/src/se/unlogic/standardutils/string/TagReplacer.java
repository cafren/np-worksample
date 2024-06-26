/*******************************************************************************
 * Copyright (c) 2010 Robert "Unlogic" Olofsson (unlogic@unlogic.se).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-3.0-standalone.html
 ******************************************************************************/
package se.unlogic.standardutils.string;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import se.unlogic.standardutils.html.HTMLUtils;


public class TagReplacer {

	protected List<TagSource> tagSources;

	public TagReplacer() {

		tagSources = new ArrayList<TagSource>();
	}

	public TagReplacer(int initialCapacity) {

		tagSources = new ArrayList<TagSource>(initialCapacity);
	}

	
	public TagReplacer(List<TagSource> tagSources) {
		super();
		this.tagSources = new ArrayList<TagSource>(tagSources);
	}

	public TagReplacer(TagSource... tagSources) {
		super();
		this.tagSources = new ArrayList<TagSource>(Arrays.asList(tagSources));
	}

	public boolean addTagSource(TagSource o) {

		if(tagSources == null){

			tagSources = new ArrayList<TagSource>();
		}

		return tagSources.add(o);
	}

	public boolean removeTagSource(TagSource o) {

		if(tagSources == null){

			return false;
		}

		return tagSources.remove(o);
	}

	public String replace(String source){
		
		return replace(source, false);
	}
	
	public String replace(String source, boolean escapeHTML){

		for(TagSource tagSource : tagSources){

			for(String tag : tagSource.getTags()){

				if(source.contains(tag)){

					String value = tagSource.getTagValue(tag);

					if(value == null){

						value = "";
						
					} else {
						
						if (escapeHTML) {
							
							value = HTMLUtils.escapeHTML(value);
						}
						
					}

					source = source.replace(tag, value);
				}
			}
		}

		return source;
	}

	public List<String> getAvailableTags(){

		List<String> tagList = new ArrayList<String>();

		for(TagSource tagSource : tagSources){

			tagList.addAll(tagSource.getTags());
		}

		return tagList;
	}

	public void addTagSources(List<TagSource> tagSources) {

		this.tagSources.addAll(tagSources);
	}
}
