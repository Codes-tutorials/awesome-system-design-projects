import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterModule, ActivatedRoute } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { LoadingService } from '../../../core/services/loading.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  template: `
    <div class="auth-container">
      <div class="container">
        <div class="row justify-content-center">
          <div class="col-lg-5 col-md-7">
            <div class="auth-card">
              <div class="auth-header text-center mb-4">
                <div class="auth-logo mb-3">
                  <i class="fas fa-cricket-ball"></i>
                </div>
                <h2 class="auth-title">Welcome Back!</h2>
                <p class="auth-subtitle">Sign in to book your IPL tickets</p>
              </div>

              <form [formGroup]="loginForm" (ngSubmit)="onSubmit()" class="auth-form">
                <div class="mb-3">
                  <label for="email" class="form-label">Email Address</label>
                  <div class="input-group">
                    <span class="input-group-text">
                      <i class="fas fa-envelope"></i>
                    </span>
                    <input
                      type="email"
                      id="email"
                      class="form-control"
                      formControlName="email"
                      placeholder="Enter your email"
                      [class.is-invalid]="isFieldInvalid('email')"
                    >
                  </div>
                  <div class="invalid-feedback" *ngIf="isFieldInvalid('email')">
                    <small *ngIf="loginForm.get('email')?.errors?.['required']">
                      Email is required
                    </small>
                    <small *ngIf="loginForm.get('email')?.errors?.['email']">
                      Please enter a valid email
                    </small>
                  </div>
                </div>

                <div class="mb-3">
                  <label for="password" class="form-label">Password</label>
                  <div class="input-group">
                    <span class="input-group-text">
                      <i class="fas fa-lock"></i>
                    </span>
                    <input
                      [type]="showPassword ? 'text' : 'password'"
                      id="password"
                      class="form-control"
                      formControlName="password"
                      placeholder="Enter your password"
                      [class.is-invalid]="isFieldInvalid('password')"
                    >
                    <button
                      type="button"
                      class="btn btn-outline-secondary"
                      (click)="togglePassword()"
                    >
                      <i [class]="showPassword ? 'fas fa-eye-slash' : 'fas fa-eye'"></i>
                    </button>
                  </div>
                  <div class="invalid-feedback" *ngIf="isFieldInvalid('password')">
                    <small *ngIf="loginForm.get('password')?.errors?.['required']">
                      Password is required
                    </small>
                    <small *ngIf="loginForm.get('password')?.errors?.['minlength']">
                      Password must be at least 6 characters
                    </small>
                  </div>
                </div>

                <div class="mb-3 d-flex justify-content-between align-items-center">
                  <div class="form-check">
                    <input
                      type="checkbox"
                      id="rememberMe"
                      class="form-check-input"
                      formControlName="rememberMe"
                    >
                    <label for="rememberMe" class="form-check-label">
                      Remember me
                    </label>
                  </div>
                  <a routerLink="/auth/forgot-password" class="text-decoration-none">
                    Forgot Password?
                  </a>
                </div>

                <div class="alert alert-danger" *ngIf="errorMessage">
                  <i class="fas fa-exclamation-circle me-2"></i>
                  {{ errorMessage }}
                </div>

                <button
                  type="submit"
                  class="btn btn-primary w-100 mb-3"
                  [disabled]="loginForm.invalid || (isLoading$ | async)"
                >
                  <span *ngIf="isLoading$ | async" class="spinner-border spinner-border-sm me-2"></span>
                  <i class="fas fa-sign-in-alt me-2" *ngIf="!(isLoading$ | async)"></i>
                  {{ (isLoading$ | async) ? 'Signing In...' : 'Sign In' }}
                </button>

                <div class="text-center">
                  <p class="mb-0">
                    Don't have an account?
                    <a routerLink="/auth/register" class="text-decoration-none fw-bold">
                      Sign Up
                    </a>
                  </p>
                </div>
              </form>

              <div class="auth-divider">
                <span>or</span>
              </div>

              <div class="social-login">
                <button type="button" class="btn btn-outline-danger w-100 mb-2">
                  <i class="fab fa-google me-2"></i>
                  Continue with Google
                </button>
                <button type="button" class="btn btn-outline-primary w-100">
                  <i class="fab fa-facebook-f me-2"></i>
                  Continue with Facebook
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .auth-container {
      min-height: 100vh;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      display: flex;
      align-items: center;
      padding: 2rem 0;
    }

    .auth-card {
      background: white;
      border-radius: 15px;
      padding: 2.5rem;
      box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
      backdrop-filter: blur(10px);
    }

    .auth-logo {
      font-size: 3rem;
      color: var(--primary-color);
      animation: bounce 2s infinite;
    }

    .auth-title {
      color: var(--dark-color);
      font-weight: 700;
      margin-bottom: 0.5rem;
    }

    .auth-subtitle {
      color: #6c757d;
      margin-bottom: 0;
    }

    .input-group-text {
      background-color: #f8f9fa;
      border-right: none;
    }

    .form-control {
      border-left: none;
    }

    .form-control:focus {
      box-shadow: none;
      border-color: var(--primary-color);
    }

    .form-control:focus + .input-group-text {
      border-color: var(--primary-color);
    }

    .btn-primary {
      background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
      border: none;
      padding: 12px;
      font-weight: 600;
      border-radius: 8px;
      transition: all 0.3s ease;
    }

    .btn-primary:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
    }

    .auth-divider {
      text-align: center;
      margin: 1.5rem 0;
      position: relative;
    }

    .auth-divider::before {
      content: '';
      position: absolute;
      top: 50%;
      left: 0;
      right: 0;
      height: 1px;
      background: #dee2e6;
    }

    .auth-divider span {
      background: white;
      padding: 0 1rem;
      color: #6c757d;
      font-size: 0.9rem;
    }

    .social-login .btn {
      border-radius: 8px;
      padding: 12px;
      font-weight: 500;
      transition: all 0.3s ease;
    }

    .social-login .btn:hover {
      transform: translateY(-2px);
    }

    @keyframes bounce {
      0%, 20%, 50%, 80%, 100% {
        transform: translateY(0);
      }
      40% {
        transform: translateY(-10px);
      }
      60% {
        transform: translateY(-5px);
      }
    }

    @media (max-width: 768px) {
      .auth-card {
        margin: 1rem;
        padding: 2rem;
      }
    }
  `]
})
export class LoginComponent implements OnInit {
  loginForm: FormGroup;
  showPassword = false;
  errorMessage = '';
  returnUrl = '';
  isLoading$ = this.loadingService.isLoading$;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private loadingService: LoadingService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      rememberMe: [false]
    });
  }

  ngOnInit(): void {
    this.returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/matches';
    
    // Clear any previous errors
    this.authService.clearError();
  }

  onSubmit(): void {
    if (this.loginForm.valid) {
      this.errorMessage = '';
      const { email, password } = this.loginForm.value;
      
      this.authService.login({ email, password }).subscribe({
        next: (response) => {
          this.router.navigate([this.returnUrl]);
        },
        error: (error) => {
          this.errorMessage = error.message || 'Login failed. Please try again.';
        }
      });
    } else {
      this.markFormGroupTouched();
    }
  }

  togglePassword(): void {
    this.showPassword = !this.showPassword;
  }

  isFieldInvalid(fieldName: string): boolean {
    const field = this.loginForm.get(fieldName);
    return !!(field && field.invalid && (field.dirty || field.touched));
  }

  private markFormGroupTouched(): void {
    Object.keys(this.loginForm.controls).forEach(key => {
      const control = this.loginForm.get(key);
      control?.markAsTouched();
    });
  }
}