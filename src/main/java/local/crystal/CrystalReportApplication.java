// Commit-Date: 2026-10-06T21:45:00Z
// Commit-Version: 0.1.0-20261006214500
package local.crystal;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Punto de entrada headless para exportar un RPT y dejar un PDF persistente.
 *
 * <p>Autor: Antonio Duce. Version del programa: 0.1.0.</p>
 * <p>La configuracion se carga desde fuera del JAR mediante Spring Boot. La ruta
 * de impresora es un dato del trabajo y no una propiedad global.</p>
 */
@SpringBootApplication
@EnableConfigurationProperties(CrystalDatabaseProperties.class)
public class CrystalReportApplication {

    /** Inicia Spring Boot y delega la exportacion al runner de linea de comandos. */
    public static void main(String[] args) {
        SpringApplication.run(CrystalReportApplication.class, args);
    }

    /** Valida los argumentos y genera el PDF antes de registrar el destino de impresion. */
    @Bean
    CommandLineRunner exportReport(ExportCrystalReport exporter) {
        return args -> {
            if (args.length < 3 || args.length > 4) {
                throw new IllegalArgumentException("Uso: CrystalReportApplication <entrada.rpt> <salida.pdf> [Campo=valor;Otro=valor] [\\\\servidor\\impresora]");
            }
            exporter.export(args[0], args[1], args[2]);
            if (args.length == 4 && !args[3].isBlank()) {
                Logger.getLogger(CrystalReportApplication.class.getName()).info(
                        () -> "Destino de impresion solicitado para el PDF conservado: " + args[3]);
            }
        };
    }

    /** Configura consola y fichero rotativo usando propiedades externas. */
    @Bean
    LoggingSetup loggingSetup(Environment environment) {
        return new LoggingSetup(
                environment.getProperty("crystal.logging.directory", "logs"),
                environment.getProperty("crystal.logging.file", "crystal-exporter.log"),
                environment.getProperty("crystal.logging.level", "INFO"));
    }

    static final class LoggingSetup {
        /** Inicializa los handlers y el nivel global de java.util.logging. */
        LoggingSetup(String logDirectoryName, String logFileName, String logLevelName) {
            try {
            Level logLevel = parseLevel(logLevelName);
                Path logDirectory = Path.of(logDirectoryName);
                Files.createDirectories(logDirectory);

                Logger rootLogger = Logger.getLogger("");
                for (Handler handler : rootLogger.getHandlers()) {
                    rootLogger.removeHandler(handler);
                }

                FileHandler fileHandler = new FileHandler(
                    logDirectory.resolve(logFileName).toString(),
                        10 * 1024 * 1024,
                        5,
                        true);
                fileHandler.setFormatter(new SimpleFormatter());
                fileHandler.setLevel(logLevel);

                ConsoleHandler consoleHandler = new ConsoleHandler();
                consoleHandler.setFormatter(new SimpleFormatter());
                consoleHandler.setLevel(logLevel);

                rootLogger.addHandler(fileHandler);
                rootLogger.addHandler(consoleHandler);
                rootLogger.setLevel(logLevel);
                Logger.getLogger("com.crystaldecisions").setLevel(logLevel);
            } catch (IOException exception) {
                throw new IllegalStateException("No se pudo configurar el log", exception);
            }
        }

        private static Level parseLevel(String value) {
            return switch (value.trim().toUpperCase()) {
                case "TRACE" -> Level.FINER;
                case "DEBUG" -> Level.FINE;
                case "WARN" -> Level.WARNING;
                case "ERROR" -> Level.SEVERE;
                case "OFF" -> Level.OFF;
                case "ALL" -> Level.ALL;
                default -> Level.parse(value.trim().toUpperCase());
            };
        }
    }
}
