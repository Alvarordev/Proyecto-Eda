/**
 * Clase principal del proyecto: Comparación de Algoritmos de Asignación
 * Gale-Shapley vs Algoritmo Húngaro.
 *
 * Problema modelado: Asignación de médicos residentes a hospitales.
 *
 * Ambos algoritmos resuelven el mismo problema de asignación bipartita
 * pero desde perspectivas distintas:
 *   - Gale-Shapley: produce un emparejamiento ESTABLE usando listas de preferencias.
 *                   Ningún médico y hospital se prefieren mutuamente más que
 *                   a sus asignaciones actuales. Complejidad: O(n²).
 *   - Algoritmo Húngaro: produce un emparejamiento de MÁXIMA COMPATIBILIDAD
 *                   usando una matriz de puntajes numéricos. Maximiza la suma
 *                   total de compatibilidades del sistema. Complejidad: O(n³).
 *
 * Ambos algoritmos reciben entrada equivalente derivada de la misma
 * matriz de compatibilidad, garantizando una comparación justa.
 *
 * El programa realiza dos etapas:
 *   1. Verificación de correctitud con el ejemplo pequeño (n=3) del Marco Teórico.
 *   2. Experimento completo midiendo tiempos para n ∈ {10, 50, 100, 250, 500, 750, 1000}.
 *
 * Para compilar y ejecutar:
 *   javac *.java
 *   java Main
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("Comparación de Algoritmos: Gale-Shapley vs Algoritmo Húngaro");
        System.out.println("Problema: Asignación de Médicos Residentes a Hospitales\n");

        // --- Etapa 1: Verificación de correctitud ---
        // Se comprueba que ambos algoritmos producen el resultado esperado
        // usando los ejemplos del Marco Teórico (secciones 4.2.2 y 4.3.2).
        System.out.println("--- Verificación de correctitud (n=3) ---");
        verificarEjemploPequenio();

        // --- Etapa 2: Experimento comparativo ---
        // Se ejecutan ambos algoritmos con múltiples tamaños de entrada
        // y se registran tiempos y calidad de la solución.
        System.out.println("\n--- Experimento: Comparación de Tiempo y Compatibilidad ---");
        ExperimentRunner.run();
    }

    /**
     * Verifica la correctitud de ambos algoritmos usando los ejemplos del Marco Teórico.
     *
     * Para Gale-Shapley se usa el ejemplo de la sección 4.2.2:
     *   Resultado esperado: M1→H2, M2→H1, M3→H3
     *
     * Para el Algoritmo Húngaro se usa el ejemplo de la sección 4.3.2:
     *   Matriz de compatibilidad: [[8,5,6],[4,7,3],[6,4,9]]
     *   Resultado esperado: M1→H1, M2→H2, M3→H3 con compatibilidad total = 24
     */
    private static void verificarEjemploPequenio() {
        int n = 3;

        // Listas de preferencias del ejemplo 4.2.2
        int[][] preferenciasMedicos = {
                {0, 1, 2}, // Médico 1: prefiere H1 > H2 > H3
                {0, 2, 1}, // Médico 2: prefiere H1 > H3 > H2
                {1, 0, 2}  // Médico 3: prefiere H2 > H1 > H3
        };
        int[][] preferenciasHospitales = {
                {1, 0, 2}, // Hospital 1: prefiere M2 > M1 > M3
                {0, 2, 1}, // Hospital 2: prefiere M1 > M3 > M2
                {0, 1, 2}  // Hospital 3: prefiere M1 > M2 > M3
        };

        // Ejecutar Gale-Shapley y mostrar resultado
        int[] resultadoGS = GaleShapley.run(preferenciasMedicos, preferenciasHospitales, n);
        System.out.print("Gale-Shapley resultado: ");
        for (int i = 0; i < n; i++) {
            System.out.printf("M%d→H%d  ", i + 1, resultadoGS[i] + 1);
        }
        System.out.println();
        System.out.println("Esperado:               M1→H2  M2→H1  M3→H3");

        // Matriz de compatibilidad del ejemplo 4.3.2
        int[][] matrizCompat = {
                {8, 5, 6},
                {4, 7, 3},
                {6, 4, 9}
        };

        // Ejecutar Algoritmo Húngaro y mostrar resultado
        int[] resultadoHU = HungarianAlgorithm.run(matrizCompat);
        System.out.print("Algoritmo Húngaro resultado: ");
        for (int i = 0; i < n; i++) {
            System.out.printf("M%d→H%d  ", i + 1, resultadoHU[i] + 1);
        }
        System.out.println();
        System.out.println("Esperado:                    M1→H1  M2→H2  M3→H3");

        // Verificar compatibilidad total
        int compatTotal = DataGenerator.computeTotalCompatibility(resultadoHU, matrizCompat);
        System.out.println("Compatibilidad total: " + compatTotal + " (esperado: 24)");
    }
}