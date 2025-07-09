const API_BASE_URL = 'http://localhost:8081/reparafacil-api/api/v1/reparaciones';
let allServicios = [];
let currentServicios = [];
let userSession = null;

// Cargar servicios al iniciar
document.addEventListener('DOMContentLoaded', function() {
    checkUserSession();
    loadAllServicios();
    loadStatistics();
    setMinDateTime();
    
    // Agregar event listener para el formulario
    const newServiceForm = document.getElementById('newServiceForm');
    if (newServiceForm) {
        newServiceForm.addEventListener('submit', handleNewServiceSubmit);
    }
});

// Establecer fecha mínima para agendamiento (solo fechas futuras)
function setMinDateTime() {
    const fechaAgendadaInput = document.getElementById('fechaAgendada');
    if (fechaAgendadaInput) {
        const now = new Date();
        now.setMinutes(now.getMinutes() - now.getTimezoneOffset());
        fechaAgendadaInput.min = now.toISOString().slice(0, 16);
    }
}

// Verificar sesión de usuario
function checkUserSession() {
    const sessionData = localStorage.getItem('userSession');
    if (sessionData) {
        try {
            const session = JSON.parse(sessionData);
            const loginTime = new Date(session.loginTime);
            const now = new Date();
            const hoursDifference = (now - loginTime) / (1000 * 60 * 60);
            
            if (hoursDifference < 24) {
                userSession = session;
                updateUIForLoggedUser();
            } else {
                localStorage.removeItem('userSession');
                userSession = null;
            }
        } catch (error) {
            localStorage.removeItem('userSession');
            userSession = null;
        }
    }
    
    if (!userSession) {
        updateUIForGuest();
    }
}

// Actualizar UI para usuario logueado
function updateUIForLoggedUser() {
    const header = document.querySelector('.header');
    if (!header) return;
    
    const userInfo = document.createElement('div');
    userInfo.className = 'user-info';
    userInfo.innerHTML = `
        <div class="user-welcome">
            <span>¡Hola, ${userSession.user.nombre}! - Rol: ${userSession.user.rol}</span>
            <div class="user-actions">
                <button class="btn btn-secondary" onclick="logout()">
                    <i class="fas fa-sign-out-alt"></i> Cerrar Sesión
                </button>
            </div>
        </div>
    `;
    
    const description = header.querySelector('p');
    if (description) {
        description.insertAdjacentElement('afterend', userInfo);
    }
    
    addUserStyles();
}

// Actualizar UI para usuario invitado
function updateUIForGuest() {
    const header = document.querySelector('.header');
    if (!header) return;
    
    const loginPrompt = document.createElement('div');
    loginPrompt.className = 'login-prompt';
    loginPrompt.innerHTML = `
        <div class="guest-actions">
            <p>¿Eres técnico o administrador? Inicia sesión para gestionar servicios</p>
            <button class="btn btn-primary" onclick="goToLogin()">
                <i class="fas fa-sign-in-alt"></i> Iniciar Sesión
            </button>
        </div>
    `;
    
    const description = header.querySelector('p');
    if (description) {
        description.insertAdjacentElement('afterend', loginPrompt);
    }
    
    addGuestStyles();
}

// Ir a login
function goToLogin() {
    window.location.href = 'login.html';
}

// Cerrar sesión
function logout() {
    if (confirm('¿Estás seguro de que quieres cerrar sesión?')) {
        localStorage.removeItem('userSession');
        userSession = null;
        showError('Sesión cerrada exitosamente. ¡Hasta pronto!', true);
        setTimeout(() => {
            window.location.reload();
        }, 2000);
    }
}

// Función para realizar peticiones HTTP
async function fetchAPI(endpoint, options = {}) {
    try {
        const headers = {
            'Content-Type': 'application/json',
            ...options.headers
        };
        
        if (userSession && userSession.sessionToken) {
            headers['Authorization'] = `Bearer ${userSession.sessionToken}`;
        }
        
        const response = await fetch(`${API_BASE_URL}${endpoint}`, {
            headers: headers,
            ...options
        });
        
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        return await response.json();
    } catch (error) {
        console.error('Error fetching data:', error);
        showError('Error de conexión con el servidor. Verifica que el servicio esté ejecutándose.');
        throw error;
    }
}

// Cargar todos los servicios
async function loadAllServicios() {
    try {
        showLoading();
        const servicios = await fetchAPI('');
        allServicios = servicios;
        currentServicios = servicios;
        displayServicios(servicios);
        hideError();
    } catch (error) {
        showError('Error al cargar los servicios');
    }
}

// Cargar servicios por estado
async function loadServiciosByEstado(estado) {
    try {
        showLoading();
        const servicios = await fetchAPI(`/estado/${estado}`);
        currentServicios = servicios;
        displayServicios(servicios);
        hideError();
    } catch (error) {
        showError('Error al cargar los servicios por estado');
    }
}

// Buscar servicios
async function searchServicios() {
    const query = document.getElementById('searchInput').value.trim();
    if (!query) {
        loadAllServicios();
        return;
    }

    try {
        showLoading();
        const servicios = await fetchAPI(`/buscar?q=${encodeURIComponent(query)}`);
        currentServicios = servicios;
        displayServicios(servicios);
        hideError();
    } catch (error) {
        showError('Error al buscar servicios');
    }
}

// Filtrar servicios localmente
function filterServicios() {
    const estado = document.getElementById('estadoFilter').value;
    const tipo = document.getElementById('tipoFilter').value;
    const email = document.getElementById('emailFilter').value.trim();
    const prioridad = document.getElementById('prioridadFilter').value;

    let filtered = allServicios.filter(servicio => {
        const matchesEstado = !estado || servicio.estado === estado;
        const matchesTipo = !tipo || servicio.tipoDispositivo === tipo;
        const matchesEmail = !email || servicio.email.toLowerCase().includes(email.toLowerCase());
        const matchesPrioridad = !prioridad || servicio.prioridad === prioridad;

        return matchesEstado && matchesTipo && matchesEmail && matchesPrioridad;
    });

    currentServicios = filtered;
    displayServicios(filtered);
}

// Mostrar servicios
function displayServicios(servicios) {
    const container = document.getElementById('serviciosContainer');
    if (!container) return;
    
    if (!servicios || servicios.length === 0) {
        container.innerHTML = `
            <div class="no-services">
                <i class="fas fa-tools"></i>
                <h3>No se encontraron servicios</h3>
                <p>Intenta modificar los filtros de búsqueda</p>
            </div>
        `;
        return;
    }

    const serviciosHTML = servicios.map(servicio => createServicioCard(servicio)).join('');
    container.innerHTML = `<div class="servicios-grid">${serviciosHTML}</div>`;
}

// Crear tarjeta de servicio
function createServicioCard(servicio) {
    const estadoBadge = getEstadoBadge(servicio.estado);
    const prioridadBadge = getPrioridadBadge(servicio.prioridad);
    
    const fechaAgendada = new Date(servicio.fechaAgendada).toLocaleString('es-CL');
    const fechaCreacion = new Date(servicio.fechaCreacion).toLocaleString('es-CL');
    
    const actionButtons = userSession ? 
        `<div class="service-actions">
            <button class="btn btn-sm btn-primary" onclick="cambiarEstado(${servicio.id})">
                <i class="fas fa-edit"></i> Cambiar Estado
            </button>
            <button class="btn btn-sm btn-info" onclick="verDetalle(${servicio.id})">
                <i class="fas fa-eye"></i> Ver Detalle
            </button>
         </div>` : '';

    return `
        <div class="service-card">
            <div class="service-header">
                <div class="service-badges">
                    ${estadoBadge}
                    ${prioridadBadge}
                </div>
                <div class="service-id">#${servicio.id}</div>
            </div>
            <div class="service-info">
                <h3 class="service-title">
                    <i class="fas fa-${getDeviceIcon(servicio.tipoDispositivo)}"></i>
                    ${servicio.marca} ${servicio.modelo}
                </h3>
                <div class="service-client">
                    <strong>${servicio.nombreCliente}</strong>
                    <div class="contact-info">
                        <span><i class="fas fa-phone"></i> ${servicio.telefono}</span>
                        <span><i class="fas fa-envelope"></i> ${servicio.email}</span>
                    </div>
                </div>
                <div class="service-problem">
                    <strong>Problema:</strong>
                    <p>${servicio.descripcionProblema}</p>
                </div>
                <div class="service-dates">
                    <div class="date-info">
                        <i class="fas fa-calendar-plus"></i>
                        <strong>Agendado:</strong> ${fechaAgendada}
                    </div>
                    <div class="date-info">
                        <i class="fas fa-clock"></i>
                        <strong>Creado:</strong> ${fechaCreacion}
                    </div>
                    <div class="date-info">
                        <i class="fas fa-hourglass-half"></i>
                        <strong>Días transcurridos:</strong> ${servicio.diasTranscurridos}
                    </div>
                </div>
                ${servicio.tecnicoAsignado ? `
                <div class="service-tech">
                    <i class="fas fa-user-cog"></i>
                    <strong>Técnico:</strong> ${servicio.tecnicoAsignado}
                </div>
                ` : ''}
                ${servicio.costoEstimado ? `
                <div class="service-cost">
                    <i class="fas fa-dollar-sign"></i>
                    <strong>Costo estimado:</strong> $${servicio.costoEstimado.toLocaleString()}
                </div>
                ` : ''}
                ${actionButtons}
            </div>
        </div>
    `;
}

// Obtener badge de estado
function getEstadoBadge(estado) {
    const badges = {
        'AGENDADO': '<span class="badge badge-info">Agendado</span>',
        'EN_REVISION': '<span class="badge badge-warning">En Revisión</span>',
        'EN_REPARACION': '<span class="badge badge-primary">En Reparación</span>',
        'ESPERANDO_REPUESTOS': '<span class="badge badge-secondary">Esperando Repuestos</span>',
        'COMPLETADO': '<span class="badge badge-success">Completado</span>',
        'ENTREGADO': '<span class="badge badge-success">Entregado</span>',
        'CANCELADO': '<span class="badge badge-danger">Cancelado</span>',
        'EN_GARANTIA': '<span class="badge badge-warning">En Garantía</span>'
    };
    return badges[estado] || `<span class="badge badge-secondary">${estado}</span>`;
}

// Obtener badge de prioridad
function getPrioridadBadge(prioridad) {
    const badges = {
        'BAJA': '<span class="badge badge-light">Baja</span>',
        'NORMAL': '<span class="badge badge-info">Normal</span>',
        'ALTA': '<span class="badge badge-warning">Alta</span>',
        'URGENTE': '<span class="badge badge-danger">Urgente</span>'
    };
    return badges[prioridad] || `<span class="badge badge-secondary">${prioridad}</span>`;
}

// Obtener icono del dispositivo
function getDeviceIcon(tipo) {
    const icons = {
        'Smartphone': 'mobile-alt',
        'Laptop': 'laptop',
        'Tablet': 'tablet-alt', 
        'Computador': 'desktop',
        'Smartwatch': 'clock',
        'Auriculares': 'headphones',
        'Consola': 'gamepad'
    };
    return icons[tipo] || 'tools';
}

// Mostrar formulario de nuevo servicio
function showNewServiceForm() {
    const modal = document.getElementById('newServiceModal');
    if (modal) {
        modal.style.display = 'block';
    }
}

// Mostrar formulario de consulta
function showConsultaForm() {
    const modal = document.getElementById('consultaModal');
    if (modal) {
        modal.style.display = 'block';
    }
}

// Cerrar modal
function closeModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.style.display = 'none';
        if (modalId === 'newServiceModal') {
            const form = document.getElementById('newServiceForm');
            if (form) {
                form.reset();
            }
        }
    }
}

// Manejar formulario de nuevo servicio
function handleNewServiceSubmit(e) {
    e.preventDefault();
    
    const formData = new FormData(e.target);
    const serviceData = {
        nombreCliente: formData.get('nombreCliente'),
        telefono: formData.get('telefono'),
        email: formData.get('email'),
        tipoDispositivo: formData.get('tipoDispositivo'),
        marca: formData.get('marca'),
        modelo: formData.get('modelo'),
        descripcionProblema: formData.get('descripcionProblema'),
        fechaAgendada: new Date(formData.get('fechaAgendada')).toISOString()
    };

    createNewServicio(serviceData);
}

// Crear nuevo servicio
async function createNewServicio(serviceData) {
    try {
        showLoading();
        const response = await fetchAPI('', {
            method: 'POST',
            body: JSON.stringify(serviceData)
        });

        if (response.success) {
            showError('¡Servicio agendado exitosamente!', true);
            closeModal('newServiceModal');
            loadAllServicios();
        } else {
            showError(response.message);
        }
    } catch (error) {
        showError('Error al agendar servicio');
    }
}

// Consultar servicios por email
async function consultarPorEmail() {
    const email = document.getElementById('emailConsulta').value.trim();
    if (!email) {
        showError('Por favor ingresa un email válido');
        return;
    }

    try {
        const servicios = await fetchAPI(`/cliente/${encodeURIComponent(email)}`);
        const resultadosDiv = document.getElementById('resultadosConsulta');
        
        if (!resultadosDiv) return;
        
        if (servicios.length === 0) {
            resultadosDiv.innerHTML = `
                <div class="no-results">
                    <i class="fas fa-inbox"></i>
                    <h4>No se encontraron servicios</h4>
                    <p>No hay servicios registrados para este email.</p>
                </div>
            `;
        } else {
            const serviciosHTML = servicios.map(servicio => `
                <div class="consulta-item">
                    <div class="consulta-header">
                        <h4>${servicio.marca} ${servicio.modelo}</h4>
                        ${getEstadoBadge(servicio.estado)}
                    </div>
                    <p><strong>Problema:</strong> ${servicio.descripcionProblema}</p>
                    <div class="consulta-dates">
                        <span><i class="fas fa-calendar"></i> Agendado: ${new Date(servicio.fechaAgendada).toLocaleString('es-CL')}</span>
                        ${servicio.tecnicoAsignado ? `<span><i class="fas fa-user-cog"></i> Técnico: ${servicio.tecnicoAsignado}</span>` : ''}
                    </div>
                </div>
            `).join('');
            
            resultadosDiv.innerHTML = `
                <h4>Servicios encontrados (${servicios.length})</h4>
                ${serviciosHTML}
            `;
        }
        
        resultadosDiv.style.display = 'block';
    } catch (error) {
        showError('Error al consultar servicios');
    }
}

// Cambiar estado de servicio
async function cambiarEstado(servicioId) {
    if (!userSession) {
        showError('Debes iniciar sesión para cambiar estados');
        return;
    }

    const nuevoEstado = prompt('Ingresa el nuevo estado:\n\nAGENDADO, EN_REVISION, EN_REPARACION, ESPERANDO_REPUESTOS, COMPLETADO, ENTREGADO, CANCELADO, EN_GARANTIA');
    
    if (!nuevoEstado) return;

    try {
        const response = await fetchAPI(`/${servicioId}/estado`, {
            method: 'PUT',
            body: JSON.stringify({ estado: nuevoEstado.toUpperCase() })
        });

        if (response.success) {
            showError('Estado actualizado exitosamente', true);
            loadAllServicios();
        } else {
            showError(response.message);
        }
    } catch (error) {
        showError('Error al cambiar estado');
    }
}

// Ver detalle de servicio
function verDetalle(servicioId) {
    const servicio = allServicios.find(s => s.id === servicioId);
    if (!servicio) return;

    alert(`Detalle del Servicio #${servicio.id}

Cliente: ${servicio.nombreCliente}
Email: ${servicio.email}
Teléfono: ${servicio.telefono}

Dispositivo: ${servicio.tipoDispositivo}
Marca: ${servicio.marca}
Modelo: ${servicio.modelo}

Problema: ${servicio.descripcionProblema}

Estado: ${servicio.estadoDescripcion}
Prioridad: ${servicio.prioridad}

Fecha Agendada: ${new Date(servicio.fechaAgendada).toLocaleString('es-CL')}
Días Transcurridos: ${servicio.diasTranscurridos}

${servicio.tecnicoAsignado ? `Técnico Asignado: ${servicio.tecnicoAsignado}` : 'Sin técnico asignado'}
${servicio.costoEstimado ? `Costo Estimado: $${servicio.costoEstimado.toLocaleString()}` : ''}
${servicio.observaciones ? `Observaciones: ${servicio.observaciones}` : ''}`);
}

// Cargar estadísticas
async function loadStatistics() {
    try {
        const stats = await fetchAPI('/estadisticas');
        displayStatistics(stats);
    } catch (error) {
        console.error('Error al cargar estadísticas:', error);
    }
}

// Mostrar estadísticas
function displayStatistics(stats) {
    const statsGrid = document.getElementById('statsGrid');
    if (!statsGrid) return;
    
    statsGrid.innerHTML = `
        <div class="stat-card">
            <div class="stat-number">${stats.totalServicios || 0}</div>
            <div class="stat-label">Total Servicios</div>
        </div>
        <div class="stat-card">
            <div class="stat-number">${stats.serviciosAgendados || 0}</div>
            <div class="stat-label">Agendados</div>
        </div>
        <div class="stat-card">
            <div class="stat-number">${stats.serviciosEnReparacion || 0}</div>
            <div class="stat-label">En Reparación</div>
        </div>
        <div class="stat-card">
            <div class="stat-number">${stats.serviciosCompletados || 0}</div>
            <div class="stat-label">Completados</div>
        </div>
        <div class="stat-card">
            <div class="stat-number">${stats.totalTecnicos || 0}</div>
            <div class="stat-label">Técnicos Activos</div>
        </div>
    `;
}

// Mostrar loading
function showLoading() {
    const container = document.getElementById('serviciosContainer');
    if (container) {
        container.innerHTML = `
            <div class="loading">
                <i class="fas fa-spinner"></i>
                <div>Cargando servicios...</div>
            </div>
        `;
    }
}

// Mostrar error/éxito
function showError(message, isSuccess = false) {
    const errorClass = isSuccess ? 'success' : 'error';
    const iconClass = isSuccess ? 'fa-check-circle' : 'fa-exclamation-triangle';
    
    const errorContainer = document.getElementById('errorContainer');
    if (errorContainer) {
        errorContainer.innerHTML = `
            <div class="${errorClass}">
                <i class="fas ${iconClass}"></i>
                ${message}
            </div>
        `;
        
        setTimeout(() => {
            hideError();
        }, 5000);
    }
}

// Ocultar error
function hideError() {
    const errorContainer = document.getElementById('errorContainer');
    if (errorContainer) {
        errorContainer.innerHTML = '';
    }
}

// Evento para buscar con Enter
const searchInput = document.getElementById('searchInput');
if (searchInput) {
    searchInput.addEventListener('keypress', function(e) {
        if (e.key === 'Enter') {
            searchServicios();
        }
    });
}

// Cerrar modales al hacer click fuera
window.onclick = function(event) {
    if (event.target.classList.contains('modal')) {
        event.target.style.display = 'none';
    }
}

// Agregar estilos para usuarios
function addUserStyles() {
    if (!document.querySelector('#user-info-styles')) {
        const styles = document.createElement('style');
        styles.id = 'user-info-styles';
        styles.textContent = `
            .user-info {
                margin-top: 20px;
                padding: 15px;
                background: rgba(40, 167, 69, 0.1);
                border-radius: 15px;
                border: 2px solid rgba(40, 167, 69, 0.2);
            }
            
            .user-welcome {
                display: flex;
                justify-content: space-between;
                align-items: center;
                flex-wrap: wrap;
                gap: 15px;
            }
            
            .user-welcome span {
                font-size: 1.1rem;
                font-weight: 600;
                color: #28a745;
            }
        `;
        document.head.appendChild(styles);
    }
}

// Agregar estilos para invitados
function addGuestStyles() {
    if (!document.querySelector('#login-prompt-styles')) {
        const styles = document.createElement('style');
        styles.id = 'login-prompt-styles';
        styles.textContent = `
            .login-prompt {
                margin-top: 20px;
                padding: 15px;
                background: rgba(0, 123, 255, 0.1);
                border-radius: 15px;
                border: 2px solid rgba(0, 123, 255, 0.2);
                text-align: center;
            }
            
            .guest-actions p {
                margin-bottom: 15px;
                color: #495057;
                font-size: 1rem;
            }
        `;
        document.head.appendChild(styles);
    }
}