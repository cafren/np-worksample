package se.unlogic.standardutils.string;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import se.unlogic.standardutils.string.TagReplacer;
import se.unlogic.standardutils.string.TagSource;

public class URLEncodingTagReplacer extends TagReplacer {

	private static final Charset CHARSET = StandardCharsets.UTF_8;

	@Override
	public String replace(String source) {

		try {

			for (TagSource tagSource : tagSources) {

				for (String tag : tagSource.getTags()) {

					if (source.contains(tag)) {

						String value = tagSource.getTagValue(tag);

						if (value == null) {

							value = "";

						} else {

							value = URLEncoder.encode(value, CHARSET.toString());
						}

						source = source.replace(tag, value);
					}
				}
			}

			return source;

		} catch (UnsupportedEncodingException e) {

			throw new RuntimeException(e);
		}
	}

}
