/*******************************************************************************
 * Copyright (c) 2005-2023 Maxprograms.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-v10.html
 *
 * Contributors:
 *     Maxprograms - initial API and implementation
 *******************************************************************************/ 
package com.maxprograms.tmxvalidation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.maxprograms.xml.CustomErrorHandler;
import com.maxprograms.xml.SAXBuilder;

public class TMXValidator {

	private static final Logger LOGGER = System.getLogger(TMXValidator.class.getName());
	
	private SAXBuilder builder;
	private TMXValidatingHandler handler;
	private TMXResolver resolver;
	
	public TMXValidator() {
		handler = new TMXValidatingHandler();
		resolver = new TMXResolver();
		builder = new SAXBuilder();
		builder.setValidating(true);
		builder.setContentHandler(handler);
		builder.setEntityResolver(resolver);
		builder.setErrorHandler(new CustomErrorHandler());
	}

	public void validate(File file) throws IOException, SAXException, ParserConfigurationException {
		try {
			builder.build(file);
		} catch (SAXException sax) {
			if (sax.getMessage().equals(TMXValidatingHandler.RELOAD)) {
				// TMX DTD was not declared
				String version = handler.getVersion();
				File copy = File.createTempFile("copy", ".tmx"); //$NON-NLS-1$ //$NON-NLS-2$
				copy.deleteOnExit();
				copyFile(file, copy, version);
				builder.build(copy);
			} else {
				throw sax;
			}
		}
	}

	private static void copyFile(File file, File copy, String version) throws IOException, SAXException, ParserConfigurationException {
		String systemID = "tmx14.dtd"; //$NON-NLS-1$
		if (version.equals("1.3")) { //$NON-NLS-1$
			systemID = "tmx13.dtd"; //$NON-NLS-1$
		} else if (version.equals("1.2")) { //$NON-NLS-1$
			systemID = "tmx12.dtd"; //$NON-NLS-1$
		} else if (version.equals("1.1")) { //$NON-NLS-1$
			systemID = "tmx11.dtd"; //$NON-NLS-1$
		}
		try (FileOutputStream out = new FileOutputStream(copy)) {
			writeString(out, "<?xml version=\"1.0\" ?>\n"); //$NON-NLS-1$
			writeString(out, "<!DOCTYPE tmx SYSTEM \"" + systemID + "\">\n"); //$NON-NLS-1$ //$NON-NLS-2$
			SAXBuilder copyBuilder = new SAXBuilder();
			TMXCopyHandler copyHandler = new TMXCopyHandler(out);
			copyBuilder.setContentHandler(copyHandler);
			copyBuilder.build(file);
		}
	}

	private static void writeString(FileOutputStream out, String string) throws IOException {
		out.write(string.getBytes(StandardCharsets.UTF_8));
	}
	
	public static void main(String[] args) {
		String[] commandLine = fixPath(args);
		String tmx = ""; //$NON-NLS-1$
		for (int i = 0; i < commandLine.length; i++) {
			String arg = commandLine[i];
			if (arg.equals("-version")) { //$NON-NLS-1$
				MessageFormat mf = new MessageFormat(Messages.getString("TMXValidator.0") ); //$NON-NLS-1$
				LOGGER.log(Level.INFO, () -> mf.format(new String[] {Constants.VERSION, Constants.BUILD}));
				return;
			}
			if (arg.equals("-help")) { //$NON-NLS-1$
				help();
				return;
			}
			if (arg.equals("-tmx") && (i + 1) < commandLine.length) { //$NON-NLS-1$
				tmx = commandLine[i + 1];
			}
		}
		if (tmx.isEmpty()) {
			help();
			return;
		}
		try {
			TMXValidator validator = new TMXValidator();
			validator.validate(new File(tmx));
			LOGGER.log(Level.INFO, Messages.getString("TMXValidator.1")); //$NON-NLS-1$
		} catch (IOException | SAXException | ParserConfigurationException e) {
			LOGGER.log(Level.ERROR, e.getMessage());
		}
	}
	
	private static void help() {
		String launcher = "tmxvalidator.sh"; //$NON-NLS-1$
		if (System.getProperty("file.separator").equals("\\")) { //$NON-NLS-1$ //$NON-NLS-2$
			launcher = "tmxvalidator.bat"; //$NON-NLS-1$
		}
		MessageFormat mf = new MessageFormat(Messages.getString("TMXValidator.2")); //$NON-NLS-1$
		System.out.println(mf.format(new String[] {launcher}));
	}

	private static String[] fixPath(String[] args) {
		List<String> result = new ArrayList<>();
		StringBuilder current = new StringBuilder();
		for (int i = 0; i < args.length; i++) {
			String arg = args[i];
			if (arg.startsWith("-")) { //$NON-NLS-1$
				if (current.length() > 0) {
					result.add(current.toString().trim());
					current = new StringBuilder();
				}
				result.add(arg);
			} else {
				current.append(' ');
				current.append(arg);
			}
		}
		if (current.length() > 0) {
			result.add(current.toString().trim());
		}
		return result.toArray(new String[result.size()]);
	}
}
