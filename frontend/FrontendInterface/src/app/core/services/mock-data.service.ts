import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import {
  Student,
  Course,
  TimetableEntry,
  GradeRecord,
  Room,
  RoomReservation,
  Exam,
  ExamQuestion,
  ExamSubmissionResult,
  OfficialCertificateRequest,
  UserRole
} from '../models';

@Injectable({
  providedIn: 'root'
})
export class MockDataService {
  // Current active role
  private currentRoleSubject = new BehaviorSubject<UserRole>('student');
  public currentRole$ = this.currentRoleSubject.asObservable();

  // Students list
  private studentsSubject = new BehaviorSubject<Student[]>([
    {
      id: 'STD-101',
      matricule: 'IIT-2024-0012',
      cin: '09876543',
      firstName: 'Youssef',
      lastName: 'Ben Amor',
      email: 'youssef.benamor@iit.tn',
      phone: '+216 22 345 678',
      branch: 'Génie Informatique (GI)',
      level: '3ème Année Ingénieur',
      status: 'Inscrit',
      gpa: 16.85,
      creditsAcquired: 120,
      eligibilityStatus: 'VALIDE',
      eligibilityReason: 'Dossier complet, scolarité réglée, 120 crédits ECTS acquis',
      photoUrl: 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150&auto=format&fit=crop&q=80'
    },
    {
      id: 'STD-102',
      matricule: 'IIT-2024-0045',
      cin: '11223344',
      firstName: 'Sarra',
      lastName: 'Triki',
      email: 'sarra.triki@iit.tn',
      phone: '+216 55 987 654',
      branch: 'Génie Informatique (GI)',
      level: '3ème Année Ingénieur',
      status: 'Inscrit',
      gpa: 17.40,
      creditsAcquired: 120,
      eligibilityStatus: 'VALIDE',
      eligibilityReason: 'Major de promotion, prérequis académiques satisfaits',
      photoUrl: 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150&auto=format&fit=crop&q=80'
    },
    {
      id: 'STD-103',
      matricule: 'IIT-2024-0089',
      cin: '08765432',
      firstName: 'Ahmed',
      lastName: 'Karray',
      email: 'ahmed.karray@iit.tn',
      phone: '+216 98 123 456',
      branch: 'Génie Télécoms & Réseaux (TR)',
      level: '2ème Année Ingénieur',
      status: 'Inscrit',
      gpa: 14.10,
      creditsAcquired: 60,
      eligibilityStatus: 'VALIDE',
      eligibilityReason: 'Semestre 1 validé sans dette académique',
      photoUrl: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150&auto=format&fit=crop&q=80'
    },
    {
      id: 'STD-104',
      matricule: 'IIT-2024-0120',
      cin: '14325678',
      firstName: 'Mariem',
      lastName: 'Abid',
      email: 'mariem.abid@iit.tn',
      phone: '+216 29 444 888',
      branch: 'Génie Mécatronique & Robotique',
      level: '1ère Année Ingénieur',
      status: 'Inscrit',
      gpa: 15.20,
      creditsAcquired: 30,
      eligibilityStatus: 'VALIDE',
      eligibilityReason: 'Dossier administratif approuvé par la scolarité',
      photoUrl: 'https://images.unsplash.com/photo-1517841905240-472988babdf9?w=150&auto=format&fit=crop&q=80'
    },
    {
      id: 'STD-105',
      matricule: 'IIT-2023-0199',
      cin: '07654321',
      firstName: 'Mohamed',
      lastName: 'Sellami',
      email: 'mohamed.sellami@iit.tn',
      phone: '+216 20 111 222',
      branch: 'Génie Informatique (GI)',
      level: '3ème Année Ingénieur',
      status: 'En attente',
      gpa: 11.80,
      creditsAcquired: 108,
      eligibilityStatus: 'EN_ATTENTE',
      eligibilityReason: 'En attente de validation du stage ingénieur et quitus bibliothèque',
      photoUrl: 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=150&auto=format&fit=crop&q=80'
    }
  ]);
  public students$ = this.studentsSubject.asObservable();

  // Courses list
  private coursesSubject = new BehaviorSubject<Course[]>([
    {
      id: 'CRS-01',
      code: 'INF-301',
      title: 'Architectures Logicielles & Services Distribués',
      credits: 5,
      department: 'Génie Informatique',
      semester: 'Semestre 1',
      teacher: 'Dr. Tarek Louati',
      room: 'Labo Informatique (B2-04)',
      enrolledCount: 38,
      maxCapacity: 40,
      scheduleTime: 'Lundi 08:30 - 11:30',
      description: 'Conception des architectures distribuées d\'entreprise, modélisation des processus métier et intégration logicielle.',
      isEnrolled: true
    },
    {
      id: 'CRS-02',
      code: 'CLD-302',
      title: 'Cloud Computing & DevOps (Docker, Kubernetes)',
      credits: 4,
      department: 'Génie Informatique',
      semester: 'Semestre 1',
      teacher: 'Ing. Slim Rekik',
      room: 'Salle Info 102',
      enrolledCount: 36,
      maxCapacity: 40,
      scheduleTime: 'Mardi 10:15 - 13:15',
      description: 'Conteneurisation, orchestration Kubernetes, intégration continue et déploiement d\'applications universitaires.',
      isEnrolled: true
    },
    {
      id: 'CRS-03',
      code: 'DAT-303',
      title: 'Bases de Données Avancées & Big Data',
      credits: 4,
      department: 'Génie Informatique',
      semester: 'Semestre 1',
      teacher: 'Dr. Sonia Mezghani',
      room: 'Amphithéâtre Ibn Khaldoun',
      enrolledCount: 42,
      maxCapacity: 50,
      scheduleTime: 'Mercredi 14:00 - 17:00',
      description: 'Gestion des grands volumes de données, bases NoSQL, traitement distribué et analytique de données.',
      isEnrolled: true
    },
    {
      id: 'CRS-04',
      code: 'SEC-304',
      title: 'Cybersécurité & Sécurisation des Systèmes d\'Information',
      credits: 3,
      department: 'Génie Informatique',
      semester: 'Semestre 1',
      teacher: 'Dr. Walid Chaari',
      room: 'Labo Sécurité Réseau',
      enrolledCount: 34,
      maxCapacity: 35,
      scheduleTime: 'Jeudi 08:30 - 11:30',
      description: 'Cryptographie appliquée, authentification multi-facteurs, gestion des accès et protection des réseaux.',
      isEnrolled: false
    },
    {
      id: 'CRS-05',
      code: 'IA-305',
      title: 'Intelligence Artificielle & Apprentissage Automatique',
      credits: 4,
      department: 'Génie Informatique',
      semester: 'Semestre 1',
      teacher: 'Prof. Anis Fakhfakh',
      room: 'Amphithéâtre Al-Kindi',
      enrolledCount: 45,
      maxCapacity: 60,
      scheduleTime: 'Vendredi 08:30 - 11:30',
      description: 'Réseaux de neurones, vision par ordinateur, traitement automatique du langage naturel et aide à la décision.',
      isEnrolled: false
    }
  ]);
  public courses$ = this.coursesSubject.asObservable();

  // Weekly Timetable
  private timetableSubject = new BehaviorSubject<TimetableEntry[]>([
    {
      id: 'TT-01',
      day: 'Lundi',
      timeSlot: '08:30 - 11:30',
      courseCode: 'INF-301',
      courseTitle: 'Architectures Logicielles & Services Distribués',
      teacher: 'Dr. Tarek Louati',
      room: 'Labo Informatique (B2-04)',
      group: '3ème Année GI - Groupe 1',
      type: 'Cours',
      color: '#114388'
    },
    {
      id: 'TT-02',
      day: 'Lundi',
      timeSlot: '13:30 - 16:30',
      courseCode: 'INF-301',
      courseTitle: 'Séance Pratique de Modélisation & Développement',
      teacher: 'Dr. Tarek Louati',
      room: 'Labo Informatique 101',
      group: '3ème Année GI - Groupe 1',
      type: 'TP',
      color: '#10b981'
    },
    {
      id: 'TT-03',
      day: 'Mardi',
      timeSlot: '10:15 - 13:15',
      courseCode: 'CLD-302',
      courseTitle: 'Cloud Computing & Déploiement Applicatif',
      teacher: 'Ing. Slim Rekik',
      room: 'Salle Info 102',
      group: '3ème Année GI - Groupe 1',
      type: 'Cours',
      color: '#0284c7'
    },
    {
      id: 'TT-04',
      day: 'Mercredi',
      timeSlot: '08:30 - 11:30',
      courseCode: 'DAT-303',
      courseTitle: 'Bases de Données Avancées & NoSQL',
      teacher: 'Dr. Sonia Mezghani',
      room: 'Amphithéâtre Ibn Khaldoun',
      group: '3ème Année GI - Tous Groupes',
      type: 'Cours',
      color: '#d97706'
    },
    {
      id: 'TT-05',
      day: 'Jeudi',
      timeSlot: '14:00 - 17:00',
      courseCode: 'SEC-304',
      courseTitle: 'Sécurité des Systèmes d\'Information',
      teacher: 'Dr. Walid Chaari',
      room: 'Labo Sécurité Réseau',
      group: '3ème Année GI - Groupe 1',
      type: 'TD',
      color: '#8b5cf6'
    },
    {
      id: 'TT-06',
      day: 'Vendredi',
      timeSlot: '08:30 - 11:30',
      courseCode: 'IA-305',
      courseTitle: 'Intelligence Artificielle & Deep Learning',
      teacher: 'Prof. Anis Fakhfakh',
      room: 'Amphithéâtre Al-Kindi',
      group: '3ème Année GI - Tous Groupes',
      type: 'Cours',
      color: '#e11d48'
    }
  ]);
  public timetable$ = this.timetableSubject.asObservable();

  // Grades
  private gradesSubject = new BehaviorSubject<GradeRecord[]>([
    {
      id: 'GRD-01',
      studentId: 'STD-101',
      studentName: 'Youssef Ben Amor',
      matricule: 'IIT-2024-0012',
      courseCode: 'INF-301',
      courseTitle: 'Architectures Logicielles & Services Distribués',
      examScore: 17.5,
      dsScore: 16.0,
      tpScore: 18.0,
      finalScore: 17.2,
      status: 'Admis',
      session: 'Principale'
    },
    {
      id: 'GRD-02',
      studentId: 'STD-101',
      studentName: 'Youssef Ben Amor',
      matricule: 'IIT-2024-0012',
      courseCode: 'CLD-302',
      courseTitle: 'Cloud Computing & DevOps',
      examScore: 16.0,
      dsScore: 15.5,
      tpScore: 17.0,
      finalScore: 16.2,
      status: 'Admis',
      session: 'Principale'
    },
    {
      id: 'GRD-03',
      studentId: 'STD-101',
      studentName: 'Youssef Ben Amor',
      matricule: 'IIT-2024-0012',
      courseCode: 'DAT-303',
      courseTitle: 'Bases de Données Avancées',
      examScore: 15.0,
      dsScore: 14.5,
      tpScore: 16.5,
      finalScore: 15.3,
      status: 'Admis',
      session: 'Principale'
    },
    {
      id: 'GRD-04',
      studentId: 'STD-102',
      studentName: 'Sarra Triki',
      matricule: 'IIT-2024-0045',
      courseCode: 'INF-301',
      courseTitle: 'Architectures Logicielles & Services Distribués',
      examScore: 18.5,
      dsScore: 17.5,
      tpScore: 19.0,
      finalScore: 18.3,
      status: 'Admis',
      session: 'Principale'
    }
  ]);
  public grades$ = this.gradesSubject.asObservable();

  // Rooms (Smart Space)
  private roomsSubject = new BehaviorSubject<Room[]>([
    {
      id: 'RM-101',
      code: 'AMP-IK',
      name: 'Amphithéâtre Ibn Khaldoun',
      type: 'Amphithéâtre',
      capacity: 180,
      floor: 'Rez-de-chaussée (Bâtiment A)',
      equipment: ['Vidéoprojecteur Laser 4K', 'Système Sonore Surround', '2 Micros HF', 'Visioconférence', 'Climatisation'],
      status: 'DISPONIBLE'
    },
    {
      id: 'RM-102',
      code: 'LAB-INF',
      name: 'Labo Informatique Avancée (B2-04)',
      type: 'Labo Informatique',
      capacity: 35,
      floor: '2ème Étage (Bâtiment B)',
      equipment: ['35 Postes i7 32GB RAM', 'Double Écran', 'Fibre 1Gbps Dédiée', 'Tableau Interactif', 'Climatisation'],
      status: 'OCCUPE',
      currentBooking: 'Cours INF-301 - Dr. Tarek Louati (08:30 - 11:30)'
    },
    {
      id: 'RM-103',
      code: 'LAB-IA',
      name: 'Labo Intelligence Artificielle & Data',
      type: 'Labo Informatique',
      capacity: 30,
      floor: '2ème Étage (Bâtiment B)',
      equipment: ['30 Postes avec GPU Haute Performance', 'Serveur Local', 'Projecteur interactif', 'Caméra HD'],
      status: 'DISPONIBLE'
    },
    {
      id: 'RM-104',
      code: 'SAL-204',
      name: 'Salle d\'Examen & Séminaire 204',
      type: 'Salle de Cours',
      capacity: 50,
      floor: '1er Étage (Bâtiment A)',
      equipment: ['Projecteur Full HD', 'Tableau Blanc magnétique', 'Chaises espacées pour examen'],
      status: 'DISPONIBLE'
    },
    {
      id: 'RM-105',
      code: 'CONF-IIT',
      name: 'Salle du Conseil & Conférence IIT',
      type: 'Salle de Conférence',
      capacity: 25,
      floor: '3ème Étage (Direction)',
      equipment: ['Table de conférence ovale', 'Écran géant tactile', 'Système Visioconférence Haute Définition', 'Micros de table'],
      status: 'DISPONIBLE'
    }
  ]);
  public rooms$ = this.roomsSubject.asObservable();

  // Reservations
  private reservationsSubject = new BehaviorSubject<RoomReservation[]>([
    {
      id: 'RES-801',
      roomId: 'RM-102',
      roomName: 'Labo Informatique Avancée (B2-04)',
      bookedBy: 'Dr. Tarek Louati',
      role: 'Enseignant',
      purpose: 'Séance pratique de développement logiciel',
      date: '2026-11-26',
      startTime: '08:30',
      endTime: '11:30',
      status: 'EN_COURS'
    },
    {
      id: 'RES-802',
      roomId: 'RM-101',
      roomName: 'Amphithéâtre Ibn Khaldoun',
      bookedBy: 'Prof. Anis Fakhfakh',
      role: 'Enseignant',
      purpose: 'Conférence universitaire sur l\'intelligence artificielle',
      date: '2026-11-27',
      startTime: '14:00',
      endTime: '17:30',
      status: 'CONFIRME'
    },
    {
      id: 'RES-803',
      roomId: 'RM-104',
      roomName: 'Salle d\'Examen & Séminaire 204',
      bookedBy: 'Direction des Études IIT',
      role: 'Administration',
      purpose: 'Session d\'examen semestrielle',
      date: '2026-11-28',
      startTime: '09:00',
      endTime: '11:00',
      status: 'CONFIRME'
    }
  ]);
  public reservations$ = this.reservationsSubject.asObservable();

  // Exams
  private examsSubject = new BehaviorSubject<Exam[]>([
    {
      id: 'EXM-01',
      code: 'EX-INF-2026',
      title: 'Examen : Conception et Développement Logiciel',
      courseCode: 'INF-301',
      date: '2026-11-28',
      startTime: '09:00',
      durationMinutes: 45,
      room: 'Salle d\'Examen 204 & En Ligne',
      supervisor: 'Dr. Tarek Louati',
      totalQuestions: 5,
      status: 'A_VENIR',
      avgScore: 16.4
    },
    {
      id: 'EXM-02',
      code: 'EX-CLD-2026',
      title: 'Évaluation Continue : Déploiement & Cloud',
      courseCode: 'CLD-302',
      date: '2026-12-02',
      startTime: '10:30',
      durationMinutes: 60,
      room: 'Labo Informatique (B2-04)',
      supervisor: 'Ing. Slim Rekik',
      totalQuestions: 20,
      status: 'A_VENIR'
    },
    {
      id: 'EXM-03',
      code: 'EX-DAT-2026',
      title: 'Examen : Bases de Données & Modélisation',
      courseCode: 'DAT-303',
      date: '2026-11-20',
      startTime: '14:00',
      durationMinutes: 90,
      room: 'Amphithéâtre Ibn Khaldoun',
      supervisor: 'Dr. Sonia Mezghani',
      totalQuestions: 25,
      status: 'TERMINE',
      avgScore: 15.2
    }
  ]);
  public exams$ = this.examsSubject.asObservable();

  // Academic Exam Questions
  public examQuestions: ExamQuestion[] = [
    {
      id: 1,
      text: 'Quel est l\'objectif principal d\'une plateforme universitaire centralisée comme EduCampus ?',
      options: [
        'Séparer les bases de données et multiplier les démarches papier manuelles',
        'Simplifier et centraliser la gestion académique, les examens en ligne et la réservation intelligente des salles',
        'Remplacer les professeurs par des logiciels autonomes',
        'Supprimer les emplois du temps et les plannings de cours'
      ],
      correctAnswer: 1,
      theme: 'Pédagogie & Centralisation',
      explanation: 'EduCampus centralise les services essentiels (étudiants, cours, examens, salles) afin d\'éviter les conflits et de fluidifier le travail administratif et enseignant.'
    },
    {
      id: 2,
      text: 'Comment la réservation intelligente des salles (Smart Space) évite-t-elle les conflits d\'occupation ?',
      options: [
        'En réservant au hasard selon le premier arrivé',
        'Grâce à la vérification instantanée des créneaux horaires, capacités et disponibilités en temps réel',
        'En fermant toutes les salles aux enseignants',
        'En affichant les réservations uniquement sur un tableau d\'affichage physique'
      ],
      correctAnswer: 1,
      theme: 'Gestion des Espaces',
      explanation: 'Le service vérifie immédiatement la disponibilité des salles et bloque instantanément tout double créneau.'
    },
    {
      id: 3,
      text: 'Quelle est la responsabilité du service administratif universitaire ?',
      options: [
        'Uniquement surveiller les parkings du campus',
        'Vérifier le statut officiel de l\'étudiant, valider son éligibilité (quitus, paiements) et délivrer les certifications officielles',
        'Rédiger les sujets de cours des enseignants',
        'Supprimer les notes des étudiants après chaque semestre'
      ],
      correctAnswer: 1,
      theme: 'Administration & Scolarité',
      explanation: 'Le pôle administratif valide les conditions d\'éligibilité et délivre les diplômes, attestations de réussite et certificats de scolarité authentifiés.'
    },
    {
      id: 4,
      text: 'Quel avantage apporte la correction automatique des examens en ligne ?',
      options: [
        'Retarder la publication des notes de plusieurs mois',
        'Fournir un résultat et une note instantanés à l\'étudiant tout en transmettant la progression à la scolarité',
        'Interdire l\'accès aux corrigés pédagogiques',
        'Obliger les étudiants à recopier leurs réponses à la main'
      ],
      correctAnswer: 1,
      theme: 'Examens en Ligne',
      explanation: 'La correction automatique permet un calcul immédiat de la note et une transmission instantanée des résultats à l\'administration.'
    },
    {
      id: 5,
      text: 'Quelles informations essentielles figurent sur une attestation officielle délivrée par l\'IIT ?',
      options: [
        'Seulement le prénom de l\'étudiant sans aucune signature',
        'Identité de l\'étudiant (CIN, matricule), filière, année universitaire, cachet officiel et signature de la direction',
        'La liste de courses de la cafétéria',
        'Uniquement le plan d\'accès de la ville'
      ],
      correctAnswer: 1,
      theme: 'Certification & Documents Officiels',
      explanation: 'Une attestation officielle requiert l\'identification complète de l\'étudiant, sa promotion, ainsi que les éléments d\'authentification légaux (cachet et signature).'
    }
  ];

  // Official Certificate Requests
  private certificatesSubject = new BehaviorSubject<OfficialCertificateRequest[]>([
    {
      id: 'CERT-2026-091',
      referenceNumber: 'IIT/SCOL/2026/0412',
      studentId: 'STD-101',
      studentName: 'Youssef Ben Amor',
      studentCin: '09876543',
      matricule: 'IIT-2024-0012',
      branch: 'Génie Informatique (GI)',
      level: '3ème Année Ingénieur',
      documentType: 'Certificat de Scolarité',
      dateRequested: '2026-11-25',
      status: 'DÉLIVRÉ',
      academicYear: '2025-2026',
      deliveryFormat: 'PDF Officiel avec Signature & Cachet',
      qrVerificationUrl: 'https://iit.tn/verify/cert/0412-2026'
    },
    {
      id: 'CERT-2026-092',
      referenceNumber: 'IIT/SCOL/2026/0413',
      studentId: 'STD-102',
      studentName: 'Sarra Triki',
      studentCin: '11223344',
      matricule: 'IIT-2024-0045',
      branch: 'Génie Informatique (GI)',
      level: '3ème Année Ingénieur',
      documentType: 'Attestation de Réussite',
      dateRequested: '2026-11-26',
      status: 'VALIDÉ',
      academicYear: '2025-2026',
      deliveryFormat: 'PDF Officiel avec Signature & Cachet',
      qrVerificationUrl: 'https://iit.tn/verify/cert/0413-2026'
    },
    {
      id: 'CERT-2026-093',
      referenceNumber: 'IIT/SCOL/2026/0414',
      studentId: 'STD-103',
      studentName: 'Ahmed Karray',
      studentCin: '08765432',
      matricule: 'IIT-2024-0089',
      branch: 'Génie Télécoms & Réseaux (TR)',
      level: '2ème Année Ingénieur',
      documentType: 'Relevé de Notes Officiel',
      dateRequested: '2026-11-26',
      status: 'EN ATTENTE',
      academicYear: '2025-2026',
      deliveryFormat: 'Version Papier',
      qrVerificationUrl: 'https://iit.tn/verify/cert/0414-2026'
    }
  ]);
  public certificates$ = this.certificatesSubject.asObservable();

  constructor() {}

  public setRole(role: UserRole): void {
    this.currentRoleSubject.next(role);
  }

  public getRole(): UserRole {
    return this.currentRoleSubject.value;
  }

  public addStudent(studentData: Partial<Student>): void {
    const current = this.studentsSubject.value;
    const newStudent: Student = {
      id: `STD-${100 + current.length + 1}`,
      matricule: `IIT-2024-0${150 + current.length}`,
      cin: studentData.cin || '12345678',
      firstName: studentData.firstName || 'Nouvel',
      lastName: studentData.lastName || 'Étudiant',
      email: studentData.email || 'etudiant@iit.tn',
      phone: studentData.phone || '+216 20 000 000',
      branch: studentData.branch || 'Génie Informatique (GI)',
      level: studentData.level || '1ère Année Ingénieur',
      status: 'Inscrit',
      gpa: 14.5,
      creditsAcquired: 30,
      eligibilityStatus: 'VALIDE',
      eligibilityReason: 'Dossier complet, inscription validée par la scolarité',
      photoUrl: 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150&auto=format&fit=crop&q=80'
    };

    this.studentsSubject.next([newStudent, ...current]);
  }

  public toggleCourseEnrollment(courseId: string): void {
    const list = this.coursesSubject.value.map(c => {
      if (c.id === courseId) {
        const isEnrolled = !c.isEnrolled;
        const count = isEnrolled ? c.enrolledCount + 1 : Math.max(0, c.enrolledCount - 1);
        return { ...c, isEnrolled, enrolledCount: count };
      }
      return c;
    });
    this.coursesSubject.next(list);
  }

  public reserveRoom(roomId: string, purpose: string, startTime: string, endTime: string, bookedBy: string, role: 'Enseignant' | 'Administration' | 'Club Étudiant'): boolean {
    const rooms = this.roomsSubject.value;
    const room = rooms.find(r => r.id === roomId);
    if (!room) return false;

    const updatedRooms = rooms.map(r => {
      if (r.id === roomId) {
        return {
          ...r,
          status: 'OCCUPE' as const,
          currentBooking: `${purpose} - ${bookedBy} (${startTime} - ${endTime})`
        };
      }
      return r;
    });
    this.roomsSubject.next(updatedRooms);

    const newReservation: RoomReservation = {
      id: `RES-${Math.floor(Math.random() * 900 + 100)}`,
      roomId: room.id,
      roomName: room.name,
      bookedBy,
      role,
      purpose,
      date: new Date().toISOString().split('T')[0],
      startTime,
      endTime,
      status: 'CONFIRME'
    };

    this.reservationsSubject.next([newReservation, ...this.reservationsSubject.value]);
    return true;
  }

  public submitExam(answers: { [questionId: number]: number }): ExamSubmissionResult {
    let correctCount = 0;
    const total = this.examQuestions.length;

    this.examQuestions.forEach(q => {
      if (answers[q.id] === q.correctAnswer) {
        correctCount++;
      }
    });

    const scoreOutOf20 = Number(((correctCount / total) * 20).toFixed(1));
    const percentage = Math.round((correctCount / total) * 100);

    return {
      examId: 'EXM-01',
      examTitle: 'Examen : Conception et Développement Logiciel',
      studentName: 'Youssef Ben Amor',
      matricule: 'IIT-2024-0012',
      totalQuestions: total,
      correctAnswers: correctCount,
      score: scoreOutOf20,
      percentage,
      status: scoreOutOf20 >= 10 ? 'Réussi' : 'Échoué',
      submittedAt: new Date().toLocaleTimeString('fr-FR')
    };
  }

  public requestCertificate(
    studentCin: string,
    documentType: 'Attestation de Réussite' | 'Certificat de Scolarité' | 'Relevé de Notes Officiel' | 'Attestation d\'Inscription',
    deliveryFormat: 'PDF Officiel avec Signature & Cachet' | 'Version Papier'
  ): OfficialCertificateRequest {
    const student = this.studentsSubject.value.find(s => s.cin === studentCin) || this.studentsSubject.value[0];
    const randNum = Math.floor(Math.random() * 900 + 420);
    const ref = `IIT/SCOL/2026/${randNum}`;

    const newReq: OfficialCertificateRequest = {
      id: `CERT-2026-${randNum}`,
      referenceNumber: ref,
      studentId: student.id,
      studentName: `${student.firstName} ${student.lastName}`,
      studentCin: student.cin,
      matricule: student.matricule,
      branch: student.branch,
      level: student.level,
      documentType,
      dateRequested: new Date().toISOString().split('T')[0],
      status: 'VALIDÉ',
      academicYear: '2025-2026',
      deliveryFormat,
      qrVerificationUrl: `https://iit.tn/verify/cert/${randNum}-2026`
    };

    this.certificatesSubject.next([newReq, ...this.certificatesSubject.value]);
    return newReq;
  }
}
