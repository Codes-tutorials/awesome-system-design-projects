# IPL Ticket Booking Frontend

A modern, responsive Angular application for booking IPL cricket match tickets. Built with Angular 17, Bootstrap 5, and designed to handle high-traffic scenarios with excellent user experience.

## 🎯 Features

### Core Features
- **Match Browsing**: View all IPL matches with advanced filtering
- **Seat Selection**: Interactive stadium layout with real-time seat availability
- **Secure Booking**: JWT-based authentication and secure payment processing
- **User Management**: Profile management, booking history, and preferences
- **Responsive Design**: Mobile-first design that works on all devices
- **Real-time Updates**: Live seat availability and match status updates

### Advanced Features
- **Progressive Web App (PWA)**: Offline support and app-like experience
- **Performance Optimized**: Lazy loading, code splitting, and optimized bundles
- **Accessibility**: WCAG 2.1 compliant with screen reader support
- **Internationalization**: Multi-language support (English, Hindi)
- **Dark Mode**: User preference-based theme switching

## 🏗️ Architecture

### Project Structure
```
src/
├── app/
│   ├── core/                 # Core services, guards, interceptors
│   │   ├── guards/          # Route guards (auth, role-based)
│   │   ├── interceptors/    # HTTP interceptors (auth, error, loading)
│   │   ├── models/          # TypeScript interfaces and types
│   │   └── services/        # Core services (API, auth, loading)
│   ├── features/            # Feature modules (lazy-loaded)
│   │   ├── auth/           # Authentication (login, register, forgot password)
│   │   ├── matches/        # Match listing, details, seat selection
│   │   ├── booking/        # Booking process, payment, confirmation
│   │   └── profile/        # User profile, settings, booking history
│   ├── shared/             # Shared components, pipes, directives
│   │   ├── components/     # Reusable UI components
│   │   ├── pipes/          # Custom pipes
│   │   └── directives/     # Custom directives
│   └── app.component.ts    # Root component
├── assets/                 # Static assets (images, icons, fonts)
├── environments/           # Environment configurations
└── styles.scss            # Global styles and theme
```

### Key Technologies
- **Angular 17**: Latest Angular with standalone components
- **Bootstrap 5**: Responsive UI framework
- **RxJS**: Reactive programming for state management
- **TypeScript**: Type-safe development
- **SCSS**: Advanced styling with variables and mixins

## 🚀 Getting Started

### Prerequisites
- Node.js 18+ and npm 9+
- Angular CLI 17+
- Git

### Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd ipl-ticket-booking-frontend
   ```

2. **Install dependencies**
   ```bash
   npm install
   ```

3. **Configure environment**
   ```bash
   # Copy environment template
   cp src/environments/environment.ts.example src/environments/environment.ts
   
   # Update API URL and other configurations
   nano src/environments/environment.ts
   ```

4. **Start development server**
   ```bash
   npm start
   # or
   ng serve
   ```

5. **Open browser**
   ```
   http://localhost:4200
   ```

### Environment Configuration

Update `src/environments/environment.ts`:

```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api',  // Your Spring Boot API URL
  appName: 'IPL Ticket Booking',
  features: {
    enableNotifications: true,
    enableAnalytics: false,
    enableDebugMode: true
  },
  payment: {
    razorpayKey: 'your_razorpay_key_here',
    stripeKey: 'your_stripe_key_here'
  }
};
```

## 🎨 UI/UX Features

### Design System
- **Color Palette**: IPL-themed colors with accessibility compliance
- **Typography**: Roboto font family with proper hierarchy
- **Spacing**: Consistent 8px grid system
- **Components**: Reusable UI components with variants

### Responsive Breakpoints
- **Mobile**: 320px - 767px
- **Tablet**: 768px - 1023px
- **Desktop**: 1024px - 1439px
- **Large Desktop**: 1440px+

### Animations
- **Micro-interactions**: Hover effects, button states
- **Page Transitions**: Smooth route transitions
- **Loading States**: Skeleton screens and spinners
- **Success Animations**: Booking confirmation celebrations

## 🔐 Security Features

### Authentication
- JWT token-based authentication
- Automatic token refresh
- Secure token storage
- Route protection with guards

### Data Protection
- Input validation and sanitization
- XSS protection
- CSRF protection
- Secure HTTP headers

## 📱 Mobile Experience

### Progressive Web App
- Service worker for offline support
- App manifest for installation
- Push notifications
- Background sync

### Mobile Optimizations
- Touch-friendly interface
- Swipe gestures
- Mobile-specific layouts
- Optimized images and assets

## 🧪 Testing

### Unit Testing
```bash
# Run unit tests
npm test

# Run tests with coverage
npm run test:coverage

# Run tests in watch mode
npm run test:watch
```

### E2E Testing
```bash
# Run end-to-end tests
npm run e2e

# Run E2E tests in headless mode
npm run e2e:headless
```

### Testing Strategy
- **Unit Tests**: Component logic, services, pipes
- **Integration Tests**: Component interactions
- **E2E Tests**: User workflows and critical paths

## 🚀 Deployment

### Build for Production
```bash
# Build production bundle
npm run build

# Build with specific environment
ng build --configuration=production
```

### Deployment Options

#### 1. Static Hosting (Netlify, Vercel)
```bash
# Build and deploy
npm run build
# Upload dist/ folder to hosting service
```

#### 2. Docker Deployment
```dockerfile
FROM nginx:alpine
COPY dist/ipl-ticket-booking-frontend /usr/share/nginx/html
COPY nginx.conf /etc/nginx/nginx.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

#### 3. AWS S3 + CloudFront
```bash
# Build and sync to S3
npm run build
aws s3 sync dist/ s3://your-bucket-name --delete
aws cloudfront create-invalidation --distribution-id YOUR_DISTRIBUTION_ID --paths "/*"
```

## 📊 Performance Optimization

### Bundle Optimization
- **Lazy Loading**: Feature modules loaded on demand
- **Tree Shaking**: Unused code elimination
- **Code Splitting**: Vendor and app bundles separated
- **Compression**: Gzip and Brotli compression

### Runtime Performance
- **OnPush Change Detection**: Optimized change detection
- **Virtual Scrolling**: Large lists optimization
- **Image Optimization**: WebP format with fallbacks
- **Caching**: HTTP caching and service worker

### Performance Metrics
- **First Contentful Paint**: < 1.5s
- **Largest Contentful Paint**: < 2.5s
- **Cumulative Layout Shift**: < 0.1
- **First Input Delay**: < 100ms

## 🔧 Development Tools

### Code Quality
```bash
# Linting
npm run lint

# Formatting
npm run format

# Type checking
npm run type-check
```

### Development Workflow
- **Husky**: Git hooks for quality checks
- **Prettier**: Code formatting
- **ESLint**: Code linting
- **Commitizen**: Conventional commits

## 📚 API Integration

### Service Architecture
```typescript
// Example API service usage
@Injectable()
export class MatchService {
  constructor(private apiService: ApiService) {}
  
  getMatches(): Observable<Match[]> {
    return this.apiService.get<Match[]>('/matches');
  }
  
  bookSeats(request: BookingRequest): Observable<BookingResponse> {
    return this.apiService.post<BookingResponse>('/bookings', request);
  }
}
```

### Error Handling
- Global error interceptor
- User-friendly error messages
- Retry mechanisms for failed requests
- Offline support with queue

## 🎯 User Flows

### Booking Flow
1. **Browse Matches** → Filter and search matches
2. **Select Match** → View match details and venue
3. **Choose Seats** → Interactive seat selection
4. **Login/Register** → Authentication if needed
5. **Review Booking** → Confirm selection and pricing
6. **Payment** → Secure payment processing
7. **Confirmation** → Booking confirmation and tickets

### User Management
1. **Registration** → Account creation with verification
2. **Profile Setup** → Personal information and preferences
3. **Booking History** → View past and upcoming bookings
4. **Settings** → Notification preferences and security

## 🌐 Browser Support

### Supported Browsers
- **Chrome**: 90+
- **Firefox**: 88+
- **Safari**: 14+
- **Edge**: 90+
- **Mobile Safari**: 14+
- **Chrome Mobile**: 90+

### Polyfills
- Core-js for ES6+ features
- Web Animations API
- Intersection Observer
- ResizeObserver

## 🤝 Contributing

### Development Setup
1. Fork the repository
2. Create feature branch: `git checkout -b feature/amazing-feature`
3. Commit changes: `git commit -m 'Add amazing feature'`
4. Push to branch: `git push origin feature/amazing-feature`
5. Open Pull Request

### Coding Standards
- Follow Angular style guide
- Use TypeScript strict mode
- Write unit tests for new features
- Update documentation

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🆘 Support

### Documentation
- [Angular Documentation](https://angular.io/docs)
- [Bootstrap Documentation](https://getbootstrap.com/docs/)
- [RxJS Documentation](https://rxjs.dev/)

### Getting Help
- Create an issue for bugs
- Use discussions for questions
- Check existing issues before creating new ones

## 🚀 Roadmap

### Upcoming Features
- [ ] Real-time seat locking visualization
- [ ] Social login integration
- [ ] Push notifications
- [ ] Offline booking support
- [ ] Multi-language support
- [ ] Dark mode theme
- [ ] Voice search
- [ ] AR seat preview

### Performance Improvements
- [ ] Service worker optimization
- [ ] Image lazy loading
- [ ] Bundle size reduction
- [ ] CDN integration

---

**Built with ❤️ for cricket fans by the IPL Ticket Booking team**