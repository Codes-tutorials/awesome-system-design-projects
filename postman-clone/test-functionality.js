// Comprehensive test script for Postman Clone functionality
class PostmanCloneTests {
    constructor() {
        this.testResults = [];
        this.passedTests = 0;
        this.failedTests = 0;
    }

    log(message, type = 'info') {
        const timestamp = new Date().toLocaleTimeString();
        const logEntry = `[${timestamp}] ${type.toUpperCase()}: ${message}`;
        console.log(logEntry);
        this.testResults.push({ message, type, timestamp });
    }

    async runAllTests() {
        this.log('Starting Postman Clone functionality tests...');
        
        try {
            await this.testHttpClient();
            await this.testStorageManager();
            await this.testUIManager();
            await this.testRealAPIRequests();
            
            this.log(`Tests completed: ${this.passedTests} passed, ${this.failedTests} failed`);
            this.displayResults();
            
        } catch (error) {
            this.log(`Test suite failed: ${error.message}`, 'error');
        }
    }

    async testHttpClient() {
        this.log('Testing HTTP Client...');
        
        try {
            // Test URL validation
            const isValidUrl = window.httpClient.isValidUrl('https://api.example.com');
            this.assert(isValidUrl, 'URL validation should work');
            
            // Test invalid URL
            const isInvalidUrl = !window.httpClient.isValidUrl('not-a-url');
            this.assert(isInvalidUrl, 'Invalid URL should be rejected');
            
            // Test auth headers preparation
            const authHeaders = window.httpClient.prepareAuthHeaders({
                type: 'bearer',
                token: 'test-token'
            });
            this.assert(authHeaders.Authorization === 'Bearer test-token', 'Bearer token auth should work');
            
            // Test basic auth
            const basicAuthHeaders = window.httpClient.prepareAuthHeaders({
                type: 'basic',
                username: 'user',
                password: 'pass'
            });
            this.assert(basicAuthHeaders.Authorization.startsWith('Basic '), 'Basic auth should work');
            
            this.log('HTTP Client tests passed', 'success');
            
        } catch (error) {
            this.log(`HTTP Client test failed: ${error.message}`, 'error');
        }
    }

    async testStorageManager() {
        this.log('Testing Storage Manager...');
        
        try {
            // Test settings
            const settings = window.storageManager.getSettings();
            this.assert(typeof settings === 'object', 'Settings should be an object');
            
            // Test history
            const initialHistory = window.storageManager.getHistory();
            this.assert(Array.isArray(initialHistory), 'History should be an array');
            
            // Test adding to history
            const testRequest = {
                method: 'GET',
                url: 'https://test.com',
                headers: {},
                params: {},
                body: null
            };
            
            const historyItem = window.storageManager.addToHistory(testRequest);
            this.assert(historyItem && historyItem.id, 'Should be able to add to history');
            
            // Test collections
            const collections = window.storageManager.getCollections();
            this.assert(Array.isArray(collections), 'Collections should be an array');
            
            // Test creating collection
            const collection = window.storageManager.createCollection('Test Collection', 'Test description');
            this.assert(collection && collection.id, 'Should be able to create collection');
            
            this.log('Storage Manager tests passed', 'success');
            
        } catch (error) {
            this.log(`Storage Manager test failed: ${error.message}`, 'error');
        }
    }

    async testUIManager() {
        this.log('Testing UI Manager...');
        
        try {
            // Test UI elements exist
            this.assert(window.uiManager.urlInput, 'URL input should exist');
            this.assert(window.uiManager.methodSelect, 'Method select should exist');
            this.assert(window.uiManager.sendBtn, 'Send button should exist');
            
            // Test request validation
            window.uiManager.urlInput.value = '';
            const isInvalid = !window.uiManager.validateRequest();
            this.assert(isInvalid, 'Empty URL should be invalid');
            
            window.uiManager.urlInput.value = 'https://api.example.com';
            const isValid = window.uiManager.validateRequest();
            this.assert(isValid, 'Valid URL should pass validation');
            
            // Test request config building
            window.uiManager.methodSelect.value = 'POST';
            const config = window.uiManager.buildRequestConfig();
            this.assert(config.method === 'POST', 'Should build correct request config');
            
            this.log('UI Manager tests passed', 'success');
            
        } catch (error) {
            this.log(`UI Manager test failed: ${error.message}`, 'error');
        }
    }

    async testRealAPIRequests() {
        this.log('Testing real API requests...');
        
        try {
            // Test GET request to JSONPlaceholder
            const getResponse = await window.httpClient.makeRequest({
                method: 'GET',
                url: 'https://jsonplaceholder.typicode.com/posts/1',
                headers: {
                    'Accept': 'application/json'
                }
            });
            
            this.assert(getResponse.success, 'GET request should succeed');
            this.assert(getResponse.status === 200, 'GET request should return 200');
            this.assert(getResponse.data && getResponse.data.id === 1, 'GET request should return correct data');
            
            // Test POST request
            const postResponse = await window.httpClient.makeRequest({
                method: 'POST',
                url: 'https://jsonplaceholder.typicode.com/posts',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: {
                    type: 'json',
                    data: {
                        title: 'Test Post',
                        body: 'Test content',
                        userId: 1
                    }
                }
            });
            
            this.assert(postResponse.success, 'POST request should succeed');
            this.assert(postResponse.status === 201, 'POST request should return 201');
            
            // Test request with query parameters
            const paramsResponse = await window.httpClient.makeRequest({
                method: 'GET',
                url: 'https://jsonplaceholder.typicode.com/posts',
                params: {
                    userId: '1'
                }
            });
            
            this.assert(paramsResponse.success, 'Request with params should succeed');
            this.assert(Array.isArray(paramsResponse.data), 'Should return array of posts');
            
            this.log('Real API request tests passed', 'success');
            
        } catch (error) {
            this.log(`Real API request test failed: ${error.message}`, 'error');
        }
    }

    assert(condition, message) {
        if (condition) {
            this.passedTests++;
            this.log(`✅ ${message}`, 'pass');
        } else {
            this.failedTests++;
            this.log(`❌ ${message}`, 'fail');
            throw new Error(`Assertion failed: ${message}`);
        }
    }

    displayResults() {
        const resultsDiv = document.getElementById('test-results');
        if (resultsDiv) {
            let html = '<h2>Test Results</h2>';
            html += `<p><strong>Passed:</strong> ${this.passedTests} | <strong>Failed:</strong> ${this.failedTests}</p>`;
            html += '<div class="test-log">';
            
            this.testResults.forEach(result => {
                const className = result.type === 'error' || result.type === 'fail' ? 'error' : 
                                result.type === 'success' || result.type === 'pass' ? 'success' : 'info';
                html += `<div class="log-entry ${className}">${result.message}</div>`;
            });
            
            html += '</div>';
            resultsDiv.innerHTML = html;
        }
    }
}

// Auto-run tests when all scripts are loaded
window.addEventListener('load', () => {
    // Wait a bit for all scripts to initialize
    setTimeout(async () => {
        if (window.httpClient && window.storageManager && window.uiManager) {
            const tester = new PostmanCloneTests();
            await tester.runAllTests();
        } else {
            console.error('Not all required modules are loaded');
        }
    }, 1000);
});
</text>
</invoke>