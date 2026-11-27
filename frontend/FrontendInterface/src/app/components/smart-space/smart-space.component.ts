import { Component, OnInit } from '@angular/core';
import { MockDataService } from '../../core/services/mock-data.service';
import { Room, RoomReservation, Exam, ExamQuestion, ExamSubmissionResult } from '../../core/models';

@Component({
  selector: 'app-smart-space',
  templateUrl: './smart-space.component.html',
  styleUrls: ['./smart-space.component.css']
})
export class SmartSpaceComponent implements OnInit {
  activeTab: 'rooms' | 'exams' | 'live-exam' | 'results' = 'rooms';

  rooms: Room[] = [];
  reservations: RoomReservation[] = [];
  exams: Exam[] = [];
  questions: ExamQuestion[] = [];

  // Filter
  selectedRoomType = 'ALL';
  selectedEquipment = 'ALL';

  // Booking Modal
  showBookingModal = false;
  selectedRoomForBooking: Room | null = null;
  bookingForm = {
    purpose: '',
    startTime: '14:00',
    endTime: '16:00',
    bookedBy: 'Dr. Tarek Louati',
    role: 'Enseignant' as const
  };

  // Live Exam Simulation
  userAnswers: { [questionId: number]: number } = {};
  currentQuestionIndex = 0;
  examSubmitted = false;
  examResult: ExamSubmissionResult | null = null;
  examTimerMinutes = 44;
  examTimerSeconds = 59;
  timerInterval: any = null;

  toastMessage: string | null = null;

  constructor(public mockService: MockDataService) {}

  ngOnInit(): void {
    this.mockService.rooms$.subscribe(r => this.rooms = r);
    this.mockService.reservations$.subscribe(res => this.reservations = res);
    this.mockService.exams$.subscribe(e => this.exams = e);
    this.questions = this.mockService.examQuestions;
  }

  get filteredRooms(): Room[] {
    return this.rooms.filter(r => {
      const matchType = this.selectedRoomType === 'ALL' || r.type === this.selectedRoomType;
      const matchEquip = this.selectedEquipment === 'ALL' || r.equipment.some(e => e.includes(this.selectedEquipment));
      return matchType && matchEquip;
    });
  }

  openBooking(room: Room): void {
    this.selectedRoomForBooking = room;
    this.showBookingModal = true;
  }

  closeBooking(): void {
    this.showBookingModal = false;
    this.selectedRoomForBooking = null;
  }

  confirmBooking(): void {
    if (!this.selectedRoomForBooking) return;
    if (!this.bookingForm.purpose) {
      alert('Veuillez préciser le motif de la réservation.');
      return;
    }

    const success = this.mockService.reserveRoom(
      this.selectedRoomForBooking.id,
      this.bookingForm.purpose,
      this.bookingForm.startTime,
      this.bookingForm.endTime,
      this.bookingForm.bookedBy,
      this.bookingForm.role
    );

    if (success) {
      this.showToast(`La salle ${this.selectedRoomForBooking.name} a été réservée avec succès !`);
      this.closeBooking();
    }
  }

  startExamSession(): void {
    this.activeTab = 'live-exam';
    this.currentQuestionIndex = 0;
    this.userAnswers = {};
    this.examSubmitted = false;
    this.examResult = null;
    this.startTimer();
  }

  selectAnswer(questionId: number, optionIndex: number): void {
    this.userAnswers[questionId] = optionIndex;
  }

  nextQuestion(): void {
    if (this.currentQuestionIndex < this.questions.length - 1) {
      this.currentQuestionIndex++;
    }
  }

  prevQuestion(): void {
    if (this.currentQuestionIndex > 0) {
      this.currentQuestionIndex--;
    }
  }

  submitExamNow(): void {
    if (Object.keys(this.userAnswers).length < this.questions.length) {
      if (!confirm('Vous n\'avez pas répondu à toutes les questions. Souhaitez-vous quand même valider votre épreuve ?')) {
        return;
      }
    }

    this.examResult = this.mockService.submitExam(this.userAnswers);
    this.examSubmitted = true;
    this.activeTab = 'results';
    this.stopTimer();
    this.showToast(`Épreuve corrigée automatiquement ! Votre note est de ${this.examResult.score}/20.`);
  }

  startTimer(): void {
    this.stopTimer();
    this.timerInterval = setInterval(() => {
      if (this.examTimerSeconds > 0) {
        this.examTimerSeconds--;
      } else {
        if (this.examTimerMinutes > 0) {
          this.examTimerMinutes--;
          this.examTimerSeconds = 59;
        } else {
          this.submitExamNow();
        }
      }
    }, 1000);
  }

  stopTimer(): void {
    if (this.timerInterval) {
      clearInterval(this.timerInterval);
      this.timerInterval = null;
    }
  }

  showToast(msg: string): void {
    this.toastMessage = msg;
    setTimeout(() => {
      this.toastMessage = null;
    }, 3500);
  }
}
