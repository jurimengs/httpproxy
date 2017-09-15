package com.http.proxy;

import org.junit.Test;

public class HttpExecutorBuilderTest {
	@Test
	public void testBuild() {
		// http://localhost:3001/notifyCenter/auth/CRM/44AE97ED-C2EB-4E94-8E99-7228B19DAB37
		HttpExecutor exe = HttpExecutorBuilder.newBuilder()
				.host("http://localhost:3001/notifyCenter")
				.create(HttpExecutor.class);
		TestReq res = exe.querySth("Test", "44AE97ED-C2EB-4E94-8E99-7228B19DAB37");
		System.out.println(res);
	}
}
