const API_BASE_URL = 'http://localhost:8081/reparafacil-api/api/v1/reparaciones';
const AUTH_API_URL = 'http://localhost:8081/reparafacil-api/api/v1/auth';
let allServicios = [];
let currentServicios = [];
let userSession = null;
let isBackendConnected = false;

// Datos demo para fallback (mantener los existentes)
const demoServicios = [
    {
        id: 1,
        nombreCliente: "María González",
        telefono: "+56 9 1234 5678",
        email: "maria.gonzalez@email.com",
        tipoDispositivo: "Smartphone",
        marca: "Samsung",
        modelo: "Galaxy S21",
        descripcionProblema: "Pantalla rota después de una caída",
        estado: "AGENDADO",
        prioridad: "ALTA",
        fechaAgendada: "2025-06-25T14:00:00",
        fechaCreacion: "2025-06-24T10:30:00",
        diasTranscurridos: 1,
        tecnicoAsignado: "Juan Pérez",
        costoEstimado: 85000
    },
    {
        id: 2,
        nombreCliente: "Carlos Rodríguez",
        telefono: "+56 9 8765 4321",
        email: "carlos.rodriguez@email.com",
        tipoDispositivo: "Laptop",
        marca: "HP",
        modelo: "Pavilion 15",
        descripcionProblema: "No enciende, posible problema con la fuente de poder",
        estado: "EN_REPARACION",
        prioridad: "NORMAL",
        fechaAgendada: "2025-06-24T09:00:00",
        fechaCreacion: "2025-06-23T16:45:00",
        diasTranscurridos: 2,
        tecnicoAsignado: "Ana López",
        costoEstimado: 45000
    },
    {
        id: 3,
        nombreCliente: "Sofía Martínez",
        telefono: "+56 9 5555 6666",
        email: "sofia.martinez@email.com",
        tipoDispositivo: "Tablet",
        marca: "iPad",
        modelo: "Air 4",
        descripcionProblema: "Batería se agota muy rápido",
        estado: "COMPLETADO",
        prioridad: "BAJA",
        fechaAgendada: "2025-06-22T11:00:00",
        fechaCreacion: "2025-06-21T14:20:00",
        diasTranscurridos: 3,
        tecnicoAsignado: "Pedro Sánchez",
        costoEstimado: 65000
    }
];

// Cargar servicios al iniciar
document.addEventListener('DOMContentLoaded', function() {
    checkUserSession();
    initializeApp();
    setMinDateTime();
    
    // Agregar event listener para el formulario
    const newServiceForm = document.getElementById('newServiceForm');
    if (newServiceForm) {
        newServiceForm.addEventListener('submit', handleNewServiceSubmit);
    }
    
    // Formatear input de costo con separadores de miles
    const costoInput = document.getElementById('costoEstimado');
    if (costoInput) {
        costoInput.addEventListener('input', formatCostoInput);
    }
});

// Inicializar aplicación - intentar conectar con backend
async function initializeApp() {
    showConnectionStatus('Conectando con el servidor...', 'connecting');
    
    try {
        // Verificar estado del backend
        await checkBackendHealth();
        
        if (isBackendConnected) {
            showConnectionStatus('✓ Conectado al servidor', 'connected');
            await loadServiciosFromBackend();
        } else {
            throw new Error('Backend no disponible');
        }
    } catch (error) {
        console.warn('Backend no disponible, usando datos demo:', error);
        showConnectionStatus('⚠ Modo offline - Datos de demostración', 'offline');
        loadDemoData();
    }
    
    await loadStatistics();
}

// Verificar salud del backend
async function checkBackendHealth() {
    try {
        const response = await fetch(`${API_BASE_URL}/health`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            },
            timeout: 5000
        });
        
        if (response.ok) {
            isBackendConnected = true;
            return true;
        }
        throw new Error('Backend health check failed');
    } catch (error) {
        console.warn('Backend health check failed:', error);
        isBackendConnected = false;
        return false;
    }
}

// Mostrar estado de conexión
function showConnectionStatus(message, type) {
    const header = document.querySelector('.header');
    let statusElement = document.getElementById('connectionStatus');
    
    if (!statusElement) {
        statusElement = document.createElement('div');
        statusElement.id = 'connectionStatus';
        statusElement.style.cssText = `
            padding: 10px;
            margin: 10px 0;
            border-radius: 8px;
            text-align: center;
            font-weight: 600;
            font-size: 14px;
        `;
        header.appendChild(statusElement);
    }
    
    statusElement.textContent = message;
    
    // Estilos según tipo de conexión
    switch (type) {
        case 'connecting':
            statusElement.style.backgroundColor = '#FFF3CD';
            statusElement.style.color = '#856404';
            statusElement.style.border = '1px solid #FFEAA7';
            break;
        case 'connected':
            statusElement.style.backgroundColor = '#D4EDDA';
            statusElement.style.color = '#155724';
            statusElement.style.border = '1px solid #C3E6CB';
            break;
        case 'offline':
            statusElement.style.backgroundColor = '#F8D7DA';
            statusElement.style.color = '#721C24';
            statusElement.style.border = '1px solid #F5C6CB';
            break;
    }
}

// Función para realizar peticiones HTTP al backend
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
        // Si la petición falla, marcar backend como desconectado
        isBackendConnected = false;
        showConnectionStatus('⚠ Conexión perdida - Modo offline', 'offline');
        throw error;
    }
}

// Cargar servicios desde el backend
async function loadServiciosFromBackend() {
    try {
        showLoading();
        console.log('Cargando servicios desde backend...');
        
        const servicios = await fetchAPI('');
        console.log('Servicios cargados:', servicios);
        
        // Adaptar formato del backend al frontend
        allServicios = servicios.map(adaptServicioFromBackend);
        currentServicios = allServicios;
        
        displayServicios(allServicios);
        hideError();
        
        console.log(`✓ ${allServicios.length} servicios cargados desde el backend`);
    } catch (error) {
        console.error('Error al cargar servicios del backend:', error);
        
        // Fallback a datos demo
        showConnectionStatus('⚠ Error de conexión - Usando datos demo', 'offline');
        loadDemoData();
        
        showError('No se pudo conectar con el servidor. Mostrando datos de demostración.', false);
    }
}

// Adaptar formato de servicio del backend al frontend
function adaptServicioFromBackend(backendServicio) {
    return {
        id: backendServicio.id,
        nombreCliente: backendServicio.nombreCliente,
        telefono: backendServicio.telefono,
        email: backendServicio.email,
        tipoDispositivo: backendServicio.tipoDispositivo,
        marca: backendServicio.marca,
        modelo: backendServicio.modelo,
        descripcionProblema: backendServicio.descripcionProblema,
        estado: backendServicio.estado,
        prioridad: backendServicio.prioridad || 'NORMAL',
        fechaAgendada: backendServicio.fechaAgendada,
        fechaCreacion: backendServicio.fechaCreacion,
        diasTranscurridos: backendServicio.diasTranscurridos,
        tecnicoAsignado: backendServicio.tecnicoAsignado,
        costoEstimado: backendServicio.costoEstimado,
        costoFinal: backendServicio.costoFinal,
        observaciones: backendServicio.observaciones
    };
}

// Adaptar formato de servicio del frontend al backend
function adaptServicioToBackend(frontendServicio) {
    return {
        nombreCliente: frontendServicio.nombreCliente,
        telefono: frontendServicio.telefono,
        email: frontendServicio.email,
        tipoDispositivo: frontendServicio.tipoDispositivo,
        marca: frontendServicio.marca,
        modelo: frontendServicio.modelo,
        descripcionProblema: frontendServicio.descripcionProblema,
        fechaAgendada: frontendServicio.fechaAgendada
    };
}

// Cargar datos demo (fallback)
function loadDemoData() {
    allServicios = demoServicios;
    currentServicios = demoServicios;
    displayServicios(demoServicios);
    console.log('Datos demo cargados como fallback');
}

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

// Cargar todos los servicios
async function loadAllServicios() {
    if (isBackendConnected) {
        await loadServiciosFromBackend();
    } else {
        displayServicios(allServicios);
        currentServicios = allServicios;
    }
}

// Cargar servicios por estado
async function loadServiciosByEstado(estado) {
    try {
        showLoading();
        
        if (isBackendConnected) {
            const servicios = await fetchAPI(`/estado/${estado}`);
            const adaptedServicios = servicios.map(adaptServicioFromBackend);
            displayServicios(adaptedServicios);
            currentServicios = adaptedServicios;
        } else {
            const filtered = allServicios.filter(s => s.estado === estado);
            displayServicios(filtered);
            currentServicios = filtered;
        }
        
        hideError();
    } catch (error) {
        console.error('Error al cargar servicios por estado:', error);
        showError('Error al cargar servicios por estado');
        // Fallback local
        const filtered = allServicios.filter(s => s.estado === estado);
        displayServicios(filtered);
        currentServicios = filtered;
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
        
        if (isBackendConnected) {
            const servicios = await fetchAPI(`/buscar?q=${encodeURIComponent(query)}`);
            const adaptedServicios = servicios.map(adaptServicioFromBackend);
            displayServicios(adaptedServicios);
            currentServicios = adaptedServicios;
        } else {
            // Búsqueda local
            const filtered = allServicios.filter(servicio => 
                servicio.nombreCliente.toLowerCase().includes(query.toLowerCase()) ||
                servicio.email.toLowerCase().includes(query.toLowerCase()) ||
                servicio.tipoDispositivo.toLowerCase().includes(query.toLowerCase()) ||
                servicio.marca.toLowerCase().includes(query.toLowerCase()) ||
                servicio.modelo.toLowerCase().includes(query.toLowerCase()) ||
                servicio.descripcionProblema.toLowerCase().includes(query.toLowerCase())
            );
            
            displayServicios(filtered);
            currentServicios = filtered;
        }
        
        hideError();
    } catch (error) {
        console.error('Error al buscar servicios:', error);
        showError('Error al buscar servicios');
        
        // Fallback búsqueda local
        const filtered = allServicios.filter(servicio => 
            servicio.nombreCliente.toLowerCase().includes(query.toLowerCase()) ||
            servicio.email.toLowerCase().includes(query.toLowerCase()) ||
            servicio.tipoDispositivo.toLowerCase().includes(query.toLowerCase()) ||
            servicio.marca.toLowerCase().includes(query.toLowerCase()) ||
            servicio.modelo.toLowerCase().includes(query.toLowerCase()) ||
            servicio.descripcionProblema.toLowerCase().includes(query.toLowerCase())
        );
        
        displayServicios(filtered);
        currentServicios = filtered;
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
                ${!isBackendConnected ? '<p><small>Modo offline - Datos limitados</small></p>' : ''}
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

    const modeIndicator = !isBackendConnected ? 
        '<div class="mode-indicator">🔒 Modo Demo</div>' : '';

    return `
        <div class="service-card">
            ${modeIndicator}
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
                    <strong>Costo estimado:</strong> $${servicio.costoEstimado.toLocaleString('es-CL')}
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
                // Limpiar preview de costo
                const preview = form.querySelector('.cost-preview');
                if (preview) {
                    preview.remove();
                }
            }
        }
    }
}

// Formatear input de costo con separadores de miles
function formatCostoInput(e) {
    let value = e.target.value.replace(/\D/g, ''); // Solo números
    if (value) {
        // Convertir a número y formatear con separadores de miles
        const numberValue = parseInt(value);
        e.target.value = numberValue;
        
        // Mostrar preview formateado
        const preview = e.target.parentNode.querySelector('.cost-preview');
        if (preview) {
            preview.remove();
        }
        
        if (numberValue > 0) {
            const formattedValue = numberValue.toLocaleString('es-CL');
            const previewElement = document.createElement('small');
            previewElement.className = 'cost-preview';
            previewElement.style.color = '#0D47A1';
            previewElement.style.fontSize = '12px';
            previewElement.style.fontWeight = 'bold';
            previewElement.textContent = `Formato: $${formattedValue}`;
            e.target.parentNode.appendChild(previewElement);
        }
    }
}

// Manejar formulario de nuevo servicio
async function handleNewServiceSubmit(e) {
    e.preventDefault();
    
    const formData = new FormData(e.target);
    const costoEstimado = formData.get('costoEstimado');
    
    const serviceData = {
        nombreCliente: formData.get('nombreCliente'),
        telefono: formData.get('telefono'),
        email: formData.get('email'),
        tipoDispositivo: formData.get('tipoDispositivo'),
        marca: formData.get('marca'),
        modelo: formData.get('modelo'),
        descripcionProblema: formData.get('descripcionProblema'),
        fechaAgendada: formData.get('fechaAgendada')
    };

    try {
        if (isBackendConnected) {
            // Enviar al backend
            const response = await fetchAPI('', {
                method: 'POST',
                body: JSON.stringify(serviceData)
            });
            
            if (response.success) {
                showError('¡Servicio agendado exitosamente en el servidor!', true);
                closeModal('newServiceModal');
                await loadAllServicios();
                await loadStatistics();
            } else {
                throw new Error(response.message || 'Error al crear servicio');
            }
        } else {
            // Modo demo - agregar localmente
            const newService = {
                id: allServicios.length + 1,
                ...serviceData,
                fechaCreacion: new Date().toISOString(),
                estado: 'AGENDADO',
                prioridad: formData.get('prioridad') || 'NORMAL',
                tecnicoAsignado: formData.get('tecnicoAsignado') || null,
                costoEstimado: costoEstimado ? parseInt(costoEstimado) : null,
                diasTranscurridos: 0
            };

            allServicios.push(newService);
            showError('¡Servicio agendado en modo demo!', true);
            closeModal('newServiceModal');
            loadAllServicios();
            loadStatistics();
        }
    } catch (error) {
        console.error('Error al crear servicio:', error);
        showError(`Error al agendar servicio: ${error.message}`);
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
        let servicios = [];
        
        if (isBackendConnected) {
            const response = await fetchAPI(`/cliente/${encodeURIComponent(email)}`);
            servicios = response.map(adaptServicioFromBackend);
        } else {
            servicios = allServicios.filter(s => s.email.toLowerCase().includes(email.toLowerCase()));
        }
        
        const resultadosDiv = document.getElementById('resultadosConsulta');
        
        if (!resultadosDiv) return;
        
        if (servicios.length === 0) {
            resultadosDiv.innerHTML = `
                <div class="no-results">
                    <i class="fas fa-inbox"></i>
                    <h4>No se encontraron servicios</h4>
                    <p>No hay servicios registrados para este email.</p>
                    ${!isBackendConnected ? '<p><small>Búsqueda en modo offline</small></p>' : ''}
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
                    ${servicio.costoEstimado ? `<p><strong>Costo estimado:</strong> ${servicio.costoEstimado.toLocaleString('es-CL')}</p>` : ''}
                    <div class="consulta-dates">
                        <span><i class="fas fa-calendar"></i> Agendado: ${new Date(servicio.fechaAgendada).toLocaleString('es-CL')}</span>
                        ${servicio.tecnicoAsignado ? `<span><i class="fas fa-user-cog"></i> Técnico: ${servicio.tecnicoAsignado}</span>` : ''}
                    </div>
                </div>
            `).join('');
            
            resultadosDiv.innerHTML = `
                <h4>Servicios encontrados (${servicios.length})</h4>
                ${!isBackendConnected ? '<p><small>Resultados desde datos locales</small></p>' : ''}
                ${serviciosHTML}
            `;
        }
        
        resultadosDiv.style.display = 'block';
    } catch (error) {
        console.error('Error al consultar servicios:', error);
        showError('Error al consultar servicios');
        
        // Fallback búsqueda local
        const servicios = allServicios.filter(s => s.email.toLowerCase().includes(email.toLowerCase()));
        const resultadosDiv = document.getElementById('resultadosConsulta');
        
        if (resultadosDiv) {
            if (servicios.length === 0) {
                resultadosDiv.innerHTML = `
                    <div class="no-results">
                        <i class="fas fa-inbox"></i>
                        <h4>No se encontraron servicios</h4>
                        <p>No hay servicios registrados para este email.</p>
                        <p><small>Búsqueda local por error de conexión</small></p>
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
                        ${servicio.costoEstimado ? `<p><strong>Costo estimado:</strong> ${servicio.costoEstimado.toLocaleString('es-CL')}</p>` : ''}
                        <div class="consulta-dates">
                            <span><i class="fas fa-calendar"></i> Agendado: ${new Date(servicio.fechaAgendada).toLocaleString('es-CL')}</span>
                            ${servicio.tecnicoAsignado ? `<span><i class="fas fa-user-cog"></i> Técnico: ${servicio.tecnicoAsignado}</span>` : ''}
                        </div>
                    </div>
                `).join('');
                
                resultadosDiv.innerHTML = `
                    <h4>Servicios encontrados (${servicios.length})</h4>
                    <p><small>Resultados desde datos locales por error de conexión</small></p>
                    ${serviciosHTML}
                `;
            }
            resultadosDiv.style.display = 'block';
        }
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
        if (isBackendConnected) {
            // Actualizar en el backend
            const response = await fetchAPI(`/${servicioId}/estado`, {
                method: 'PUT',
                body: JSON.stringify({ estado: nuevoEstado.toUpperCase() })
            });
            
            if (response.success) {
                showError('Estado actualizado exitosamente en el servidor', true);
                await loadAllServicios();
                await loadStatistics();
            } else {
                throw new Error(response.message || 'Error al cambiar estado');
            }
        } else {
            // Modo demo - actualizar localmente
            const servicio = allServicios.find(s => s.id === servicioId);
            if (servicio) {
                servicio.estado = nuevoEstado.toUpperCase();
                showError('Estado actualizado en modo demo', true);
                displayServicios(currentServicios);
                loadStatistics();
            }
        }
    } catch (error) {
        console.error('Error al cambiar estado:', error);
        showError(`Error al cambiar estado: ${error.message}`);
        
        // Fallback local en caso de error
        const servicio = allServicios.find(s => s.id === servicioId);
        if (servicio) {
            servicio.estado = nuevoEstado.toUpperCase();
            showError('Estado actualizado localmente (error de conexión)', true);
            displayServicios(currentServicios);
            loadStatistics();
        }
    }
}

// Ver detalle de servicio
function verDetalle(servicioId) {
    const servicio = allServicios.find(s => s.id === servicioId);
    if (!servicio) return;

    const modeText = isBackendConnected ? 'Datos del servidor' : 'Datos demo/locales';

    alert(`Detalle del Servicio #${servicio.id} (${modeText})

Cliente: ${servicio.nombreCliente}
Email: ${servicio.email}
Teléfono: ${servicio.telefono}

Dispositivo: ${servicio.tipoDispositivo}
Marca: ${servicio.marca}
Modelo: ${servicio.modelo}

Problema: ${servicio.descripcionProblema}

Estado: ${servicio.estado}
Prioridad: ${servicio.prioridad}

Fecha Agendada: ${new Date(servicio.fechaAgendada).toLocaleString('es-CL')}
Días Transcurridos: ${servicio.diasTranscurridos}

${servicio.tecnicoAsignado ? `Técnico Asignado: ${servicio.tecnicoAsignado}` : 'Sin técnico asignado'}
${servicio.costoEstimado ? `Costo Estimado: ${servicio.costoEstimado.toLocaleString('es-CL')}` : 'Sin costo estimado'}
${servicio.observaciones ? `Observaciones: ${servicio.observaciones}` : ''}`);
}

// Cargar estadísticas
async function loadStatistics() {
    try {
        let stats;
        
        if (isBackendConnected) {
            // Obtener estadísticas del backend
            stats = await fetchAPI('/estadisticas');
        } else {
            // Calcular estadísticas locales
            stats = {
                totalServicios: allServicios.length,
                serviciosAgendados: allServicios.filter(s => s.estado === 'AGENDADO').length,
                serviciosEnReparacion: allServicios.filter(s => s.estado === 'EN_REPARACION').length,
                serviciosCompletados: allServicios.filter(s => s.estado === 'COMPLETADO').length,
                totalTecnicos: new Set(allServicios.map(s => s.tecnicoAsignado).filter(t => t)).size || 5
            };
        }
        
        displayStatistics(stats);
    } catch (error) {
        console.warn('Error al cargar estadísticas del backend, usando cálculo local:', error);
        
        // Fallback - calcular estadísticas locales
        const stats = {
            totalServicios: allServicios.length,
            serviciosAgendados: allServicios.filter(s => s.estado === 'AGENDADO').length,
            serviciosEnReparacion: allServicios.filter(s => s.estado === 'EN_REPARACION').length,
            serviciosCompletados: allServicios.filter(s => s.estado === 'COMPLETADO').length,
            totalTecnicos: new Set(allServicios.map(s => s.tecnicoAsignado).filter(t => t)).size || 5
        };
        displayStatistics(stats);
    }
}

// Mostrar estadísticas
function displayStatistics(stats) {
    const statsGrid = document.getElementById('statsGrid');
    if (!statsGrid) return;
    
    const modeIndicator = !isBackendConnected ? '<small>📊 Datos locales</small>' : '<small>🌐 Datos del servidor</small>';
    
    statsGrid.innerHTML = `
        <div class="stat-card">
            <div class="stat-number">${stats.totalServicios || 0}</div>
            <div class="stat-label">Total Servicios</div>
            ${modeIndicator}
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
                ${!isBackendConnected ? '<small>Modo offline</small>' : '<small>Conectando con servidor</small>'}
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

// Reconectar con el backend
async function tryReconnect() {
    showConnectionStatus('Intentando reconectar...', 'connecting');
    
    try {
        await checkBackendHealth();
        
        if (isBackendConnected) {
            showConnectionStatus('✓ Reconectado al servidor', 'connected');
            await loadServiciosFromBackend();
            await loadStatistics();
            showError('¡Reconectado exitosamente al servidor!', true);
        } else {
            throw new Error('Backend no disponible');
        }
    } catch (error) {
        showConnectionStatus('⚠ Sin conexión - Modo offline', 'offline');
        showError('No se pudo reconectar al servidor');
    }
}

// Agregar botón de reconexión
function addReconnectButton() {
    const connectionStatus = document.getElementById('connectionStatus');
    if (connectionStatus && !isBackendConnected) {
        const reconnectBtn = document.createElement('button');
        reconnectBtn.innerHTML = '<i class="fas fa-sync-alt"></i> Intentar Reconectar';
        reconnectBtn.className = 'btn btn-sm btn-secondary';
        reconnectBtn.style.marginLeft = '10px';
        reconnectBtn.onclick = tryReconnect;
        
        if (!connectionStatus.querySelector('button')) {
            connectionStatus.appendChild(reconnectBtn);
        }
    }
}

// Verificar conexión periódicamente
setInterval(async () => {
    if (!isBackendConnected) {
        console.log('Verificando reconexión automática...');
        await checkBackendHealth();
        
        if (isBackendConnected) {
            showConnectionStatus('✓ Reconectado automáticamente', 'connected');
            await loadServiciosFromBackend();
            await loadStatistics();
            showError('¡Conexión restaurada automáticamente!', true);
        } else {
            addReconnectButton();
        }
    }
}, 30000); // Verificar cada 30 segundos

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

// Función para testing - forzar modo offline
function toggleOfflineMode() {
    isBackendConnected = !isBackendConnected;
    
    if (isBackendConnected) {
        showConnectionStatus('✓ Modo online habilitado', 'connected');
        loadServiciosFromBackend();
    } else {
        showConnectionStatus('⚠ Modo offline forzado', 'offline');
        loadDemoData();
    }
}

// Exportar funciones para testing (solo en desarrollo)
if (typeof window !== 'undefined') {
    window.toggleOfflineMode = toggleOfflineMode;
    window.tryReconnect = tryReconnect;
    window.checkBackendHealth = checkBackendHealth;
}