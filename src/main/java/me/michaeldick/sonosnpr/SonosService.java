package me.michaeldick.sonosnpr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;
import javax.jws.WebService;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.soap.Detail;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPFault;
import javax.xml.ws.Holder;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.soap.SOAPFaultException;

import org.apache.cxf.headers.Header;
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.jaxb.JAXBDataBinding;
import org.apache.cxf.jaxws.context.WrappedMessageContext;
import org.apache.cxf.message.Message;
import org.apache.log4j.Logger;
import org.eclipse.jetty.server.session.HouseKeeper;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Node;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mixpanel.mixpanelapi.ClientDelivery;
import com.mixpanel.mixpanelapi.MessageBuilder;
import com.mixpanel.mixpanelapi.MixpanelAPI;
import com.sonos.services._1.AbstractMedia;
import com.sonos.services._1.AddToContainerResult;
import com.sonos.services._1.AlbumArtUrl;
import com.sonos.services._1.AppLinkResult;
import com.sonos.services._1.ContentKey;
import com.sonos.services._1.CreateContainerResult;
import com.sonos.services._1.Credentials;
import com.sonos.services._1.DeleteContainerResult;
import com.sonos.services._1.DeviceAuthTokenResult;
import com.sonos.services._1.DeviceLinkCodeResult;
import com.sonos.services._1.DynamicData;
import com.sonos.services._1.EncryptionContext;
import com.sonos.services._1.ExtendedMetadata;
import com.sonos.services._1.GetExtendedMetadata;
import com.sonos.services._1.GetExtendedMetadataResponse;
import com.sonos.services._1.GetExtendedMetadataText;
import com.sonos.services._1.GetExtendedMetadataTextResponse;
import com.sonos.services._1.GetMediaMetadata;
import com.sonos.services._1.GetMediaMetadataResponse;
import com.sonos.services._1.GetMetadata;
import com.sonos.services._1.GetMetadataResponse;
import com.sonos.services._1.GetSessionId;
import com.sonos.services._1.GetSessionIdResponse;
import com.sonos.services._1.HttpHeaders;
import com.sonos.services._1.ItemRating;
import com.sonos.services._1.ItemType;
import com.sonos.services._1.LastUpdate;
import com.sonos.services._1.LoginToken;
import com.sonos.services._1.MediaCollection;
import com.sonos.services._1.MediaList;
import com.sonos.services._1.MediaMetadata;
import com.sonos.services._1.MediaUriAction;
import com.sonos.services._1.PositionInformation;
import com.sonos.services._1.Property;
import com.sonos.services._1.RateItem;
import com.sonos.services._1.RateItemResponse;
import com.sonos.services._1.RemoveFromContainerResult;
import com.sonos.services._1.RenameContainerResult;
import com.sonos.services._1.ReorderContainerResult;
import com.sonos.services._1.ReportPlaySecondsResult;
import com.sonos.services._1.Search;
import com.sonos.services._1.SearchResponse;
import com.sonos.services._1.TrackMetadata;
import com.sonos.services._1.UserInfo;
import com.sonos.services._1_1.CustomFault;
import com.sonos.services._1_1.SonosSoap;

import me.michaeldick.npr.model.Channel;
import me.michaeldick.npr.model.Media;
import me.michaeldick.npr.model.NprAuth;
import me.michaeldick.npr.model.Rating;
import me.michaeldick.npr.model.RatingsList;

@WebService
public class SonosService implements SonosSoap {

	public static String NPR_CLIENT_ID = "";
	public static String NPR_CLIENT_SECRET = "";		
	
	public static String MIXPANEL_PROJECT_TOKEN = "";
	
	public static final String PROGRAM = "program";
    public static final String DEFAULT = "default";
    public static final String HISTORY = "history";
    public static final String PODCAST = "podcasts";
    public static final String AGGREGATION = "aggregation";
    public static final String MUSIC = "music";
    public static final String SESSIONIDTOKEN = "###";

    
    // Error codes
    public static final String SESSION_INVALID = "Client.SessionIdInvalid";
    public static final String LOGIN_INVALID = "Client.LoginInvalid";
    public static final String SERVICE_UNKNOWN_ERROR = "Client.ServiceUnknownError";
    public static final String SERVICE_UNAVAILABLE = "Client.ServiceUnavailable";
    public static final String ITEM_NOT_FOUND = "Client.ItemNotFound"; 
    public static final String AUTH_TOKEN_EXPIRED = "Client.AuthTokenExpired";
    public static final String NOT_LINKED_RETRY = "Client.NOT_LINKED_RETRY";
    public static final String NOT_LINKED_FAILURE = "Client.NOT_LINKED_FAILURE";
	
    public static final String PLAYSTATUS_SKIPPED = "skippedTrack";      
    private static final String RATING_ISINTERESTING = "isliked";

    //private static final String IDENTITY_API_URI_DEFAULT = "https://identity.api.npr.org/v2/user";
    private static final String LISTENING_API_URI_DEFAULT = "https://listening.api.npr.org/v2";
    private static final String DEVICE_LINK_URI_DEFAULT = "https://authorization.api.npr.org/v2/device";
    private static final String DEVICE_TOKEN_URI_DEFAULT = "https://authorization.api.npr.org/v2/token";
    
    //private static String IDENTITY_API_URI;
    private static String LISTENING_API_URI;
    private static String DEVICE_LINK_URI;
    private static String DEVICE_TOKEN_URI;
    private static boolean isDebug = false;
    private static int NUMBER_OF_STORIES_PER_CALL = 3;
        
    private static Cache<String, Media> ListeningResponseCache;
    private static Cache<String, List<Rating>> RatingCache;    
    private static Cache<String, List<AbstractMedia>> LastResponseToPlayer;

    // Disable severe log message for SoapFault
    private static java.util.logging.Logger COM_ROOT_LOGGER = java.util.logging.Logger.getLogger("com.sun.xml.internal.messaging.saaj.soap.ver1_1");
    private static Logger logger = Logger.getLogger(SonosService.class.getSimpleName());
    private static MessageBuilder messageBuilder;
    
    @Resource
	private WebServiceContext context;
    
    public WebServiceContext getContext() {
		return this.context;
	}
    
    public SonosService(Properties conf) {    	
    	//IDENTITY_API_URI = conf.getProperty("IDENTITY_API_URI", IDENTITY_API_URI_DEFAULT);
    	LISTENING_API_URI = conf.getProperty("LISTENING_API_URI", LISTENING_API_URI_DEFAULT);
    	DEVICE_LINK_URI = conf.getProperty("DEVICE_LINK_URI", DEVICE_LINK_URI_DEFAULT);
    	DEVICE_TOKEN_URI = conf.getProperty("DEVICE_TOKEN_URI", DEVICE_TOKEN_URI_DEFAULT);
    	isDebug = Boolean.parseBoolean(conf.getProperty("SETDEBUG", "false"));
    	
    	NPR_CLIENT_ID = conf.getProperty("NPR_CLIENT_ID", System.getenv("NPR_CLIENT_ID"));
    	NPR_CLIENT_SECRET = conf.getProperty("NPR_CLIENT_SECRET", System.getenv("NPR_CLIENT_SECRET"));
    	
    	MIXPANEL_PROJECT_TOKEN = conf.getProperty("MIXPANEL_PROJECT_TOKEN", System.getenv("MIXPANEL_PROJECT_TOKEN"));
    	initializeCaches(); 
    	initializeMetrics();
    	
    	COM_ROOT_LOGGER.setLevel(java.util.logging.Level.OFF);
    }
    
    public SonosService () {
    	//IDENTITY_API_URI = IDENTITY_API_URI_DEFAULT;
    	LISTENING_API_URI = LISTENING_API_URI_DEFAULT;
    	DEVICE_LINK_URI = DEVICE_LINK_URI_DEFAULT;
    	DEVICE_TOKEN_URI = DEVICE_TOKEN_URI_DEFAULT;
    	
    	MIXPANEL_PROJECT_TOKEN = System.getenv("MIXPANEL_PROJECT_TOKEN");
    	NPR_CLIENT_ID = System.getenv("NPR_CLIENT_ID");
    	NPR_CLIENT_SECRET = System.getenv("NPR_CLIENT_SECRET");
    	initializeCaches();
    	initializeMetrics();
    	
    	COM_ROOT_LOGGER.setLevel(java.util.logging.Level.OFF);
    }
    
    private void initializeCaches() {
 	
    	ListeningResponseCache = CacheBuilder.newBuilder()
		       .maximumSize(600)
		       .expireAfterWrite(20, TimeUnit.MINUTES).build();
 	
    	RatingCache = CacheBuilder.newBuilder()
		       .maximumSize(600)
		       .expireAfterWrite(20, TimeUnit.MINUTES).build();
    	
    	LastResponseToPlayer = CacheBuilder.newBuilder()
  		       .maximumSize(600)
  		       .expireAfterWrite(20, TimeUnit.MINUTES).build();
    }
    
    public void initializeMetrics() {    	    	
    	messageBuilder = new MessageBuilder(MIXPANEL_PROJECT_TOKEN);    	
    }
    
	@Override
	public String getScrollIndices(String id) throws CustomFault {
		logger.debug("getScrollIndices id:"+id);
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AddToContainerResult addToContainer(String id, String parentId,
			int index, String updateId) throws CustomFault {
		logger.debug("addToContainer");
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GetExtendedMetadataResponse getExtendedMetadata(
			GetExtendedMetadata parameters) throws CustomFault {
		logger.debug("getExtendedMetadata id:"+parameters.getId());

		NprAuth auth = getNprAuth();
				
		GetExtendedMetadataResponse response = new GetExtendedMetadataResponse();		
		Media m = ListeningResponseCache.getIfPresent(auth.getUserId()+parameters.getId());
		
        if (m != null) {
        	logger.debug("ListeningResponseCache hit");
        	MediaMetadata mmd = buildMMD(m);
        	ExtendedMetadata resultWrapper = new ExtendedMetadata();
			MediaMetadata result = new MediaMetadata();
			result.setId(mmd.getId());
			result.setItemType(mmd.getItemType());
			result.setTrackMetadata(mmd.getTrackMetadata());
			result.setMimeType(mmd.getMimeType());		
			result.setTitle(mmd.getTitle());
			result.setDynamic(mmd.getDynamic());
			
			resultWrapper.setMediaMetadata(result);
			response.setGetExtendedMetadataResult(resultWrapper);
			return response;					
		}
        
        throwSoapFault(ITEM_NOT_FOUND);
		return null;		
	}

	@Override
	public ReportPlaySecondsResult reportPlaySeconds(String id, int seconds, String contextId, String privateData,
			Integer offsetMillis) throws CustomFault {
		logger.debug("reportPlaySeconds id:"+id+" seconds:"+seconds);
		if(seconds <= 1) {
			NprAuth auth = getNprAuth();
													
			List<Rating> ratingList = RatingCache.getIfPresent(auth.getUserId());
			
			if(ratingList == null) {
				logger.debug("ratingList is empty");
				ratingList = new ArrayList<Rating>();		
			} else {
				logger.debug("ratingList cache hit");
				for(Rating r : ratingList) {
					if(r.getRating().equals(RatingsList.START)) {
						logger.debug("rating set to completed");
						r.setRating(RatingsList.COMPLETED);
					}
				}
			}		
	
			Media media = ListeningResponseCache.getIfPresent(auth.getUserId()+id);			
			if(media != null) {
				Media m = media;
				logger.debug("media cache hit");
				m.getRating().setRating(RatingsList.START);
				ratingList.add(new Rating(m.getRating()));
				List<Rating> list = new ArrayList<Rating>();
				list.add(new Rating(m.getRating()));
				RatingCache.put(auth.getUserId(), list);					 					 
				sendRecommendations(ratingList, m.getRecommendationLink(), auth);												
			}	 		
		}
		ReportPlaySecondsResult result = new ReportPlaySecondsResult();
		result.setInterval(0);
		return result;
	}

	@Override
	public void reportStatus(String id, int errorCode, String message)
			throws CustomFault {
		logger.debug("reportStatus");
		// TODO Auto-generated method stub		
	}

	@Override
	public RateItemResponse rateItem(RateItem parameters) throws CustomFault {
		logger.debug("rateItem id:"+parameters.getId()+" rating:"+parameters.getRating());

		NprAuth auth = getNprAuth();
				
		List<Rating> list = RatingCache.getIfPresent(auth.getUserId());			
		if(list != null) {
			logger.debug("RatingCache hit");
			boolean alreadyThumbed = false;
			for(Rating r : list) {
				if(r.getMediaId().equals(parameters.getId()) && r.getRating().equals(RatingsList.THUMBUP)) {
					alreadyThumbed = true;
				}
			}
			if (!alreadyThumbed) {
				for(Rating r : list) {
					if(r.getMediaId().equals(parameters.getId())) {
						logger.debug("Setting rating");
						Rating rnew = new Rating(r);
						rnew.setRating(RatingsList.THUMBUP);
						list.add(rnew);
						RatingCache.put(auth.getUserId(), list);
						Gson gson = new GsonBuilder().create();
						logger.debug("Rating cache:"+gson.toJson(list));
						break;
					}
				}
			}
		}		
		
		Media media = ListeningResponseCache.getIfPresent(auth.getUserId()+parameters.getId());
		if(media != null) {
			Media ratedItem = media;
			ratedItem.getRating().setRating(RatingsList.THUMBUP);
			ListeningResponseCache.put(auth.getUserId()+parameters.getId(), media);
		}
		
		ItemRating rating = new ItemRating();
		rating.setShouldSkip(false);
		
		RateItemResponse response = new RateItemResponse();
		response.setRateItemResult(rating);
		return response;
	}

	@Override
	public void reportAccountAction(String type) throws CustomFault {
		logger.debug("reportAccountAction");
		// TODO Auto-generated method stub

	}

	@Override
	public GetExtendedMetadataTextResponse getExtendedMetadataText(
			GetExtendedMetadataText parameters) throws CustomFault {
		logger.debug("getExtendedMetadataText id:"+parameters.getId());
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RenameContainerResult renameContainer(String id, String title)
			throws CustomFault {
		logger.debug("renameContainer");
		// TODO Auto-generated method stub
		
		return null;
	}

	@Override
	public void setPlayedSeconds(String id, int seconds, String contextId, String privateData, Integer offsetMillis)
			throws CustomFault {
		logger.debug("setPlayedSeconds id:"+id+" sec:"+seconds);

		NprAuth auth = getNprAuth();
		
		List<Rating> list = RatingCache.getIfPresent(auth.getUserId());			
		if(list != null) {
			logger.debug("RatingCache hit");
			for(Rating r : list) {
				if(r.getMediaId().equals(id)) {
					logger.debug("Setting seconds");
					r.setElapsed(seconds);
					RatingCache.put(auth.getUserId(), list);
					break;
				}
			}		
		}
		ListeningResponseCache.invalidate(auth.getUserId()+id);
	}

	@Override
	public LastUpdate getLastUpdate() throws CustomFault {
		logger.debug("getLastUpdate");	
		
		NprAuth auth = getNprAuth();	
		
		sendMetricsEvent(auth.getUserId(), "getLastUpdate", null);
		
		LastUpdate response = new LastUpdate();
		
		List<Rating> list = RatingCache.getIfPresent(auth.getUserId());				
		if(list != null) 
			response.setFavorites(Integer.toString(list.hashCode()));
		else
			response.setFavorites("1");
		response.setCatalog("currentCatalog");	
		logger.debug("Fav: "+response.getFavorites()+" Cat: "+response.getCatalog());
		return response;
	}

	@Override
	public DeviceLinkCodeResult getDeviceLinkCode(String householdId)
			throws CustomFault {	
		logger.debug("getDeviceLinkCode");
		
		
        sendMetricsEvent(householdId, "getDeviceLinkCode", null);        
        
		Form form = new Form();
		form.param("client_id", NPR_CLIENT_ID);				
		form.param("client_secret", NPR_CLIENT_SECRET);
		form.param("scope", "identity.readonly "+ 
				"identity.write " + 
				"listening.readonly " + 
				"listening.write " + 
				"localactivation");
		
		String json = "";
		try {
			Client client = ClientBuilder.newClient();
			json = client.target(DEVICE_LINK_URI)
					.request(MediaType.APPLICATION_JSON_TYPE)
					.post(Entity.form(form), String.class);
			client.close();
		} catch (NotAuthorizedException e) {			
			logger.info(householdId.hashCode() +": login NotAuthorized");
			logger.debug(householdId.hashCode() +": "+e.getMessage());
			throwSoapFault(LOGIN_INVALID);
		} catch (BadRequestException e) {
			logger.error("Bad request: "+e.getMessage());
			logger.error(e.getResponse().readEntity(String.class));
			throwSoapFault(SERVICE_UNKNOWN_ERROR);
		}
		
		JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(json);
        String verification_uri = "";
        String user_code = "";
        String device_code = "";
        if (element.isJsonObject()) {
        	JsonObject root = element.getAsJsonObject();
            verification_uri = root.get("verification_uri").getAsString();
            user_code = root.get("user_code").getAsString();
            device_code = root.get("device_code").getAsString();
            logger.info(householdId.hashCode() +": Got verification uri");
        }
		    
        DeviceLinkCodeResult response = new DeviceLinkCodeResult();
		response.setLinkCode(user_code);
		response.setRegUrl(verification_uri);
		response.setLinkDeviceId(device_code);
        response.setShowLinkCode(true);
		return response;
	}

	@Override
	public void deleteItem(String favorite) throws CustomFault {
		logger.debug("deleteItem");
		// TODO Auto-generated method stub

	}

	@Override
	public DeviceAuthTokenResult getDeviceAuthToken(String householdId, String linkCode, String linkDeviceId,
			String callbackPath) throws CustomFault {
		logger.debug("getDeviceAuthToken");
		
		Form form = new Form();
		form.param("client_id", NPR_CLIENT_ID);				
		form.param("client_secret", NPR_CLIENT_SECRET);
		form.param("code", linkDeviceId);
		form.param("grant_type", "device_code");
		
		String json = "";
		try {
			Client client = ClientBuilder.newClient();
			json = client.target(DEVICE_TOKEN_URI)
					.request(MediaType.APPLICATION_JSON_TYPE)
					.post(Entity.entity(form,MediaType.APPLICATION_FORM_URLENCODED_TYPE), String.class);
			client.close();
		} catch (NotAuthorizedException e) {
			logger.info(householdId.hashCode() +": Not linked retry");
			logger.debug(householdId.hashCode() +": "+e.getMessage());
			logger.debug(householdId.hashCode() +": Detailed response: "+e.getResponse().readEntity(String.class));
			throwSoapFault(NOT_LINKED_RETRY, "NOT_LINKED_RETRY", "5");
		} catch (BadRequestException e) {
			logger.error("Bad request: "+e.getMessage());
			throwSoapFault(NOT_LINKED_FAILURE, "NOT_LINKED_FAILURE", "6");
		}
		
		JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(json);
        String access_token = "";        
        if (element.isJsonObject()) {
        	JsonObject root = element.getAsJsonObject();
        	access_token = root.get("access_token").getAsString();            
            logger.info(householdId.hashCode() +": Got token");
        }
		    
        sendMetricsEvent(householdId, "getDeviceAuthToken", null);        
        
        DeviceAuthTokenResult response = new DeviceAuthTokenResult();
		response.setAuthToken(access_token);	
		response.setPrivateKey("KEY");
		return response;
	}

	@Override
	public CreateContainerResult createContainer(String containerType,
			String title, String parentId, String seedId) throws CustomFault {
		logger.debug("createContainer");
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ReorderContainerResult reorderContainer(String id, String from,
			int to, String updateId) throws CustomFault {
		logger.debug("reorderContainer");
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void getMediaURI(String id, MediaUriAction action, Integer secondsSinceExplicit,
			Holder<String> deviceSessionToken, Holder<String> getMediaURIResult,
			Holder<EncryptionContext> deviceSessionKey, Holder<EncryptionContext> contentKey,
			Holder<HttpHeaders> httpHeaders, Holder<Integer> uriTimeout,
			Holder<PositionInformation> positionInformation, Holder<String> privateDataFieldName) throws CustomFault {
		logger.debug("getMediaURI id:"+id);
		
		NprAuth auth = getNprAuth();	
		
		Media m = ListeningResponseCache.getIfPresent(auth.getUserId()+id);
		if(m != null) {
			if(m.getAudioLink() != null) {			
				//getMediaURIResult.value = m.getAudioLink();
				// Temporary workaround for connection issues
				getMediaURIResult.value = m.getAudioLink().replace("https://ondemand.npr.org/anon.npr-mp3/","https://sonosnproneproxy.azurewebsites.net/");
				logger.debug("Media URI found: "+getMediaURIResult.value);
			} else {
				logger.debug("Item not found");				
				throwSoapFault(ITEM_NOT_FOUND);
			}
		} else {
			logger.debug("MediaURICache miss");
			throwSoapFault(ITEM_NOT_FOUND);
		}
	}

	@Override
	public GetMediaMetadataResponse getMediaMetadata(GetMediaMetadata parameters)
			throws CustomFault {
		logger.debug("getMediaMetadata id:"+parameters.getId());

		NprAuth auth = getNprAuth();
				
		GetMediaMetadataResponse response = new GetMediaMetadataResponse();		
		Media m = ListeningResponseCache.getIfPresent(auth.getUserId()+parameters.getId());
		
        if (m != null) {
        	logger.debug("ListeningResponseCache hit");
        	MediaMetadata mmd = buildMMD(m);
        	response.setGetMediaMetadataResult(mmd);
			return response;					
		}
        
        throwSoapFault(ITEM_NOT_FOUND);
		return null;					
	}

	@Override
	public GetMetadataResponse getMetadata(GetMetadata parameters)
			throws CustomFault {
		logger.debug("getMetadata id:"+parameters.getId()+" count:"+parameters.getCount()+" index:"+parameters.getIndex());
		logger.debug(String.format("Cache stats: ListeningResponseCache %d RatingCache %d PlayerCache %d",ListeningResponseCache.size(), RatingCache.size(), LastResponseToPlayer.size())) ;
		
		NprAuth auth = getNprAuth();		
        
        // Mixpanel event
//		if(parameters.getId().equals(SonosService.PROGRAM+":"+SonosService.DEFAULT)
//			|| parameters.getId().equals(SonosService.PROGRAM+":"+SonosService.HISTORY)
//			|| parameters.getId().equals(SonosService.PROGRAM+":"+SonosService.MUSIC)
//			|| parameters.getId().equals(ItemType.SEARCH.value())) {
//					
//	        JSONObject props = new JSONObject();
//	        try {
//				props.put("Program", parameters.getId());
//			} catch (JSONException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}        
//	        
//	        sendMetricsEvent(auth.getUserId(), "getMetadata", props);
//		}        
		
        GetMetadataResponse response = new GetMetadataResponse();
        
		if(parameters.getId().equals("root")) {					
			response.setGetMetadataResult(getChannels(auth));											
		} else if(parameters.getId().startsWith(SonosService.PROGRAM+":"+SonosService.DEFAULT) && parameters.getCount() > 0) {
			response.setGetMetadataResult(getProgram(auth));
		} else if(parameters.getId().startsWith(SonosService.PROGRAM+":"+SonosService.HISTORY)) {			
			response.setGetMetadataResult(getHistory(auth));
		} else if(parameters.getId().equals(SonosService.PROGRAM+":"+SonosService.MUSIC)) {
			response.setGetMetadataResult(getMusicPrograms());
		} else if(parameters.getId().startsWith(SonosService.PROGRAM)) {			
			response.setGetMetadataResult(getChannel(auth, parameters.getId().replaceAll(SonosService.PROGRAM+":", "")));		
		} else if(parameters.getId().startsWith(SonosService.PODCAST)) {
			MediaList ml = getProgram(auth);
			Media m = ListeningResponseCache.getIfPresent(auth.getUserId()+parameters.getId().replaceAll(SonosService.PODCAST+":", ""));
			if (m != null) {
				ml.getMediaCollectionOrMediaMetadata().add(0, buildMMD(m));			
				ml.setCount(ml.getCount()+1);
				ml.setTotal(ml.getTotal()+1);
			}			
			response.setGetMetadataResult(ml);
		} else if(parameters.getId().startsWith(SonosService.AGGREGATION)) {			
			response.setGetMetadataResult(getAggregation(parameters.getId().replaceAll(SonosService.AGGREGATION+":",""), auth));
		} else if(parameters.getId().equals(ItemType.SEARCH.value())) {			
			MediaList ml = new MediaList();
			List<AbstractMedia> mcList = ml.getMediaCollectionOrMediaMetadata();
			
			MediaCollection mc1 = new MediaCollection();			
			mc1.setTitle("Podcasts");
			mc1.setId(SonosService.PODCAST);
			mc1.setItemType(ItemType.SEARCH);
			mcList.add(mc1);			 
			
			ml.setCount(mcList.size());
			ml.setTotal(mcList.size());
			ml.setIndex(0);
			response.setGetMetadataResult(ml);							
		} else {
			return null;
		}
		
		String logLine = auth.getUserId().hashCode() + ": Got Metadata for "+parameters.getId()+", "+response.getGetMetadataResult().getCount();
		logLine += " (";
		for(AbstractMedia m : response.getGetMetadataResult().getMediaCollectionOrMediaMetadata()) {
			logLine += m.getId().substring(m.getId().length() - 2) + " ";
		}
		logLine += ")";
		
		logger.info(logLine);
		return response;
	}

	private MediaList getChannels(NprAuth auth) {
		
		String json = nprApiGetRequest("channels", "exploreOnly", "true", auth);
				
		JsonParser parser = new JsonParser();
		JsonElement element = parser.parse(json);
	        
		JsonArray mainResultList = element.getAsJsonObject().getAsJsonArray("items");			
		
		MediaList ml = new MediaList();
		List<AbstractMedia> mcList = ml.getMediaCollectionOrMediaMetadata();  
		
		MediaCollection mc1 = new MediaCollection();			
		mc1.setTitle("Play NPR One");
		mc1.setId(SonosService.PROGRAM+":"+SonosService.DEFAULT);
		mc1.setItemType(ItemType.PROGRAM);
		mc1.setCanPlay(true);
		mc1.setCanEnumerate(true);
		mcList.add(mc1);
		
        if (mainResultList != null) {         	
            for (int i = 0; i < mainResultList.size(); i++) { 
            	Channel c = new Channel(mainResultList.get(i).getAsJsonObject());
	            	if(!c.getId().equals("promo") && !c.getId().equals("deepdives")) {
	            	MediaCollection mc = new MediaCollection();			
	        		mc.setTitle(c.getFullName());
	        		mc.setId(SonosService.PROGRAM+":"+c.getId());
	        		mc.setItemType(ItemType.COLLECTION);
	        		mc.setCanPlay(false);
	        		mc.setCanEnumerate(true);
	        		mcList.add(mc);		            
            	}
			}
        }
        
        // NPR Music fake node
        MediaCollection mc = new MediaCollection();
        mc.setTitle("NPR Music");
        mc.setId(SonosService.PROGRAM+":"+SonosService.MUSIC);
        mc.setItemType(ItemType.COLLECTION);
        mc.setCanPlay(false);
        mc.setCanEnumerate(true);
        mcList.add(mc);
        
		ml.setCount(mcList.size());
		ml.setIndex(0);
		ml.setTotal(mcList.size());				
    	logger.debug("Got program list: "+mcList.size());
    	return ml;
	}
	
	private MediaList getMusicPrograms() {
		MediaList ml = new MediaList();
		List<AbstractMedia> mcList = ml.getMediaCollectionOrMediaMetadata();
		
		class nprMusicProgram {
			String title;
			String id;
			String logoUrl;
			
			nprMusicProgram(String t, String i, String l) {
				title = t;
				id = i;
				logoUrl = l;
			}
		}
		
		List<nprMusicProgram> programs = new ArrayList<nprMusicProgram>();		
		programs.add(new nprMusicProgram("First Listen", "98679384", null));
		programs.add(new nprMusicProgram("All Songs Considered", "510019", "https://media.npr.org/images/podcasts/primary/icon_510019-045e9424ceb1fd4f5ae73df269de73b8094cd25e.jpg?s=600"));
		programs.add(new nprMusicProgram("Songs We Love", "122356178", null));
		programs.add(new nprMusicProgram("Tiny Desk", "510306","https://media.npr.org/images/podcasts/primary/icon_510306_sq-e07b7d616c85f470d3f723646c10bdfe42b845c2-s400-c85.jpg?s=600"));
		programs.add(new nprMusicProgram("Alt.Latino", "192684845","https://media.npr.org/assets/img/2015/03/19/altlatino_sq-1d6a428fce03069afa0ff73c0f8e83aa6075e23f.jpg?s=600"));
		programs.add(new nprMusicProgram("From The Top", "510026","https://media.npr.org/images/podcasts/primary/icon_510026-f866286349b685887d938edddea04dd710d21f6d-s400-c85.jpg?s=600"));
		programs.add(new nprMusicProgram("Jazz Night In America", "347174538", null));
		programs.add(new nprMusicProgram("Metropolis", "216842113", null));
		programs.add(new nprMusicProgram("Mountain Stage", "382110109","https://media.npr.org/images/podcasts/primary/icon_382110109-a093242f6974f77af440828c4b538e41c9c1fe19.png?s=600"));
		programs.add(new nprMusicProgram("Piano Jazz", "15773266","https://media.npr.org/images/podcasts/primary/icon_510056-98c2e1a249277d0d7c5343b2f2f0d7c487007ef4.jpg?s=600"));
		programs.add(new nprMusicProgram("Song Travels", "150560513","https://media.npr.org/images/podcasts/primary/icon_510304-c565bd4967b2c06e2191eb0b6282ed4551dbf536.jpg?s=600"));
		programs.add(new nprMusicProgram("The Thistle & Shamrock", "510069", null));		
		//programs.add(new nprMusicProgram("World Cafe", ""));		
		
		for(nprMusicProgram entry : programs) {
			MediaCollection mc = new MediaCollection();
	        mc.setTitle(entry.title);
	        mc.setId(SonosService.AGGREGATION+":"+entry.id);
	        mc.setItemType(ItemType.COLLECTION);
	        mc.setCanPlay(false);
	        mc.setCanEnumerate(true);
	        
	        if(entry.logoUrl != null) {
	        	AlbumArtUrl url = new AlbumArtUrl();
	        	url.setValue(entry.logoUrl);
	        	mc.setAlbumArtURI(url);
	        }
	        		        
	        mcList.add(mc);	
		}		
        
		ml.setCount(mcList.size());
		ml.setIndex(0);
		ml.setTotal(mcList.size());				
    	logger.debug("Got music programs: "+mcList.size());    	
		return ml;
	}

	// No longer used after switch to oauth
	@Override
	public GetSessionIdResponse getSessionId(GetSessionId parameters)
			throws CustomFault {
		logger.error("getSessionId (deprecated)");
		
		throwSoapFault(AUTH_TOKEN_EXPIRED);
		
		return null;
//		if(parameters.getUsername().equals("") || parameters.getPassword().equals(""))
//			throwSoapFault(LOGIN_INVALID);
//		
//		logger.debug("Attempting login");
//		String authParameter = "{\"username\":\""+parameters.getUsername()+"\",\"password\":\""+parameters.getPassword()+"\"}";
//		byte[] encodedAuth = Base64.encodeBase64(authParameter.getBytes());
//		Form form = new Form();
//		form.param("auth", new String(encodedAuth));		
//		
//		String json = "";
//		try {
//			Client client = ClientBuilder.newClient();
//			json = client.target(IDENTITY_API_URI)
//					.request(MediaType.APPLICATION_JSON_TYPE)
//					.post(Entity.entity(form,MediaType.APPLICATION_FORM_URLENCODED_TYPE), String.class);
//			client.close();
//		} catch (NotAuthorizedException e) {
//			logger.debug("login NotAuthorized: "+e.getMessage());
//			throwSoapFault(LOGIN_INVALID);
//		}
//		
//		JsonParser parser = new JsonParser();
//        JsonElement element = parser.parse(json);
//        String auth_token = "";
//        String userId = "";
//        if (element.isJsonObject()) {
//        	JsonObject root = element.getAsJsonObject();
//            JsonObject data = root.getAsJsonObject("data");
//            auth_token = data.get("auth_token").getAsString();
//            userId = data.getAsJsonObject("user").get("id").getAsString();
//            logger.debug("Login successful for: "+userId);
//        }
//		    
//		GetSessionIdResponse response = new GetSessionIdResponse();
//		response.setGetSessionIdResult(userId+"###"+auth_token);
//		return response;
	}

	@Override
	public ContentKey getContentKey(String id, String uri, String deviceSessionToken) throws CustomFault {
		logger.debug("getContentKey");
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RemoveFromContainerResult removeFromContainer(String id,
			String indices, String updateId) throws CustomFault {
		logger.debug("removeFromContainer");
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DeleteContainerResult deleteContainer(String id) throws CustomFault {
		logger.debug("deleteContainer");
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void reportPlayStatus(String id, String status, String contextId, Integer offsetMillis) throws CustomFault {		
		logger.debug("reportPlayStatus");

		NprAuth auth = getNprAuth();
		
		if(status.equals(PLAYSTATUS_SKIPPED)) {
			logger.debug("PlayStatus is skipped");
			List<Rating> list = RatingCache.getIfPresent(auth.getUserId());			
			if(list != null) {
				logger.debug("Cache hit");
				for(Rating r : list) {
					if(r.getMediaId().equals(id)) {
						r.setRating(RatingsList.SKIP);
						RatingCache.put(auth.getUserId(), list);
						logger.debug("Rating set");
						break;
					}
				}
			}
			ListeningResponseCache.invalidate(auth.getUserId()+id);
		}
	}

	@Override
	public String createItem(String favorite) throws CustomFault {
		logger.debug("createItem favorite:"+favorite);
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SearchResponse search(Search parameters) throws CustomFault {
		logger.debug("search");
		if(parameters.getTerm() == null || parameters.getTerm().length() < 4)
		{
			SearchResponse response = new SearchResponse();
			response.setSearchResult(new MediaList());
			return response;
		}
		
		NprAuth auth = getNprAuth();
		
		String json = nprApiGetRequest("search/recommendations", "searchTerms", parameters.getTerm(), auth);
		
		SearchResponse response = new SearchResponse();
		response.setSearchResult(parseMediaListResponse(auth.getUserId(), json));				
		return response;
	}

	@Override
	public AppLinkResult getAppLink(String householdId, String hardware, String osVersion, String sonosAppName,
			String callbackPath) throws CustomFault {
		logger.debug("getAppLink");
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public UserInfo getUserInfo() throws CustomFault {
		logger.debug("getUserInfo");
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public DeviceAuthTokenResult refreshAuthToken() throws CustomFault {
		logger.debug("refreshAuthToken");
		// TODO Auto-generated method stub
		return null;
	}
	
	// Private methods
	
	private static MediaList getProgram(NprAuth auth) {							
		MediaList ml = new MediaList();
    	List<AbstractMedia> mcList = ml.getMediaCollectionOrMediaMetadata();		
					
		String json = nprApiGetRequest("recommendations", "channel", "npr", auth);		
		
		JsonParser parser = new JsonParser();
		JsonElement element = parser.parse(json);
	        
		JsonArray searchResultList = element.getAsJsonObject().getAsJsonArray("items");		
        
		List<AbstractMedia> lastProgramCall = LastResponseToPlayer.getIfPresent(auth.getUserId());
		
        if (searchResultList == null)
        	return new MediaList(); 
        	        	
        LinkedList<String> newPlayQueue = new LinkedList<String>();
        for (int i = 0; i < searchResultList.size(); i++) { 
        	Media m = new Media(searchResultList.get(i).getAsJsonObject());
			MediaMetadata mmd = buildMMD(m);
			if(mmd != null) {							
				if(mcList.size() < NUMBER_OF_STORIES_PER_CALL) {
					boolean wasInLastCall = false;
						if(lastProgramCall != null) {
						for(AbstractMedia ele : lastProgramCall) {					
						if(ele.getId().equals(mmd.getId())) {						
							wasInLastCall = true;
							break;
						}
					}
				}
				if(!wasInLastCall)
					mcList.add(mmd);
				}
				newPlayQueue.add(mmd.getId());
				logger.debug("adding track id: "+mmd.getId());
				ListeningResponseCache.put(auth.getUserId()+mmd.getId(), m);					
			}
		}	        		
		
        ml.setCount(mcList.size());
		ml.setIndex(0);
		ml.setTotal(mcList.size());				
    	logger.debug("Got program list: "+mcList.size());
    	LastResponseToPlayer.put(auth.getUserId(), mcList);
			
    	return ml;                			
	}
	
	private static String nprApiGetRequest(String path, String queryParamName, String queryParam, NprAuth auth) {
		String json = "";
		try {	
			Client client = ClientBuilder.newClient();
			WebTarget target = client
					.target(LISTENING_API_URI)
					.path(path);
			if(queryParamName != null && queryParam != null) {
				target = target.queryParam(queryParamName, queryParam);
			}
			json = target.request(MediaType.APPLICATION_JSON_TYPE)
				  .header("Authorization", "Bearer " + auth.getAuth())
				  .get(String.class);
			client.close();
			
		} catch (NotAuthorizedException e) {
			logger.debug("request NotAuthorized: "+e.getMessage());
			throwSoapFault(AUTH_TOKEN_EXPIRED);		
		} catch (BadRequestException e) {
			logger.error("Bad request: "+e.getMessage());
			logger.error(e.getResponse().readEntity(String.class));
		}		
		return json;
	}
	
	private static MediaList getChannel(NprAuth auth, String channel) {		
		String json = nprApiGetRequest("recommendations", "channel", channel, auth);						
		return parseMediaListResponse(auth.getUserId(), json);						
	}
	
	private static MediaList getHistory(NprAuth auth) {		
		String json = nprApiGetRequest("history", null, null, auth);				
		return parseMediaListResponse(auth.getUserId(), json);						
	}

	private static MediaList parseMediaListResponse(String userId, String json) {
		JsonParser parser = new JsonParser();
		JsonElement element = parser.parse(json);
	        
		JsonArray mainResultList = element.getAsJsonObject().getAsJsonArray("items");			
		
        if (mainResultList != null) { 
        	MediaList ml = new MediaList();
        	List<AbstractMedia> mcList = ml.getMediaCollectionOrMediaMetadata();    
        	
            for (int i = 0; i < mainResultList.size(); i++) { 
            	Media m = new Media(mainResultList.get(i).getAsJsonObject());
            	if(m.getType()==Media.AggregationDocumentType.aggregation) {
            		mcList.add(buildMC(m));
            	} else {
					MediaMetadata mmd = buildMMD(m);
					// Trying to avoid duplicates here
					if(mmd != null) {		
						boolean doesExist = false;
						for(AbstractMedia cachedM : mcList)
						{
							if(cachedM.getId().equals(mmd.getId())) {
								doesExist = true;
								break;
							}
						}
						if(!doesExist) {
							mcList.add(buildMC(m));
							logger.debug("adding track id: "+mmd.getId());
							ListeningResponseCache.put(userId+mmd.getId(), m);
						} else {
							logger.debug("tracking existing in cache: "+mmd.getId());
						}					
					}
            	}
			}
			ml.setCount(mcList.size());
			ml.setIndex(0);
			ml.setTotal(mcList.size());				
        	logger.debug("Got program list: "+mcList.size());
        	return ml;
        } else {
        	return new MediaList();
        }
	}
	
	private static MediaList getAggregation(String id, NprAuth auth) {
		String json = nprApiGetRequest("aggregation/"+id+"/recommendations", "startNum", "0", auth);		
		
		return parseMediaListResponse(auth.getUserId(), json);
	}
	
	public static void main(String[] args) {
		Form form = new Form();
		form.param("client_id", NPR_CLIENT_ID);				
		form.param("client_secret", NPR_CLIENT_SECRET);
		form.param("scope", "identity.readonly "+ 
				"identity.write " + 
				"listening.readonly " + 
				"listening.write " + 
				"localactivation");
		
		String json = "";
		try {
			Client client = ClientBuilder.newClient();
			json = client.target(DEVICE_LINK_URI)
					.request(MediaType.APPLICATION_JSON_TYPE)
					.post(Entity.form(form), String.class);
			client.close();
		} catch (NotAuthorizedException e) {
			logger.debug("login NotAuthorized: "+e.getMessage());			
		} catch (BadRequestException e) {
			logger.error("Bad request: "+e.getMessage());
			logger.error(e.getResponse().readEntity(String.class));
		}
		System.out.println(json);
	}
	
	private static void sendRecommendations(List<Rating> ratingsToSend, String uri, NprAuth auth) {
	
		if(ratingsToSend != null)
		{
			logger.debug("sendRecommendations "+ratingsToSend.size()+" to "+uri);
			Gson gson = new GsonBuilder().create();
			String jsonOutput = gson.toJson(ratingsToSend);
			logger.debug("sending :"+jsonOutput);
			
			Client client = ClientBuilder.newClient();
			client.target(uri)					
					.request(MediaType.APPLICATION_JSON_TYPE)
					.header("Authorization", "Bearer "+ auth.getAuth())
					.post(Entity.json(jsonOutput), String.class);
			client.close();						
		}
	}
	
	private static MediaCollection buildMC(Media m) {	
		MediaCollection mc = new MediaCollection();
		
		if(m.getType().equals(Media.AggregationDocumentType.audio)) {
			Media audio = m;
			mc.setId(SonosService.PODCAST+":"+audio.getUid());
			mc.setItemType(ItemType.PROGRAM);
			mc.setTitle(audio.getTitle());
			mc.setArtist(audio.getProgram());
			mc.setCanPlay(true);
			mc.setCanEnumerate(true);
			
			if(audio.getImageLinkSquare() != null) {
				logger.debug("Album art found");
				String albumArtUrlString = audio.getImageLinkSquare();
				if(albumArtUrlString != null) {
					AlbumArtUrl albumArtUrl = new AlbumArtUrl();
					albumArtUrl.setValue(albumArtUrlString);
					mc.setAlbumArtURI(albumArtUrl);
				}
			}	
		} else if(m.getType().equals(Media.AggregationDocumentType.aggregation)) {
			Media agg = m;
			mc.setId(SonosService.AGGREGATION+":"+agg.getAffiliationId());
			mc.setItemType(ItemType.COLLECTION);
			mc.setTitle(agg.getTitle());			
			mc.setCanPlay(false);
			mc.setCanEnumerate(true);	
			
			if(agg.getImageLinkLogoSquare() != null) {
				logger.debug("Album art found");
				String albumArtUrlString = agg.getImageLinkLogoSquare();
				if(albumArtUrlString != null) {
					AlbumArtUrl albumArtUrl = new AlbumArtUrl();
					albumArtUrl.setValue(albumArtUrlString);
					mc.setAlbumArtURI(albumArtUrl);
				}
			}
		}
		
		return mc;
	}
	
	private static MediaMetadata buildMMD(Media m) {
		MediaMetadata mmd = new MediaMetadata();
		TrackMetadata tmd = new TrackMetadata();
		if(m==null)
			return null;
		
		mmd.setId(m.getUid());
		
		if(m.getAudioLink() != null) {
			// Just allowing mp3's for now						
			mmd.setMimeType("audio/mp3");
		} else {
			logger.debug("No audio links found");
			return null;
		}
		
		mmd.setItemType(ItemType.TRACK);		
				
		mmd.setTitle(m.getTitle());
		
		Property property = new Property();
		property.setName(RATING_ISINTERESTING);
		if(m.getRating().getRating().equals(RatingsList.THUMBUP)) {
			property.setValue("1");			
		} else {
			property.setValue("0");
		}
		DynamicData dynData = new DynamicData();
		dynData.getProperty().add(property);
		mmd.setDynamic(dynData);
				
		tmd.setCanSkip(m.isSkippable());		
		tmd.setArtist(m.getProgram());
				
		if(m.getImageLinkSquare() != null) {
			logger.debug("Album art found");
			String albumArtUrlString = m.getImageLinkSquare();
			if(albumArtUrlString != null) {
				AlbumArtUrl albumArtUrl = new AlbumArtUrl();
				albumArtUrl.setValue(albumArtUrlString);
				tmd.setAlbumArtURI(albumArtUrl);
			}
		}
		tmd.setDuration(m.getDuration());

		mmd.setTrackMetadata(tmd);
		
		return mmd;
	}	
	
	private static void throwSoapFault(String faultMessage) {
		throwSoapFault(faultMessage, "", "");
	}
	
	private static void throwSoapFault(String faultMessage, String ExceptionDetail, String SonosError) throws RuntimeException {
		SOAPFault soapFault;
		try {
            soapFault = SOAPFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL).createFault();
            soapFault.setFaultString(faultMessage);
            soapFault.setFaultCode(new QName(faultMessage));
            
            if(!ExceptionDetail.isEmpty() && !SonosError.isEmpty()) {            
            	Detail detail = soapFault.addDetail();
            	SOAPElement el1 = detail.addChildElement("ExceptionDetail");
    	        el1.setValue(ExceptionDetail);
	            SOAPElement el = detail.addChildElement("SonosError");
	            el.setValue(SonosError);	            
            }
            
        } catch (Exception e2) {
            throw new RuntimeException("Problem processing SOAP Fault on service-side." + e2.getMessage());
        }
            throw new SOAPFaultException(soapFault);

    }
	
	private void sendMetricsEvent(String userId, String eventName, JSONObject properties) {
		JSONObject sentEvent = messageBuilder.event(userId, eventName, properties);
	    
        ClientDelivery delivery = new ClientDelivery();
        delivery.addMessage(sentEvent);
        
        MixpanelAPI mixpanel = new MixpanelAPI();
        try {
			mixpanel.deliver(delivery);
		} catch (IOException e1) {
			logger.debug("Mixpanel error: "+eventName);
		}
	}
	
	private NprAuth getNprAuth() {
		Credentials creds = getCredentialsFromHeaders();
		if(creds == null)
			throwSoapFault(SESSION_INVALID);
		
		logger.debug("Got userId from header:"+creds.getLoginToken().getHouseholdId());		
		return new NprAuth(creds.getLoginToken().getHouseholdId(), creds.getLoginToken().getToken());	
	}
	
	
	private Credentials getCredentialsFromHeaders() {
		if(isDebug) {
			Credentials c = new Credentials();
			LoginToken t = new LoginToken();
			t.setHouseholdId("[thehouseholdid]");
			t.setToken("[thetoken]");
			c.setLoginToken(t);
			return c;
		}
		if(context == null)
			return null;
		MessageContext messageContext = context.getMessageContext();
		if (messageContext == null
				|| !(messageContext instanceof WrappedMessageContext)) {
			logger.error("Message context is null or not an instance of WrappedMessageContext.");
			return null;
		}

		Message message = ((WrappedMessageContext) messageContext)
				.getWrappedMessage();
		List<Header> headers = CastUtils.cast((List<?>) message
				.get(Header.HEADER_LIST));
		if (headers != null) {
			for (Header h : headers) {
				Object o = h.getObject();
				// Unwrap the node using JAXB
				if (o instanceof Node) {
					JAXBContext jaxbContext;
					try {
						jaxbContext = new JAXBDataBinding(Credentials.class)
								.getContext();
						Unmarshaller unmarshaller = jaxbContext
								.createUnmarshaller();
						o = unmarshaller.unmarshal((Node) o);
					} catch (JAXBException e) {
						// failed to get the credentials object from the headers
						logger.error(
								"JaxB error trying to unwrapp credentials", e);
					}
				}
				if (o instanceof Credentials) {
					return (Credentials) o;										
				} else {
					logger.error("no Credentials object");
				}
			}
		} else {
			logger.error("no headers found");
		}
		return null;
	}
}
