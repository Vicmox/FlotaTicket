# Plan frontend — Copetran Swing (minimalista)

## Contexto

La capa de negocio (`paquete negocio`) ya está completamente implementada. Este plan cubre únicamente el paquete `presentacion`. El objetivo es una interfaz **funcional, simple y limpia** — sin animaciones, sin componentes personalizados complejos, sin pintar nada a mano. Solo Swing estándar organizado de forma clara.

La instancia de `EmpresaTransporte` se crea una sola vez en `Main.java` y se pasa por constructor a cada ventana y panel. Nunca crear una segunda instancia.

---

## Principios de diseño para esta implementación

- Usar **solo componentes Swing estándar**: `JFrame`, `JPanel`, `JLabel`, `JButton`, `JTextField`, `JPasswordField`, `JComboBox`, `JTable`, `JScrollPane`, `JTabbedPane`, `JRadioButton`, `JOptionPane`
- **Sin `paintComponent` personalizado**, sin bordes redondeados, sin gradientes
- Layouts: solo `BorderLayout`, `GridLayout`, `FlowLayout` y `BoxLayout` — suficiente para todo
- Color de fondo del frame principal: `Color(241, 239, 232)` — gris cálido suave
- Botones de acción principal: fondo `Color(26, 79, 138)`, texto blanco
- Tablas con `setRowHeight(24)` y encabezado legible — nada más
- Toda validación de formulario con `JOptionPane.showMessageDialog`
- Toda confirmación de acción con `JOptionPane.showConfirmDialog`

---

## Paleta mínima (5 colores, todo lo demás por defecto)

```java
// Pegar directamente donde se necesite, sin crear clase aparte
Color AZUL       = new Color(26,  79,  138); // botones primarios, header
Color FONDO      = new Color(241, 239, 232); // fondo general de paneles
Color VERDE_TX   = new Color(59,  109, 17);  // texto estado disponible/vigente
Color ROJO_TX    = new Color(163, 45,  45);  // texto estado cancelado/error
Color GRIS_TX    = new Color(136, 135, 128); // texto secundario / labels
```

No pintar fondos de celdas ni badges — simplemente cambiar el color del texto según el estado es suficiente para esta implementación minimalista.

---

## Estructura de archivos

```
presentacion/
├── Main.java                   ← Punto de entrada
├── LoginFrame.java             ← Ventana de login
├── MainFrame.java              ← Frame principal con CardLayout
├── DashboardPanel.java         ← RF5 resumen (panel inicio)
├── ParametrizacionPanel.java   ← RF1 (JTabbedPane: Rutas, Buses, Salidas)
├── VentasPanel.java            ← RF2 y RF3 (JTabbedPane: 1 tiquete / Ida y vuelta)
├── CancelacionesPanel.java     ← RF4
└── ReportesPanel.java          ← RF5 completo
```

---

## Main.java

```java
public class Main {
    public static void main(String[] args) {
        EmpresaTransporte empresa = new EmpresaTransporte(); // carga datos base
        SwingUtilities.invokeLater(() -> {
            LoginFrame login = new LoginFrame(empresa);
            login.setVisible(true);
        });
    }
}
```

---

## LoginFrame.java

**Ventana:** `JFrame` 380×420px, centrado en pantalla, no redimensionable.

**Layout del panel central:** `BoxLayout` vertical con padding de 40px horizontal.

**Componentes de arriba a abajo:**
1. `JLabel` "COPETRAN" — fuente 22px bold, color `AZUL`, centrado
2. `JLabel` "Sistema de gestión de flota" — fuente 12px, color `GRIS_TX`, centrado
3. Separador vertical de 20px (`Box.createVerticalStrut(20)`)
4. `JLabel` "Usuario" + `JTextField txUsuario`
5. `JLabel` "Contraseña" + `JPasswordField txPassword`
6. `JLabel` "Perfil" + `JComboBox<String>` con opciones: `"Administrador"`, `"Cajero"`, `"Supervisor"`
7. Separador de 10px
8. `JButton` "Ingresar" — fondo `AZUL`, texto blanco, ancho completo

**Lógica del botón Ingresar:**
- Si `txUsuario` o `txPassword` están vacíos → `JOptionPane` error "Complete todos los campos"
- Si no están vacíos → cerrar `LoginFrame`, abrir `MainFrame(empresa, usuario)`

---

## MainFrame.java

**Ventana:** `JFrame` 900×620px, centrado, título "Copetran — Sistema de gestión".

**Layout:** `BorderLayout`

**NORTH — barra superior:** `JPanel` con `FlowLayout(LEFT)`, fondo `AZUL`
- `JLabel` "COPETRAN" blanco bold 16px
- `JLabel` " | " blanco
- `JLabel` con el nombre del usuario activo, blanco 12px
- Del lado derecho (`FlowLayout` o `BorderLayout` interno): `JLabel` con la fecha actual

**CENTER — navegación lateral + contenido:** `JSplitPane` o simplemente un `JPanel` con `BorderLayout`:
- **WEST:** `JPanel` con `GridLayout(6,1)` — botones de navegación verticales (150px de ancho):
  - `[Dashboard]`, `[Parametrización]`, `[Ventas]`, `[Cancelaciones]`, `[Reportes]`
  - Fondo `AZUL`, texto blanco, sin borde
  - El botón activo tiene fondo `Color(22, 63, 114)` (azul más oscuro)
- **CENTER:** `JPanel` con `CardLayout` conteniendo todos los paneles nombrados:
  - `"dashboard"` → `DashboardPanel`
  - `"parametrizacion"` → `ParametrizacionPanel`
  - `"ventas"` → `VentasPanel`
  - `"cancelaciones"` → `CancelacionesPanel`
  - `"reportes"` → `ReportesPanel`

**Navegación:** cada botón del lado WEST llama `cardLayout.show(centerPanel, "nombre")` y actualiza el color del botón activo.

---

## DashboardPanel.java (RF5 — resumen)

**Layout:** `BorderLayout`

**NORTH — 4 etiquetas de resumen en `GridLayout(1,4)` con borde:**
Cada celda es un `JPanel` con `BorderLayout` y `LineBorder`:
- `JLabel` nombre de métrica arriba (pequeño, `GRIS_TX`)
- `JLabel` valor abajo (grande, bold)

| Métrica | Fuente de dato |
|---|---|
| Tiquetes vendidos | `empresa.getCajaVenta().getTotalVendidos()` |
| Total vendido | `empresa.getCajaVenta().getMontoCaja()` formateado "$#,###" |
| Reembolsos | `empresa.getCajaVenta().getTotalReembolsos()` |
| Ingreso neto | `empresa.getCajaVenta().getIngresoNeto()` formateado "$#,###" |

**CENTER — `JTabbedPane` con dos tabs:**

Tab "Salidas del día":
- `JTable` no editable, columnas: ID Salida, Ruta, Fecha, Hora, Bus, Estado
- Datos: `empresa.listarSalidas()` — todas las salidas
- Color de texto en columna Estado: `VERDE_TX` si PROGRAMADA/COMPLETADA, `ROJO_TX` si CANCELADA, negro si EN_RUTA
- Dentro de `JScrollPane`

Tab "Tiquetes vendidos":
- `JTable` no editable, columnas: ID Tiquete, Pasajero, Ruta, Silla, Valor, Estado
- Datos: `empresa.listarTickets()`
- Color de texto en columna Estado: `VERDE_TX` si VIGENTE, `ROJO_TX` si REEMBOLSADO

**SOUTH — barra de estado:**
- `JLabel` con texto: `"Última actualización: " + LocalDateTime.now()`
- `JButton` "Actualizar" que refresca las tablas y las métricas del NORTH

---

## ParametrizacionPanel.java (RF1)

**Layout raíz:** `JTabbedPane` con tres pestañas: **Rutas**, **Buses**, **Salidas**

---

### Tab Rutas

**Layout:** `BorderLayout`

**NORTH — formulario en `GridLayout(3,4)` con gap:**
- `JLabel` "Código" + `JTextField txCodigo`
- `JLabel` "Origen" + `JTextField txOrigen`
- `JLabel` "Destino" + `JTextField txDestino`
- `JLabel` "Tarifa (COP)" + `JTextField txTarifa`
- `JButton` "Guardar" (fondo `AZUL`, texto blanco)
- `JButton` "Limpiar"

**CENTER — `JTable` dentro de `JScrollPane`:**
- Columnas: Código, Origen, Destino, Tarifa
- Datos: `empresa.listarRutas()`
- Al seleccionar fila → cargar datos en el formulario para edición
- `JButton` "Eliminar seleccionada" debajo de la tabla → llama `empresa.eliminarRuta(codigo)` tras confirmación

**Lógica Guardar:**
- Si `txCodigo` coincide con ruta existente → modo edición (`empresa.editarRuta(...)`)
- Si no existe → modo creación (`empresa.crearRuta(...)`)
- Refrescar tabla después de cada operación

---

### Tab Buses

**Layout:** `BorderLayout`

**NORTH — formulario en `GridLayout(3,4)` con gap:**
- `JLabel` "Placa" + `JTextField txPlaca`
- `JLabel` "Tipo" + `JComboBox<String>` opciones: `"NORMAL"`, `"EJECUTIVO"`
- `JLabel` "Capacidad" + `JTextField txCapacidad`
- `JLabel` "Estado" + `JComboBox<String>` opciones: `"DISPONIBLE"`, `"EN_RUTA"`, `"MANTENIMIENTO"`
- `JButton` "Guardar" (fondo `AZUL`, texto blanco)
- `JButton` "Limpiar"

**CENTER — `JTable` dentro de `JScrollPane`:**
- Columnas: Placa, Tipo, Capacidad, Estado
- Datos: `empresa.listarBuses()`
- Al seleccionar fila → cargar datos en formulario para edición
- `JButton` "Eliminar seleccionada" debajo → llama `empresa.eliminarBus(placa)`

---

### Tab Salidas

**Layout:** `BorderLayout`

**NORTH — formulario en `GridLayout(3,4)` con gap:**
- `JLabel` "Ruta" + `JComboBox<String>` poblado con `empresa.listarRutas()` mostrando `"codigo — origen → destino"`
- `JLabel` "Bus" + `JComboBox<String>` poblado con `empresa.listarBuses()` mostrando `"placa (tipo, capacidad)"`
- `JLabel` "Fecha (dd/MM/yyyy)" + `JTextField txFecha`
- `JLabel` "Hora (HH:mm)" + `JTextField txHora`
- `JLabel` "Precio base" + `JTextField txPrecio` (se autocompleta con tarifa de la ruta al cambiar el combo)
- `JButton` "Programar salida" (fondo `AZUL`, texto blanco)
- `JButton` "Limpiar"

**CENTER — `JTable` dentro de `JScrollPane`:**
- Columnas: ID Salida, Ruta, Fecha, Hora, Bus, Estado
- Datos: `empresa.listarSalidas()`
- Color de texto en Estado igual que Dashboard
- `JButton` "Cancelar salida seleccionada" debajo → redirige a `CancelacionesPanel` o abre diálogo de cancelación directamente

---

## VentasPanel.java (RF2 y RF3)

**Layout raíz:** `JTabbedPane` con dos pestañas: **1 Tiquete** y **Ida y Vuelta**

---

### Tab "1 Tiquete" (RF2)

**Layout:** `BorderLayout`

**WEST — formulario de selección y datos (ancho fijo 320px):**

Panel 1 "Seleccionar salida" con `GridLayout(4,2)`:
- `JLabel` "Salida" + `JComboBox<String>` con salidas en estado `PROGRAMADA`  
  Mostrar: `"S001 — R01 Cúcuta→Bucaramanga 15/03 06:00 (Bus KAA-101)"`
- Al cambiar selección: actualizar el panel de sillas del CENTER

Panel 2 "Datos del pasajero" con `GridLayout(4,2)`:
- `JLabel` "Cédula" + `JTextField txCedula`
- `JLabel` "Nombre" + `JTextField txNombre`
- `JLabel` "Dirección" + `JTextField txDireccion`
- `JLabel` "Silla seleccionada" + `JLabel lbSilla` (solo lectura, se llena al hacer clic en el mapa)

Panel 3 "Resumen" con `GridLayout(3,2)`:
- `JLabel` "Tarifa base:" + `JLabel` con valor de la ruta
- `JLabel` "Cargo servicio:" + `JLabel "$2.500"`
- `JLabel` "**Total:**" bold + `JLabel` con total calculado bold

`JButton` "Generar tiquete" — fondo `AZUL`, texto blanco, ancho completo al fondo del WEST

**CENTER — mapa de sillas:**
- `JPanel` con `GridLayout(filas, columnas)` donde filas y columnas se calculan según capacidad del bus
  - Ejemplo: 40 sillas → `GridLayout(10, 4)` con gap de 4px
- Cada silla es un `JButton` de texto con el número de silla
  - Disponible: fondo verde claro `Color(234,243,222)`, texto `VERDE_TX`
  - Ocupada: deshabilitado `setEnabled(false)`, fondo gris `Color(220,220,220)`
  - Seleccionada: fondo `AZUL`, texto blanco
- Al clic en silla disponible: desmarcar la anterior (si hay), marcar la nueva, actualizar `lbSilla`
- Título arriba del mapa: `JLabel` con `"Bus: KAA-101 — 22/40 sillas disponibles"`

**Lógica botón "Generar tiquete":**
1. Validar que haya salida seleccionada, silla seleccionada y campos del pasajero no vacíos
2. Llamar `empresa.registrarPasajeroYSilla(...)` y `empresa.generarTicket(...)`
3. Mostrar `JOptionPane` con mensaje: `"Tiquete generado exitosamente\nID: TK-XXXX\nPasajero: Nombre\nSilla: 7\nTotal: $87.500"`
4. Limpiar formulario y refrescar mapa de sillas

---

### Tab "Ida y Vuelta" (RF3)

**Layout:** `BorderLayout`

**NORTH — dos combos de selección lado a lado en `GridLayout(1,2)`:**

Panel izquierdo "Salida IDA":
- `JLabel` "Salida de ida" + `JComboBox<String>` con salidas PROGRAMADAS
- `JLabel` "Silla ida" + `JComboBox<String>` con sillas disponibles de esa salida
  (poblar al cambiar el combo de salida)

Panel derecho "Salida VUELTA":
- `JLabel` "Salida de vuelta" + `JComboBox<String>` con salidas PROGRAMADAS en la **misma ruta** que la salida de ida seleccionada
  (actualizar al cambiar el combo de ida)
- `JLabel` "Silla vuelta" + `JComboBox<String>` con sillas disponibles de esa salida
- `JLabel` de validación de ruta: texto verde "✓ Misma ruta validada" o rojo "✗ Rutas distintas"

**CENTER — datos del pasajero en `GridLayout(3,2)` + resumen:**
- `JTextField` Cédula, Nombre, Dirección (mismo pasajero para ambos tiquetes)
- Panel resumen con `GridLayout(4,2)`:
  - Tiquete ida: $XXX.XXX
  - Tiquete vuelta: $XXX.XXX
  - Descuento 10%: -$XX.XXX
  - **Total: $XXX.XXX** bold

**SOUTH:**
- `JButton` "Generar 2 tiquetes" — fondo `AZUL`, texto blanco
- Lógica: validar campos, llamar `empresa.ventaIdaYVuelta(...)`, mostrar `JOptionPane` con resumen de ambos tiquetes

---

## CancelacionesPanel.java (RF4)

**Layout:** `BorderLayout`

**NORTH — buscador en `FlowLayout(LEFT)`:**
- `JLabel` "ID de salida:" + `JTextField txIdSalida` (ancho 120px)
- `JButton` "Buscar" (fondo `AZUL`, texto blanco)
- `JLabel lbResultado` — inicialmente vacío; al buscar muestra `"Salida encontrada: S001 — Bogotá→Bucaramanga 15/03 07:00"` en verde, o `"Salida no encontrada"` en rojo

**CENTER — panel de detalle** (visible solo cuando se encuentra la salida):

`JSplitPane` horizontal:

Panel izquierdo "Datos de la salida" con `GridLayout(5,2)`:
- ID Salida, Ruta, Fecha, Bus asignado, Tiquetes vigentes — todo como `JLabel` pares clave:valor

Panel derecho "Tiquetes afectados":
- `JTable` con columnas: ID Tiquete, Pasajero, Silla, Estado
- Color de texto en Estado: `VERDE_TX` si VIGENTE, `ROJO_TX` si ya reembolsado/reprogramado
- Dentro de `JScrollPane`

**SOUTH — acciones:**
- `JPanel` con `FlowLayout(LEFT)`:
  - `JRadioButton rbReprogramar` "Reprogramar automáticamente"
  - `JRadioButton rbReembolsar` "Marcar como REEMBOLSADO"
  - Los dos en un `ButtonGroup`
  - `rbReprogramar` seleccionado por defecto
- `JButton` "Confirmar cancelación" — fondo `ROJO_TX` (`Color(163,45,45)`), texto blanco
- `JButton` "Cancelar"

**Lógica "Confirmar cancelación":**
1. Verificar que haya una salida buscada y una opción seleccionada
2. `JOptionPane.showConfirmDialog` — "¿Confirma cancelar la salida S001? Esta acción no se puede deshacer."
3. Si acepta: llamar `empresa.cancelarSalida(idSalida, accion)` donde accion es `"REPROGRAMAR"` o `"REEMBOLSAR"`
4. Mostrar `JOptionPane` con resumen: cuántos tiquetes reprogramados y cuántos reembolsados
5. Limpiar el panel y resetear el buscador

---

## ReportesPanel.java (RF5)

**Layout raíz:** `JTabbedPane` con tres pestañas

---

### Tab "Ventas por ruta"

**NORTH — filtros en `FlowLayout(LEFT)`:**
- `JLabel` "Desde:" + `JTextField txDesde` (formato dd/MM/yyyy)
- `JLabel` "Hasta:" + `JTextField txHasta`
- `JComboBox<String>` rutas (`"Todas"` + lista de `empresa.listarRutas()`)
- `JButton` "Generar" (fondo `AZUL`)

**CENTER — `JTable` dentro de `JScrollPane`:**
- Columnas: Ruta, Origen → Destino, Tiquetes vendidos, Reembolsos, Total neto
- Datos: iterar `empresa.listarTickets()` filtrando por ruta y rango de fechas
- Fila de totales al final con texto bold

**SOUTH:** `JLabel` con totales rápidos: `"Total vendido: $X | Reembolsos: $X | Ingreso neto: $X"`

---

### Tab "Totales del día"

**Layout:** `GridLayout(2, 1)` con gap

Panel superior — 3 celdas en `GridLayout(1,3)`:
Cada celda un `JPanel` con `LineBorder` y dos `JLabel` (nombre + valor):
- "Total vendido" → `empresa.getCajaVenta().getMontoCaja()` — texto `VERDE_TX`
- "Total reembolsado" → `empresa.getCajaVenta().getTotalReembolsos()` — texto `ROJO_TX`
- "Ingreso neto" → `empresa.getCajaVenta().getIngresoNeto()` — texto `AZUL`

Panel inferior — `JLabel` con: `"Reporte generado: " + LocalDateTime.now()` y `JButton` "Actualizar"

---

### Tab "Ventas por mes / rango"

**NORTH — filtros en `FlowLayout(LEFT)`:**
- `JLabel` "Mes:" + `JComboBox<Integer>` (1–12)
- `JLabel` "Año:" + `JTextField txAnio` (4 dígitos)
- `JButton` "Generar"

**CENTER — `JTable` dentro de `JScrollPane`:**
- Columnas: Fecha, ID Tiquete, Pasajero, Ruta, Valor, Estado
- Datos: `empresa.listarTickets()` filtrados por mes y año

---

## Reglas generales para el agente

1. **No crear una segunda instancia de `EmpresaTransporte`** en ningún panel. Siempre recibirla por constructor.

2. **Refrescar tablas** después de cada operación de escritura. Patrón recomendado:
   ```java
   // Después de cualquier crearX / editarX / eliminarX
   DefaultTableModel model = (DefaultTableModel) tabla.getModel();
   model.setRowCount(0); // limpiar
   for (Ruta r : empresa.listarRutas()) {
       model.addRow(new Object[]{ r.getCodigo(), r.getOrigen(), r.getDestino(), r.getTarifa() });
   }
   ```

3. **Color de texto por estado** en tablas — usar un `TableCellRenderer` simple:
   ```java
   tabla.getColumnModel().getColumn(indiceEstado).setCellRenderer(
       new DefaultTableCellRenderer() {
           @Override
           public Component getTableCellRendererComponent(JTable t, Object v,
                   boolean sel, boolean foc, int row, int col) {
               super.getTableCellRendererComponent(t, v, sel, foc, row, col);
               String estado = v == null ? "" : v.toString();
               if (estado.equals("VIGENTE") || estado.equals("PROGRAMADA") || estado.equals("DISPONIBLE"))
                   setForeground(new Color(59, 109, 17));
               else if (estado.equals("CANCELADA") || estado.equals("REEMBOLSADO"))
                   setForeground(new Color(163, 45, 45));
               else
                   setForeground(Color.BLACK);
               return this;
           }
       }
   );
   ```

4. **Validación de campos vacíos** — método utilitario recomendado:
   ```java
   private boolean camposVacios(JTextField... campos) {
       for (JTextField c : campos)
           if (c.getText().trim().isEmpty()) return true;
       return false;
   }
   ```

5. **Combo de salidas** — mostrar la información relevante usando `toString()` en `Salida`, o construir el string en el panel:
   ```java
   for (Salida s : empresa.listarSalidas()) {
       if (s.getEstado().equals("PROGRAMADA"))
           comboSalidas.addItem(s.getIdSalida() + " — " + s.getMyRuta().getOrigen()
               + "→" + s.getMyRuta().getDestino()
               + " " + s.getFecha().format(formatter)
               + " (Bus: " + s.getMyBus().getPlaca() + ")");
   }
   ```
   Guardar en un `Map<String, Salida>` paralelo para recuperar el objeto al seleccionar.

6. **Mapa de sillas** — reconstruir el `JPanel` de sillas cada vez que cambie la salida seleccionada. Llamar `revalidate()` y `repaint()` sobre el panel contenedor después de agregar los botones.

7. **Tamaño de ventana** — no usar `setResizable(false)` en el `MainFrame`; sí usarlo en `LoginFrame`.

8. El **orden de implementación** sugerido es:
   - `LoginFrame` → `MainFrame` (estructura vacía con navegación) → `DashboardPanel` → `ParametrizacionPanel` → `VentasPanel` → `CancelacionesPanel` → `ReportesPanel`
