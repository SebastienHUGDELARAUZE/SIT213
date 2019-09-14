package transmetteurs;

import destinations.DestinationInterface;
import information.Information;
import information.InformationNonConforme;
import visualisations.SondeLogique;

/**
 * Classe du transmetteur logique parfait, celui-ci reçois et émet (pour chaque destination qui lui est connectée)
 * des Boolean sans les modifier et sans impacter les informations.
 * Une sonde logique de nom "Sonde sortie transmetteur parfait" lui est connecté en sortie
 * @author Lucas
 */
public class TransmetteurParfait extends Transmetteur <Boolean, Boolean> {
    @Override
    public void recevoir(Information<Boolean> information) throws InformationNonConforme {
        informationRecue = information;
        emettre();
    }

    @Override
    public void emettre() throws InformationNonConforme {
        informationEmise = informationRecue;

        // Création d'une sonde et envoi des informations à émettre en sortie du transmetteur à cette dernière
        SondeLogique sonde = new SondeLogique("Sonde sortie transmetteur parfait", 10);
        sonde.recevoir(informationEmise);

        // Pour chaque destination connectée on envoie les informations à émettre
        for (DestinationInterface<Boolean> destination : destinationsConnectees) {
            destination.recevoir(informationEmise);
        }
    }
}
