# Prueba Crystal Reports con Java 22

Esta prueba usa Spring Boot 3 con una estructura convencional (`src/main/java` y
`src/main/resources/application.properties`). No tiene servidor web ni interfaz
grafica todavia: abre un RPT y genera un PDF. Lombok se usa para el logging del servicio.

## Sobre las dependencias

Spring Boot y Lombok se resuelven desde Maven Central. El motor JRC de SAP no se
publica como dependencia Spring ni en Maven Central, por lo que sus JARs estan en
`lib_local`. No se pueden sustituir por dependencias de Spring: son el motor que
interpreta el formato `.rpt` y exporta el PDF.

La carpeta local se mantiene aislada para poder revisar mas adelante que JARs son
realmente necesarios. No conviene eliminar transitivas del runtime SAP a ciegas:
algunas se cargan de forma reflexiva durante la apertura o exportacion del reporte.

## Ejecucion sencilla

Puedes arrastrar un `.rpt` sobre `run-report.bat`, o ejecutarlo desde una consola:

```bat
run-report.bat "C:\ruta\mi-reporte.rpt"
```

Para los reportes reales de `Crystal-Reports-master` puedes usar el BAT corto:

```bat
run-crystal-report.bat "Customer List.rpt" "build\output\customer.pdf" "Country=USA;CustomerId=12345"
```

Si no indicas nombre, usa `BeforeTV.rpt` como prueba:

```bat
run-crystal-report.bat
```

La salida se crea en `build/output` con el mismo nombre del reporte. Tambien puedes indicar la ruta exacta del PDF:

```bat
run-report.bat "C:\ruta\mi-reporte.rpt" "C:\ruta\salida\mi-reporte.pdf"
```

## Ejecutar

Desde esta carpeta, usando el Gradle local indicado:

```powershell
$env:JAVA_HOME = 'C:\dev\tools\java\jdk22'
& 'C:\dev\tools\gradle\bin\gradle.bat' runReport --no-daemon
```

El comando abre el reporte de ejemplo `Statement of Account.rpt` y escribe:

```text
build/output/reporte.pdf
```

Para probar otro reporte:

```powershell
& 'C:\dev\tools\gradle\bin\gradle.bat' runReport `
  "-Prpt=C:\ruta\mi-reporte.rpt" `
  "-Ppdf=C:\ruta\salida\mi-reporte.pdf" `
  "-Pparameters=Company=ACME;OrderId=12345"
```

Los parametros se aplican al reporte principal antes de exportar mediante
`ParameterFieldController.setCurrentValue`. En el BAT se pueden pasar como tercer
argumento:

```bat
run-report.bat "Crystal-Reports-master\Customer List.rpt" "build\output\customer.pdf" "Country=USA;CustomerId=12345"
```

El formato inicial es `Nombre=valor;OtroNombre=valor`. El nombre debe coincidir
exactamente con el nombre del parametro definido en Crystal. Los valores se pasan
como texto; para fechas, numeros con formato regional o parametros multivalor
habra que añadir conversion tipada cuando conozcamos los RPT reales.

El reporte debe poder resolverse sin pedir credenciales o parametros interactivos.
Si usa una base de datos, el siguiente paso sera configurar el `DatabaseController` antes de exportar.

## Pruebas

Los tests unitarios se ejecutan con:

```powershell
& 'C:\dev\tools\gradle\bin\gradle.bat' test --no-daemon
```

La prueba de integracion intenta abrir y exportar todos los `.rpt` de
`Crystal-Reports-master`; puede tardar y depende de que los reportes puedan
resolver sus datos sin credenciales:

```powershell
& 'C:\dev\tools\gradle\bin\gradle.bat' integrationTest --no-daemon
```

Sus PDFs se escriben en `build/integration-output` y los fallos aparecen en el informe de JUnit.

## Logs

Cada ejecucion escribe en `logs/crystal-exporter.log.0` y mantiene hasta cinco
ficheros rotados de 10 MB. Se registra el reporte de entrada, el PDF de salida,
su tamano y el stack trace completo si la exportacion falla. La carpeta `logs`
esta excluida de Git porque contiene resultados de ejecucion.

El nivel se configura en `src/main/resources/application.properties`:

```properties
crystal.logging.level=INFO
```

Valores utiles: `OFF`, `ERROR`, `WARN`, `INFO`, `DEBUG`, `TRACE` y `ALL`.
`DEBUG` se traduce a `FINE` y `TRACE` a `FINER` de `java.util.logging`.

El proyecto no incluye `spring-boot-starter-web` ni el starter de logging de Spring Boot:
no son necesarios para este proceso de consola. Para convertirlo en una aplicacion
web se anadira `spring-boot-starter-web` y, para monitorizacion, `spring-boot-starter-actuator`.

## Conexion de base de datos

Por defecto el reporte usa la conexion guardada en el propio RPT. Para forzar una
conexion antes de exportar, configura estas propiedades en el entorno o en
`application.properties`:

```properties
crystal.database.enabled=true
crystal.database.server=servidor\instancia
crystal.database.name=nombre_base_datos
crystal.database.username=usuario
crystal.database.password=secreto
```

El exportador llama a `DatabaseController.logonEx(...)` antes de generar el PDF.
La contraseña nunca se escribe en el log. En un entorno real conviene inyectarla
como variable o secreto, por ejemplo `CRYSTAL_DATABASE_PASSWORD`, en vez de
guardarla en el fichero.