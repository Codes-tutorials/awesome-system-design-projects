import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MatchService } from '../services/match.service';
import { Match, MatchStatus } from '../../../core/models/match.model';
import { LoadingService } from '../../../core/services/loading.service';

@Component({
  selector: 'app-match-list',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  template: `
    <div class="matches-container">
      <div class="container py-4">
        <!-- Header Section -->
        <div class="matches-header mb-5">
          <div class="row align-items-center">
            <div class="col-lg-6">
              <h1 class="display-4 fw-bold text-primary mb-2">
                <i class="fas fa-cricket-ball me-3"></i>
                IPL Matches
              </h1>
              <p class="lead text-muted">
                Book tickets for the most exciting cricket matches
              </p>
            </div>
            <div class="col-lg-6 text-lg-end">
              <div class="match-stats">
                <div class="stat-item">
                  <span class="stat-number">{{ totalMatches }}</span>
                  <span class="stat-label">Total Matches</span>
                </div>
                <div class="stat-item">
                  <span class="stat-number">{{ upcomingMatches }}</span>
                  <span class="stat-label">Upcoming</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Filters Section -->
        <div class="filters-section mb-4">
          <div class="row">
            <div class="col-lg-3 col-md-6 mb-3">
              <label class="form-label">Search Matches</label>
              <div class="input-group">
                <span class="input-group-text">
                  <i class="fas fa-search"></i>
                </span>
                <input
                  type="text"
                  class="form-control"
                  placeholder="Search by team or venue..."
                  [(ngModel)]="searchQuery"
                  (input)="onSearch()"
                >
              </div>
            </div>
            <div class="col-lg-2 col-md-6 mb-3">
              <label class="form-label">Status</label>
              <select class="form-select" [(ngModel)]="selectedStatus" (change)="onFilterChange()">
                <option value="">All Matches</option>
                <option value="UPCOMING">Upcoming</option>
                <option value="LIVE">Live</option>
                <option value="COMPLETED">Completed</option>
              </select>
            </div>
            <div class="col-lg-2 col-md-6 mb-3">
              <label class="form-label">City</label>
              <select class="form-select" [(ngModel)]="selectedCity" (change)="onFilterChange()">
                <option value="">All Cities</option>
                <option *ngFor="let city of cities" [value]="city">{{ city }}</option>
              </select>
            </div>
            <div class="col-lg-3 col-md-6 mb-3">
              <label class="form-label">Date Range</label>
              <input
                type="date"
                class="form-control"
                [(ngModel)]="selectedDate"
                (change)="onFilterChange()"
              >
            </div>
            <div class="col-lg-2 col-md-12 mb-3">
              <label class="form-label">&nbsp;</label>
              <button class="btn btn-outline-secondary w-100" (click)="clearFilters()">
                <i class="fas fa-times me-2"></i>
                Clear
              </button>
            </div>
          </div>
        </div>

        <!-- Loading State -->
        <div class="text-center py-5" *ngIf="isLoading">
          <div class="spinner-border text-primary" role="status">
            <span class="visually-hidden">Loading matches...</span>
          </div>
          <p class="mt-3 text-muted">Loading matches...</p>
        </div>

        <!-- Matches Grid -->
        <div class="matches-grid" *ngIf="!isLoading">
          <div class="row" *ngIf="filteredMatches.length > 0">
            <div class="col-lg-6 col-xl-4 mb-4" *ngFor="let match of filteredMatches">
              <div class="match-card" [class.live]="match.status === 'LIVE'">
                <div class="match-header">
                  <div class="match-status" [ngClass]="getStatusClass(match.status)">
                    <i [class]="getStatusIcon(match.status)" class="me-1"></i>
                    {{ match.status }}
                  </div>
                  <div class="match-date">
                    {{ formatDate(match.matchDate) }}
                  </div>
                </div>

                <div class="match-teams">
                  <div class="team">
                    <img [src]="match.homeTeam.logo" [alt]="match.homeTeam.name" class="team-logo">
                    <div class="team-info">
                      <h6 class="team-name">{{ match.homeTeam.name }}</h6>
                      <small class="team-short">{{ match.homeTeam.shortName }}</small>
                    </div>
                  </div>
                  
                  <div class="vs-section">
                    <span class="vs-text">VS</span>
                    <div class="match-time">{{ match.matchTime }}</div>
                  </div>
                  
                  <div class="team">
                    <div class="team-info text-end">
                      <h6 class="team-name">{{ match.awayTeam.name }}</h6>
                      <small class="team-short">{{ match.awayTeam.shortName }}</small>
                    </div>
                    <img [src]="match.awayTeam.logo" [alt]="match.awayTeam.name" class="team-logo">
                  </div>
                </div>

                <div class="match-venue">
                  <i class="fas fa-map-marker-alt me-2"></i>
                  <strong>{{ match.stadium.name }}</strong>, {{ match.stadium.city }}
                </div>

                <div class="match-info">
                  <div class="info-item">
                    <i class="fas fa-chair me-2"></i>
                    <span>{{ match.availableSeats }}/{{ match.totalSeats }} seats available</span>
                  </div>
                  <div class="info-item">
                    <i class="fas fa-rupee-sign me-2"></i>
                    <span>₹{{ match.minPrice }} - ₹{{ match.maxPrice }}</span>
                  </div>
                </div>

                <div class="seat-availability">
                  <div class="availability-bar">
                    <div 
                      class="availability-fill" 
                      [style.width.%]="getAvailabilityPercentage(match)"
                    ></div>
                  </div>
                  <small class="availability-text">
                    {{ getAvailabilityPercentage(match) }}% available
                  </small>
                </div>

                <div class="match-actions">
                  <button 
                    class="btn btn-outline-primary me-2"
                    [routerLink]="['/matches', match.id]"
                  >
                    <i class="fas fa-info-circle me-2"></i>
                    View Details
                  </button>
                  <button 
                    class="btn btn-primary"
                    [routerLink]="['/matches', match.id, 'seats']"
                    [disabled]="match.availableSeats === 0 || match.status !== 'UPCOMING'"
                  >
                    <i class="fas fa-ticket-alt me-2"></i>
                    {{ match.availableSeats === 0 ? 'Sold Out' : 'Book Now' }}
                  </button>
                </div>
              </div>
            </div>
          </div>

          <!-- No Matches Found -->
          <div class="text-center py-5" *ngIf="filteredMatches.length === 0">
            <div class="no-matches">
              <i class="fas fa-search fa-3x text-muted mb-3"></i>
              <h4 class="text-muted">No matches found</h4>
              <p class="text-muted">
                Try adjusting your search criteria or check back later for new matches.
              </p>
              <button class="btn btn-primary" (click)="clearFilters()">
                <i class="fas fa-refresh me-2"></i>
                Show All Matches
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .matches-container {
      background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
      min-height: 100vh;
    }

    .matches-header {
      background: white;
      border-radius: 15px;
      padding: 2rem;
      box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
    }

    .match-stats {
      display: flex;
      gap: 2rem;
      justify-content: flex-end;
    }

    .stat-item {
      text-align: center;
    }

    .stat-number {
      display: block;
      font-size: 2rem;
      font-weight: bold;
      color: var(--primary-color);
    }

    .stat-label {
      font-size: 0.9rem;
      color: #6c757d;
    }

    .filters-section {
      background: white;
      border-radius: 10px;
      padding: 1.5rem;
      box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
    }

    .match-card {
      background: white;
      border-radius: 15px;
      padding: 1.5rem;
      box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
      transition: all 0.3s ease;
      height: 100%;
      display: flex;
      flex-direction: column;
    }

    .match-card:hover {
      transform: translateY(-5px);
      box-shadow: 0 8px 30px rgba(0, 0, 0, 0.15);
    }

    .match-card.live {
      border: 2px solid #dc3545;
      animation: pulse 2s infinite;
    }

    .match-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 1rem;
    }

    .match-status {
      padding: 0.25rem 0.75rem;
      border-radius: 20px;
      font-size: 0.8rem;
      font-weight: 600;
      text-transform: uppercase;
    }

    .match-status.upcoming {
      background: rgba(40, 167, 69, 0.1);
      color: #28a745;
    }

    .match-status.live {
      background: rgba(220, 53, 69, 0.1);
      color: #dc3545;
    }

    .match-status.completed {
      background: rgba(108, 117, 125, 0.1);
      color: #6c757d;
    }

    .match-date {
      font-size: 0.9rem;
      color: #6c757d;
      font-weight: 500;
    }

    .match-teams {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 1rem;
    }

    .team {
      display: flex;
      align-items: center;
      gap: 0.75rem;
      flex: 1;
    }

    .team-logo {
      width: 50px;
      height: 50px;
      border-radius: 50%;
      object-fit: cover;
      border: 2px solid #e9ecef;
    }

    .team-name {
      margin: 0;
      font-size: 0.9rem;
      font-weight: 600;
    }

    .team-short {
      color: #6c757d;
      font-weight: 500;
    }

    .vs-section {
      text-align: center;
      padding: 0 1rem;
    }

    .vs-text {
      display: block;
      font-weight: bold;
      color: var(--primary-color);
      font-size: 1.1rem;
    }

    .match-time {
      font-size: 0.8rem;
      color: #6c757d;
      margin-top: 0.25rem;
    }

    .match-venue {
      background: #f8f9fa;
      padding: 0.75rem;
      border-radius: 8px;
      margin-bottom: 1rem;
      font-size: 0.9rem;
    }

    .match-info {
      margin-bottom: 1rem;
    }

    .info-item {
      display: flex;
      align-items: center;
      margin-bottom: 0.5rem;
      font-size: 0.9rem;
      color: #6c757d;
    }

    .seat-availability {
      margin-bottom: 1.5rem;
    }

    .availability-bar {
      height: 6px;
      background: #e9ecef;
      border-radius: 3px;
      overflow: hidden;
      margin-bottom: 0.5rem;
    }

    .availability-fill {
      height: 100%;
      background: linear-gradient(90deg, #28a745, #20c997);
      transition: width 0.3s ease;
    }

    .availability-text {
      color: #6c757d;
      font-size: 0.8rem;
    }

    .match-actions {
      margin-top: auto;
      display: flex;
      gap: 0.5rem;
    }

    .match-actions .btn {
      flex: 1;
      border-radius: 8px;
      font-weight: 500;
    }

    .no-matches {
      padding: 3rem 2rem;
    }

    @keyframes pulse {
      0% {
        box-shadow: 0 4px 20px rgba(220, 53, 69, 0.3);
      }
      50% {
        box-shadow: 0 4px 20px rgba(220, 53, 69, 0.6);
      }
      100% {
        box-shadow: 0 4px 20px rgba(220, 53, 69, 0.3);
      }
    }

    @media (max-width: 768px) {
      .match-stats {
        justify-content: center;
        margin-top: 1rem;
      }

      .match-teams {
        flex-direction: column;
        gap: 1rem;
      }

      .vs-section {
        order: -1;
      }

      .team {
        width: 100%;
        justify-content: center;
      }

      .match-actions {
        flex-direction: column;
      }
    }
  `]
})
export class MatchListComponent implements OnInit {
  matches: Match[] = [];
  filteredMatches: Match[] = [];
  cities: string[] = [];
  
  searchQuery = '';
  selectedStatus = '';
  selectedCity = '';
  selectedDate = '';
  
  isLoading = false;
  totalMatches = 0;
  upcomingMatches = 0;

  constructor(
    private matchService: MatchService,
    private loadingService: LoadingService
  ) {}

  ngOnInit(): void {
    this.loadMatches();
  }

  loadMatches(): void {
    this.isLoading = true;
    this.loadingService.show();

    this.matchService.getMatches().subscribe({
      next: (matches) => {
        this.matches = matches;
        this.filteredMatches = matches;
        this.extractCities();
        this.calculateStats();
        this.isLoading = false;
        this.loadingService.hide();
      },
      error: (error) => {
        console.error('Error loading matches:', error);
        this.isLoading = false;
        this.loadingService.hide();
      }
    });
  }

  onSearch(): void {
    this.applyFilters();
  }

  onFilterChange(): void {
    this.applyFilters();
  }

  applyFilters(): void {
    this.filteredMatches = this.matches.filter(match => {
      const matchesSearch = !this.searchQuery || 
        match.homeTeam.name.toLowerCase().includes(this.searchQuery.toLowerCase()) ||
        match.awayTeam.name.toLowerCase().includes(this.searchQuery.toLowerCase()) ||
        match.stadium.name.toLowerCase().includes(this.searchQuery.toLowerCase()) ||
        match.stadium.city.toLowerCase().includes(this.searchQuery.toLowerCase());

      const matchesStatus = !this.selectedStatus || match.status === this.selectedStatus;
      const matchesCity = !this.selectedCity || match.stadium.city === this.selectedCity;
      
      const matchesDate = !this.selectedDate || 
        new Date(match.matchDate).toDateString() === new Date(this.selectedDate).toDateString();

      return matchesSearch && matchesStatus && matchesCity && matchesDate;
    });
  }

  clearFilters(): void {
    this.searchQuery = '';
    this.selectedStatus = '';
    this.selectedCity = '';
    this.selectedDate = '';
    this.filteredMatches = this.matches;
  }

  private extractCities(): void {
    const citySet = new Set(this.matches.map(match => match.stadium.city));
    this.cities = Array.from(citySet).sort();
  }

  private calculateStats(): void {
    this.totalMatches = this.matches.length;
    this.upcomingMatches = this.matches.filter(match => match.status === MatchStatus.UPCOMING).length;
  }

  formatDate(date: Date): string {
    return new Date(date).toLocaleDateString('en-US', {
      weekday: 'short',
      month: 'short',
      day: 'numeric'
    });
  }

  getStatusClass(status: string): string {
    return status.toLowerCase();
  }

  getStatusIcon(status: string): string {
    switch (status) {
      case MatchStatus.UPCOMING:
        return 'fas fa-clock';
      case MatchStatus.LIVE:
        return 'fas fa-circle';
      case MatchStatus.COMPLETED:
        return 'fas fa-check-circle';
      default:
        return 'fas fa-question-circle';
    }
  }

  getAvailabilityPercentage(match: Match): number {
    return Math.round((match.availableSeats / match.totalSeats) * 100);
  }
}