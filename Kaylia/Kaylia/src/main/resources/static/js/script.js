
//for profile icon update to go to login page or dashboard
document.addEventListener('DOMContentLoaded', async () => {
    try {
        const response = await fetch('/auth/status', {
            method: 'GET',
            headers: {
                'Accept': 'application/json'
            }
        });

        const result = await response.json();
        const profileIcon = document.getElementById('profile-icon');
        const skinAnalysisLink = document.getElementById('skin-analysis-btn');
        const cartLink = document.getElementById('cart-icon');

        if (result.loggedIn) {
            console.log('User logged in:', result.email);

            // Redirect based on role
            switch(result.role) {
                case 'ROLE_ADMIN':
                case 'ROLE_SUPER_ADMIN':
                    profileIcon.href = '/admin/dashboard';
                    // Hide user-specific nav items
                    if (skinAnalysisLink) skinAnalysisLink.classList.add('hidden');
                    if (cartLink) cartLink.classList.add('hidden');
                    break;
                case 'ROLE_VENDOR':
                    // Hide user-specific nav items
                    if (skinAnalysisLink) skinAnalysisLink.classList.add('hidden');
                    if (cartLink) cartLink.classList.add('hidden');
                    profileIcon.href = '/vendor/dashboard';
                    break;
                default:
                    skinAnalysisLink.classList.remove('hidden');
                    cartLink.classList.remove('hidden');
                    profileIcon.href = '/dashboard'; // regular user dashboard
            }


        } else {
            profileIcon.href = '/auth/login';
        }
    } catch (error) {
        console.error('Error checking session:', error);
    }
});



// Theme Toggle Functionality
function toggleTheme() {
    const html = document.documentElement;
    const currentTheme = html.getAttribute('data-theme');
    const newTheme = currentTheme === 'light' ? 'dark' : 'light';
    
    html.setAttribute('data-theme', newTheme);
    localStorage.setItem('theme', newTheme);
    
    // Update theme toggle icon
    updateThemeIcon(newTheme);
}

function updateThemeIcon(theme) {
    const themeToggle = document.querySelector('.theme-toggle');
    if (!themeToggle) return;
    
    const sunPaths = themeToggle.querySelectorAll('.sun');
    if (theme === 'dark') {
        themeToggle.innerHTML = `
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"/>
            </svg>
        `;
    } else {
        themeToggle.innerHTML = `
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path class="sun" d="M12 1v2M12 21v2M4.22 4.22l1.42 1.42M18.36 18.36l1.42 1.42M1 12h2M21 12h2M4.22 19.78l1.42-1.42M18.36 5.64l1.42-1.42"/>
                <circle class="sun" cx="12" cy="12" r="5"/>
            </svg>
        `;
    }
}

// Initialize theme on page load
document.addEventListener('DOMContentLoaded', function() {
    const savedTheme = localStorage.getItem('theme') || 'light';
    document.documentElement.setAttribute('data-theme', savedTheme);
    updateThemeIcon(savedTheme);
    
    // Add fade-in animation to elements
    const elements = document.querySelectorAll('.card, .product-card');
    elements.forEach((el, index) => {
        el.style.animationDelay = `${index * 0.1}s`;
        el.classList.add('fade-in');
    });
});

// Cart functionality
let cart = JSON.parse(localStorage.getItem('cart')) || [];

function addToCart(product) {
    const existingItem = cart.find(item => item.id === product.id);
    
    if (existingItem) {
        existingItem.quantity += 1;
    } else {
        cart.push({ ...product, quantity: 1 });
    }
    
    localStorage.setItem('cart', JSON.stringify(cart));
    updateCartCount();
    showToast('Product added to cart!');
}

function removeFromCart(productId) {
    cart = cart.filter(item => item.id !== productId);
    localStorage.setItem('cart', JSON.stringify(cart));
    updateCartCount();
    showToast('Product removed from cart!');
}

function updateCartCount() {
    const cartButtons = document.querySelectorAll('[href="cart.html"]');
    const count = cart.reduce((total, item) => total + item.quantity, 0);
    
    cartButtons.forEach(button => {
        if (button.textContent.includes('Cart')) {
            button.textContent = `Cart (${count})`;
        }
    });
}

function showToast(message) {
    // Create toast element
    const toast = document.createElement('div');
    toast.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        background: hsl(var(--primary));
        color: white;
        padding: 1rem 1.5rem;
        border-radius: 0.5rem;
        box-shadow: var(--shadow-elegant);
        z-index: 1000;
        animation: slideIn 0.3s ease-out;
    `;
    toast.textContent = message;
    
    // Add slide-in animation
    const style = document.createElement('style');
    style.textContent = `
        @keyframes slideIn {
            from { transform: translateX(100%); opacity: 0; }
            to { transform: translateX(0); opacity: 1; }
        }
    `;
    document.head.appendChild(style);
    
    document.body.appendChild(toast);
    
    // Remove toast after 3 seconds
    setTimeout(() => {
        toast.remove();
        style.remove();
    }, 3000);
}

// Search functionality
function handleSearch(query) {
    if (!query.trim()) return;
    
    // Store search query and redirect to products page
    sessionStorage.setItem('searchQuery', query);
    window.location.href = 'products.html';
}

// Product filter functionality
function filterProducts(category) {
    const products = document.querySelectorAll('.product-card');
    
    products.forEach(product => {
        const productCategory = product.dataset.category;
        
        if (category === 'all' || productCategory === category) {
            product.style.display = 'block';
        } else {
            product.style.display = 'none';
        }
    });
    
    // Update active filter button
    const filterButtons = document.querySelectorAll('.filter-btn');
    filterButtons.forEach(btn => {
        btn.classList.remove('active');
        if (btn.dataset.category === category) {
            btn.classList.add('active');
        }
    });
}

// Sort products functionality
function sortProducts(sortBy) {
    const container = document.querySelector('.product-grid');
    if (!container) return;
    
    const products = Array.from(container.querySelectorAll('.product-card'));
    
    products.sort((a, b) => {
        switch (sortBy) {
            case 'price-low':
                return parseFloat(a.dataset.price) - parseFloat(b.dataset.price);
            case 'price-high':
                return parseFloat(b.dataset.price) - parseFloat(a.dataset.price);
            case 'name':
                return a.dataset.name.localeCompare(b.dataset.name);
            case 'rating':
                return parseFloat(b.dataset.rating) - parseFloat(a.dataset.rating);
            default:
                return 0;
        }
    });
    
    // Re-append sorted products
    products.forEach(product => container.appendChild(product));
}

// Form validation
function validateForm(form) {
    const inputs = form.querySelectorAll('input[required]');
    let isValid = true;
    
    inputs.forEach(input => {
        if (!input.value.trim()) {
            input.style.borderColor = 'hsl(var(--destructive))';
            isValid = false;
        } else {
            input.style.borderColor = 'hsl(var(--border))';
        }
    });
    
    return isValid;
}

// Mobile menu toggle
function toggleMobileMenu() {
    const menu = document.querySelector('.nav-menu');
    menu.classList.toggle('mobile-open');
}

// Initialize cart count on page load
document.addEventListener('DOMContentLoaded', updateCartCount);

// Smooth scrolling for anchor links
document.addEventListener('click', function(e) {
    if (e.target.matches('a[href^="#"]')) {
        e.preventDefault();
        const target = document.querySelector(e.target.getAttribute('href'));
        if (target) {
            target.scrollIntoView({ behavior: 'smooth' });
        }
    }
});

// Image lazy loading
function lazyLoadImages() {
    const images = document.querySelectorAll('img[data-src]');
    
    const imageObserver = new IntersectionObserver((entries, observer) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                const img = entry.target;
                img.src = img.dataset.src;
                img.classList.remove('lazy');
                observer.unobserve(img);
            }
        });
    });
    
    images.forEach(img => imageObserver.observe(img));
}

// Initialize lazy loading if supported
if ('IntersectionObserver' in window) {
    document.addEventListener('DOMContentLoaded', lazyLoadImages);
}

// --- Product Page Sidebar Filter Logic ---
function updateProductFilters() {
    const category = document.querySelector('input[name="category"]:checked').value;
    const skin = document.querySelector('input[name="skin"]:checked').value;
    const searchTerm = document.getElementById('search-input') ? document.getElementById('search-input').value.toLowerCase() : '';
    const products = document.querySelectorAll('.product-card');
    let visibleCount = 0;

    products.forEach(product => {
        const productCategory = product.dataset.category;
        const productName = product.dataset.name.toLowerCase();
        // For demo, use tags in .product-tags for skin type (if present)
        const tags = product.querySelector('.product-tags') ? product.querySelector('.product-tags').textContent.toLowerCase() : '';
        let show = true;
        if (category !== 'all' && productCategory !== category) show = false;
        if (skin !== 'all' && !tags.includes(skin)) show = false;
        if (searchTerm && !productName.includes(searchTerm)) show = false;
        product.style.display = show ? 'block' : 'none';
        if (show) visibleCount++;
    });
    updateResultsCount(visibleCount);
}

function updateResultsCount(count) {
    const resultsCount = document.getElementById('results-count');
    if (resultsCount) {
        const total = document.querySelectorAll('.product-card').length;
        resultsCount.textContent = `Showing ${count} of ${total} products`;
    }
}

document.addEventListener('DOMContentLoaded', function() {
    // Attach event listeners for sidebar filters
    document.querySelectorAll('input[name="category"]').forEach(radio => {
        radio.addEventListener('change', updateProductFilters);
    });
    document.querySelectorAll('input[name="skin"]').forEach(radio => {
        radio.addEventListener('change', updateProductFilters);
    });
    const searchInput = document.getElementById('search-input');
    if (searchInput) {
        searchInput.addEventListener('input', updateProductFilters);
    }
    const clearBtn = document.getElementById('clear-filters');
    if (clearBtn) {
        clearBtn.addEventListener('click', function() {
            document.querySelector('input[name="category"][value="all"]').checked = true;
            document.querySelector('input[name="skin"][value="all"]').checked = true;
            if (searchInput) searchInput.value = '';
            updateProductFilters();
        });
    }
    updateProductFilters();
});