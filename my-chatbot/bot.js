const { ActivityHandler, CardFactory } = require('botbuilder');
const axios = require('axios');

class MyBot extends ActivityHandler {
    constructor() {
        super();
        this.users = {};

        this.onMessage(async (context, next) => {
            const userId = context.activity.from.id;
            const userState = this.users[userId] || { loggedIn: false };

            const userMessage = context.activity.text?.trim().toLowerCase();
            console.log("Mensaje del usuario:", userMessage);
            console.log("Estado actual del usuario:", userState);

            if (context.activity.value) {
                console.log("Formulario recibido:", context.activity.value);
                await this.handleFormSubmission(context, userState);
                return;
            } else {
                console.log("No se recibió ningún valor del formulario.");
            }

            // Opciones de conversación
            switch (userMessage) {
                case 'a':
                    await context.sendActivity('¡Hola! 👋 Para comenzar, por favor proporciona tu **correo electrónico** 📧 y verificaremos tu cuenta. 😊');
                    userState.awaitingLogin = true;
                    break;
                case 'b':
                    if (userState.loggedIn) {
                         await context.sendActivity(`✔️ ¡Ya estás registrado e iniciado sesión! 😄 ¿Qué te gustaría hacer ahora?
                             \n🏨 **c. Crear una nueva reserva** 🛏️
                             \n📝 **d. Ver habitaciones disponibles** 🔍
                             \n📅 **e. Ver mis reservas** 📋
                             \n🚪 **Salir. Cerrar sesión** 🔒
                         \n✨ Por favor, selecciona una opción o escribe **salir** para cerrar sesión. ¡Estoy aquí para ayudarte! 💬`);
                    } else{
                        await context.sendActivity('Para continuar, necesitamos tu consentimiento para tratar tus datos personales. 😇');
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
                            await context.sendActivity(`Por favor, selecciona el **ID de la habitación** para continuar con tu reserva.
                                \nEscribe el número de la habitación que te interesa, por ejemplo: **1, 2, 3**... 😊`);
                            userState.awaitingRoomSelection = true;
                        } else {
                            await context.sendActivity("Lo siento, no hay habitaciones 🏨 disponibles en este momento.");
                        }
                    } else {
                       const welcomeMessage = `Para poder realizar una reserva, necesitas iniciar sesión o registrarte primero. 😊
                          \n\n🔑 **a. Iniciar sesión** 🏷️
                          \n📝 **b. Registrarme** ✍️
                          \n\n¡Elige una opción para comenzar! 💬`;
                       await context.sendActivity(welcomeMessage);
                    }
                    break;
                case 'd':
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
                           await context.sendActivity(`Por favor, selecciona una habitación escribiendo el **ID de la habitación**. Ejemplo: **1, 2, 3**. 😊`);
                           userState.awaitingRoomSelection = true;
                       } else {
                           await context.sendActivity("Lo siento, no hay habitaciones 🏨 disponibles en este momento.");
                       }
                    } else {
                       const welcomeMessage = `Para acceder a esta opción, primero debes iniciar sesión o registrarte. 😊
                          \n\n🔑 **a. Iniciar sesión** 🏷️
                          \n📝 **b. Registrarme** ✍️
                          \n\nElige una opción para empezar. 💬`;
                       await context.sendActivity(welcomeMessage);
                    }
                    break;
                case 'e':
                    if (userState.loggedIn) {
                       const reservas = await this.obtenerReservasPorClienteId(userState.clienteId);
                       if (reservas.length > 0) {
                           const reservasMessage = reservas.map((reserva, index) => {
                               return (`------------------------------------------
                                   \n🧾 **Reserva #${index + 1}**
                                   \n🏨 **Habitación:** ${reserva.habitacionId}
                                   \n📅 **Ingreso:** ${reserva.fechaIngreso}
                                   \n📅 **Salida:** ${reserva.fechaSalida}
                                   \n👨‍💼 **Empleado:** ${reserva.empleadoId ? reserva.empleadoId : "No asignado"}
                                   \n💳 **Método de Pago:** ${reserva.metodoPago}
                                   \n💲 **Total:** $${reserva.total.toFixed(2)}
                               \n------------------------------------------`);
                           }).join("\n\n");
                           await context.sendActivity(`✨ **Tus Reservas Actuales:**
                              \n${reservasMessage}
                           `);
                           await context.sendActivity(`🌟 **¿Qué te gustaría hacer ahora?** 😊
                              \n🏨 **c. Crear una reserva**
                              \n📝 **d. Ver habitaciones disponibles**
                              \n🚪 **Salir. Cerrar sesión** 🔒
                           \n✨ Elige una opción o escribe **salir** para cerrar sesión. ¡Aquí estoy para ayudarte! 💬`);
                       } else {
                           await context.sendActivity("🚫 No tienes reservas en este momento.");
                       }
                    } else {
                       await context.sendActivity("🔐 Debes iniciar sesión para ver tus reservas. 😊");
                    }
                    break;
                case 'salir':
                    if (userState.loggedIn) {
                        // Eliminar estado del usuario
                        delete this.users[userId];
                        userState.loggedIn = false;

                        // Enviar mensaje de despedida
                        await context.sendActivity('👋 **Has cerrado sesión con éxito.** ¡Vuelve cuando lo necesites! 😊');
                    } else {
                        await context.sendActivity('❌ **No tienes una sesión activa.** \n\nEscribe "a" para iniciar sesión.');
                    }
                    break;
                default:
                    if (userState.awaitingLogin) {
                       const cliente = await this.verificarCliente(userMessage);
                       if (cliente) {
                           userState.loggedIn = true;
                           userState.awaitingLogin = false;
                           userState.clienteId = cliente.id; // Guardar el clienteId en el estado

                           await context.sendActivity(`🎉 ¡Hola **${cliente.nombre}**! Ya estás registrado y has iniciado sesión correctamente. 😄
                              \n¿Qué te gustaría hacer ahora? 😊
                              \n🏨 **c. Crear una reserva**
                              \n📝 **d. Ver habitaciones disponibles**
                              \n📅 **e. Ver mis reservas**
                              \n🚪 **Salir. Cerrar sesión** 🔒
                           \n✨ Elige una opción del menú o escribe **salir** para cerrar sesión. ¡Estoy aquí para ayudarte! 💬`);
                       } else{
                           await context.sendActivity(`No encontramos tu cuenta. ¿Te gustaría registrarte? Escribe "b" para iniciar el registro.`);
                       }
                    } else if (userState.awaitingRoomSelection) {
                        const selectedRoom = userMessage; // ID de la habitación seleccionada
                        const habitacion = await this.obtenerHabitacionPorId(selectedRoom);

                        if (habitacion) {
                            userState.selectedRoom = habitacion.id;
                            userState.awaitingRoomSelection = false;

                            await context.sendActivity(`🏨 ¡Habitación ${selectedRoom} seleccionada! 🎉`);

                            const empleadosDisponibles = await this.obtenerEmpleadosDisponibles();
                            if (empleadosDisponibles.length > 0) {
                                const cards = empleadosDisponibles.map(e => this.createEmployeeCard(e));
                                await context.sendActivity({
                                    attachments: cards.map(card => ({
                                        contentType: "application/vnd.microsoft.card.adaptive",
                                        content: card,
                                    })),
                                });
                                await context.sendActivity(`Selecciona un **empleado** para continuar. 👩‍💼👨‍💼 Escribe el número correspondiente, por ejemplo: **1, 2, 3**...`);
                                userState.awaitingEmployeeSelection = true;
                            } else {
                                await context.sendActivity("Lo siento, no hay empleados disponibles en este momento. Por favor, intenta más tarde.");
                                userState.awaitingEmployeeSelection = false;
                            }
                        } else {
                            await context.sendActivity("No se encontró una habitación con ese ID. Por favor, intenta nuevamente.");
                        }
                    } else if (userState.awaitingEmployeeSelection) {
                       const selectedEmployee = parseInt(context.activity.value?.empleadoId || userMessage, 10);
                       const empleadosDisponibles = await this.obtenerEmpleadosDisponibles();

                       if (!isNaN(selectedEmployee) && empleadosDisponibles.some(e => e.id === selectedEmployee)) {
                           userState.selectedEmployee = selectedEmployee;
                           userState.awaitingEmployeeSelection = false;

                           await context.sendActivity(`👨‍💼 ¡Empleado ${selectedEmployee} seleccionado con éxito! ✅`);
                           await context.sendActivity(`✨ ¿Quieres proceder con la reserva? 😊
                               \n👍 **Escribe "sí" para continuar** o ❌ **"no" para cancelar.**`);

                           userState.awaitingReservationConfirmation = true;
                       } else {
                           await context.sendActivity("Por favor selecciona un empleado válido (un número).");
                       }
                    } else if (userState.awaitingReservationConfirmation) {
                        if (userMessage === "sí") {
                            userState.awaitingReservationConfirmation = false;

                            await context.sendActivity("Perfecto, completa el siguiente formulario para finalizar tu reserva:");

                            const reservationForm = this.createReservationForm(
                                userState.selectedRoom,
                                userState.selectedEmployee,
                                userState.clienteId
                            );

                            await context.sendActivity({ attachments: [reservationForm] });
                        } else if (userMessage === "no") {
                            userState.awaitingReservationConfirmation = false;
                            await context.sendActivity("La reserva ha sido cancelada 🚫. Si necesitas algo más, estoy a tu disposición.");
                        } else {
                            await context.sendActivity(
                                "Por favor responde con 'sí' para proceder o 'no' para cancelar."
                            );

                            await context.sendActivity("Lo siento, no entendí lo que dijiste. 😕 \nPor favor responde con 'sí' para proceder o 'no' para cancelar la reserva.");
                        }
                    }
                    else {
                        // Si está logueado, manejar comandos no reconocidos
                        await context.sendActivity(`❓Opción no reconocida. Por favor, elige una opción válida del menú.
                           \n🔐 **a. Iniciar sesión** 🏷️
                           \n✍️ **b. Registrarme** 📝
                        `);
                    }
            }
            this.users[userId] = userState;
            await next();
        });

        this.onMembersAdded(async (context, next) => {
            for (const member of context.activity.membersAdded) {
                if (member.id !== context.activity.recipient.id) {
                    const welcomeMessage = (`🌟 **¡Bienvenido/a!** 👋 Soy tu **asistente virtual** para reservas 😊.
                        \n💻 **¿Qué te gustaría hacer hoy?**
                            \n🔐 **a. Iniciar sesión** 🏷️
                            \n✍️ **b. Registrarme** 📝
                        \n🛠️ **Elige una opción para comenzar**.
                    \n¡Estoy aquí para ayudarte a gestionar todo fácilmente! 🚀`);
                    await context.sendActivity(welcomeMessage);
                }
            }
            await next();
        });
    }

    async handleFormSubmission(context, userState) {
        const action = context.activity.value?.action; // Verificar acción enviada
        const userId = context.activity.from.id;

        // Manejar formularios según estado (registro o reserva).
        if (context.activity.value.consent) {
            if (context.activity.value.consent === "yes") {
                await context.sendActivity('Gracias por aceptar el tratamiento de datos 😊. Ahora Completa tu registro.');
                const registrationCard = this.createRegistrationForm();
                await context.sendActivity({ attachments: [registrationCard] });
            } else {
                await context.sendActivity('No podremos proceder sin tu consentimiento. Gracias 😊.');
            }
        } else if (context.activity.value.nombre) {
            const response = await this.registrarCliente(context.activity.value);
            if (response.includes('¡Registro exitoso! 🎉 \n Ahora puedes iniciar sesión y acceder al sistema de reservas 🛏.\nEscribe "a" para iniciar sesión 😇.')) {
                userState.loggedIn = true;
            }
            await context.sendActivity(response);
        }

        // Verificar si se está esperando una acción post-reserva
        else if (userState.awaitingPostReservaAction) {
            switch(action) {
                case "c":
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
                            await context.sendActivity(`Por favor, selecciona el ID de la **habitación** para continuar con la reserva. \n\nPor ejemplo, escribe: **1, 2, 3**...`);
                            userState.awaitingRoomSelection = true;
                        } else {
                            await context.sendActivity("No hay habitaciones 🏨 disponibles en este momento.");
                        }
                    } else {
                        const welcomeMessage = `Debes iniciar sesión o registrarte primero para acceder a esta opción.😊.
                           \n\n🔑**a. Iniciar sesión** 🏷️
                           \n📝**b. Registrarme** ✍️
                        \n\nPara empezar, elige una opción válida del menú. 💬`;
                        await context.sendActivity(welcomeMessage);
                    }
                    break;
                case "e":
                    if (userState.loggedIn) {
                        const reservas = await this.obtenerReservasPorClienteId(userState.clienteId);
                        if (reservas.length > 0) {
                            const reservasMessage = reservas.map((reserva, index) => {
                               return (`------------------------------------------
                                 \n🧾 **Comprobante de Reserva #${index + 1}**
                                 \n🏨 **Habitación:** ${reserva.habitacionId}
                                 \n📅 **Ingreso:** ${reserva.fechaIngreso}
                                 \n📅 **Salida:** ${reserva.fechaSalida}
                                 \n👨‍💼 **Empleado:** ${reserva.empleadoId ? reserva.empleadoId : "No asignado"}
                                 \n💳 **Método de Pago:** ${reserva.metodoPago}
                                 \n💲 **Total:** $${reserva.total.toFixed(2)}
                               \n------------------------------------------`);
                            }).join("\n\n");

                            await context.sendActivity(`✨ **Tus Reservas Actuales:**
                              \n${reservasMessage} 😊
                            `);
                            await context.sendActivity(`🌟 **¿Qué te gustaría hacer ahora?** 😊
                               \n📝 **c. Crear una reserva**
                               \n🏨 **d. Ver habitaciones disponibles**
                               \n🚪 **salir. Cerrar sesión** 🔒
                            \n✨Elige una opción o escribe la frase **salir** para cerrar sesión. ¡Estoy aquí para ayudarte! 💬 `);
                        } else {
                            await context.sendActivity("🚫 No tienes reservas en este momento.");
                        }
                    } else {
                       await context.sendActivity("🔐 Debes iniciar sesión para ver tus reservas. 😊");
                    }
                    break;
            }
        }

        // Acción: Cancelar Reserva
        else if (action === "cancel") {
            await context.sendActivity("La reserva ha sido cancelada 😊.");
            await this.mostrarOpcionesPostReserva(context);
            return;
        }

        // Acción: Confirmar Reserva
        else if (action === "confirm") {
            try {
                const { clienteId, empleadoId, fechaIngreso, fechaSalida, metodoPago, total } = context.activity.value;
                // Validar que los datos sean completos
                if (!clienteId || !fechaIngreso || !fechaSalida || !metodoPago || !total) {
                    await context.sendActivity("Por favor, completa todos los campos del formulario para confirmar la reserva.");
                    return;
                }
                const reservaInfo = {
                    habitacionId: userState.selectedRoom,
                    clienteId,
                    empleadoId,
                    fechaIngreso,
                    fechaSalida,
                    metodoPago,
                    total,
                };
                const response = await this.confirmarReserva(reservaInfo);
                userState.lastReservationId = response.id;
                this.users[userId] = userState;

                await context.sendActivity(`¡Reserva: ${response.id} confirmada con éxito! 🎉 `);
                await this.mostrarOpcionesPostReserva(context);
            } catch (error) {
                console.error("Error en confirmación de reserva:", error);
                await context.sendActivity("Hubo un problema al confirmar la reserva. Por favor, inténtalo más tarde.");
            }
            return;
        }
    }

    async mostrarOpcionesPostReserva(context) {
        const userId = context.activity.from.id;
        const userState = this.users[userId] || {};
        userState.awaitingPostReservaAction = true;  // Asegúrate de que esto esté siendo establecido
        this.users[userId] = userState;
        await context.sendActivity(`🌟 **¿Qué te gustaría hacer ahora?** 😊
             \nc. **Crear otra reserva** 🛏️
             \ne. **Ver tu listado de reservas** 🧾
             \n🚪 **salir. Cerrar sesión** 🔒
        \n✨Elige una opción o escribe la frase **salir** para cerrar sesión. ¡Estoy aquí para ayudarte! 💬`);
    }

    async obtenerReservasPorClienteId(clienteId) {
        const response = await fetch(`http://localhost:8080/api/v1/usuarios-estado/cliente/${clienteId}`);
        if (response.ok) {
            return await response.json();
        } else {
            throw new Error("No se pudieron obtener las reservas");
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
                return `El cliente con email ${info.correoElectronico} ya está registrado. \nPor favor, escribe "a" para iniciar sesión 😇.`;
            }
            info.consentimiento = true;
            await axios.post('http://localhost:8080/api/v1/clientes/crear', info);
            return '¡Registro exitoso! 🎉 \nAhora puedes iniciar sesión y acceder al sistema de reservas 🛏.\nEscribe "a" para iniciar sesión 😇.';
        } catch (error) {
            console.error('Error al registrar cliente:', error.message);
            return 'Hubo un error al procesar tu registro. Por favor intenta más tarde ⚠️.';
        }
    }

    createRegistrationForm() {
        return CardFactory.adaptiveCard({
            $schema: "http://adaptivecards.io/schemas/adaptive-card.json",
            type: "AdaptiveCard",
            version: "1.3",
            body: [
                {
                    type: "TextBlock",
                    text: "👥 Registro de Cliente",
                    weight: "Bolder",
                    size: "Large",
                    wrap: true,
                    horizontalAlignment: "Center",
                    spacing: "Medium"
                },
                {
                    type: "TextBlock",
                    text: "Por favor, completa los siguientes datos para registrarte:",
                    wrap: true,
                    spacing: "Small"
                },
                {
                    type: "Input.Text",
                    id: "nombre",
                    label: "👤 Nombre",
                    placeholder: "Ingresa tu nombre",
                    maxLength: 50,
                    isRequired: true,
                    errorMessage: "El nombre es obligatorio."
                },
                {
                    type: "Input.Text",
                    id: "apellido",
                    label: "📝 Apellido",
                    placeholder: "Ingresa tu apellido",
                    maxLength: 50,
                    isRequired: true,
                    errorMessage: "El apellido es obligatorio."
                },
                {
                    type: "Input.Text",
                    id: "correoElectronico",
                    label: "📧 Correo Electrónico",
                    placeholder: "Ingresa tu correo electrónico",
                    style: "email",
                    maxLength: 100,
                    isRequired: true,
                    errorMessage: "El correo electrónico es obligatorio."
                },
                {
                    type: "Input.Text",
                    id: "telefono",
                    label: "📞 Teléfono",
                    placeholder: "Ingresa tu número de teléfono",
                    style: "tel",
                    maxLength: 15,
                    isRequired: true,
                    errorMessage: "El teléfono es obligatorio."
                }
            ],
            actions: [{ type: "Action.Submit", title: "✅ Registrar" }]
        });
    }

   // card consentimiento datos
    createConsentCard() {
        return CardFactory.adaptiveCard({
            $schema: "http://adaptivecards.io/schemas/adaptive-card.json",
            type: "AdaptiveCard",
            version: "1.3",
            body: [
                {
                    type: "TextBlock", text: "🛡️ Consentimiento de Tratamiento de Datos Personales",
                    weight: "Bolder", size: "Large", wrap: true, horizontalAlignment: "Center", spacing: "Medium"
                },
                {
                    type: "TextBlock", text: "🔒 Tu privacidad es importante para nosotros.",
                    weight: "Bolder",size: "Medium", wrap: true, spacing: "Small"
                },
                {
                    type: "TextBlock",
                    text: "Queremos informarte que utilizaremos tus datos personales con los siguientes propósitos:",
                    wrap: true, spacing: "Small"
                },
                {
                    type: "FactSet",
                    facts: [
                        { title: "📌", value: "Gestionar tu registro y cuenta de usuario." },
                        { title: "📋", value: "Proporcionar servicios relacionados con reservas de habitaciones." },
                        { title: "📧", value: "Comunicarnos contigo sobre actualizaciones o promociones." }
                    ]
                },
                {
                    type: "TextBlock",
                    text: "📄 Para más detalles, por favor revisa nuestra [Política de Privacidad](https://www.ejemplo.com/politica-de-privacidad).",
                    wrap: true, spacing: "Medium"
                },
                {
                    type: "TextBlock",
                    text: "¿Estás de acuerdo con el tratamiento de tus datos personales para los fines mencionados?",
                    wrap: true, weight: "Bolder", spacing: "Large"
                }
            ],
                actions: [
                   {
                       type: "Action.Submit", title: "✅ Sí, acepto", style: "positive",
                       data: { consent: "yes" }
                   },
                   {
                       type: "Action.Submit", title: "❌ No, no acepto", style: "destructive",
                       data: { consent: "no" }
                   }
               ]
        });
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

    async obtenerEmpleadosDisponibles() {
        try {
            const { data } = await axios.get('http://localhost:8080/api/v1/empleados/disponibilidad');
            return data.map(e => ({ ...e, id: Number(e.id) })); // Convertir ID a número si no lo es
        } catch (error) {
            console.error('Error al obtener empleados disponibles:', error.message);
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
            const response = await axios.post('http://localhost:8080/api/v1/usuarios-estado/crear', reservaInfo);
            console.log("Reserva confirmada:", response.data);
            return response.data; // Devuelve la información de la reserva
        } catch (error) {
            console.error("Error al confirmar la reserva:", error);
            throw new Error("Hubo un problema al confirmar la reserva. Inténtalo más tarde.");
        }
    }

    createRoomCard(habitacion) {
         return {
             $schema: "http://adaptivecards.io/schemas/adaptive-card.json",
             type: "AdaptiveCard",
             version: "1.3",
             body: [
                  {
                     type: "TextBlock", text: `ID: ${habitacion.id}`,
                     size: "Medium", color: "Dark", spacing: "Small",
                 },
                 {
                     type: "TextBlock", text: `${habitacion.tipo}`, weight: "Bolder", size: "Medium",
                     horizontalAlignment: "Center",   color: "Accent",  spacing: "Medium", wrap: true
                 },
                 {
                     type: "Image", url: habitacion.imagenUrl, size: "Stretch",
                     altText: "Imagen de la habitación", horizontalAlignment: "Center",
                     spacing: "Medium"
                 },
                 {
                     type: "ColumnSet",
                     columns: [
                         {
                             type: "Column", width: "auto",
                             items: [
                                 {
                                     type: "TextBlock", text: "🧑‍🤝‍🧑", size: "Small", altText: "Capacidad", spacing: "None"
                                 }
                             ]
                         },
                         {
                             type: "Column", width: "stretch",
                             items: [
                                 {
                                     type: "TextBlock", text: `Capacidad: ${habitacion.capacidad} persona(s)`,
                                     wrap: true, color: "Dark"
                                 }
                             ]
                         }
                     ]
                 },
                 {
                     type: "ColumnSet",
                     columns: [
                         {
                             type: "Column", width: "auto",
                             items: [
                                 {
                                     type: "TextBlock", text: "💵",  // Emoji para precio
                                     size: "Small", altText: "Precio", spacing: "None"
                                 }
                             ]
                         },
                         {
                             type: "Column", width: "stretch",
                             items: [
                                 {
                                     type: "TextBlock", text: `Precio: $${habitacion.precioPorNoche} por noche`,
                                     wrap: true, color: "Accent", weight: "Bolder"
                                 }
                             ]
                         }
                     ]
                 },
                 {
                     type: "ColumnSet",
                     columns: [
                         {
                             type: "Column", width: "auto",
                             items: [
                                 {
                                     type: "TextBlock", text: "📅", size: "Small",
                                     altText: "Disponibilidad", spacing: "None"
                                 }
                             ]
                         },
                         {
                             type: "Column", width: "stretch",
                             items: [
                                 {
                                     type: "TextBlock", text: habitacion.disponible ? "Disponible: Sí" : "Disponible: No",
                                     wrap: true, color: habitacion.disponible ? "Good" : "Attention", weight: "Bolder"
                                 }
                             ]
                         }
                     ]
                 },
                 {
                     type: "ActionSet",
                     actions: [
                         {
                             data: { habitacionId: habitacion.id }
                         }
                     ],
                     spacing: "Medium"
                 }
             ]
         };
    }

    createEmployeeCard(empleado) {
         const rolEmojis = {
             Recepcionista: "🛎️",
             Aseador: "🧹",
             Mesero: "🍽️",
             Mesera: "🍽️",
             default: "👔",
         };
         const emojiRol = rolEmojis[empleado.rol] || rolEmojis.default;
         return {
             type: "AdaptiveCard",
             body: [
                  {
                     type: "TextBlock", text: `ID: ${empleado.id}`,
                     size: "Medium", color: "Dark", spacing: "Small",
                 },
                 {
                     type: "TextBlock", text: `${empleado.nombre} ${empleado.apellido}`, weight: "Bolder",
                     size: "ExtraLarge", horizontalAlignment: "Center", color: "Accent", spacing: "Small",
                 },
                 {
                     type: "Image", url: "https://cdn-icons-png.flaticon.com/512/149/149071.png",
                     size: "Medium", altText: "Imagen del empleado", horizontalAlignment: "Center", spacing: "Medium"
                 },
                 {
                     type: "ColumnSet",
                     columns: [
                         {
                             type: "Column", width: "auto",
                             items: [
                                 {
                                     type: "TextBlock", text: emojiRol, size: "Large",
                                     spacing: "None", horizontalAlignment: "Center"
                                 }
                             ]
                         },
                         {
                             type: "Column", width: "stretch",
                             items: [
                                 {
                                     type: "TextBlock", text: `Rol: ${empleado.rol}`,
                                     size: "Medium", color: "Dark", spacing: "Small",
                                 }
                             ]
                         }
                     ]
                 },
                 // Emoji de Email
                 {
                     type: "ColumnSet",
                     columns: [
                         {
                             type: "Column", width: "auto",
                             items: [
                                 {
                                     type: "TextBlock", text: "✉️",  size: "Large",
                                     spacing: "None", horizontalAlignment: "Center"
                                 }
                             ]
                         },
                         {
                             type: "Column", width: "stretch",
                             items: [
                                 {
                                     type: "TextBlock", text: `Email: ${empleado.correoElectronico}`,
                                     size: "Medium", color: "Dark", spacing: "Small",
                                 }
                             ]
                         }
                     ]
                 }
             ],
             actions: [
                 {
                     type: "ActionSet",
                     actions: [
                         {
                             data: { empleadoId: empleado.id }
                         }
                     ],
                     spacing: "Medium"
                 }
             ],
             $schema: "http://adaptivecards.io/schemas/adaptive-card.json",
             version: "1.3",
         };
    }

    // Actualización del formulario de reserva
    createReservationForm(habitacionId, empleadoId, clienteId) {
        return CardFactory.adaptiveCard({
            $schema: "http://adaptivecards.io/schemas/adaptive-card.json",
            type: "AdaptiveCard",
            version: "1.3",
            body: [
                {
                    type: "TextBlock",
                    text: "📝 **Formulario de Confirmación de Reserva**",
                    wrap: true,
                    weight: "Bolder",
                    size: "Medium",
                    separator: true
                },
                {
                    type: "TextBlock",
                    text: `👤 **Cliente:** ${clienteId}`,
                    wrap: true,
                    spacing: "Medium"
                },
                {
                    type: "TextBlock",
                    text: `🛏️ **Habitación:** ${habitacionId}`,
                    wrap: true
                },
                {
                    type: "TextBlock",
                    text: `👨‍💼 **Empleado:** ${empleadoId || "No asignado"}`,
                    wrap: true
                },
                {
                    type: "TextBlock",
                    text: "🔎 **Completa los siguientes datos:**",
                    wrap: true,
                    separator: true,
                    weight: "Bolder"
                },
                {
                    type: "Input.Date",
                    id: "fechaIngreso",
                    label: "📅 Fecha de Ingreso"
                },
                {
                    type: "Input.Date",
                    id: "fechaSalida",
                    label: "📆 Fecha de Salida"
                },
                {
                    type: "Input.ChoiceSet",
                    id: "metodoPago",
                    label: "💳 Método de Pago",
                    choices: [
                        { title: "Tarjeta de Crédito", value: "TARJETA_CREDITO" },
                        { title: "Tarjeta de Débito", value: "TARJETA_DEBITO" },
                        { title: "Efectivo", value: "EFECTIVO" },
                        { title: "Transferencia Bancaria", value: "TRANSFERENCIA_BANCARIA" }
                    ],
                    placeholder: "Selecciona un método"
                },
                {
                    type: "Input.Number",
                    id: "total",
                    label: "💰 Total (COP)",
                    placeholder: "Ingresa el total",
                    min: 0
                }
            ],
            actions: [
                {
                    type: "Action.Submit",
                    title: "✅ Confirmar Reserva",
                    style: "positive",
                    data: {
                        action: "confirm",
                        clienteId,
                        empleadoId,
                        habitacionId
                    }
                },
                {
                    type: "Action.Submit",
                    title: "❌ Cancelar",
                    style: "destructive",
                    data: {
                        action: "cancel"
                    }
                }
            ]
        });
    }
}
module.exports.MyBot = MyBot;
