# Crafteos

## La pieza base

Todas las señales ferroviarias (menos las de estación y el número de vía) se fabrican a partir del
bloque **Base**.

**Base** — una reja de hierro encima de hormigón gris (o su polvo), una sobre la otra
(en cualquier posición de la mesa):

```
[ Reja de hierro ]
[ Hormigón gris  ]   ->  1 Base
```

> El hormigón gris se puede sustituir por **polvo de hormigón gris**.

## Señales (sistema base + tintes)

Cada señal usa la **columna central** con bases y la **fila de arriba** con tintes:

- **Normal**: 2 bases en la columna central. **Short**: 1 base.
- Los tintes van siempre en la fila superior.
- **Cada receta rinde 2 señales.**

### Plantilla

```
Normal:                 Short:
[T] [T] [T]             [T] [T] [T]
    [Base]                  [Base]
    [Base]
```

(donde `[T]` son los tintes de la fila superior según la señal)

### Distribución de tintes por señal

| Señal | Izquierda | Centro | Derecha |
|---|---|---|---|
| Velocidad | Blanco | Blanco | Blanco |
| LTV | Amarillo | Amarillo | Amarillo |
| Cartel de señales | — | Blanco | — |
| Fin de LTV | — | Amarillo | — |
| Apeadero | Blanco | **Gris** | Blanco |
| Silbato | **Gris** | Blanco | **Gris** |
| Detención inmediata | Blanco | **Rojo** | Blanco |

### Ejemplos

**Señal de velocidad (normal)** — 3 tintes blancos + 2 bases → 2 señales:

```
[Blanco] [Blanco] [Blanco]
         [Base]
         [Base]
```

**Cartel de señales (short)** — 1 tinte blanco + 1 base → 2 señales:

```
   [Blanco]
   [Base]
```

**Señal de apeadero (normal)** — blanco/gris/blanco + 2 bases → 2 señales:

```
[Blanco] [Gris] [Blanco]
         [Base]
         [Base]
```

## Señales de estación (sistema propio, no usan base)

**Cartel de estación de Rodalies** (con marca naranja):

```
[Gris] [Gris] [Gris]
[Naranja] [Hierro]
          [Hierro]
```

**Cartel de estación genérico**:

```
[Gris] [Gris] [Gris]
       [Hierro]
       [Hierro]
```

**Cartel de número de vía** (→ 2 carteles):

```
[Gris]
[Hierro]
[Hierro]
```

## Otros bloques

**Parabrisas del 447** (→ 1 parabrisas, coloca 3 celdas):

```
      [Hierro]
[Cristal negro] [Cristal negro] [Cristal negro]
```

**Logo de Cercanías** (→ 4 bloques): tinte rojo rodeado en cruz de smooth quartz.
