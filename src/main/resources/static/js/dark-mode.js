// src/main/resources/static/js/dark-mode.js
document.addEventListener('DOMContentLoaded', function() {
    // Verificar si hay un toggle de modo oscuro en la página actual
    const darkModeSwitch = document.getElementById('darkModeSwitch');
    
    // Comprobar la preferencia de tema del usuario actual
    checkUserThemePreference();
    
    // Si existe el switch de modo oscuro, agregar event listener
    if (darkModeSwitch) {
        darkModeSwitch.addEventListener('change', function() {
            if (this.checked) {
                setDarkMode(true);
                saveDarkModePreference(true);
            } else {
                setDarkMode(false);
                saveDarkModePreference(false);
            }
        });
    }
    
    // Función para guardar la preferencia via AJAX
    function saveDarkModePreference(isDarkMode) {
        // Obtener el token CSRF
        const token = document.querySelector('meta[name="_csrf"]')?.content || '';
        const header = document.querySelector('meta[name="_csrf_header"]')?.content || 'X-CSRF-TOKEN';
        
        // Crear los headers para la petición
        const headers = new Headers({
            'Content-Type': 'application/x-www-form-urlencoded'
        });
        
        // Agregar el token CSRF si existe
        if (token) {
            headers.append(header, token);
        }
        
        fetch('/perfil/toggle-dark-mode', {
            method: 'POST',
            headers: headers,
            body: `darkMode=${isDarkMode}`
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('Error al guardar preferencia de tema');
            }
            return response.json();
        })
        .then(data => console.log('Preferencia de tema guardada:', data))
        .catch(error => console.error('Error:', error));
    }
    
    function checkUserThemePreference() {
        fetch('/perfil/check-dark-mode')
            .then(response => {
                if (!response.ok) {
                    throw new Error('Error al obtener preferencia de tema');
                }
                return response.json();
            })
            .then(data => {
                setDarkMode(data.darkMode);
                // Si hay un switch en la página, actualizarlo
                if (darkModeSwitch) {
                    darkModeSwitch.checked = data.darkMode;
                }
            })
            .catch(error => {
                console.error('Error:', error);
                // Usar el modo claro por defecto
                setDarkMode(false);
            });
    }
    
    function setDarkMode(isDarkMode) {
        if (isDarkMode) {
            document.documentElement.setAttribute('data-theme', 'dark');
        } else {
            document.documentElement.setAttribute('data-theme', 'light');
        }
    }
});