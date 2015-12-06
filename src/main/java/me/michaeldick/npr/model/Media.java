package me.michaeldick.npr.model;

import java.util.HashMap;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class Media {
	public enum AggregationDocumentType { audio, intro, aggregation, stationId, donate, music, sponsorship, featureCardInformational, featureCardNotification, featureCardExternalLink, featureCardPromotion };
	
	AggregationDocumentType type;
	
	String uid;
    String title;
    boolean skippable;
    String slug;
    String provider;
    String program;
    int duration;
    String date;
    String description;
    Rating rating;
    HashMap<String,String> webLinks;
    HashMap<String,String> audioLinks;
    HashMap<String,String> imageLinks;
    HashMap<String,String> recommendations;
	String affiliation;
    String affiliationId;
	String affiliationMetaHref;
    boolean following;    
	
    public Media(JsonObject data) {    	    	
    	JsonObject attributes = data.getAsJsonObject("attributes");
    	type = attributes.has("type") ? AggregationDocumentType.valueOf(attributes.get("type").getAsString()) : AggregationDocumentType.audio;    	    	    	
		uid = attributes.has("uid") ? attributes.get("uid").getAsString() : null;		
    	title = attributes.has("title") ? attributes.get("title").getAsString() : null;    	  
		skippable = attributes.has("skippable") ? attributes.get("skippable").getAsBoolean() : true;
		slug = attributes.has("slug") ? attributes.get("slug").getAsString() : null;
    	provider = attributes.has("provider") ? attributes.get("provider").getAsString() : null;
    	program = attributes.has("program") ? attributes.get("program").getAsString() : null;
    	duration = attributes.has("duration") ? attributes.get("duration").getAsInt() : 0;
    	date = attributes.has("date") ? attributes.get("date").getAsString() : null;
		rating = attributes.has("rating") ? new Rating(attributes.get("rating").getAsJsonObject()) : null;		
		description = attributes.has("description") ? attributes.get("description").getAsString() : null;		    	
    	affiliation = attributes.has("affiliation") ? attributes.get("affiliation").getAsString() : null;
		
    	if(attributes.has("affiliationMeta")) {
    		JsonObject affiliationMeta = attributes.getAsJsonObject("affiliationMeta");        	
    		affiliationId = affiliationMeta.has("id") ? affiliationMeta.get("id").getAsString() : null;
    		affiliationMetaHref = affiliationMeta.has("href") ? affiliationMeta.get("href").getAsString() : null;
    		following = affiliationMeta.has("following") ? affiliationMeta.get("following").getAsBoolean() : false;
    	}    	      	    	     	
    	
    	JsonObject links = data.getAsJsonObject("links");
    	if(links.has("web")) {
    		webLinks = new HashMap<String, String>();
    		for(JsonElement el : links.get("web").getAsJsonArray()) {    			
    			webLinks.put(el.getAsJsonObject().get("content-type").getAsString(), el.getAsJsonObject().get("href").getAsString());
    		}    		    	
    	}  
    	if(links.has("image")) {  
    		imageLinks = new HashMap<String, String>();
    		for(JsonElement el : links.get("image").getAsJsonArray()) { 
    			if(el.getAsJsonObject().has("rel") && el.getAsJsonObject().has("href"))
    			imageLinks.put(el.getAsJsonObject().get("rel").getAsString(), el.getAsJsonObject().get("href").getAsString());
    		}    		    	
    	} 
    	if(links.has("audio")) { 
    		audioLinks = new HashMap<String, String>();
    		for(JsonElement el : links.get("audio").getAsJsonArray()) {    			
    			audioLinks.put(el.getAsJsonObject().get("content-type").getAsString(), el.getAsJsonObject().get("href").getAsString());
    		}    		    	
    	} 
    	if(links.has("recommendations")) {    	
    		recommendations = new HashMap<String, String>();
    		for(JsonElement el : links.get("recommendations").getAsJsonArray()) {    			
    			recommendations.put(el.getAsJsonObject().get("content-type").getAsString(), el.getAsJsonObject().get("href").getAsString());
    		}    		    	
    	}        	    	    
    }

	public AggregationDocumentType getType() {
		return type;
	}

	public String getUid() {
		return uid;
	}

	public String getTitle() {
		return title;
	}

	public boolean isSkippable() {
		return skippable;
	}

	public String getSlug() {
		return slug;
	}

	public String getProvider() {
		return provider;
	}

	public String getProgram() {
		return program;
	}

	public int getDuration() {
		return duration;
	}

	public String getDate() {
		return date;
	}

	public String getDescription() {
		return description;
	}

	public Rating getRating() {
		return rating;
	}

	public HashMap<String, String> getWebLinks() {
		return webLinks;
	}

	public HashMap<String, String> getAudioLinks() {
		return audioLinks;
	}

	public HashMap<String, String> getImageLinks() {
		return imageLinks;
	}

	public HashMap<String, String> getRecommendations() {
		return recommendations;
	}

	public String getAffiliation() {
		return affiliation;
	}

	public String getAffiliationId() {
		return affiliationId;
	}

	public String getAffiliationMetaHref() {
		return affiliationMetaHref;
	}

	public boolean isFollowing() {
		return following;
	}


}

