# Prueba Crystal Reports con Java 22

> Autor: Antonio Duce | Version base del programa: 0.1.0

La version final de cada artefacto se calcula automaticamente como la version
base mas la revision corta de Git, por ejemplo `0.1.0-fc727d4`. El manifiesto
del JAR contiene esa version y puede consultarse con `gradle gitVersion`.

Esta prueba usa Spring Boot 3 con una estructura convencional (`src/main/java` y
configuracion externa equivalente a `application.properties`). No tiene servidor web ni interfaz
grafica todavia: abre un RPT y genera un PDF. Lombok se usa para el logging del servicio.

## Configuracion externa obligatoria

La configuracion real no se empaqueta dentro del JAR. La plantilla esta en
`config-example/application.properties` y debe copiarse a una ruta protegida del
servidor:

- Windows Server 2022: `C:\ProgramData\CrystalReportService\config\application.properties`
- Red Hat 9: `/etc/crystal-report-service/application.properties`

Los scripts buscan esas rutas por defecto. Se puede cambiar la ruta con:

```text
CRYSTAL_CONFIG_DIR=/ruta/privada/de/configuracion
```

La ubicacion se pasa como `spring.config.location`, por lo que el fichero externo
es obligatorio y no hay un fallback de configuracion dentro del JAR. Al ejecutar
el JAR directamente, usar por ejemplo:

```powershell
java -jar build\libs\crystal-java22-test-0.1.0.jar `
  --spring.config.location=file:C:/ProgramData/CrystalReportService/config/ `
  reporte.rpt salida.pdf
```

La cuenta del servicio debe poder leer el fichero, pero los usuarios normales no
deberian tener acceso. La contraseña Oracle, wallets y otros secretos deben estar
en esa ruta protegida o en un gestor de secretos, nunca dentro del repositorio ni
del JAR.

Para comprobar que no se ha incluido por accidente:

```powershell
& 'C:\dev\tools\gradle\bin\gradle.bat' bootJar
jar tf build\libs\crystal-java22-test-*.jar | Select-String 'application.properties'
```

La segunda orden no debe devolver `application.properties`.

## Sobre las dependencias

Spring Boot y Lombok se resuelven desde Maven Central. El motor JRC de SAP no se
publica como dependencia Spring ni en Maven Central, por lo que sus JARs estan en
`lib_local`. No se pueden sustituir por dependencias de Spring: son el motor que
interpreta el formato `.rpt` y exporta el PDF.

La carpeta local se mantiene aislada para poder revisar mas adelante que JARs son
realmente necesarios. No conviene eliminar transitivas del runtime SAP a ciegas:
algunas se cargan de forma reflexiva durante la apertura o exportacion del reporte.

## Licenciamiento y revision legal

Esta prueba mezcla componentes con titulares y condiciones de uso diferentes. No
se debe interpretar este README como una confirmacion legal de que todo el
contenido pueda redistribuirse.

Antes de hacer commit o push del proyecto completo, Legal debe revisar como minimo:

1. **SAP Crystal Reports JRC**: los JAR de `lib_local` proceden del runtime de SAP.
  Hay que confirmar si la licencia permite incluirlos en este repositorio, en un
  instalador, en una imagen Docker y en la aplicacion productiva.
2. **Alcance de uso**: el escenario previsto es uso interno de la empresa para
  generar e imprimir albaranes de un operador 3PL. La aplicacion y el runtime no
  se distribuiran a clientes ni a terceros. Aun asi, Legal debe confirmar que la
  licencia de SAP permite este uso productivo interno y el numero de instalaciones
  previstas. Los escenarios de redistribucion masiva, SaaS, hosting para terceros
  o reventa no forman parte de este proyecto.
3. **Crystal Reports Designer**: el diseñador de escritorio y el runtime Java son
  productos/componentes diferentes. La licencia del diseñador no debe asumirse
  como permiso automatico para redistribuir el runtime.
4. **Archivos `.rpt`**: confirmar que podemos guardar en GitHub los reportes de
  prueba y que no contienen datos reales, credenciales, nombres sensibles,
  formulas propietarias o informacion de clientes.
5. **Base de datos Oracle**: confirmar la licencia y el modo de redistribucion del
  driver Oracle que se use en el proyecto real. El driver no esta incluido ahora.
6. **Dependencias de terceros**: revisar los avisos incluidos en
  `assets/CR4ERL32_0-80004572/third_party` y conservar sus textos de licencia si
  se redistribuyen esos componentes.
7. **Licencias del proyecto futuro**: decidir la licencia del codigo propio y si
  es compatible con las obligaciones de SAP, Oracle y las librerias auxiliares.

Hasta recibir confirmacion, la recomendacion para este escenario interno es:

- Mantener `lib_local` en repositorios y servidores internos con acceso restringido.
- No publicar reportes reales ni la base de datos `xtreme.mdb` fuera de los sistemas
  corporativos si no se ha validado su contenido y licencia.
- Mantener los JAR propietarios bajo control interno y documentar su procedencia.
- No incluir contraseñas, wallets Oracle, `tnsnames.ora` ni cadenas de conexion
  reales en Git.
- Mantener separados el codigo fuente, los binarios SAP y los reportes de cliente.

El hecho de que no exista distribucion externa reduce el alcance de la revision,
pero no elimina la necesidad de confirmar el uso productivo interno del runtime SAP,
el driver Oracle y los reportes utilizados para los albaranes.

Si se utiliza GitHub para guardar el codigo, el repositorio debe configurarse como
privado y con acceso solo a los equipos autorizados. Uso interno de la aplicacion
no equivale a repositorio publico: `lib_local`, los RPT y los datos de prueba no
deben quedar visibles por error.

La carpeta `lib_local` ocupa aproximadamente 72 MB y contiene 26 JAR del runtime.
Su presencia local permite probar, pero no demuestra que el repositorio tenga
derecho a redistribuirlos. La aprobacion debe basarse en los acuerdos y terminos
vigentes de SAP, no solo en que los archivos se hayan podido descargar.

### Evidencias para Legal

Conviene entregar a Legal:

- Nombre y version exacta del paquete SAP descargado.
- URL y cuenta/licencia con la que se obtuvo.
- Lista de archivos que se pretenden distribuir.
- Destino: servidores y puestos internos de la empresa.
- Numero estimado de instalaciones, servidores, impresoras y usuarios internos.
- Si los `.rpt` son propios, de un cliente o de terceros.
- Que los documentos generados son albaranes operativos de un 3PL y si contienen
  datos personales, direcciones, referencias de pedido o datos comerciales.
- Lista de avisos de terceros y sus textos de licencia.
- Si Oracle se conecta por JDBC, wallet, alias TNS o servicio gestionado.

La nota [precio licencia.md](../doc/precio%20licencia.md) contiene una orientacion
inicial, pero no es una licencia ni una autorizacion de redistribucion. Debe
confirmarse contra los terminos actuales de SAP y el caso de uso concreto.

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

La impresora compartida es el cuarto argumento y se decide para cada trabajo:

```bat
run-report.bat "Crystal-Reports-master\Customer List.rpt" "build\output\customer.pdf" "Country=USA;CustomerId=12345" "\\PRINTSERVER\ALBARANES_03"
```

El destino no se guarda en la configuracion global porque puede haber cientos de
impresoras. Actualmente se registra como destino solicitado junto al resultado;
el adaptador que lo entregue al servidor de impresion se conectara despues.

El formato inicial es `Nombre=valor;OtroNombre=valor`. El nombre debe coincidir
exactamente con el nombre del parametro definido en Crystal. Los valores se pasan
como texto; para fechas, numeros con formato regional o parametros multivalor
habra que añadir conversion tipada cuando conozcamos los RPT reales.

El reporte debe poder resolverse sin pedir credenciales o parametros interactivos.
Si usa una base de datos, el siguiente paso sera configurar el `DatabaseController` antes de exportar.

En Windows, [run-report.bat](run-report.bat) exige que exista la configuracion
externa y usa `C:\ProgramData\CrystalReportService\config` por defecto. En Red Hat,
usa [run-report.sh](run-report.sh) y `/etc/crystal-report-service` por defecto:

```bash
chmod 750 run-report.sh
./run-report.sh /ruta/Customer\ List.rpt /var/lib/crystal-report-service/output/customer.pdf 'Country=USA'
```

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

El nivel se configura en el `application.properties` externo:

```properties
crystal.logging.level=INFO
```

Valores utiles: `OFF`, `ERROR`, `WARN`, `INFO`, `DEBUG`, `TRACE` y `ALL`.
`DEBUG` se traduce a `FINE` y `TRACE` a `FINER` de `java.util.logging`.

El proyecto no incluye `spring-boot-starter-web` ni el starter de logging de Spring Boot:
no son necesarios para este proceso de consola. Para convertirlo en una aplicacion
web se anadira `spring-boot-starter-web` y, para monitorizacion, `spring-boot-starter-actuator`.

## Impresion en servidor Windows

La generacion del PDF es independiente del sistema operativo. El proceso puede
correr en Windows Server 2022 o Red Hat 9. Las impresoras del 3PL estan en un
servidor de impresion Windows. La impresora concreta se recibe por trabajo como
ruta compartida UNC, por ejemplo `\\PRINTSERVER\ALBARANES_03`:

```text
RPT + Oracle -> PDF en el servidor de la aplicacion -> cola del servidor Windows
```

No se debe asumir que `java.awt.print` vera una impresora Windows desde Red Hat.
Para esa fase habra que elegir un mecanismo autorizado por infraestructura, por
ejemplo una cola accesible por IPP/SMB, un servicio HTTP interno o un agente
Windows que reciba el PDF y la ruta de impresora. Se mantienen abiertas esas
opciones. La configuracion global solo controla la politica:

```properties
crystal.print.enabled=false
crystal.print.keep-pdf=true
```

El PDF se conserva siempre antes de intentar imprimir. No debe borrarse tras el
envio: sera el artefacto auditable, permitira reintentar una impresion y ayudara
a verificar fallos o diferencias entre la salida y el documento fisico.

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