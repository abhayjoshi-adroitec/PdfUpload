const API_BASE_URL = 'http://localhost:8080/api/pdf';

let pdfDoc = null;
let pageNum = 1;
let pageRendering = false;
let pageNumPending = null;
let scale = 1.0;
let canvas = null;
let ctx = null;
let documentId = null;
let currentBookmark = null;

// Initialize PDF.js
pdfjsLib.GlobalWorkerOptions.workerSrc = 'https://cdnjs.cloudflare.com/ajax/libs/pdf.js/3.11.174/pdf.worker.min.js';

// Initialize the viewer
document.addEventListener('DOMContentLoaded', function() {
    canvas = document.getElementById('pdfCanvas');
    ctx = canvas.getContext('2d');

    const urlParams = new URLSearchParams(window.location.search);
    documentId = urlParams.get('id');
    const bookmarkedPage = urlParams.get('page');

    if (documentId) {
        loadDocument(documentId, bookmarkedPage ? parseInt(bookmarkedPage) : 1);
        loadBookmarkStatus();
    } else {
        showError('No document ID provided');
    }

    // Disable print functionality
    disablePrintFunctionality();

    // Add screenshot protection (limited effectiveness)
    implementScreenshotProtection();
});

// Load bookmark status for current document
async function loadBookmarkStatus() {
    try {
        const response = await fetch(`${API_BASE_URL}/bookmark/${documentId}`);
        if (response.ok) {
            currentBookmark = await response.json();
            updateBookmarkButton(true);
        } else {
            updateBookmarkButton(false);
        }
    } catch (error) {
        console.log('No bookmark found for this document');
        updateBookmarkButton(false);
    }
}

// Update bookmark button appearance
function updateBookmarkButton(isBookmarked) {
    const bookmarkBtn = document.getElementById('bookmarkBtn');
    const icon = bookmarkBtn.querySelector('i');
    const text = bookmarkBtn.querySelector('.bookmark-text');

    if (isBookmarked) {
        icon.className = 'fas fa-bookmark';
        text.textContent = 'Bookmarked';
        bookmarkBtn.classList.add('bookmarked');
    } else {
        icon.className = 'far fa-bookmark';
        text.textContent = 'Bookmark';
        bookmarkBtn.classList.remove('bookmarked');
    }
}

// Toggle bookmark
function toggleBookmark() {
    if (currentBookmark) {
        // Remove bookmark
        removeBookmark();
    } else {
        // Show bookmark modal
        showBookmarkModal();
    }
}

// Show bookmark modal
function showBookmarkModal() {
    document.getElementById('currentPageDisplay').textContent = pageNum;
    document.getElementById('bookmarkName').value = '';
    document.getElementById('bookmarkModal').style.display = 'block';
}

// Close bookmark modal
function closeBookmarkModal() {
    document.getElementById('bookmarkModal').style.display = 'none';
}

// Save bookmark
async function saveBookmark() {
    const bookmarkName = document.getElementById('bookmarkName').value.trim();

    try {
        const response = await fetch(`${API_BASE_URL}/bookmark/${documentId}?page=${pageNum}&name=${encodeURIComponent(bookmarkName)}`, {
            method: 'POST'
        });

        if (response.ok) {
            currentBookmark = await response.json();
            updateBookmarkButton(true);
            closeBookmarkModal();
            showNotification('Bookmark saved successfully!', 'success');
        } else {
            throw new Error('Failed to save bookmark');
        }
    } catch (error) {
        console.error('Error saving bookmark:', error);
        showNotification('Failed to save bookmark', 'error');
    }
}

// Remove bookmark
async function removeBookmark() {
    if (!confirm('Are you sure you want to remove this bookmark?')) {
        return;
    }

    try {
        const response = await fetch(`${API_BASE_URL}/bookmark/${documentId}`, {
            method: 'DELETE'
        });

        if (response.ok) {
            currentBookmark = null;
            updateBookmarkButton(false);
            showNotification('Bookmark removed successfully!', 'success');
        } else {
            throw new Error('Failed to remove bookmark');
        }
    } catch (error) {
        console.error('Error removing bookmark:', error);
        showNotification('Failed to remove bookmark', 'error');
    }
}

// Download PDF with watermark
function downloadPdf() {
    document.getElementById('downloadModal').style.display = 'block';
}

// Close download modal
function closeDownloadModal() {
    document.getElementById('downloadModal').style.display = 'none';
}

// Confirm download
async function confirmDownload() {
    const progressDiv = document.getElementById('downloadProgress');
    const progressFill = progressDiv.querySelector('.progress-fill');
    const progressText = progressDiv.querySelector('.progress-text');

    try {
        // Show progress
        progressDiv.style.display = 'block';
        progressFill.style.width = '30%';
        progressText.textContent = 'Preparing watermarked PDF...';

        // Make download request
        const response = await fetch(`${API_BASE_URL}/download/${documentId}`);

        if (!response.ok) {
            throw new Error('Download failed');
        }

        progressFill.style.width = '70%';
        progressText.textContent = 'Adding watermarks...';

        // Get the blob
        const blob = await response.blob();

        progressFill.style.width = '100%';
        progressText.textContent = 'Download ready!';

        // Get filename from response headers
        const contentDisposition = response.headers.get('content-disposition');
        let filename = 'watermarked_document.pdf';
        if (contentDisposition) {
            const filenameMatch = contentDisposition.match(/filename="(.+)"/);
            if (filenameMatch) {
                filename = filenameMatch[1];
            }
        }

        // Create download link
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = filename;
        document.body.appendChild(a);
        a.click();
        window.URL.revokeObjectURL(url);
        document.body.removeChild(a);

        // Close modal
        setTimeout(() => {
            closeDownloadModal();
            showNotification('PDF downloaded with watermarks', 'success');
            progressDiv.style.display = 'none';
            progressFill.style.width = '0%';
        }, 1000);

    } catch (error) {
        console.error('Error downloading PDF:', error);
        showNotification('Failed to download PDF', 'error');
        progressDiv.style.display = 'none';
    }
}

// Disable print functionality
function disablePrintFunctionality() {
    document.addEventListener('keydown', function(e) {
        if ((e.ctrlKey || e.metaKey) && e.key === 'p') {
            e.preventDefault();
            e.stopPropagation();
            showPrintDisabledMessage();
            return false;
        }

        if (e.key === 'F12') {
            e.preventDefault();
            e.stopPropagation();
            return false;
        }

        if ((e.ctrlKey || e.metaKey) && e.shiftKey && e.key === 'I') {
            e.preventDefault();
            e.stopPropagation();
            return false;
        }

        if ((e.ctrlKey || e.metaKey) && e.key === 'u') {
            e.preventDefault();
            e.stopPropagation();
            return false;
        }

        if ((e.ctrlKey || e.metaKey) && e.key === 's') {
            e.preventDefault();
            e.stopPropagation();
            showSaveDisabledMessage();
            return false;
        }

        if (e.key === 'PrintScreen') {
            e.preventDefault();
            e.stopPropagation();
            showScreenshotDisabledMessage();
            return false;
        }
    });

    canvas.addEventListener('contextmenu', function(e) {
        e.preventDefault();
        return false;
    });

    canvas.addEventListener('selectstart', function(e) {
        e.preventDefault();
        return false;
    });

    canvas.addEventListener('dragstart', function(e) {
        e.preventDefault();
        return false;
    });

    window.print = function() {
        showPrintDisabledMessage();
        return false;
    };

    const style = document.createElement('style');
    style.textContent = `
        @media print {
            body { display: none !important; }
        }

        input[type="button"][value="Print"],
        button[title="Print"],
        *[aria-label*="Print"],
        *[title*="Print"] {
            display: none !important;
        }
    `;
    document.head.appendChild(style);
}

// Limited screenshot protection
function implementScreenshotProtection() {
    let screenshotAttempts = 0;

    document.addEventListener('keydown', function(e) {
        if (e.key === 'PrintScreen' ||
            (e.altKey && e.key === 'PrintScreen') ||
            (e.ctrlKey && e.shiftKey && e.key === 'S') ||
            (e.metaKey && e.shiftKey && e.key === '3') ||
            (e.metaKey && e.shiftKey && e.key === '4') ||
            (e.metaKey && e.shiftKey && e.key === '5')) {

            screenshotAttempts++;
            showScreenshotDisabledMessage();
            blurContentTemporarily();
        }
    });

    window.addEventListener('blur', function() {
        blurContentTemporarily();
    });

    window.addEventListener('focus', function() {
        restoreContent();
    });

    detectDevTools();
}

function blurContentTemporarily() {
    const pdfContent = document.querySelector('.pdf-content');
    if (pdfContent) {
        pdfContent.style.filter = 'blur(10px)';
        pdfContent.style.opacity = '0.3';
        showScreenshotWarning();

        setTimeout(() => {
            restoreContent();
        }, 2000);
    }
}

function restoreContent() {
    const pdfContent = document.querySelector('.pdf-content');
    if (pdfContent) {
        pdfContent.style.filter = 'none';
        pdfContent.style.opacity = '1';
        hideScreenshotWarning();
    }
}

function showScreenshotWarning() {
    let warning = document.getElementById('screenshotWarning');
    if (!warning) {
        warning = document.createElement('div');
        warning.id = 'screenshotWarning';
        warning.innerHTML = `
            <div class="screenshot-warning-content">
                <i class="fas fa-shield-alt"></i>
                <h3>Content Protected</h3>
                <p>Screenshots and screen recording are not permitted</p>
            </div>
        `;
        warning.className = 'screenshot-warning';
        document.body.appendChild(warning);
    }
    warning.style.display = 'flex';
}

function hideScreenshotWarning() {
    const warning = document.getElementById('screenshotWarning');
    if (warning) {
        warning.style.display = 'none';
    }
}

function detectDevTools() {
    let devtools = false;
    setInterval(function() {
        if (window.outerHeight - window.innerHeight > 160 || window.outerWidth - window.innerWidth > 160) {
            if (!devtools) {
                devtools = true;
                showNotification('Developer tools detected. Content protection is active.', 'warning');
                blurContentTemporarily();
            }
        } else {
            devtools = false;
        }
    }, 500);
}

function showPrintDisabledMessage() {
    showNotification('Print functionality is disabled for this document', 'warning');
}

function showSaveDisabledMessage() {
    showNotification('Save functionality is disabled for this document', 'warning');
}

function showScreenshotDisabledMessage() {
    showNotification('Screenshots are not permitted for this document', 'warning');
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

// Load document from server
async function loadDocument(id, startPage = 1) {
    try {
        showLoading(true);

        const infoResponse = await fetch(`${API_BASE_URL}/document/${id}`);
        if (!infoResponse.ok) {
            throw new Error('Failed to load document info');
        }

        const documentInfo = await infoResponse.json();
        document.getElementById('documentTitle').textContent = documentInfo.title;

        const pdfResponse = await fetch(`${API_BASE_URL}/view/${id}`);
        if (!pdfResponse.ok) {
            throw new Error('Failed to load PDF data');
        }

        const pdfArrayBuffer = await pdfResponse.arrayBuffer();

        const loadingTask = pdfjsLib.getDocument({data: pdfArrayBuffer});
        pdfDoc = await loadingTask.promise;

        document.getElementById('pageCount').textContent = '/ ' + pdfDoc.numPages;
        document.getElementById('pageInput').max = pdfDoc.numPages;

        // Start from bookmarked page if provided
        pageNum = startPage;
        document.getElementById('pageInput').value = pageNum;

        renderPage(pageNum);

    } catch (error) {
        console.error('Error loading document:', error);
        showError('Failed to load document: ' + error.message);
    } finally {
        showLoading(false);
    }
}

// Render page with watermark
async function renderPage(num) {
    if (pageRendering) {
        pageNumPending = num;
        return;
    }

    pageRendering = true;

    try {
        const page = await pdfDoc.getPage(num);
        let viewport = page.getViewport({scale: scale});

        if (scale === 'fit' || scale === 'auto') {
            const container = document.querySelector('.pdf-container');
            const containerWidth = container.clientWidth - 40;
            const containerHeight = container.clientHeight - 40;

            if (scale === 'fit') {
                scale = containerWidth / viewport.width;
            } else if (scale === 'auto') {
                const scaleW = containerWidth / viewport.width;
                const scaleH = containerHeight / viewport.height;
                scale = Math.min(scaleW, scaleH);
            }

            viewport = page.getViewport({scale: scale});

            const zoomSelect = document.getElementById('zoomSelect');
            const currentValue = zoomSelect.value;
            if (currentValue === 'fit' || currentValue === 'auto') {
                // Keep current selection
            } else {
                zoomSelect.value = scale.toFixed(2);
            }
        }

        canvas.height = viewport.height;
        canvas.width = viewport.width;

        const renderContext = {
            canvasContext: ctx,
            viewport: viewport
        };

        const renderTask = page.render(renderContext);
        await renderTask.promise;

        addWatermark();

        pageRendering = false;

        if (pageNumPending !== null) {
            renderPage(pageNumPending);
            pageNumPending = null;
        }

        document.getElementById('pageInput').value = num;
        document.getElementById('prevBtn').disabled = (num <= 1);
        document.getElementById('nextBtn').disabled = (num >= pdfDoc.numPages);

    } catch (error) {
        console.error('Error rendering page:', error);
        pageRendering = false;
    }
}

// Add watermark to canvas
function addWatermark() {
    const watermarkText = 'CONFIDENTIAL - DO NOT PRINT';

    ctx.save();
    ctx.globalAlpha = 0.1;
    ctx.font = '48px Arial';
    ctx.fillStyle = '#FF0000';
    ctx.textAlign = 'center';

    const centerX = canvas.width / 2;
    const centerY = canvas.height / 2;

    ctx.translate(centerX, centerY);
    ctx.rotate(-Math.PI / 4);
    ctx.fillText(watermarkText, 0, 0);

    ctx.restore();
}

// Navigation functions
function previousPage() {
    if (pageNum <= 1) return;
    pageNum--;
    renderPage(pageNum);
}

function nextPage() {
    if (pageNum >= pdfDoc.numPages) return;
    pageNum++;
    renderPage(pageNum);
}

function goToPage() {
    const input = document.getElementById('pageInput');
    const newPageNum = parseInt(input.value);

    if (newPageNum < 1 || newPageNum > pdfDoc.numPages) {
        input.value = pageNum;
        return;
    }

    pageNum = newPageNum;
    renderPage(pageNum);
}

// Zoom functions
function zoomIn() {
    if (typeof scale === 'number' && scale < 3) {
        scale += 0.25;
        renderPage(pageNum);
        document.getElementById('zoomSelect').value = scale.toFixed(2);
    }
}

function zoomOut() {
    if (typeof scale === 'number' && scale > 0.25) {
        scale -= 0.25;
        renderPage(pageNum);
        document.getElementById('zoomSelect').value = scale.toFixed(2);
    }
}

function changeZoom() {
    const zoomSelect = document.getElementById('zoomSelect');
    const newScale = zoomSelect.value;

    if (newScale === 'fit' || newScale === 'auto') {
        scale = newScale;
    } else {
        scale = parseFloat(newScale);
    }

    renderPage(pageNum);
}

// Fullscreen toggle
function toggleFullscreen() {
    const container = document.querySelector('.viewer-container');
    const button = event.currentTarget;
    const icon = button.querySelector('i');

    if (container.classList.contains('fullscreen')) {
        container.classList.remove('fullscreen');
        icon.className = 'fas fa-expand';
        if (document.exitFullscreen) {
            document.exitFullscreen();
        }
    } else {
        container.classList.add('fullscreen');
        icon.className = 'fas fa-compress';
        if (container.requestFullscreen) {
            container.requestFullscreen();
        }
    }
}

// Go back to document list
function goBack() {
    if (window.history.length > 1) {
        window.history.back();
    } else {
        window.location.href = 'index.html';
    }
}

// Utility functions
function showLoading(show) {
    document.getElementById('loadingSpinner').style.display = show ? 'block' : 'none';
}

function showError(message) {
    showNotification(message, 'error');
}

// Enhanced keyboard shortcuts
document.addEventListener('keydown', function(e) {
    if (e.target.tagName !== 'INPUT') {
        switch(e.key) {
            case 'ArrowLeft':
                if (!e.ctrlKey && !e.metaKey) {
                    previousPage();
                }
                break;
            case 'ArrowRight':
                if (!e.ctrlKey && !e.metaKey) {
                    nextPage();
                }
                break;
            case 'Home':
                if (!e.ctrlKey && !e.metaKey) {
                    pageNum = 1;
                    renderPage(pageNum);
                }
                break;
            case 'End':
                if (!e.ctrlKey && !e.metaKey) {
                    pageNum = pdfDoc ? pdfDoc.numPages : 1;
                    renderPage(pageNum);
                }
                break;
            case '+':
            case '=':
                if (e.ctrlKey || e.metaKey) {
                    e.preventDefault();
                    zoomIn();
                }
                break;
            case '-':
                if (e.ctrlKey || e.metaKey) {
                    e.preventDefault();
                    zoomOut();
                }
                break;
            case 'f':
                if (!e.ctrlKey && !e.metaKey) {
                    e.preventDefault();
                    toggleFullscreen();
                }
                break;
            case 'Escape':
                const container = document.querySelector('.viewer-container');
                if (container.classList.contains('fullscreen')) {
                    toggleFullscreen();
                }
                break;
        }
    }
});

// Handle window resize
window.addEventListener('resize', function() {
    if (scale === 'fit' || scale === 'auto') {
        renderPage(pageNum);
    }
});

// Close modals when clicking outside
window.addEventListener('click', function(event) {
    const bookmarkModal = document.getElementById('bookmarkModal');
    const downloadModal = document.getElementById('downloadModal');

    if (event.target === bookmarkModal) {
        closeBookmarkModal();
    }
    if (event.target === downloadModal) {
        closeDownloadModal();
    }
});

canvas.addEventListener('dragstart', function(e) {
    e.preventDefault();
    return false;
});

canvas.addEventListener('selectstart', function(e) {
    e.preventDefault();
    return false;
});