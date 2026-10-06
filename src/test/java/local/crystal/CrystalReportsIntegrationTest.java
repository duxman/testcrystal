package local.crystal;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prueba de integracion que exporta los RPT de prueba a PDFs temporales de build.
 * Autor: Antonio Duce. Version del programa: 0.1.0.
 */
@Tag("integration")
class CrystalReportsIntegrationTest {

    /** Verifica que todos los reportes del directorio de muestras puedan exportarse. */
    @Test
    void everyReportCanBeExported() throws Exception {
        Path reportsDirectory = Path.of("Crystal-Reports-master");
        Path outputDirectory = Path.of("build", "integration-output");
        ExportCrystalReport exporter = new ExportCrystalReport();
        List<String> failures = new ArrayList<>();

        try (Stream<Path> reports = Files.list(reportsDirectory)) {
            reports.filter(path -> path.toString().toLowerCase().endsWith(".rpt"))
                    .forEach(report -> {
                        try {
                            Path pdf = outputDirectory.resolve(report.getFileName().toString() + ".pdf");
                            exporter.export(report.toString(), pdf.toString());
                        } catch (Exception exception) {
                            failures.add(report.getFileName() + ": " + exception.getMessage());
                        }
                    });
        }

        assertTrue(!failures.isEmpty() || Files.exists(outputDirectory),
                "No se exporto ningun reporte");
        assertTrue(failures.isEmpty(), "Fallos de exportacion: " + failures);
    }
}