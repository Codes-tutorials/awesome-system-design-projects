// Local Storage Management
class StorageManager {
    constructor() {
        this.HISTORY_KEY = 'api_client_history';
        this.COLLECTIONS_KEY = 'api_client_collections';
        this.SETTINGS_KEY = 'api_client_settings';
    }

    // History Management
    getHistory() {
        try {
            const history = localStorage.getItem(this.HISTORY_KEY);
            return history ? JSON.parse(history) : [];
        } catch (error) {
            console.error('Error loading history:', error);
            return [];
        }
    }

    addToHistory(request) {
        try {
            const history = this.getHistory();
            const historyItem = {
                id: Date.now(),
                timestamp: new Date().toISOString(),
                method: request.method,
                url: request.url,
                headers: request.headers,
                body: request.body,
                response: request.response
            };

            // Add to beginning of array
            history.unshift(historyItem);

            // Keep only last 100 requests
            if (history.length > 100) {
                history.splice(100);
            }

            localStorage.setItem(this.HISTORY_KEY, JSON.stringify(history));
            return historyItem;
        } catch (error) {
            console.error('Error saving to history:', error);
            return null;
        }
    }

    clearHistory() {
        try {
            localStorage.removeItem(this.HISTORY_KEY);
            return true;
        } catch (error) {
            console.error('Error clearing history:', error);
            return false;
        }
    }

    removeFromHistory(id) {
        try {
            const history = this.getHistory();
            const filteredHistory = history.filter(item => item.id !== id);
            localStorage.setItem(this.HISTORY_KEY, JSON.stringify(filteredHistory));
            return true;
        } catch (error) {
            console.error('Error removing from history:', error);
            return false;
        }
    }

    // Collections Management
    getCollections() {
        try {
            const collections = localStorage.getItem(this.COLLECTIONS_KEY);
            return collections ? JSON.parse(collections) : [];
        } catch (error) {
            console.error('Error loading collections:', error);
            return [];
        }
    }

    createCollection(name, description = '') {
        try {
            const collections = this.getCollections();
            const collection = {
                id: Date.now(),
                name: name,
                description: description,
                requests: [],
                createdAt: new Date().toISOString(),
                updatedAt: new Date().toISOString()
            };

            collections.push(collection);
            localStorage.setItem(this.COLLECTIONS_KEY, JSON.stringify(collections));
            return collection;
        } catch (error) {
            console.error('Error creating collection:', error);
            return null;
        }
    }

    updateCollection(id, updates) {
        try {
            const collections = this.getCollections();
            const collectionIndex = collections.findIndex(c => c.id === id);
            
            if (collectionIndex === -1) {
                return null;
            }

            collections[collectionIndex] = {
                ...collections[collectionIndex],
                ...updates,
                updatedAt: new Date().toISOString()
            };

            localStorage.setItem(this.COLLECTIONS_KEY, JSON.stringify(collections));
            return collections[collectionIndex];
        } catch (error) {
            console.error('Error updating collection:', error);
            return null;
        }
    }

    deleteCollection(id) {
        try {
            const collections = this.getCollections();
            const filteredCollections = collections.filter(c => c.id !== id);
            localStorage.setItem(this.COLLECTIONS_KEY, JSON.stringify(filteredCollections));
            return true;
        } catch (error) {
            console.error('Error deleting collection:', error);
            return false;
        }
    }

    addRequestToCollection(collectionId, request) {
        try {
            const collections = this.getCollections();
            const collection = collections.find(c => c.id === collectionId);
            
            if (!collection) {
                return null;
            }

            const savedRequest = {
                id: Date.now(),
                name: request.name,
                method: request.method,
                url: request.url,
                headers: request.headers,
                body: request.body,
                createdAt: new Date().toISOString()
            };

            collection.requests.push(savedRequest);
            collection.updatedAt = new Date().toISOString();

            localStorage.setItem(this.COLLECTIONS_KEY, JSON.stringify(collections));
            return savedRequest;
        } catch (error) {
            console.error('Error adding request to collection:', error);
            return null;
        }
    }

    removeRequestFromCollection(collectionId, requestId) {
        try {
            const collections = this.getCollections();
            const collection = collections.find(c => c.id === collectionId);
            
            if (!collection) {
                return false;
            }

            collection.requests = collection.requests.filter(r => r.id !== requestId);
            collection.updatedAt = new Date().toISOString();

            localStorage.setItem(this.COLLECTIONS_KEY, JSON.stringify(collections));
            return true;
        } catch (error) {
            console.error('Error removing request from collection:', error);
            return false;
        }
    }

    // Settings Management
    getSettings() {
        try {
            const settings = localStorage.getItem(this.SETTINGS_KEY);
            return settings ? JSON.parse(settings) : this.getDefaultSettings();
        } catch (error) {
            console.error('Error loading settings:', error);
            return this.getDefaultSettings();
        }
    }

    getDefaultSettings() {
        return {
            theme: 'light',
            requestTimeout: 30000,
            maxHistoryItems: 100,
            autoFormatJson: true,
            showResponseTime: true,
            showResponseSize: true
        };
    }

    updateSettings(updates) {
        try {
            const currentSettings = this.getSettings();
            const newSettings = { ...currentSettings, ...updates };
            localStorage.setItem(this.SETTINGS_KEY, JSON.stringify(newSettings));
            return newSettings;
        } catch (error) {
            console.error('Error updating settings:', error);
            return null;
        }
    }

    // Export/Import
    exportData() {
        try {
            const data = {
                history: this.getHistory(),
                collections: this.getCollections(),
                settings: this.getSettings(),
                exportedAt: new Date().toISOString(),
                version: '1.0'
            };
            return JSON.stringify(data, null, 2);
        } catch (error) {
            console.error('Error exporting data:', error);
            return null;
        }
    }

    importData(jsonData) {
        try {
            const data = JSON.parse(jsonData);
            
            if (data.history) {
                localStorage.setItem(this.HISTORY_KEY, JSON.stringify(data.history));
            }
            
            if (data.collections) {
                localStorage.setItem(this.COLLECTIONS_KEY, JSON.stringify(data.collections));
            }
            
            if (data.settings) {
                localStorage.setItem(this.SETTINGS_KEY, JSON.stringify(data.settings));
            }
            
            return true;
        } catch (error) {
            console.error('Error importing data:', error);
            return false;
        }
    }

    // Utility Methods
    getStorageSize() {
        try {
            let total = 0;
            for (let key in localStorage) {
                if (localStorage.hasOwnProperty(key)) {
                    total += localStorage[key].length + key.length;
                }
            }
            return total;
        } catch (error) {
            console.error('Error calculating storage size:', error);
            return 0;
        }
    }

    clearAllData() {
        try {
            localStorage.removeItem(this.HISTORY_KEY);
            localStorage.removeItem(this.COLLECTIONS_KEY);
            localStorage.removeItem(this.SETTINGS_KEY);
            return true;
        } catch (error) {
            console.error('Error clearing all data:', error);
            return false;
        }
    }
}

// Create global instance
window.storageManager = new StorageManager();