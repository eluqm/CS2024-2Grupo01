let currentTable = '';
let dataTable = null;
let currentEditId = null;

// Configuración de campos por tabla
const tableConfigs = {
    '/usuarios': {
        title: 'Usuarios',
        fields: [
            { name: 'userId', type: 'hidden' },
            { 
                name: 'dniUsuario', 
                type: 'text', 
                label: 'DNI', 
                required: true,
                maxLength: 20,
                pattern: '^[0-9]{8}$',
                validationMessage: 'El DNI debe tener 8 dígitos'
            },
            { 
                name: 'nombreUsuario', 
                type: 'text', 
                label: 'Nombre', 
                required: true,
                maxLength: 255
            },
            { 
                name: 'apellidoUsuario', 
                type: 'text', 
                label: 'Apellido', 
                required: true,
                maxLength: 255
            },
            { 
                name: 'celularUsuario', 
                type: 'tel', 
                label: 'Celular', 
                required: true,
                maxLength: 15,
                pattern: '^[0-9]{9}$',
                validationMessage: 'El celular debe tener 9 dígitos'
            },
            { 
                name: 'passwordHash', 
                type: 'password', 
                label: 'Contraseña', 
                required: true,
                minLength: 8,
                validationMessage: 'La contraseña debe tener al menos 8 caracteres'
            },
            { 
                name: 'escuelaId', 
                type: 'number', 
                label: 'ID Escuela', 
                required: true,
                min: 1
            },
            { 
                name: 'semestre', 
                type: 'select', 
                label: 'Semestre', 
                required: false,
                options: ['I', 'II', 'III', 'IV', 'V', 'VI', 'VII', 'VIII', 'IX', 'X', 'XI', 'XII']
            },
            { 
                name: 'email', 
                type: 'email', 
                label: 'Email', 
                required: true,
                maxLength: 255
            },
            { 
                name: 'tipoUsuario', 
                type: 'select', 
                label: 'Tipo de Usuario', 
                required: true,
                options: ['psicologia', 'coordinador', 'mentor', 'mentoriado']
            }
        ],
        columns: [
            { data: 'userId', title: 'ID' },
            { data: 'dniUsuario', title: 'DNI' },
            { data: 'nombreUsuario', title: 'Nombre' },
            { data: 'apellidoUsuario', title: 'Apellido' },
            { data: 'celularUsuario', title: 'Celular' },
            { data: 'email', title: 'Email' },
            { data: 'tipoUsuario', title: 'Tipo' },
            { data: 'semestre', title: 'Semestre' },
            { 
                data: null, 
                title: 'Acciones',
                render: function(data, type, row) {
                    return `
                        <div class="action-buttons">
                            <button class="btn btn-sm btn-primary" onclick="editRecord(${row.userId})">
                                <i class="fas fa-edit"></i>
                            </button>
                            <button class="btn btn-sm btn-danger" onclick="deleteRecord(${row.userId})">
                                <i class="fas fa-trash"></i>
                            </button>
                        </div>
                    `;
                }
            }
        ]
    },
    '/escuelas': {
        title: 'Escuelas',
        fields: [
            { name: 'id', type: 'hidden' },
            { name: 'nombre', type: 'text', label: 'Nombre', required: true },
            { name: 'direccion', type: 'text', label: 'Dirección', required: true },
            { name: 'telefono', type: 'text', label: 'Teléfono', required: true }
        ]
    },
    // Agregar configuraciones para otras tablas...
};

// Inicialización
document.addEventListener('DOMContentLoaded', function() {
    const urlParams = new URLSearchParams(window.location.search);
    currentTable = urlParams.get('table');
    
    if (!currentTable || !tableConfigs[currentTable]) {
        window.location.href = 'dashboard.html';
        return;
    }

    document.getElementById('pageTitle').textContent = tableConfigs[currentTable].title;
    document.getElementById('tableTitle').textContent = tableConfigs[currentTable].title;

    initializeTable();
    generateFilters();
    generateForm();
    loadData();
});

// Inicializar DataTable
function initializeTable() {
    dataTable = $('#dataTable').DataTable({
        language: {
            url: '//cdn.datatables.net/plug-ins/1.11.5/i18n/es-ES.json'
        },
        pageLength: 10,
        order: [[0, 'desc']],
        columns: tableConfigs[currentTable].columns,
        columnDefs: [
            { targets: -1, orderable: false }
        ]
    });
}

// Generar filtros
function generateFilters() {
    const filtersContainer = document.getElementById('filters');
    filtersContainer.innerHTML = '';

    tableConfigs[currentTable].fields.forEach(field => {
        if (field.type !== 'hidden') {
            const col = document.createElement('div');
            col.className = 'col-md-3 mb-3';
            
            let input = '';
            if (field.type === 'select') {
                input = `
                    <select class="form-select filter-input" data-field="${field.name}">
                        <option value="">Todos</option>
                        ${field.options.map(opt => `<option value="${opt}">${opt}</option>`).join('')}
                    </select>
                `;
            } else {
                input = `
                    <input type="${field.type}" class="form-control filter-input" 
                        data-field="${field.name}" placeholder="Filtrar ${field.label}">
                `;
            }

            col.innerHTML = `
                <label class="form-label">${field.label}</label>
                ${input}
            `;
            filtersContainer.appendChild(col);
        }
    });

    // Agregar evento de filtrado
    document.querySelectorAll('.filter-input').forEach(input => {
        input.addEventListener('input', function() {
            const field = this.dataset.field;
            const value = this.value;
            dataTable.column(field + ':name').search(value).draw();
        });
    });
}

// Generar formulario
function generateForm() {
    const form = document.getElementById('dataForm');
    form.innerHTML = '';

    tableConfigs[currentTable].fields.forEach(field => {
        const div = document.createElement('div');
        div.className = 'mb-3';

        let input = '';
        if (field.type === 'select') {
            input = `
                <select class="form-select" name="${field.name}" 
                    ${field.required ? 'required' : ''}>
                    ${field.options.map(opt => `<option value="${opt}">${opt}</option>`).join('')}
                </select>
            `;
        } else {
            const validationAttrs = [];
            if (field.required) validationAttrs.push('required');
            if (field.maxLength) validationAttrs.push(`maxlength="${field.maxLength}"`);
            if (field.minLength) validationAttrs.push(`minlength="${field.minLength}"`);
            if (field.pattern) validationAttrs.push(`pattern="${field.pattern}"`);
            if (field.min) validationAttrs.push(`min="${field.min}"`);

            input = `
                <input type="${field.type}" class="form-control" name="${field.name}" 
                    ${validationAttrs.join(' ')}>
            `;
        }

        div.innerHTML = `
            <label class="form-label">${field.label}</label>
            ${input}
            ${field.validationMessage ? `<div class="invalid-feedback">${field.validationMessage}</div>` : ''}
        `;

        form.appendChild(div);
    });
}

// Cargar datos
async function loadData() {
    try {
        const response = await fetch(currentTable);
        if (!response.ok) throw new Error('Error al cargar datos');
        
        const data = await response.json();
        dataTable.clear();
        dataTable.rows.add(data);
        dataTable.draw();
    } catch (error) {
        showAlert('Error al cargar datos: ' + error.message, 'danger');
    }
}

// Guardar datos
document.getElementById('saveButton').addEventListener('click', async function() {
    const form = document.getElementById('dataForm');
    if (!form.checkValidity()) {
        form.reportValidity();
        return;
    }

    const formData = new FormData(form);
    const data = Object.fromEntries(formData.entries());

    try {
        const url = currentEditId ? `${currentTable}/${currentEditId}` : currentTable;
        const method = currentEditId ? 'PUT' : 'POST';

        const response = await fetch(url, {
            method: method,
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(data)
        });

        if (!response.ok) throw new Error('Error al guardar datos');

        showAlert('Datos guardados correctamente', 'success');
        $('#createModal').modal('hide');
        loadData();
    } catch (error) {
        showAlert('Error al guardar datos: ' + error.message, 'danger');
    }
});

// Editar registro
function editRecord(id) {
    currentEditId = id;
    const row = dataTable.row(`#${id}`).data();
    
    const form = document.getElementById('dataForm');
    tableConfigs[currentTable].fields.forEach(field => {
        const input = form.querySelector(`[name="${field.name}"]`);
        if (input) input.value = row[field.name];
    });

    document.getElementById('modalTitle').textContent = 'Editar Usuario';
    $('#createModal').modal('show');
}

// Eliminar registro
async function deleteRecord(id) {
    if (!confirm('¿Está seguro de eliminar este usuario? Esta acción no se puede deshacer.')) return;

    try {
        const response = await fetch(`${currentTable}/${id}`, {
            method: 'DELETE'
        });

        if (!response.ok) throw new Error('Error al eliminar usuario');

        showAlert('Usuario eliminado correctamente', 'success');
        loadData();
    } catch (error) {
        showAlert('Error al eliminar usuario: ' + error.message, 'danger');
    }
}

// Mostrar alerta
function showAlert(message, type) {
    const alertDiv = document.createElement('div');
    alertDiv.className = `alert alert-${type} alert-dismissible fade show`;
    alertDiv.innerHTML = `
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;

    const container = document.querySelector('.container');
    container.insertBefore(alertDiv, container.firstChild);

    setTimeout(() => {
        alertDiv.remove();
    }, 5000);
}

// Limpiar formulario al cerrar modal
$('#createModal').on('hidden.bs.modal', function() {
    document.getElementById('dataForm').reset();
    currentEditId = null;
    document.getElementById('modalTitle').textContent = 'Nuevo Usuario';
}); 