/**
 * Implementación del Algoritmo Húngaro (Kuhn-Munkres) para el problema de
 * asignación de máximo beneficio (Maximum Weight Bipartite Matching).
 *
 * En el contexto de este trabajo, la entrada es una matriz de compatibilidad
 * donde cada celda representa el puntaje de afinidad entre un médico y un
 * hospital. El algoritmo encuentra la asignación que MAXIMIZA la suma total
 * de compatibilidades del sistema, sin importar la estabilidad del resultado.
 *
 * Internamente convierte el problema de maximización a minimización restando
 * cada valor del valor máximo de la matriz, y luego aplica los pasos clásicos
 * del algoritmo: reducción de filas y columnas, cobertura de ceros con líneas,
 * ajuste de la matriz y extracción de la asignación óptima.
 *
 * Complejidad temporal: O(n³)
 * Complejidad espacial: O(n²)
 */
public class HungarianAlgorithm {

    /**
     * Ejecuta el Algoritmo Húngaro y devuelve la asignación de máxima compatibilidad.
     *
     * @param compatMatrix matriz n×n donde compatMatrix[i][j] es el puntaje de
     *                     compatibilidad entre el médico i y el hospital j
     * @return arreglo matching donde matching[i] = índice del hospital asignado
     *         al médico i
     */
    public static int[] run(int[][] compatMatrix) {
        int n = compatMatrix.length;

        // --- Paso 0: Convertir maximización a minimización ---
        // Se resta cada valor del máximo de la matriz.
        // Así, el par con mayor compatibilidad pasa a tener costo 0 (el mínimo).
        int max = 0;
        for (int[] fila : compatMatrix)
            for (int valor : fila)
                if (valor > max) max = valor;

        int[][] costo = new int[n][n];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                costo[i][j] = max - compatMatrix[i][j];

        // Trabajar sobre una copia para no modificar la matriz original
        int[][] c = new int[n][n];
        for (int i = 0; i < n; i++)
            c[i] = costo[i].clone();

        // --- Paso 1: Reducción de filas ---
        // Restar el valor mínimo de cada fila a todos los elementos de esa fila.
        // Esto garantiza que cada fila tenga al menos un cero.
        for (int i = 0; i < n; i++) {
            int minFila = Integer.MAX_VALUE;
            for (int j = 0; j < n; j++)
                if (c[i][j] < minFila) minFila = c[i][j];
            for (int j = 0; j < n; j++)
                c[i][j] -= minFila;
        }

        // --- Paso 2: Reducción de columnas ---
        // Restar el valor mínimo de cada columna a todos los elementos de esa columna.
        // Esto garantiza que cada columna también tenga al menos un cero.
        for (int j = 0; j < n; j++) {
            int minCol = Integer.MAX_VALUE;
            for (int i = 0; i < n; i++)
                if (c[i][j] < minCol) minCol = c[i][j];
            for (int i = 0; i < n; i++)
                c[i][j] -= minCol;
        }

        // Arreglos auxiliares para el proceso de cobertura y marcado
        int[] filasCubiertas = new int[n];   // 1 si la fila está cubierta, 0 si no
        int[] colsCubiertas  = new int[n];   // 1 si la columna está cubierta, 0 si no
        int[][] marcadoEstrella = new int[n][n]; // ceros "estrellados" (candidatos a asignación)
        int[][] marcadoPrima    = new int[n][n]; // ceros "primados" (auxiliares del algoritmo)

        // --- Paso 3: Marcar con estrella ceros iniciales ---
        // Se estrella un cero de cada fila siempre que su columna no tenga ya un cero estrellado.
        // Esto produce una asignación parcial inicial.
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (c[i][j] == 0 && filasCubiertas[i] == 0 && colsCubiertas[j] == 0) {
                    marcadoEstrella[i][j] = 1;
                    filasCubiertas[i] = 1;
                    colsCubiertas[j] = 1;
                }
            }
        }
        // Limpiar coberturas (solo se usaron temporalmente para el marcado inicial)
        java.util.Arrays.fill(filasCubiertas, 0);
        java.util.Arrays.fill(colsCubiertas, 0);

        // --- Bucle principal ---
        while (true) {

            // --- Paso 4: Cubrir columnas que contienen un cero estrellado ---
            // Cada cero estrellado representa una asignación tentativa.
            for (int i = 0; i < n; i++)
                for (int j = 0; j < n; j++)
                    if (marcadoEstrella[i][j] == 1) colsCubiertas[j] = 1;

            // Contar columnas cubiertas
            int colsCubierdasCount = 0;
            for (int j = 0; j < n; j++) if (colsCubiertas[j] == 1) colsCubierdasCount++;

            // Si todas las columnas están cubiertas, la asignación óptima está completa
            if (colsCubierdasCount == n) break;

            // --- Paso 5: Buscar un cero no cubierto y marcarlo con prima ---
            while (true) {
                int[] ceroCubierto = buscarCeroNoCubierto(c, filasCubiertas, colsCubiertas, n);

                if (ceroCubierto == null) {
                    // --- Paso 6: No hay ceros no cubiertos → ajustar la matriz ---
                    // Encontrar el valor mínimo entre los elementos no cubiertos.
                    // Restarlo de todos los no cubiertos y sumarlo a los cubiertos por dos líneas.
                    // Esto crea nuevos ceros sin destruir los existentes.
                    int minVal = buscarMinNoCubierto(c, filasCubiertas, colsCubiertas, n);
                    for (int i = 0; i < n; i++) {
                        for (int j = 0; j < n; j++) {
                            if (filasCubiertas[i] == 1) c[i][j] += minVal; // cubierto por fila
                            if (colsCubiertas[j] == 0)  c[i][j] -= minVal; // no cubierto por columna
                        }
                    }

                } else {
                    int fila = ceroCubierto[0];
                    int col  = ceroCubierto[1];

                    // Marcar este cero con prima
                    marcadoPrima[fila][col] = 1;

                    // ¿Hay un cero estrellado en la misma fila?
                    int colEstrella = buscarEstellaEnFila(marcadoEstrella, fila, n);

                    if (colEstrella >= 0) {
                        // Sí hay estrella en la fila: cubrir fila, descubrir columna de la estrella
                        filasCubiertas[fila] = 1;
                        colsCubiertas[colEstrella] = 0;

                    } else {
                        // No hay estrella en la fila → aumentar el camino alternante
                        // Esto mejora la asignación actual flippeando estrellados y primados
                        aumentarCamino(marcadoEstrella, marcadoPrima, fila, col, n);

                        // Limpiar coberturas y marcas prima para la siguiente iteración
                        java.util.Arrays.fill(filasCubiertas, 0);
                        java.util.Arrays.fill(colsCubiertas, 0);
                        for (int i = 0; i < n; i++)
                            java.util.Arrays.fill(marcadoPrima[i], 0);
                        break;
                    }
                }
            }
        }

        // --- Extraer asignación final ---
        // Cada cero estrellado representa una asignación médico → hospital
        int[] matching = new int[n];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                if (marcadoEstrella[i][j] == 1) matching[i] = j;

        return matching;
    }

    /**
     * Busca el primer cero no cubierto por ninguna fila ni columna.
     * Devuelve su posición [fila, columna] o null si no existe.
     */
    private static int[] buscarCeroNoCubierto(int[][] c, int[] filas, int[] cols, int n) {
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                if (c[i][j] == 0 && filas[i] == 0 && cols[j] == 0)
                    return new int[]{i, j};
        return null;
    }

    /**
     * Encuentra el valor mínimo entre todos los elementos no cubiertos de la matriz.
     * Se usa en el Paso 6 para ajustar la matriz y crear nuevos ceros.
     */
    private static int buscarMinNoCubierto(int[][] c, int[] filas, int[] cols, int n) {
        int min = Integer.MAX_VALUE;
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                if (filas[i] == 0 && cols[j] == 0 && c[i][j] < min)
                    min = c[i][j];
        return min;
    }

    /**
     * Busca un cero estrellado en la fila indicada.
     * Devuelve el índice de la columna donde está la estrella, o -1 si no existe.
     */
    private static int buscarEstellaEnFila(int[][] estrellados, int fila, int n) {
        for (int j = 0; j < n; j++)
            if (estrellados[fila][j] == 1) return j;
        return -1;
    }

    /**
     * Busca un cero estrellado en la columna indicada.
     * Devuelve el índice de la fila donde está la estrella, o -1 si no existe.
     */
    private static int buscarEstellaEnColumna(int[][] estrellados, int col, int n) {
        for (int i = 0; i < n; i++)
            if (estrellados[i][col] == 1) return i;
        return -1;
    }

    /**
     * Busca un cero primado en la fila indicada.
     * Devuelve el índice de la columna donde está la prima, o -1 si no existe.
     */
    private static int buscarPrimaEnFila(int[][] primados, int fila, int n) {
        for (int j = 0; j < n; j++)
            if (primados[fila][j] == 1) return j;
        return -1;
    }

    /**
     * Aumenta el camino alternante de ceros primados y estrellados.
     *
     * Recorre una cadena que alterna entre ceros primados y estrellados,
     * comenzando en el cero primado sin estrella encontrado en el Paso 5.
     * Al final, invierte el estado de cada elemento del camino:
     * los estrellados se desestrelan y los primados se estrellan.
     *
     * Esto incrementa en uno el número de ceros estrellados (asignaciones),
     * acercando la solución al óptimo.
     */
    private static void aumentarCamino(int[][] estrellados, int[][] primados,
                                       int fila, int col, int n) {
        java.util.List<int[]> camino = new java.util.ArrayList<>();
        camino.add(new int[]{fila, col}); // Primer elemento: cero primado sin estrella

        while (true) {
            // Buscar estrella en la columna del último elemento del camino
            int filaEstrella = buscarEstellaEnColumna(
                    estrellados, camino.get(camino.size() - 1)[1], n);
            if (filaEstrella < 0) break; // No hay más estrellas → fin del camino

            camino.add(new int[]{filaEstrella, camino.get(camino.size() - 1)[1]});

            // Buscar prima en la fila de la estrella recién encontrada
            int colPrima = buscarPrimaEnFila(primados, filaEstrella, n);
            camino.add(new int[]{filaEstrella, colPrima});
        }

        // Invertir el estado de todos los elementos del camino
        for (int[] punto : camino) {
            if (estrellados[punto[0]][punto[1]] == 1)
                estrellados[punto[0]][punto[1]] = 0; // Desestrellar
            else
                estrellados[punto[0]][punto[1]] = 1; // Estrellar
        }
    }
}