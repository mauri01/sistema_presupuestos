package com.example.controller;

import com.example.model.Negocio;
import com.example.model.User;
import com.example.service.NegocioService;
import com.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
import java.util.Objects;
import java.util.Optional;

@Controller
public class NegocioController {
    @Autowired
    private UserService userService;

    @Autowired
    private NegocioService negocioService;

    @RequestMapping(value="/admin/minegocio", method = RequestMethod.GET)
    public ModelAndView negocio() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("admin/negocio");
        User user = getUserAuth();
        Optional<Negocio> negocioCreated = negocioService.findAll().stream()
                .filter(negocio -> Objects.nonNull(negocio.getUserId())
                        && negocio.getUserId() == user.getId()).findFirst();

        if (negocioCreated.isPresent()) {
            Negocio negocio = negocioCreated.get();
            modelAndView.addObject("negocio", negocio);
        } else {
            Negocio negocio = new Negocio();
            modelAndView.addObject("negocio", negocio);
        }

        return modelAndView;
    }

    private User getUserAuth() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = null;
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            username = userDetails.getUsername();

        } else if (authentication != null) {
            username = authentication.getPrincipal().toString();
        }
        return userService.findUserByEmail(username);
    }

    @RequestMapping(value="/minegocio", method = RequestMethod.POST)
    public ModelAndView addNegocio(@Valid Negocio negocio, @RequestParam("file") MultipartFile file){
        User user = getUserAuth();
        negocio.setUserId((long) user.getId());

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("admin/negocio");
        String messageError = null;
        String message = null;
        
        try{
            byte[] fileData = file.getBytes();
            negocio.setLogo(fileData);
            negocioService.saveNegocio(negocio);
            message = "El Negocio fue creado de forma correcta";

        }catch (Exception e){
            messageError = "Ocurrio un error al crear el Negocio";
        }

        modelAndView.addObject("error", messageError);
        modelAndView.addObject("message", message);
        return modelAndView;
    }
}
