package com.http.proxy;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 默认的post请求
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HTTP {
//	    　　　1.所有基本数据类型（int,float,boolean,byte,double,char,long,short)
//	　　　　2.String类型
//	　　　　3.Class类型
//	　　　　4.enum类型
//	　　　　5.Annotation类型
//	　　　　6.以上所有类型的数组
	String[] value() default {};
	
	String[] headers() default {};
	
	String url();
	
	HttpTypeEnum type() default HttpTypeEnum.POST;
}
