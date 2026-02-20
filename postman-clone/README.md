# Postman Clone - REST API Client

A web-based REST API client similar to Postman for testing HTTP endpoints.

## Features

- **HTTP Methods**: Support for GET, POST, PUT, DELETE, PATCH, HEAD, OPTIONS
- **Request Builder**: Easy-to-use interface for building HTTP requests
- **Headers Management**: Add custom headers with key-value pairs
- **Request Body**: Support for JSON, form data, and raw text
- **Response Viewer**: Formatted display of response data, headers, and status
- **History**: Keep track of previous requests
- **Collections**: Organize requests into collections
- **Environment Variables**: Manage different environments (dev, staging, prod)
- **Authentication**: Basic Auth, Bearer Token support
- **Pretty Print**: JSON and XML response formatting
- **Response Time**: Display request duration and response size

## Technology Stack

- **Frontend**: HTML5, CSS3, JavaScript (ES6+)
- **Styling**: Modern CSS with Flexbox/Grid
- **Icons**: Font Awesome
- **JSON Formatting**: Built-in JSON prettifier
- **Local Storage**: For saving history and collections

## Getting Started

### Quick Start
1. Open `index.html` in your web browser
2. Enter your API endpoint URL
3. Select HTTP method
4. Add headers and request body as needed
5. Click "Send" to make the request
6. View the formatted response

### Testing the Application
- **Quick Demo**: Open `demo.html` for a simple functionality test
- **Complete Test Suite**: Open `test-complete.html` for comprehensive testing
- **Local Server**: Run `python -m http.server 8080` in the project directory for best results

### Test Files
- `demo.html` - Quick demonstration of core features
- `test-complete.html` - Comprehensive test suite with real API calls
- `test-functionality.js` - Automated test runner
- `test-app.html` - Basic HTTP client test

## Usage Examples

### GET Request
```
URL: https://jsonplaceholder.typicode.com/posts/1
Method: GET
```

### POST Request
```
URL: https://jsonplaceholder.typicode.com/posts
Method: POST
Headers: Content-Type: application/json
Body: {
  "title": "Test Post",
  "body": "This is a test post",
  "userId": 1
}
```

### Authentication
- **Bearer Token**: Add Authorization header with "Bearer YOUR_TOKEN"
- **Basic Auth**: Use built-in Basic Auth helper

## File Structure

```
postman-clone/
├── index.html          # Main application
├── css/
│   ├── styles.css      # Main styles
│   └── components.css  # Component-specific styles
├── js/
│   ├── app.js          # Main application logic
│   ├── http-client.js  # HTTP request handling
│   ├── ui-manager.js   # UI management
│   └── storage.js      # Local storage management
└── README.md           # This file
```

## Browser Compatibility

- Chrome 60+
- Firefox 55+
- Safari 12+
- Edge 79+

## CORS Notice

Due to browser CORS policies, some requests to external APIs may be blocked. For development, you can:
1. Use a CORS proxy service
2. Disable CORS in your browser (not recommended for production)
3. Use browser extensions that disable CORS
4. Test with APIs that have CORS enabled

## License

MIT License - Feel free to use and modify as needed.