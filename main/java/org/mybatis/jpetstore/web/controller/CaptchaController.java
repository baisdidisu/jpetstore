package org.mybatis.jpetstore.web.controller;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CaptchaController {

  public static final String CAPTCHA_SESSION_KEY = "captcha";

  @GetMapping("/captcha")
  public void captcha(HttpSession session, HttpServletResponse response) throws IOException {
    int width = 110;
    int height = 36;
    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    Graphics2D g = image.createGraphics();

    try {
      Random random = new Random();
      g.setColor(Color.WHITE);
      g.fillRect(0, 0, width, height);

      for (int i = 0; i < 10; i++) {
        g.setColor(new Color(140 + random.nextInt(100), 140 + random.nextInt(100), 140 + random.nextInt(100)));
        g.drawLine(random.nextInt(width), random.nextInt(height), random.nextInt(width), random.nextInt(height));
      }

      String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
      StringBuilder code = new StringBuilder();
      for (int i = 0; i < 4; i++) {
        code.append(chars.charAt(random.nextInt(chars.length())));
      }
      session.setAttribute(CAPTCHA_SESSION_KEY, code.toString());

      g.setFont(new Font("Arial", Font.BOLD, 24));
      for (int i = 0; i < code.length(); i++) {
        g.setColor(new Color(20 + random.nextInt(100), 20 + random.nextInt(100), 20 + random.nextInt(100)));
        g.drawString(String.valueOf(code.charAt(i)), 12 + i * 23, 27 + random.nextInt(4));
      }

      response.setContentType("image/png");
      response.setHeader("Cache-Control", "no-store, no-cache");
      ImageIO.write(image, "png", response.getOutputStream());
    } finally {
      g.dispose();
    }
  }
}
