package bsu.fpmi.chat.util;

import bsu.fpmi.chat.model.Message;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public final class MessageUtil {
	public static final String TOKEN = "token";
	public static final String MESSAGES = "messages";
	public static final String ID = "id";
	private static final String TN = "TN";
	private static final String EN = "EN";
	private static final String MESSAGE = "message";
	private static final String USERNAME = "userName";
	private static final String STATE = "state";
	private static final String SENDER = "sender";

	private MessageUtil() {
	}

	public static String getToken(int index) {
		Integer number = index * 8 + 11;
		return TN + number + EN;
	}

	public static int getIndex(String token) {
		return (Integer.valueOf(token.substring(2, token.length() - 2)) - 11) / 8;
	}

	public static JSONObject stringToJson(String data) throws ParseException {
		JSONParser parser = new JSONParser();
		return (JSONObject) parser.parse(data.trim());
	}

	public static Message jsonToMessage(JSONObject json) {
		Object id = json.get(ID);
		Object message = json.get(MESSAGE);
		Object userName = json.get(USERNAME);
		Object state = json.get(STATE);
		Object sender = json.get(SENDER);

		if (id != null && message != null && userName != null && state != null && sender != null) {
			return new Message((String) id, (String) userName, (String) message, (String) state, (String) sender);
		}
		return null;
	}
}
