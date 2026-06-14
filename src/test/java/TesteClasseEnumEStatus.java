import br.edu.ifsc.fln.model.domain.EStatus;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TesteClasseEnumEStatus {

    @Test
    public void puxarValoresEStatus_Sucesso() {
        assertEquals(3, EStatus.values().length);
    }

}
