package me.michaeldick.npr.model;

import com.google.gson.JsonObject;

public class Channel {
    String href;
    String id;
    String fullName;
    String description;
    String displayType;
    
    public Channel(JsonObject data) {
    	href = data.has("href") ? data.get("href").getAsString() : null;
    	JsonObject attributes = data.getAsJsonObject("attributes");
    	id = attributes.has("id") ? attributes.get("id").getAsString() : null;    	    	    	
		fullName = attributes.has("fullName") ? attributes.get("fullName").getAsString() : null;		
    	description = attributes.has("description") ? attributes.get("description").getAsString() : null;    	  
		displayType = attributes.has("displayType") ? attributes.get("displayType").getAsString() : null;
    }

	public String getHref() {
		return href;
	}

	public String getId() {
		return id;
	}

	public String getFullName() {
		return fullName;
	}

	public String getDescription() {
		return description;
	}

	public String getDisplayType() {
		return displayType;
	}
}
