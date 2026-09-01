# Integración con Create

Rodalies Decorations ofrece una integración **opcional** con el mod
[Create](https://www.curseforge.com/minecraft/mc-mods/create). El mod **no depende de Create**: si
no está instalado, todo funciona con normalidad.

## Qué aporta

La **puerta corredera de tren** puede montarse en un convoy de Create. Cuando Create está presente,
la puerta se **abre y cierra de forma automática al detenerse el tren en una estación**, empleando
el mismo mecanismo de control de estación que usan las puertas propias de Create.

## Cómo funciona

- La puerta pertenece a la etiqueta de **puertas de madera** de Minecraft. Esto permite que Create
  la reconozca y la accione mientras el bloque viaja sobre un contraption.
- La **animación de apertura** la dibuja el propio *block entity* del mod, tanto en el mundo como a
  bordo del tren en movimiento.
- La apertura automática en estación se activa únicamente si Create está cargado. El mod comprueba
  su presencia antes de registrar el comportamiento, de modo que sus clases nunca se cargan cuando
  Create no está instalado.

## Comportamiento sin Create, o con versiones distintas

| Situación | Resultado |
|---|---|
| Create instalado y compatible | La puerta se abre a mano **y** automáticamente al parar en estación. |
| Create no instalado | La puerta se abre a mano; el resto del mod es idéntico. |
| Create presente pero de versión incompatible | La apertura en estación no se activa; la puerta se abre a mano y conserva su animación. |

En todos los casos la puerta mantiene su funcionamiento manual y su animación. La integración solo
**añade** la apertura automática cuando es posible; nunca es un requisito.

## Requisitos

- Create **0.5** o superior (declarado como dependencia **opcional**).
- No es necesaria ninguna configuración adicional por parte del jugador.
