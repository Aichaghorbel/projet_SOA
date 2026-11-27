import { Component, OnInit } from '@angular/core';
import { MockDataService } from '../../core/services/mock-data.service';
import { UserRole, Course, TimetableEntry, Exam, OfficialCertificateRequest, GradeRecord, RoomReservation } from '../../core/models';

@Component({
  selector: 'app-portal',
  templateUrl: './portal.component.html',
  styleUrls: ['./portal.component.css']
})
export class PortalComponent implements OnInit {
  currentRole: UserRole = 'student';
  courses: Course[] = [];
  timetable: TimetableEntry[] = [];
  exams: Exam[] = [];
  certificates: OfficialCertificateRequest[] = [];
  grades: GradeRecord[] = [];
  reservations: RoomReservation[] = [];

  constructor(public mockService: MockDataService) {}

  ngOnInit(): void {
    this.mockService.currentRole$.subscribe(r => this.currentRole = r);
    this.mockService.courses$.subscribe(c => this.courses = c);
    this.mockService.timetable$.subscribe(t => this.timetable = t);
    this.mockService.exams$.subscribe(e => this.exams = e);
    this.mockService.certificates$.subscribe(cert => this.certificates = cert);
    this.mockService.grades$.subscribe(g => this.grades = g);
    this.mockService.reservations$.subscribe(res => this.reservations = res);
  }

  setRole(role: UserRole): void {
    this.mockService.setRole(role);
  }
}
