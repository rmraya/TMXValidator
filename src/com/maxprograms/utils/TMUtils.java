/*******************************************************************************
 * Copyright (c) 2003, 2018 Maxprograms.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-v10.html
 *
 * Contributors:
 *     Maxprograms - initial API and implementation
 *******************************************************************************/ 
package com.maxprograms.utils;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import com.maxprograms.languages.RegistryParser;
import com.maxprograms.xml.Element;
import com.maxprograms.xml.TextNode;
import com.maxprograms.xml.XMLNode;

public class TMUtils {

	private static RegistryParser registry;

	public static String pureText(Element seg) {
		List<XMLNode> l = seg.getContent();
		Iterator<XMLNode> i = l.iterator();
		String text = "";
		while (i.hasNext()) {
			XMLNode o = i.next();
			if (o.getNodeType() == XMLNode.TEXT_NODE) {
				text = text + ((TextNode) o).getText();
			} else if (o.getNodeType() == XMLNode.ELEMENT_NODE) {
				String type = ((Element) o).getName();
				// discard all inline elements
				// except <mrk> and <hi>
				if (type.equals("sub") || type.equals("hi")) {
					Element e = (Element) o;
					text = text + pureText(e);
				}
			}
		}
		i = null;
		l = null;
		return text;
	}

	public static String normalizeLang(String lang) throws IOException {
		if (registry == null) {
			registry = new RegistryParser();
		}
		if (lang == null) {
			return null;
		}
		if (lang.length() == 2 || lang.length() == 3) {
			if (registry.getTagDescription(lang).length() > 0) {
				return lang.toLowerCase();
			}
			return null;
		}
		lang = lang.replaceAll("_", "-");
		String[] parts = lang.split("-");

		if (parts.length == 2) {
			if (parts[1].length() == 2) {
				// has country code
				String code = lang.substring(0, 2).toLowerCase() + "-" + lang.substring(3).toUpperCase();
				if (registry.getTagDescription(code).length() > 0) {
					return code;
				}
				return null;
			}
			// may have a script
			String code = lang.substring(0, 2).toLowerCase() + "-" + lang.substring(3, 4).toUpperCase()
					+ lang.substring(4).toLowerCase();
			if (registry.getTagDescription(code).length() > 0) {
				return code;
			}
			return null;
		}
		// check if its a valid thing with more than 2 parts
		if (registry.getTagDescription(lang).length() > 0) {
			return lang;
		}
		return null;
	}

	public static String createId() {
		Date now = new Date();
		long lng = now.getTime();
		// wait until we are in the next millisecond
		// before leaving to ensure uniqueness
		Date next = new Date();
		while (next.getTime() == lng) {
			next = null;
			next = new Date();
		}
		return "" + lng;
	}

	public static String TMXDate() {
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		String sec = (calendar.get(Calendar.SECOND) < 10 ? "0" : "") + calendar.get(Calendar.SECOND);
		String min = (calendar.get(Calendar.MINUTE) < 10 ? "0" : "") + calendar.get(Calendar.MINUTE);
		String hour = (calendar.get(Calendar.HOUR_OF_DAY) < 10 ? "0" : "") + calendar.get(Calendar.HOUR_OF_DAY);
		String mday = (calendar.get(Calendar.DATE) < 10 ? "0" : "") + calendar.get(Calendar.DATE);
		String mon = (calendar.get(Calendar.MONTH) < 9 ? "0" : "") + (calendar.get(Calendar.MONTH) + 1);
		String longyear = "" + calendar.get(Calendar.YEAR);

		return longyear + mon + mday + "T" + hour + min + sec + "Z";
	}

	public static long getGMTtime(String TMXDate) {
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		try {
			int second = Integer.parseInt(TMXDate.substring(13, 15));
			int minute = Integer.parseInt(TMXDate.substring(11, 13));
			int hour = Integer.parseInt(TMXDate.substring(9, 11));
			int date = Integer.parseInt(TMXDate.substring(6, 8));
			int month = Integer.parseInt(TMXDate.substring(4, 6)) - 1;
			int year = Integer.parseInt(TMXDate.substring(0, 4));
			calendar.set(year, month, date, hour, minute, second);
			return calendar.getTimeInMillis();
		} catch (Exception e) {
			// e.printStackTrace();
			return 0l;
		}
	}
}
