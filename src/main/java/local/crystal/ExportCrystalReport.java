package local.crystal;

import com.crystaldecisions.sdk.occa.report.application.PrintOutputController;
import com.crystaldecisions.sdk.occa.report.application.ReportClientDocument;
import com.crystaldecisions.sdk.occa.report.exportoptions.ReportExportFormat;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Servicio que abre un RPT con SAP Crystal Reports JRC y conserva su exportacion PDF.
 *
 * <p>Autor: Antonio Duce. Version del programa: 0.1.0.</p>
 * <p>La impresion fisica se mantiene fuera de esta clase: el PDF debe existir antes
 * de enviarlo a una impresora dinamica del servidor Windows.</p>
 */
@Log
@Service
public class ExportCrystalReport {

    private final CrystalDatabaseProperties databaseProperties;

    public ExportCrystalReport() {
        this(new CrystalDatabaseProperties());
    }

    @Autowired
    public ExportCrystalReport(CrystalDatabaseProperties databaseProperties) {
        this.databaseProperties = databaseProperties;
    }

    /** Exporta un reporte sin parametros usando la ruta de salida indicada. */
    public void export(String reportFile, String pdfFile) throws Exception {
        export(reportFile, pdfFile, "");
    }

    /**
     * Abre el RPT, configura base de datos y parametros, y escribe un PDF completo.
     * El archivo existente se reemplaza para que los reintentos sean deterministas.
     */
    public void export(String reportFile, String pdfFile, String parameterText) throws Exception {
        Path reportPath = Path.of(reportFile).toAbsolutePath().normalize();
        Path pdfPath = Path.of(pdfFile).toAbsolutePath().normalize();
        Map<String, String> parameters = parseParameters(parameterText);

        validateReportPath(reportPath);

        Files.createDirectories(pdfPath.getParent());
        log.info(() -> "Iniciando exportacion: " + reportPath + " -> " + pdfPath);

        ReportClientDocument report = null;
        try {
            report = ReportClientDocument.openReport(new File(reportPath.toString()));
            configureDatabase(report);
            configureParameters(report, parameters);
            PrintOutputController outputController = report.getPrintOutputController();

            try (InputStream pdf = outputController.export(ReportExportFormat.PDF)) {
                Files.copy(pdf, pdfPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }

            long pdfSize = Files.size(pdfPath);
            log.info(() -> "PDF generado: " + pdfPath + " (" + pdfSize + " bytes)");
            } catch (Exception exception) {
                log.log(java.util.logging.Level.SEVERE,
                    "Error exportando " + reportPath + " a " + pdfPath, exception);
                throw exception;
        } finally {
            if (report != null && report.isOpen()) {
                report.close();
            }
        }
    }

    /** Comprueba que la entrada existe y es un fichero antes de invocar el runtime JRC. */
    static void validateReportPath(Path reportPath) throws IOException {
        if (!Files.isRegularFile(reportPath)) {
            throw new IOException("No existe el reporte: " + reportPath);
        }
    }

    /**
     * Convierte el formato de linea de comandos Nombre=valor;Otro=valor en un mapa.
     * El separador de igualdad solo se interpreta en su primera aparicion para
     * permitir valores que contengan dicho caracter.
     */
    static Map<String, String> parseParameters(String parameterText) {
        Map<String, String> parameters = new LinkedHashMap<>();
        if (parameterText == null || parameterText.isBlank()) {
            return parameters;
        }

        for (String entry : parameterText.split(";")) {
            if (entry.isBlank()) {
                continue;
            }
            int separator = entry.indexOf('=');
            if (separator <= 0) {
                throw new IllegalArgumentException("Parametro invalido: " + entry + ". Usa Campo=valor");
            }
            String name = entry.substring(0, separator).trim();
            String value = entry.substring(separator + 1).trim();
            if (name.isBlank()) {
                throw new IllegalArgumentException("El nombre del parametro no puede estar vacio");
            }
            parameters.put(name, value);
        }
        return parameters;
    }

    /** Aplica los parametros de texto al reporte principal mediante la API JRC. */
    private void configureParameters(ReportClientDocument report, Map<String, String> parameters) throws Exception {
        if (parameters.isEmpty()) {
            return;
        }

        log.info(() -> "Configurando parametros Crystal: " + parameters.keySet());
        var parameterController = report.getDataDefController().getParameterFieldController();
        for (Map.Entry<String, String> parameter : parameters.entrySet()) {
            parameterController.setCurrentValue("", parameter.getKey(), parameter.getValue());
        }
    }

    /** Inicia sesion contra la base de datos solo cuando la configuracion lo solicita. */
    private void configureDatabase(ReportClientDocument report) throws Exception {
        if (!databaseProperties.isEnabled()) {
            return;
        }

        requireDatabaseValue("server", databaseProperties.getServer());
        requireDatabaseValue("name", databaseProperties.getName());
        requireDatabaseValue("username", databaseProperties.getUsername());
        requireDatabaseValue("password", databaseProperties.getPassword());

        log.info(() -> "Configurando conexion Crystal para servidor "
                + databaseProperties.getServer() + " y base de datos " + databaseProperties.getName());
        report.getDatabaseController().logonEx(
                databaseProperties.getServer(),
                databaseProperties.getName(),
                databaseProperties.getUsername(),
                databaseProperties.getPassword());
    }

    /** Rechaza una propiedad obligatoria ausente sin revelar su contenido. */
    private static void requireDatabaseValue(String name, String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Falta crystal.database." + name);
        }
    }
}