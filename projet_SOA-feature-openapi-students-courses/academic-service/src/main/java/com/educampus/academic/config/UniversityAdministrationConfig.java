package com.educampus.academic.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.transport.http.HttpUrlConnectionMessageSender;
import java.time.Duration;

/** Client SOAP vers University Administration (contrat : src/main/resources/xsd). */
@Configuration
public class UniversityAdministrationConfig {

    @Bean
    public Jaxb2Marshaller universityAdministrationMarshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setContextPath("com.educampus.academic.soap.generated");
        return marshaller;
    }

    @Bean
    public WebServiceTemplate universityAdministrationTemplate(
            Jaxb2Marshaller marshaller,
            @Value("${university-admin.url}") String url,
            @Value("${university-admin.connect-timeout-ms:2000}") int connectTimeoutMs,
            @Value("${university-admin.read-timeout-ms:5000}") int readTimeoutMs) {

        HttpUrlConnectionMessageSender sender = new HttpUrlConnectionMessageSender();
        sender.setConnectionTimeout(Duration.ofMillis(connectTimeoutMs));
        sender.setReadTimeout(Duration.ofMillis(readTimeoutMs));

        WebServiceTemplate template = new WebServiceTemplate();
        template.setMarshaller(marshaller);
        template.setUnmarshaller(marshaller);
        template.setDefaultUri(url);
        template.setMessageSender(sender);
        return template;
    }
}