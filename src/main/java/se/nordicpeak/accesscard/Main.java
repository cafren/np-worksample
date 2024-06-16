package se.nordicpeak.accesscard;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
        String xmlPath = "src/main/resources/NordicPeakEmployees.xml";
        AccessCardInfoReader accessCardInfoReader = new AccessCardInfoReader(xmlPath);
        List<AccessCardInfo> accessCardInfos = accessCardInfoReader.getAccessCardInfo();

        AccessCardGenerator accessCardGenerator = new AccessCardGenerator();
        accessCardGenerator.createAccessCards(accessCardInfos);
    }
}