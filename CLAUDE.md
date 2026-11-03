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
| `.\gradlew publishMod` | Publica el jar en CurseForge, Modrinth y GitHub Releases (ver *Publicación*) |

Si Gradle se queja de red y las dependencias ya están cacheadas: añade `--offline`.

## Port NeoForge 1.21.1

El mismo mod (mismo `mod_id = rodalies`, misma versión) existe para **NeoForge 1.21.1** en la carpeta
hermana **`../Rodalies-NeoForge-1.21.1`**. Es otro proyecto Gradle (ModDevGradle, Java 21) y su código
vive en la rama **`neoforge-1.21.1`** de este mismo repo de GitHub, con historia independiente de
`main` (no se mergean entre sí).

- **Se mantiene al día con Forge.** Cada cambio de este repo se porta allí con un merge a 3 bandas
  en un repo temporal: base = commit de Forge de la última sincronización, "ours" = ficheros del port,
  "theirs" = Forge HEAD (solo `src/main/java`, `assets` y `data/minecraft`). Normalizar CRLF→LF en
  los tres lados antes de mergear. Las recetas/loot del port se regeneran con su propio `runData`.
- **Diferencias de API 1.21 ya resueltas** (aplicarlas al código nuevo): paquetes con
  `PacketDistributor.sendToServer` (no `ModNetwork.CHANNEL`); `useWithoutItem`/`useItemOn` en vez de
  `use`; `codec()` obligatorio en `HorizontalDirectionalBlock`; sin `DistExecutor` (llamar a
  `ClientHooks` bajo `isClientSide`); BlockEntity con `HolderLookup.Provider` en save/load;
  `getRenderBoundingBox` va en el renderer; `ResourceLocation.fromNamespaceAndPath`;
  `Properties.ofFullCopy`; sin `setRenderLayer` (el `render_type` va en el modelo/datagen); UV de
  sprite normalizados 0..1; tags en `data/<ns>/tags/block` (singular); en `Screen`, `super.render`
  ya pinta el fondo.
- El jar del port se llama `rodalies-neoforge-<versión>.jar` (el de Forge, `rodalies-<versión>.jar`).

## Publicación (CurseForge, Modrinth, GitHub)

Ambos proyectos usan el plugin **modpublisher** (bloque `publisher {}` al final de `build.gradle`):

- Un solo proyecto por plataforma para los dos loaders: CurseForge `1500015`, Modrinth `joFpSEnd`
  (`curseforge_id` / `modrinth_id` en `gradle.properties`). `publish_game_versions` fija las
  versiones de Minecraft con las que se etiqueta el archivo.
- Tokens en variables de entorno de usuario de Windows: `CURSE_TOKEN` y `MODRINTH_TOKEN`. GitHub
  usa `gh auth token`. Una sesión abierta antes de crearlos no los ve: leerlos con
  `[Environment]::GetEnvironmentVariable('CURSE_TOKEN','User')` y pasarlos a Gradle.
- Las notas de la versión salen de **`release-notes.md`** (una por proyecto; en inglés, sin
  caracteres raros como flechas). El historial completo está en la página `Version-History` de la
  wiki de GitHub (repo aparte `Rodalies-Decorations.wiki.git`); `changelog.txt` es el del MDK.
- Simular sin subir nada: `.\gradlew publishMod -PpublishDryRun`.
- La release de GitHub (`v<versión>`, título `v<versión> — Descripción`) la comparten los dos jars.
  Si `publishGithub` falla (ya pasó en 2.2.7: se quedó en borrador), terminarla con
  `gh release upload` + `gh release edit --draft=false`.

**Flujo de una versión nueva:** poner en `mod_version` la versión que diga el usuario (en los dos proyectos),
portar los cambios a NeoForge, `build`, commit + push de ambos, actualizar la wiki y
`release-notes.md`, y `publishMod` en los dos (Forge primero).

> **Nunca publiques** (`publishMod`, releases) salvo que el usuario lo diga explícitamente **y** diga a qué
> versión pasarlo. Ver la regla en `../CLAUDE.md` §3.

## Carpeta `things_for_claude/` (área de staging)

Todo lo que el usuario deja para que Claude copie/instale va en **`things_for_claude/`** (carpeta
**ignorada por git**, local). Subcarpetas:

- `blockbench_models/` — exports `.json` de Blockbench (fuente de los modelos; se instalan a mano
  en `models/block/` con las rutas de textura corregidas a `rodalies:block/...`).
- `textures/` — texturas fuente sueltas antes de colocarlas en `assets/rodalies/textures/block/`.
- `screenshots/` — capturas del juego (las "buenas" para la wiki y las de prueba del runClient).
- `create_jars/` — jars de Create dejados para pruebas en runtime.

Si el usuario dice "te he dejado algo", está aquí. No versionar esta carpeta.

## Notas / deuda técnica conocida

- En `ModBlocks.java` los bloques antiguos definen una subclase vacía por bloque
  (`Rgrande`, `RenfeLogo`, `PurpleSide`) que son idénticas; los bloques nuevos usan
  `new Block(...)` directamente. Se pueden colapsar las viejas con `/simplify`.
