package local.crystal;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Pruebas unitarias de validacion de ficheros y parseo de parametros.
 * Autor: Antonio Duce. Version del programa: 0.1.0.
 */
class ExportCrystalReportTest {

    /** Confirma que un RPT existente pasa la validacion inicial. */
    @Test
    void acceptsAnExistingReport() throws IOException {
        Path report = Files.createTempFile("report-", ".rpt");

        assertDoesNotThrow(() -> ExportCrystalReport.validateReportPath(report));
    }

    /** Confirma que una entrada inexistente se rechaza antes de abrir JRC. */
    @Test
    void rejectsMissingReport() {
        Path report = Path.of("build", "does-not-exist.rpt");

        assertThrows(IOException.class, () -> ExportCrystalReport.validateReportPath(report));
    }

    /** Comprueba que el formato de parametros se transforma en pares nombre-valor. */
    @Test
    void parsesReportParameters() {
        var parameters = ExportCrystalReport.parseParameters("Company=ACME;OrderId=12345");

        assertEquals("ACME", parameters.get("Company"));
        assertEquals("12345", parameters.get("OrderId"));
    }

    /** Comprueba que una entrada sin igualdad produce un error claro. */
    @Test
    void rejectsMalformedReportParameter() {
        assertThrows(IllegalArgumentException.class,
                () -> ExportCrystalReport.parseParameters("Company"));
    }
}