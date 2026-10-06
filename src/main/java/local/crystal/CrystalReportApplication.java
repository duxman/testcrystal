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

@SpringBootApplication
@EnableConfigurationProperties(CrystalDatabaseProperties.class)
public class CrystalReportApplication {

    public static void main(String[] args) {
        SpringApplication.run(CrystalReportApplication.class, args);
    }

    @Bean
    CommandLineRunner exportReport(ExportCrystalReport exporter) {
        return args -> {
            if (args.length != 3) {
                throw new IllegalArgumentException("Uso: CrystalReportApplication <entrada.rpt> <salida.pdf> [Campo=valor;Otro=valor]");
            }
            exporter.export(args[0], args[1], args[2]);
        };
    }

    @Bean
    LoggingSetup loggingSetup(Environment environment) {
        return new LoggingSetup(
                environment.getProperty("crystal.logging.directory", "logs"),
                environment.getProperty("crystal.logging.file", "crystal-exporter.log"),
                environment.getProperty("crystal.logging.level", "INFO"));
    }

    static final class LoggingSetup {
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