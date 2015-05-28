package internal;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;

public class CaptchaGenerator {
    public static class CaptchaPackage {
        public final String text;
        public final byte[] captcha;
        public CaptchaPackage(String text, byte[] captcha) {
            this.text = text;
            this.captcha = captcha;
        }
    }
    public static CaptchaPackage next(){
        DefaultKaptcha captchaPro = new DefaultKaptcha();
        Config conf = new Config(new Properties());
        captchaPro.setConfig(conf);
        String text = captchaPro.createText();
        BufferedImage img = captchaPro.createImage(text);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try{
            ImageIO.write(img, "jpg", baos);
            baos.flush();
        } catch(IOException e){
            e.printStackTrace();
        }
        return new CaptchaPackage(text, baos.toByteArray());
    }
}
