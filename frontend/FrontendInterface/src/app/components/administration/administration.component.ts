import { Component, OnInit } from '@angular/core';
import { MockDataService } from '../../core/services/mock-data.service';
import { Student, OfficialCertificateRequest } from '../../core/models';

@Component({
  selector: 'app-administration',
  templateUrl: './administration.component.html',
  styleUrls: ['./administration.component.css']
})
export class AdministrationComponent implements OnInit {
  activeTab: 'eligibility' | 'requests' | 'preview-doc' = 'eligibility';

  students: Student[] = [];
  certificates: OfficialCertificateRequest[] = [];

  // Eligibility verification state
  searchQuery = '09876543'; // Default to Youssef
  searchedStudent: Student | null = null;
  isVerifying = false;
  verificationReport: any = null;

  // New Request Modal
  showNewRequestModal = false;
  newRequestForm = {
    studentCin: '09876543',
    documentType: 'Certificat de Scolarité' as const,
    deliveryFormat: 'PDF Officiel avec Signature & Cachet' as const
  };

  // Preview Document state
  selectedCertificateForPreview: OfficialCertificateRequest | null = null;

  toastMessage: string | null = null;

  constructor(public mockService: MockDataService) {}

  ngOnInit(): void {
    this.mockService.students$.subscribe(s => {
      this.students = s;
      if (!this.searchedStudent && s.length > 0) {
        this.runEligibilityCheck(s[0].cin);
      }
    });

    this.mockService.certificates$.subscribe(c => {
      this.certificates = c;
      if (!this.selectedCertificateForPreview && c.length > 0) {
        this.selectedCertificateForPreview = c[0];
      }
    });
  }

  runEligibilityCheck(cinOrMatricule: string): void {
    this.isVerifying = true;
    setTimeout(() => {
      const found = this.students.find(
        s => s.cin === cinOrMatricule || s.matricule.toLowerCase() === cinOrMatricule.toLowerCase()
      ) || this.students[0];

      this.searchedStudent = found;
      this.verificationReport = {
        cin: found.cin,
        matricule: found.matricule,
        fullName: `${found.firstName} ${found.lastName}`,
        filiere: found.branch,
        tuitionPaid: true,
        libraryQuitus: true,
        disciplinaryClean: true,
        minCreditsMet: found.creditsAcquired >= 30,
        status: found.eligibilityStatus,
        refDossier: `DOSSIER-${found.matricule}`,
        dateAudit: new Date().toLocaleDateString('fr-FR')
      };
      this.isVerifying = false;
    }, 300);
  }

  previewDocument(cert: OfficialCertificateRequest): void {
    this.selectedCertificateForPreview = cert;
    this.activeTab = 'preview-doc';
  }

  openNewRequest(): void {
    this.showNewRequestModal = true;
  }

  closeNewRequest(): void {
    this.showNewRequestModal = false;
  }

  submitCertificateRequest(): void {
    const cert = this.mockService.requestCertificate(
      this.newRequestForm.studentCin,
      this.newRequestForm.documentType,
      this.newRequestForm.deliveryFormat
    );

    this.selectedCertificateForPreview = cert;
    this.showNewRequestModal = false;
    this.showToast(`Demande de ${cert.documentType} enregistrée avec succès ! Référence : ${cert.referenceNumber}`);
    this.activeTab = 'preview-doc';
  }

  printCertificate(): void {
    window.print();
  }

  showToast(msg: string): void {
    this.toastMessage = msg;
    setTimeout(() => {
      this.toastMessage = null;
    }, 4000);
  }
}
