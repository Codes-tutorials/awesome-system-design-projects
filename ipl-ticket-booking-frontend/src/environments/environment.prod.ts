export const environment = {
  production: true,
  apiUrl: 'https://your-production-api.com/api',
  appName: 'IPL Ticket Booking',
  version: '1.0.0',
  features: {
    enableNotifications: true,
    enableAnalytics: true,
    enableDebugMode: false
  },
  api: {
    timeout: 30000,
    retryAttempts: 3
  },
  payment: {
    razorpayKey: 'your_production_razorpay_key',
    stripeKey: 'your_production_stripe_key'
  },
  social: {
    googleClientId: 'your_production_google_client_id',
    facebookAppId: 'your_production_facebook_app_id'
  }
};