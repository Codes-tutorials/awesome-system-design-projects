import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-footer',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <footer class="bg-dark text-light py-5 mt-5">
      <div class="container">
        <div class="row">
          <div class="col-lg-4 col-md-6 mb-4">
            <h5 class="mb-3">
              <i class="fas fa-cricket-ball me-2"></i>
              IPL Tickets
            </h5>
            <p class="text-muted">
              Your trusted platform for booking IPL cricket match tickets. 
              Secure, fast, and reliable ticket booking experience.
            </p>
            <div class="social-links">
              <a href="#" class="text-light me-3">
                <i class="fab fa-facebook-f"></i>
              </a>
              <a href="#" class="text-light me-3">
                <i class="fab fa-twitter"></i>
              </a>
              <a href="#" class="text-light me-3">
                <i class="fab fa-instagram"></i>
              </a>
              <a href="#" class="text-light">
                <i class="fab fa-youtube"></i>
              </a>
            </div>
          </div>
          
          <div class="col-lg-2 col-md-6 mb-4">
            <h6 class="mb-3">Quick Links</h6>
            <ul class="list-unstyled">
              <li class="mb-2">
                <a routerLink="/matches" class="text-muted text-decoration-none">
                  Matches
                </a>
              </li>
              <li class="mb-2">
                <a routerLink="/teams" class="text-muted text-decoration-none">
                  Teams
                </a>
              </li>
              <li class="mb-2">
                <a routerLink="/stadiums" class="text-muted text-decoration-none">
                  Stadiums
                </a>
              </li>
              <li class="mb-2">
                <a routerLink="/schedule" class="text-muted text-decoration-none">
                  Schedule
                </a>
              </li>
            </ul>
          </div>
          
          <div class="col-lg-2 col-md-6 mb-4">
            <h6 class="mb-3">Support</h6>
            <ul class="list-unstyled">
              <li class="mb-2">
                <a href="#" class="text-muted text-decoration-none">
                  Help Center
                </a>
              </li>
              <li class="mb-2">
                <a href="#" class="text-muted text-decoration-none">
                  Contact Us
                </a>
              </li>
              <li class="mb-2">
                <a href="#" class="text-muted text-decoration-none">
                  FAQ
                </a>
              </li>
              <li class="mb-2">
                <a href="#" class="text-muted text-decoration-none">
                  Refund Policy
                </a>
              </li>
            </ul>
          </div>
          
          <div class="col-lg-4 col-md-6 mb-4">
            <h6 class="mb-3">Contact Info</h6>
            <div class="contact-info">
              <p class="text-muted mb-2">
                <i class="fas fa-envelope me-2"></i>
                support@ipltickets.com
              </p>
              <p class="text-muted mb-2">
                <i class="fas fa-phone me-2"></i>
                +91 1800-123-4567
              </p>
              <p class="text-muted mb-2">
                <i class="fas fa-clock me-2"></i>
                24/7 Customer Support
              </p>
            </div>
            
            <div class="newsletter mt-3">
              <h6 class="mb-2">Newsletter</h6>
              <div class="input-group">
                <input type="email" class="form-control" placeholder="Your email">
                <button class="btn btn-primary" type="button">
                  <i class="fas fa-paper-plane"></i>
                </button>
              </div>
            </div>
          </div>
        </div>
        
        <hr class="my-4">
        
        <div class="row align-items-center">
          <div class="col-md-6">
            <p class="text-muted mb-0">
              &copy; {{ currentYear }} IPL Tickets. All rights reserved.
            </p>
          </div>
          <div class="col-md-6 text-md-end">
            <a href="#" class="text-muted text-decoration-none me-3">Privacy Policy</a>
            <a href="#" class="text-muted text-decoration-none me-3">Terms of Service</a>
            <a href="#" class="text-muted text-decoration-none">Cookie Policy</a>
          </div>
        </div>
      </div>
    </footer>
  `,
  styles: [`
    footer {
      margin-top: auto;
    }
    
    .social-links a {
      display: inline-block;
      width: 40px;
      height: 40px;
      line-height: 40px;
      text-align: center;
      border-radius: 50%;
      background-color: rgba(255, 255, 255, 0.1);
      transition: all 0.3s ease;
    }
    
    .social-links a:hover {
      background-color: var(--primary-color);
      transform: translateY(-3px);
    }
    
    .list-unstyled a:hover {
      color: var(--primary-color) !important;
      padding-left: 5px;
      transition: all 0.3s ease;
    }
    
    .newsletter .input-group {
      max-width: 300px;
    }
    
    .newsletter .form-control {
      border-radius: 25px 0 0 25px;
      border: none;
    }
    
    .newsletter .btn {
      border-radius: 0 25px 25px 0;
      border: none;
    }
    
    @media (max-width: 768px) {
      .text-md-end {
        text-align: center !important;
        margin-top: 1rem;
      }
      
      .social-links {
        text-align: center;
        margin-top: 1rem;
      }
    }
  `]
})
export class FooterComponent {
  currentYear = new Date().getFullYear();
}