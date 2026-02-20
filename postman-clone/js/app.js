// Main Application Entry Point
class PostmanCloneApp {
    constructor() {
        this.version = '1.0.0';
        this.initialized = false;
        this.settings = null;
    }

    async initialize() {
        if (this.initialized) {
            return;
        }

        try {
            console.log('🚀 Initializing Postman Clone App v' + this.version);
            
            // Load settings
            this.settings = window.storageManager.getSettings();
            
            // Apply theme
            this.applyTheme(this.settings.theme);
            
            // Initialize UI Manager
            window.uiManager.initialize();
            
            // Set up global error handling
            this.setupErrorHandling();
            
            // Set up service worker (if available)
            this.setupServiceWorker();
            
            // Check for updates
            this.checkForUpdates();
            
            // Add sample data if first time user
            this.setupFirstTimeUser();
            
            this.initialized = true;
            console.log('✅ App initialized successfully');
            
        } catch (error) {
            console.error('❌ Failed to initialize app:', error);
            this.showCriticalError('Failed to initialize application. Please refresh the page.');
        }
    }

    applyTheme(theme) {
        document.documentElement.setAttribute('data-theme', theme);
        
        if (theme === 'dark') {
            document.body.classList.add('dark-theme');
        } else {
            document.body.classList.remove('dark-theme');
        }
    }

    setupErrorHandling() {
        // Global error handler
        window.addEventListener('error', (event) => {
            console.error('Global error:', event.error);
            this.logError(event.error);
        });

        // Unhandled promise rejection handler
        window.addEventListener('unhandledrejection', (event) => {
            console.error('Unhandled promise rejection:', event.reason);
            this.logError(event.reason);
        });

        // Network error detection
        window.addEventListener('online', () => {
            window.uiManager.showSuccess('Connection restored');
        });

        window.addEventListener('offline', () => {
            window.uiManager.showError('Connection lost - working offline');
        });
    }

    setupServiceWorker() {
        if ('serviceWorker' in navigator) {
            navigator.serviceWorker.register('/sw.js')
                .then(registration => {
                    console.log('Service Worker registered:', registration);
                })
                .catch(error => {
                    console.log('Service Worker registration failed:', error);
                });
        }
    }

    checkForUpdates() {
        // Check for app updates (in a real app, this would check a server)
        const lastVersion = localStorage.getItem('app_version');
        if (lastVersion && lastVersion !== this.version) {
            this.showUpdateNotification();
        }
        localStorage.setItem('app_version', this.version);
    }

    showUpdateNotification() {
        const notification = document.createElement('div');
        notification.className = 'update-notification';
        notification.innerHTML = `
            <div class="update-content">
                <i class="fas fa-download"></i>
                <span>App updated to v${this.version}</span>
                <button class="btn btn-sm btn-primary" onclick="location.reload()">
                    Refresh
                </button>
            </div>
        `;
        notification.style.cssText = `
            position: fixed;
            top: 80px;
            right: 20px;
            background: #667eea;
            color: white;
            padding: 1rem;
            border-radius: 8px;
            box-shadow: 0 4px 12px rgba(0,0,0,0.15);
            z-index: 1002;
            animation: slideIn 0.3s ease-out;
        `;
        
        document.body.appendChild(notification);
        
        setTimeout(() => {
            notification.remove();
        }, 10000);
    }

    setupFirstTimeUser() {
        const isFirstTime = !localStorage.getItem('app_initialized');
        
        if (isFirstTime) {
            this.createSampleData();
            localStorage.setItem('app_initialized', 'true');
            this.showWelcomeMessage();
        }
    }

    createSampleData() {
        // Create sample collection
        const sampleCollection = window.storageManager.createCollection(
            'Sample Requests',
            'Example API requests to get you started'
        );

        if (sampleCollection) {
            // Add sample requests
            const sampleRequests = [
                {
                    name: 'Get Posts',
                    method: 'GET',
                    url: 'https://jsonplaceholder.typicode.com/posts',
                    headers: {
                        'Accept': 'application/json'
                    },
                    body: null
                },
                {
                    name: 'Create Post',
                    method: 'POST',
                    url: 'https://jsonplaceholder.typicode.com/posts',
                    headers: {
                        'Content-Type': 'application/json',
                        'Accept': 'application/json'
                    },
                    body: {
                        type: 'json',
                        data: {
                            title: 'Sample Post',
                            body: 'This is a sample post created via API',
                            userId: 1
                        }
                    }
                },
                {
                    name: 'Get User',
                    method: 'GET',
                    url: 'https://jsonplaceholder.typicode.com/users/1',
                    headers: {
                        'Accept': 'application/json'
                    },
                    body: null
                }
            ];

            sampleRequests.forEach(request => {
                window.storageManager.addRequestToCollection(sampleCollection.id, request);
            });
        }
    }

    showWelcomeMessage() {
        setTimeout(() => {
            const welcome = document.createElement('div');
            welcome.className = 'welcome-modal';
            welcome.innerHTML = `
                <div class="welcome-content">
                    <div class="welcome-header">
                        <i class="fas fa-rocket"></i>
                        <h2>Welcome to API Client!</h2>
                    </div>
                    <div class="welcome-body">
                        <p>Your powerful REST API testing tool is ready to use.</p>
                        <ul>
                            <li>✨ Test any REST API with GET, POST, PUT, DELETE methods</li>
                            <li>📝 Organize requests in collections</li>
                            <li>📊 View detailed response data and headers</li>
                            <li>🔐 Support for various authentication methods</li>
                            <li>📱 Responsive design for desktop and mobile</li>
                        </ul>
                        <p>We've created some sample requests to get you started!</p>
                    </div>
                    <div class="welcome-footer">
                        <button class="btn btn-primary" onclick="this.closest('.welcome-modal').remove()">
                            Get Started
                        </button>
                    </div>
                </div>
            `;
            welcome.style.cssText = `
                position: fixed;
                top: 0;
                left: 0;
                right: 0;
                bottom: 0;
                background: rgba(0,0,0,0.8);
                display: flex;
                align-items: center;
                justify-content: center;
                z-index: 2000;
                animation: fadeIn 0.3s ease-out;
            `;
            
            const style = document.createElement('style');
            style.textContent = `
                .welcome-content {
                    background: white;
                    border-radius: 12px;
                    padding: 2rem;
                    max-width: 500px;
                    margin: 2rem;
                    box-shadow: 0 20px 40px rgba(0,0,0,0.3);
                }
                .welcome-header {
                    text-align: center;
                    margin-bottom: 1.5rem;
                }
                .welcome-header i {
                    font-size: 3rem;
                    color: #667eea;
                    margin-bottom: 1rem;
                }
                .welcome-header h2 {
                    color: #333;
                    margin: 0;
                }
                .welcome-body ul {
                    margin: 1rem 0;
                    padding-left: 1rem;
                }
                .welcome-body li {
                    margin: 0.5rem 0;
                    color: #555;
                }
                .welcome-footer {
                    text-align: center;
                    margin-top: 2rem;
                }
                @keyframes fadeIn {
                    from { opacity: 0; transform: scale(0.9); }
                    to { opacity: 1; transform: scale(1); }
                }
            `;
            document.head.appendChild(style);
            document.body.appendChild(welcome);
        }, 1000);
    }

    logError(error) {
        // In a real app, this would send errors to a logging service
        const errorLog = {
            timestamp: new Date().toISOString(),
            error: error.toString(),
            stack: error.stack,
            userAgent: navigator.userAgent,
            url: window.location.href
        };
        
        console.error('Error logged:', errorLog);
        
        // Store in local storage for debugging
        const errors = JSON.parse(localStorage.getItem('error_logs') || '[]');
        errors.push(errorLog);
        
        // Keep only last 50 errors
        if (errors.length > 50) {
            errors.splice(0, errors.length - 50);
        }
        
        localStorage.setItem('error_logs', JSON.stringify(errors));
    }

    showCriticalError(message) {
        const errorDiv = document.createElement('div');
        errorDiv.innerHTML = `
            <div style="
                position: fixed;
                top: 50%;
                left: 50%;
                transform: translate(-50%, -50%);
                background: #dc3545;
                color: white;
                padding: 2rem;
                border-radius: 8px;
                text-align: center;
                z-index: 9999;
                box-shadow: 0 10px 30px rgba(0,0,0,0.3);
            ">
                <i class="fas fa-exclamation-triangle" style="font-size: 2rem; margin-bottom: 1rem;"></i>
                <h3 style="margin: 0 0 1rem 0;">Critical Error</h3>
                <p style="margin: 0 0 1rem 0;">${message}</p>
                <button onclick="location.reload()" class="btn btn-light">
                    Reload Page
                </button>
            </div>
        `;
        document.body.appendChild(errorDiv);
    }

    // Public API methods
    exportData() {
        const data = window.storageManager.exportData();
        if (data) {
            const blob = new Blob([data], { type: 'application/json' });
            const url = URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = `api-client-backup-${new Date().toISOString().split('T')[0]}.json`;
            document.body.appendChild(a);
            a.click();
            document.body.removeChild(a);
            URL.revokeObjectURL(url);
            
            window.uiManager.showSuccess('Data exported successfully');
        }
    }

    importData(file) {
        const reader = new FileReader();
        reader.onload = (e) => {
            try {
                const success = window.storageManager.importData(e.target.result);
                if (success) {
                    window.uiManager.showSuccess('Data imported successfully');
                    window.uiManager.loadHistory();
                    window.uiManager.loadCollections();
                } else {
                    window.uiManager.showError('Failed to import data');
                }
            } catch (error) {
                window.uiManager.showError('Invalid backup file');
            }
        };
        reader.readAsText(file);
    }

    getAppInfo() {
        return {
            version: this.version,
            initialized: this.initialized,
            storageSize: window.storageManager.getStorageSize(),
            historyCount: window.storageManager.getHistory().length,
            collectionsCount: window.storageManager.getCollections().length
        };
    }
}

// Initialize app when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    window.app = new PostmanCloneApp();
    window.app.initialize();
});

// Add CSS animations
const style = document.createElement('style');
style.textContent = `
    @keyframes slideIn {
        from {
            transform: translateX(100%);
            opacity: 0;
        }
        to {
            transform: translateX(0);
            opacity: 1;
        }
    }
    
    .notification {
        animation: slideIn 0.3s ease-out;
    }
    
    .update-notification .update-content {
        display: flex;
        align-items: center;
        gap: 1rem;
    }
    
    .fade-in {
        animation: fadeIn 0.3s ease-out;
    }
    
    @keyframes fadeIn {
        from {
            opacity: 0;
            transform: translateY(10px);
        }
        to {
            opacity: 1;
            transform: translateY(0);
        }
    }
`;
document.head.appendChild(style);