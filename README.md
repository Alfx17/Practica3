<h1 align="center">Práctica 3: Aplicación Móvil Básica</h1>

---

## Insignias
![Estado](https://img.shields.io/badge/Estado-Terminado-brightgreen)
![Lenguaje](https://img.shields.io/badge/Kotlin-✓-purple)
![Plataforma](https://img.shields.io/badge/Plataforma-Android-blue)

---

## Índice
- [Título](#practica-2--aplicacion-movil-basica)
- [Insignias](#insignias)
- [Índice](#índice)
- [Objetivo](#objetivo)
- [Descripción del proyecto](#descripción-del-proyecto)
- [Implementación de Temas con SharedPreferences](#implementación-de-Temas-con-SharedPreferences)
- [Descripción de cómo se implementó la funcionalidad de cambio de tema](#descripción-de-como-se-implementó-la-funcionalidad-de-cambio-de-tema)
- [Ejemplo de Uso](#ejemplo-de-uso)
- [Presentación de la aplicación](#presentación-de-la-aplicación)
- [Estado de la tarea](#estado-de-la-tarea)
- [Características de la aplicación](#características-de-la-aplicación)
- [Acceso al proyecto](#acceso-al-proyecto)
- [Tecnologías utilizadas](#tecnologías-utilizadas)
- [Desarrollador](#-desarrollador)

---

## Objetivo
<p align="justify">El objetivo de esta práctica es implementar la persistencia de datos a través de SharedPreferences para guardar las preferencias del usuario. Específicamente, añadirán una funcionalidad que permita al usuario seleccionar y aplicar un tema visual (claro u oscuro) a la aplicación, asegurando que esta elección se mantenga entre diferentes sesiones de uso.
</p>

---

## Descripción del proyecto
<p align="justify">Esta aplicación de Android contiene un Activity principal en el que se muestran los diferentes países de América. En dicha pantalla se encuentran una serie de botones sobre algunos de los países, que al ser seleccionados nos llevarán a otro activity, el cuál contendrá un mapa de mayor tamaño del país. Los países disponibles se listan a continuación:</p>
<ul>
  <li>Alaska</li>
  <li>Groenlandia</li>
  <li>Canadá</li>
  <li>Estados Unidos de América</li>
  <li>México</li>
  <li>Venezuela</li>
  <li>Colombia</li>
  <li>Perú</li>
  <li>Brasil</li>
  <li>Argentina</li>
  
</ul>
<p align="center">
  <img src="fondo/America.jpeg" alt="Prueba" width="300"/>
</p>
<p align="justify">Cada uno de los países tiene un activity, en el que se muestran por lo menos 1 de los lugares turísticos más emblemáticos del país. Cada uno de esos lugares tiene una imagen represenatativa, la cual funciona como un botón, que al darle click me envía a otro activity, en el que se mostrará un pequeño resúmen del lugar y un video representativo. En este activity tenemos la opción de regresar al mapa de América con el botón que se encuentra en la parte inferior del activity.</p>

<p align="center">
  <img src="fondo/Mexico.jpeg" alt="Prueba" width="300" />
</p>

<p align="justify">Una vez que se le dio click en la imagen de lugar turístico que se desea conocer, se abre un activity en el que se carga el fragment correspondiente al lugar. En este fragment se muestra el título en la parte superior, seguido de una descripción breve y un video representativo. Al final tenemos un botón que nos permite regresar al mapa del país en el que visitamos el lugar para poder seguir observando otros lugares.  
</p>

<p align="center">
  <img src="fondo/Vallarta.jpeg" alt="Prueba" width="300" />
</p>

---

## Implementación de Temas con SharedPreferences
<table>
  <tr>
    <th>Elección del país a visitar</th>
    <th>Elección del lugar visitar</th>
    <th>Resúmen del lugar</th>
    <th>Resúmen del lugar</th>
  </tr>
  <tr>
    <th>
      <img src="fondo/themes0.jpg" alt="Prueba" width="300" />
    </th>
    <th>
      <img src="fondo/themes2.jpg" alt="Prueba" width="300" />
    </th>
    <th>
      <img src="fondo/themes4.jpg" alt="Prueba" width="300" />
    </th>
    <th>
      <img src="fondo/themes7.jpg" alt="Prueba" width="300" />
    </th>
  </tr>
  <tr>
    <th>
      <img src="fondo/themes1.jpg" alt="Prueba" width="300" />
    </th>
    <th>
      <img src="fondo/themes3.jpg" alt="Prueba" width="300" />
    </th>
    <th>
      <img src="fondo/themes5.jpg" alt="Prueba" width="300" />
    </th>
    <th>
      <img src="fondo/themes6.jpg" alt="Prueba" width="300" />
    </th>
  </tr>
</table>

---

## Descripción de cómo se implementó la funcionalidad de cambio de tema

<p align="justify">La implementación realizada consiste en un sistema para alternar entre el modo claro y el modo oscuro dentro de una aplicación Android, aprovechando el uso de SharedPreferences para mantener el estado del tema incluso después de cerrar la aplicación. Se empieza obteniendo una referencia al archivo de preferencias, desde donde se consulta un valor booleano que indica si el modo nocturno está activado o no. En función de ese valor, antes de crear la interfaz de usuario, se configura el tema global de la aplicación utilizando la clase AppCompatDelegate, la cual permite aplicar el modo claro o el modo oscuro de forma consistente en todas las actividades. 
Después de establecer el tema, se infla el layout principal y se utiliza el botón responsable de cambiar el tema. Este botón cambia su ícono dependiendo del modo actual: si la aplicación se encuentra en modo oscuro, se muestra un ícono de sol que indica la posibilidad de volver al modo claro, mientras que si el tema activo es el claro, se muestra un ícono de luna que representa la opción de activar el modo oscuro.
Cuando el usuario presiona el botón, se vuelve a consultar el estado almacenado en las preferencias para determinar qué acción ejecutar. Si el modo oscuro está habilitado, la aplicación cambia a modo claro mediante la función de AppCompatDelegate y actualiza el ícono para reflejar el nuevo estado. En caso contrario, se activa el modo oscuro, se cambia el ícono nuevamente y se modifica el valor guardado en SharedPreferences para que el cambio persista. Finalmente, el editor de preferencias aplica los cambios, asegurando que la próxima vez que se abra la aplicación, esta recuerde el último tema seleccionado y lo aplique automáticamente antes de cargar la interfaz. </p>

---
## Ejemplo de Uso
<table>
  <tr>
    <th>Elección del país a visitar</th>
    <th>Elección del lugar visitar</th>
    <th>Resúmen del lugar</th>
  </tr>
  <tr>
    <th>
      <img src="fondo/America.jpeg" alt="Prueba" width="300" />
    </th>
    <th>
      <img src="fondo/Colombia.jpeg" alt="Prueba" width="300" />
    </th>
    <th>
      <img src="fondo/Cartagena.jpeg" alt="Prueba" width="300" />
    </th>
  </tr>
</table>


---

## Presentación de la aplicación 

https://github.com/user-attachments/assets/397df654-3046-4811-aed0-a5de56668449

---

## Estado de la tarea
- ✅ Tarea finalizada

---

## Características de la aplicación 
- [x] Pantalla de inicio
- [x] Uso de Activities
- [x] Uso de Fragments
- [x] Los botones de la pantalla de inicio me dirigen a otro Activity
- [x] Cada país tiene un Activity diferente
- [x] Los botones de cada país me dirigen a otro Activity
- [x] En le Activity del lugar turístico se carga el Fragment correspondiente
- [x] Uso de Themes para el modo claro y modo oscuro

---

## Acceso al proyecto

<p>Comando para clonar repositorio:</p>
git clone https://github.com/Alfx17/Aventura_Interactiva.git

---

## Tecnologías utilizadas
- Kotlin
- Android Studio

---

## Desarrollador

- Flores Morales Aldahir Andrés
