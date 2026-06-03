# Plan de desarrollo — Sistema de venta de pasajes Copetran (v2)

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
│   ├── TipoPasajero.java         ← NUEVO (enum: PREFERENCIAL, NO_FRECUENTE)
│   ├── Persona.java
│   ├── Empleado.java
│   ├── Pasajero.java             ← MODIFICADO (tipo, contador de pasajes, descuento)
│   ├── Conductor.java            ← NUEVO (extends Persona)
│   ├── Bus.java
│   ├── BusTipoNormal.java
│   ├── BusTipoEjecutivo.java
│   ├── Puesto.java
│   ├── Ruta.java
│   ├── Salida.java               ← MODIFICADO (mínimo 5 pasajes para ser efectiva)
│   ├── PasajeTicket.java         ← MODIFICADO (atributo idaYVuelta: boolean)
│   ├── CajaVenta.java            ← MODIFICADO (montoCaja, totalVendido, totalReembolsado, ingresoNeto)
│   └── EmpresaTransporte.java    ← Clase contenedora del problema
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

### Enumeraciones
- **TipoEmpleado**: `CAJERO`, `ADMINISTRADOR`
- **TipoPasajero** *(NUEVO)*: `PREFERENCIAL`, `NO_FRECUENTE`
  - Un pasajero es `PREFERENCIAL` si ha comprado **mínimo 5 pasajes** con la empresa. Recibe un descuento especial.

### Jerarquía de personas

- **Persona** *(abstracta)*: `cedula: String`, `nombre: String`, `direccion: String`
  - **Empleado** extends Persona: `idEmpleado: String`, `rol: TipoEmpleado`
  - **Pasajero** *(MODIFICADO)* extends Persona:
    - `idPasajero: String`
    - `correo: String`
    - `telefono: String`
    - `tipo: TipoPasajero`  ← calculado automáticamente según historial
    - `totalPasajesComprados: int`  ← se incrementa con cada compra; al llegar a 5 cambia tipo a PREFERENCIAL
  - **Conductor** *(NUEVO)* extends Persona:
    - `cedula: String` (heredado)
    - `nombre: String` (heredado)
    - `correo: String`
    - `telefono: String`
    - `sueldo: float`

### Flota
- **Bus** *(abstracta)*: `placa: String`, `estado: String`
  - **BusTipoNormal** extends Bus: `myPuestos[]: Puesto[40]`
  - **BusTipoEjecutivo** extends Bus: `myPuestos[]: Puesto[30]`
- **Puesto**: `fila: char`, `numero: int`, `myPasajero: Pasajero`

### Rutas y salidas
- **Ruta**: `codigo: String`, `origen: String` *(siempre "Cúcuta")*, `destino: String`, `tarifa: float`
- **Salida** *(MODIFICADO)*:
  - `idSalida: String`, `myRuta: Ruta`, `fecha: LocalDateTime`, `myBus: Bus`, `estado: String`
  - `totalPasajesVendidos(): int`  ← método auxiliar para verificar mínimo de 5 pasajes
  - Estados: `PROGRAMADA`, `EN_RUTA`, `FINALIZADA`, `CANCELADA`
  - **Regla:** Para que una salida pueda ser **efectiva** debe tener como mínimo **5 pasajes vendidos**.
  - **Regla:** El estado `FINALIZADA` solo puede asignarse si han transcurrido **mínimo 2 días** desde la fecha actual del sistema hacia la fecha de la salida.

### Tickets y caja
- **PasajeTicket** *(MODIFICADO)*:
  - `mySalida: Salida`
  - `puesto: int`
  - `valorPagar: float`
  - `estado: String`  (estados: `VIGENTE`, `REEMBOLSADO`)
  - `myPasajero: Pasajero`
  - `idaYVuelta: boolean`  ← NUEVO; si es `true`, el cobro aplica doble (con las reglas de descuento vigentes)

- **CajaVenta** *(MODIFICADO)*:
  - `montoCaja: float`  ← dinero total en caja
  - `totalVendido: float`  ← suma de todos los valores vendidos
  - `totalReembolsado: float`  ← suma de todos los reembolsos
  - `ingresoNeto: float`  ← calculado: `totalVendido - totalReembolsado`

### Contenedor principal
- **EmpresaTransporte**: agrega `Bus[]`, `Ruta[]`, `PasajeTicket[]`, `Caja: CajaVenta`, `Empleado[]`, `Conductor[]` *(NUEVO)*, `Pasajero[]`

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
- Crear una ruta con: `codigo`, `origen` (siempre "Cúcuta"), `destino`, `tarifa`
- Validar unicidad por `codigoRuta`
- Listar rutas: `listarRutas(): Ruta[]`
- Editar y eliminar ruta por código

**Salidas:**
- Crear una salida programada asignando: ruta, bus, fecha, hora
- Validar unicidad por combinación `(placa + codigoRuta + idSalida)`
- Listar salidas: `listarSalidas(): Salida[]`
- Editar y cancelar salida por ID
- **Finalizar salida** (JButton "Estado Finalizado Salida"): cambiar estado a `FINALIZADA` solo si la fecha de la salida es **mínimo 2 días anterior** a la fecha actual del sistema. Si no cumple la condición, mostrar error.

**Conductores** *(NUEVO)*:
- Crear un conductor recibiendo: `cedula`, `nombre`, `correo`, `telefono`, `sueldo`
- Listar conductores: `listarConductores(): Conductor[]`
- Editar y eliminar conductor por cédula

---

### RF2 — Venta de pasaje (1 o más tiquetes en puestos seguidos)

Flujo completo de venta:

1. Seleccionar una salida disponible (estado `PROGRAMADA`) — origen siempre **Cúcuta**
2. Mostrar puestos disponibles del bus asignado a esa salida
3. Ingresar **cantidad de puestos** a comprar (deben ser puestos **seguidos/consecutivos**)
4. El sistema valida que existan suficientes puestos consecutivos libres
5. Solicitar la **cédula de cada pasajero** que va a viajar en cada puesto:
   - Si una sola persona ocupa dos puestos seguidos, se acepta la misma cédula dos veces
6. Para cada cédula, buscar si el pasajero ya existe en el sistema o registrarlo con: `cedula`, `nombre`, `correo`, `telefono`
7. Calcular valor a pagar por puesto (tarifa de la ruta); aplicar descuento si el pasajero es `PREFERENCIAL`
8. Generar un `PasajeTicket` por cada puesto con estado `VIGENTE`
9. Incrementar `totalPasajesComprados` de cada pasajero; actualizar `tipo` a `PREFERENCIAL` si llega a 5
10. Actualizar `CajaVenta`: incrementar `totalVendido` y `montoCaja`

> **Nota:** Copetran solo expide tiquetes desde **Cúcuta** hacia otra ciudad. El origen siempre es Cúcuta.

Métodos principales en `EmpresaTransporte`:
```
registrarPasajeroYSilla(cedulas: String[], puestos: int[]): void
generarTickets(cantidad: int): PasajeTicket[]
buscarOCrearPasajero(cedula: String): Pasajero
verificarPuestosConsecutivos(salida: Salida, cantidad: int): int[]
```

---

### RF3 — Venta ida y vuelta (cobro doble con descuento)

Extensión del RF2 para viaje de regreso. El tiquete de ida y vuelta se representa con el atributo `idaYVuelta = true` en `PasajeTicket`:

1. Ejecutar venta de salida de ida (igual que RF2)
2. Seleccionar salida de regreso en la **misma ruta** (validar que `ruta.codigo` coincida)
3. Validar disponibilidad de puestos consecutivos en la salida de regreso
4. Solicitar cédulas de los pasajeros para los puestos de regreso (igual que RF2)
5. Aplicar **descuento del 10%** sobre el valor total (ida + vuelta)
6. Generar los `PasajeTicket` con `idaYVuelta = true` para ambas direcciones
7. Actualizar `CajaVenta` con el total descontado

Método en `EmpresaTransporte`:
```
ventaIdaYVuelta(): Boolean
```

---

### RF4 — Cancelación de salida

Gestión de cancelación con reprogramación automática o reembolso:

1. Cambiar estado de la `Salida` a `CANCELADA`
2. Buscar todos los `PasajeTicket` con estado `VIGENTE` asociados a esa salida
3. Para cada ticket vigente, aplicar la acción seleccionada:

   **a) Reprogramar automáticamente:**
   - Buscar la **salida más próxima** en la misma ruta con estado `PROGRAMADA` y cupos disponibles
   - La búsqueda prioriza primero el **mismo día** en una hora posterior, luego **máximo un día después** de la fecha de la salida cancelada
   - Si se encuentra una salida próxima dentro del rango: reasignar el ticket a esa salida
   - Si **no** se encuentra ninguna salida válida en ese rango: proceder automáticamente con la devolución (reembolso)

   **b) Reembolsar:**
   - Cambiar estado del ticket a `REEMBOLSADO`
   - Descontar el valor del ticket de `montoCaja`
   - Incrementar `totalReembolsado` en `CajaVenta`

4. Generar reporte del proceso de cancelación

Método en `EmpresaTransporte`:
```
cancelarSalida(idSalida: String): void
buscarSalidaProxima(salida: Salida): Salida   ← busca mismo día hora posterior o hasta 1 día después
```

---

### RF5 — Finalización de salida

Cambio de estado de una salida a `FINALIZADA`:

1. Verificar que la salida exista y esté en estado `PROGRAMADA` o `EN_RUTA`
2. Verificar que la fecha de la salida sea **mínimo 2 días antes** de la fecha actual del sistema (`LocalDate.now().minusDays(2)`)
3. Si se cumple la condición: cambiar estado a `FINALIZADA`
4. Si no se cumple: mostrar mensaje de error indicando que aún no es posible finalizar la salida

> Este RF se activa desde un `JButton` "Estado Finalizado Salida" en el panel de Parametrización → pestaña Salidas.

Método en `EmpresaTransporte`:
```
finalizarSalida(idSalida: String): boolean
```

---

### RF6 — Validación de salida efectiva

Una salida programada solo puede ejecutarse (ser efectiva) si cumple:

- Tiene **mínimo 5 pasajes vendidos** (tiquetes con estado `VIGENTE` asociados)

Esta validación se aplica al intentar cambiar el estado de la salida de `PROGRAMADA` a `EN_RUTA`.

Método en `EmpresaTransporte` / `Salida`:
```
esSalidaEfectiva(idSalida: String): boolean   ← retorna true si hay >= 5 tiquetes vigentes
```

---

### RF7 — Reportes del día (Caja)

Generación de reportes operativos desde `CajaVenta`. La caja del día muestra:

- **Monto en caja**: dinero físico acumulado (`montoCaja`)
- **Total vendido**: suma de todos los valores de tiquetes generados (`totalVendido`)
- **Total reembolsado**: suma de todos los valores devueltos (`totalReembolsado`)
- **Ingreso neto**: `totalVendido - totalReembolsado`

Reportes adicionales:
- **(a) Ventas por ruta**: cantidad de tiquetes vendidos agrupados por `ruta.codigo`
- **(b) Totales del día**: `totalVendido`, `totalReembolsado`, `ingresoNeto`
- **(c) Ventas por mes o rango de fechas**: filtrar tickets por `salida.fecha` en un rango dado

Métodos en `CajaVenta`:
```
getMontoCaja(): float
getTotalVendido(): float
getTotalReembolsado(): float
getIngresoNeto(): float        ← calculado: totalVendido - totalReembolsado
reportesDelDia(): CajaVenta
```

---

## Iteración 1 — Capa de negocio (alcance actual)

**Objetivo**: implementar completamente el paquete `negocio` con todos los modelos y la lógica de los RF1 al RF7.

### Tareas

#### 1. Enumeraciones y jerarquía base
- [ ] Crear `TipoEmpleado.java` (enum: `CAJERO`, `ADMINISTRADOR`)
- [ ] Crear `TipoPasajero.java` *(NUEVO)* (enum: `PREFERENCIAL`, `NO_FRECUENTE`)
- [ ] Crear `Persona.java` (clase abstracta con `cedula`, `nombre`, `direccion`)
- [ ] Crear `Empleado.java` extends `Persona` con `idEmpleado`, `rol: TipoEmpleado`
- [ ] Crear `Pasajero.java` *(MODIFICADO)* extends `Persona`:
  - Atributos: `idPasajero`, `correo`, `telefono`, `tipo: TipoPasajero`, `totalPasajesComprados: int`
  - Método: `incrementarPasajes()` — suma 1 a `totalPasajesComprados`; si >= 5, cambia `tipo` a `PREFERENCIAL`
  - Método: `esPreferencial(): boolean`
- [ ] Crear `Conductor.java` *(NUEVO)* extends `Persona`:
  - Atributos: `correo`, `telefono`, `sueldo: float`

#### 2. Flota
- [ ] Crear `Puesto.java` con `fila: char`, `numero: int`, `myPasajero: Pasajero` (null = libre)
- [ ] Crear `Bus.java` (abstracta) con `placa`, `estado`, arreglo `myPuestos[]`
- [ ] Crear `BusTipoNormal.java` extends `Bus` — inicializa 40 puestos, implementa `mostrarPuestosLibres(): int[]`
- [ ] Crear `BusTipoEjecutivo.java` extends `Bus` — inicializa 30 puestos, implementa `mostrarPuestosLibres(): int[]`

#### 3. Rutas y salidas
- [ ] Crear `Ruta.java` con `codigo`, `origen` (siempre "Cúcuta"), `destino`, `tarifa`
- [ ] Crear `Salida.java` *(MODIFICADO)*:
  - Atributos: `idSalida`, `myRuta`, `fecha: LocalDateTime`, `myBus`, `estado`
  - Estados constantes: `PROGRAMADA`, `EN_RUTA`, `FINALIZADA`, `CANCELADA`
  - Método: `totalPasajesVendidos(tickets: List<PasajeTicket>): int`
  - Método: `esSalidaEfectiva(tickets: List<PasajeTicket>): boolean` (>= 5 vigentes)
  - Método: `puedeFinalizarse(): boolean` (fecha salida <= hoy - 2 días)

#### 4. Tickets y caja
- [ ] Crear `PasajeTicket.java` *(MODIFICADO)*:
  - Atributos: `mySalida`, `puesto: int`, `valorPagar`, `estado`, `myPasajero`, `idaYVuelta: boolean`
  - Estados constantes: `VIGENTE`, `REEMBOLSADO`
- [ ] Crear `CajaVenta.java` *(MODIFICADO)*:
  - Atributos: `montoCaja: float`, `totalVendido: float`, `totalReembolsado: float`
  - Método: `getIngresoNeto(): float` → retorna `totalVendido - totalReembolsado`
  - Métodos de reporte del RF7

#### 5. Contenedor principal — `EmpresaTransporte.java`
- [ ] Atributos: `myBuses[]`, `myRutas[]`, `myTickets[]`, `myCaja: CajaVenta`, `myEmpleado[]`, `myConductores[]` *(NUEVO)*, `myPasajeros[]` *(NUEVO — para buscar por cédula)*
- [ ] Implementar RF1: `crearBus()`, `listarBuses()`, `editarBus()`, `eliminarBus()`, `crearRuta()`, `listarRutas()`, `editarRuta()`, `eliminarRuta()`, `crearSalida()`, `listarSalidas()`, `editarSalida()`, `cancelarSalida()`
- [ ] Implementar RF1 (conductores): `crearConductor()`, `listarConductores()`, `editarConductor()`, `eliminarConductor()`
- [ ] Implementar RF2: `buscarOCrearPasajero(cedula)`, `verificarPuestosConsecutivos()`, `registrarPasajerosYSillas()`, `generarTickets()`
- [ ] Implementar RF3: `ventaIdaYVuelta()` con validación de misma ruta, descuento del 10% y `idaYVuelta = true`
- [ ] Implementar RF4: `cancelarSalida()` con `buscarSalidaProxima()` (mismo día hora posterior o máximo 1 día después); si no hay cupo → reembolso automático
- [ ] Implementar RF5: `finalizarSalida(idSalida)` — validar mínimo 2 días de antigüedad
- [ ] Implementar RF6: `esSalidaEfectiva(idSalida)` — verificar >= 5 pasajes vigentes
- [ ] Implementar RF7: delegar en `CajaVenta` los métodos de reporte

#### 6. Validaciones requeridas
- [ ] Unicidad de `placa` al crear buses
- [ ] Unicidad de `codigoRuta` al crear rutas
- [ ] Unicidad de `(placa + codigoRuta + idSalida)` al crear salidas
- [ ] Origen de ruta siempre debe ser **"Cúcuta"**
- [ ] Disponibilidad de puesto antes de asignarlo (`myPasajero == null`)
- [ ] Validar que los puestos seleccionados sean **consecutivos**
- [ ] Permitir la misma cédula en puestos seguidos (una persona puede ocupar más de un puesto)
- [ ] Estado de salida `PROGRAMADA` antes de vender tiquete
- [ ] Misma ruta en ambas salidas para venta ida y vuelta
- [ ] Mínimo 5 pasajes vendidos para activar una salida (`EN_RUTA`)
- [ ] Mínimo 2 días de antigüedad para finalizar una salida

---

## Iteración 2 — Capa de presentación

**Objetivo**: implementar todas las pantallas Swing conectadas a la instancia única de `EmpresaTransporte`.

### Patrón de conexión entre capas

```java
// Main.java
public class Main {
    public static void main(String[] args) {
        EmpresaTransporte empresa = new EmpresaTransporte();
        SwingUtilities.invokeLater(() -> new LoginFrame(empresa).setVisible(true));
    }
}
```

---

### Paleta de colores — clase `Colores.java`

```java
package presentacion;
import java.awt.Color;

public class Colores {
    public static final Color AZUL_PRIMARIO    = new Color(26,  79,  138);
    public static final Color AZUL_OSCURO      = new Color(22,  63,  114);
    public static final Color AZUL_MEDIO       = new Color(24,  95,  165);
    public static final Color AZUL_CLARO       = new Color(230, 241, 251);
    public static final Color AMBAR_ACENTO     = new Color(245, 166, 35);
    public static final Color FONDO_GENERAL    = new Color(241, 239, 232);
    public static final Color FONDO_TARJETA    = new Color(255, 255, 255);
    public static final Color FONDO_SUPERFICIE = new Color(248, 248, 248);
    public static final Color BORDE            = new Color(211, 209, 199);
    public static final Color TEXTO_SECUNDARIO = new Color(136, 135, 128);
    public static final Color TEXTO_PRIMARIO   = new Color(28,  28,  26);
    public static final Color SUBTITULO_HEADER = new Color(198, 211, 226);
    public static final Color ESTADO_VERDE     = new Color(234, 243, 222);
    public static final Color ESTADO_VERDE_TX  = new Color(59,  109, 17);
    public static final Color ESTADO_ROJO      = new Color(252, 235, 235);
    public static final Color ESTADO_ROJO_TX   = new Color(163, 45,  45);
    public static final Color ESTADO_AMBAR     = new Color(250, 238, 218);
    public static final Color ESTADO_AMBAR_TX  = new Color(133, 79,  11);
    public static final Color ESTADO_AZUL_TX   = new Color(24,  95,  165);
}
```

---

### Componente reutilizable — `HeaderPanel.java`

Panel de cabecera azul reutilizable:
- Fondo: `AZUL_PRIMARIO`
- Ícono de bus (`\uD83D\uDE8C`), título "Copetran — Sistema de gestión" (bold 17px, blanco), subtítulo (12px, `SUBTITULO_HEADER`)
- Nombre del usuario activo alineado a la derecha

```java
public HeaderPanel(String usuario) { ... }
```

---

### Componente reutilizable — `NavPanel.java`

Barra de navegación debajo del header:
- Fondo: `AZUL_OSCURO`
- Botones: "Dashboard", "Parametrización", "Ventas", "Cancelaciones", "Reportes"
- Botón activo: borde inferior 3px `AMBAR_ACENTO`, texto blanco puro
- Al hacer clic, intercambia panel central con `CardLayout`

---

### Pantalla 1 — `LoginFrame.java`

400 × 520 px, centrado en pantalla.

Componentes:
1. Panel ícono bus (60×60px, fondo `AZUL_PRIMARIO`)
2. `JLabel` "Copetran" — 20px bold
3. `JLabel` subtítulo — 12px `TEXTO_SECUNDARIO`
4. `JTextField` usuario
5. `JPasswordField` contraseña
6. `JComboBox` perfil: `["Administrador", "Cajero", "Supervisor"]`
7. `JButton` "Ingresar al sistema" — `AZUL_PRIMARIO`, blanco, ancho completo
8. `JLabel` "¿Olvidaste tu contraseña?" — 12px `AZUL_MEDIO`
9. `JSeparator`
10. `JLabel` versión — 11px `TEXTO_SECUNDARIO`

Lógica: cualquier credencial no vacía abre `MainFrame`; si vacía, `JOptionPane` error.

---

### Pantalla 2 — `MainFrame.java`

900 × 650 px. `BorderLayout`:
- `NORTH`: `HeaderPanel` + `NavPanel`
- `CENTER`: `JPanel` con `CardLayout`: `"dashboard"`, `"parametrizacion"`, `"ventas"`, `"cancelaciones"`, `"reportes"`

---

### Pantalla 3 — `DashboardPanel.java`

**4 tarjetas métricas (GridLayout 1×4):**

| Métrica | Fuente | Color |
|---|---|---|
| Tiquetes vendidos | `caja.getTotalVendidos()` | `TEXTO_PRIMARIO` |
| Ingreso neto | `caja.getIngresoNeto()` (COP) | `ESTADO_VERDE_TX` |
| Reembolsos | `caja.getTotalReembolsado()` | `ESTADO_ROJO_TX` |
| Buses activos | buses con estado `DISPONIBLE` | `TEXTO_PRIMARIO` |

**Sección central (GridLayout 1×2):**
- Panel izq. "Ocupación por ruta": `JTable` con Ruta, Vendidos, Capacidad, Ocupación %
- Panel der. "Próximas salidas": salidas `PROGRAMADA` ordenadas por fecha, con badge de estado

**Acceso rápido (GridLayout 1×4):**
- Botones: "Nueva venta", "Ida y vuelta", "Cancelar salida", "Ver reportes"

---

### Pantalla 4 — `ParametrizacionPanel.java` (RF1)

`JTabbedPane` con cuatro pestañas: **Rutas**, **Buses**, **Salidas**, **Conductores** *(NUEVA pestaña)*.

#### Tab "Rutas"
`JSplitPane` izq/der:
- Formulario: Código, Origen (fijo "Cúcuta", no editable), Destino, Tarifa
- Tabla: Código, Origen → Destino, Tarifa, Acciones (Editar)

#### Tab "Buses"
`JSplitPane` izq/der:
- Formulario: Placa, Tipo (`NORMAL`/`EJECUTIVO`), Capacidad, Estado
- Lista de tarjetas: placa, tipo, badge estado, botón Editar

#### Tab "Salidas"
`BorderLayout`: formulario superior, tabla inferior.
- Formulario: JComboBox ruta, JComboBox bus, JSpinner fecha, JSpinner hora, precio base
- **JButton "Estado Finalizado Salida"** *(NUEVO)*: al hacer clic llama `empresa.finalizarSalida(idSalida)`:
  - Si exitoso: badge verde "FINALIZADA"
  - Si falla (no cumple 2 días): `JOptionPane` con mensaje de error explicando la condición
- Tabla: ID, Ruta, Fecha, Hora, Bus, Estado, Pasajes vendidos, Acciones

#### Tab "Conductores" *(NUEVA)*
`JSplitPane` izq/der:
- Formulario izquierdo:
  - `JTextField` Cédula
  - `JTextField` Nombre
  - `JTextField` Correo
  - `JTextField` Teléfono
  - `JFormattedTextField` Sueldo (solo números)
  - Botones: `[Limpiar]` `[Guardar conductor]`
- Tabla derecha: Cédula, Nombre, Correo, Teléfono, Sueldo, Acciones (Editar)

---

### Pantalla 5 — `VentasPanel.java` (RF2 y RF3)

Layout superior: dos tabs visuales — "1 o más tiquetes" y "Ida y vuelta" — con `CardLayout`.

#### Subpanel "1 o más tiquetes" (RF2)

`JSplitPane` izq/der:

**Izquierda — paso 1 "Seleccionar salida":**
- `JComboBox` Destino (Origen fijo: Cúcuta)
- `JSpinner` fecha
- `JComboBox` salida disponible (estado `PROGRAMADA`)

**Izquierda — paso 2 "Seleccionar cantidad y puestos":** *(MODIFICADO)*
- `JSpinner` "Cantidad de puestos" (mínimo 1)
- Mapa de sillas: `JPanel` con `GridLayout` — `JButton` por puesto (40×40px):
  - Verde (`ESTADO_VERDE`): libre y clickeable
  - Gris (`FONDO_SUPERFICIE`): ocupado, deshabilitado
  - Azul (`AZUL_PRIMARIO`): seleccionado actualmente
- Validación visual: resalta en ámbar los puestos que formarían el bloque consecutivo
- Leyenda: Disponible / Seleccionada / Ocupada

**Izquierda — paso 3 "Datos de los pasajeros":** *(MODIFICADO)*
- Panel dinámico: genera un sub-formulario por cada puesto seleccionado
- Cada sub-formulario: `JTextField` Cédula (al perder foco, busca pasajero existente y autocompleta)
- Si el pasajero existe: mostrar nombre y badge "PREFERENCIAL" o "CLIENTE" según tipo
- Si no existe: mostrar campos: Nombre, Correo, Teléfono para registro

**Derecha — resumen de compra:**
- Ruta, Salida, Bus, Puestos seleccionados (lista)
- Por pasajero: nombre, puesto, valor base, descuento si es preferencial
- Total general en bold 15px
- `JComboBox` forma de pago: `["Efectivo", "Tarjeta débito", "Tarjeta crédito"]`
- `JButton` "Generar tiquetes" → llama `empresa.generarTickets(...)`

#### Subpanel "Ida y vuelta" (RF3)

Igual al anterior pero con dos secciones de salida (ida y regreso):
- Selector de salida de ida y selector de salida de vuelta
- Validar misma ruta; si no coincide: error en rojo; si coincide: verde "Misma ruta validada"
- Resumen muestra ambas tandas de tiquetes con descuento del 10% desglosado
- `JButton` "Generar tiquetes ida y vuelta" → llama `empresa.ventaIdaYVuelta(...)`

---

### Pantalla 6 — `CancelacionesPanel.java` (RF4)

`BorderLayout`: buscador arriba, resultado central, acciones abajo.

**Buscador superior:**
- `JTextField` ID de salida, `JComboBox` ruta (opcional), `JSpinner` fecha (opcional), `JButton` "Buscar"

**Panel de alerta:**
- `JPanel` fondo `ESTADO_ROJO`, `JLabel` ID en bold, advertencia sobre cambio de estado

**Sección central (dos paneles):**
- Panel izq. "Datos de la salida": tabla clave-valor con código, ruta, fecha/hora, bus, tiquetes vigentes
- Panel der. "Tiquetes afectados": `JTable` con Tiquete, Pasajero, Silla, Estado

**Acciones inferiores:**
- `JRadioButton` "Reprogramar automáticamente" — descripción: busca próxima salida en el mismo día hora posterior o máximo 1 día después; si no hay cupo, se reembolsa automáticamente *(MODIFICADO)*
- `JRadioButton` "Marcar como REEMBOLSADO" — devuelve el valor al pasajero
- Botones: `[Cancelar]` `[Confirmar cancelación y generar reporte]`
- Al confirmar: `empresa.cancelarSalida(idSalida)`, mostrar `JOptionPane` con resumen del proceso y detalle de tiquetes reprogramados vs. reembolsados

---

### Pantalla 7 — `ReportesPanel.java` (RF7)

`JTabbedPane` con tres tabs:

#### Tab "Ventas por ruta"
- Filtros: `JSpinner` Desde / Hasta, `JComboBox` Ruta, `JButton` "Generar"
- 4 tarjetas de métricas: Total vendido, Reembolsado, Ingreso neto, Ocupación promedio
- `JTable`: Ruta, Tiquetes, Reembolsos, Total neto, Ocupación % (con barra de progreso)
- `JButton` "Exportar" → `JOptionPane` o exportar a `.txt`

#### Tab "Totales del día" *(MODIFICADO)*
- Sin filtros — acumulado actual de `CajaVenta`
- 4 tarjetas: **Monto en caja** (azul), **Total vendido** (verde), **Reembolsado** (rojo), **Ingreso neto** (verde oscuro)
- `JLabel` con fecha y hora del reporte (`LocalDateTime.now()`)

#### Tab "Ventas por mes / rango"
- `JSpinner` mes (1-12) + `JSpinner` año, o rango libre
- `JButton` "Generar"
- `JTable`: Fecha, Ruta, Tiquetes vendidos, Reembolsos, Total

---

### Tareas de la iteración 2

#### Componentes base
- [ ] Crear `Colores.java` con todas las constantes de color
- [ ] Crear `HeaderPanel.java` reutilizable
- [ ] Crear `NavPanel.java` con lógica de navegación por `CardLayout`

#### Ventanas principales
- [ ] Implementar `LoginFrame.java`
- [ ] Implementar `MainFrame.java` con `CardLayout`

#### Paneles de contenido
- [ ] Implementar `DashboardPanel.java`
- [ ] Implementar `ParametrizacionPanel.java` — tabs Rutas, Buses, Salidas (con botón Finalizar), Conductores
- [ ] Implementar `VentasPanel.java` — puestos consecutivos, cédulas por pasajero, RF2 y RF3
- [ ] Implementar `CancelacionesPanel.java` — lógica de próxima salida (mismo día/día siguiente) con fallback a reembolso
- [ ] Implementar `ReportesPanel.java` — tres tabs con métricas de caja del día

#### Integración
- [ ] Conectar todos los paneles a la instancia de `EmpresaTransporte`
- [ ] Verificar que venta, cancelación y parametrización actualicen el `DashboardPanel` al navegar

---

## Parametrización inicial obligatoria (datos base)

Al instanciar `EmpresaTransporte` se deben cargar automáticamente los siguientes datos base.

### Rutas base (mínimo 4)

| Código | Origen | Destino | Tarifa base (COP) |
|---|---|---|---|
| R01 | Cúcuta | Bucaramanga | 80.000 |
| R02 | Cúcuta | Bogotá | 160.000 |
| R03 | Cúcuta | Medellín | 180.000 |
| R04 | Cúcuta | Cartagena | 220.000 |

### Buses base (mínimo 6)

| Placa | Tipo | Capacidad | Estado inicial |
|---|---|---|---|
| KAA-101 | NORMAL | 40 | DISPONIBLE |
| KBB-202 | EJECUTIVO | 30 | DISPONIBLE |
| KCC-303 | NORMAL | 40 | DISPONIBLE |
| KDD-404 | EJECUTIVO | 30 | DISPONIBLE |
| KEE-505 | NORMAL | 40 | MANTENIMIENTO |
| KFF-606 | NORMAL | 30 | DISPONIBLE |

### Salidas programadas base (mínimo 8)

| IdSalida | Ruta | Fecha | Hora | Bus | Estado |
|---|---|---|---|---|---|
| S001 | R01 | 15/03/2026 | 06:00 | KAA-101 | PROGRAMADA |
| S002 | R01 | 15/03/2026 | 14:00 | KBB-202 | PROGRAMADA |
| S003 | R02 | 16/03/2026 | 07:00 | KCC-303 | PROGRAMADA |
| S004 | R02 | 16/03/2026 | 20:00 | KDD-404 | PROGRAMADA |
| S005 | R03 | 17/03/2026 | 05:30 | KFF-606 | PROGRAMADA |
| S006 | R03 | 17/03/2026 | 18:00 | KAA-101 | PROGRAMADA |
| S007 | R04 | 18/03/2026 | 06:30 | KCC-303 | PROGRAMADA |
| S008 | R04 | 18/03/2026 | 19:30 | KBB-202 | PROGRAMADA |

### Implementación sugerida

```java
public EmpresaTransporte() {
    this.myBuses      = new ArrayList<>();
    this.myRutas      = new ArrayList<>();
    this.mySalidas    = new ArrayList<>();
    this.myTickets    = new ArrayList<>();
    this.myEmpleado   = new ArrayList<>();
    this.myConductores = new ArrayList<>();   // NUEVO
    this.myPasajeros  = new ArrayList<>();    // NUEVO
    this.myCaja       = new CajaVenta();
    cargarDatosBase();
}

private void cargarDatosBase() {
    // Rutas (origen siempre Cúcuta)
    myRutas.add(new Ruta("R01", "Cúcuta", "Bucaramanga", 80000f));
    myRutas.add(new Ruta("R02", "Cúcuta", "Bogotá",      160000f));
    myRutas.add(new Ruta("R03", "Cúcuta", "Medellín",    180000f));
    myRutas.add(new Ruta("R04", "Cúcuta", "Cartagena",   220000f));

    // Buses
    myBuses.add(new BusTipoNormal    ("KAA-101", "DISPONIBLE",    40));
    myBuses.add(new BusTipoEjecutivo ("KBB-202", "DISPONIBLE",    30));
    myBuses.add(new BusTipoNormal    ("KCC-303", "DISPONIBLE",    40));
    myBuses.add(new BusTipoEjecutivo ("KDD-404", "DISPONIBLE",    30));
    myBuses.add(new BusTipoNormal    ("KEE-505", "MANTENIMIENTO", 40));
    myBuses.add(new BusTipoNormal    ("KFF-606", "DISPONIBLE",    30));

    // Salidas
    mySalidas.add(new Salida("S001", getRutaPorCodigo("R01"), parseFecha("15/03/2026 06:00"), getBusPorPlaca("KAA-101"), "PROGRAMADA"));
    mySalidas.add(new Salida("S002", getRutaPorCodigo("R01"), parseFecha("15/03/2026 14:00"), getBusPorPlaca("KBB-202"), "PROGRAMADA"));
    mySalidas.add(new Salida("S003", getRutaPorCodigo("R02"), parseFecha("16/03/2026 07:00"), getBusPorPlaca("KCC-303"), "PROGRAMADA"));
    mySalidas.add(new Salida("S004", getRutaPorCodigo("R02"), parseFecha("16/03/2026 20:00"), getBusPorPlaca("KDD-404"), "PROGRAMADA"));
    mySalidas.add(new Salida("S005", getRutaPorCodigo("R03"), parseFecha("17/03/2026 05:30"), getBusPorPlaca("KFF-606"), "PROGRAMADA"));
    mySalidas.add(new Salida("S006", getRutaPorCodigo("R03"), parseFecha("17/03/2026 18:00"), getBusPorPlaca("KAA-101"), "PROGRAMADA"));
    mySalidas.add(new Salida("S007", getRutaPorCodigo("R04"), parseFecha("18/03/2026 06:30"), getBusPorPlaca("KCC-303"), "PROGRAMADA"));
    mySalidas.add(new Salida("S008", getRutaPorCodigo("R04"), parseFecha("18/03/2026 19:30"), getBusPorPlaca("KBB-202"), "PROGRAMADA"));
}

private Ruta getRutaPorCodigo(String codigo) {
    for (Ruta r : myRutas) if (r.getCodigo().equals(codigo)) return r;
    return null;
}

private Bus getBusPorPlaca(String placa) {
    for (Bus b : myBuses) if (b.getPlaca().equals(placa)) return b;
    return null;
}

private java.time.LocalDateTime parseFecha(String fechaHora) {
    return java.time.LocalDateTime.parse(fechaHora,
        java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
}
```

---

## Notas para el agente

- Usar **arreglos de tamaño fijo** para `myBuses`; para `myRutas`, `myTickets`, `myEmpleado`, `myConductores` y `myPasajeros` se permite `ArrayList`.
- `CajaVenta` se instancia una sola vez dentro de `EmpresaTransporte` y se actualiza en cada transacción.
- Los estados de `Salida` y `PasajeTicket` deben manejarse como `String` con constantes en la misma clase (ej. `public static final String PROGRAMADA = "PROGRAMADA"`).
- El ID de salida se genera automáticamente: `"SAL-" + año + "-" + secuencial`.
- **Herencia simple**: `Pasajero` y `Conductor` extienden `Persona`; `Empleado` también extiende `Persona`. Las tres son hojas de la misma jerarquía.
- El origen de **todas** las rutas es siempre **"Cúcuta"**; validar esto al crear una ruta.
- La lógica de puestos consecutivos debe buscar el primer bloque de `n` puestos libres seguidos en el arreglo `myPuestos[]` del bus; si no existe bloque de ese tamaño, informar al usuario.
- Al buscar salida próxima en cancelación: primero iterar salidas del **mismo día** con hora posterior a la cancelada; si no hay cupo, iterar salidas del **día siguiente** (máximo). Más allá de ese rango → reembolso.
- Comenzar la implementación en el orden de las tareas de la Iteración 1 (de menor a mayor dependencia).
