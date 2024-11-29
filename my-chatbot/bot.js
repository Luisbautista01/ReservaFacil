const { ActivityHandler, CardFactory } = require('botbuilder');
const axios = require('axios');

class MyBot extends ActivityHandler {
    constructor() {
        super();

        this.onMessage(async (context, next) => {
            const userMessage = context.activity.text?.toLowerCase();

            if (context.activity.value) {
                // Manejo de respuestas de formulario
                if (context.activity.value.consent) {
                    const consentResponse = context.activity.value.consent;
                    if (consentResponse === "yes") {
                        await context.sendActivity('¡Gracias por aceptar el tratamiento de datos! Ahora puedes completar tu registro 😊.');
                        const registrationCard = this.createRegistrationForm();
                        await context.sendActivity({ attachments: [registrationCard] });
                    } else {
                        await context.sendActivity('Entendido. No podremos proceder sin tu consentimiento. ¡Gracias por tu tiempo! 😇');
                    }
                } else if (
                    context.activity.value.nombre &&
                    context.activity.value.apellido &&
                    context.activity.value.correoElectronico &&
                    context.activity.value.telefono
                ) {
                    const info = context.activity.value;
                    const response = await this.registrarCliente(info);
                    await context.sendActivity(response);
                } else if (
                    context.activity.value.fechaIngreso &&
                    context.activity.value.fechaSalida &&
                    context.activity.value.metodoPago
                ) {
                    const reservaInfo = { ...context.activity.value };
                    const confirmacion = await this.confirmarReserva(reservaInfo);
                    await context.sendActivity(confirmacion);
                } else {
                    await context.sendActivity('Por favor, completa todos los campos requeridos antes de continuar ⚠️.');
                }
                return;
            }

            // Opciones de conversación
            switch (userMessage) {
                case '1':
                    await context.sendActivity('Por favor, proporciona tu correo electrónico para verificar tu cuenta 😊.');
                    break;

                case '2':
                    await context.sendActivity('Antes de registrarte, necesitamos tu consentimiento para tratar tus datos personales 😇.');
                    const consentCard = this.createConsentCard();
                    await context.sendActivity({ attachments: [consentCard] });
                    break;

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
        if (userMessage?.includes('@')) {
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
        } else if (userMessage === '3') {
            const habitaciones = await this.obtenerHabitacionesDisponibles();
            if (habitaciones.length > 0) {
                const cards = habitaciones.map(h => this.createRoomCard(h));
                const attachments = cards.map(card => ({ contentType: "application/vnd.microsoft.card.adaptive", content: card }));
                await context.sendActivity({ attachments });
                await context.sendActivity('Por favor, selecciona el ID de la habitación que deseas reservar.');
            } else {
                await context.sendActivity('Lo siento, no hay habitaciones disponibles en este momento 😔.');
            }
        } else if (/^\d+$/.test(userMessage)) {
            const habitacionId = parseInt(userMessage, 10);
            const habitacion = await this.obtenerHabitacionPorId(habitacionId);
            if (habitacion) {
                const reservaForm = this.createReservationForm(habitacionId);
                await context.sendActivity({ attachments: [reservaForm] });
            } else {
                await context.sendActivity("El ID de habitación ingresado no es válido.");
            }
        }
    }

    async verificarCliente(email) {
        try {
            const { data } = await axios.get(`http://localhost:8080/api/v1/clientes/obtener-por-correo?correo=${email}`);
            return data;
        } catch (error) {
            console.error('Error al verificar cliente ⚠️:', error.message);
            return null;
        }
    }

    async registrarCliente(info) {
        try {
            const clienteExistente = await this.verificarCliente(info.correoElectronico);
            if (clienteExistente) {
                return 'El cliente ya está registrado. Por favor, inicia sesión con tu correo 😇.';
            }
            info.consentimiento = true;
            await axios.post('http://localhost:8080/api/v1/clientes/crear', info);
            return '¡Registro exitoso! 🎉 \n Ahora puedes iniciar sesión y acceder al sistema de reservas 🛏.';
        } catch (error) {
            console.error('Error al registrar cliente:', error.message);
            return 'Hubo un error al procesar tu registro. Por favor intenta más tarde ⚠️.';
        }
    }

    async obtenerHabitacionesDisponibles() {
        try {
            const { data } = await axios.get('http://localhost:8080/api/v1/habitaciones/disponibilidad');
            return data;
        } catch (error) {
            console.error('Error al obtener habitaciones disponibles:', error.message);
            return [];
        }
    }

    async obtenerHabitacionPorId(habitacionId) {
        try {
            const { data } = await axios.get(`http://localhost:8080/api/v1/habitaciones/obtener/${habitacionId}`);
            return data;
        } catch (error) {
            console.error('Error al obtener habitación por ID:', error.message);
            return null;
        }
    }

    async confirmarReserva(reservaInfo) {
        try {
            await axios.post('http://localhost:8080/api/v1/reservas/crear', reservaInfo);
            return '¡Reserva confirmada con éxito! 🎉 Gracias por usar nuestro servicio.';
        } catch (error) {
            console.error('Error al confirmar la reserva:', error.message);
            return 'Hubo un problema al confirmar tu reserva. Por favor intenta nuevamente.';
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

    createReservationForm(habitacionId) {
        return CardFactory.adaptiveCard({
            $schema: "http://adaptivecards.io/schemas/adaptive-card.json",
            type: "AdaptiveCard",
            version: "1.3",
            body: [
                { type: "TextBlock", text: "Crear Reserva", weight: "Bolder", size: "Medium" },
                { type: "Input.Date", id: "fechaIngreso", placeholder: "Fecha de Ingreso" },
                { type: "Input.Date", id: "fechaSalida", placeholder: "Fecha de Salida" },
                {
                    type: "Input.ChoiceSet",
                    id: "metodoPago",
                    placeholder: "Método de Pago",
                    choices: [
                        { title: "Tarjeta de Crédito", value: "TARJETA_CREDITO" },
                        { title: "Tarjeta de Débito", value: "TARJETA_DEBITO" },
                        { title: "Transferencia Bancaria", value: "TRANSFERENCIA" }
                    ]
                }
            ],
            actions: [{ type: "Action.Submit", title: "Confirmar" }],
            additionalData: { habitacionId }
        });
    }


   createRoomCard(habitacion) {
       return {
           $schema: "http://adaptivecards.io/schemas/adaptive-card.json",
           type: "AdaptiveCard",
           version: "1.3",
           body: [
               {
                   type: "TextBlock",
                   text: `Habitación: ${habitacion.tipo}`,
                   weight: "Bolder",
                   size: "Medium"
               },
               {
                   type: "Image",
                   url: habitacion.imagenUrl,
                   size: "Stretch"
               },
               {
                   type: "TextBlock",
                   text: `Capacidad: ${habitacion.capacidad}`,
                   wrap: true
               },
               {
                   type: "TextBlock",
                   text: `Precio por noche: $${habitacion.precioPorNoche}`,
                   wrap: true
               },
               {
                   type: "TextBlock",
                   text: habitacion.disponible ? "Disponible: Sí" : "Disponible: No",
                   wrap: true
               }
           ],
           actions: [
               {
                   type: "Action.Submit",
                   title: "Reservar",
                   data: { habitacionId: habitacion.id }
               }
           ]
       };
       console.log(habitacion);
   }


}

module.exports.MyBot = MyBot;
