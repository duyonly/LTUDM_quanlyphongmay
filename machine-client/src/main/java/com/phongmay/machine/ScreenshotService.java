package com.phongmay.machine;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.GraphicsEnvironment;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

public class ScreenshotService {
    private Robot robot;

    /**
     * Chụp màn hình chính -> (thu nhỏ nếu rộng hơn maxWidth) -> JPEG -> Base64.
     * Kết quả: {format, screenWidth, screenHeight, width, height, image}
     * screenWidth/Height là kích thước thật của màn hình (dùng cho remote mouse ở tuần 6).
     */
    public synchronized Map<String, Object> capture(int maxWidth, float quality) throws Exception {
        if (GraphicsEnvironment.isHeadless()) {
            throw new IllegalStateException("No display available (headless)");
        }
        if (robot == null) robot = new Robot();
        quality = Math.max(0.1f, Math.min(1.0f, quality));

        Rectangle screen = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        BufferedImage img = scale(robot.createScreenCapture(screen), maxWidth);
        byte[] jpeg = toJpeg(img, quality);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("format", "jpeg");
        result.put("screenWidth", screen.width);
        result.put("screenHeight", screen.height);
        result.put("width", img.getWidth());
        result.put("height", img.getHeight());
        result.put("image", Base64.getEncoder().encodeToString(jpeg));
        return result;
    }

    private static BufferedImage scale(BufferedImage src, int maxWidth) {
        if (maxWidth <= 0 || src.getWidth() <= maxWidth) return src;
        int w = maxWidth;
        int h = src.getHeight() * maxWidth / src.getWidth();
        BufferedImage dst = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = dst.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(src, 0, 0, w, h, null);
        g.dispose();
        return dst;
    }

    private static byte[] toJpeg(BufferedImage img, float quality) throws Exception {
        ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
        ImageWriteParam param = writer.getDefaultWriteParam();
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(quality);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ImageOutputStream ios = ImageIO.createImageOutputStream(bos)) {
            writer.setOutput(ios);
            writer.write(null, new IIOImage(img, null, null), param);
        } finally {
            writer.dispose();
        }
        return bos.toByteArray();
    }
}