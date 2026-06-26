import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Clase encargada de ejecutar el experimento comparativo entre
 * Gale-Shapley y el Algoritmo Húngaro.
 *
 * Para cada tamaño de entrada n definido en SIZES, realiza lo siguiente:
 *   1. Genera la matriz de compatibilidad con semilla fija (reproducibilidad).
 *   2. Deriva las listas de preferencias para Gale-Shapley.
 *   3. Ejecuta cada algoritmo REPETITIONS veces para obtener tiempos estables.
 *   4. Calcula el tiempo promedio y la desviación estándar de cada algoritmo.
 *   5. Registra la compatibilidad total del emparejamiento producido por cada uno.
 *   6. Imprime los resultados en consola y los guarda en un archivo CSV.
 *
 * NOTA: La impresión en consola y el guardado en CSV son independientes.
 * Si el CSV falla (carpeta inexistente, permisos, etc.), los resultados
 * de consola siguen siendo válidos y completos.
 *
 * El CSV generado puede usarse directamente para graficar los resultados
 * en Excel, Python (matplotlib) u otras herramientas.
 */
public class ExperimentRunner {

    // Tamaños de entrada a evaluar (número de médicos = número de hospitales)
    private static final int[] SIZES = {10, 50, 100, 250, 500, 750, 1000};

    // Número de repeticiones por tamaño para promediar y reducir ruido del sistema
    private static final int REPETITIONS = 30;

    // Semilla fija para garantizar que todos los tamaños usen los mismos datos aleatorios
    private static final long SEED = 42L;

    // Ruta del archivo CSV de salida (se guarda en la carpeta raíz del proyecto)
    private static final String OUTPUT_PATH = "results.csv";

    /**
     * Ejecuta el experimento completo para todos los tamaños definidos en SIZES.
     * Primero imprime todos los resultados en consola, luego intenta guardar el CSV.
     * Una falla en el CSV no interrumpe ni oculta los resultados de consola.
     */
    public static void run() {
        // Encabezado de la tabla en consola
        System.out.println("=".repeat(75));
        System.out.printf("%-6s | %-12s | %-12s | %-12s | %-12s | %-8s | %-8s%n",
                "n", "GS avg(ms)", "GS std(ms)", "HU avg(ms)", "HU std(ms)",
                "GS compat", "HU compat");
        System.out.println("=".repeat(75));

        // Arreglos para almacenar resultados en memoria antes de escribir el CSV
        int[]    ns          = new int[SIZES.length];
        double[] promediosGS = new double[SIZES.length];
        double[] desvsGS     = new double[SIZES.length];
        double[] promediosHU = new double[SIZES.length];
        double[] desvsHU     = new double[SIZES.length];
        int[]    compatsGS   = new int[SIZES.length];
        int[]    compatsHU   = new int[SIZES.length];

        for (int idx = 0; idx < SIZES.length; idx++) {
            int n = SIZES[idx];

            // --- Generación de datos de entrada ---
            // Se genera UNA SOLA VEZ por tamaño n para ambos algoritmos,
            // garantizando que compiten sobre exactamente los mismos datos.
            int[][] compatMatrix  = DataGenerator.generateCompatibilityMatrix(n, SEED);
            int[][] doctorPrefs   = DataGenerator.deriveDoctorPreferences(compatMatrix);
            int[][] hospitalPrefs = DataGenerator.deriveHospitalPreferences(compatMatrix);

            // Arreglos para almacenar los tiempos de cada repetición
            double[] tiemposGS = new double[REPETITIONS];
            double[] tiemposHU = new double[REPETITIONS];

            int[] matchingGS = null;
            int[] matchingHU = null;

            // --- Medición de tiempos: Gale-Shapley ---
            // Se mide solo el tiempo de cómputo del algoritmo, excluyendo
            // la generación de datos de entrada.
            for (int r = 0; r < REPETITIONS; r++) {
                long inicio = System.nanoTime();
                matchingGS = GaleShapley.run(doctorPrefs, hospitalPrefs, n);
                long fin = System.nanoTime();
                tiemposGS[r] = (fin - inicio) / 1_000_000.0; // Convertir nanosegundos a milisegundos
            }

            // --- Medición de tiempos: Algoritmo Húngaro ---
            for (int r = 0; r < REPETITIONS; r++) {
                long inicio = System.nanoTime();
                matchingHU = HungarianAlgorithm.run(compatMatrix);
                long fin = System.nanoTime();
                tiemposHU[r] = (fin - inicio) / 1_000_000.0; // Convertir nanosegundos a milisegundos
            }

            // --- Cálculo de estadísticas ---
            double promedioGS   = promedio(tiemposGS);
            double desviacionGS = desviacionEstandar(tiemposGS, promedioGS);
            double promedioHU   = promedio(tiemposHU);
            double desviacionHU = desviacionEstandar(tiemposHU, promedioHU);

            // Compatibilidad total: suma de puntajes de todos los pares asignados
            int compatGS = DataGenerator.computeTotalCompatibility(matchingGS, compatMatrix);
            int compatHU = DataGenerator.computeTotalCompatibility(matchingHU, compatMatrix);

            // --- Imprimir fila en consola (siempre, independiente del CSV) ---
            System.out.printf("%-6d | %-12.3f | %-12.3f | %-12.3f | %-12.3f | %-8d | %-8d%n",
                    n, promedioGS, desviacionGS, promedioHU, desviacionHU,
                    compatGS, compatHU);

            // Guardar resultados de esta iteración en memoria para el CSV
            ns[idx]          = n;
            promediosGS[idx] = promedioGS;
            desvsGS[idx]     = desviacionGS;
            promediosHU[idx] = promedioHU;
            desvsHU[idx]     = desviacionHU;
            compatsGS[idx]   = compatGS;
            compatsHU[idx]   = compatHU;
        }

        System.out.println("=".repeat(75));

        // --- Guardar CSV (separado de la consola para no bloquear resultados) ---
        try (PrintWriter csv = new PrintWriter(new FileWriter(OUTPUT_PATH))) {
            csv.println("n,gs_avg_ms,gs_std_ms,hu_avg_ms,hu_std_ms,gs_compatibility,hu_compatibility");
            for (int idx = 0; idx < SIZES.length; idx++) {
                csv.printf("%d,%.4f,%.4f,%.4f,%.4f,%d,%d%n",
                        ns[idx], promediosGS[idx], desvsGS[idx],
                        promediosHU[idx], desvsHU[idx],
                        compatsGS[idx], compatsHU[idx]);
            }
            System.out.println("Resultados guardados en: " + OUTPUT_PATH);
        } catch (IOException e) {
            System.err.println("Advertencia: no se pudo guardar el CSV: " + e.getMessage());
            System.err.println("Los resultados de consola mostrados arriba son válidos.");
        }
    }

    /**
     * Calcula el promedio de un arreglo de valores decimales.
     *
     * @param valores arreglo de tiempos medidos en milisegundos
     * @return promedio de los valores
     */
    private static double promedio(double[] valores) {
        double suma = 0;
        for (double v : valores) suma += v;
        return suma / valores.length;
    }

    /**
     * Calcula la desviación estándar de un arreglo de valores decimales.
     * Un valor bajo indica que los tiempos medidos son estables entre repeticiones.
     * Un valor alto indica variabilidad, posiblemente por interferencia del sistema operativo.
     *
     * @param valores  arreglo de tiempos medidos en milisegundos
     * @param promedio promedio previamente calculado
     * @return desviación estándar de los valores
     */
    private static double desviacionEstandar(double[] valores, double promedio) {
        double suma = 0;
        for (double v : valores) suma += (v - promedio) * (v - promedio);
        return Math.sqrt(suma / valores.length);
    }
}