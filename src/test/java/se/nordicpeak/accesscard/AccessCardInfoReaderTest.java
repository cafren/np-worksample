package se.nordicpeak.accesscard;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AccessCardInfoReaderTest {


    @Test
    void getAccessCardInfo_onlyValid() {
        String filePath = "C:\\Users\\carol\\IdeaProjects\\np-worksample\\src\\test\\resources\\TestData.xml";
        AccessCardInfoReader accessCardInfoReader = new AccessCardInfoReader(filePath);

        List<AccessCardInfo> accessCardInfos = accessCardInfoReader.getAccessCardInfo();

        assertEquals(1, accessCardInfos.size());

        AccessCardInfo accessCardInfo = accessCardInfos.get(0);

        assertEquals("2", accessCardInfo.getEmployeeID());
        assertEquals("Valid", accessCardInfo.getFirstName());
        assertEquals("Validson", accessCardInfo.getLastName());
    }
}