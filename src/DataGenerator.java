import java.util.Random;

/**
 * Clase utilitaria para generar los datos de entrada del experimento.
 *
 * Actúa como la fuente única de datos para ambos algoritmos:
 * a partir de una misma matriz de compatibilidad, se derivan
 * tanto las listas de preferencias (para Gale-Shapley) como
 * la matriz de costos (para el Algoritmo Húngaro), garantizando
 * una comparación justa entre ambos.
 */
public class DataGenerator {

    /**
     * Genera una matriz de compatibilidad aleatoria de tamaño n×n.
     * Cada celda matrix[i][j] representa el puntaje de compatibilidad
     * entre el médico i y el hospital j, con valores en el rango [1, 100].
     *
     * Se usa una semilla fija (seed) para que los resultados sean
     * reproducibles: la misma semilla siempre produce la misma matriz.
     *
     * @param n    número de médicos (igual al número de hospitales)
     * @param seed semilla para el generador de números aleatorios
     * @return matriz de compatibilidad de tamaño n×n
     */
    public static int[][] generateCompatibilityMatrix(int n, long seed) {
        Random random = new Random(seed);
        int[][] matrix = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                // Valores entre 1 y 100 inclusive
                matrix[i][j] = random.nextInt(100) + 1;
            }
        }
        return matrix;
    }

    /**
     * Deriva las listas de preferencias de los médicos a partir de la
     * matriz de compatibilidad.
     *
     * Cada médico prefiere el hospital con el que tiene mayor puntaje
     * de compatibilidad. Las listas se ordenan de mayor a menor preferencia.
     *
     * Ejemplo: si el médico 0 tiene puntajes [80, 30, 60] con los hospitales
     * 0, 1 y 2, su lista de preferencias será [0, 2, 1].
     *
     * @param compatMatrix matriz de compatibilidad n×n
     * @return doctorPrefs[i] = arreglo de índices de hospitales ordenados
     *         por preferencia del médico i (el más preferido primero)
     */
    public static int[][] deriveDoctorPreferences(int[][] compatMatrix) {
        int n = compatMatrix.length;
        int[][] doctorPrefs = new int[n][n];

        for (int i = 0; i < n; i++) {
            // Crear arreglo de índices [0, 1, 2, ..., n-1]
            Integer[] hospitals = new Integer[n];
            for (int j = 0; j < n; j++) hospitals[j] = j;

            // Ordenar índices por puntaje de compatibilidad de mayor a menor
            final int doctor = i;
            java.util.Arrays.sort(hospitals, (a, b) ->
                    compatMatrix[doctor][b] - compatMatrix[doctor][a]
            );

            // Copiar resultado al arreglo de preferencias del médico i
            for (int j = 0; j < n; j++) doctorPrefs[i][j] = hospitals[j];
        }
        return doctorPrefs;
    }

    /**
     * Deriva las listas de preferencias de los hospitales a partir de la
     * matriz de compatibilidad.
     *
     * Cada hospital prefiere al médico con el que tiene mayor puntaje
     * de compatibilidad. Las listas se ordenan de mayor a menor preferencia.
     *
     * Ejemplo: si el hospital 0 tiene puntajes [50, 90, 70] con los médicos
     * 0, 1 y 2, su lista de preferencias será [1, 2, 0].
     *
     * @param compatMatrix matriz de compatibilidad n×n
     * @return hospitalPrefs[j] = arreglo de índices de médicos ordenados
     *         por preferencia del hospital j (el más preferido primero)
     */
    public static int[][] deriveHospitalPreferences(int[][] compatMatrix) {
        int n = compatMatrix.length;
        int[][] hospitalPrefs = new int[n][n];

        for (int j = 0; j < n; j++) {
            // Crear arreglo de índices [0, 1, 2, ..., n-1]
            Integer[] doctors = new Integer[n];
            for (int i = 0; i < n; i++) doctors[i] = i;

            // Ordenar índices por puntaje de compatibilidad de mayor a menor
            final int hospital = j;
            java.util.Arrays.sort(doctors, (a, b) ->
                    compatMatrix[b][hospital] - compatMatrix[a][hospital]
            );

            // Copiar resultado al arreglo de preferencias del hospital j
            for (int i = 0; i < n; i++) hospitalPrefs[j][i] = doctors[i];
        }
        return hospitalPrefs;
    }

    /**
     * Calcula la compatibilidad total de un emparejamiento.
     * Se suma el puntaje de compatibilidad de cada par médico-hospital
     * asignado. Este valor permite comparar la calidad de la solución
     * producida por cada algoritmo.
     *
     * @param matching     arreglo donde matching[i] = hospital asignado al médico i
     * @param compatMatrix matriz de compatibilidad n×n
     * @return suma total de compatibilidades del emparejamiento
     */
    public static int computeTotalCompatibility(int[] matching, int[][] compatMatrix) {
        int total = 0;
        for (int i = 0; i < matching.length; i++) {
            total += compatMatrix[i][matching[i]];
        }
        return total;
    }
}