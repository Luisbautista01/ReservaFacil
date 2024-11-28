const { BotFrameworkAdapter } = require('botbuilder');
const restify = require('restify');
const { MyBot } = require('./bot');
const configureServer = require('./server');
require('dotenv').config();

// Configurar el adaptador
const adapter = new BotFrameworkAdapter({
    appId: process.env.MicrosoftAppId,
    appPassword: process.env.MicrosoftAppPassword
});

// Manejo de errores globales
adapter.onTurnError = async (context, error) => {
    console.error(`\n [onTurnError]: ${error.message || error}`);
    await context.sendActivity('Hubo un error al procesar tu solicitud.');
};

// Inicializar el bot
const bot = new MyBot();

// Crear e iniciar el servidor
const server = configureServer(adapter, bot);

// Puerto de escucha
const PORT = process.env.PORT || 3978;
server.listen(PORT, () => {
    console.log(`\nBot running on http://localhost:${PORT}`);
});





