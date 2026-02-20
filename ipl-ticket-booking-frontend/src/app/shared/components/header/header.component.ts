import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { User } from '../../../core/models/user.model';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <nav class="navbar navbar-expand-lg navbar-dark bg-primary fixed-top">
      <div class="container">
        <a class="navbar-brand" routerLink="/">
          <i class="fas fa-cricket-ball me-2"></i>
          IPL Tickets
        </a>
        
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" 
                data-bs-target="#navbarNav" aria-controls="navbarNav" 
                aria-expanded="false" aria-label="Toggle navigation">
          <span class="navbar-toggler-icon"></span>
        </button>
        
        <div class="collapse navbar-collapse" id="navbarNav">
          <ul class="navbar-nav me-auto">
            <li class="nav-item">
              <a class="nav-link" routerLink="/matches" routerLinkActive="active">
                <i class="fas fa-calendar-alt me-1"></i>
                Matches
              </a>
            </li>
            <li class="nav-item" *ngIf="isAuthenticated$ | async">
              <a class="nav-link" routerLink="/booking/history" routerLinkActive="active">
                <i class="fas fa-history me-1"></i>
                My Bookings
              </a>
            </li>
          </ul>
          
          <ul class="navbar-nav">
            <ng-container *ngIf="isAuthenticated$ | async; else guestMenu">
              <li class="nav-item dropdown">
                <a class="nav-link dropdown-toggle" href="#" id="navbarDropdown" 
                   role="button" data-bs-toggle="dropdown" aria-expanded="false">
                  <i class="fas fa-user-circle me-1"></i>
                  {{ (user$ | async)?.firstName }}
                </a>
                <ul class="dropdown-menu">
                  <li>
                    <a class="dropdown-item" routerLink="/profile">
                      <i class="fas fa-user me-2"></i>
                      Profile
                    </a>
                  </li>
                  <li>
                    <a class="dropdown-item" routerLink="/profile/settings">
                      <i class="fas fa-cog me-2"></i>
                      Settings
                    </a>
                  </li>
                  <li><hr class="dropdown-divider"></li>
                  <li>
                    <a class="dropdown-item" href="#" (click)="logout($event)">
                      <i class="fas fa-sign-out-alt me-2"></i>
                      Logout
                    </a>
                  </li>
                </ul>
              </li>
            </ng-container>
            
            <ng-template #guestMenu>
              <li class="nav-item">
                <a class="nav-link" routerLink="/auth/login">
                  <i class="fas fa-sign-in-alt me-1"></i>
                  Login
                </a>
              </li>
              <li class="nav-item">
                <a class="btn btn-outline-light ms-2" routerLink="/auth/register">
                  <i class="fas fa-user-plus me-1"></i>
                  Sign Up
                </a>
              </li>
            </ng-template>
          </ul>
        </div>
      </div>
    </nav>
  `,
  styles: [`
    .navbar {
      box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
    }
    
    .navbar-brand {
      font-weight: bold;
      font-size: 1.5rem;
    }
    
    .nav-link {
      font-weight: 500;
      transition: all 0.3s ease;
    }
    
    .nav-link:hover {
      transform: translateY(-2px);
    }
    
    .nav-link.active {
      background-color: rgba(255, 255, 255, 0.1);
      border-radius: 5px;
    }
    
    .dropdown-menu {
      border: none;
      box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
      border-radius: 8px;
    }
    
    .dropdown-item {
      transition: all 0.3s ease;
    }
    
    .dropdown-item:hover {
      background-color: var(--primary-color);
      color: white;
      transform: translateX(5px);
    }
    
    @media (max-width: 768px) {
      .navbar-nav {
        text-align: center;
      }
      
      .btn {
        margin-top: 0.5rem;
      }
    }
  `]
})
export class HeaderComponent implements OnInit {
  isAuthenticated$: Observable<boolean>;
  user$: Observable<User | null>;

  constructor(private authService: AuthService) {
    this.isAuthenticated$ = this.authService.isAuthenticated$;
    this.user$ = this.authService.user$;
  }

  ngOnInit(): void {}

  logout(event: Event): void {
    event.preventDefault();
    this.authService.logout();
  }
}