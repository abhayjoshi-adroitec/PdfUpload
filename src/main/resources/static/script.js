// PDF Document Management System - JavaScript

// Configuration
const API_BASE_URL = 'http://localhost:8080/pdf';

// Initialize PDF.js
pdfjsLib.GlobalWorkerOptions.workerSrc = 'https://cdnjs.cloudflare.com/ajax/libs/pdf.js/3.11.174/pdf.worker.min.js';

// Global variables
let currentPdfFile = null;
let pdfDoc = null;

// DOM Elements
let fileInput, uploadForm, uploadBtn, pdfPreview, documentsGrid, fileUploadArea, searchInput;

// Initialize application when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    console.log('PDF Management System - Initializing...');

    // Get DOM elements
    initializeDOMElements();

    // Test API connection
    testConnection();

    // Load existing documents
    loadDocuments();

    // Setup event listeners
    setupEventListeners();
});

/**
 * Initialize DOM elements
 */
function initializeDOMElements() {
    fileInput = document.getElementById('fileInput');
    uploadForm = document.getElementById('uploadForm');
    uploadBtn = document.getElementById('uploadBtn');
    pdfPreview = document.getElementById('pdfPreview');
    documentsGrid = document.getElementById('documentsGrid');
    fileUploadArea = document.querySelector('.file-upload-area');
    searchInput = document.getElementById('searchInput');

    console.log('DOM elements initialized');
}

/**
 * Setup all event listeners
 */
function setupEventListeners() {
    // File input change
    fileInput.addEventListener('change', handleFileSelect);

    // Form submission
    uploadForm.addEventListener('submit', handleFormSubmit);

    // Search input with debouncing
    searchInput.addEventListener('input', debounce(handleSearch, 500));

    // Drag and drop events
    fileUploadArea.addEventListener('dragover', handleDragOver);
    fileUploadArea.addEventListener('dragleave', handleDragLeave);
    fileUploadArea.addEventListener('drop', handleDrop);

    // Prevent default drag behaviors on document
    document.addEventListener('dragover', function(e) {
        e.preventDefault();
    });

    document.addEventListener('drop', function(e) {
        e.preventDefault();
    });

    console.log('Event listeners setup complete');
}

/**
 * Test API connection
 */
async function testConnection() {
    try {
        console.log('Testing API connection...');
        console.log('Testing URL:', `${API_BASE_URL}/test`);

        const response = await fetch(`${API_BASE_URL}/test`);
        console.log('Response status:', response.status);

        if (!response.ok) {
            throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }

        const result = await response.json();
        console.log('API Test Result:', result);

        if (result.success) {
            showNotification('Connected to server successfully', 'success');
        } else {
            showNotification('Server responded but with error: ' + result.message, 'error');
        }
    } catch (error) {
        console.error('API connection test failed:', error);
        showNotification('Unable to connect to server: ' + error.message, 'error');
    }
}

/**
 * Handle file selection
 */
function handleFileSelect(event) {
    const file = event.target.files[0];
    if (file && file.type === 'application/pdf') {
        processFile(file);
    } else {
        showNotification('Please select a valid PDF file', 'error');
        resetFileInput();
    }
}

/**
 * Handle drag over events
 */
function handleDragOver(event) {
    event.preventDefault();
    fileUploadArea.classList.add('dragover');
}

/**
 * Handle drag leave events
 */
function handleDragLeave(event) {
    event.preventDefault();
    fileUploadArea.classList.remove('dragover');
}

/**
 * Handle file drop events
 */
function handleDrop(event) {
    event.preventDefault();
    fileUploadArea.classList.remove('dragover');

    const files = event.dataTransfer.files;
    if (files.length > 0 && files[0].type === 'application/pdf') {
        fileInput.files = files;
        processFile(files[0]);
    } else {
        showNotification('Please drop a valid PDF file', 'error');
    }
}

/**
 * Process selected PDF file
 */
async function processFile(file) {
    console.log('Processing file:', file.name);
    currentPdfFile = file;

    // Update form fields with file information
    updateFileInfo(file);

    try {
        showLoading(true);

        // Load PDF with PDF.js to get page count and preview
        const arrayBuffer = await file.arrayBuffer();
        const loadingTask = pdfjsLib.getDocument(arrayBuffer);
        pdfDoc = await loadingTask.promise;

        // Update page count
        document.getElementById('pageCount').value = pdfDoc.numPages;

        // Render preview of first page
        await renderPreview();

        // Enable upload button
        uploadBtn.disabled = false;

        showNotification('PDF loaded successfully!', 'success');

    } catch (error) {
        console.error('Error processing PDF:', error);
        showNotification('Error loading PDF: ' + error.message, 'error');
        resetFileInput();
    } finally {
        showLoading(false);
    }
}

/**
 * Update file information in form
 */
function updateFileInfo(file) {
    document.getElementById('fileName').value = file.name;
    document.getElementById('fileSize').value = formatFileSize(file.size);

    // Auto-generate title from filename if empty
    const titleInput = document.getElementById('title');
    if (!titleInput.value.trim()) {
        titleInput.value = file.name.replace(/\.pdf$/i, '');
    }
}

/**
 * Render PDF preview
 */
async function renderPreview() {
    if (!pdfDoc) return;

    try {
        const page = await pdfDoc.getPage(1);
        const scale = 0.8;
        const viewport = page.getViewport({ scale: scale });

        // Create canvas element
        const canvas = document.createElement('canvas');
        canvas.id = 'pdfCanvas';
        const context = canvas.getContext('2d');
        canvas.height = viewport.height;
        canvas.width = viewport.width;

        // Clear preview area and add canvas
        pdfPreview.innerHTML = '';
        pdfPreview.appendChild(canvas);

        // Render PDF page
        const renderContext = {
            canvasContext: context,
            viewport: viewport
        };

        await page.render(renderContext).promise;
        console.log('PDF preview rendered successfully');

    } catch (error) {
        console.error('Error rendering preview:', error);
        pdfPreview.innerHTML = '<div class="preview-placeholder"><p>Error rendering preview</p></div>';
    }
}

/**
 * Handle form submission
 */
async function handleFormSubmit(event) {
    event.preventDefault();

    console.log('=== FORM SUBMISSION STARTED ===');
    console.log('Upload URL:', `${API_BASE_URL}/upload`);

    if (!currentPdfFile) {
        showNotification('Please select a file first', 'error');
        return;
    }

    // Validate file
    if (currentPdfFile.type !== 'application/pdf') {
        showNotification('Please select a PDF file', 'error');
        return;
    }

    // Get form values
    const formData = createFormData();

    try {
        showLoading(true);
        uploadBtn.disabled = true;

        console.log('Sending upload request...');
        const response = await fetch(`${API_BASE_URL}/upload`, {
            method: 'POST',
            body: formData
        });

        console.log('Response received:', {
            status: response.status,
            statusText: response.statusText,
            ok: response.ok
        });

        if (!response.ok) {
            const errorText = await response.text();
            console.error('Server error response:', errorText);
            throw new Error(`Server error: ${response.status} - ${errorText}`);
        }

        const result = await response.json();
        console.log('Upload success:', result);

        if (result.success) {
            showNotification('Document uploaded successfully!', 'success');
            resetForm();
            loadDocuments();
        } else {
            throw new Error(result.message || 'Upload failed for unknown reason');
        }

    } catch (error) {
        console.error('=== UPLOAD ERROR ===');
        console.error('Error:', error.message);
        showNotification('Upload failed: ' + error.message, 'error');
    } finally {
        console.log('=== UPLOAD COMPLETED ===');
        showLoading(false);
        uploadBtn.disabled = false;
    }
}

/**
 * Create FormData object from form inputs
 */
function createFormData() {
    const formData = new FormData();

    // Add file
    formData.append('file', currentPdfFile);

    // Get form values
    const title = document.getElementById('title').value.trim();
    const productCode = document.getElementById('productCode').value.trim();
    const edition = document.getElementById('edition').value.trim();
    const publicationDate = document.getElementById('publicationDate').value;
    const notes = document.getElementById('notes').value.trim();

    console.log('Form values:', { title, productCode, edition, publicationDate, notes });

    // Add form data (only if values exist)
    if (title) formData.append('title', title);
    if (productCode) formData.append('productCode', productCode);
    if (edition) formData.append('edition', edition);
    if (publicationDate) formData.append('publicationDate', publicationDate);
    if (notes) formData.append('notes', notes);
    formData.append('createdBy', 'web-user');

    return formData;
}

/**
 * Load documents from server
 */
async function loadDocuments() {
    console.log('Loading documents...');

    try {
        showLoading(true);

        const response = await fetch(`${API_BASE_URL}/documents`);
        console.log('Documents response:', response.status, response.ok);

        if (!response.ok) {
            throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }

        const result = await response.json();
        console.log('Documents loaded:', result.data?.length || 0);

        if (result.success) {
            renderDocuments(result.data || []);
            updateDocumentCount(result.data?.length || 0);
        } else {
            throw new Error(result.message || 'Failed to load documents');
        }

    } catch (error) {
        console.error('Error loading documents:', error);
        showNotification('Error loading documents: ' + error.message, 'error');
        renderDocuments([]);
        updateDocumentCount(0);
    } finally {
        showLoading(false);
    }
}

/**
 * Render documents in the grid
 */
function renderDocuments(documents) {
    if (documents.length === 0) {
        documentsGrid.innerHTML = `
            <div class="empty-state">
                <i class="fas fa-folder-open"></i>
                <h3>No documents found</h3>
                <p>Upload your first PDF document to get started</p>
            </div>
        `;
        return;
    }

    documentsGrid.innerHTML = documents.map(doc => `
        <div class="document-card">
            <div class="document-header">
                <div class="document-icon">
                    <i class="fas fa-file-pdf"></i>
                </div>
                <div style="flex: 1; min-width: 0;">
                    <div class="document-title">${escapeHtml(doc.title)}</div>
                </div>
            </div>

            <div class="document-meta">
                <div class="meta-item">
                    <span class="meta-label">Product Code:</span>
                    <span>${escapeHtml(doc.productCode || 'N/A')}</span>
                </div>
                <div class="meta-item">
                    <span class="meta-label">Edition:</span>
                    <span>${escapeHtml(doc.edition || 'N/A')}</span>
                </div>
                <div class="meta-item">
                    <span class="meta-label">Pages:</span>
                    <span>${doc.pageCount || 'N/A'}</span>
                </div>
                <div class="meta-item">
                    <span class="meta-label">Size:</span>
                    <span>${formatFileSize(doc.fileSize)}</span>
                </div>
                <div class="meta-item">
                    <span class="meta-label">Publication:</span>
                    <span>${doc.publicationDate ? new Date(doc.publicationDate).toLocaleDateString() : 'N/A'}</span>
                </div>
                <div class="meta-item">
                    <span class="meta-label">Uploaded:</span>
                    <span>${new Date(doc.uploadDate).toLocaleDateString()}</span>
                </div>
            </div>

            ${doc.notes ? `
                <div style="margin-bottom: 1rem; padding: 0.5rem; background: #f8f9fa; border-radius: 5px; font-style: italic; color: #666; border-left: 3px solid #667eea;">
                    "${escapeHtml(doc.notes)}"
                </div>
            ` : ''}

            <div class="document-actions">
                <button class="btn btn-small" onclick="viewDocument(${doc.id})">
                    <i class="fas fa-eye"></i> View
                </button>
                <button class="btn btn-small btn-secondary" onclick="downloadDocument(${doc.id})">
                    <i class="fas fa-download"></i> Download
                </button>
                <button class="btn btn-small btn-danger" onclick="deleteDocument(${doc.id})">
                    <i class="fas fa-trash"></i> Delete
                </button>
            </div>
        </div>
    `).join('');
}

/**
 * Search documents
 */
async function searchDocuments() {
    const query = searchInput.value.trim();
    console.log('Searching with query:', query);

    if (query === '') {
        loadDocuments();
        return;
    }

    try {
        showLoading(true);

        const searchUrl = `${API_BASE_URL}/search?query=${encodeURIComponent(query)}`;
        console.log('Search URL:', searchUrl);

        const response = await fetch(searchUrl);

        if (!response.ok) {
            throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }

        const result = await response.json();

        if (result.success) {
            renderDocuments(result.data || []);
            updateDocumentCount(result.data?.length || 0);
            console.log('Search completed, found:', result.data?.length || 0, 'documents');
        } else {
            throw new Error(result.message || 'Search failed');
        }

    } catch (error) {
        console.error('Search error:', error);
        showNotification('Search failed: ' + error.message, 'error');
    } finally {
        showLoading(false);
    }
}

/**
 * Clear search and reload all documents
 */
function clearSearch() {
    searchInput.value = '';
    loadDocuments();
}

/**
 * Handle search input
 */
function handleSearch() {
    searchDocuments();
}

/**
 * View document in new tab
 */
function viewDocument(id) {
    const viewUrl = `${API_BASE_URL}/view/${id}`;
    console.log('Opening document for viewing:', viewUrl);
    window.open(viewUrl, '_blank');
}

/**
 * Download document
 */
function downloadDocument(id) {
    const downloadUrl = `${API_BASE_URL}/download/${id}`;
    console.log('Downloading document:', downloadUrl);
    window.open(downloadUrl, '_blank');
}

/**
 * Delete document
 */
async function deleteDocument(id) {
    if (!confirm('Are you sure you want to delete this document?')) {
        return;
    }

    console.log('Deleting document ID:', id);

    try {
        showLoading(true);

        const response = await fetch(`${API_BASE_URL}/document/${id}`, {
            method: 'DELETE'
        });

        console.log('Delete response:', response.status, response.ok);

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(`HTTP ${response.status}: ${errorText}`);
        }

        const result = await response.json();

        if (result.success) {
            showNotification('Document deleted successfully', 'success');
            loadDocuments(); // Refresh the list
        } else {
            throw new Error(result.message || 'Delete failed');
        }

    } catch (error) {
        console.error('Delete error:', error);
        showNotification('Delete failed: ' + error.message, 'error');
    } finally {
        showLoading(false);
    }
}

/**
 * Reset form to initial state
 */
function resetForm() {
    uploadForm.reset();
    resetFileInput();
    resetPreview();
}

/**
 * Reset file input and related variables
 */
function resetFileInput() {
    currentPdfFile = null;
    pdfDoc = null;
    uploadBtn.disabled = true;
    fileInput.value = '';
}

/**
 * Reset PDF preview area
 */
function resetPreview() {
    pdfPreview.innerHTML = `
        <div class="preview-placeholder">
            <i class="fas fa-file-pdf"></i>
            <h3>No document selected</h3>
            <p>Upload a PDF to see preview</p>
        </div>
    `;
}

/**
 * Update document count display
 */
function updateDocumentCount(count) {
    document.getElementById('documentCount').textContent = count.toString();
}

/**
 * Show loading overlay
 */
function showLoading(show) {
    document.getElementById('loadingOverlay').style.display = show ? 'flex' : 'none';
}

/**
 * Show notification message
 */
function showNotification(message, type = 'info') {
    console.log(`NOTIFICATION [${type.toUpperCase()}]:`, message);

    // Remove existing notifications
    const existingNotifications = document.querySelectorAll('.notification');
    existingNotifications.forEach(notif => notif.remove());

    const notification = document.createElement('div');
    notification.className = `notification ${type}`;
    notification.innerHTML = `
        <i class="fas ${getNotificationIcon(type)}"></i>
        <span>${message}</span>
    `;

    document.body.appendChild(notification);

    // Auto-remove after 5 seconds
    setTimeout(() => {
        if (notification.parentNode) {
            notification.remove();
        }
    }, 5000);
}

/**
 * Get notification icon based on type
 */
function getNotificationIcon(type) {
    switch (type) {
        case 'success': return 'fa-check-circle';
        case 'error': return 'fa-exclamation-triangle';
        case 'info': return 'fa-info-circle';
        default: return 'fa-info-circle';
    }
}

/**
 * Format file size in human readable format
 */
function formatFileSize(bytes) {
    if (bytes === 0) return '0 Bytes';

    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));

    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
}

/**
 * Escape HTML characters to prevent XSS
 */
function escapeHtml(unsafe) {
    if (typeof unsafe !== 'string') {
        return String(unsafe);
    }

    return unsafe
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}

/**
 * Debounce function to limit function calls
 */
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

/**
 * Debug function for troubleshooting
 */
function debugCurrentState() {
    console.log('=== DEBUG STATE ===');
    console.log('API_BASE_URL:', API_BASE_URL);
    console.log('Current PDF file:', currentPdfFile);
    console.log('PDF document loaded:', !!pdfDoc);
    console.log('Upload button disabled:', uploadBtn?.disabled);
    console.log('Form values:', {
        title: document.getElementById('title')?.value,
        productCode: document.getElementById('productCode')?.value,
        edition: document.getElementById('edition')?.value,
        publicationDate: document.getElementById('publicationDate')?.value,
        notes: document.getElementById('notes')?.value
    });
    console.log('==================');
}

// Expose debug function to window for manual testing
window.debugCurrentState = debugCurrentState;