package se.nordicpeak.accesscard;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class AccessCardGenerator {

    public AccessCardGenerator() {
    }


    public void createAccessCards(List<AccessCardInfo> accessCardInfos) {
        for (AccessCardInfo accessCardInfo : accessCardInfos) {
            BufferedImage accessCard = GraphicsUtil.drawAccessCard(accessCardInfo);
            saveAccessCard(accessCard, accessCardInfo.getEmployeeID());
        }
    }

    public static void saveAccessCard(BufferedImage image, String employeeID) {
        try {
            String dirPath = "validation_cards/";
            File validationCards = new File(dirPath);
            if (!validationCards.exists()) {
                validationCards.mkdirs();
            }

            File file = new File(validationCards, "validation_card_" + employeeID + ".png");
            ImageIO.write(image, "png", file);

            System.out.println("Image saved successfully at " + file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
