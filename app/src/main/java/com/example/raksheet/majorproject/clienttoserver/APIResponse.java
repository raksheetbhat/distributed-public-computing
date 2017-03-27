package com.example.raksheet.majorproject.clienttoserver;

import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;

public class APIResponse {
	private static Logger logger = Logger.getLogger(APIResponse.class.getName());
	private int statusCode;
	private String responseMessage;
	private String responseContent;

	public APIResponse(int statusCode, String responseMessage, String responseContent) {
		this.statusCode = statusCode;
		this.responseMessage = responseMessage;
		this.responseContent = responseContent;
	}

	public int getStatusCode() {
		return statusCode;
	}
	public String getResponseMessage() {
		return responseMessage;
	}
	public String getResponseContent() {
		return responseContent;
	}

	public JSONObject getJSON() {
		try{
			if(responseContent != null && !responseContent.isEmpty()) {
				JSONObject jsonObj = new JSONObject(this.responseContent);
				return jsonObj;
			}
		} catch(JSONException e) {
			logger.warning(e.getLocalizedMessage());
		}
		return null;
	}
}
