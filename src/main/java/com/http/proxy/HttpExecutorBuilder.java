package com.http.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;

import lombok.extern.slf4j.Slf4j;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;

import com.alibaba.fastjson.JSON;

@Slf4j
public class HttpExecutorBuilder {
	
	private HttpExecutorBuilder() {
	}

	public static HttpExecutorBuilder newBuilder() {
		return new HttpExecutorBuilder();
	}
	
	public HttpExecutorBuilder host(String host) {
		this.host = host;
		return this;
	}
	
	public HttpExecutorBuilder httpClient(HttpClient httpClient) {
		this.httpClient = httpClient;
		return this;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T create(Class<T> clazz) {
		
		Object obj = Proxy.newProxyInstance(clazz.getClassLoader(),
				new Class<?>[] { clazz }, new HttpExecutorInvocationHandler());

		return (T) obj;
	}

	private String host;
	private HttpClient httpClient; 
	
	private class HttpExecutorInvocationHandler implements InvocationHandler {
		
		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			return doSendByAnno(method, args);
		}

		private Object doSendByAnno(Method method, Object[] args) {
			Class<?> returnClass = method.getReturnType();
			Parameter[] parameters = method.getParameters();
			HTTP http = method.getAnnotation(HTTP.class);
			Object obj = null;
			if(StringUtils.isEmpty(host)) {
				throw new HttpExecuteException("host can not be null");
			}
			if(httpClient == null) {
				throw new HttpExecuteException("httpClient can not be null");
			}
			
			String httpUrl = host + http.url();
			Header[] header = getHeadersFromAnno(http);
			
			
			if (http.type() == HttpTypeEnum.POST) {
				// 因为post参数类型约定为json格式的String类型，所以可以直接
				obj = sendPost(args, httpUrl, header, returnClass);
			} else if (http.type() == HttpTypeEnum.GET) {
				obj = sendGet(args, httpUrl, header, parameters, returnClass);
			}
			return obj;
		}

		private Object sendGet(Object[] args, String httpUrl, Header[] header, 
				Parameter[] parameters, Class<?> returnClass) {
			String urlTemp = transGetUrl(httpUrl, args, parameters);
			try {
				String res = httpClient.sendHttpGet(urlTemp, header);
				JSON resJson = JSON.parseObject(res);
				return JSON.toJavaObject(resJson, returnClass);
			} catch (Exception e) {
				log.error("httpUrl: {}", httpUrl, e);
				throw new HttpExecuteException("send http request exception.");
			}
			
		}

		private Object sendPost(Object[] args, String httpUrl, Header[] header, Class<?> returnClass) {
			try {
				String res = null;
				if(args != null && args.length > 0 ) {
					String params = JSON.toJSONString(args[0]);
					res = httpClient.sendHttpPost(httpUrl, params, header);
				} else {
					res = httpClient.sendHttpPost(httpUrl, header);
				}
				JSON resJson = JSON.parseObject(res);
				
				return JSON.toJavaObject(resJson, returnClass);
			} catch (Exception e) {
				log.error("httpUrl: {}", httpUrl, e);
				throw new HttpExecuteException("send http request exception.");
			}
		}

		private String transGetUrl(String httpUrl, Object[] args,
				Parameter[] parameters) {
			// args 因为是由动态代理传递，而parameters 是由method反射而来，因此两数组长度必定相匹配
			String resUrl = httpUrl;
			if(args != null && args.length > 0) {
				for (int i = 0; i < parameters.length; i++) {
					PathVariable pv = parameters[i].getAnnotation(PathVariable.class);
					if(pv == null) {
						throw new HttpExecuteException("param must have annotation : \"PathVariable\"");
					}
					resUrl = resUrl.replace("{"+ pv.value() +"}", args[i].toString());
				}
			}
			return resUrl;
		}

		private Header[] getHeadersFromAnno(HTTP http) {
			String[] headerStrings = http.headers();
			Header[] headers = new BasicHeader[headerStrings.length];
			if(headerStrings.length > 0){
				String[] strArr = null;
				for (int i = 0; i < headerStrings.length; i++) {
					strArr = headerStrings[i].split(SymbolConstant.COLON);
					if(strArr.length != 2) {
						throw new HttpExecuteException("header format must like \"{key:value}\"");
					}
					headers[i] = new BasicHeader(strArr[0], strArr[1]);
				}
			}
			return headers;
		}
	}
}
