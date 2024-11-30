const { ActivityHandler, CardFactory } = require('botbuilder');
const axios = require('axios');

class MyBot extends ActivityHandler {
    constructor() {
        super();
        this.users = {};

        this.onMessage(async (context, next) => {
            const userId = context.activity.from.id;
            const userState = this.users[userId] || { loggedIn: false };

            const userMessage = context.activity.text?.toLowerCase();
            console.log("Mensaje del usuario:", userMessage);
            console.log("Estado del usuario:", userState);

            if (context.activity.value) {

               await this.handleFormSubmission(context, userState);
                return;
            }

            // Opciones de conversación
            switch (userMessage) {
                case 'a':
                    await context.sendActivity('Por favor, proporciona tu correo electrónico para verificar tu cuenta 😊.');
                    userState.awaitingLogin = true;
                    break;

                case 'b':
                    if (userState.loggedIn) {
                        await context.sendActivity(`Ya estás registrado e iniciado sesión. ¿Qué te gustaría hacer ahora? 😊
                            \n c. Ver habitaciones disponibles
                            \n d. Crear una reserva
                        \nPor favor, elige una opción válida del menú.`);
                    } else{
                        await context.sendActivity('Necesitamos tu consentimiento para tratar tus datos personales 😇.');
                        const consentCard = this.createConsentCard();
                        await context.sendActivity({ attachments: [consentCard] });
                        userState.awaitingRegistration = true;
                    }
                    break;

                case 'c':
                    if (userState.loggedIn) {
                        const habitaciones = await this.obtenerHabitacionesDisponibles();
                        if (habitaciones.length > 0) {
                            const cards = habitaciones.map(h => this.createRoomCard(h));
                            await context.sendActivity({
                                attachments: cards.map(card => ({
                                    contentType: "application/vnd.microsoft.card.adaptive",
                                    content: card
                                }))
                            });
                            await context.sendActivity(`Por favor, selecciona el ID de la habitación para continuar con la reserva. \nEj: 1, 2 ,3..`);
                            userState.awaitingRoomSelection = true;
                        } else {
                            await context.sendActivity("No hay habitaciones disponibles en este momento.");
                        }
                    } else {
                          const welcomeMessage = `Debes iniciar sesión o registrarte primero para acceder a esta opción.😊.
                             \n a. Iniciar sesión
                             \n b. Registrarme
                          \n Por favor, selecciona una opción válida del menú.`;
                          await context.sendActivity(welcomeMessage);
                    }
                    break;

                case 'd':
                    if (userState.loggedIn) {
                        const reservaId = '1';
                        const reserva = await this.obtenerReservaPorId(reservaId);
                        if (reserva) {
                            const reservaCard = this.createReservationCard(reserva);
                            await context.sendActivity({
                                attachments: [reservaCard]
                            });
                        } else {
                            await context.sendActivity('Lo siento, no pude encontrar los detalles de la reserva 😔.');
                        }
                    } else {
                            const welcomeMessage = `Debes iniciar sesión o registrarte primero para acceder a esta opción.😊.
                                \n a. Iniciar sesión
                                \n b. Registrarme
                            \n Por favor, selecciona una opción válida del menú.`;
                            await context.sendActivity(welcomeMessage);
                    }
                    break;

                default:
                    if (userState.awaitingLogin) {
                        const cliente = await this.verificarCliente(userMessage);
                        if (cliente) {
                            userState.loggedIn = true;
                            userState.awaitingLogin = false;
                            await context.sendActivity(`¡Bienvenido de nuevo, ${cliente.nombre} ${cliente.apellido}! ¿Qué te gustaría hacer ahora? 😊
                                \n c. Ver habitaciones disponibles
                                \n d. Crear una reserva
                            \nPor favor, elige una opción válida del menú.`);
                        } else{
                             await context.sendActivity(`No encontramos tu cuenta. ¿Te gustaría registrarte? \n Escribe "2" para iniciar el registro.`);
                        }
                    }

                    else if (userState.awaitingRoomSelection) {
                        const selectedRoom = userMessage; // ID de la habitación seleccionada
                        const habitacion = await this.obtenerHabitacionPorId(selectedRoom);

                        if (habitacion) {
                            userState.selectedRoom = habitacion.id;
                            userState.awaitingRoomSelection = false;
                            await context.sendActivity(`Has seleccionado la habitación: ${selectedRoom}. ¿Te gustaría realizar una reserva? (sí/no)`);
                            userState.awaitingReservationConfirmation = true;
                        } else {
                            await context.sendActivity("No se encontró una habitación con ese ID. Por favor, inténtalo de nuevo.");
                        }
                    } else if (userState.awaitingReservationConfirmation) {
                        if (userMessage === "sí") {
                            await context.sendActivity("Perfecto. Completa el siguiente formulario para crear tu reserva:");
                            const reservationForm = this.createReservationForm(userState.selectedRoom);
                            await context.sendActivity({ attachments: [reservationForm] });
                            userState.awaitingReservationConfirmation = false;
                        } else {
                            await context.sendActivity("Entendido. Si necesitas algo más, no dudes en pedírmelo. 😊");
                            userState.awaitingReservationConfirmation = false;
                        }
                    }
                    else {
                         const welcomeMessage = `Por favor, selecciona una opción válida del menú 😊.
                             \n a. Iniciar sesión
                             \n b. Registrarme`;
                         await context.sendActivity(welcomeMessage);
                    }
                    break;
            }
            this.users[userId] = userState;
            await next();

        });

        this.onMembersAdded(async (context, next) => {
            for (const member of context.activity.membersAdded) {
                if (member.id !== context.activity.recipient.id) {
                    const welcomeMessage = `¡Hola! Soy tu asistente virtual para reservas 😊. ¿Qué te gustaría hacer?
                        \n a. Iniciar sesión
                        \n b. Registrarme
                    \nPara empezar, elige una opción válida del menú.`;
                    await context.sendActivity(welcomeMessage);
                }
            }
            await next();
        });

    }

    async handleFormSubmission(context, userState) {
        // Manejar formularios según estado (registro o reserva).
        if (context.activity.value.consent) {
            if (context.activity.value.consent === "yes") {
                await context.sendActivity('Gracias por aceptar el tratamiento de datos. Completa tu registro.');
                const registrationCard = this.createRegistrationForm();
                await context.sendActivity({ attachments: [registrationCard] });
            } else {
                await context.sendActivity('No podremos proceder sin tu consentimiento. Gracias.');
            }
        } else if (context.activity.value.nombre) {
            const response = await this.registrarCliente(context.activity.value);
            if (response.includes('¡Registro exitoso! 🎉 \n Ahora puedes iniciar sesión y acceder al sistema de reservas 🛏.\nEscribe "1" para iniciar sesión 😇.')) {
                userState.loggedIn = true;
            }
            await context.sendActivity(response);
        }
        else if (context.activity.value.nombreCliente) {
            const reservaInfo = {
               habitacionId: userState.selectedRoom,
               clienteId: context.activity.value.clienteId,
               empleadoId: context.activity.value.empleadoId,
               fechaIngreso: context.activity.value.fechaIngreso,
               fechaSalida: context.activity.value.fechaSalida,
               metodoPago: context.activity.value.metodoPago,
               total: context.activity.value.total,
            };

            const response = await this.confirmarReserva(reservaInfo);
            await context.sendActivity(response);
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
                return 'El cliente ya está registrado. \n Por favor, escribe "1" para iniciar sesión 😇.';
            }
            info.consentimiento = true;
            await axios.post('http://localhost:8080/api/v1/clientes/crear', info);
            return '¡Registro exitoso! 🎉 \nAhora puedes iniciar sesión y acceder al sistema de reservas 🛏.\nEscribe "1" para iniciar sesión 😇.';
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
            const response = await axios.post('http://localhost:8080/api/v1/reservas/crear', reservaInfo);
            return '¡Reserva creada exitosamente! 🎉 Gracias por usar nuestro servicio.';
        } catch (error) {
            console.error('Error al confirmar reserva ⚠️:', error.response?.data || error.message);
            return 'Error al crear la reserva: verifica que todos los campos sean válidos.';
        }
    }

    async obtenerReservaPorId(reservaId) {
        try {
            const response = await axios.get(`http://localhost:8080/api/v1/reservas/obtener/${reservaId}`);
            return response.data;
        } catch (error) {
            console.error('Error al obtener la reserva ⚠️:', error.message);
            return null;
        }
    }

// card consentimiento datos
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

//card del registro Cliente
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


// Card de las habitaciones
    createRoomCard(habitacion) {
        return {
            $schema: "http://adaptivecards.io/schemas/adaptive-card.json",
            type: "AdaptiveCard",
            version: "1.3",
            body: [

                {
                    type: "TextBlock",
                    text: `ID: ${habitacion.id}`,
                    wrap: true
                },
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
                    text: `Capacidad: ${habitacion.capacidad} persona(s)`,
                    wrap: true
                },
                {
                    type: "TextBlock",
                    text: `Precio: $${habitacion.precioPorNoche} por noche`,
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
    }


  // Card de las habitaciones
  createReservationForm(habitacion) {
      return CardFactory.adaptiveCard({
          $schema: "http://adaptivecards.io/schemas/adaptive-card.json",
          type: "AdaptiveCard",
          version: "1.3",
          body: [
              { type: "TextBlock", text: "Formulario de Reserva", weight: "Bolder", size: "Medium" },
              { type: "TextBlock", text: `Habitación Seleccionada:  ${habitacion}`},
              { type: "Input.Text", id: "nombreCliente", placeholder: "Ingresa tu nombre completo", label: "Nombre del Cliente" },
              { type: "Input.Text", id: "nombreEmpleado", placeholder: "Ingresa nombre empleado", label: "Nombre del Empleado" },
              { type: "Input.Date", id: "fechaIngreso", placeholder: "Selecciona la fecha de ingreso", label: "Fecha de Ingreso" },
              { type: "Input.Date", id: "fechaSalida", placeholder: "Selecciona la fecha de salida", label: "Fecha de Salida" },
              { type: "Input.ChoiceSet", id: "metodoPago", label: "Método de Pago", choices: [
                  { title: "Tarjeta de Crédito", value: "tarjeta" },
                  { title: "Tarjeta de Débito", value: "tarjeta" },
                  { title: "Efectivo", value: "efectivo" },
                  { title: "Transferencia Bancaria", value: "transferencia" }
              ] }
          ],
          actions: [{ type: "Action.Submit", title: "Reservar" }]
      });
          
  }

}

module.exports.MyBot = MyBot;
