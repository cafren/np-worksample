package se.nordicpeak.accesscard;

import se.unlogic.standardutils.xml.XMLParser;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Objects;


public class GraphicsUtil {
    public static BufferedImage drawAccessCard(AccessCardInfo accessCardInfo) {
        try {
            BufferedImage templateImage = ImageIO.read(Objects.requireNonNull(Main.class.getResourceAsStream("/Passerkort.png")));
            Graphics2D g2d = templateImage.createGraphics();

            g2d.setColor(Color.BLACK);

            Font font = new Font("Arial", Font.PLAIN, 21);
            g2d.setFont(font);

            drawVerticalString(g2d, accessCardInfo.getEmployeeID(), 145, 265);
            drawVerticalString(g2d, accessCardInfo.getEmploymentDate(), 470, 270);
            g2d.drawString(accessCardInfo.getFirstName(), 150, 530);
            g2d.drawString(accessCardInfo.getLastName(), 150, 600);
            g2d.drawString(accessCardInfo.getDepartmentName(), 150, 670);
            g2d.drawString(accessCardInfo.getLocation(), 150, 740);

            String base64Image = accessCardInfo.getProfileImage();
            if (base64Image != null && !base64Image.isEmpty()) {
                //If decodeBase...throws exception... it will break? so this should be in a try/catch???
                BufferedImage profileImage = decodeBase64ToImage(base64Image);
                if (profileImage != null) {
                    g2d.drawImage(profileImage, 152, 105, 295, 320, null);
                }
            }

            g2d.dispose();

            return templateImage;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static BufferedImage decodeBase64ToImage(String base64String) throws IOException {
        base64String = base64String.trim();
        byte[] imageBytes = Base64.getDecoder().decode(base64String);
        ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
        return ImageIO.read(bis);
    }
    public static void drawVerticalString(Graphics2D g2d, String text, int x, int y) {
        AffineTransform originalTransform = g2d.getTransform();
        g2d.rotate(Math.toRadians(-90), x, y);
        g2d.drawString(text, x, y);
        g2d.setTransform(originalTransform);
    }


}
