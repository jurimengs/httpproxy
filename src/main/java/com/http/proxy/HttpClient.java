package com.http.proxy;

import org.apache.http.Header;

public interface HttpClient {
	public String sendHttpPost(String httpUrl, String params, Header[] header);

	public String sendHttpPost(String httpUrl, Header[] header);

	public String sendHttpGet(String urlTemp, Header[] header);

}
