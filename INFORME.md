# Informe — Racha App

Documento de apoyo para la entrega del final (Aplicaciones móviles).  
Describe cada pantalla, las funcionalidades esperadas y el flujo de uso.

Herramienta de mock / presentación visual (opcional): Canva — https://canva.link/bc5yckwbhbnnr9a

---

## 1. Objetivo de la aplicación

Racha App ayuda a mantener hábitos diarios (“rachas”). El usuario registra actividades, marca el cumplimiento del día, consulta el historial en un calendario y se conecta con otras personas que siguen la misma racha.

---

## 2. Flujo general de uso

```
Login / Registro
        │
        ▼
     Inicio ──────────► Detalle de racha
        │                      │
        │                      ├── agregar amigo
        │                      └── eliminar racha
        │
        ├── Calendario (historial real por día)
        ├── Amigos (buscar / eliminar)
        └── Perfil (editar / logout)
```

1. El usuario entra con email y contraseña (o se registra).
2. En Inicio ve sus rachas y puede marcar el día o crear una nueva.
3. Al tocar una racha abre el detalle: estado, personas con la misma racha, eliminar.
4. El Calendario muestra el mes actual según completions en Firestore.
5. Amigos lista contactos agregados; se puede buscar y eliminar.
6. Perfil muestra datos y estadísticas; permite editar nombre/teléfono y cerrar sesión.

---

## 3. Pantallas

### 3.1 Login (`LoginActivity`)

**Funcionalidades**
- Inicio de sesión con email y contraseña (Firebase Auth).
- Validación de campos vacíos.
- Toast ante error.
- Acceso a la pantalla de registro.
- Si ya hay sesión activa, redirige a Inicio.

**Flujo**
Usuario completa email/password → Iniciar sesión → Inicio.

---

### 3.2 Registro (`RegisterActivity`)

**Funcionalidades**
- Alta de cuenta (nombre, teléfono, email, password).
- Creación del usuario en Auth y del documento en Firestore (`users/{uid}`).
- Generación de avatar por URL.
- Toast ante errores.

**Flujo**
Completar formulario → Crear cuenta → Inicio. La cuenta nueva comienza sin rachas y la pantalla invita a crear la primera.

---

### 3.3 Inicio (`MainActivity`)

**Funcionalidades**
- Saludo con nombre y avatar.
- Lista de rachas del usuario.
- Estado vacío con acceso a "Nueva racha" cuando el usuario todavía no agregó ninguna.
- Contador de rachas completadas hoy.
- Marcar racha del día (🔥) → actualiza `dias`, `lastCompletedDate` y escribe en `completions`.
- Crear nueva racha (diálogo).
- Navegación inferior.
- Al volver desde Detalle, recarga la lista.

**Flujo**
Si la lista está vacía, crear la primera racha → marcar el día / crear más rachas / abrir detalle.

---

### 3.4 Detalle de racha (`RachaDetailActivity`)

**Funcionalidades**
- Muestra nombre, ícono, días y estado del día (extras del Intent).
- Lista usuarios con la misma `nombreKey`.
- Agregar amigo (Firestore `friends` + `selectedRachas`).
- Eliminar racha (documento + historial `completions`) con confirmación y Toast.
- Botón volver.

**Flujo**
Desde Inicio → ver detalle → agregar amigo o eliminar racha → volver.

---

### 3.5 Calendario (`CalendarActivity`)

**Funcionalidades**
- Auth-guard (sin sesión → Login).
- Mes actual real.
- Días coloreados según completions:
  - verde: todas las rachas del día;
  - naranja: parcial;
  - rosa: hoy pendiente.
- 🔥 en días con actividad.
- Estadísticas del mes (días con actividad, mejor racha).
- Al tocar un día, lista de rachas de ese día (✓ / ○).
- Datos desde Firestore (no mock).

**Flujo**
Desde navegación → ver mes → tocar día → revisar rachas de esa fecha.

---

### 3.6 Amigos (`FriendsActivity`)

**Funcionalidades**
- Lista de amigos agregados y rachas en común.
- Búsqueda/filtro por nombre o email.
- Eliminar amigo con confirmación y Toast.
- Hint de cómo agregar amigos (desde el detalle de una racha).

**Flujo**
Abrir Amigos → buscar → eliminar si hace falta.  
Para sumar amigos: Inicio → Detalle → Agregar.

---

### 3.7 Perfil (`ProfileActivity`)

**Funcionalidades**
- Auth-guard.
- Nombre, email, teléfono y avatar.
- Estadísticas: rachas activas, completadas hoy, mejor racha.
- Editar perfil (nombre y teléfono → Firestore + Auth displayName).
- Cerrar sesión.

**Flujo**
Abrir Perfil → editar datos o cerrar sesión.

---

## 4. Datos en Firebase (resumen)

```
users/{userId}
  fullName, phoneNumber, email, photoUrl
  rachas/{rachaId}
    nombre, nombreKey, icono, dias, lastCompletedDate, ...
    completions/{yyyy-MM-dd}
      date, nombre, icono, completedAt
  friends/{friendUserId}
    fullName, email, photoUrl, selectedRachas, ...
```

---

## 5. Criterios técnicos cubiertos

- Varias pantallas y navegación.
- ConstraintLayout / LinearLayout, Button, TextView.
- Contenido real e imágenes (Glide / avatares por URL).
- Eventos y comportamiento dinámico (listas, marcar día, filtros).
- Pasaje de datos entre Activities (extras).
- Firebase Auth (login + registro).
- Firestore (documentos y colecciones; lectura y escritura).
- Toasts en operaciones.
- Recursos en `strings` / `colors` / `dimens`.
