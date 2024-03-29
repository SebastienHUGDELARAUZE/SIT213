package convertisseurs;

import communs.Outils;
import destinations.DestinationInterface;
import filtres.FiltreAdapte;
import filtres.FiltreMiseEnForme;
import information.Information;
import information.InformationNonConforme;
import transmetteurs.Transmetteur;

/**
 * La classe Recepteur permet de specifier un élément qui sera ensuite ajouté dans une chaine de transmission.
 *
 * Le recepteur récupére en entrer une suite de symbole modulé selon une forme specifier, RZ, NRZ, ou NRZT, et les sors
 * sous la forme de symbole binaire 0 ou 1
 */
public class Recepteur extends Transmetteur<Float,Boolean> {
    private FiltreAdapte filtreAdapte;

    int[] retardCanal;
    Float [] attenuationCanal;
    int nbTrajetIndirect = 0;
    Float amplMin = 0f;
    Float amplMax = 0f;
    
    /**
     * Constructeur du recepteur, spécifiant la forme utilisée
     * @param filtre spécifie la forme à utiliser
     */
	public Recepteur(FiltreMiseEnForme filtre) {
	    filtreAdapte = new FiltreAdapte(filtre);
	}
   public Recepteur(FiltreMiseEnForme filtre, Float [] attenuationCanal, int [] retardCanal, Float amplitudeMin, Float amplitudeMax){
	    filtreAdapte = new FiltreAdapte(filtre);
	    this.attenuationCanal = attenuationCanal;
	    this.retardCanal = retardCanal;
	    nbTrajetIndirect = retardCanal.length;
	    amplMin = amplitudeMin;
	    amplMax = amplitudeMax;
    }

    /**
     * Permet de recuperer l'information, la code et retransmet l'information aux destinations connectees
     * @param information  l'information  reçue
     * @throws InformationNonConforme Cas d'erreur remonté par l'information
     */
    @Override
    public void recevoir(Information<Float> information) throws InformationNonConforme {
	    informationRecue = information;
	    if(nbTrajetIndirect != 0) 
	    	filtreTrajetMultiple();
        decodage();
        emettre();
    }
 

    /**
     * Décode les informations reçues en transformant les donées reçu (modulées par la forme spécifiée)
     * en données binaire
     */
    private void decodage(){
	    informationEmise = new Information<>();
	    Float[] dataIn = informationRecue.getArray();
	    double[] signal = Outils.convertToDoubleArray(dataIn);
	    Boolean[] dataOut = filtreAdapte.appliquer(signal);
	    informationEmise = new Information<>(dataOut);
    }
    
    public void filtreTrajetMultiple() {
    	
    	int nbTrajetIndirect = retardCanal.length;
    	int maxRetard = retardCanal[nbTrajetIndirect - 1];
    	Float[] infoMelangee = informationRecue.getArray();
    	Float[] infoDetectee = new Float[infoMelangee.length - maxRetard];
    
		//Suppression du premier trajet indirect
    	int debut = 0;
    	int fin = retardCanal[0];
		for (int j = debut; j<fin; j++) 
			infoDetectee[j] = infoMelangee[j];
	
		debut = fin;
		fin = (nbTrajetIndirect>1)? retardCanal[1] : infoDetectee.length; 
		for (int j = debut; j<fin; j++) {
			float trajetIndirect1 =(attenuationCanal[0]* infoDetectee[j-retardCanal[0]]);
			infoDetectee[j] = infoMelangee[j] - trajetIndirect1;
			//éviter que les erreurs de calcul se propagent lors de manipulations d'un nombre important d'échantillon
			if(nbTrajetIndirect == 1)
				infoDetectee[j] = (infoDetectee[j] > amplMax)?amplMax:(infoDetectee[j] < amplMin)?amplMin:infoDetectee[j];
		}
		//Suppression du second trajet multiple
		if(nbTrajetIndirect>=2) {
			debut = fin;
			fin = infoDetectee.length; 
			for (int j = debut; j<fin; j++) {
				float trajetIndirect1 =(attenuationCanal[0]* infoDetectee[j-retardCanal[0]]);
				float  trajetIndirect2=(attenuationCanal[1] * infoDetectee[j-retardCanal[1]]);
				infoDetectee[j] = infoMelangee[j] -  trajetIndirect1 -  trajetIndirect2 ;
				//éviter que les erreurs de calcul se propagent lors de manipulations d'un nombre important d'échantillon
				if(nbTrajetIndirect == 2)
					infoDetectee[j] = (infoDetectee[j] > amplMax)?amplMax:(infoDetectee[j] < amplMin)?amplMin:infoDetectee[j];
				
			}}
		//TODO : renvoyer une donnée au lieu de modifier informationRecue
		informationRecue = new Information<Float>(infoDetectee); 

    }

    /**
     * Pour chaque destination connectée, on transmet les informations reçues
     * @throws InformationNonConforme Cas d'erreur remonté par l'information
     */
    @Override
    public void emettre() throws InformationNonConforme {
        for (DestinationInterface<Boolean> destination : destinationsConnectees) {
            destination.recevoir(informationEmise);
        }
    }
}


