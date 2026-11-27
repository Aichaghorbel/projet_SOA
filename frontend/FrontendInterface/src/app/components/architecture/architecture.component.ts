import { Component } from '@angular/core';

@Component({
  selector: 'app-architecture',
  templateUrl: './architecture.component.html',
  styleUrls: ['./architecture.component.css']
})
export class ArchitectureComponent {
  selectedTech = 'ALL';

  architectureMatrix = [
    {
      service: 'Academic Service',
      style: 'REST',
      port: 8081,
      format: 'JSON / HTTP',
      responsabilite: 'Gérer les étudiants, les cours, les inscriptions, les emplois du temps, les examens et les résultats.',
      justification: 'REST permet au portail web de communiquer facilement avec le système grâce à HTTP et JSON.',
      endpoints: ['GET /api/v1/students', 'POST /api/v1/students', 'GET /api/v1/courses', 'PUT /api/v1/enrollments'],
      badgeClass: 'badge-service-rest'
    },
    {
      service: 'Smart Space & Exam Service',
      style: 'gRPC',
      port: 9092,
      format: 'Protocol Buffers v3 / HTTP/2',
      responsabilite: 'Gérer les réservations de salles, vérifier les disponibilités, corriger automatiquement les examens et transmettre leur progression.',
      justification: 'gRPC permet des communications rapides entre microservices et prend en charge le streaming, c’est-à-dire l’envoi de plusieurs événements successifs pendant un même échange.',
      endpoints: ['rpc ReserveRoom(...)', 'rpc StreamRoomAvailability(...)', 'rpc SubmitExamAnswer(...)', 'rpc AutoCorrectExam(...)'],
      badgeClass: 'badge-service-grpc'
    },
    {
      service: 'University Administration Service',
      style: 'SOAP',
      port: 8083,
      format: 'XML Enveloppe / WSDL 1.2',
      responsabilite: 'Communiquer avec le système administratif de l’université pour vérifier le statut des étudiants, valider leur éligibilité et transmettre les demandes officielles de certification.',
      justification: 'SOAP est adapté à l’intégration des systèmes administratifs existants qui utilisent des messages XML et des contrats WSDL.',
      endpoints: ['VerifyStudentEligibilityRequest', 'GenerateOfficialCertificationRequest', 'GetOfficialTranscriptsRequest'],
      badgeClass: 'badge-service-soap'
    }
  ];
}
