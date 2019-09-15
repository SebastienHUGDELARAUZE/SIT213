package destinations;

import information.Information;
import information.InformationNonConforme;

/**
 * Classe DestinationFinale se contantant de recevoir les informations et de les stocker
 * @author Lucas
 */
public class DestinationFinale extends Destination<Boolean> {
    @Override
    public void recevoir(Information<Boolean> information) throws InformationNonConforme {
        informationRecue = information;
    }
}
