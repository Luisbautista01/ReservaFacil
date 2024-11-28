const { ActivityHandler, CardFactory } = require('botbuilder');
const axios = require('axios');

class MyBot extends ActivityHandler {
    constructor() {
        super();

        this.onMessage(async (context, next) => {
            const userMessage = context.activity.text?.toLowerCase();

            if (context.activity.value) {
                // Manejar la respuesta de consentimiento
                if (context.activity.value.consent) {
                    const consentResponse = context.activity.value.consent;
                    if (consentResponse === "yes") {
                        await context.sendActivity('¡Gracias por aceptar el tratamiento de datos! Ahora puedes completar tu registro.');
                        const registrationCard = this.createRegistrationForm();
                        await context.sendActivity({ attachments: [registrationCard] });
                    } else {
                        await context.sendActivity('Entendido. No podremos proceder sin tu consentimiento. ¡Gracias por tu tiempo!');
                    }
                }
                // Verifica que se haya recibido un ID válido para la habitación seleccionada
                if (!this.selectedRoomId) {
                    await context.sendActivity('Por favor selecciona una habitación primero.');
                    return;
                }

                // Verifica que la habitación esté disponible antes de proceder
                const habitacionSeleccionada = await this.obtenerHabitacionPorId(this.selectedRoomId);
                if (!habitacionSeleccionada || !habitacionSeleccionada.disponible) {
                    await context.sendActivity('La habitación seleccionada no está disponible.');
                    return;
                }

                // Manejar datos de registro
                else if (
                    context.activity.value.nombre &&
                    context.activity.value.apellido &&
                    context.activity.value.correoElectronico &&
                    context.activity.value.telefono
                ) {
                    const info = context.activity.value;
                    const response = await this.registrarCliente(info);
                    await context.sendActivity(response);
                    await context.sendActivity('Ahora puedes hacer una reserva o ver las habitaciones disponibles: \n3. Ver habitaciones disponibles \n4. Crear una reserva.');
                } else {
                    await context.sendActivity('Por favor, completa todos los campos requeridos antes de continuar.');
                }
                return;
            }

            // Opciones de conversación
            switch (userMessage) {
                case '1':
                    await context.sendActivity('Por favor, proporciona tu correo electrónico para verificar tu cuenta 😊.');
                    break;

                case '2':
                    await context.sendActivity('Antes de registrarte, necesitamos tu consentimiento para tratar tus datos personales.');
                    const consentCard = this.createConsentCard();
                    await context.sendActivity({ attachments: [consentCard] });
                     break;

                case '3': {
                    const habitaciones = await this.obtenerHabitacionesDisponibles();
                    if (habitaciones.length > 0) {
                        await context.sendActivity('Estas son las habitaciones disponibles:');
                        const heroCards = habitaciones.map(h =>
                            CardFactory.heroCard(
                                `Habitación: ${h.title}`,
                                `Capacidad: ${h.subtitle}, Precio: $${h.precioPorNoche}/noche`,
                                h.images,
                                [
                                    {
                                        type: "imBack",
                                        title: `Seleccionar ${h.title}`,
                                        value: `reservar ${h.id}`
                                    }
                                ]
                            )
                        );
                        await context.sendActivity({ attachments: heroCards });
                    } else {
                        await context.sendActivity('No hay habitaciones disponibles en este momento.');
                    }
                    break;
                }

                default:
                    await this.handleUserMessage(context, userMessage);
                    break;
            }
            await next();
        });

        this.onMembersAdded(async (context, next) => {
            for (const member of context.activity.membersAdded) {
                if (member.id !== context.activity.recipient.id) {
                    const welcomeMessage = `¡Hola! Soy tu asistente virtual para reservas 😊.
                        ¿Qué te gustaría hacer?
                        \n1. Iniciar sesión
                        \n2. Registrarme
                        \nPara empezar, elige una opción.`;
                    await context.sendActivity(welcomeMessage);
                }
            }
            await next();
        });
    }

    async handleUserMessage(context, userMessage) {
        if (userMessage?.startsWith('reservar')) {
           const habitacionId = parseInt(userMessage.split(' ')[1]);
           const habitacion = await this.obtenerHabitacionPorId(habitacionId);
            if (habitacion && habitacion.disponible) {
                const empleadoId = await this.obtenerEmpleadoAsignado();
                const formularioReserva = this.crearFormularioReserva(habitacionId, empleadoId);
                await context.sendActivity({ attachments: [formularioReserva] });
            } else {
                await context.sendActivity('La habitación seleccionada no está disponible.');
            }
        } else  {
            await context.sendActivity('Escribe "reservar [ID]" para seleccionar una habitación.');
        }

        if (userMessage === 'sí' && this.obtenerHabitacionPorId(habitacionId)) {
             // Verifica que selectedRoomId está disponible antes de proceder
             if (!this.obtenerHabitacionPorId(habitacionId)) {
                await context.sendActivity('Por favor, selecciona una habitación antes de proceder.');
                 return;
             }
             const habitacionSeleccionada = await this.obtenerHabitacionPorId(habitacionId);
             if (!habitacionSeleccionada) {
                await context.sendActivity('No se pudo encontrar la habitación seleccionada.');
                 return;
             }
        } else if (userMessage === 'no') {
              await context.sendActivity('Reserva cancelada. ¿Hay algo más en lo que pueda ayudarte? 😊');

        } else if (userMessage?.includes('@')) {
              const cliente = await this.verificarCliente(userMessage);
              if (cliente) {
                  await context.sendActivity(`¡Hola ${cliente.nombre} ${cliente.apellido}! ¿Qué te gustaría hacer hoy? 😊
                  \n3. Ver habitaciones disponibles
                  \n4. Crear una reserva`);
              } else {
                  await context.sendActivity('No encontramos una cuenta con ese correo. ¿Te gustaría registrarte? Responde con "Sí" o "No" 😊.');
              }
        } else if (userMessage === 'sí') {
            const registrationCard = this.createRegistrationForm();
            await context.sendActivity({ attachments: [registrationCard] });
        } else {
            await context.sendActivity('No entendí tu solicitud. Por favor selecciona una opción válida o escribe un mensaje más claro 😇.');
        }
    }

    createConsentCard() {
        return CardFactory.adaptiveCard({
            $schema: "http://adaptivecards.io/schemas/adaptive-card.json",
            type: "AdaptiveCard",
            version: "1.3",
            body: [
                { type: "TextBlock", text: "Consentimiento de Tratamiento de Datos Personales", weight: "Bolder", size: "Medium" },
                { type: "TextBlock", text: "Tu privacidad es importante para nosotros. Queremos informarte que utilizaremos tus datos personales con los siguientes propósitos:", wrap: true },
                { type: "TextBlock", text: "- Gestionar tu registro y cuenta de usuario.\n- Proporcionar servicios relacionados con reservas de habitaciones.\n- Comunicarnos contigo en caso de actualizaciones o promociones relacionadas con nuestros servicios.", wrap: true },
                { type: "TextBlock", text: "Para más detalles, por favor revisa nuestra [Política de Privacidad](https://www.ejemplo.com/politica-de-privacidad).", wrap: true },
                { type: "TextBlock", text: "¿Estás de acuerdo con el tratamiento de tus datos personales para los fines mencionados?", wrap: true }
            ],
            actions: [
                { type: "Action.Submit", title: "Sí, acepto", data: { consent: "yes" } },
                { type: "Action.Submit", title: "No, no acepto", data: { consent: "no" } }
            ]
        });
    }

    createRegistrationForm() {
        return CardFactory.adaptiveCard({
            $schema: "http://adaptivecards.io/schemas/adaptive-card.json",
            type: "AdaptiveCard",
            version: "1.3",
            body: [
                { type: "TextBlock", text: "Registro de Cliente", weight: "Bolder", size: "Medium" },
                { type: "Input.Text", id: "nombre", placeholder: "Ingresa tu nombre", label: "Nombre" },
                { type: "Input.Text", id: "apellido", placeholder: "Ingresa tu apellido", label: "Apellido" },
                { type: "Input.Text", id: "correoElectronico", placeholder: "Ingresa tu correo electrónico", label: "Correo Electrónico", style: "email" },
                { type: "Input.Text", id: "telefono", placeholder: "Ingresa tu número de teléfono", label: "Teléfono", style: "tel" }
            ],
            actions: [{ type: "Action.Submit", title: "Registrar" }]
        });
    }

    async verificarCliente(email) {
        try {
            const { data } = await axios.get(`http://localhost:8080/api/v1/clientes/obtener-por-correo?correo=${email}`);
            return data;
        } catch (error) {
            console.error('Error al verificar cliente:', error.message);
            return null;
        }
    }

    async registrarCliente(info) {
        try {
            const clienteExistente = await this.verificarCliente(info.correoElectronico);
            if (clienteExistente) {
                return 'El cliente ya está registrado. Por favor, inicia sesión con tu correo.';
            }

            info.consentimiento = true;
            await axios.post('http://localhost:8080/api/v1/clientes/crear', info);
            return '¡Registro exitoso! Ahora puedes acceder al sistema de reservas 🎉.';
        } catch (error) {
            console.error('Error al registrar cliente:', error.message);
            return 'Hubo un error al procesar tu registro. Por favor intenta más tarde.';
        }
    }

    async obtenerHabitacionesDisponibles() {
        try {
            const { data } = await axios.get('http://localhost:8080/api/v1/habitaciones/disponibilidad');
            return data.map(h => ({
                title: `Habitación: ${h.tipo}`,
                subtitle: `Capacidad: ${h.capacidad}, Precio: $${h.precioPorNoche}/noche`,
                images: [{ url: h.imagenUrl }],
                id: h.id
            }));
        } catch (error) {
            console.error('Error al obtener habitaciones:', error.message);
            return [];
        }
    }

    async obtenerHabitacionPorId(habitacionId) {
        try {
           const response = await axios.get(`http://localhost:8080/api/v1/reservas/habitaciones/${habitacionId}`);
           return response.data;
        } catch (error) {
           console.error(error);
           return null;
        }
    }

    async obtenerEmpleadoAsignado() {
        try {
            // Suponiendo que tienes una API que te proporciona el empleado asignado
            const response = await axios.get('http://localhost:8080/api/v1/empleados/disponibilidad');
            return response.data; // Asegúrate de que esto devuelve el empleado asignado
        } catch (error) {
            console.error('Error al obtener empleado asignado:', error);
            return null; // O maneja el error de otra manera
        }
    }

    crearFormularioReserva(habitacionId, empleadoId) {
        return CardFactory.adaptiveCard({
            $schema: "http://adaptivecards.io/schemas/adaptive-card.json",
            type: "AdaptiveCard",
            version: "1.3",
            body: [
                { type: "TextBlock", text: "Detalles de Reserva", weight: "Bolder", size: "Medium" },
                { type: "Input.Text", id: "clienteId", placeholder: "ID del cliente" },
                { type: "Input.Text", id: "habitacionId", value: `${habitacionId}`, isVisible: false },
                { type: "Input.Text", id: "empleadoId", value: `${empleadoId}`, isVisible: false },
                {
                    type: "Input.Date",
                    id: "fechaIngreso",
                    label: "Fecha de Ingreso",
                    min: new Date().toISOString().split('T')[0]
                },
                {
                    type: "Input.Date",
                    id: "fechaSalida",
                    label: "Fecha de Salida",
                    min: new Date().toISOString().split('T')[0]
                },
                {
                    type: "Input.ChoiceSet",
                    id: "metodoPago",
                    label: "Método de Pago",
                    choices: [
                        { title: "Nequi", value: "Nequi" },
                        { title: "Bancolombia", value: "Bancolombia" },
                        { title: "Daviplata", value: "Daviplata" }
                    ]
                },
                { type: "Input.Number", id: "total", placeholder: "Total a pagar" }
            ],
            actions: [
                { type: "Action.Submit", title: "Confirmar Reserva" }
            ]
        });
    }

}

module.exports.MyBot = MyBot;
