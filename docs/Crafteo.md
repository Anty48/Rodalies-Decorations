# Crafteo

Todas las recetas se elaboran en la **mesa de crafteo**. Salvo que se indique lo contrario, las
recetas con forma requieren la disposición mostrada; las sin forma admiten cualquier posición.

## La pieza Base

La mayoría de las señales de vía se fabrican a partir del bloque **Base**: una reja de hierro sobre
hormigón gris (o su polvo), una encima de la otra.

```
[ Reja de hierro ]
[ Hormigón gris  ]   →  1 Base
```

> El hormigón gris puede sustituirse por **polvo de hormigón gris**.

## Señales de vía (sistema Base + tintes)

Las señales de vía usan la **columna central** con bloques Base y la **fila superior** con tintes:

- **Versión normal** — 2 Base en la columna central.
- **Versión corta** — 1 Base en la columna central.
- Los tintes se colocan siempre en la fila superior.
- **Cada receta produce 2 señales.**

```
Normal:                 Corta:
[T] [T] [T]             [T] [T] [T]
    [Base]                  [Base]
    [Base]
```

Distribución de tintes en la fila superior según la señal:

| Señal | Izquierda | Centro | Derecha |
|---|---|---|---|
| Velocidad | Blanco | Blanco | Blanco |
| LTV | Amarillo | Amarillo | Amarillo |
| Cartel de señales | — | Blanco | — |
| Fin de LTV | — | Amarillo | — |
| Apeadero | Blanco | Gris | Blanco |
| Silbato | Gris | Blanco | Gris |
| Detención inmediata | Blanco | Rojo | Blanco |

**Ejemplo — Señal de velocidad (normal):** tres tintes blancos y dos Base → 2 señales.

```
[Blanco] [Blanco] [Blanco]
         [Base]
         [Base]
```

**Ejemplo — Cartel de señales (corto):** un tinte blanco y una Base → 2 señales.

```
   [Blanco]
   [Base]
```

## Carteles de estación

Estos carteles tienen receta propia y no emplean el bloque Base.

**Señal de estación de Rodalies** (→ 1):

```
[Hormigón gris] [Hormigón gris] [Hormigón gris]
[Tinte naranja] [Lingote de hierro]
                [Lingote de hierro]
```

**Señal de estación genérica** (→ 1):

```
[Hormigón gris] [Hormigón gris] [Hormigón gris]
                [Lingote de hierro]
                [Lingote de hierro]
```

**Cartel de número de vía** (→ 2):

```
[Hormigón gris]
[Lingote de hierro]
[Lingote de hierro]
```

## Material rodante

**Parabrisas del tren 447** (→ 1; ocupa 3 celdas al colocarse):

```
              [Lingote de hierro]
[Cristal negro] [Cristal negro] [Cristal negro]
```

**Puerta corredera de tren** (→ 3):

```
[Terracota naranja] [Terracota naranja]
[Cristal negro]     [Cristal negro]
[Terracota naranja] [Terracota naranja]
```

## Logotipos

| Logotipo | Receta | Resultado |
|---|---|---|
| Logo R de Rodalies | Cruz de terracota naranja con cuarzo en el centro | 4 |
| Logo Renfe | Cruz de bloque de cuarzo con tinte púrpura en el centro | 4 |
| Logo de Cercanías | Cruz de cuarzo pulido con tinte rojo en el centro | 4 |
| Cuarzo con línea púrpura | Bloque de cuarzo + tinte púrpura (sin forma) | 1 |

Ejemplo de disposición en cruz (Logo R de Rodalies):

```
      [Terracota naranja]
[Terracota naranja] [Cuarzo] [Terracota naranja]
      [Terracota naranja]
```

## Conversiones 1:1 (sin coste de materiales)

Las siguientes parejas se **intercambian** entre sí mediante recetas sin forma que no consumen
materiales adicionales:

- Cada logotipo ↔ su **variante de una cara**.
- Puerta corredera **(abre adelante)** ↔ puerta corredera **(abre atrás)**.
