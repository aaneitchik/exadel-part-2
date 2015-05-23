package bsu.fpmi.chat.model;

import org.json.simple.JSONObject;

public class Message {
	private String id;
	private String message;
	private String userName;
	private String state;
    private String sender;
	private static int counter = 0;
	
    Message() {
    	++counter;
    	userName = "User " + counter;
    }
    	
    public Message(String id, String userName, String message, String state, String sender) {
    	this.id = id;
    	this.userName = userName;
    	this.message = message;
    	this.state = state;
        this.sender = sender;
    }
	
    public Message(JSONObject json) {
    	this.id = (String)json.get("id");
        this.userName = (String)json.get("userName");
        this.message = (String)json.get("message");
        this.state = (String)json.get("state");
        this.sender = (String)json.get("sender");
    }
    	
    public String getId() {
    	return id;
    }

    public int getIntId() {
        return Integer.parseInt(this.id);
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

    public String getSender() {
        return sender;
    }
    	
    public void setId(int id) {
    	this.id = String.valueOf(id);
    }
    	
    public void setState(String state) {
    	this.state = state;
    }
        
    public void setMessage(String message) {
    	this.message = message;
    }
}