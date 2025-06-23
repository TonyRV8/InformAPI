// src/main/resources/static/js/favoritos.js
document.addEventListener('DOMContentLoaded', function() {
    
    // Función para manejar la adición/eliminación de favoritos con AJAX
    function toggleFavoriteAjax(form) {
        const formData = new FormData(form);
        const token = document.querySelector('meta[name="_csrf"]')?.content || '';
        const header = document.querySelector('meta[name="_csrf_header"]')?.content || 'X-CSRF-TOKEN';
        
        // Crear los headers para la petición
        const headers = new Headers();
        
        // Agregar el token CSRF si existe
        if (token) {
            headers.append(header, token);
        }
        
        // Construir el cuerpo de la petición
        const params = new URLSearchParams();
        for (let [key, value] of formData.entries()) {
            params.append(key, value);
        }
        
        fetch(form.action, {
            method: 'POST',
            headers: headers,
            body: params
        })
        .then(response => {
            if (response.ok) {
                // Buscar el botón de favorito en este formulario
                const button = form.querySelector('button[type="submit"]');
                const icon = button.querySelector('i');
                
                // Alternar el estado visual del botón
                if (button.classList.contains('btn-warning')) {
                    // Era favorito, ahora no lo es
                    button.classList.remove('btn-warning');
                    button.classList.add('btn-outline-warning');
                    icon.classList.remove('fas');
                    icon.classList.add('far');
                    showNotification('Artículo eliminado de favoritos', 'info');
                } else {
                    // No era favorito, ahora sí lo es
                    button.classList.remove('btn-outline-warning');
                    button.classList.add('btn-warning');
                    icon.classList.remove('far');
                    icon.classList.add('fas');
                    showNotification('Artículo agregado a favoritos', 'success');
                }
            } else {
                showNotification('Error al procesar la solicitud', 'error');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            showNotification('Error de conexión', 'error');
        });
    }
    
    // Función para mostrar notificaciones
    function showNotification(message, type) {
        // Crear elemento de notificación
        const notification = document.createElement('div');
        notification.className = `alert alert-${type === 'success' ? 'success' : type === 'info' ? 'info' : 'danger'} alert-dismissible fade show position-fixed`;
        notification.style.cssText = 'top: 20px; right: 20px; z-index: 1055; min-width: 300px;';
        
        notification.innerHTML = `
            <i class="fas fa-${type === 'success' ? 'check-circle' : type === 'info' ? 'info-circle' : 'exclamation-circle'} me-2"></i>
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        `;
        
        document.body.appendChild(notification);
        
        // Auto-eliminar después de 3 segundos
        setTimeout(() => {
            if (notification.parentNode) {
                notification.remove();
            }
        }, 3000);
    }
    
    // Agregar event listeners a todos los formularios de favoritos
    const favoriteForms = document.querySelectorAll('form[action*="/favoritos/toggle"]');
    favoriteForms.forEach(form => {
        form.addEventListener('submit', function(e) {
            e.preventDefault();
            toggleFavoriteAjax(this);
        });
    });
    
    // Verificar el estado de favoritos para artículos cargados
    function checkFavoriteStatus() {
        const articleCards = document.querySelectorAll('[data-article-id]');
        
        articleCards.forEach(card => {
            const articleId = card.getAttribute('data-article-id');
            if (articleId) {
                fetch(`/favoritos/check/${encodeURIComponent(articleId)}`)
                    .then(response => response.json())
                    .then(data => {
                        const button = card.querySelector('button[type="submit"]');
                        const icon = button.querySelector('i');
                        
                        if (data.esFavorito) {
                            button.classList.remove('btn-outline-warning');
                            button.classList.add('btn-warning');
                            icon.classList.remove('far');
                            icon.classList.add('fas');
                            button.innerHTML = '<i class="fas fa-star me-1"></i> Quitar de favoritos';
                        } else {
                            button.classList.remove('btn-warning');
                            button.classList.add('btn-outline-warning');
                            icon.classList.remove('fas');
                            icon.classList.add('far');
                            button.innerHTML = '<i class="far fa-star me-1"></i> Agregar a favoritos';
                        }
                    })
                    .catch(error => console.error('Error checking favorite status:', error));
            }
        });
    }
    
    // Ejecutar verificación si estamos en la página de resultados
    if (window.location.pathname.includes('/search/results')) {
        checkFavoriteStatus();
    }
});

// Función global para confirmar eliminación de favoritos
function confirmarEliminacion(articleTitle) {
    return confirm(`¿Estás seguro de que quieres eliminar "${articleTitle}" de tus favoritos?`);
}