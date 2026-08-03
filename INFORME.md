# Informe — Racha App

Documento de apoyo para la entrega del final (Aplicaciones móviles).  
Describe cada pantalla, las funcionalidades esperadas y el flujo de uso.

Herramienta de mock / presentación visual (opcional): Canva — https://canva.link/yn7nn9z2ad5z1bx

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
- Navegación entre meses anteriores para consultar el historial; permite volver hasta el mes actual.
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
- Tarjeta inferior de inspiración diaria obtenida desde ZenQuotes mediante Retrofit, con autor, atribución y fallback local.
- Editar perfil (nombre y teléfono → Firestore + Auth displayName).
- Cerrar sesión.

**Flujo**
Abrir Perfil → editar datos o cerrar sesión.

---

## 4. Datos en Firebase (resumen)

### Usuarios

Los datos principales de cada cuenta se guardan en:

`users/{userId}`

Cada documento contiene información como `fullName`, `phoneNumber`, `email` y `photoUrl`.

### Rachas

Cada usuario tiene una subcolección independiente con sus propias rachas:

`users/{userId}/rachas/{rachaId}`

Cada documento de racha almacena `nombre`, `nombreKey`, `icono`, `dias`,
`lastCompletedDate`, `userId`, `createdAt` y `updatedAt`. Esta separación permite que
cada cuenta consulte y modifique solamente sus propios hábitos.

### Historial de completions

Dentro de cada racha se guarda el historial de los días completados:

`users/{userId}/rachas/{rachaId}/completions/{yyyy-MM-dd}`

Cada completion contiene `date`, `nombre`, `icono` y `completedAt`. Estos datos se
utilizan para construir el calendario y las estadísticas mensuales.

### Amigos

Los amigos agregados por cada usuario se guardan en:

`users/{userId}/friends/{friendUserId}`

Cada documento contiene `fullName`, `email`, `photoUrl` y `selectedRachas`, donde se
conserva la información de las rachas compartidas.

### Catálogo de rachas

Las sugerencias disponibles al crear una nueva racha se guardan en una colección
compartida:

`rachaCatalog/{nombreNormalizado}`

Cada documento contiene `nombre`, `nombreKey`, `icono`, `active` y `createdAt`.

### Estructura completa

```
users/{userId}
  fullName, phoneNumber, email, photoUrl
  rachas/{rachaId}
    nombre, nombreKey, icono, dias, lastCompletedDate, ...
    completions/{yyyy-MM-dd}
      date, nombre, icono, completedAt
  friends/{friendUserId}
    fullName, email, photoUrl, selectedRachas, ...

rachaCatalog/{nombreNormalizado}
  nombre, nombreKey, icono, active, createdAt
```

---

## 5. Criterios técnicos cubiertos

- Varias pantallas y navegación.
- ConstraintLayout / LinearLayout, Button, TextView.
- Contenido real e imágenes (Glide / avatares por URL).
- Contenido JSON desde URL mediante Retrofit (inspiración diaria de ZenQuotes en Perfil).
- Eventos y comportamiento dinámico (listas, marcar día, filtros).
- Pasaje de datos entre Activities (extras).
- Firebase Auth (login + registro).
- Firestore (documentos y colecciones; lectura y escritura).
- Toasts en operaciones.
- Recursos en `strings` / `colors` / `dimens`.
