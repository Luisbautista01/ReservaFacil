const restify = require('restify');
const path = require('path');

/**
 * Configura y devuelve el servidor Restify.
 * @param {BotFrameworkAdapter} adapter - El adaptador de bot para procesar actividades.
 * @param {MyBot} bot - La instancia del bot.
 * @returns {restify.Server} - Servidor configurado.
 */
module.exports = function configureServer(adapter, bot) {
    const server = restify.createServer();

    // Middleware para analizar el cuerpo de las solicitudes
    server.use(restify.plugins.bodyParser());

    // Servir imágenes desde la carpeta 'img'
    server.get('/img/*', restify.plugins.serveStatic({
        directory: path.join(__dirname, 'img'), // Asegúrate de que 'img' esté en el mismo nivel que tu archivo server.js
        appendRequestPath: false
    }));

    // Endpoint para procesar mensajes
    server.post('/api/messages', async (req, res) => {
        try {
            await adapter.processActivity(req, res, async (context) => {
                await bot.run(context);
            });
        } catch (error) {
            console.error(`Error al procesar la actividad: ${error.message || error}`);
            res.send(500, { error: 'Ocurrió un error procesando tu solicitud.' });
        }
    });

    return server;
};
