package se.nordicpeak.accesscard;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        String xmlPath = "src/main/resources/NordicPeakEmployees.xml";
        AccessCardInfoReader accessCardInfoReader = new AccessCardInfoReader(xmlPath);
        List<AccessCardInfo> accessCardInfos = accessCardInfoReader.getAccessCardInfo();

        AccessCardGenerator accessCardGenerator = new AccessCardGenerator();
        accessCardGenerator.createAccessCards(accessCardInfos);
    }
}