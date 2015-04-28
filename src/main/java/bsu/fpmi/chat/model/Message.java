package bsu.fpmi.chat.model;

import org.json.simple.JSONObject;

public class Message {
	private String id;
	private String message;
	private String userName;
	private String state;
	private static int counter = 0;
	
    Message() {
    	++counter;
    	userName = "User " + counter;
    }
    	
    public Message(String id, String userName, String message, String state) {
    	this.id = id;
    	this.userName = userName;
    	this.message = message;
    	this.state = state;
    }
	
    public Message(JSONObject json) {
    	this.id = (String)json.get("id");
        this.userName = (String)json.get("userName");
        this.message = (String)json.get("message");
        this.state = (String)json.get("state");
    }
    	
    public String getId() {
    	return id;
    }
    
    public String getUserName() {
    	return userName;
    }
    	
    public String getState() {
    	return state;
    }
    	
    public String getMessage() {
    	return message;
    }
    	
    public void setId(int id) {
    	this.id = String.valueOf(id);
    }

    public void setUserName(String userName) {
    	this.userName = userName;
   	}
    	
    public void setState(String state) {
    	this.state = state;
    }
        
    public void setMessage(String message) {
    	this.message = message;
    }
}