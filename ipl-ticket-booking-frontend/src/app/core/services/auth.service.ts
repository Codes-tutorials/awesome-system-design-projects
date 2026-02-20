import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, tap, map, catchError, of } from 'rxjs';
import { Router } from '@angular/router';
import { ApiService } from './api.service';
import { User, LoginRequest, LoginResponse, RegisterRequest, AuthState } from '../models/user.model';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly TOKEN_KEY = 'ipl_booking_token';
  private readonly USER_KEY = 'ipl_booking_user';
  
  private authStateSubject = new BehaviorSubject<AuthState>({
    user: null,
    token: null,
    isAuthenticated: false,
    isLoading: false,
    error: null
  });

  public authState$ = this.authStateSubject.asObservable();
  public isAuthenticated$ = this.authState$.pipe(map(state => state.isAuthenticated));
  public user$ = this.authState$.pipe(map(state => state.user));
  public isLoading$ = this.authState$.pipe(map(state => state.isLoading));

  constructor(
    private apiService: ApiService,
    private router: Router
  ) {
    this.initializeAuth();
  }

  /**
   * Initialize authentication state from localStorage
   */
  private initializeAuth(): void {
    const token = localStorage.getItem(this.TOKEN_KEY);
    const userStr = localStorage.getItem(this.USER_KEY);
    
    if (token && userStr) {
      try {
        const user = JSON.parse(userStr);
        this.updateAuthState({
          user,
          token,
          isAuthenticated: true,
          isLoading: false,
          error: null
        });
      } catch (error) {
        this.clearAuthData();
      }
    }
  }

  /**
   * Login user
   */
  login(credentials: LoginRequest): Observable<LoginResponse> {
    this.setLoading(true);
    
    return this.apiService.post<LoginResponse>('/auth/login', credentials)
      .pipe(
        tap(response => {
          this.setAuthData(response.token, response.user);
          this.updateAuthState({
            user: response.user,
            token: response.token,
            isAuthenticated: true,
            isLoading: false,
            error: null
          });
        }),
        catchError(error => {
          this.updateAuthState({
            user: null,
            token: null,
            isAuthenticated: false,
            isLoading: false,
            error: error.message
          });
          throw error;
        })
      );
  }

  /**
   * Register new user
   */
  register(userData: RegisterRequest): Observable<LoginResponse> {
    this.setLoading(true);
    
    return this.apiService.post<LoginResponse>('/auth/register', userData)
      .pipe(
        tap(response => {
          this.setAuthData(response.token, response.user);
          this.updateAuthState({
            user: response.user,
            token: response.token,
            isAuthenticated: true,
            isLoading: false,
            error: null
          });
        }),
        catchError(error => {
          this.updateAuthState({
            user: null,
            token: null,
            isAuthenticated: false,
            isLoading: false,
            error: error.message
          });
          throw error;
        })
      );
  }

  /**
   * Logout user
   */
  logout(): void {
    this.clearAuthData();
    this.updateAuthState({
      user: null,
      token: null,
      isAuthenticated: false,
      isLoading: false,
      error: null
    });
    this.router.navigate(['/auth/login']);
  }

  /**
   * Get current user
   */
  getCurrentUser(): Observable<User> {
    return this.apiService.get<User>('/auth/me')
      .pipe(
        tap(user => {
          this.updateAuthState({
            ...this.authStateSubject.value,
            user
          });
          localStorage.setItem(this.USER_KEY, JSON.stringify(user));
        })
      );
  }

  /**
   * Update user profile
   */
  updateProfile(userData: Partial<User>): Observable<User> {
    return this.apiService.put<User>('/auth/profile', userData)
      .pipe(
        tap(user => {
          this.updateAuthState({
            ...this.authStateSubject.value,
            user
          });
          localStorage.setItem(this.USER_KEY, JSON.stringify(user));
        })
      );
  }

  /**
   * Change password
   */
  changePassword(currentPassword: string, newPassword: string): Observable<any> {
    return this.apiService.post('/auth/change-password', {
      currentPassword,
      newPassword
    });
  }

  /**
   * Forgot password
   */
  forgotPassword(email: string): Observable<any> {
    return this.apiService.post('/auth/forgot-password', { email });
  }

  /**
   * Reset password
   */
  resetPassword(token: string, newPassword: string): Observable<any> {
    return this.apiService.post('/auth/reset-password', { token, newPassword });
  }

  /**
   * Verify email
   */
  verifyEmail(token: string): Observable<any> {
    return this.apiService.post('/auth/verify-email', { token });
  }

  /**
   * Resend verification email
   */
  resendVerificationEmail(): Observable<any> {
    return this.apiService.post('/auth/resend-verification', {});
  }

  /**
   * Check if user is authenticated
   */
  isAuthenticated(): boolean {
    return this.authStateSubject.value.isAuthenticated;
  }

  /**
   * Get current user synchronously
   */
  getCurrentUserSync(): User | null {
    return this.authStateSubject.value.user;
  }

  /**
   * Get auth token
   */
  getToken(): string | null {
    return this.authStateSubject.value.token;
  }

  /**
   * Set authentication data
   */
  private setAuthData(token: string, user: User): void {
    localStorage.setItem(this.TOKEN_KEY, token);
    localStorage.setItem(this.USER_KEY, JSON.stringify(user));
  }

  /**
   * Clear authentication data
   */
  private clearAuthData(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
  }

  /**
   * Update authentication state
   */
  private updateAuthState(state: AuthState): void {
    this.authStateSubject.next(state);
  }

  /**
   * Set loading state
   */
  private setLoading(loading: boolean): void {
    this.updateAuthState({
      ...this.authStateSubject.value,
      isLoading: loading
    });
  }

  /**
   * Clear error
   */
  clearError(): void {
    this.updateAuthState({
      ...this.authStateSubject.value,
      error: null
    });
  }
}