# IPL Ticket Booking Frontend - Quick Start Guide

## 🚀 Quick Setup (5 minutes)

### Prerequisites
- Node.js 18+ and npm 9+
- Git (optional)

### Option 1: Automated Setup (Windows)
```bash
# Run the setup script
setup.bat
```

### Option 2: Manual Setup
```bash
# 1. Install Angular CLI globally
npm install -g @angular/cli

# 2. Install dependencies
npm install

# 3. Start development server
npm start
```

### Option 3: Using CMD (if PowerShell issues)
```cmd
# Install Angular CLI
npm install -g @angular/cli

# Install dependencies
npm install

# Start the application
ng serve
```

## 🌐 Access the Application

Once the server starts, open your browser and navigate to:
```
http://localhost:4200
```

## 🔧 Configuration

### 1. Backend API Configuration
Update `src/environments/environment.ts`:
```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api',  // Your Spring Boot API URL
  // ... other configurations
};
```

### 2. Development vs Production
- **Development**: Uses `environment.ts`
- **Production**: Uses `environment.prod.ts`

## 📱 Features Available

### ✅ Implemented Features
- **Authentication System**
  - Login/Register forms
  - JWT token management
  - Route protection
  
- **Match Management**
  - Match listing with filters
  - Match details view
  - Seat selection interface
  
- **UI Components**
  - Responsive header/footer
  - Loading states
  - Error handling
  - Modern Bootstrap 5 design

### 🚧 In Development
- Booking process completion
- Payment integration
- User profile management
- Real-time seat updates

## 🎯 Key Components

### Core Services
- `AuthService`: User authentication and session management
- `ApiService`: HTTP client with interceptors
- `MatchService`: Match and seat management
- `LoadingService`: Global loading state

### Feature Modules
- `auth/`: Login, register, password reset
- `matches/`: Match listing, details, seat selection
- `booking/`: Booking process and payment
- `profile/`: User profile and settings

## 🔍 Development Commands

```bash
# Start development server
npm start
# or
ng serve

# Build for production
npm run build

# Run tests
npm test

# Run linting
npm run lint

# Generate new component
ng generate component feature-name

# Generate new service
ng generate service service-name
```

## 🎨 Styling

### CSS Framework
- **Bootstrap 5**: Responsive grid and components
- **Custom SCSS**: IPL-themed colors and animations
- **Font Awesome**: Icons and symbols

### Theme Colors
- **Primary**: #1976d2 (Blue)
- **Secondary**: #ff6b35 (Orange)
- **Success**: #4caf50 (Green)
- **Danger**: #f44336 (Red)

## 📱 Responsive Design

The application is fully responsive and works on:
- **Mobile**: 320px - 767px
- **Tablet**: 768px - 1023px
- **Desktop**: 1024px+

## 🔐 Authentication Flow

1. **Login**: User enters credentials
2. **JWT Token**: Received from backend API
3. **Storage**: Token stored in localStorage
4. **Interceptor**: Automatically adds token to requests
5. **Guards**: Protect routes requiring authentication

## 🎫 Booking Flow (Planned)

1. **Browse Matches**: Filter and search available matches
2. **Select Match**: View match details and stadium info
3. **Choose Seats**: Interactive seat selection
4. **Authentication**: Login/register if needed
5. **Review**: Confirm booking details
6. **Payment**: Process payment securely
7. **Confirmation**: Receive booking confirmation

## 🛠️ Troubleshooting

### Common Issues

**1. Angular CLI not found**
```bash
npm install -g @angular/cli
```

**2. Port 4200 already in use**
```bash
ng serve --port 4201
```

**3. Node modules issues**
```bash
rm -rf node_modules
npm install
```

**4. PowerShell execution policy (Windows)**
```powershell
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
```

### Getting Help
- Check browser console for errors
- Verify backend API is running on port 8080
- Ensure environment configuration is correct
- Check network connectivity

## 📚 Next Steps

1. **Complete the setup** following this guide
2. **Explore the codebase** starting with `src/app/`
3. **Run the application** and test basic functionality
4. **Connect to backend** by updating API URL
5. **Customize styling** in `src/styles.scss`

## 🤝 Development Workflow

1. **Create feature branch**: `git checkout -b feature/new-feature`
2. **Make changes**: Implement your feature
3. **Test locally**: `npm start` and test in browser
4. **Build production**: `npm run build` to verify
5. **Commit changes**: `git commit -m "Add new feature"`

---

**🎉 You're ready to start developing! The application should now be running at http://localhost:4200**