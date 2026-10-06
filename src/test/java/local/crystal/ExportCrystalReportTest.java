package local.crystal;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ExportCrystalReportTest {

    @Test
    void acceptsAnExistingReport() throws IOException {
        Path report = Files.createTempFile("report-", ".rpt");

        assertDoesNotThrow(() -> ExportCrystalReport.validateReportPath(report));
    }

    @Test
    void rejectsMissingReport() {
        Path report = Path.of("build", "does-not-exist.rpt");

        assertThrows(IOException.class, () -> ExportCrystalReport.validateReportPath(report));
    }

    @Test
    void parsesReportParameters() {
        var parameters = ExportCrystalReport.parseParameters("Company=ACME;OrderId=12345");

        assertEquals("ACME", parameters.get("Company"));
        assertEquals("12345", parameters.get("OrderId"));
    }

    @Test
    void rejectsMalformedReportParameter() {
        assertThrows(IllegalArgumentException.class,
                () -> ExportCrystalReport.parseParameters("Company"));
    }
}