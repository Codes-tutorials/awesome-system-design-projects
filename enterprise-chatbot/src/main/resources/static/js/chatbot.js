class EnterpriseChatbot {
    constructor() {
        this.sessionId = null;
        this.isTyping = false;
        this.init();
    }

    init() {
        this.bindEvents();
        this.createNewSession();
        this.loadDocuments();
    }

    bindEvents() {
        // Chat events
        document.getElementById('sendBtn').addEventListener('click', () => this.sendMessage());
        document.getElementById('messageInput').addEventListener('keypress', (e) => {
            if (e.key === 'Enter' && !e.shiftKey) {
                e.preventDefault();
                this.sendMessage();
            }
        });
        document.getElementById('newSessionBtn').addEventListener('click', () => this.createNewSession());

        // File upload events
        const fileUpload = document.getElementById('fileUpload');
        const fileInput = document.getElementById('fileInput');

        fileUpload.addEventListener('click', () => fileInput.click());
        fileUpload.addEventListener('dragover', this.handleDragOver.bind(this));
        fileUpload.addEventListener('dragleave', this.handleDragLeave.bind(this));
        fileUpload.addEventListener('drop', this.handleFileDrop.bind(this));
        fileInput.addEventListener('change', this.handleFileSelect.bind(this));
    }

    async createNewSession() {
        try {
            const response = await fetch('/api/chat/session', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: 'userId=anonymous'
            });

            if (response.ok) {
                this.sessionId = await response.text();
                this.clearChat();
                this.addMessage('assistant', 'New session started! How can I help you today?');
                console.log('New session created:', this.sessionId);
            } else {
                throw new Error('Failed to create session');
            }
        } catch (error) {
            console.error('Error creating session:', error);
            this.addMessage('assistant', 'Sorry, I encountered an error starting a new session. Please refresh the page.');
        }
    }

    async sendMessage() {
        const input = document.getElementById('messageInput');
        const message = input.value.trim();

        if (!message || this.isTyping) return;

        if (!this.sessionId) {
            await this.createNewSession();
        }

        // Add user message to chat
        this.addMessage('user', message);
        input.value = '';

        // Show typing indicator
        this.showTyping();

        try {
            const response = await fetch('/api/chat/message', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    sessionId: this.sessionId,
                    message: message
                })
            });

            if (response.ok) {
                const data = await response.json();
                this.hideTyping();
                this.addMessage('assistant', data.message);
            } else {
                throw new Error('Failed to send message');
            }
        } catch (error) {
            console.error('Error sending message:', error);
            this.hideTyping();
            this.addMessage('assistant', 'Sorry, I encountered an error processing your message. Please try again.');
        }
    }

    addMessage(role, content) {
        const messagesContainer = document.getElementById('chatMessages');
        const messageDiv = document.createElement('div');
        messageDiv.className = `message ${role}`;

        const avatar = document.createElement('div');
        avatar.className = 'message-avatar';
        avatar.innerHTML = role === 'user' ? '<i class="fas fa-user"></i>' : '<i class="fas fa-robot"></i>';

        const messageContent = document.createElement('div');
        messageContent.className = 'message-content';
        messageContent.textContent = content;

        messageDiv.appendChild(avatar);
        messageDiv.appendChild(messageContent);
        messagesContainer.appendChild(messageDiv);

        // Scroll to bottom
        messagesContainer.scrollTop = messagesContainer.scrollHeight;
    }

    showTyping() {
        this.isTyping = true;
        document.getElementById('typingIndicator').style.display = 'block';
        document.getElementById('sendBtn').disabled = true;
        
        // Scroll to show typing indicator
        const messagesContainer = document.getElementById('chatMessages');
        messagesContainer.scrollTop = messagesContainer.scrollHeight;
    }

    hideTyping() {
        this.isTyping = false;
        document.getElementById('typingIndicator').style.display = 'none';
        document.getElementById('sendBtn').disabled = false;
    }

    clearChat() {
        const messagesContainer = document.getElementById('chatMessages');
        messagesContainer.innerHTML = '';
    }

    // File upload methods
    handleDragOver(e) {
        e.preventDefault();
        e.currentTarget.classList.add('dragover');
    }

    handleDragLeave(e) {
        e.preventDefault();
        e.currentTarget.classList.remove('dragover');
    }

    handleFileDrop(e) {
        e.preventDefault();
        e.currentTarget.classList.remove('dragover');
        const files = Array.from(e.dataTransfer.files);
        this.uploadFiles(files);
    }

    handleFileSelect(e) {
        const files = Array.from(e.target.files);
        this.uploadFiles(files);
        e.target.value = ''; // Reset input
    }

    async uploadFiles(files) {
        for (const file of files) {
            await this.uploadFile(file);
        }
        this.loadDocuments(); // Refresh document list
    }

    async uploadFile(file) {
        // Validate file
        if (file.size > 10 * 1024 * 1024) {
            alert(`File "${file.name}" is too large. Maximum size is 10MB.`);
            return;
        }

        const allowedTypes = ['application/pdf', 'text/plain', 'application/msword', 
                             'application/vnd.openxmlformats-officedocument.wordprocessingml.document'];
        if (!allowedTypes.includes(file.type)) {
            alert(`File "${file.name}" is not a supported type. Please upload PDF, Word, or text files.`);
            return;
        }

        const formData = new FormData();
        formData.append('file', file);

        try {
            // Add uploading indicator
            this.addDocumentToList({
                filename: file.name,
                status: 'UPLOADING',
                fileSize: file.size
            });

            const response = await fetch('/api/documents/upload', {
                method: 'POST',
                body: formData
            });

            if (response.ok) {
                const document = await response.json();
                console.log('File uploaded successfully:', document);
                this.addMessage('assistant', `Document "${file.name}" uploaded successfully and is being processed. You can ask questions about it once processing is complete.`);
            } else {
                const error = await response.text();
                throw new Error(error);
            }
        } catch (error) {
            console.error('Error uploading file:', error);
            alert(`Error uploading "${file.name}": ${error.message}`);
        }
    }

    async loadDocuments() {
        try {
            const response = await fetch('/api/documents');
            if (response.ok) {
                const documents = await response.json();
                this.displayDocuments(documents);
            }
        } catch (error) {
            console.error('Error loading documents:', error);
        }
    }

    displayDocuments(documents) {
        const documentsList = document.getElementById('documentsList');
        
        if (documents.length === 0) {
            documentsList.innerHTML = '<p style="color: #6c757d; font-style: italic;">No documents uploaded yet</p>';
            return;
        }

        documentsList.innerHTML = '';
        documents.forEach(doc => {
            this.addDocumentToList(doc);
        });
    }

    addDocumentToList(document) {
        const documentsList = document.getElementById('documentsList');
        
        // Remove "no documents" message if it exists
        const noDocsMsg = documentsList.querySelector('p');
        if (noDocsMsg && noDocsMsg.textContent.includes('No documents')) {
            noDocsMsg.remove();
        }

        // Check if document already exists (for updates)
        const existingDoc = documentsList.querySelector(`[data-filename="${document.filename}"]`);
        if (existingDoc) {
            existingDoc.remove();
        }

        const docDiv = document.createElement('div');
        docDiv.className = 'document-item';
        docDiv.setAttribute('data-filename', document.filename);

        const statusClass = document.status ? document.status.toLowerCase() : 'uploading';
        const statusText = document.status || 'UPLOADING';
        
        docDiv.innerHTML = `
            <div>
                <strong>${document.filename}</strong>
                <span class="status ${statusClass}">${statusText}</span>
            </div>
            <small>${this.formatFileSize(document.fileSize)}</small>
        `;

        documentsList.appendChild(docDiv);
    }

    formatFileSize(bytes) {
        if (bytes === 0) return '0 Bytes';
        const k = 1024;
        const sizes = ['Bytes', 'KB', 'MB', 'GB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
    }
}

// Initialize the chatbot when the page loads
document.addEventListener('DOMContentLoaded', () => {
    new EnterpriseChatbot();
});