# Apilado de señales

La **señal de velocidad** puede llevar más señales apiladas por debajo, de forma análoga a como se
acumulan las capas de nieve. El apilado se realiza con **clic derecho**, sosteniendo en la mano la
señal que se desea añadir sobre una señal ya colocada.

> El apilado solo funciona con las señales **normales**, no con las versiones cortas.

## Combinaciones admitidas

| Estado | Cómo se obtiene | Resultado |
|---|---|---|
| Doble de velocidad | Velocidad + clic derecho con otra señal de velocidad | Dos señales de velocidad apiladas |
| Triple de velocidad | Doble + clic derecho con otra señal de velocidad | Tres señales de velocidad apiladas |
| Velocidad + Fin de LTV | Velocidad + clic derecho con Fin de LTV, o Fin de LTV + clic derecho con velocidad | Velocidad arriba, Fin de LTV abajo |

- La progresión de velocidad va de **simple → doble → triple**.
- En la combinada **velocidad + Fin de LTV**, la señal de velocidad queda siempre **en la parte
  superior**, con independencia del orden en que se coloquen.
- No se admiten otras mezclas: por ejemplo, dos señales de velocidad **más** un Fin de LTV no es una
  combinación válida.

## Edición de los textos

Cada señal de velocidad de la pila se edita **por separado**, haciendo clic derecho con la mano
vacía a la altura de la señal correspondiente:

- **Doble** — clic arriba edita la señal superior; clic abajo, la inferior.
- **Triple** — clic arriba, centro y abajo edita cada una de las tres.
- En la combinada con Fin de LTV, el Fin de LTV **no lleva texto**; solo se edita la señal de
  velocidad superior.

## Al romper la pila

Al romper una señal apilada se **devuelven todas las señales originales** que la componen:

| Pila | Objetos devueltos |
|---|---|
| Doble | 2 señales de velocidad |
| Triple | 3 señales de velocidad |
| Velocidad + Fin de LTV | 1 señal de velocidad + 1 Fin de LTV |
