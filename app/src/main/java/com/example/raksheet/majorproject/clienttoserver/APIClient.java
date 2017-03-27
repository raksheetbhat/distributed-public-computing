package com.example.raksheet.majorproject.clienttoserver;

import android.os.AsyncTask;


import org.apache.http.NameValuePair;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.HttpsURLConnection;

public class APIClient extends AsyncTask<Void, Void, APIResponse> {

	private static Logger logger = Logger.getLogger(APIClient.class.getName());
	private String method;
	private String url;
	private List<? extends NameValuePair> parameters;
	private APIResponseListner<APIResponse> listener;

	public static class REQUEST_METHOD {
		public static final String GET = "GET";
		public static final String POST = "POST";
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public APIClient(String method, String url, List<? extends NameValuePair> parameters, APIResponseListner<APIResponse> listener) {
		this.method = method.toUpperCase(Locale.US);
		this.url = url;
		this.parameters = parameters;
		//((ArrayList)parameters).add(new BasicNameValuePair(Constants.HTTP_PARAM.CLIENT_KEY, SharedPreferenceFile.clientKey));
		this.listener = listener;
	}

	@Override
	protected APIResponse doInBackground(Void... params) {
		APIResponse response = null;
		if("GET".equals(this.method)) {
			response = doGet();
		} else if("POST".equals(this.method)) {
			response = doPost();
		}
		return response;
	}

	@Override
	protected void onPostExecute(APIResponse result) {
		if(listener != null) {
			if(result != null) {
				this.listener.onApiCallSucceded(result);
			} else {
				this.listener.onApiCallFailed();
			}
		}
	}

	private URLConnection getConnection(URL url) throws IOException {
		URLConnection connection = url.openConnection();

		if(connection instanceof HttpURLConnection) {
			HttpURLConnection httpURLConnection = (HttpURLConnection) connection;
			httpURLConnection.setReadTimeout(50000);
			httpURLConnection.setConnectTimeout(75000);
			httpURLConnection.setRequestMethod(this.method); //GET, POST
			httpURLConnection.setDoInput(true);
			httpURLConnection.setRequestProperty("Accept-Encoding", "" );
			httpURLConnection.setRequestProperty("Connection", "close");

			if("POST".equals(this.method)) httpURLConnection.setDoOutput(true);
			httpURLConnection.setChunkedStreamingMode(0);
		}

		if(connection instanceof HttpsURLConnection) {
		}
		return connection;
	}

	private void closeConnection(URLConnection connection) {
		if(connection instanceof HttpsURLConnection) {
			((HttpsURLConnection)connection).disconnect();
		} else {
			//Sorry, we don't know how to disconnect!
		}
	}

	private APIResponse getResponse(URLConnection urlConnection) throws IOException {
		if(urlConnection instanceof HttpURLConnection) {
			HttpURLConnection connection = (HttpURLConnection) urlConnection;
			InputStream responseStream = connection.getInputStream();

			int responseCode = connection.getResponseCode();
			String responseMessage = connection.getResponseMessage();
			String responseContent = "";

			if(responseStream != null) {
				BufferedReader responseStreamReader = new BufferedReader(new InputStreamReader(responseStream));
				StringBuffer response = new StringBuffer(100);
				do {
					String line = responseStreamReader.readLine();
					if(line != null) {
						response.append(line);
					} else {
						break;
					}
				}while(true);

				responseStream.close();
				if(responseStreamReader != null) {
					responseStreamReader.close();
				}
				responseContent = response.toString();

				return new APIResponse(responseCode, responseMessage, responseContent);
			}
		}
		return null;
	}

	private String getQuery()
	{
		StringBuilder result = new StringBuilder();
		boolean first = true;
		if(this.parameters != null) {
			for (NameValuePair pair : this.parameters)
			{
				try{
					String param = URLEncoder.encode(pair.getName(), "UTF-8");

					String value = URLEncoder.encode(pair.getValue(), "UTF-8");

					//Append to query only if encoding of both key and value are successful, else an exception would be thrown ;)
					if(!first) result.append("&");
					first = false;

					result.append(param);
					result.append("=");
					result.append(value);
				} catch(UnsupportedEncodingException e) {
					logger.warning(e.getLocalizedMessage()); //Ideally we should throw an exception, But more ideal is to not reach this point in code
				} catch(Exception e){
					e.printStackTrace();
				}
			}
		}
		logger.log(Level.ALL, result.toString());
		return result.toString();
	}

	private APIResponse doGet() {
		URLConnection urlConnection = null;
		try {
			String parameterString = getQuery();
			URL theUrl = new URL(this.url +(this.url.contains("?")? "&":"?") + parameterString);
			urlConnection = getConnection(theUrl);
			logger.warning("The URL in doGET is "+ theUrl);
			return getResponse(urlConnection);
		} catch (MalformedURLException e) {
			logger.warning(e.getLocalizedMessage());
		} catch (IOException e) {
			logger.warning(e.getLocalizedMessage());
		} finally {
			if(urlConnection != null) {
				closeConnection(urlConnection);
			}
		}
		return null;
	}

	private APIResponse doPost() {
		URLConnection conn = null;
		try {
			URL theUrl = new URL(this.url);
			logger.log(Level.ALL, theUrl.toString() + "  Logging in the Post function");
			conn = getConnection(theUrl);

			OutputStream outputStream = conn.getOutputStream();
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
			writer.write(getQuery());
			writer.flush();
			writer.close();
			outputStream.close();

			return getResponse(conn);
		} catch (MalformedURLException e) {
			logger.warning("Malformed URL exception Error " + e.getLocalizedMessage());
		} catch (IOException e) {
			e.printStackTrace();
			logger.warning("IO Error " + e.getLocalizedMessage());
		} finally {
			if(conn != null) {
				closeConnection(conn);
			}
		}
		return null;
	}
}
