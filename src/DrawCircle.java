
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;
import javax.imageio.ImageIO;

public class DrawCircle {

    public static void main(String[] args) {
        BufferedImage img = null;
        Graphics g = null;
        BufferedReader buffReader = null;
        String line, token;
        StringTokenizer st = null;
        int x, y;
        try {
            buffReader = new BufferedReader(new FileReader(args[1]));
            img = ImageIO.read(new File(args[0]));
            g = img.getGraphics();
            g.drawImage(img, 0, 0, null);
            while (true) {
                line = buffReader.readLine();
                if (line == null) {
                    break;
                }
                st = new StringTokenizer(line);
                x = Math.round(Float.parseFloat(st.nextToken()));
                y = Math.round(Float.parseFloat(st.nextToken()));
                //token = st.nextToken();

                //if (token.equals("correct")) {
                    g.setColor(Color.WHITE);
                /*} else if (token.equals("wrong")){
                    g.setColor(Color.RED);
                }
                else {
                    g.setColor(Color.BLACK);
                }*/
                g.drawOval(x, y, 3, 3);
            }
            buffReader.close();
            ImageIO.write(img, "png", (new File(args[0] + ".png")));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
