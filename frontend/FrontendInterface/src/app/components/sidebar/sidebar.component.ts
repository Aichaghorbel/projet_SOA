import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { MockDataService } from '../../core/services/mock-data.service';
import { UserRole } from '../../core/models';

@Component({
  selector: 'app-sidebar',
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.css']
})
export class SidebarComponent implements OnInit {
  currentRole: UserRole = 'student';

  constructor(public mockService: MockDataService, public router: Router) {}

  ngOnInit(): void {
    this.mockService.currentRole$.subscribe(role => {
      this.currentRole = role;
    });
  }

  setRole(role: UserRole): void {
    this.mockService.setRole(role);
  }
}
