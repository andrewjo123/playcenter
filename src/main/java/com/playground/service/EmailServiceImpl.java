package com.playground.service;

import com.playground.dto.MemberFormDto;
import com.playground.entity.Member;
import com.playground.repository.MemberRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.*;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private TemplateEngine templateEngine;

    @Value("${itemImgLocation}")
    private String itemImgLocation;

    @Override
    public void sendEmail(String toEmail, String subject, String template, Context context) {
        try {
            String body = templateEngine.process(template, context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom("놀이마당<gitemail@naver.com>");
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(body, true); // true로 설정하여 HTML로 전송

            ClassPathResource resource = new ClassPathResource("static/img/logo_s.png");
            InputStream inputStream = resource.getInputStream();
            byte[] fileContent = inputStream.readAllBytes();
            ByteArrayResource byteArrayResource = new ByteArrayResource(fileContent);

            helper.addInline("logo-image", byteArrayResource, "image/png");

            mailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace(); // 예외 처리
        }
    }

    @Override
    public MemberFormDto getUser(String email){
        Member member=memberRepository.findByEmail(email);
        MemberFormDto dto=new MemberFormDto();
        dto.setEmail(member.getEmail());
        dto.setAddress(member.getAddress());
        dto.setName(member.getName());
        return dto;
    }

    @Override
    public void sendEmailToMany(List<String> emailList, String subject, String template, Context context) {
        emailList.forEach(toEmail -> {
            try {
                String body = templateEngine.process(template, context);

                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true);

                helper.setFrom("놀이마당<gitemail@naver.com>");
                helper.setTo(toEmail);
                helper.setSubject(subject);
                helper.setText(body, true); // true로 설정하여 HTML로 전송

                ClassPathResource resource = new ClassPathResource("static/img/logo_s.png");
                InputStream inputStream = resource.getInputStream();
                byte[] fileContent = inputStream.readAllBytes();
                ByteArrayResource byteArrayResource = new ByteArrayResource(fileContent);
    
                helper.addInline("logo-image", byteArrayResource, "image/png");

                File productImg=new File(itemImgLocation+File.separator+context.getVariable("itemImgName"));
                helper.addInline("product-image", productImg);

                mailSender.send(message);
            } catch (Exception e) {
                e.printStackTrace(); // 예외 처리
            }
        });
    }

    @Override
    public void sendAllCodes(String toEmail, String subject, String template,  Map<List<String>, List<String>> codesList) {

        try {
            Context context=new Context();
            context.setVariable("codeLists",codesList);
            String body = templateEngine.process(template, context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom("놀이마당<gitemail@naver.com>");
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(body, true); // true로 설정하여 HTML로 전송

            ClassPathResource resource = new ClassPathResource("static/img/logo_s.png");
            InputStream inputStream = resource.getInputStream();
            byte[] fileContent = inputStream.readAllBytes();
            ByteArrayResource byteArrayResource = new ByteArrayResource(fileContent);

            helper.addInline("logo-image", byteArrayResource, "image/png");

            AtomicInteger i = new AtomicInteger(0);
            codesList.forEach((key,value)->{
                key.add("image"+i.get());
                File productImg = new File(itemImgLocation+File.separator+key.get(1));
                try {
                    File resizedImage = resizeImage(productImg, 150);
                    InputStreamSource imageSource = new FileSystemResource(resizedImage);

                    helper.addInline(key.get(2), imageSource, "image/jpg");
                } catch (MessagingException | IOException e) {
                    throw new RuntimeException(e);
                }
                i.incrementAndGet();
            });

            mailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace(); // 예외 처리
        }
    }


    public static File resizeImage(File originalImageFile, int maxHeight) throws IOException {
        BufferedImage originalImage = ImageIO.read(originalImageFile);
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();

        double aspectRatio = (double) originalWidth / originalHeight;
        int newWidth = (int) (maxHeight * aspectRatio);

        Image tmp = originalImage.getScaledInstance(newWidth, maxHeight, Image.SCALE_SMOOTH);
        BufferedImage resizedImage = new BufferedImage(newWidth, maxHeight, BufferedImage.TYPE_INT_RGB);

        Graphics2D g2d = resizedImage.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();

        String newFileName = "resized_" + originalImageFile.getName();
        ImageIO.write(resizedImage, "jpg", new File(originalImageFile.getParent(), newFileName));

        return new File(originalImageFile.getParent(), newFileName);
    }

}
