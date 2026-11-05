package com.knot.provisioning.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.config.annotation.WsConfigurerAdapter;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.soap.security.wss4j2.Wss4jSecurityInterceptor;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.springframework.xml.xsd.XsdSchema;

@Configuration
@EnableWs
public class WebServiceConfig extends WsConfigurerAdapter {
	
	@Autowired
	private Wss4jSecurityInterceptor wsSecurityInterceptor;

	@Override
	public void addInterceptors(List<EndpointInterceptor> interceptors) {
		interceptors.add(wsSecurityInterceptor);
	}

	@Bean
	public ServletRegistrationBean<MessageDispatcherServlet> messageDispatcherServlet(
			ApplicationContext applicationContext) {
		MessageDispatcherServlet servlet = new MessageDispatcherServlet();
		servlet.setApplicationContext(applicationContext);
		servlet.setTransformWsdlLocations(true);
		ServletRegistrationBean<MessageDispatcherServlet> registration = new ServletRegistrationBean<>(servlet,
				"/soap/*");
		registration.setName("messageDispatcherServlet");
		registration.setLoadOnStartup(1);
		registration.setOrder(0);
		return registration;
	}

	@Bean
	public XsdSchema activeFirstSchema() {
		return new SimpleXsdSchema(new ClassPathResource("xsd/active-first.xsd"));
	}

	@Bean(name = "activeFirst")
	public DefaultWsdl11Definition activeFirstWsdl(XsdSchema activeFirstSchema) {
		DefaultWsdl11Definition wsdl = new DefaultWsdl11Definition();
		wsdl.setPortTypeName("ActiveFirstPort");
		wsdl.setServiceName("ActiveFirstService");
		wsdl.setLocationUri("/soap/activeFirst");
		wsdl.setTargetNamespace("http://procesprov.com/");
		wsdl.setSchema(activeFirstSchema);
		return wsdl;
	}
}
