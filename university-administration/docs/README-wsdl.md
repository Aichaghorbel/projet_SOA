# Livrable WSDL (approche contract-first)

Le XSD est la source de verite : `src/main/resources/xsd/university-administration.xsd`.
Le WSDL n'est PAS ecrit a la main : Spring-WS le genere a partir du XSD (bean `DefaultWsdl11Definition`).

## Exporter le WSDL comme livrable
```bash
mvn spring-boot:run
curl http://localhost:8083/ws/university-administration.wsdl -o docs/university-administration.wsdl
```
Verifier qu'il contient les operations `CheckEligibility` et `SubmitCertification`,
puis l'importer dans SoapUI pour valider.
