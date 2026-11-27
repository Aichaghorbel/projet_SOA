import { Component, OnInit } from '@angular/core';
import { MockDataService } from '../../core/services/mock-data.service';
import { Student, Course, TimetableEntry, GradeRecord } from '../../core/models';

@Component({
  selector: 'app-academic',
  templateUrl: './academic.component.html',
  styleUrls: ['./academic.component.css']
})
export class AcademicComponent implements OnInit {
  activeTab: 'students' | 'courses' | 'timetable' | 'grades' = 'students';

  students: Student[] = [];
  courses: Course[] = [];
  timetable: TimetableEntry[] = [];
  grades: GradeRecord[] = [];

  // Filter states
  studentSearch = '';
  selectedBranch = 'ALL';

  selectedDay = 'ALL';
  selectedGroup = '3ème Année GI - Groupe 1';

  // Modals
  showAddStudentModal = false;
  selectedStudentForDetail: Student | null = null;
  notificationMessage: string | null = null;

  // New Student Form Model
  newStudentData: Partial<Student> = {
    firstName: '',
    lastName: '',
    cin: '',
    email: '',
    phone: '',
    branch: 'Génie Informatique (GI)',
    level: '3ème Année Ingénieur'
  };

  constructor(public mockService: MockDataService) {}

  ngOnInit(): void {
    this.mockService.students$.subscribe(s => this.students = s);
    this.mockService.courses$.subscribe(c => this.courses = c);
    this.mockService.timetable$.subscribe(t => this.timetable = t);
    this.mockService.grades$.subscribe(g => this.grades = g);
  }

  get filteredStudents(): Student[] {
    return this.students.filter(s => {
      const matchSearch =
        this.studentSearch === '' ||
        (s.firstName + ' ' + s.lastName).toLowerCase().includes(this.studentSearch.toLowerCase()) ||
        s.matricule.toLowerCase().includes(this.studentSearch.toLowerCase()) ||
        s.cin.includes(this.studentSearch);

      const matchBranch = this.selectedBranch === 'ALL' || s.branch.includes(this.selectedBranch);
      return matchSearch && matchBranch;
    });
  }

  get filteredTimetable(): TimetableEntry[] {
    return this.timetable.filter(item => {
      const matchDay = this.selectedDay === 'ALL' || item.day === this.selectedDay;
      const matchGroup = this.selectedGroup === 'ALL' || item.group.includes(this.selectedGroup) || item.group.includes('Tous');
      return matchDay && matchGroup;
    });
  }

  toggleEnroll(courseId: string): void {
    this.mockService.toggleCourseEnrollment(courseId);
    this.showToast('Votre inscription au cours a été mise à jour avec succès.');
  }

  openAddStudent(): void {
    this.showAddStudentModal = true;
  }

  closeAddStudent(): void {
    this.showAddStudentModal = false;
  }

  saveStudent(): void {
    if (!this.newStudentData.firstName || !this.newStudentData.lastName) {
      alert('Veuillez renseigner le prénom et le nom de l\'étudiant.');
      return;
    }
    this.mockService.addStudent(this.newStudentData);
    this.newStudentData = {
      firstName: '',
      lastName: '',
      cin: '',
      email: '',
      phone: '',
      branch: 'Génie Informatique (GI)',
      level: '3ème Année Ingénieur'
    };
    this.showAddStudentModal = false;
    this.showToast('Étudiant inscrit avec succès dans le registre universitaire.');
  }

  viewStudent(student: Student): void {
    this.selectedStudentForDetail = student;
  }

  closeStudentDetail(): void {
    this.selectedStudentForDetail = null;
  }

  showToast(msg: string): void {
    this.notificationMessage = msg;
    setTimeout(() => {
      this.notificationMessage = null;
    }, 3500);
  }
}
