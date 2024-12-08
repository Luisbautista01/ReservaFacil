package com.gestion.hotel.Chatbot_Estado;

import com.gestion.hotel.Excepciones.ClienteNoEncontradoExcepcion;
import com.gestion.hotel.Excepciones.HabitacionNoEncontradaExcepcion;
import com.gestion.hotel.Modelo.Cliente;
import com.gestion.hotel.Modelo.Empleado;
import com.gestion.hotel.Modelo.Habitacion;
import com.gestion.hotel.Repositorio.ClienteRepositorio;
import com.gestion.hotel.Repositorio.EmpleadoRepositorio;
import com.gestion.hotel.Repositorio.HabitacionRepositorio;
import com.itextpdf.barcodes.BarcodeQRCode;
import com.itextpdf.io.exceptions.IOException;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/api/v1/usuarios-estado")
public class UsuarioControlador {

    private final UsuarioEstadoService usuarioEstadoService;
    private final ClienteRepositorio clienteRepositorio;
    private final HabitacionRepositorio habitacionRepositorio;
    private final EmpleadoRepositorio empleadoRepositorio;

    @Autowired
    public UsuarioControlador(UsuarioEstadoService usuarioEstadoService, ClienteRepositorio clienteRepositorio, HabitacionRepositorio habitacionRepositorio, EmpleadoRepositorio empleadoRepositorio) {
        this.usuarioEstadoService = usuarioEstadoService;
        this.clienteRepositorio = clienteRepositorio;
        this.habitacionRepositorio = habitacionRepositorio;
        this.empleadoRepositorio = empleadoRepositorio;
    }

    @GetMapping("/habitaciones/tipo/{tipo}")
    public ResponseEntity<List<Habitacion>> obtenerHabitacionesPorTipo(@PathVariable String tipo) {
        List<Habitacion> habitaciones = habitacionRepositorio.findByTipo(tipo);
        return habitaciones.isEmpty()
                ? new ResponseEntity<>(HttpStatus.NO_CONTENT)
                : new ResponseEntity<>(habitaciones, HttpStatus.OK);
    }

    @GetMapping("/clientes/nombre/{nombre}")
    public ResponseEntity<List<Cliente>> buscarClientesPorNombre(@PathVariable String nombre) {
        List<Cliente> clientes = clienteRepositorio.findByNombreContainingIgnoreCase(nombre);
        return clientes.isEmpty()
                ? new ResponseEntity<>(HttpStatus.NO_CONTENT)
                : new ResponseEntity<>(clientes, HttpStatus.OK);
    }

    @GetMapping("/empleados/rol")
    public ResponseEntity<List<Empleado>> buscarEmpleadosPorRol(@PathVariable String rol) {
        List<Empleado> empleados = empleadoRepositorio.findByRol(rol);
        return empleados.isEmpty()
                ? new ResponseEntity<>(HttpStatus.NO_CONTENT)
                : new ResponseEntity<>(empleados, HttpStatus.OK);
    }

    @PostMapping("/crear")
    public ResponseEntity<UsuarioEstado> crearEstado(@RequestBody UsuarioEstado usuarioEstado) {
        try {
            // Establecer la fecha de la reserva como la fecha actual
            usuarioEstado.setFechaReserva(LocalDate.now());

            // Guardar el estado en la base de datos
            UsuarioEstado nuevaReserva = usuarioEstadoService.guardarEstado(usuarioEstado);
            return new ResponseEntity<>(nuevaReserva, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioEstado> obteneEstadoPorId(@PathVariable Long id) {
        Optional<UsuarioEstado> reserva = usuarioEstadoService.obtenerEstadoPorId(id);
        return reserva.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<UsuarioEstado>> obtenerEstadosPorCliente(@PathVariable Long clienteId) {
        try {
            List<UsuarioEstado> usuarioEstados = usuarioEstadoService.obtenerEstadosPorCliente(clienteId);
            if (usuarioEstados.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(usuarioEstados, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}/comprobante-pdf")
    public ResponseEntity<byte[]> generarComprobantePdf(@PathVariable Long id) throws IOException {

        Optional<UsuarioEstado> estadoOpt = usuarioEstadoService.obtenerEstadoPorId(id);

        if (estadoOpt.isPresent()) {

            UsuarioEstado estado = estadoOpt.get();
            Cliente cliente = clienteRepositorio.findById(estado.getClienteId())
                    .orElseThrow(() -> new ClienteNoEncontradoExcepcion(estado.getClienteId()));
            Habitacion habitacion = habitacionRepositorio.findById(estado.getHabitacionId())
                    .orElseThrow(() -> new HabitacionNoEncontradaExcepcion(estado.getHabitacionId()));
            Empleado empleado = estado.getEmpleadoId() != null
                    ? empleadoRepositorio.findById(estado.getEmpleadoId()).orElse(null)
                    : null;

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            PdfDocument pdfDocument = new PdfDocument(new PdfWriter(byteArrayOutputStream));
            Document document = new Document(pdfDocument);

            // Configuración del documento
            try {
                PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            } catch (java.io.IOException e) {
                throw new RuntimeException(e);
            }
            PdfFont boldFont = null;
            try {
                boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            } catch (java.io.IOException e) {
                throw new RuntimeException(e);
            }

            // Logotipo y título centrados
            URL logoUrl = null;
            try {
                logoUrl = new URL("http://localhost:3978/img/logo.png");
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
            Image logo = new Image(ImageDataFactory.create(logoUrl)).setWidth(120).setHeight(100);

            Paragraph titulo = new Paragraph("CONFIRMACIÓN DE RESERVA")
                    .setFontSize(24)
                    .setBold()
                    .setFontColor(DeviceRgb.WHITE)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBackgroundColor(new DeviceRgb(33, 150, 243))
                    .setPadding(8);

            document.add(new Paragraph("\n"));
            Paragraph subtitulo = new Paragraph("Gracias por confiar en ReservaFácil")
                    .setFontSize(12)
                    .setFont(boldFont)
                    .setTextAlignment(TextAlignment.CENTER);

            document.add(logo.setHorizontalAlignment(HorizontalAlignment.CENTER));
            document.add(titulo);
            document.add(subtitulo);

            // Espaciado
            document.add(new LineSeparator(new SolidLine()));
            document.add(new Paragraph(""));

            // Tabla de detalles
            Table detallesTable = new Table(UnitValue.createPercentArray(new float[]{30, 70}))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setMarginTop(10);

            // Añadir detalles
            detallesTable.addCell(celdaNegrita("Cliente:"));
            detallesTable.addCell(celdaNormal(cliente.getNombre() + " " + cliente.getApellido()));

            detallesTable.addCell(celdaNegrita("Atendido por:"));
            detallesTable.addCell(celdaNormal(empleado != null
                    ? empleado.getNombre() + " " + empleado.getApellido()
                    : "N/A"));

            detallesTable.addCell(celdaNegrita("Tipo de Habitación:"));
            detallesTable.addCell(celdaNormal(habitacion.getTipo()));

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            detallesTable.addCell(celdaNegrita("Fecha de Reserva:"));
            detallesTable.addCell(celdaNormal(formatter.format(estado.getFechaReserva())));

            detallesTable.addCell(celdaNegrita("Fecha de Ingreso:"));
            detallesTable.addCell(celdaNormal(formatter.format(estado.getFechaIngreso())));

            detallesTable.addCell(celdaNegrita("Fecha de Salida:"));
            detallesTable.addCell(celdaNormal(formatter.format(estado.getFechaSalida())));

            detallesTable.addCell(celdaNegrita("Método de Pago:"));
            detallesTable.addCell(celdaNormal(estado.getMetodoPago()));

            detallesTable.addCell(celdaNegrita("Total:"));
            detallesTable.addCell(celdaNormal(estado.getTotal() + " COP"));

            document.add(detallesTable);

            // Pie de página centrado
            document.add(new Paragraph(""));
            document.add(new Paragraph(""));

            document.add(new LineSeparator(new SolidLine()));
            document.add(new Paragraph(""));

            // Pie de página centrado con fecha y hora
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            Paragraph footer = new Paragraph("Generado el: " + LocalDateTime.now().format(dateTimeFormatter) +
                    " | Número de Reserva: " + id)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(10);
            document.add(footer);

            // Código QR
            BarcodeQRCode qrCode = new BarcodeQRCode("Reserva ID: " + id + "\nCliente: " + cliente.getNombre() +
                    "\nTotal: " + estado.getTotal() + " COP");
            Image qrImage = new Image(qrCode.createFormXObject(pdfDocument))
                    .setWidth(100)
                    .setHeight(100)
                    .setHorizontalAlignment(HorizontalAlignment.CENTER);

            document.add(new Paragraph("Escanee el código QR para más detalles:")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(12)
                    .setFont(boldFont));
            document.add(qrImage);

            document.close();

            byte[] pdfBytes = byteArrayOutputStream.toByteArray();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=comprobante_reserva.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);
        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    private Cell celdaNegrita(String texto) {
        try {
            return new Cell().add(new Paragraph(texto).setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD)))
                    .setBorder(Border.NO_BORDER)
                    .setBackgroundColor(new DeviceRgb(200, 220, 255));
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Cell celdaNormal(String texto) {
        try {
            return new Cell().add(new Paragraph(texto).setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA)))
                    .setBorder(Border.NO_BORDER);
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
    }


}
