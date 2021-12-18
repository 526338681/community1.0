package com.fan.community.controller;

import com.fan.community.entity.User;
import com.fan.community.service.UserService;
import com.fan.community.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Map;

@Controller
public class LoginController implements CommunityConstant {
    @Autowired
    private UserService userService;
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
            model.addAttribute("target","/index");
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
}
