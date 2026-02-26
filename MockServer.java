import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import javax.imageio.ImageIO;

public class MockServer {
    static int frameCount = 0;

    public static void main(String[] args) throws Exception {
        int port = 8888;
        System.out.println("Mock Android server starting on port " + port + "...");
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Waiting for client connection...");
        Socket socket = serverSocket.accept();
        System.out.println("Client connected!");

        OutputStream out = socket.getOutputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        new Thread(() -> {
            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("Received command: " + line);
                }
            } catch (IOException e) {
                System.out.println("Reader stopped");
            }
        }).start();

        while (!socket.isClosed()) {
            BufferedImage img = createFrame(frameCount++);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(img, "jpg", baos);
            byte[] jpegData = baos.toByteArray();

            out.write(2);
            out.write((jpegData.length >> 24) & 0xFF);
            out.write((jpegData.length >> 16) & 0xFF);
            out.write((jpegData.length >> 8) & 0xFF);
            out.write(jpegData.length & 0xFF);
            out.write(jpegData);
            out.flush();

            Thread.sleep(100);
        }
    }

    static BufferedImage createFrame(int frame) {
        int w = 540, h = 960;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color bg1 = new Color(30, 30, 60);
        Color bg2 = new Color(60, 30, 90);
        GradientPaint gradient = new GradientPaint(0, 0, bg1, w, h, bg2);
        g.setPaint(gradient);
        g.fillRect(0, 0, w, h);

        g.setColor(new Color(255, 255, 255, 40));
        g.fillRoundRect(20, 30, w - 40, 60, 15, 15);
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 20));
        g.drawString("12:34  Android Screen Share", 40, 68);

        int time = frame % 360;
        for (int i = 0; i < 6; i++) {
            int row = i / 3;
            int col = i % 3;
            int x = 30 + col * (w - 60) / 3;
            int y = 150 + row * 180;
            int size = 120;

            float hue = (time + i * 60) / 360.0f;
            Color c = Color.getHSBColor(hue, 0.7f, 0.9f);
            g.setColor(c);
            g.fillRoundRect(x, y, size, size, 20, 20);
            g.setColor(Color.WHITE);
            g.setFont(new Font("SansSerif", Font.PLAIN, 14));
            String[] labels = {"Camera", "Photos", "Settings", "Music", "Maps", "Clock"};
            FontMetrics fm = g.getFontMetrics();
            g.drawString(labels[i], x + (size - fm.stringWidth(labels[i])) / 2, y + size + 18);
        }

        int ballX = w / 2 + (int)(150 * Math.cos(Math.toRadians(frame * 3)));
        int ballY = 620 + (int)(80 * Math.sin(Math.toRadians(frame * 3)));
        Color ballColor = Color.getHSBColor((frame % 100) / 100.0f, 0.8f, 1.0f);
        g.setColor(ballColor);
        g.fillOval(ballX - 25, ballY - 25, 50, 50);

        g.setColor(new Color(255, 255, 255, 60));
        g.fillRoundRect(20, h - 100, w - 40, 70, 15, 15);
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 16));
        g.drawString("Frame: " + frame, 40, h - 60);

        g.dispose();
        return img;
    }
}
