package me.michaeldick.npr.model;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class Rating {	
	String mediaId;	
    String origin;
    RatingsList rating;
    int elapsed;
    int duration;
    String timestamp;
    String[] affiliations;
    String channel;
    String cohort;

    public Rating(Rating r) {
    	mediaId = r.getMediaId();
    	origin = r.getOrigin();
    	rating = r.getRating();
    	elapsed = r.getElapsed();
    	duration = r.getDuration();
    	timestamp = r.getTimestamp();    	
    	affiliations = r.getAffiliations();
    	channel = r.getChannel();
    	cohort = r.getCohort();
    }
    
    public Rating(JsonObject object) {
    	mediaId = object.get("mediaId").getAsString();
    	origin = object.get("origin").getAsString();
    	rating = RatingsList.valueOf(object.get("rating").getAsString());
    	elapsed = object.get("elapsed").getAsInt();
    	duration = object.get("duration").getAsInt();
    	timestamp = object.get("timestamp").getAsString();
    	JsonArray aff = object.get("affiliations").getAsJsonArray();   
    	List<String> affiliationsList = new ArrayList<String>();
    	for(JsonElement a : aff)
    		affiliationsList.add(a.getAsString());
    	affiliations = affiliationsList.toArray(new String[affiliationsList.size()]);
    	channel = object.get("channel").getAsString();   
    	if(object.has("cohort") && !object.get("cohort").isJsonNull())
    		cohort = object.get("cohort").getAsString();
    	    }

	public void setRating(RatingsList rating) {
		this.rating = rating;
	}

	public void setElapsed(int elapsed) {
		this.elapsed = elapsed;
	}

	public String getMediaId() {
		return mediaId;
	}

	public String getOrigin() {
		return origin;
	}

	public RatingsList getRating() {
		return rating;
	}

	public int getElapsed() {
		return elapsed;
	}

	public int getDuration() {
		return duration;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public String[] getAffiliations() {
		return affiliations;
	}

	public String getChannel() {
		return channel;
	}	
	
	public String getCohort() {
		return cohort;
	}
}
