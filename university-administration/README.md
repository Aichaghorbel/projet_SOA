# University Administration Service (SOAP)

Branche : `feature/soap-xsd-wsdl` - Issue 5 (Sprint 1)

## Contenu
- `src/main/resources/xsd/university-administration.xsd` : contrat ecrit a la main (source de verite)
- WSDL : genere par Spring-WS a partir du XSD (contract-first), exporte dans `docs/university-administration.wsdl` (voir `docs/README-wsdl.md`)
- `src/main/java/...` : squelette Spring-WS (config + 2 endpoints simules)
- `requests/` : requetes SOAP d'exemple pour SoapUI / Postman

## Lancer
```bash
mvn spring-boot:run
```
- Service SOAP : http://localhost:8083/ws
- WSDL genere : http://localhost:8083/ws/university-administration.wsdl

## Tester
Importer le WSDL dans SoapUI, ou envoyer `requests/*.xml` en POST (Content-Type: text/xml) sur `/ws`.
`studentId = UNKNOWN` declenche un SOAP Fault.
