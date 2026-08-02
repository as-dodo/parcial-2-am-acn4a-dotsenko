# Racha App

Aplicación Android para registrar, mantener y consultar hábitos personales llamados **rachas**.

Cada usuario puede crear una cuenta, iniciar sesión, ver sus rachas, marcar las completadas del día, consultar el historial en el calendario, agregar amigos con rachas en común y editar su perfil.

## Funcionalidades principales

- Registro e inicio de sesión con Firebase Authentication (email y contraseña).
- Perfil de usuario en Cloud Firestore (nombre, teléfono, email, foto).
- Edición de perfil (nombre y teléfono) con actualización en Firestore.
- Rachas por usuario en Firestore, con seed inicial para cuentas nuevas.
- Creación, marcado diario y eliminación de rachas.
- Historial de completions por día (`users/{uid}/rachas/{id}/completions/{yyyy-MM-dd}`).
- Calendario con datos reales del mes, colores según progreso y detalle por día.
- Detalle de racha y búsqueda de otros usuarios con la misma racha.
- Amigos: agregar desde rachas en común, buscar, eliminar.
- Estadísticas en perfil y calendario.
- Avatares desde URL con Glide.
- Toasts en operaciones clave.
- Navegación inferior: Inicio, Calendario, Amigos, Perfil.
- Pasaje de datos entre Activities con Intent extras.

## Pantallas

| Pantalla | Clase | Qué hace |
|---|---|---|
| Login | `LoginActivity` | Inicio de sesión y acceso al registro |
| Registro | `RegisterActivity` | Alta de cuenta y perfil en Firestore |
| Inicio | `MainActivity` | Saludo, rachas del día, alta y marcado |
| Detalle | `RachaDetailActivity` | Detalle, amigos candidatos, eliminar racha |
| Calendario | `CalendarActivity` | Mes actual, historial y rachas por día |
| Amigos | `FriendsActivity` | Lista, búsqueda y eliminación de amigos |
| Perfil | `ProfileActivity` | Datos, estadísticas, editar perfil, logout |

## Flujo de uso

1. Registrarse o iniciar sesión.
2. En **Inicio**, ver rachas, marcar el día con 🔥 o crear una nueva.
3. Abrir una racha para ver detalle, agregar amigos o eliminarla.
4. En **Calendario**, ver el mes y tocá un día para ver qué se completó.
5. En **Amigos**, buscar y gestionar la lista.
6. En **Perfil**, revisar estadísticas, editar datos o cerrar sesión.

## Informe

Descripción por pantalla (funciones esperadas + flujo): ver [`INFORME.md`](INFORME.md).

Informe visual (Canva): https://canva.link/bc5yckwbhbnnr9a

## Stack

- Java, Activities, layouts XML (ConstraintLayout / LinearLayout)
- Firebase Auth + Cloud Firestore
- Glide
- Recursos organizados en `strings.xml`, `colors.xml`, `dimens.xml`
