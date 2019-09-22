import org.apache.commons.cli.*;
import sources.*;
import destinations.*;
import transmetteurs.*;

import information.*;
import visualisations.Sonde;
import visualisations.SondeLogique;

/**
 * La classe Simulateur permet de construire et simuler une cha�ne de
 * transmission compos�e d'une Source, d'un nombre variable de Transmetteur(s)
 * et d'une Destination.
 *
 * @author cousin
 * @author prou
 */
public class Simulateur {

    /**
     * indique si le Simulateur est inhib� par seulement afficher l'aide
     */
    private boolean affichageAide = false;
    /**
     * indique si le Simulateur utilise des sondes d'affichage
     */
    private boolean affichage = false;
    /**
     * indique si le Simulateur utilise un message g�n�r� de mani�re al�atoire
     */
    private boolean messageAleatoire = true;
    /**
     * indique si le Simulateur utilise un germe pour initialiser les g�n�rateurs
     * al�atoires
     */
    private boolean aleatoireAvecGerme = false;
    /**
     * la valeur de la semence utilis�e pour les g�n�rateurs al�atoires
     */
    private Integer seed = null;
    /**
     * la longueur du message al�atoire � transmettre si un message n'est pas impose
     */
    private int nbBitsMess = 100;
    /**
     * la cha�ne de caract�res correspondant � m dans l'argument -mess m
     */
    private String messageString = "100";

    /**
     * le composant Source de la chaine de transmission
     */
    private Source<Boolean> source = null;
    /**
     * le composant Transmetteur parfait logique de la chaine de transmission
     */
    private Transmetteur<Boolean, Boolean> transmetteurLogique = null;
    /**
     * le composant Destination de la chaine de transmission
     */
    private Destination<Boolean> destination = null;

    /**
     * Le constructeur de Simulateur construit une cha�ne de transmission compos�e
     * d'une Source <Boolean>, d'une Destination <Boolean> et de Transmetteur(s)
     * [voir la m�thode analyseArguments]... <br>
     * Les diff�rents composants de la cha�ne de transmission (Source,
     * Transmetteur(s), Destination, Sonde(s) de visualisation) sont cr��s et
     * connect�s.
     *
     * @param args le tableau des diff�rents arguments.
     * @throws ArgumentsException si un des arguments est incorrect
     */
    public Simulateur(String[] args) throws ArgumentsException {
        analyseArguments(args);
        simulationParfaite();
    }

    private void simulationParfaite() throws ArgumentsException {

        // Configuration de la SOURCE ALEATOIRE|FIXE
        if (messageAleatoire) {
            if (aleatoireAvecGerme)
                source = new SourceAleatoire(nbBitsMess, seed);
            else
                source = new SourceAleatoire(nbBitsMess);
        } else {
            try {
                source = new SourceFixe(Information.stringToBoolean(messageString));
            } catch (InformationNonConforme exception) {
                throw new ArgumentsException(exception.toString());
            }
        }

        // Configuration du TRANSMETTEUR PARFAIT
        transmetteurLogique = new TransmetteurParfait<>();
        source.connecter(transmetteurLogique);

        // Configuration de la DESTINATION
        destination = new DestinationFinale();
        transmetteurLogique.connecter(destination);

        if (affichage) {
            // Ajout des SONDES LOGIQUES
            Sonde<Boolean> sonde_entree = new SondeLogique("Entr�e du syst�me", 100);
            source.connecter(sonde_entree);
            Sonde<Boolean> sonde_sortie = new SondeLogique("Sortie du syst�me", 100);
            transmetteurLogique.connecter(sonde_sortie);
        }
    }

    /**
     * La m�thode analyseArguments extrait d'un tableau de cha�nes de caract�res les
     * diff�rentes options de la simulation. Elle met � jour les attributs du
     * Simulateur.
     *
     * @param args Pour plus d'information, lancer le simulateur avec le param�tre '-h'
     * @throws ArgumentsException si un des arguments est incorrect.
     */
    private void analyseArguments(String[] args) throws ArgumentsException {

        // Prepare options
        final Options options = configParameters();
        final CommandLineParser parser = new DefaultParser();
        CommandLine commandLine = null;

        // Parse the options
        try {
            commandLine = parser.parse(options, args);
        } catch (ParseException e) {
            throw new ArgumentsException(e.toString());
        }

        // Process the options
        affichageAide = commandLine.hasOption("h");
        if (affichageAide) {
            final String header = "Simulateur de cha�ne de transmission\n\n";
            final String footer = "\nProjet r�alis� par M.Bartoli, L.Dumestre, O.Gueye, S.HugdeLarauze et F.Ludovic\n";

            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("Simulateur", header, options, footer, true);
            return;
        }

        affichage = commandLine.hasOption("s");

        aleatoireAvecGerme = commandLine.hasOption("seed");
        if (aleatoireAvecGerme) {
            try {
                seed = Integer.parseInt(commandLine.getOptionValue("seed"));
            } catch (Exception e) {
                throw new ArgumentsException("Valeur du parametre -seed  invalide :" + commandLine.getOptionValue("seed"));
            }
        }

        messageAleatoire = commandLine.hasOption("mess");
        if (messageAleatoire) {
            messageString = commandLine.getOptionValue("mess");
            if (messageString.matches("[0,1]{7,}")) {
                messageAleatoire = false;
                nbBitsMess = messageString.length();
            } else if (messageString.matches("[0-9]{1,6}")) {
                messageAleatoire = true;
                nbBitsMess = Integer.parseInt(messageString);
                if (nbBitsMess < 1)
                    throw new ArgumentsException("Valeur du parametre -mess invalide : " + nbBitsMess);
            } else
                throw new ArgumentsException("Valeur du parametre -mess invalide : " + messageString);
        }
    }

    /**
     * Permet de d�finir les options disponibles en ligne de commande
     * @return un objet contenant les diff�rentes options
     */
    private static Options configParameters() {
        final Option sondeOption = Option.builder("s")
                .desc("Active l'utilisation des sondes")
                .build();

        final Option messageOption = Option.builder("mess")
                .desc("Permet de pr�ciser le message ou la longueur du message")
                .hasArg()
                .argName("m")
                .build();

        final Option germeOption = Option.builder("seed")
                .desc("Permet d'initialiser le g�n�rateur al�atoire avec une germe sp�cifique")
                .hasArg()
                .argName("v")
                .build();

        final Option aideOption = Option.builder("h")
                .desc("Affiche la liste des usages")
                .build();

        final Options options = new Options();

        options.addOption(sondeOption);
        options.addOption(messageOption);
        options.addOption(germeOption);
        options.addOption(aideOption);

        return options;
    }

    /**
     * La m�thode execute effectue un envoi de message par la source de la cha�ne de
     * transmission du Simulateur.
     *
     * @throws Exception si un probl�me survient lors de l'ex�cution
     */
    private void execute() throws Exception {
        source.emettre();
    }

    /**
     * La m�thode qui calcule le taux d'erreur binaire en comparant les bits du
     * message �mis avec ceux du message re�u.
     *
     * @return La valeur du Taux d'Erreur Binaire.
     */
    private float calculTauxErreurBinaire() {
        int nb_error = 0;

        Information<Boolean> informationsEmises, informationsRecues;
        informationsEmises = source.getInformationEmise();
        informationsRecues = destination.getInformationRecue();

        for (int i = 0; i < informationsEmises.nbElements(); i++) {
            if (!informationsEmises.iemeElement(i).equals(informationsRecues.iemeElement(i))) {
                nb_error++;
            }
        }

        return (float) nb_error / informationsEmises.nbElements() * 100;
    }

    /**
     * La fonction main instancie un Simulateur � l'aide des arguments param�tres et
     * affiche le r�sultat de l'ex�cution d'une transmission.
     *
     * @param args les diff�rents arguments qui serviront � l'instanciation du
     *             Simulateur.
     */
    public static void main(String[] args) {
        Simulateur simulateur = null;

        try {
            simulateur = new Simulateur(args);
        } catch (Exception e) {
            System.out.println(e);
            System.exit(-1);
        }

        if (simulateur.affichageAide)
            return;

        try {
            simulateur.execute();
            float tauxErreurBinaire = simulateur.calculTauxErreurBinaire();
            String s = "java  Simulateur  ";
            for (int i = 0; i < args.length; i++) {
                s += args[i] + "  ";
            }
            System.out.println(s + "  =>   TEB : " + tauxErreurBinaire);
        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
            System.exit(-2);
        }
    }

}
