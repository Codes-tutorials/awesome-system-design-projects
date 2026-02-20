import { Injectable } from '@angular/core';
import { Observable, BehaviorSubject } from 'rxjs';
import { ApiService } from '../../../core/services/api.service';
import { Match, Seat, SeatCategory } from '../../../core/models/match.model';

@Injectable({
  providedIn: 'root'
})
export class MatchService {
  private selectedSeatsSubject = new BehaviorSubject<Seat[]>([]);
  public selectedSeats$ = this.selectedSeatsSubject.asObservable();

  constructor(private apiService: ApiService) {}

  /**
   * Get all matches
   */
  getMatches(filters?: any): Observable<Match[]> {
    return this.apiService.get<Match[]>('/matches', filters);
  }

  /**
   * Get match by ID
   */
  getMatchById(id: number): Observable<Match> {
    return this.apiService.get<Match>(`/matches/${id}`);
  }

  /**
   * Get upcoming matches
   */
  getUpcomingMatches(): Observable<Match[]> {
    return this.apiService.get<Match[]>('/matches/upcoming');
  }

  /**
   * Get matches by team
   */
  getMatchesByTeam(teamId: number): Observable<Match[]> {
    return this.apiService.get<Match[]>(`/matches/team/${teamId}`);
  }

  /**
   * Get matches by stadium
   */
  getMatchesByStadium(stadiumId: number): Observable<Match[]> {
    return this.apiService.get<Match[]>(`/matches/stadium/${stadiumId}`);
  }

  /**
   * Get seat categories for a match
   */
  getSeatCategories(matchId: number): Observable<SeatCategory[]> {
    return this.apiService.get<SeatCategory[]>(`/matches/${matchId}/seat-categories`);
  }

  /**
   * Get seats for a match
   */
  getSeats(matchId: number, categoryId?: number): Observable<Seat[]> {
    const params = categoryId ? { categoryId } : {};
    return this.apiService.get<Seat[]>(`/matches/${matchId}/seats`, params);
  }

  /**
   * Get available seats for a match
   */
  getAvailableSeats(matchId: number): Observable<Seat[]> {
    return this.apiService.get<Seat[]>(`/matches/${matchId}/seats/available`);
  }

  /**
   * Lock seats temporarily
   */
  lockSeats(matchId: number, seatIds: number[]): Observable<any> {
    return this.apiService.post(`/matches/${matchId}/seats/lock`, { seatIds });
  }

  /**
   * Release locked seats
   */
  releaseSeatLocks(matchId: number, seatIds: number[]): Observable<any> {
    return this.apiService.post(`/matches/${matchId}/seats/release`, { seatIds });
  }

  /**
   * Search matches
   */
  searchMatches(query: string): Observable<Match[]> {
    return this.apiService.get<Match[]>('/matches/search', { q: query });
  }

  /**
   * Get match statistics
   */
  getMatchStats(matchId: number): Observable<any> {
    return this.apiService.get(`/matches/${matchId}/stats`);
  }

  /**
   * Selected seats management
   */
  addSelectedSeat(seat: Seat): void {
    const currentSeats = this.selectedSeatsSubject.value;
    if (!currentSeats.find(s => s.id === seat.id)) {
      seat.isSelected = true;
      this.selectedSeatsSubject.next([...currentSeats, seat]);
    }
  }

  removeSelectedSeat(seatId: number): void {
    const currentSeats = this.selectedSeatsSubject.value;
    const updatedSeats = currentSeats.filter(seat => {
      if (seat.id === seatId) {
        seat.isSelected = false;
        return false;
      }
      return true;
    });
    this.selectedSeatsSubject.next(updatedSeats);
  }

  clearSelectedSeats(): void {
    const currentSeats = this.selectedSeatsSubject.value;
    currentSeats.forEach(seat => seat.isSelected = false);
    this.selectedSeatsSubject.next([]);
  }

  getSelectedSeats(): Seat[] {
    return this.selectedSeatsSubject.value;
  }

  getTotalSelectedAmount(): number {
    return this.selectedSeatsSubject.value.reduce((total, seat) => total + seat.price, 0);
  }

  getSelectedSeatCount(): number {
    return this.selectedSeatsSubject.value.length;
  }
}