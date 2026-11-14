package com.educampus.administration.config;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.config.annotation.WsConfigurerAdapter;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.springframework.xml.xsd.XsdSchema;

@EnableWs
@Configuration
public class WebServiceConfig extends WsConfigurerAdapter {

    public static final String NAMESPACE_URI = "http://educampus.com/soap/university-administration";

    /** Servlet SOAP exposee sur /ws/* */
    @Bean
    public ServletRegistrationBean<MessageDispatcherServlet> messageDispatcherServlet(ApplicationContext context) {
        MessageDispatcherServlet servlet = new MessageDispatcherServlet();
        servlet.setApplicationContext(context);
        servlet.setTransformWsdlLocations(true);
        return new ServletRegistrationBean<>(servlet, "/ws/*");
    }

    /** WSDL genere a l'execution a partir du XSD : http://localhost:8083/ws/university-administration.wsdl */
    @Bean(name = "university-administration")
    public DefaultWsdl11Definition defaultWsdl11Definition(XsdSchema universityAdministrationSchema) {
        DefaultWsdl11Definition wsdl = new DefaultWsdl11Definition();
        wsdl.setPortTypeName("UniversityAdministrationPort");
        wsdl.setLocationUri("/ws");
        wsdl.setTargetNamespace(NAMESPACE_URI);
        wsdl.setSchema(universityAdministrationSchema);
        return wsdl;
    }

    @Bean
    public XsdSchema universityAdministrationSchema() {
        return new SimpleXsdSchema(new ClassPathResource("xsd/university-administration.xsd"));
    }
}
