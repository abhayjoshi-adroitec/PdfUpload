const API_BASE_URL = 'http://localhost:8080/api/pdf';

let documents = [];
let bookmarks = [];
let isLoading = false;
let currentView = 'documents';

// Initialize the application
document.addEventListener('DOMContentLoaded', function() {
    loadDocuments();
    loadBookmarks();
    initializeEventListeners();
});

function initializeEventListeners() {
    // File input change event
    document.getElementById('fileInput').addEventListener('change', handleFileSelect);

    // Upload form submit
    document.getElementById('uploadForm').addEventListener('submit', handleUploadSubmit);

    // Search input
    document.getElementById('searchInput').addEventListener('input', debounce(handleSearch, 300));

    // Drag and drop events
    const fileInputDisplay = document.querySelector('.file-input-display');
    fileInputDisplay.addEventListener('dragover', handleDragOver);
    fileInputDisplay.addEventListener('dragleave', handleDragLeave);
    fileInputDisplay.addEventListener('drop', handleFileDrop);
}

// Load all documents from the server
async function loadDocuments() {
    try {
        showLoading(true);
        const response = await fetch(`${API_BASE_URL}/documents`);

        if (!response.ok) {
            throw new Error('Failed to fetch documents');
        }

        documents = await response.json();
        if (currentView === 'documents') {
            renderDocuments(documents);
        }
    } catch (error) {
        console.error('Error loading documents:', error);
        showError('Failed to load documents. Please try again.');
    } finally {
        showLoading(false);
    }
}

// Load bookmarks from the server
async function loadBookmarks() {
    try {
        const response = await fetch(`${API_BASE_URL}/bookmarks`);

        if (!response.ok) {
            throw new Error('Failed to fetch bookmarks');
        }

        bookmarks = await response.json();
        updateBookmarkCount();
        if (currentView === 'bookmarks') {
            renderBookmarks(bookmarks);
        }
    } catch (error) {
        console.error('Error loading bookmarks:', error);
        bookmarks = [];
        updateBookmarkCount();
    }
}

// Update bookmark count in tab
function updateBookmarkCount() {
    document.getElementById('bookmarkCount').textContent = bookmarks.length;
}

// Show documents view
function showDocuments() {
    currentView = 'documents';
    document.getElementById('documentsTab').classList.add('active');
    document.getElementById('bookmarksTab').classList.remove('active');
    document.getElementById('documentsSection').classList.add('active');
    document.getElementById('bookmarksSection').classList.remove('active');
    renderDocuments(documents);
}

// Show bookmarks view
function showBookmarks() {
    currentView = 'bookmarks';
    document.getElementById('documentsTab').classList.remove('active');
    document.getElementById('bookmarksTab').classList.add('active');
    document.getElementById('documentsSection').classList.remove('active');
    document.getElementById('bookmarksSection').classList.add('active');
    renderBookmarks(bookmarks);
}

// Toggle bookmarks view (for mobile button)
function toggleBookmarksView() {
    if (currentView === 'bookmarks') {
        showDocuments();
    } else {
        showBookmarks();
    }
}

// Render documents in the grid
function renderDocuments(docs) {
    const grid = document.getElementById('documentsGrid');

    if (docs.length === 0) {
        grid.innerHTML = `
            <div class="empty-state">
                <i class="fas fa-file-pdf"></i>
                <h3>No documents found</h3>
                <p>Upload your first PDF document to get started</p>
            </div>
        `;
        return;
    }

    grid.innerHTML = docs.map(doc => `
        <div class="document-card" onclick="viewDocument(${doc.id})">
            <div class="document-preview">
                <i class="fas fa-file-pdf"></i>
                ${isBookmarked(doc.id) ? '<div class="bookmark-indicator"><i class="fas fa-bookmark"></i></div>' : ''}
            </div>
            <div class="document-info">
                <h3 class="document-title">${escapeHtml(doc.title)}</h3>
                <div class="document-meta">
                    <span>${doc.pageCount || 0} pages</span>
                    <span>${formatFileSize(doc.fileSize)}</span>
                </div>
                <div class="document-meta">
                    <small>${formatDate(doc.uploadDate)}</small>
                </div>
                <div class="document-actions">
                    <button class="btn-small" onclick="event.stopPropagation(); viewDocument(${doc.id})">
                        <i class="fas fa-eye"></i> View
                    </button>
                    <button class="btn-small btn-danger" onclick="event.stopPropagation(); deleteDocument(${doc.id})">
                        <i class="fas fa-trash"></i> Delete
                    </button>
                </div>
            </div>
        </div>
    `).join('');
}

// Render bookmarks list
function renderBookmarks(bookmarksList) {
    const container = document.getElementById('bookmarksList');

    if (bookmarksList.length === 0) {
        container.innerHTML = `
            <div class="empty-state">
                <i class="fas fa-bookmark"></i>
                <h3>No bookmarks yet</h3>
                <p>Start reading documents and bookmark pages you want to return to later</p>
                <button class="btn-primary" onclick="showDocuments()">
                    <i class="fas fa-file-alt"></i>
                    Browse Documents
                </button>
            </div>
        `;
        return;
    }

    container.innerHTML = bookmarksList.map(bookmark => `
        <div class="bookmark-item" onclick="viewBookmarkedDocument(${bookmark.pdfDocument.id}, ${bookmark.pageNumber})">
            <div class="bookmark-icon">
                <i class="fas fa-bookmark"></i>
            </div>
            <div class="bookmark-info">
                <h3 class="bookmark-title">${escapeHtml(bookmark.pdfDocument.title)}</h3>
                <p class="bookmark-details">
                    <span class="bookmark-page">Page ${bookmark.pageNumber}</span>
                    ${bookmark.bookmarkName ? `<span class="bookmark-name">${escapeHtml(bookmark.bookmarkName)}</span>` : ''}
                </p>
                <small class="bookmark-date">Bookmarked ${formatDate(bookmark.createdDate)}</small>
            </div>
            <div class="bookmark-actions">
                <button class="btn-small" onclick="event.stopPropagation(); viewBookmarkedDocument(${bookmark.pdfDocument.id}, ${bookmark.pageNumber})">
                    <i class="fas fa-external-link-alt"></i> Open
                </button>
                <button class="btn-small btn-danger" onclick="event.stopPropagation(); removeBookmark(${bookmark.pdfDocument.id})">
                    <i class="fas fa-trash"></i> Remove
                </button>
            </div>
        </div>
    `).join('');
}

// Check if document is bookmarked
function isBookmarked(documentId) {
    return bookmarks.some(bookmark => bookmark.pdfDocument.id === documentId);
}

// View bookmarked document at specific page
function viewBookmarkedDocument(documentId, pageNumber) {
    window.open(`viewer.html?id=${documentId}&page=${pageNumber}`, '_blank');
}

// Remove bookmark
async function removeBookmark(documentId) {
    if (!confirm('Are you sure you want to remove this bookmark?')) {
        return;
    }

    try {
        const response = await fetch(`${API_BASE_URL}/bookmark/${documentId}`, {
            method: 'DELETE'
        });

        if (!response.ok) {
            throw new Error('Failed to remove bookmark');
        }

        // Reload bookmarks
        await loadBookmarks();
        showSuccess('Bookmark removed successfully');

        // Refresh current view
        if (currentView === 'bookmarks') {
            renderBookmarks(bookmarks);
        } else {
            renderDocuments(documents);
        }
    } catch (error) {
        console.error('Error removing bookmark:', error);
        showError('Failed to remove bookmark. Please try again.');
    }
}

// Search documents
async function searchDocuments() {
    const query = document.getElementById('searchInput').value.trim();

    if (query === '') {
        if (currentView === 'documents') {
            renderDocuments(documents);
        }
        return;
    }

    if (currentView === 'bookmarks') {
        // Filter bookmarks locally
        const filteredBookmarks = bookmarks.filter(bookmark =>
            bookmark.pdfDocument.title.toLowerCase().includes(query.toLowerCase()) ||
            (bookmark.bookmarkName && bookmark.bookmarkName.toLowerCase().includes(query.toLowerCase()))
        );
        renderBookmarks(filteredBookmarks);
        return;
    }

    try {
        const response = await fetch(`${API_BASE_URL}/search?query=${encodeURIComponent(query)}`);

        if (!response.ok) {
            throw new Error('Search failed');
        }

        const results = await response.json();
        renderDocuments(results);
    } catch (error) {
        console.error('Error searching documents:', error);
        showError('Search failed. Please try again.');
    }
}

// Handle search with debouncing
function handleSearch() {
    searchDocuments();
}

// View document
function viewDocument(id) {
    window.open(`viewer.html?id=${id}`, '_blank');
}

// Delete document
async function deleteDocument(id) {
    if (!confirm('Are you sure you want to delete this document? This will also remove any bookmarks.')) {
        return;
    }

    try {
        const response = await fetch(`${API_BASE_URL}/document/${id}`, {
            method: 'DELETE'
        });

        if (!response.ok) {
            throw new Error('Failed to delete document');
        }

        // Reload documents and bookmarks
        await Promise.all([loadDocuments(), loadBookmarks()]);
        showSuccess('Document deleted successfully');
    } catch (error) {
        console.error('Error deleting document:', error);
        showError('Failed to delete document. Please try again.');
    }
}

// Modal functions
function showUploadModal() {
    document.getElementById('uploadModal').style.display = 'block';
}

function closeUploadModal() {
    document.getElementById('uploadModal').style.display = 'none';
    document.getElementById('uploadForm').reset();
    document.getElementById('fileInputText').textContent = 'Choose PDF file or drag and drop';
    document.getElementById('uploadProgress').style.display = 'none';
}

// File handling
function handleFileSelect(event) {
    const file = event.target.files[0];
    if (file) {
        updateFileInputDisplay(file.name);
    }
}

function handleDragOver(event) {
    event.preventDefault();
    event.currentTarget.style.borderColor = '#1a73e8';
    event.currentTarget.style.backgroundColor = '#f8f9ff';
}

function handleDragLeave(event) {
    event.preventDefault();
    event.currentTarget.style.borderColor = '#e1e8ed';
    event.currentTarget.style.backgroundColor = 'transparent';
}

function handleFileDrop(event) {
    event.preventDefault();
    event.currentTarget.style.borderColor = '#e1e8ed';
    event.currentTarget.style.backgroundColor = 'transparent';

    const files = event.dataTransfer.files;
    if (files.length > 0) {
        const file = files[0];
        if (file.type === 'application/pdf') {
            document.getElementById('fileInput').files = files;
            updateFileInputDisplay(file.name);
        } else {
            showError('Please select a PDF file');
        }
    }
}

function updateFileInputDisplay(fileName) {
    document.getElementById('fileInputText').textContent = fileName;
}

// Upload form submission
async function handleUploadSubmit(event) {
    event.preventDefault();

    const fileInput = document.getElementById('fileInput');
    const titleInput = document.getElementById('titleInput');
    const file = fileInput.files[0];

    if (!file) {
        showError('Please select a file');
        return;
    }

    if (file.type !== 'application/pdf') {
        showError('Please select a PDF file');
        return;
    }

    const formData = new FormData();
    formData.append('file', file);
    if (titleInput.value.trim()) {
        formData.append('title', titleInput.value.trim());
    }

    try {
        // Show upload progress
        document.getElementById('uploadProgress').style.display = 'block';

        const response = await fetch(`${API_BASE_URL}/upload`, {
            method: 'POST',
            body: formData
        });

        if (!response.ok) {
            const error = await response.text();
            throw new Error(error);
        }

        const result = await response.json();

        // Close modal and reload documents
        closeUploadModal();
        await loadDocuments();
        showSuccess('Document uploaded successfully');

    } catch (error) {
        console.error('Error uploading document:', error);
        showError('Failed to upload document: ' + error.message);
    } finally {
        document.getElementById('uploadProgress').style.display = 'none';
    }
}

// Utility functions
function showLoading(show) {
    document.getElementById('loadingSpinner').style.display = show ? 'flex' : 'none';
}

function showError(message) {
    showNotification(message, 'error');
}

function showSuccess(message) {
    showNotification(message, 'success');
}

function showNotification(message, type = 'info') {
    const notification = document.createElement('div');
    notification.className = `notification notification-${type}`;
    notification.innerHTML = `
        <i class="fas fa-info-circle"></i>
        <span>${message}</span>
        <button class="notification-close" onclick="this.parentElement.remove()">
            <i class="fas fa-times"></i>
        </button>
    `;

    document.body.appendChild(notification);

    setTimeout(() => {
        if (notification.parentElement) {
            notification.remove();
        }
    }, 5000);
}

function formatFileSize(bytes) {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
}

function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleDateString() + ' ' + date.toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'});
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

// Close modal when clicking outside
window.addEventListener('click', function(event) {
    const modal = document.getElementById('uploadModal');
    if (event.target === modal) {
        closeUploadModal();
    }
});