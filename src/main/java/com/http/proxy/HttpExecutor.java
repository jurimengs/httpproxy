package com.http.proxy;

import org.springframework.web.bind.annotation.PathVariable;


public interface HttpExecutor {
	@HTTP(url="/auth/{appId}/{appSecret}", headers={}, type=HttpTypeEnum.GET)
	TestReq querySth(@PathVariable("appId") String appId, @PathVariable("appSecret") String appSecret);

}
