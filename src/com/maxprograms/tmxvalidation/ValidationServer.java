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
package com.maxprograms.tmxvalidation;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Hashtable;

import javax.xml.parsers.ParserConfigurationException;

import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;

import com.maxprograms.converters.Constants;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class ValidationServer implements HttpHandler {

	private static final Logger LOGGER = System.getLogger(ValidationServer.class.getName());

	private HttpServer server;
	private Hashtable<String, String> running;
	private Hashtable<String, JSONObject> validationResults;

	public ValidationServer(int port) throws IOException {
		running = new Hashtable<>();
		validationResults = new Hashtable<>();
		server = HttpServer.create(new InetSocketAddress(port), 0);
		server.createContext("/ValidationServer", this);
		server.setExecutor(null); // creates a default executor
	}

	public static void main(String[] args) {
		String port = "8010";
		for (int i = 0; i < args.length; i++) {
			String arg = args[i];
			if (arg.equals("-version")) {
				LOGGER.log(Level.INFO, () -> "Version: " + Constants.VERSION + " Build: " + Constants.BUILD);
				return;
			}
			if (arg.equals("-port") && (i + 1) < args.length) {
				port = args[i + 1];
			}
		}
		try {
			ValidationServer instance = new ValidationServer(Integer.valueOf(port));
			instance.run();
		} catch (Exception e) {
			LOGGER.log(Level.ERROR, "Server error", e);
		}
	}

	private void run() {
		server.start();
		LOGGER.log(Level.INFO, "Validation server started");
	}

	@Override
	public void handle(HttpExchange t) throws IOException {
		InputStream is = t.getRequestBody();
		String request = readRequestBody(is);
		is.close();

		JSONObject json = null;
		String response = "";
		String command = "version";
		try {
			if (!request.isBlank()) {
				json = new JSONObject(request);
				command = json.getString("command");
			}
			if (command.equals("version")) {
				response = "{\"tool\":\"TMXValidator\", \"version\": \"" + Constants.VERSION + "\", \"build\": \""
						+ Constants.BUILD + "\"}";
			}
			if (json != null) {
				if (command.equals("validate")) {
					response = validate(json);
				}
				if (command.equals("status")) {
					response = getStatus(json);
				}
				if (command.equals("validationResult")) {
					response = getValidationResult(json);
				}
				t.getResponseHeaders().add("content-type", "application/json");
				t.sendResponseHeaders(200, response.length());
				try (BufferedReader reader = new BufferedReader(
						new InputStreamReader(new ByteArrayInputStream(response.getBytes())))) {
					try (OutputStream os = t.getResponseBody()) {
						String line;
						while ((line = reader.readLine()) != null) {
							os.write(line.getBytes());
						}
					}
				}
			}
		} catch (IOException e) {
			response = e.getMessage();
			t.sendResponseHeaders(500, response.length());
			try (OutputStream os = t.getResponseBody()) {
				os.write(response.getBytes());
			}
		}
	}

	private static String readRequestBody(InputStream is) throws IOException {
		StringBuilder request = new StringBuilder();
		try (BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
			String line;
			while ((line = rd.readLine()) != null) {
				request.append(line);
			}
		}
		return request.toString();
	}

	private String getStatus(JSONObject json) {
		String status = "unknown";
		try {
			String process = json.getString("process");
			status = running.get(process);
		} catch (JSONException je) {
			status = "error";
		}
		if (status == null) {
			status = "Error";
		}
		return "{\"status\": \"" + status + "\"}";
	}

	private String getValidationResult(JSONObject json) {
		JSONObject result = new JSONObject();
		try {
			String process = json.getString("process");
			result = validationResults.get(process);
			validationResults.remove(process);
		} catch (JSONException je) {
			LOGGER.log(Level.ERROR, je);
			result.put("valid", false);
			result.put("reason", "Error retrieving result from server");
		}
		return result.toString(2);
	}

	private String validate(JSONObject json) {
		String file = json.getString("file");

		String process = "" + System.currentTimeMillis();
		new Thread(new Runnable() {

			@Override
			public void run() {
				running.put(process, "running");
				TMXValidator validator = new TMXValidator();
				boolean valid;
				String reason = "";
				try {
					validator.validate(new File(file));
					valid = true;
				} catch (IOException | SAXException | ParserConfigurationException e) {
					valid = false;
					reason = e.getMessage();
				}
				JSONObject result = new JSONObject();
				result.put("valid", valid);
				if (valid) {
					result.put("comment", "Selected file is valid TMX");
				} else {
					result.put("reason", reason);
				}
				validationResults.put(process, result);
				if (running.get(process).equals(("running"))) {
					LOGGER.log(Level.INFO, "Validation completed");
					running.put(process, "completed");
				}
			}
		}).start();
		return "{\"process\":\"" + process + "\"}";
	}
}
