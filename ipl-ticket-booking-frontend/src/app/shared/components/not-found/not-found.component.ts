import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-not-found',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="not-found-container">
      <div class="container">
        <div class="row justify-content-center">
          <div class="col-lg-6 col-md-8 text-center">
            <div class="error-content">
              <div class="error-icon mb-4">
                <i class="fas fa-exclamation-triangle"></i>
              </div>
              <h1 class="error-code">404</h1>
              <h2 class="error-title mb-3">Page Not Found</h2>
              <p class="error-message mb-4">
                Oops! The page you're looking for doesn't exist. 
                It might have been moved, deleted, or you entered the wrong URL.
              </p>
              <div class="error-actions">
                <a routerLink="/" class="btn btn-primary me-3">
                  <i class="fas fa-home me-2"></i>
                  Go Home
                </a>
                <button class="btn btn-outline-secondary" (click)="goBack()">
                  <i class="fas fa-arrow-left me-2"></i>
                  Go Back
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .not-found-container {
      min-height: 100vh;
      display: flex;
      align-items: center;
      justify-content: center;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
    }
    
    .error-content {
      padding: 2rem;
      background: rgba(255, 255, 255, 0.1);
      border-radius: 15px;
      backdrop-filter: blur(10px);
      box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
    }
    
    .error-icon {
      font-size: 4rem;
      color: #ffd700;
      animation: bounce 2s infinite;
    }
    
    .error-code {
      font-size: 6rem;
      font-weight: bold;
      margin: 0;
      text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.3);
    }
    
    .error-title {
      font-size: 2rem;
      font-weight: 600;
    }
    
    .error-message {
      font-size: 1.1rem;
      opacity: 0.9;
      line-height: 1.6;
    }
    
    .btn {
      padding: 12px 24px;
      font-weight: 500;
      border-radius: 25px;
      transition: all 0.3s ease;
    }
    
    .btn:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
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
      .error-code {
        font-size: 4rem;
      }
      
      .error-title {
        font-size: 1.5rem;
      }
      
      .error-actions {
        display: flex;
        flex-direction: column;
        gap: 1rem;
      }
      
      .btn {
        width: 100%;
      }
    }
  `]
})
export class NotFoundComponent {
  goBack(): void {
    window.history.back();
  }
}