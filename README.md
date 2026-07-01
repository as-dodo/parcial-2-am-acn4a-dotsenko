# parcial-2-am-acn4a-dotsenko

## Descripción breve
Racha App es una aplicación Android para registrar, mantener y consultar hábitos personales llamados “rachas”.  
La app permite que cada usuario cree una cuenta, inicie sesión, vea sus rachas, marque cuáles completó en el día, agregue nuevas rachas y encuentre otros usuarios que estén siguiendo la misma actividad.


## Funcionalidades principales
- Registro de usuarios con Firebase Authentication.
- Inicio de sesión con email y contraseña.
- Persistencia del perfil del usuario en Cloud Firestore.
- Persistencia de rachas por usuario en Firestore.
- Creación automática de rachas iniciales para usuarios nuevos.
- Creación manual de nuevas rachas.
- Marcado de rachas como completadas en el día.
- Actualización dinámica del contador de días acumulados.
- Visualización del detalle de una racha.
- Pasaje de datos entre Activities mediante Intent extras.
- Búsqueda de otros usuarios que tengan una racha con el mismo nombre.
- Posibilidad de agregar amigos desde rachas en común.
- Pantalla de amigos con usuarios agregados y rachas compartidas.
- Pantalla de perfil con datos del usuario y estadísticas calculadas.
- Cierre de sesión.
- Carga de avatares desde URL usando Glide.
- Navegación inferior entre Inicio, Calendario, Amigos y Perfil.

## Pantallas implementadas

- `LoginActivity`: permite iniciar sesión y acceder al registro.
- `RegisterActivity`: permite crear una cuenta nueva y guardar el perfil en Firestore.
- `MainActivity`: muestra el saludo, la fecha actual, el resumen del día y la lista de rachas del usuario.
- `RachaDetailActivity`: muestra el detalle de una racha seleccionada y usuarios con la misma racha.
- `CalendarActivity`: muestra una grilla visual de calendario como prototipo para una futura integración con historial real.
- `FriendsActivity`: muestra amigos agregados desde rachas en común.
- `ProfileActivity`: muestra datos del usuario, estadísticas personales y permite cerrar sesión.


## Informe

https://canva.link/bc5yckwbhbnnr9a



