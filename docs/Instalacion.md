# Instalación

## Requisitos

| Requisito | Versión |
|---|---|
| Minecraft | 1.20.1 |
| Minecraft Forge | 47.4.10 o superior |
| Java | 17 |

## Instalación del mod

1. Instala **Minecraft Forge 1.20.1** (versión 47.4.10 o superior) desde el
   [instalador oficial de Forge](https://files.minecraftforge.net/).
2. Descarga el archivo `rodalies-<versión>.jar` desde la sección
   [Releases](https://github.com/Anty48/Rodalies-Decorations/releases) del repositorio.
3. Copia el `.jar` en la carpeta `mods` de tu instalación de Minecraft:
   - Windows: `%APPDATA%\.minecraft\mods`
   - macOS: `~/Library/Application Support/minecraft/mods`
   - Linux: `~/.minecraft/mods`
4. Inicia el juego con el perfil de Forge 1.20.1.

El mod funciona tanto en **cliente** como en **servidor**. Para partidas multijugador debe estar
instalado en ambos lados.

## Compatibilidad con Create

La integración con [Create](https://www.curseforge.com/minecraft/mc-mods/create) es **opcional**.
Si Create está presente, la puerta corredera de tren se abre de forma automática al detenerse el
convoy en una estación. Si no lo está, el mod funciona con normalidad y la puerta se abre a mano.
Consulta **[Integración con Create](Integracion-con-Create.md)** para más detalles.

## Compilación desde el código fuente

El repositorio incluye el MDK de Forge. Para generar el `.jar` desde el código:

```bash
./gradlew build
```

El artefacto resultante se genera en `build/libs/`.

> La integración opcional con Create se compila contra las bibliotecas de Create ubicadas en la
> carpeta local `libs/` (dependencias `compileOnly`). Dichas bibliotecas no se distribuyen con el
> repositorio; sin ellas el resto del mod compila con normalidad, pero deberás retirar el módulo de
> compatibilidad o aportar tú los `.jar` para reconstruir esa parte.
