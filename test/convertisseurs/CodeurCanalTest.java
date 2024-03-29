package convertisseurs;

import destinations.Destination;
import destinations.DestinationFinale;
import information.Information;
import information.InformationBooleen;
import information.InformationNonConforme;
import org.junit.Test;
import sources.Source;
import sources.SourceFixe;

import static org.junit.Assert.*;

public class CodeurCanalTest {

    @Test
    public void nominalTest() throws InformationNonConforme {
        // ARRANGE : expected - actual
        String emitedBoolean = "01010011";
        Information<Boolean> emitedInfo = new InformationBooleen(emitedBoolean);
        String expectedBoolean = "010101010101010010101101";
        Information<Boolean> expectedInfo = new InformationBooleen(expectedBoolean);

        Source<Boolean> source = new SourceFixe(emitedInfo);
        CodeurCanal codageCanal = new CodeurCanal();
        Destination<Boolean> destination = new DestinationFinale();
        // Connecting
        source.connecter(codageCanal);
        codageCanal.connecter(destination);

        // ACT
        source.emettre();
        Information<Boolean> actualInfo = destination.getInformationRecue();

        // ASSERT
        assertEquals(expectedInfo, actualInfo);
    }
}