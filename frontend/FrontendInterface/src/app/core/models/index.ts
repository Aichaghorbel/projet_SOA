export interface Student {
  id: string;
  matricule: string;
  cin: string;
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  branch: string;
  level: string;
  status: 'Inscrit' | 'En attente' | 'Diplômé';
  gpa: number;
  creditsAcquired: number;
  eligibilityStatus: 'VALIDE' | 'EN_ATTENTE' | 'SUSPENDU';
  eligibilityReason?: string;
  photoUrl: string;
}

export interface Course {
  id: string;
  code: string;
  title: string;
  credits: number;
  department: string;
  semester: string;
  teacher: string;
  room: string;
  enrolledCount: number;
  maxCapacity: number;
  scheduleTime: string;
  description: string;
  isEnrolled?: boolean;
}

export interface TimetableEntry {
  id: string;
  day: 'Lundi' | 'Mardi' | 'Mercredi' | 'Jeudi' | 'Vendredi' | 'Samedi';
  timeSlot: string;
  courseCode: string;
  courseTitle: string;
  teacher: string;
  room: string;
  group: string;
  type: 'Cours' | 'TD' | 'TP';
  color: string;
}

export interface GradeRecord {
  id: string;
  studentId: string;
  studentName: string;
  matricule: string;
  courseCode: string;
  courseTitle: string;
  examScore: number;
  dsScore: number;
  tpScore?: number;
  finalScore: number;
  status: 'Admis' | 'Rattrapage' | 'Non Validé';
  session: 'Principale' | 'Rattrapage';
}

export interface Room {
  id: string;
  code: string;
  name: string;
  type: 'Amphithéâtre' | 'Labo Informatique' | 'Salle de Cours' | 'Salle de Conférence';
  capacity: number;
  floor: string;
  equipment: string[];
  status: 'DISPONIBLE' | 'OCCUPE';
  currentBooking?: string;
}

export interface RoomReservation {
  id: string;
  roomId: string;
  roomName: string;
  bookedBy: string;
  role: 'Enseignant' | 'Administration' | 'Club Étudiant';
  purpose: string;
  date: string;
  startTime: string;
  endTime: string;
  status: 'CONFIRME' | 'EN_COURS';
}

export interface Exam {
  id: string;
  code: string;
  title: string;
  courseCode: string;
  date: string;
  startTime: string;
  durationMinutes: number;
  room: string;
  supervisor: string;
  totalQuestions: number;
  status: 'A_VENIR' | 'EN_COURS' | 'TERMINE';
  avgScore?: number;
}

export interface ExamQuestion {
  id: number;
  text: string;
  options: string[];
  correctAnswer: number;
  selectedAnswer?: number;
  theme: string;
  explanation: string;
}

export interface ExamSubmissionResult {
  examId: string;
  examTitle: string;
  studentName: string;
  matricule: string;
  totalQuestions: number;
  correctAnswers: number;
  score: number; // out of 20
  percentage: number;
  status: 'Réussi' | 'Échoué';
  submittedAt: string;
}

export interface OfficialCertificateRequest {
  id: string;
  referenceNumber: string;
  studentId: string;
  studentName: string;
  studentCin: string;
  matricule: string;
  branch: string;
  level: string;
  documentType: 'Attestation de Réussite' | 'Certificat de Scolarité' | 'Relevé de Notes Officiel' | 'Attestation d\'Inscription';
  dateRequested: string;
  status: 'DÉLIVRÉ' | 'VALIDÉ' | 'EN ATTENTE';
  academicYear: string;
  deliveryFormat: 'PDF Officiel avec Signature & Cachet' | 'Version Papier';
  qrVerificationUrl: string;
}

export type UserRole = 'student' | 'teacher' | 'admin';
