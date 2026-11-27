import { Component, OnInit } from '@angular/core';
import { MockDataService } from '../../core/services/mock-data.service';
import { Student, Course, Room, Exam, UserRole } from '../../core/models';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
  students: Student[] = [];
  courses: Course[] = [];
  rooms: Room[] = [];
  exams: Exam[] = [];
  currentRole: UserRole = 'student';

  recentActivities = [
    {
      time: 'Il y a 10 min',
      title: 'Inscription de cours validée',
      description: '38 étudiants inscrits au cours INF-301 (Génie Informatique)',
      icon: 'fa-graduation-cap',
      colorClass: 'icon-blue'
    },
    {
      time: 'Il y a 25 min',
      title: 'Salle de cours réservée',
      description: 'Labo Informatique (B2-04) réservé par Dr. Tarek Louati (08:30 - 11:30)',
      icon: 'fa-door-open',
      colorClass: 'icon-green'
    },
    {
      time: 'Il y a 1 heure',
      title: 'Attestation officielle délivrée',
      description: 'Certificat de scolarité émis et signé pour Youssef Ben Amor (IIT-2024-0012)',
      icon: 'fa-certificate',
      colorClass: 'icon-purple'
    },
    {
      time: 'Il y a 2 heures',
      title: 'Correction d\'examen effectuée',
      description: 'Résultats publiés pour la session d\'évaluation du semestre 1',
      icon: 'fa-laptop-code',
      colorClass: 'icon-gold'
    }
  ];

  constructor(public mockService: MockDataService) {}

  ngOnInit(): void {
    this.mockService.students$.subscribe(s => this.students = s);
    this.mockService.courses$.subscribe(c => this.courses = c);
    this.mockService.rooms$.subscribe(r => this.rooms = r);
    this.mockService.exams$.subscribe(e => this.exams = e);
    this.mockService.currentRole$.subscribe(r => this.currentRole = r);
  }

  get availableRoomsCount(): number {
    return this.rooms.filter(r => r.status === 'DISPONIBLE').length;
  }
}
