// HTTP Client for making API requests
class HttpClient {
    constructor() {
        this.defaultTimeout = 30000; // 30 seconds
    }

    async makeRequest(config) {
        const startTime = performance.now();
        
        try {
            // Prepare request configuration
            const requestConfig = this.prepareRequest(config);
            
            // Make the actual request
            const response = await fetch(requestConfig.url, requestConfig.options);
            
            // Calculate response time
            const endTime = performance.now();
            const responseTime = Math.round(endTime - startTime);
            
            // Process response
            const result = await this.processResponse(response, responseTime);
            
            return {
                success: true,
                ...result
            };
            
        } catch (error) {
            const endTime = performance.now();
            const responseTime = Math.round(endTime - startTime);
            
            return {
                success: false,
                error: error.message,
                status: 0,
                statusText: 'Network Error',
                headers: {},
                data: null,
                responseTime: responseTime,
                size: 0
            };
        }
    }

    prepareRequest(config) {
        const {
            method = 'GET',
            url,
            headers = {},
            body = null,
            timeout = this.defaultTimeout
        } = config;

        // Prepare URL with query parameters
        let requestUrl = url;
        if (config.params && Object.keys(config.params).length > 0) {
            const urlObj = new URL(url);
            Object.entries(config.params).forEach(([key, value]) => {
                if (value !== '') {
                    urlObj.searchParams.append(key, value);
                }
            });
            requestUrl = urlObj.toString();
        }

        // Prepare headers
        const requestHeaders = new Headers();
        Object.entries(headers).forEach(([key, value]) => {
            if (key && value) {
                requestHeaders.append(key, value);
            }
        });

        // Prepare body
        let requestBody = null;
        if (body && method !== 'GET' && method !== 'HEAD') {
            if (typeof body === 'object' && body.type) {
                switch (body.type) {
                    case 'json':
                        requestBody = JSON.stringify(body.data);
                        if (!requestHeaders.has('Content-Type')) {
                            requestHeaders.set('Content-Type', 'application/json');
                        }
                        break;
                    case 'form':
                        const formData = new FormData();
                        Object.entries(body.data).forEach(([key, value]) => {
                            if (key && value !== '') {
                                formData.append(key, value);
                            }
                        });
                        requestBody = formData;
                        break;
                    case 'raw':
                        requestBody = body.data;
                        break;
                    default:
                        requestBody = body.data;
                }
            } else {
                requestBody = body;
            }
        }

        // Create AbortController for timeout
        const controller = new AbortController();
        const timeoutId = setTimeout(() => controller.abort(), timeout);

        const options = {
            method: method.toUpperCase(),
            headers: requestHeaders,
            body: requestBody,
            signal: controller.signal
        };

        return {
            url: requestUrl,
            options: options,
            timeoutId: timeoutId
        };
    }

    async processResponse(response, responseTime) {
        // Clear timeout
        // Note: timeoutId would need to be passed here in a real implementation
        
        // Extract headers
        const responseHeaders = {};
        response.headers.forEach((value, key) => {
            responseHeaders[key] = value;
        });

        // Get response text
        const responseText = await response.text();
        
        // Calculate response size
        const responseSize = new Blob([responseText]).size;

        // Try to parse as JSON
        let responseData = responseText;
        let contentType = response.headers.get('content-type') || '';
        
        if (contentType.includes('application/json') || this.isJsonString(responseText)) {
            try {
                responseData = JSON.parse(responseText);
            } catch (e) {
                // Keep as text if JSON parsing fails
                responseData = responseText;
            }
        }

        return {
            status: response.status,
            statusText: response.statusText,
            headers: responseHeaders,
            data: responseData,
            rawData: responseText,
            responseTime: responseTime,
            size: responseSize,
            contentType: contentType
        };
    }

    isJsonString(str) {
        if (!str || typeof str !== 'string') return false;
        
        try {
            const parsed = JSON.parse(str);
            return typeof parsed === 'object' && parsed !== null;
        } catch (e) {
            return false;
        }
    }

    // Helper method to format response size
    formatSize(bytes) {
        if (bytes === 0) return '0 B';
        
        const k = 1024;
        const sizes = ['B', 'KB', 'MB', 'GB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        
        return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
    }

    // Helper method to get status class for styling
    getStatusClass(status) {
        if (status >= 200 && status < 300) {
            return 'success';
        } else if (status >= 400 && status < 500) {
            return 'warning';
        } else if (status >= 500) {
            return 'error';
        } else {
            return 'info';
        }
    }

    // Method to validate URL
    isValidUrl(string) {
        try {
            new URL(string);
            return true;
        } catch (_) {
            return false;
        }
    }

    // Method to prepare authentication headers
    prepareAuthHeaders(authConfig) {
        const headers = {};
        
        if (!authConfig || authConfig.type === 'none') {
            return headers;
        }

        switch (authConfig.type) {
            case 'bearer':
                if (authConfig.token) {
                    headers['Authorization'] = `Bearer ${authConfig.token}`;
                }
                break;
            case 'basic':
                if (authConfig.username && authConfig.password) {
                    const credentials = btoa(`${authConfig.username}:${authConfig.password}`);
                    headers['Authorization'] = `Basic ${credentials}`;
                }
                break;
            default:
                break;
        }

        return headers;
    }

    // Method to prepare common headers
    getCommonHeaders() {
        return {
            'User-Agent': 'API-Client/1.0',
            'Accept': '*/*',
            'Cache-Control': 'no-cache'
        };
    }

    // Method to handle CORS preflight
    async checkCors(url) {
        try {
            const response = await fetch(url, {
                method: 'OPTIONS',
                mode: 'cors'
            });
            return response.ok;
        } catch (error) {
            return false;
        }
    }
}

// Create global instance
window.httpClient = new HttpClient();