// UI Management and DOM manipulation
class UIManager {
    constructor() {
        this.currentRequest = null;
        this.currentResponse = null;
        this.initializeElements();
        this.bindEvents();
    }

    initializeElements() {
        // Main elements
        this.sidebar = document.getElementById('sidebar');
        this.loadingOverlay = document.getElementById('loadingOverlay');
        this.responseSection = document.getElementById('responseSection');

        // Request elements
        this.methodSelect = document.getElementById('methodSelect');
        this.urlInput = document.getElementById('urlInput');
        this.sendBtn = document.getElementById('sendBtn');
        this.saveBtn = document.getElementById('saveBtn');
        this.clearBtn = document.getElementById('clearBtn');

        // Tab elements
        this.tabButtons = document.querySelectorAll('.tab-btn');
        this.tabPanels = document.querySelectorAll('.tab-panel');

        // Params elements
        this.paramsList = document.getElementById('paramsList');
        this.addParamBtn = document.getElementById('addParamBtn');

        // Headers elements
        this.headersList = document.getElementById('headersList');
        this.addHeaderBtn = document.getElementById('addHeaderBtn');

        // Body elements
        this.bodyTypeRadios = document.querySelectorAll('input[name="bodyType"]');
        this.jsonTextarea = document.getElementById('jsonTextarea');
        this.rawTextarea = document.getElementById('rawTextarea');
        this.formDataList = document.getElementById('formDataList');
        this.addFormFieldBtn = document.getElementById('addFormFieldBtn');
        this.formatJsonBtn = document.getElementById('formatJsonBtn');

        // Auth elements
        this.authTypeRadios = document.querySelectorAll('input[name="authType"]');
        this.bearerToken = document.getElementById('bearerToken');
        this.basicUsername = document.getElementById('basicUsername');
        this.basicPassword = document.getElementById('basicPassword');

        // Response elements
        this.statusBadge = document.getElementById('statusBadge');
        this.responseTime = document.getElementById('responseTime');
        this.responseSize = document.getElementById('responseSize');
        this.responseContent = document.getElementById('responseContent');
        this.responseHeaders = document.getElementById('responseHeaders');
        this.prettyBtn = document.getElementById('prettyBtn');
        this.rawBtn = document.getElementById('rawBtn');
        this.copyResponseBtn = document.getElementById('copyResponseBtn');

        // History and Collections
        this.historyList = document.getElementById('historyList');
        this.collectionsList = document.getElementById('collectionsList');
        this.clearHistoryBtn = document.getElementById('clearHistoryBtn');
        this.newCollectionBtn = document.getElementById('newCollectionBtn');

        // Modal elements
        this.saveModal = document.getElementById('saveModal');
        this.requestName = document.getElementById('requestName');
        this.collectionSelect = document.getElementById('collectionSelect');
        this.confirmSaveBtn = document.getElementById('confirmSaveBtn');
        this.cancelSaveBtn = document.getElementById('cancelSaveBtn');
        this.closeSaveModal = document.getElementById('closeSaveModal');
    }

    bindEvents() {
        // Tab switching
        this.tabButtons.forEach(btn => {
            btn.addEventListener('click', (e) => this.switchTab(e));
        });

        // Sidebar tabs
        document.querySelectorAll('.sidebar .tab-btn').forEach(btn => {
            btn.addEventListener('click', (e) => this.switchSidebarTab(e));
        });

        // Request configuration
        this.sendBtn.addEventListener('click', () => this.sendRequest());
        this.saveBtn.addEventListener('click', () => this.showSaveModal());
        this.clearBtn.addEventListener('click', () => this.clearRequest());

        // Params management
        this.addParamBtn.addEventListener('click', () => this.addParam());
        this.paramsList.addEventListener('click', (e) => this.handleParamAction(e));
        this.paramsList.addEventListener('input', () => this.updateUrlFromParams());

        // Headers management
        this.addHeaderBtn.addEventListener('click', () => this.addHeader());
        this.headersList.addEventListener('click', (e) => this.handleHeaderAction(e));

        // Body type switching
        this.bodyTypeRadios.forEach(radio => {
            radio.addEventListener('change', (e) => this.switchBodyType(e.target.value));
        });

        // Form data management
        this.addFormFieldBtn.addEventListener('click', () => this.addFormField());
        this.formDataList.addEventListener('click', (e) => this.handleFormFieldAction(e));

        // JSON formatting
        this.formatJsonBtn.addEventListener('click', () => this.formatJson());

        // Auth type switching
        this.authTypeRadios.forEach(radio => {
            radio.addEventListener('change', (e) => this.switchAuthType(e.target.value));
        });

        // Response formatting
        this.prettyBtn.addEventListener('click', () => this.formatResponse('pretty'));
        this.rawBtn.addEventListener('click', () => this.formatResponse('raw'));
        this.copyResponseBtn.addEventListener('click', () => this.copyResponse());

        // History management
        this.clearHistoryBtn.addEventListener('click', () => this.clearHistory());
        this.historyList.addEventListener('click', (e) => this.handleHistoryClick(e));

        // Collections management
        this.newCollectionBtn.addEventListener('click', () => this.createCollection());
        this.collectionsList.addEventListener('click', (e) => this.handleCollectionClick(e));

        // Modal events
        this.confirmSaveBtn.addEventListener('click', () => this.saveRequest());
        this.cancelSaveBtn.addEventListener('click', () => this.hideSaveModal());
        this.closeSaveModal.addEventListener('click', () => this.hideSaveModal());

        // Keyboard shortcuts
        document.addEventListener('keydown', (e) => this.handleKeyboardShortcuts(e));

        // URL input validation
        this.urlInput.addEventListener('input', () => this.validateUrl());
    }

    // Tab Management
    switchTab(e) {
        const targetTab = e.target.dataset.tab;
        
        // Update tab buttons
        e.target.parentElement.querySelectorAll('.tab-btn').forEach(btn => {
            btn.classList.remove('active');
        });
        e.target.classList.add('active');

        // Update tab panels
        e.target.closest('.request-tabs, .response-tabs').querySelectorAll('.tab-panel').forEach(panel => {
            panel.classList.remove('active');
        });
        document.getElementById(targetTab + 'Tab').classList.add('active');
    }

    switchSidebarTab(e) {
        const targetTab = e.target.dataset.tab;
        
        // Update tab buttons
        e.target.parentElement.querySelectorAll('.tab-btn').forEach(btn => {
            btn.classList.remove('active');
        });
        e.target.classList.add('active');

        // Update tab content
        document.querySelectorAll('.sidebar .tab-content').forEach(content => {
            content.classList.remove('active');
        });
        document.getElementById(targetTab + 'Tab').classList.add('active');
    }

    // Request Management
    async sendRequest() {
        if (!this.validateRequest()) {
            return;
        }

        this.showLoading();
        
        try {
            const requestConfig = this.buildRequestConfig();
            const response = await window.httpClient.makeRequest(requestConfig);
            
            this.currentRequest = requestConfig;
            this.currentResponse = response;
            
            this.displayResponse(response);
            this.addToHistory(requestConfig, response);
            
        } catch (error) {
            this.showError('Request failed: ' + error.message);
        } finally {
            this.hideLoading();
        }
    }

    buildRequestConfig() {
        const method = this.methodSelect.value;
        const url = this.urlInput.value.trim();
        
        // Build headers
        const headers = {};
        this.headersList.querySelectorAll('.header-row').forEach(row => {
            const key = row.querySelector('.header-key').value.trim();
            const value = row.querySelector('.header-value').value.trim();
            if (key && value) {
                headers[key] = value;
            }
        });

        // Add auth headers
        const authHeaders = this.getAuthHeaders();
        Object.assign(headers, authHeaders);

        // Build params
        const params = {};
        this.paramsList.querySelectorAll('.param-row').forEach(row => {
            const key = row.querySelector('.param-key').value.trim();
            const value = row.querySelector('.param-value').value.trim();
            if (key && value) {
                params[key] = value;
            }
        });

        // Build body
        const body = this.getRequestBody();

        return {
            method,
            url,
            headers,
            params,
            body
        };
    }

    getRequestBody() {
        const bodyType = document.querySelector('input[name="bodyType"]:checked').value;
        
        if (bodyType === 'none') {
            return null;
        }

        switch (bodyType) {
            case 'json':
                const jsonText = this.jsonTextarea.value.trim();
                if (jsonText) {
                    try {
                        const jsonData = JSON.parse(jsonText);
                        return {
                            type: 'json',
                            data: jsonData
                        };
                    } catch (e) {
                        throw new Error('Invalid JSON in request body');
                    }
                }
                return null;

            case 'form':
                const formData = {};
                this.formDataList.querySelectorAll('.form-field-row').forEach(row => {
                    const key = row.querySelector('.form-key').value.trim();
                    const value = row.querySelector('.form-value').value.trim();
                    if (key && value) {
                        formData[key] = value;
                    }
                });
                return Object.keys(formData).length > 0 ? {
                    type: 'form',
                    data: formData
                } : null;

            case 'raw':
                const rawText = this.rawTextarea.value.trim();
                return rawText ? {
                    type: 'raw',
                    data: rawText
                } : null;

            default:
                return null;
        }
    }

    getAuthHeaders() {
        const authType = document.querySelector('input[name="authType"]:checked').value;
        
        const authConfig = {
            type: authType,
            token: this.bearerToken.value.trim(),
            username: this.basicUsername.value.trim(),
            password: this.basicPassword.value.trim()
        };

        return window.httpClient.prepareAuthHeaders(authConfig);
    }

    validateRequest() {
        const url = this.urlInput.value.trim();
        
        if (!url) {
            this.showError('Please enter a URL');
            return false;
        }

        if (!window.httpClient.isValidUrl(url)) {
            this.showError('Please enter a valid URL');
            return false;
        }

        return true;
    }

    validateUrl() {
        const url = this.urlInput.value.trim();
        if (url && !window.httpClient.isValidUrl(url)) {
            this.urlInput.style.borderColor = '#dc3545';
        } else {
            this.urlInput.style.borderColor = '#e9ecef';
        }
    }

    clearRequest() {
        this.urlInput.value = '';
        this.methodSelect.value = 'GET';
        
        // Clear params
        this.paramsList.innerHTML = this.getEmptyParamRow();
        
        // Clear headers
        this.headersList.innerHTML = this.getEmptyHeaderRow();
        
        // Clear body
        this.jsonTextarea.value = '';
        this.rawTextarea.value = '';
        this.formDataList.innerHTML = this.getEmptyFormFieldRow();
        document.querySelector('input[name="bodyType"][value="none"]').checked = true;
        this.switchBodyType('none');
        
        // Clear auth
        this.bearerToken.value = '';
        this.basicUsername.value = '';
        this.basicPassword.value = '';
        document.querySelector('input[name="authType"][value="none"]').checked = true;
        this.switchAuthType('none');
        
        // Hide response
        this.responseSection.style.display = 'none';
    }

    // Response Display
    displayResponse(response) {
        this.responseSection.style.display = 'block';
        
        // Update status badge
        this.statusBadge.textContent = `${response.status} ${response.statusText}`;
        this.statusBadge.className = `status-badge ${window.httpClient.getStatusClass(response.status)}`;
        
        // Update meta info
        this.responseTime.textContent = `${response.responseTime}ms`;
        this.responseSize.textContent = window.httpClient.formatSize(response.size);
        
        // Display response body
        this.displayResponseBody(response);
        
        // Display response headers
        this.displayResponseHeaders(response.headers);
    }

    displayResponseBody(response) {
        let content = response.rawData;
        
        if (typeof response.data === 'object' && response.data !== null) {
            content = JSON.stringify(response.data, null, 2);
            this.responseContent.className = 'language-json';
        } else {
            this.responseContent.className = '';
        }
        
        this.responseContent.textContent = content;
        this.highlightJson();
    }

    displayResponseHeaders(headers) {
        this.responseHeaders.innerHTML = '';
        
        Object.entries(headers).forEach(([key, value]) => {
            const headerItem = document.createElement('div');
            headerItem.className = 'response-header-item';
            headerItem.innerHTML = `
                <div class="response-header-key">${key}:</div>
                <div class="response-header-value">${value}</div>
            `;
            this.responseHeaders.appendChild(headerItem);
        });
    }

    formatResponse(format) {
        // Update button states
        this.prettyBtn.classList.toggle('active', format === 'pretty');
        this.rawBtn.classList.toggle('active', format === 'raw');
        
        if (format === 'pretty' && this.currentResponse) {
            this.displayResponseBody(this.currentResponse);
        } else if (format === 'raw' && this.currentResponse) {
            this.responseContent.textContent = this.currentResponse.rawData;
            this.responseContent.className = '';
        }
    }

    copyResponse() {
        const content = this.responseContent.textContent;
        navigator.clipboard.writeText(content).then(() => {
            this.showSuccess('Response copied to clipboard');
        }).catch(() => {
            this.showError('Failed to copy response');
        });
    }

    // Dynamic Form Management
    addParam() {
        const paramRow = document.createElement('div');
        paramRow.className = 'param-row';
        paramRow.innerHTML = this.getEmptyParamRow();
        this.paramsList.appendChild(paramRow);
    }

    addHeader() {
        const headerRow = document.createElement('div');
        headerRow.className = 'header-row';
        headerRow.innerHTML = this.getEmptyHeaderRow();
        this.headersList.appendChild(headerRow);
    }

    addFormField() {
        const fieldRow = document.createElement('div');
        fieldRow.className = 'form-field-row';
        fieldRow.innerHTML = this.getEmptyFormFieldRow();
        this.formDataList.appendChild(fieldRow);
    }

    getEmptyParamRow() {
        return `
            <input type="text" placeholder="Key" class="param-key" />
            <input type="text" placeholder="Value" class="param-value" />
            <button class="btn btn-sm btn-danger remove-param">
                <i class="fas fa-times"></i>
            </button>
        `;
    }

    getEmptyHeaderRow() {
        return `
            <input type="text" placeholder="Header Name" class="header-key" />
            <input type="text" placeholder="Header Value" class="header-value" />
            <button class="btn btn-sm btn-danger remove-header">
                <i class="fas fa-times"></i>
            </button>
        `;
    }

    getEmptyFormFieldRow() {
        return `
            <input type="text" placeholder="Key" class="form-key" />
            <input type="text" placeholder="Value" class="form-value" />
            <button class="btn btn-sm btn-danger remove-form-field">
                <i class="fas fa-times"></i>
            </button>
        `;
    }

    handleParamAction(e) {
        if (e.target.classList.contains('remove-param')) {
            e.target.closest('.param-row').remove();
            this.updateUrlFromParams();
        }
    }

    handleHeaderAction(e) {
        if (e.target.classList.contains('remove-header')) {
            e.target.closest('.header-row').remove();
        }
    }

    handleFormFieldAction(e) {
        if (e.target.classList.contains('remove-form-field')) {
            e.target.closest('.form-field-row').remove();
        }
    }

    updateUrlFromParams() {
        const baseUrl = this.urlInput.value.split('?')[0];
        const params = new URLSearchParams();
        
        this.paramsList.querySelectorAll('.param-row').forEach(row => {
            const key = row.querySelector('.param-key').value.trim();
            const value = row.querySelector('.param-value').value.trim();
            if (key && value) {
                params.append(key, value);
            }
        });
        
        const paramString = params.toString();
        this.urlInput.value = paramString ? `${baseUrl}?${paramString}` : baseUrl;
    }

    // Body and Auth Type Switching
    switchBodyType(type) {
        // Hide all body sections
        document.querySelectorAll('.body-section').forEach(section => {
            section.style.display = 'none';
        });
        
        // Show selected section
        if (type !== 'none') {
            const sectionId = type + 'Body';
            const section = document.getElementById(sectionId);
            if (section) {
                section.style.display = 'block';
            }
        }
    }

    switchAuthType(type) {
        // Hide all auth sections
        document.querySelectorAll('.auth-section').forEach(section => {
            section.style.display = 'none';
        });
        
        // Show selected section
        if (type !== 'none') {
            const sectionId = type + 'Auth';
            const section = document.getElementById(sectionId);
            if (section) {
                section.style.display = 'block';
            }
        }
    }

    // JSON Formatting
    formatJson() {
        try {
            const jsonText = this.jsonTextarea.value.trim();
            if (jsonText) {
                const parsed = JSON.parse(jsonText);
                this.jsonTextarea.value = JSON.stringify(parsed, null, 2);
                this.showSuccess('JSON formatted successfully');
            }
        } catch (e) {
            this.showError('Invalid JSON format');
        }
    }

    highlightJson() {
        // Simple JSON syntax highlighting
        const content = this.responseContent.textContent;
        if (this.responseContent.className.includes('json')) {
            const highlighted = content
                .replace(/"([^"]+)":/g, '<span class="json-key">"$1":</span>')
                .replace(/: "([^"]+)"/g, ': <span class="json-string">"$1"</span>')
                .replace(/: (\d+)/g, ': <span class="json-number">$1</span>')
                .replace(/: (true|false)/g, ': <span class="json-boolean">$1</span>')
                .replace(/: null/g, ': <span class="json-null">null</span>');
            
            this.responseContent.innerHTML = highlighted;
        }
    }

    // History Management
    loadHistory() {
        const history = window.storageManager.getHistory();
        this.displayHistory(history);
    }

    displayHistory(history) {
        if (history.length === 0) {
            this.historyList.innerHTML = `
                <div class="empty-state">
                    <i class="fas fa-clock"></i>
                    <p>No requests yet</p>
                </div>
            `;
            return;
        }

        this.historyList.innerHTML = '';
        history.forEach(item => {
            const historyItem = document.createElement('div');
            historyItem.className = 'history-item';
            historyItem.dataset.id = item.id;
            historyItem.innerHTML = `
                <div class="history-method ${item.method}">${item.method}</div>
                <div class="history-url">${item.url}</div>
                <div class="history-time">${new Date(item.timestamp).toLocaleString()}</div>
            `;
            this.historyList.appendChild(historyItem);
        });
    }

    addToHistory(request, response) {
        const historyItem = window.storageManager.addToHistory({
            ...request,
            response: {
                status: response.status,
                statusText: response.statusText,
                responseTime: response.responseTime,
                size: response.size
            }
        });
        
        if (historyItem) {
            this.loadHistory();
        }
    }

    clearHistory() {
        if (confirm('Are you sure you want to clear all history?')) {
            window.storageManager.clearHistory();
            this.loadHistory();
        }
    }

    handleHistoryClick(e) {
        const historyItem = e.target.closest('.history-item');
        if (historyItem) {
            const id = parseInt(historyItem.dataset.id);
            this.loadHistoryItem(id);
        }
    }

    loadHistoryItem(id) {
        const history = window.storageManager.getHistory();
        const item = history.find(h => h.id === id);
        
        if (item) {
            this.loadRequestFromHistory(item);
        }
    }

    loadRequestFromHistory(item) {
        // Load basic request info
        this.methodSelect.value = item.method;
        this.urlInput.value = item.url;
        
        // Load headers
        this.headersList.innerHTML = '';
        if (item.headers) {
            Object.entries(item.headers).forEach(([key, value]) => {
                const headerRow = document.createElement('div');
                headerRow.className = 'header-row';
                headerRow.innerHTML = `
                    <input type="text" placeholder="Header Name" class="header-key" value="${key}" />
                    <input type="text" placeholder="Header Value" class="header-value" value="${value}" />
                    <button class="btn btn-sm btn-danger remove-header">
                        <i class="fas fa-times"></i>
                    </button>
                `;
                this.headersList.appendChild(headerRow);
            });
        }
        
        // Add empty header row
        this.addHeader();
        
        // Load body if present
        if (item.body) {
            if (item.body.type === 'json') {
                document.querySelector('input[name="bodyType"][value="json"]').checked = true;
                this.switchBodyType('json');
                this.jsonTextarea.value = JSON.stringify(item.body.data, null, 2);
            } else if (item.body.type === 'raw') {
                document.querySelector('input[name="bodyType"][value="raw"]').checked = true;
                this.switchBodyType('raw');
                this.rawTextarea.value = item.body.data;
            }
        }
    }

    // Collections Management
    loadCollections() {
        const collections = window.storageManager.getCollections();
        this.displayCollections(collections);
        this.updateCollectionSelect(collections);
    }

    displayCollections(collections) {
        if (collections.length === 0) {
            this.collectionsList.innerHTML = `
                <div class="empty-state">
                    <i class="fas fa-folder-open"></i>
                    <p>No collections yet</p>
                </div>
            `;
            return;
        }

        this.collectionsList.innerHTML = '';
        collections.forEach(collection => {
            const collectionItem = document.createElement('div');
            collectionItem.className = 'collection-item';
            collectionItem.dataset.id = collection.id;
            collectionItem.innerHTML = `
                <div class="collection-name">${collection.name}</div>
                <div class="collection-count">${collection.requests.length} requests</div>
            `;
            this.collectionsList.appendChild(collectionItem);
        });
    }

    updateCollectionSelect(collections) {
        this.collectionSelect.innerHTML = '<option value="">Select Collection</option>';
        collections.forEach(collection => {
            const option = document.createElement('option');
            option.value = collection.id;
            option.textContent = collection.name;
            this.collectionSelect.appendChild(option);
        });
    }

    createCollection() {
        const name = prompt('Enter collection name:');
        if (name && name.trim()) {
            const collection = window.storageManager.createCollection(name.trim());
            if (collection) {
                this.loadCollections();
                this.showSuccess('Collection created successfully');
            }
        }
    }

    handleCollectionClick(e) {
        const collectionItem = e.target.closest('.collection-item');
        if (collectionItem) {
            const id = parseInt(collectionItem.dataset.id);
            this.loadCollection(id);
        }
    }

    loadCollection(id) {
        // Implementation for loading collection requests
        console.log('Loading collection:', id);
    }

    // Save Request Modal
    showSaveModal() {
        if (!this.validateRequest()) {
            return;
        }
        
        this.loadCollections();
        this.saveModal.classList.add('active');
        this.requestName.focus();
    }

    hideSaveModal() {
        this.saveModal.classList.remove('active');
        this.requestName.value = '';
        this.collectionSelect.value = '';
    }

    saveRequest() {
        const name = this.requestName.value.trim();
        const collectionId = this.collectionSelect.value;
        
        if (!name) {
            this.showError('Please enter a request name');
            return;
        }
        
        if (!collectionId) {
            this.showError('Please select a collection');
            return;
        }
        
        const requestConfig = this.buildRequestConfig();
        const savedRequest = window.storageManager.addRequestToCollection(parseInt(collectionId), {
            name: name,
            ...requestConfig
        });
        
        if (savedRequest) {
            this.showSuccess('Request saved successfully');
            this.hideSaveModal();
            this.loadCollections();
        } else {
            this.showError('Failed to save request');
        }
    }

    // Utility Methods
    showLoading() {
        this.loadingOverlay.classList.add('active');
        this.sendBtn.disabled = true;
    }

    hideLoading() {
        this.loadingOverlay.classList.remove('active');
        this.sendBtn.disabled = false;
    }

    showSuccess(message) {
        this.showNotification(message, 'success');
    }

    showError(message) {
        this.showNotification(message, 'error');
    }

    showNotification(message, type) {
        // Simple notification system
        const notification = document.createElement('div');
        notification.className = `notification ${type}`;
        notification.textContent = message;
        notification.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            padding: 1rem 1.5rem;
            border-radius: 6px;
            color: white;
            font-weight: 500;
            z-index: 1001;
            animation: slideIn 0.3s ease-out;
            background: ${type === 'success' ? '#28a745' : '#dc3545'};
        `;
        
        document.body.appendChild(notification);
        
        setTimeout(() => {
            notification.remove();
        }, 3000);
    }

    handleKeyboardShortcuts(e) {
        // Ctrl/Cmd + Enter to send request
        if ((e.ctrlKey || e.metaKey) && e.key === 'Enter') {
            e.preventDefault();
            this.sendRequest();
        }
        
        // Escape to close modals
        if (e.key === 'Escape') {
            this.hideSaveModal();
        }
    }

    // Initialize UI
    initialize() {
        this.loadHistory();
        this.loadCollections();
        
        // Add initial empty rows
        this.addParam();
        this.addHeader();
        this.addFormField();
    }
}

// Create global instance
window.uiManager = new UIManager();