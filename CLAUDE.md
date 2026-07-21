# CLAUDE.md

Guía para trabajar en este repositorio. Léela al empezar cada sesión.

## Qué es

**Rodalies Mod** — mod de Minecraft para **Forge 1.20.1** que añade bloques decorativos
temáticos de Rodalies/Renfe. Java 17, mappings `official 1.20.1`.

Datos clave (en `gradle.properties`):
- `mod_id = rodalies`
- `mod_group_id = rodalies` (paquete base `rodalies`)
- Forge `47.4.10`, versión del mod `1.0.0`, autor `Anty48`

## Estructura

```
src/main/java/rodalies/
  RodaliesMod.java            @Mod principal; engancha los DeferredRegister al bus
  ModBlocks.java              Registro de BLOCKS, ITEMS y CREATIVE_TABS
  datagen/                    Data generators (generan los JSON de assets/data)
    DataGenerators.java         @Mod.EventBusSubscriber → GatherDataEvent
    ModBlockStateProvider.java  blockstate + modelo de bloque + modelo de item
    ModBlockLootTables.java     loot tables (el bloque se dropea a sí mismo)
    ModRecipeProvider.java      recetas de crafteo

src/main/resources/
  META-INF/mods.toml          metadatos (usa placeholders ${...} rellenados por Gradle)
  assets/rodalies/
    textures/block/*.png       texturas (fuente de verdad, NO se generan)
    lang/{en_us,es_es,ca_es}.json  traducciones (escritas a mano, NO se generan)
    blockstates|models/        SOLO bloques antiguos (r_logo, renfe_logo, purple_side)
  data/rodalies/
    loot_tables|recipes/       SOLO bloques antiguos

src/generated/resources/       SALIDA de los data generators (se empaqueta en el jar)
```

`build.gradle` añade `src/generated/resources` al sourceSet `main`, así que lo generado
por datagen acaba dentro del mod igual que los recursos escritos a mano.

## Cómo se registran los bloques (patrón actual)

En `ModBlocks.java`, todo con `DeferredRegister`:

1. **Bloque** en `BLOCKS` (`ForgeRegistries.BLOCKS`) — normalmente `new Block(props)`.
   Propiedades vía `BlockBehaviour.Properties.of().strength(...)`.
2. **BlockItem** en `ITEMS` (`ForgeRegistries.ITEMS`), mismo nombre de registro que el bloque.
3. Añadir el item a la pestaña creativa `RODALIES_TAB` en `displayItems`.

Convención de nombres: **el id del bloque = el id del item = el nombre de todos los JSON**.
Deben coincidir exactamente (ej. `cercanias_logo`).

## Assets: dos vías (coexisten)

- **Bloques antiguos** (`r_logo`, `renfe_logo`, `purple_side`): JSON escritos a mano en
  `src/main/resources`. No los toques salvo que haga falta.
- **Bloques nuevos**: se generan con **data generators**. Se registran en las clases de
  `datagen/` y los JSON salen a `src/generated/resources` al ejecutar `runData`.

> **lang** siempre se escribe a mano (no usamos `LanguageProvider`) para evitar colisión de
> `en_us.json` entre `src/main` y `src/generated`. Añade las claves
> `block.rodalies.<id>` e `item.rodalies.<id>` en los tres idiomas.

## Flujo para añadir un bloque nuevo (con datagen)

Ver también la skill `/add-block`, que automatiza esto.

1. Poner las texturas en `src/main/resources/assets/rodalies/textures/block/`.
2. Registrar bloque + BlockItem en `ModBlocks.java` y añadirlo a la pestaña creativa.
3. Añadirlo a los providers de `datagen/`:
   - `ModBlockStateProvider` → modelo (`cubeAll` si 1 textura, `cubeBottomTop` si top/side).
   - `ModBlockLootTables` → `dropSelf(...)` **y** añadirlo a `getKnownBlocks()`.
   - `ModRecipeProvider` → la receta.
4. Añadir las traducciones a los 3 `lang/*.json`.
5. `./gradlew runData` para generar los JSON.
6. `./gradlew build` (o `runClient`) para verificar.

## Bloques avanzados (modelo 3D / transparencia / multiblock)

Para bloques que no son un cubo simple, el bloque **parabrisas de tren** (`parabrisas_447`)
es la referencia:

- **Modelo 3D hecho en Blockbench**: el `.json` se instala a mano en `models/block/`, con las
  rutas de textura corregidas a `rodalies:block/...`. Un modelo puede sobresalir de su celda
  (rango de elementos -16..32) → un bloque puede dibujarse hasta 3 celdas de ancho.
- **Transparencia (cristal tintado / alfa)**: hay que asignar el render layer en el cliente,
  o Forge trata el bloque como sólido y recorta lo de detrás. Ver `ClientSetup.java`:
  `ItemBlockRenderTypes.setRenderLayer(bloque, RenderType.translucent())` (usa `cutout` si son
  huecos 100% transparentes sin tinte). El bloque además necesita `.noOcclusion()`.
- **Multiblock (ocupa varias celdas)**: ver `Parabrisas447Block` — `HorizontalDirectionalBlock`
  con propiedad `part` (left/center/right). La celda central dibuja el modelo completo, las
  laterales usan un modelo vacío (`*_empty.json`) y son solo colisión. `getStateForPlacement`
  comprueba que haya hueco, `setPlacedBy` coloca las celdas vecinas, y `playerWillDestroy`
  rompe las 3 y deja caer 1 item. La blockstate se escribe a mano (facing × part).
  Limitación conocida: solo se propaga la rotura al romper una celda como jugador (no ante
  explosiones/pistones).

## Comandos (Windows / PowerShell)

Usa el wrapper `.\gradlew`:

| Comando | Uso |
|---|---|
| `.\gradlew runData`   | Genera los assets/data de los bloques nuevos → `src/generated/resources` |
| `.\gradlew build`     | Compila y empaqueta el jar en `build/libs/` |
| `.\gradlew runClient` | Lanza Minecraft con el mod para probar en vivo |
| `.\gradlew runServer` | Servidor de pruebas |

Si Gradle se queja de red y las dependencias ya están cacheadas: añade `--offline`.

## Notas / deuda técnica conocida

- En `ModBlocks.java` los bloques antiguos definen una subclase vacía por bloque
  (`Rgrande`, `RenfeLogo`, `PurpleSide`) que son idénticas; los bloques nuevos usan
  `new Block(...)` directamente. Se pueden colapsar las viejas con `/simplify`.
