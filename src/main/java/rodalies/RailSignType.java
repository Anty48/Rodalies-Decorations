package rodalies;

/**
 * Configuracion de cada tipo de señal ferroviaria (bloque {@link RailSignBlock}).
 * Define cuanto texto admite y como se dibuja. El renderer ({@code RailSignRenderer}) usa
 * el tipo para saber donde y de que color colocar el texto.
 */
public enum RailSignType {
    /** Cartel de numero de via: hasta 3 caracteres, blanco, centrado, visible por AMBOS lados. */
    PLATFORM_NUMBER(3, false),
    /** Señal de velocidad permanente (rombo blanco): hasta 3 digitos, negro, un solo lado. */
    SPEED_LIMIT(3, false),
    /** LVT (rombo amarillo temporal): hasta 3 digitos, negro, un solo lado. */
    LVT(3, false),
    /** Cartel de señales (cuadrado blanco): hasta 2 lineas de texto que se adaptan, un solo lado. */
    NORMAL(40, true),
    /** Fin de LVT (cuadrado amarillo): sin texto. */
    LVT_END(0, false);

    /** Numero maximo de caracteres editables (0 = sin texto / no editable). */
    public final int maxChars;
    /** true = admite 2 lineas (separadas por '\n'); false = una sola linea. */
    public final boolean multiline;

    RailSignType(int maxChars, boolean multiline) {
        this.maxChars = maxChars;
        this.multiline = multiline;
    }

    /** ¿Este tipo lleva texto editable (y por tanto BlockEntity)? */
    public boolean editable() {
        return maxChars > 0;
    }
}
