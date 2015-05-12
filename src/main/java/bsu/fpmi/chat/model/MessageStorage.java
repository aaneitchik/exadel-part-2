package bsu.fpmi.chat.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.simple.JSONObject;

public final class MessageStorage {
	private static final List<Message> INSTANCE = Collections.synchronizedList(new ArrayList<Message>());

	private MessageStorage() {
	}

	public static void addMessage(Message message) {
		INSTANCE.add(message);
	}

	public static int getSize() {
		return INSTANCE.size();
	}

	public static List<JSONObject> getMessages() {
		List<JSONObject> messageList = new ArrayList<JSONObject>();
		for(Message m : INSTANCE) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("id", m.getId());
			jsonObject.put("userName", m.getUserName());
			jsonObject.put("message", m.getMessage());
			jsonObject.put("state", m.getState());
			jsonObject.put("sender", m.getSender());
			messageList.add(jsonObject);
		}
		return messageList;
	}

	public static Message getMessageById(String id) {
		for (Message message : INSTANCE) {
			if (message.getId().equals(id)) {
				return message;
			}
		}
		return null;
	}

}
