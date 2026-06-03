# Plan de desarrollo — Sistema de venta de pasajes Copetran

## Contexto del proyecto

Sistema de escritorio para la empresa de transporte terrestre **Copetran**, que gestiona la venta de pasajes intermunicipales, control de flota, cancelaciones y reportes operativos. La aplicación es de escritorio, construida en **Java puro (vanilla)** sin frameworks externos.

---

## Tecnologías

| Componente | Tecnología |
|---|---|
| Lenguaje | Java 17+ (vanilla, sin frameworks) |
| GUI | Java Swing (javax.swing) |
| Persistencia | En memoria (arreglos y colecciones Java) |
| IDE sugerido | NetBeans / IntelliJ IDEA |
| Build | Sin Maven/Gradle — compilación directa javac |

---

## Arquitectura — dos capas (paquetes)

```
src/
├── negocio/          ← Capa de negocio (modelos + lógica)
│   ├── TipoEmpleado.java
│   ├── Persona.java
│   ├── Empleado.java
│   ├── Pasajero.java
│   ├── Bus.java
│   ├── BusTipoNormal.java
│   ├── BusTipoEjecutivo.java
│   ├── Puesto.java
│   ├── Ruta.java
│   ├── Salida.java
│   ├── PasajeTicket.java
│   ├── CajaVenta.java
│   └── EmpresaTransporte.java   ← Clase contenedora del problema
│
└── presentacion/     ← Capa de presentación (Swing)
    ├── LoginFrame.java
    ├── DashboardFrame.java
    ├── ParametrizacionPanel.java
    ├── VentasPanel.java
    ├── CancelacionesPanel.java
    └── ReportesPanel.java
```

---

## Diagrama de clases (referencia)

El diagrama adjunto define las siguientes clases y relaciones:

### Enumeración
- **TipoEmpleado**: `cajero`, `administrador`

### Jerarquía de personas
- **Persona** *(base)*: `cedula: String`, `nombre: String`, `direccion: String`
  - **Empleado** extends Persona: `idEmpleado: String`, `rol: TipoEmpleado`
  - **Pasajero** extends Persona: `idPasajero: String`

### Flota
- **Bus** *(abstracta)*: `placa: String`, `estado: String`
  - **BusTipoNormal** extends Bus: `myPuestos[]: Puestos (40)`
  - **BusTipoEjecutivo** extends Bus: `myPuestos[]: Puestos (40)`
- **Puesto**: `fila: char`, `numero: int`, `myPasajero: Pasajero`

### Rutas y salidas
- **Ruta**: `codigo: String`, `origen: String`, `destino: String`, `tarifa: float`
- **Salida**: `idSalida: String`, `myRuta: Ruta`, `fecha: Date`, `myBus: Bus`, `estado: String`

### Tickets y caja
- **PasajeTicket**: `mySalida: Salida`, `puesto: int`, `valorPagar: float`, `estado: String`, `myPasajero: Pasajero`
- **CajaVenta**: `montoCaja: float`, `totalVendidos: int`, `totalReembolsado: int`, `ingresoNeto: float`

### Contenedor principal
- **EmpresaTransporte**: agrega `Bus[]`, `Ruta[]`, `PasajeTicket[]`, `Caja: CajaVenta`, `Empleado[]`

---

## Requerimientos funcionales

### RF1 — Parametrización del sistema

La empresa debe poder gestionar su catálogo operativo:

**Buses:**
- Crear un bus recibiendo: `placa (String)`, `estado (String)`, tipo (`Normal` o `Ejecutivo`)
- Listar todos los buses registrados: `listarBuses(): Bus[]`
- Editar estado de un bus por placa: `editarBus(placa: String)`
- Eliminar un bus por placa: `eliminarBus(placa: String)`

**Rutas:**
- Crear una ruta con: `codigo`, `origen`, `destino`, `tarifa`
- Validar unicidad por `codigoRuta`
- Listar rutas: `listarRutas(): Ruta[]`
- Editar y eliminar ruta por código

**Salidas:**
- Crear una salida programada asignando: ruta, bus, fecha, hora
- Validar unicidad por combinación `(placa + codigoRuta + idSalida)`
- Listar salidas: `listarSalidas(): Salida[]`
- Editar y cancelar salida por ID

---

### RF2 — Venta de pasaje (1 tiquete)

Flujo completo de venta individual:

1. Seleccionar una salida disponible (estado `PROGRAMADA`)
2. Mostrar puestos disponibles del bus asignado a esa salida
3. Registrar datos del pasajero (`cedula`, `nombre`, `direccion`)
4. Asignar pasajero a puesto seleccionado
5. Calcular valor a pagar (tarifa de la ruta)
6. Generar `PasajeTicket` con estado `VIGENTE`
7. Actualizar `CajaVenta`: incrementar `montoCaja` y `totalVendidos`

Método principal en `EmpresaTransporte`:
```
registrarPasajeroYSilla(): void
generarTicket(): Pasaje
```

---

### RF3 — Venta ida y vuelta (2 tiquetes en una transacción)

Extensión del RF2 para viaje de regreso:

1. Ejecutar venta de salida ida (igual que RF2)
2. Seleccionar salida de regreso en la **misma ruta** (validar que `ruta.codigo` coincida)
3. Validar disponibilidad de puestos en salida de regreso
4. Aplicar **descuento del 10%** sobre el valor total de ambos tiquetes
5. Generar dos `PasajeTicket` en una sola transacción
6. Actualizar `CajaVenta` con el total descontado

Método en `EmpresaTransporte`:
```
ventaIdaYVuelta(): Boolean
```

---

### RF4 — Cancelación de salida

Gestión de cancelación con reprogramación o reembolso:

1. Cambiar estado de la `Salida` a `CANCELADA`
2. Buscar todos los `PasajeTicket` con estado `VIGENTE` asociados a esa salida
3. Para cada ticket vigente, aplicar una de las dos acciones:
   - **Reprogramar automáticamente**: buscar la próxima `Salida` con estado `PROGRAMADA` en la misma ruta que tenga cupo disponible, y reasignar el ticket
   - **Reembolsar**: cambiar estado del ticket a `REEMBOLSADO`, descontar de `montoCaja` y aumentar `totalReembolsado` en `CajaVenta`
4. Generar reporte del proceso de cancelación

Método en `EmpresaTransporte`:
```
cancelarSalida(idSalida: String): void
```

---

### RF5 — Reportes del día

Generación de reportes operativos desde `CajaVenta`:

- **(a) Ventas por ruta**: cantidad de tiquetes vendidos agrupados por `ruta.codigo`
- **(b) Totales del día**: `totalVendido`, `totalReembolsado`, `ingresoNeto`
- **(c) Ventas por mes o rango de fechas**: filtrar tickets por `salida.fecha` en un rango dado

Métodos en `CajaVenta`:
```
getMontoCaja(): float
getTotalVendidos(): int
getTotalReembolsos(): int
getIngresoNeto(): float
reportesDelDia(): CajaVenta
```

---

## Iteración 1 — Capa de negocio (alcance actual)

**Objetivo**: implementar completamente el paquete `negocio` con todos los modelos y la lógica de los RF1 al RF5. La presentación ya está iniciada y se retoma en la iteración 2.

### Tareas

#### 1. Enumeración y jerarquía base
- [ ] Crear `TipoEmpleado.java` (enum: `CAJERO`, `ADMINISTRADOR`)
- [ ] Crear `Persona.java` (clase abstracta con `cedula`, `nombre`, `direccion`)
- [ ] Crear `Empleado.java` extends `Persona` con `idEmpleado`, `rol: TipoEmpleado`
- [ ] Crear `Pasajero.java` extends `Persona` con `idPasajero`

#### 2. Flota
- [ ] Crear `Puesto.java` con `fila: char`, `numero: int`, `myPasajero: Pasajero` (null = libre)
- [ ] Crear `Bus.java` (abstracta) con `placa`, `estado`, arreglo `myPuestos[]`
- [ ] Crear `BusTipoNormal.java` extends `Bus` — inicializa 40 puestos, implementa `mostrarPuestosLibres(): int[]`
- [ ] Crear `BusTipoEjecutivo.java` extends `Bus` — inicializa 40 puestos, implementa `mostrarPuestosLibres(): int[]`

#### 3. Rutas y salidas
- [ ] Crear `Ruta.java` con `codigo`, `origen`, `destino`, `tarifa`
- [ ] Crear `Salida.java` con `idSalida`, `myRuta`, `fecha`, `myBus`, `estado` (estados: `PROGRAMADA`, `EN_RUTA`, `COMPLETADA`, `CANCELADA`)

#### 4. Tickets y caja
- [ ] Crear `PasajeTicket.java` con `mySalida`, `puesto: int`, `valorPagar`, `estado` (estados: `VIGENTE`, `REEMBOLSADO`), `myPasajero`
- [ ] Crear `CajaVenta.java` con atributos y todos los métodos del RF5

#### 5. Contenedor principal — `EmpresaTransporte.java`
- [ ] Atributos: `myBuses[]`, `myRutas[]`, `myTickets[]`, `myCaja: CajaVenta`, `myEmpleado[]`
- [ ] Implementar RF1: `crearBus()`, `listarBuses()`, `editarBus()`, `eliminarBus()`, `crearRuta()`, `listarRutas()`, `editarRuta()`, `eliminarRuta()`, `crearSalida()`, `listarSalidas()`, `editarSalida()`, `cancelarSalida()`
- [ ] Implementar RF2: `registrarPasajeroYSilla()`, `calcularValorPasaje()`, `generarTicket()`
- [ ] Implementar RF3: `ventaIdaYVuelta()` con validación de misma ruta y descuento del 10%
- [ ] Implementar RF4: `cancelarSalida()` con lógica de reprogramación automática o reembolso
- [ ] Implementar RF5: delegar en `CajaVenta` los métodos de reporte

#### 6. Validaciones requeridas
- [ ] Unicidad de `placa` al crear buses
- [ ] Unicidad de `codigoRuta` al crear rutas
- [ ] Unicidad de `(placa + codigoRuta + idSalida)` al crear salidas
- [ ] Disponibilidad de puesto antes de asignarlo (verificar que `myPasajero == null`)
- [ ] Estado de salida `PROGRAMADA` antes de vender tiquete
- [ ] Misma ruta en ambas salidas para venta ida y vuelta

---

## Iteración 2 — Capa de presentación
 
**Objetivo**: implementar todas las pantallas Swing conectadas a la instancia única de `EmpresaTransporte`. Cada clase del paquete `presentacion` recibe la misma instancia por constructor o por un método `setEmpresa()`.
 
### Patrón de conexión entre capas
 
Crear una instancia única de `EmpresaTransporte` en el punto de entrada y pasarla a cada ventana:
 
```java
// Main.java
public class Main {
    public static void main(String[] args) {
        EmpresaTransporte empresa = new EmpresaTransporte(); // carga datos base
        SwingUtilities.invokeLater(() -> new LoginFrame(empresa).setVisible(true));
    }
}
```
 
Cada frame o panel recibe `empresa` por constructor y llama sus métodos directamente. No crear una segunda instancia de `EmpresaTransporte` en ningún panel.
 
---
 
### Paleta de colores — clase `Colores.java` en el paquete `presentacion`
 
Crear una clase de constantes para usar en todos los paneles:
 
```java
package presentacion;
import java.awt.Color;
 
public class Colores {
    public static final Color AZUL_PRIMARIO    = new Color(26,  79,  138); // header, botones primarios
    public static final Color AZUL_OSCURO      = new Color(22,  63,  114); // barra de navegación
    public static final Color AZUL_MEDIO       = new Color(24,  95,  165); // texto informativo
    public static final Color AZUL_CLARO       = new Color(230, 241, 251); // fondo badges info
    public static final Color AMBAR_ACENTO     = new Color(245, 166, 35);  // indicador nav activo
    public static final Color FONDO_GENERAL    = new Color(241, 239, 232); // fondo de página
    public static final Color FONDO_TARJETA    = new Color(255, 255, 255); // fondo de cards
    public static final Color FONDO_SUPERFICIE = new Color(248, 248, 248); // encabezados de tabla
    public static final Color BORDE            = new Color(211, 209, 199); // bordes
    public static final Color TEXTO_SECUNDARIO = new Color(136, 135, 128); // labels secundarios
    public static final Color TEXTO_PRIMARIO   = new Color(28,  28,  26);  // texto principal
    public static final Color SUBTITULO_HEADER = new Color(198, 211, 226); // subtítulo en header (blanco 75%)
    // Estados semánticos
    public static final Color ESTADO_VERDE     = new Color(234, 243, 222); // disponible / activo
    public static final Color ESTADO_VERDE_TX  = new Color(59,  109, 17);  // texto sobre verde
    public static final Color ESTADO_ROJO      = new Color(252, 235, 235); // cancelado / error
    public static final Color ESTADO_ROJO_TX   = new Color(163, 45,  45);  // texto sobre rojo
    public static final Color ESTADO_AMBAR     = new Color(250, 238, 218); // advertencia / reprogramado
    public static final Color ESTADO_AMBAR_TX  = new Color(133, 79,  11);  // texto sobre ámbar
    public static final Color ESTADO_AZUL_TX   = new Color(24,  95,  165); // texto sobre azul claro
}
```
 
---
 
### Componente reutilizable — `HeaderPanel.java`
 
Panel de cabecera azul que se reutiliza en todas las ventanas:
 
**Estructura visual:**
- Fondo: `AZUL_PRIMARIO`
- Ícono de bus (unicode `\uD83D\uDE8C` o texto "BUS") a la izquierda
- Título: `"Copetran — Sistema de gestión"` en blanco, fuente 17px bold
- Subtítulo: `"Transporte Intermunicipal de Colombia"` en `SUBTITULO_HEADER`, fuente 12px plain
- Nombre del usuario activo alineado a la derecha, fuente 12px, blanco semitransparente
**Implementación sugerida:**
```java
// HeaderPanel recibe el nombre del usuario activo
public HeaderPanel(String usuario) { ... }
```
 
---
 
### Componente reutilizable — `NavPanel.java`
 
Barra de navegación que aparece debajo del header en todas las pantallas post-login:
 
- Fondo: `AZUL_OSCURO`
- Botones de navegación: "Dashboard", "Parametrización", "Ventas", "Cancelaciones", "Reportes"
- El botón activo tiene borde inferior de 3px en `AMBAR_ACENTO` y texto blanco puro
- Los botones inactivos tienen texto blanco al 70% de opacidad
- Al hacer clic en un botón, el `MainFrame` intercambia el panel central con `CardLayout`
---
 
### Pantalla 1 — `LoginFrame.java`
 
**Tamaño sugerido:** 400 × 520 px, centrado en pantalla, sin barra de título del sistema operativo (`setUndecorated(false)` — dejar decoración estándar).
 
**Fondo:** `FONDO_GENERAL`
 
**Componentes (de arriba a abajo, centrados):**
1. Panel con fondo `AZUL_PRIMARIO` (60×60px, bordes redondeados simulados con `paintComponent`) con ícono de bus centrado, fuente 28px blanca
2. `JLabel` "Copetran" — fuente 20px, bold, `TEXTO_PRIMARIO`
3. `JLabel` "Sistema de gestión de flota" — fuente 12px, `TEXTO_SECUNDARIO`
4. `JLabel` "Usuario" + `JTextField` (ancho completo)
5. `JLabel` "Contraseña" + `JPasswordField` (ancho completo)
6. `JLabel` "Perfil de acceso" + `JComboBox` con opciones: `["Administrador", "Cajero", "Supervisor"]`
7. `JButton` "Ingresar al sistema" — fondo `AZUL_PRIMARIO`, texto blanco, ancho completo
8. `JLabel` "¿Olvidaste tu contraseña?" — fuente 12px, `AZUL_MEDIO`, alineado al centro
9. Separador `JSeparator`
10. `JLabel` "v1.0.0 — Sistema de venta de pasajes" — fuente 11px, `TEXTO_SECUNDARIO`
**Lógica:**
- Al presionar "Ingresar": validar que usuario y contraseña no estén vacíos. Para esta iteración aceptar cualquier credencial no vacía.
- Si válido: crear `MainFrame(empresa, nombreUsuario)` y cerrar el `LoginFrame`.
- Si inválido: mostrar `JOptionPane.showMessageDialog` con mensaje de error.
---
 
### Pantalla 2 — `MainFrame.java`
 
Frame principal que contiene header, navegación y área de contenido con `CardLayout`.
 
**Tamaño sugerido:** 900 × 650 px, centrado en pantalla.
 
**Estructura con `BorderLayout`:**
- `NORTH`: `HeaderPanel` + `NavPanel` apilados en un `JPanel` con `BoxLayout` vertical
- `CENTER`: `JPanel` con `CardLayout` que contiene todos los paneles nombrados con las claves: `"dashboard"`, `"parametrizacion"`, `"ventas"`, `"cancelaciones"`, `"reportes"`
**Lógica de navegación:**
- `NavPanel` emite eventos al `MainFrame` para cambiar la carta visible en el `CardLayout`
- Al construir `MainFrame`, mostrar `"dashboard"` por defecto
---
 
### Pantalla 3 — `DashboardPanel.java` (RF5 — resumen)
 
**Fondo:** `FONDO_GENERAL`
 
**Sección superior — 4 tarjetas de métricas en `GridLayout(1,4)` con gap de 10px:**
 
Cada tarjeta es un `JPanel` con fondo `FONDO_TARJETA`, borde `LineBorder(BORDE, 1)` y `BorderLayout`:
- Ícono coloreado arriba (32×32, `JLabel` con fondo de color semántico)
- `JLabel` con el nombre de la métrica — fuente 11px, `TEXTO_SECUNDARIO`
- `JLabel` con el valor — fuente 20px, bold, color semántico
| Métrica | Valor fuente | Color texto |
|---|---|---|
| Tiquetes vendidos | `empresa.getCajaVenta().getTotalVendidos()` | `TEXTO_PRIMARIO` |
| Ingreso neto | `empresa.getCajaVenta().getIngresoNeto()` formateado como COP | `ESTADO_VERDE_TX` |
| Reembolsos | `empresa.getCajaVenta().getTotalReembolsos()` | `ESTADO_ROJO_TX` |
| Buses activos | contar buses con estado `"DISPONIBLE"` | `TEXTO_PRIMARIO` |
 
**Sección central — dos paneles lado a lado (`GridLayout(1,2)`):**
 
Panel izquierdo — "Ocupación por ruta":
- `JTable` no editable con columnas: Ruta, Vendidos, Capacidad, Ocupación %
- Datos: iterar `empresa.listarSalidas()` agrupando por ruta
- Encabezado de tabla con fondo `FONDO_SUPERFICIE`, texto `TEXTO_SECUNDARIO`
- Filas alternas con fondo `FONDO_TARJETA` y `FONDO_GENERAL`
Panel derecho — "Próximas salidas":
- `JList` o `JTable` con las salidas cuyo estado sea `PROGRAMADA`, ordenadas por fecha
- Cada fila muestra: hora, ruta, bus, badge de estado coloreado
**Sección inferior — "Acceso rápido" en `GridLayout(1,4)`:**
- 4 botones con ícono + texto: "Nueva venta", "Ida y vuelta", "Cancelar salida", "Ver reportes"
- Fondo `FONDO_SUPERFICIE`, borde `BORDE`, texto `TEXTO_PRIMARIO`
- Al hacer clic navegan al panel correspondiente via `CardLayout`
---
 
### Pantalla 4 — `ParametrizacionPanel.java` (RF1)
 
Panel con `JTabbedPane` de tres pestañas: **Rutas**, **Buses**, **Salidas**.
 
#### Tab "Rutas"
 
Layout: `JSplitPane` horizontal — izquierda formulario, derecha tabla.
 
**Formulario izquierdo:**
- `JLabel` + `JTextField` para: Código, Origen, Destino
- `JLabel` + `JFormattedTextField` para: Tarifa (solo números)
- Botones: `[Limpiar]` `[Guardar ruta]`
**Tabla derecha:**
- Columnas: Código, Origen → Destino, Tarifa, Acciones
- Columna Acciones: botón "Editar" por fila — al clic carga los datos en el formulario izquierdo para edición
- Al guardar con un código existente → modo edición; con código nuevo → modo creación
- Datos: `empresa.listarRutas()`
#### Tab "Buses"
 
Layout: `JSplitPane` horizontal — izquierda formulario, derecha lista de tarjetas.
 
**Formulario izquierdo:**
- `JTextField`: Placa
- `JTextField`: Marca, `JTextField`: Modelo (en `GridLayout(1,2)`)
- `JSpinner` o `JTextField`: Capacidad (número entero)
- `JComboBox`: Tipo — `["NORMAL", "EJECUTIVO"]`
- `JComboBox`: Estado — `["DISPONIBLE", "EN_RUTA", "MANTENIMIENTO"]`
- Botones: `[Limpiar]` `[Guardar bus]`
**Lista de buses derecha:**
- Iterar `empresa.listarBuses()` y generar un `JPanel` por bus con:
  - Ícono de bus en cuadro azul claro
  - Placa en bold, detalle de tipo y capacidad en gris
  - Badge de estado coloreado (verde/ámbar/gris)
  - Botón "Editar" que carga datos en formulario
- Contenido dentro de un `JScrollPane`
#### Tab "Salidas"
 
Layout: `BorderLayout` — formulario arriba, tabla abajo.
 
**Formulario superior (dos filas en `GridLayout(2,4)`):**
- `JComboBox` de rutas (mostrar `codigo — origen → destino`): poblar con `empresa.listarRutas()`
- `JComboBox` de buses disponibles (placa y capacidad): poblar con `empresa.listarBuses()`
- `JSpinner` de fecha (`SpinnerDateModel`)
- `JSpinner` de hora
- `JTextField` precio base (precargado con la tarifa de la ruta seleccionada al elegirla en el combo)
- `JLabel` informativo: "El ID se genera automáticamente"
- Botones: `[Limpiar]` `[Programar salida]`
**Filtros + tabla inferior:**
- `JDateChooser` o dos `JSpinner` de fecha (Desde / Hasta)
- `JComboBox` estado: `["Todos", "PROGRAMADA", "EN_RUTA", "COMPLETADA", "CANCELADA"]`
- Botón `[Filtrar]`
- `JTable` con columnas: ID Salida, Ruta, Fecha, Hora, Bus, Estado, Acciones
- Botón "Editar" por fila en columna Acciones
---
 
### Pantalla 5 — `VentasPanel.java` (RF2 y RF3)
 
Layout superior: dos botones tipo tab visual — "1 tiquete" y "Ida y vuelta" — que alternan entre dos subpaneles con `CardLayout`.
 
#### Subpanel "1 tiquete" (RF2)
 
Layout general: `JSplitPane` — izquierda pasos 1-3, derecha resumen.
 
**Izquierda — paso 1 "Seleccionar salida":**
- `JComboBox` Origen, `JComboBox` Destino
- `JSpinner` fecha
- `JComboBox` salida disponible (poblar filtrando por origen/destino/fecha con estado `PROGRAMADA`)
- Al cambiar la selección en el combo de salida, actualizar automáticamente el mapa de sillas
**Izquierda — paso 2 "Seleccionar silla":**
- Panel de sillas: crear un `JPanel` con `GridLayout` de filas × columnas
- Cada silla es un `JButton` de tamaño fijo (40×40px):
  - Verde (`ESTADO_VERDE`) si `puesto.myPasajero == null` → clickeable
  - Gris (`FONDO_SUPERFICIE`) si ocupado → deshabilitado
  - Azul (`AZUL_PRIMARIO`) texto blanco si es la silla actualmente seleccionada
- Al hacer clic en silla libre → marcarla como seleccionada (cambiar color) y registrar número de silla
- Leyenda debajo: tres cuadros de color con etiquetas "Disponible", "Seleccionada", "Ocupada"
**Izquierda — paso 3 "Datos del pasajero":**
- `JComboBox` tipo de documento: `["CC", "CE", "Pasaporte"]`
- `JTextField` número de documento
- `JTextField` nombre completo
- `JTextField` teléfono
**Derecha — resumen de compra:**
- `JPanel` con fondo `FONDO_SUPERFICIE` mostrando: Ruta, Salida, Bus, Silla seleccionada
- Desglose: Valor tiquete, Cargo servicio ($2.500), Total en bold 15px
- `JComboBox` forma de pago: `["Efectivo", "Tarjeta débito", "Tarjeta crédito"]`
- `JButton` "Generar tiquete" — fondo `AZUL_PRIMARIO`, texto blanco, ancho completo
- Al generar: llamar `empresa.generarTicket(...)`, actualizar `CajaVenta`, mostrar `JOptionPane` de confirmación con número de tiquete
#### Subpanel "Ida y vuelta" (RF3)
 
Layout: igual al de 1 tiquete pero con dos secciones de salida (paso 1 duplicado para "ida" y "vuelta").
 
- Selector de salida de ida y selector de salida de vuelta
- Validar que ambas salidas pertenezcan a la misma ruta (`ruta.codigo`)
- Si no coinciden: mostrar error en `JLabel` de validación en rojo
- Si coinciden: mostrar `JLabel` de validación en verde "Misma ruta validada (RUT-XXX)"
- Resumen: mostrar ambos tiquetes con descuento del 10% desglosado
- Botón "Generar 2 tiquetes" llama `empresa.ventaIdaYVuelta(...)`
---
 
### Pantalla 6 — `CancelacionesPanel.java` (RF4)
 
Layout: `BorderLayout` — buscador arriba, resultado central, acciones abajo.
 
**Buscador superior:**
- `JTextField` ID de salida
- `JComboBox` ruta (opcional)
- `JSpinner` fecha (opcional)
- `JButton` "Buscar"
**Panel de alerta (visible solo cuando se encuentra una salida):**
- `JPanel` con fondo `ESTADO_ROJO`, borde `ESTADO_ROJO_TX`
- `JLabel` en bold con el ID encontrado
- `JLabel` con advertencia sobre el cambio de estado
**Sección central — dos paneles lado a lado:**
 
Panel izquierdo "Datos de la salida":
- Tabla clave-valor con: Código, Ruta, Fecha y hora, Bus asignado, cantidad de tiquetes VIGENTES
Panel derecho "Tiquetes afectados":
- `JTable` con columnas: Tiquete, Pasajero, Silla, Estado
- Badge de color en columna Estado
**Acciones inferiores:**
- Dos `JRadioButton` en `ButtonGroup`:
  - "Reprogramar automáticamente" — descripción: busca próxima salida disponible en la misma ruta
  - "Marcar como REEMBOLSADO" — descripción: devuelve el valor al pasajero
- Botones: `[Cancelar]` `[Confirmar cancelación y generar reporte]`
- Al confirmar: llamar `empresa.cancelarSalida(idSalida)` con la acción elegida, mostrar `JOptionPane` con resumen del proceso
---
 
### Pantalla 7 — `ReportesPanel.java` (RF5)
 
Layout: `BorderLayout` — tabs arriba, filtros en card, tabla/métricas abajo.
 
**`JTabbedPane` con tres tabs:**
 
#### Tab "Ventas por ruta"
 
- Filtros: `JSpinner` Desde, `JSpinner` Hasta, `JComboBox` Ruta, `JButton` "Generar"
- 4 tarjetas de métricas en fila (igual que Dashboard): Total vendido, Reembolsado, Ingreso neto, Ocupación promedio
- `JTable` con columnas: Ruta, Tiquetes, Reembolsos, Total neto, Ocupación %
- Columna "Ocupación" con barra de progreso: usar `JProgressBar` embebido en celda o renderizarlo con un `TableCellRenderer` personalizado que pinte un rectángulo proporcional
- `JButton` "Exportar" — mostrar `JOptionPane` informando que la funcionalidad estará disponible próximamente (o implementar exportación a `.txt`)
#### Tab "Totales del día"
 
- Sin filtros — muestra siempre el acumulado actual de `CajaVenta`
- 3 tarjetas grandes: Total vendido (verde), Reembolsado (rojo), Ingreso neto (azul)
- `JLabel` con la fecha y hora del reporte generado (`LocalDateTime.now()`)
#### Tab "Ventas por mes / rango"
 
- `JSpinner` mes (1-12) + `JSpinner` año, o bien dos `JSpinner` de fecha para rango libre
- `JButton` "Generar"
- `JTable` con columnas: Fecha, Ruta, Tiquetes vendidos, Reembolsos, Total
---
 
### Tareas de la iteración 2
 
#### Componentes base
- [ ] Crear `Colores.java` con todas las constantes de color
- [ ] Crear `HeaderPanel.java` reutilizable
- [ ] Crear `NavPanel.java` con lógica de navegación por `CardLayout`
#### Ventanas principales
- [ ] Implementar `LoginFrame.java` con validación básica y apertura de `MainFrame`
- [ ] Implementar `MainFrame.java` con `CardLayout` y navegación entre paneles
#### Paneles de contenido
- [ ] Implementar `DashboardPanel.java` — métricas, tabla de ocupación, próximas salidas, accesos rápidos
- [ ] Implementar `ParametrizacionPanel.java` — tabs Rutas, Buses, Salidas con formularios y tablas
- [ ] Implementar `VentasPanel.java` — subpanel 1 tiquete (RF2) y subpanel ida y vuelta (RF3) con mapa de sillas
- [ ] Implementar `CancelacionesPanel.java` — buscador, alerta, selector de acción (RF4)
- [ ] Implementar `ReportesPanel.java` — tres tabs con filtros y tablas (RF5)
#### Integración
- [ ] Conectar todos los paneles a la instancia de `EmpresaTransporte` del `MainFrame`
- [ ] Verificar que cada acción (venta, cancelación, parametrización) actualice los datos visibles en el `DashboardPanel` al navegar de regreso
---

## Parametrización inicial obligatoria (datos base)

Al instanciar `EmpresaTransporte` se deben cargar automáticamente por código los siguientes datos base, antes de que el usuario interactúe con el sistema.

### Rutas base (mínimo 4)

| Código | Origen | Destino | Tarifa base (COP) |
| R01 | Cúcuta | Bucaramanga | 80.000 |
| R02 | Cúcuta | Bogotá | 160.000 |
| R03 | Cúcuta | Medellín | 180.000 |
| R04 | Cúcuta | Cartagena | 220.000 |

### Buses base (mínimo 6)

| Placa | Tipo | Capacidad | Estado inicial |
| KAA-101 | NORMAL | 40 | DISPONIBLE |
| KBB-202 | EJECUTIVO | 30 | DISPONIBLE |
| KCC-303 | NORMAL | 40 | DISPONIBLE |
| KDD-404 | EJECUTIVO | 30 | DISPONIBLE |
| KEE-505 | NORMAL | 40 | MANTENIMIENTO |
| KFF-606 | NORMAL | 30 | DISPONIBLE |

> Nota: `BusTipoNormal` y `BusTipoEjecutivo` difieren en capacidad según los datos base (40 o 30 puestos). El constructor de cada subtipo debe recibir la capacidad como parámetro para inicializar el arreglo de `Puesto[]` con el tamaño correcto.

### Salidas programadas base (mínimo 8)

| IdSalida | Ruta | Fecha | Hora | Bus asignado | Estado |
| S001 | R01 | 15/03/2026 | 06:00 | KAA-101 | PROGRAMADA |
| S002 | R01 | 15/03/2026 | 14:00 | KBB-202 | PROGRAMADA |
| S003 | R02 | 16/03/2026 | 07:00 | KCC-303 | PROGRAMADA |
| S004 | R02 | 16/03/2026 | 20:00 | KDD-404 | PROGRAMADA |
| S005 | R03 | 17/03/2026 | 05:30 | KFF-606 | PROGRAMADA |
| S006 | R03 | 17/03/2026 | 18:00 | KAA-101 | PROGRAMADA |
| S007 | R04 | 18/03/2026 | 06:30 | KCC-303 | PROGRAMADA |
| S008 | R04 | 18/03/2026 | 19:30 | KBB-202 | PROGRAMADA |

### Implementación sugerida

Crear un método privado `cargarDatosBase()` dentro de `EmpresaTransporte` e invocarlo desde el constructor:

```java
public EmpresaTransporte() {
    this.myBuses    = new ArrayList<>();
    this.myRutas    = new ArrayList<>();
    this.mySalidas  = new ArrayList<>();
    this.myTickets  = new ArrayList<>();
    this.myEmpleado = new ArrayList<>();
    this.myCaja     = new CajaVenta();
    cargarDatosBase();
}

private void cargarDatosBase() {
    // Rutas
    myRutas.add(new Ruta("R01", "Cúcuta", "Bucaramanga", 80000f));
    myRutas.add(new Ruta("R02", "Cúcuta", "Bogotá",      160000f));
    myRutas.add(new Ruta("R03", "Cúcuta", "Medellín",    180000f));
    myRutas.add(new Ruta("R04", "Cúcuta", "Cartagena",   220000f));

    // Buses — el constructor recibe (placa, estado, capacidad)
    myBuses.add(new BusTipoNormal    ("KAA-101", "DISPONIBLE",     40));
    myBuses.add(new BusTipoEjecutivo ("KBB-202", "DISPONIBLE",     30));
    myBuses.add(new BusTipoNormal    ("KCC-303", "DISPONIBLE",     40));
    myBuses.add(new BusTipoEjecutivo ("KDD-404", "DISPONIBLE",     30));
    myBuses.add(new BusTipoNormal    ("KEE-505", "MANTENIMIENTO",  40));
    myBuses.add(new BusTipoNormal    ("KFF-606", "DISPONIBLE",     30));

    // Salidas — resolver ruta y bus por código/placa con métodos auxiliares
    mySalidas.add(new Salida("S001", getRutaPorCodigo("R01"), parseFecha("15/03/2026 06:00"), getBusPorPlaca("KAA-101"), "PROGRAMADA"));
    mySalidas.add(new Salida("S002", getRutaPorCodigo("R01"), parseFecha("15/03/2026 14:00"), getBusPorPlaca("KBB-202"), "PROGRAMADA"));
    mySalidas.add(new Salida("S003", getRutaPorCodigo("R02"), parseFecha("16/03/2026 07:00"), getBusPorPlaca("KCC-303"), "PROGRAMADA"));
    mySalidas.add(new Salida("S004", getRutaPorCodigo("R02"), parseFecha("16/03/2026 20:00"), getBusPorPlaca("KDD-404"), "PROGRAMADA"));
    mySalidas.add(new Salida("S005", getRutaPorCodigo("R03"), parseFecha("17/03/2026 05:30"), getBusPorPlaca("KFF-606"), "PROGRAMADA"));
    mySalidas.add(new Salida("S006", getRutaPorCodigo("R03"), parseFecha("17/03/2026 18:00"), getBusPorPlaca("KAA-101"), "PROGRAMADA"));
    mySalidas.add(new Salida("S007", getRutaPorCodigo("R04"), parseFecha("18/03/2026 06:30"), getBusPorPlaca("KCC-303"), "PROGRAMADA"));
    mySalidas.add(new Salida("S008", getRutaPorCodigo("R04"), parseFecha("18/03/2026 19:30"), getBusPorPlaca("KBB-202"), "PROGRAMADA"));
}

// Helpers auxiliares dentro de EmpresaTransporte
private Ruta getRutaPorCodigo(String codigo) {
    for (Ruta r : myRutas) if (r.getCodigo().equals(codigo)) return r;
    return null;
}

private Bus getBusPorPlaca(String placa) {
    for (Bus b : myBuses) if (b.getPlaca().equals(placa)) return b;
    return null;
}

// parseFecha usando LocalDateTime (Java 8+)
private java.time.LocalDateTime parseFecha(String fechaHora) {
    return java.time.LocalDateTime.parse(fechaHora,
        java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
}
```

---

## Notas para el agente

- Usar **arreglos de tamaño fijo** para `myBuses`, y para `myRutas`, `myTickets` y `myEmpleado` puede ser usando arreglos dinamicos, con un contador paralelo para saber cuántos elementos hay activos. Se permite el uso de Colecciones List, Set y Map (como se considere) 
- `CajaVenta` se instancia una sola vez dentro de `EmpresaTransporte` y se actualiza en cada transacción.
- Los estados de `Salida` y `PasajeTicket` deben manejarse como `String` con valores constantes definidos en la misma clase (ej. `public static final String PROGRAMADA = "PROGRAMADA"`).
- El ID de salida se genera automáticamente concatenando `"SAL-" + año + "-" + secuencial`.
- Comenzar la implementación en el orden de las tareas listadas en la Iteración 1 (de menor a mayor dependencia).
