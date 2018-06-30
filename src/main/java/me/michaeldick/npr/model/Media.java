package me.michaeldick.npr.model;

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
    String webLink;
    String audioLink;
    String imageLinkSquare;
    String imageLinkLogoSquare;
    String recommendationLink;
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
    		for(JsonElement el : links.get("web").getAsJsonArray()) {    	
    			if(el.getAsJsonObject().get("content-type").getAsString().equals("text/html")) {
        			webLink = el.getAsJsonObject().get("href").getAsString();
        			break;    				
    			}
    		}    		    	
    	}  
    	if(links.has("image")) {  
    		for(JsonElement el : links.get("image").getAsJsonArray()) { 
    			if(el.getAsJsonObject().has("rel") && el.getAsJsonObject().has("href") && el.getAsJsonObject().get("rel").getAsString().equals("square"))
    				imageLinkSquare = el.getAsJsonObject().get("href").getAsString();
    			else if(el.getAsJsonObject().has("rel") && el.getAsJsonObject().has("href") && el.getAsJsonObject().get("rel").getAsString().equals("logo_square"))
    				imageLinkLogoSquare = el.getAsJsonObject().get("href").getAsString();
    		}    		    	
    	} 
    	if(links.has("audio")) { 
    		for(JsonElement el : links.get("audio").getAsJsonArray()) {
    			if(el.getAsJsonObject().get("content-type").getAsString().equals("audio/mp3") && !el.getAsJsonObject().get("href").getAsString().endsWith(".mp4")) {			
    				audioLink = el.getAsJsonObject().get("href").getAsString();
    				break;
    			}		
    			else if(el.getAsJsonObject().get("content-type").getAsString().equals("audio/aac")) {    					
    				audioLink = el.getAsJsonObject().get("href").getAsString();
    				break;
    				
    			}    			
    		}    		    	
    	} 
    	if(links.has("recommendations") && links.get("recommendations").getAsJsonArray().size() > 0) {    	    		    			
    			recommendationLink = links.get("recommendations").getAsJsonArray().get(0).getAsJsonObject().get("href").getAsString();    		    	
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

	public String getWebLink() {
		return webLink;
	}

	public String getAudioLink() {
		return audioLink;
	}

	public String getImageLinkSquare() {
		return imageLinkSquare;
	}
	
	public String getImageLinkLogoSquare() {
		return imageLinkLogoSquare;
	}

	public String getRecommendationLink() {
		return recommendationLink;
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

