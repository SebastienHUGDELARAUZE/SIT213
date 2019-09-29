package communs;

import information.Information;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class Outils {
    /**
     * Permet de déterminer, à partir des amplitudes théoriques, le symbole en fonction de la moyenne passé.
     *
     * @param mean Valeur moyenne du signal.
     * @param amplMin Valeur de la limite basse pour l'amplitude
     * @param amplMax Valeur de la limite haute pour l'amplitude
     * @return Symbole décidé selon la distance entre les amplitudes maximum et minimum.<br>
     * Dans le cas où la valeur moyenne est à équidistance des limites, la valeur false est retournée.
     */
    public static boolean booleanDistance(float mean, float amplMin, float amplMax) {
        float maxDistance = Math.abs(mean - amplMax);
        float minDistance = Math.abs(mean - amplMin);

        return maxDistance < minDistance;
    }

    public static float puissanceMoyenne(Information<Float> signal) {
        float power = 0;  // Initialisation somme des échantillons
        for (Float datum: signal) {
            power += Math.pow(datum, 2);
        }
        return power / signal.nbElements();
    }

    public static float ecartType(float puissanceMoyenne, float snr) {
        double denom = 2f * Math.exp(snr / 10f);
        return (float) Math.sqrt(puissanceMoyenne / denom);
    }

    public static String[] concatenate(String[] ... parms) {
        // calculate size of target array
        int size = 0;
        for (String[] array : parms) {
            size += array.length;
        }

        String[] result = new String[size];

        int j = 0;
        for (String[] array : parms) {
            for (String s : array) {
                result[j++] = s;
            }
        }
        return result;
    }
}
