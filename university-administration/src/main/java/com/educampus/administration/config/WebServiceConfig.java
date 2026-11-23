package com.educampus.administration.config;

import com.educampus.administration.exception.CertificationNotFoundException;
import com.educampus.administration.exception.DuplicateCertificationException;
import com.educampus.administration.exception.InvalidStatusTransitionException;
import com.educampus.administration.exception.StudentIneligibleException;
import com.educampus.administration.exception.StudentNotFoundException;
import java.util.List;
import java.util.Properties;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.config.annotation.WsConfigurerAdapter;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.soap.server.endpoint.SoapFaultDefinition;
import org.springframework.ws.soap.server.endpoint.interceptor.PayloadValidatingInterceptor;
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

    /** Sprint 2 : validation des requetes entrantes contre le XSD (requete invalide => SOAP Fault client). */
    @Override
    public void addInterceptors(List<EndpointInterceptor> interceptors) {
        PayloadValidatingInterceptor validator = new PayloadValidatingInterceptor();
        validator.setXsdSchema(universityAdministrationSchema());
        validator.setValidateRequest(true);
        validator.setValidateResponse(false);
        interceptors.add(validator);
    }

    /** Sprint 2 : transforme les exceptions metier en SOAP Fault (+ detail ServiceFault du XSD). */
    @Bean
    public ServiceFaultExceptionResolver serviceFaultExceptionResolver() {
        ServiceFaultExceptionResolver resolver = new ServiceFaultExceptionResolver();

        // Erreurs imputables au client => faultcode Client
        Properties mappings = new Properties();
        mappings.setProperty(StudentNotFoundException.class.getName(), "CLIENT");
        mappings.setProperty(StudentIneligibleException.class.getName(), "CLIENT");
        mappings.setProperty(IllegalArgumentException.class.getName(), "CLIENT");
        mappings.setProperty(CertificationNotFoundException.class.getName(), "CLIENT");
        mappings.setProperty(DuplicateCertificationException.class.getName(), "CLIENT");
        mappings.setProperty(InvalidStatusTransitionException.class.getName(), "CLIENT");
        resolver.setExceptionMappings(mappings);

        // Tout le reste => faultcode Server
        SoapFaultDefinition internal = new SoapFaultDefinition();
        internal.setFaultCode(SoapFaultDefinition.SERVER);
        internal.setFaultStringOrReason("Erreur interne du service");
        resolver.setDefaultFault(internal);

        resolver.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return resolver;
    }
}
