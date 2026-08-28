# Rodalies Decorations

Mod de Minecraft (**Forge 1.20.1**) con bloques decorativos de temática ferroviaria (Rodalies / Renfe).

## Bloques

- Logos (R de Rodalies, Renfe, Cercanías) y cuarzo con línea púrpura
- Parabrisas de tren (multiblock 3×1 con modelo 3D y cristal translúcido)
- Carteles de estación (Rodalies y genérico) y de número de vía, con texto editable
- **Señales ferroviarias** con texto editable y versiones *short*: velocidad, LTV, fin de LTV,
  cartel de señales, apeadero, silbato y detención inmediata
- Señales de velocidad **apilables** (doble, triple, o velocidad + fin de LTV)
- Bloque **base**, pieza con la que se fabrican las señales

## Wiki

- 📖 **[Crafteos](docs/Crafteos.md)** — cómo se fabrica cada bloque
- 🧩 **[Acumular señales](docs/Acumular-senales.md)** — señales dobles/triples y combinadas

(índice completo en [`docs/`](docs/README.md))

## Desarrollo

Forge 1.20.1 (47.4.10), Java 17.

| Comando | Uso |
|---|---|
| `./gradlew runData` | Genera los assets/data de los data generators |
| `./gradlew build` | Compila el jar en `build/libs/` |
| `./gradlew runClient` | Lanza Minecraft para probar |

Ver [`CLAUDE.md`](CLAUDE.md) para el detalle del proyecto y el flujo para añadir bloques.

## Licencia

All Rights Reserved.
