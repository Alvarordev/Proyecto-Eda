import java.util.LinkedList;
import java.util.Queue;

/**
 * Implementación del Algoritmo de Gale-Shapley para el problema de
 * emparejamiento estable (Stable Matching Problem).
 *
 * En el contexto de este trabajo, los médicos actúan como proponentes
 * y los hospitales como receptores. El algoritmo garantiza que el
 * emparejamiento resultante sea ESTABLE: no existe ningún par
 * (médico, hospital) tal que ambos se prefieran mutuamente más
 * que a sus asignaciones actuales. Este par inexistente se denomina
 * "par bloqueante".
 *
 * Complejidad temporal: O(n²)
 * Complejidad espacial: O(n²)
 */
public class GaleShapley {

    /**
     * Ejecuta el algoritmo de Gale-Shapley y devuelve un emparejamiento estable.
     *
     * @param doctorPrefs   doctorPrefs[i] = hospitales ordenados por preferencia
     *                      del médico i (el más preferido en la posición 0)
     * @param hospitalPrefs hospitalPrefs[j] = médicos ordenados por preferencia
     *                      del hospital j (el más preferido en la posición 0)
     * @param n             número de médicos (igual al número de hospitales)
     * @return arreglo matching donde matching[i] = índice del hospital asignado
     *         al médico i
     */
    public static int[] run(int[][] doctorPrefs, int[][] hospitalPrefs, int n) {

        // doctorMatch[i] = hospital asignado al médico i (-1 si está libre)
        int[] doctorMatch = new int[n];

        // hospitalMatch[j] = médico asignado al hospital j (-1 si está libre)
        int[] hospitalMatch = new int[n];

        // nextProposal[i] = índice del siguiente hospital al que el médico i
        // debe proponer (avanza cada vez que el médico hace una propuesta)
        int[] nextProposal = new int[n];

        // hospitalRank[j][i] = posición del médico i en la lista de preferencias
        // del hospital j. Un valor menor indica mayor preferencia.
        // Se precalcula para comparar preferencias en tiempo O(1) durante el algoritmo.
        int[][] hospitalRank = new int[n][n];

        // Inicializar todos los médicos y hospitales como libres
        java.util.Arrays.fill(doctorMatch, -1);
        java.util.Arrays.fill(hospitalMatch, -1);

        // Construir la tabla de ranking inverso para los hospitales
        // hospitalPrefs[j][rank] = médico → hospitalRank[j][médico] = rank
        for (int j = 0; j < n; j++) {
            for (int rank = 0; rank < n; rank++) {
                hospitalRank[j][hospitalPrefs[j][rank]] = rank;
            }
        }

        // Cola de médicos libres que aún tienen hospitales a los que proponer
        Queue<Integer> freeDoctors = new LinkedList<>();
        for (int i = 0; i < n; i++) freeDoctors.add(i);

        // Bucle principal: continúa mientras haya médicos sin asignar
        while (!freeDoctors.isEmpty()) {
            int doctor = freeDoctors.poll();

            // El médico propone al siguiente hospital en su lista de preferencias
            int hospital = doctorPrefs[doctor][nextProposal[doctor]];
            nextProposal[doctor]++; // Avanzar al siguiente hospital para futuras rondas

            if (hospitalMatch[hospital] == -1) {
                // El hospital está libre: acepta provisionalmente al médico
                doctorMatch[doctor] = hospital;
                hospitalMatch[hospital] = doctor;

            } else {
                // El hospital ya tiene un médico asignado provisionalmente
                int currentDoctor = hospitalMatch[hospital];

                // Comparar: ¿el hospital prefiere al nuevo médico sobre el actual?
                if (hospitalRank[hospital][doctor] < hospitalRank[hospital][currentDoctor]) {
                    // El hospital prefiere al nuevo médico → acepta al nuevo y rechaza al actual
                    doctorMatch[doctor] = hospital;
                    hospitalMatch[hospital] = doctor;
                    doctorMatch[currentDoctor] = -1;      // El médico actual queda libre
                    freeDoctors.add(currentDoctor);       // Vuelve a la cola para proponer

                } else {
                    // El hospital prefiere al médico actual → rechaza al nuevo médico
                    // El nuevo médico vuelve a la cola para proponer al siguiente hospital
                    freeDoctors.add(doctor);
                }
            }
        }

        return doctorMatch;
    }
}