export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api',
  appName: 'IPL Ticket Booking',
  version: '1.0.0',
  features: {
    enableNotifications: true,
    enableAnalytics: false,
    enableDebugMode: true
  },
  api: {
    timeout: 30000,
    retryAttempts: 3
  },
  payment: {
    razorpayKey: 'your_razorpay_key_here',
    stripeKey: 'your_stripe_key_here'
  },
  social: {
    googleClientId: 'your_google_client_id',
    facebookAppId: 'your_facebook_app_id'
  }
};