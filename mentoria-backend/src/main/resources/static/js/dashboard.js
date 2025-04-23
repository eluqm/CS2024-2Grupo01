// Tablas disponibles en el sistema
const tables = [
    { name: 'Usuarios', icon: 'fa-users', endpoint: '/usuarios', relatedEndpoints: ['/escuelas'] },
    { name: 'Escuelas', icon: 'fa-school', endpoint: '/escuelas' },
    { name: 'Grupos', icon: 'fa-users', endpoint: '/grupos' },
    { name: 'Horarios', icon: 'fa-calendar-alt', endpoint: '/horarios' },
    { name: 'Sesiones', icon: 'fa-chalkboard-teacher', endpoint: '/sesiones_mentoria' },
    { name: 'Asistencias', icon: 'fa-clipboard-list', endpoint: '/asistencias_sesiones' },
    { name: 'Eventos', icon: 'fa-calendar-check', endpoint: '/readAllEventos' }
];

// Generar las tarjetas
function generateCards() {
    const container = document.getElementById('cardsContainer');
    container.innerHTML = '';

    tables.forEach(table => {
        const card = document.createElement('div');
        card.className = 'col';
        card.innerHTML = `
            <div class="card h-100" data-endpoint="${table.endpoint}" data-name="${table.name}">
                <div class="card-body text-center">
                    <i class="fas ${table.icon} fa-3x mb-3"></i>
                    <h5 class="card-title">${table.name}</h5>
                </div>
            </div>
        `;
        container.appendChild(card);
    });
}

// Manejar la búsqueda global
document.getElementById('globalSearch').addEventListener('input', function(e) {
    const searchTerm = e.target.value.toLowerCase();
    const cards = document.querySelectorAll('.card');
    
    cards.forEach(card => {
        const title = card.querySelector('.card-title').textContent.toLowerCase();
        if (title.includes(searchTerm)) {
            card.closest('.col').style.display = '';
        } else {
            card.closest('.col').style.display = 'none';
        }
    });
});

// Manejar clics en las tarjetas
document.getElementById('cardsContainer').addEventListener('click', function(e) {
    const card = e.target.closest('.card');
    if (card) {
        const endpoint = card.dataset.endpoint;
        const tableName = card.dataset.name;
        
        if (tableName === 'Usuarios') {
            // Para la tabla Usuarios, también necesitamos consultar a Escuelas
            const related = tables.find(t => t.name === 'Usuarios').relatedEndpoints || [];
            
            // Redirigir con parámetros para consultar ambas tablas
            window.location.href = `crud.html?table=${endpoint}&relatedTables=${related.join(',')}`;
        } else {
            // Para otras tablas, comportamiento normal
            window.location.href = `crud.html?table=${endpoint}`;
        }
    }
});

// Manejar cierre de sesión
document.getElementById('logoutBtn').addEventListener('click', function(e) {
    e.preventDefault();
    localStorage.removeItem('user');
    window.location.href = 'login.html';
});

// Verificar autenticación
window.addEventListener('load', function() {
    const user = localStorage.getItem('user');
    if (!user) {
        window.location.href = 'login.html';
    } else {
        generateCards();
    }
});