// Commit-Date: 2026-10-06T21:45:00Z
// Commit-Version: 0.1.0-20261006214500
package local.crystal;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propiedades externas usadas para iniciar sesion en la base de datos del reporte.
 *
 * <p>Autor: Antonio Duce. Version del programa: 0.1.0.</p>
 * <p>La contrasena debe proceder de la configuracion protegida o de un secreto,
 * nunca de los fuentes ni del repositorio.</p>
 */
@ConfigurationProperties(prefix = "crystal.database")
public class CrystalDatabaseProperties {

    private boolean enabled;
    private String server = "";
    private String name = "";
    private String username = "";
    private String password = "";

    /** Indica si se debe reemplazar la conexion guardada en el RPT. */
    public boolean isEnabled() {
        return enabled;
    }

    /** Activa o desactiva la conexion externa de Crystal. */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /** Devuelve el servidor o instancia de Oracle configurado. */
    public String getServer() {
        return server;
    }

    /** Configura el servidor o instancia de Oracle. */
    public void setServer(String server) {
        this.server = server;
    }

    /** Devuelve el nombre de la base de datos. */
    public String getName() {
        return name;
    }

    /** Configura el nombre de la base de datos. */
    public void setName(String name) {
        this.name = name;
    }

    /** Devuelve el usuario de la conexion. */
    public String getUsername() {
        return username;
    }

    /** Configura el usuario de la conexion. */
    public void setUsername(String username) {
        this.username = username;
    }

    /** Devuelve la contrasena sin escribirla en logs. */
    public String getPassword() {
        return password;
    }

    /** Configura la contrasena recibida desde la configuracion externa. */
    public void setPassword(String password) {
        this.password = password;
    }
}
