package com.fan.community.controller;

import com.fan.community.entity.User;
import com.fan.community.service.UserService;
import com.fan.community.util.CommunityConstant;
import com.fan.community.util.CommunityUtil;
import com.fan.community.util.MailClient;
import com.fan.community.util.RedisKeyUtil;
import com.google.code.kaptcha.Producer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.rmi.MarshalledObject;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
public class LoginController implements CommunityConstant {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
    @Autowired
    private UserService userService;
    @Autowired
    private Producer kaptchaProduce;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private RedisTemplate redisTemplate;

    @RequestMapping(path = "/register",method = RequestMethod.GET)
    public String getRegisterPage(){
        return "/site/register";
    }

    @RequestMapping(path = "/login",method = RequestMethod.GET)
    public String getLoginPage(){
        return "/site/login";
    }

    @RequestMapping(path = "register",method = RequestMethod.POST)
    public String register(Model model, User user){
        Map<String,Object> map = userService.register(user);
        if (map == null || map.isEmpty()){
            model.addAttribute("msg","我们已经向您的邮箱发送了一封激活邮件，请尽快激活吧");
            model.addAttribute("target","/login");
            return "/site/operate-result";
        }else {
            model.addAttribute("userNameMsg",map.get("userNameMsg"));
            model.addAttribute("emailMsg",map.get("emailMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            return "/site/register";
        }
    }
    @RequestMapping(path = "/activation/{userId}/{code}",method = RequestMethod.GET)
    public String activation(Model model, @PathVariable("userId") int userId, @PathVariable("code") String code){
        int result = userService.activation(userId, code);
        if (result == ACTIVATION_SUCCESS){
            model.addAttribute("msg","恭喜您，邮箱已经激活！！");
            model.addAttribute("target","/login");
        }else if (result == ACTIVATION_REPEAT){
            model.addAttribute("msg","账号已经激活，请不要重复激活！");
            model.addAttribute("target","/index");
        }else {
            model.addAttribute("msg","账号激活失败！");
            model.addAttribute("target","/index");
        }
        return "/site/register";
    }
    //验证码
    @RequestMapping(path = "kaptcha",method = RequestMethod.GET)
    public void getKaptcha(HttpServletResponse response/*, HttpSession session*/){
        //生成验证码
        String text = kaptchaProduce.createText();
        BufferedImage image = kaptchaProduce.createImage(text);

        //将验证码存入session
//        session.setAttribute("kaptcha",text);
        /**
            重铸验证码
         */
        String kaptchOwner = CommunityUtil.generateUUID();
        Cookie cookie = new Cookie("kaptchOwner", kaptchOwner);
        cookie.setMaxAge(300);
        cookie.setPath(contextPath);
        response.addCookie(cookie);
        //将验证码存入redis
        String redisKey = RedisKeyUtil.getKaptchaKey(kaptchOwner);
        redisTemplate.opsForValue().set(redisKey, text,300, TimeUnit.SECONDS);



        //将图片输出给浏览器
        response.setContentType("image/png");
        try {
            OutputStream os = response.getOutputStream();
            ImageIO.write(image,"png",os);
        } catch (IOException e) {
            logger.error("响应验证码失败！"+e.getMessage());
        }
    }

    @RequestMapping(path = "/login", method = RequestMethod.POST)
    public String login(String username, String password, String code, boolean rememberme, Model model,/*HttpSession session,*/
                        HttpServletResponse response,@CookieValue("kaptchOwner") String kaptchOwner) {
        /**
         * 重铸登录验证码
         */
        //检查验证妈
//        String kaptcha = (String) session.getAttribute("kaptcha");
        String kaptcha = null;
        if (StringUtils.isNotBlank(kaptchOwner)) {
            String redisKey = RedisKeyUtil.getKaptchaKey(kaptchOwner);
            kaptcha = (String) redisTemplate.opsForValue().get(redisKey);

        }



        if (StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !kaptcha.equalsIgnoreCase(code)) {
            model.addAttribute("codeMsg", "验证码不正确！");
            return "/site/login";
        }
        //检查账号密码
        int expiredSeconds = rememberme ? REMEMBER_EXPIRED_SECONDS : DEFAULT_EXPIRED_SECONDS;
        Map<String, Object> map = userService.login(username, password, expiredSeconds);
        if (map != null && map.containsKey("ticket")) {
            Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
            cookie.setPath(contextPath);
            cookie.setMaxAge(expiredSeconds);
            response.addCookie(cookie);
            return "redirect:/index";
        } else {
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            return "/site/login";
        }
    }
    @RequestMapping(path = "logout",method = RequestMethod.GET)
    public String logout(@CookieValue("ticket") String ticket){
        userService.logout(ticket);
        SecurityContextHolder.clearContext();
        return "redirect:/login";
    }

////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////

    // 忘记密码页面
    @RequestMapping(path = "/forget", method = RequestMethod.GET)
    public String getForgetPage(){
        return "/site/forget";
    }

    // 获取验证码
    @RequestMapping(path = "/forget/code", method = RequestMethod.GET)
    @ResponseBody
    public String getForgetCode(String email, HttpSession session) {
        if (StringUtils.isBlank(email)) {

            return CommunityUtil.getJSONString(1, "邮箱不能为空！");
        }

        // 发送邮件
        Context context = new Context();
        context.setVariable("email", email);
        String code = CommunityUtil.generateUUID().substring(0, 4);
        context.setVariable("verifyCode", code);
        String content = templateEngine.process("/mail/forget", context);
        mailClient.sendMail(email, "找回密码", content);

        // 保存验证码
        session.setAttribute("verifyCode", code);

        return CommunityUtil.getJSONString(0);
    }

    // 重置密码
    @RequestMapping(path = "/forget/password", method = RequestMethod.POST)
    public String resetPassword(String email, String verifyCode, String password, Model model, HttpSession session) {
        String code = (String) session.getAttribute("verifyCode");
        if (StringUtils.isBlank(verifyCode) || StringUtils.isBlank(code) || !code.equalsIgnoreCase(verifyCode)) {
            model.addAttribute("codeMsg", "验证码错误!");
            return "/site/forget";
        }

        Map<String, Object> map = userService.resetPassword(email, password);
        if (map.containsKey("user")) {
            return "redirect:/login";
        } else {
            model.addAttribute("emailMsg", map.get("emailMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            return "/site/forget";
        }
    }
}
