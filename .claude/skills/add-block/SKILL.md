---
name: add-block
description: Añade un bloque decorativo nuevo al mod Rodalies (Forge 1.20.1) — registro en Java, data generators (blockstate, modelo, loot, receta), lang y verificación. Úsala cuando el usuario quiera crear/añadir un bloque o ítem-bloque nuevo.
---

# Añadir un bloque nuevo a Rodalies

Automatiza el patrón del repo para crear un bloque decorativo. Sigue estos pasos en orden.
Consulta `CLAUDE.md` para el contexto general del proyecto.

## 0. Recoge los datos necesarios

Antes de tocar código, asegúrate de tener (pregunta al usuario lo que falte):

- **id** del bloque en `snake_case` (ej. `cercanias_logo`). Será el id del bloque, del item
  y el nombre de TODOS los JSON. Debe coincidir en todo.
- **Texturas**: ¿una sola (`cubeAll`) o top/bottom + side (`cubeBottomTop`)? Localiza los PNG
  (a veces el usuario los deja en la raíz del proyecto). Muévelos a
  `src/main/resources/assets/rodalies/textures/block/` con nombres coherentes.
- **Dureza / comportamiento**: valor de `strength(...)` o "como X bloque vanilla".
- **Receta**: forma (shaped/shapeless), ingredientes y cantidad de salida.
- **Nombres** para las traducciones en `en_us`, `es_es`, `ca_es`.

## 1. Texturas

Copia/mueve los PNG a `src/main/resources/assets/rodalies/textures/block/`.
Convención: `<id>.png` para 1 textura; `<id>_top_bottom.png` + `<id>_side.png` para dos.

## 2. Registro en `src/main/java/rodalies/ModBlocks.java`

Añade (usa `new Block(...)` directamente, no crees subclase):

```java
public static final RegistryObject<Block> <UPPER> =
    BLOCKS.register("<id>", () -> new Block(BlockBehaviour.Properties.of().strength(<dureza>f)));

public static final RegistryObject<Item> <UPPER>_ITEM =
    ITEMS.register("<id>", () -> new BlockItem(<UPPER>.get(), new Item.Properties()));
```

Y añádelo a `RODALIES_TAB` dentro de `displayItems`:
```java
output.accept(<UPPER>_ITEM.get());
```

## 3. Data generators (`src/main/java/rodalies/datagen/`)

- **`ModBlockStateProvider.registerStatesAndModels()`**: genera modelo + item.
  - 1 textura: `simpleBlockWithItem(ModBlocks.<UPPER>.get(), cubeAll(ModBlocks.<UPPER>.get()));`
  - top/side: crea el modelo `models().cubeBottomTop("<id>", side, bottom, top)` y pásalo a
    `simpleBlockWithItem(...)`. El modelo de item hereda del de bloque automáticamente.
- **`ModBlockLootTables`**: `dropSelf(ModBlocks.<UPPER>.get());` en `generate()` **y** añade el
  bloque a la lista que devuelve `getKnownBlocks()` (si no, la validación falla).
- **`ModRecipeProvider.buildRecipes(...)`**: añade la receta con `ShapedRecipeBuilder` /
  `ShapelessRecipeBuilder`, `RecipeCategory.BUILDING_BLOCKS`, la cantidad de salida y un
  `.unlockedBy("has_...", has(<ingrediente>))`.

## 4. Traducciones (a mano, NO datagen)

Añade a `assets/rodalies/lang/en_us.json`, `es_es.json` y `ca_es.json`:
```json
"block.rodalies.<id>": "<Nombre>",
"item.rodalies.<id>":  "<Nombre>",
```

## 5. Generar y verificar

```
.\gradlew runData     # genera los JSON en src/generated/resources
.\gradlew build       # confirma que compila y empaqueta
```

Comprueba que aparecieron en `src/generated/resources`:
`blockstates/<id>.json`, `models/block/<id>.json`, `models/item/<id>.json`,
`loot_tables/blocks/<id>.json`, `recipes/<id>.json`.

Si `runData` se queja de una textura inexistente, revisa el paso 1 (el nombre del PNG debe
coincidir con el `ResourceLocation` usado en el provider).
