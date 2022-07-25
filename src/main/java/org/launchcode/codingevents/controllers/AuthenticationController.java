package org.launchcode.codingevents.controllers;

import org.launchcode.codingevents.data.UserRepository;
import org.launchcode.codingevents.models.User;
import org.launchcode.codingevents.models.dto.LoginFormDTO;
import org.launchcode.codingevents.models.dto.RegisterFormDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.Optional;

@Controller
public class AuthenticationController {

    @Autowired
    UserRepository userRepository;

    private static final String userSessionKey = "user";

    public User getUserFromSession(HttpSession session) {
        Integer userId = (Integer) session.getAttribute(userSessionKey);
        if (userId == null) {
            return null;
        }

        Optional<User> user = userRepository.findById(userId);

        if (user.isEmpty()) {
            return null;
        }

        return user.get();
    }

    private static void setUserInSession(HttpSession session, User user) {
        session.setAttribute(userSessionKey, user.getId());
    }

    @GetMapping("/register")
    public String displayRegistrationForm(Model model) {
        // will have label "registerFormDTO" (class name, but lower-cased 1st letter), if no label is specified
        model.addAttribute(new RegisterFormDTO());
        model.addAttribute("title", "Register");
        return "register";
    }

    /**
     *   Lines 60-62 => define handler method at the route "/register";
     *   takes a valid RegisterFormDTO object, associated errors, and a Model;
     *   this method needs an HttpServletRequest object (represents the incoming request provided by Spring)
      */
    @PostMapping("/register")
    public String processRegistrationForm(@ModelAttribute @Valid RegisterFormDTO registerFormDTO,
                                          Errors errors, HttpServletRequest request,
                                          Model model) {

        // Returns the user to the form if a validation error occurs
        if (errors.hasErrors()) {
            model.addAttribute("title", "Register");
            return "register";
        }

        // Retrieve the user with the given username from the database
        User existingUser = userRepository.findByUsername(registerFormDTO.getUsername());

        // If a user with a given username already exists, register a custom error with the errors object and return the user to the form
        if (existingUser != null) {
            errors.rejectValue("username", "username.alreadyexists", "A user with that username already exists");
            model.addAttribute("title", "Register");
            return "register";
        }

        // Compare the two passwords submitted; if they don't match, register a custom error and return the user to the form
        String password = registerFormDTO.getVerifyPassword();
        String verifyPassword = registerFormDTO.getVerifyPassword();
        if (!password.equals(verifyPassword)) {
            errors.rejectValue("password", "passwords.mismatch", "Passwords do not match");
            model.addAttribute("title", "Register");
            return "register";
        }

        // User with a given username does not exist and is valid; creates new user object, stores it in the database, and creates a new session for the user
        User newUser = new User(registerFormDTO.getUsername(), registerFormDTO.getPassword());
        userRepository.save(newUser);
        setUserInSession(request.getSession(), newUser);

        // Redirect the user to the home page
        return "redirect:";
    }

    @GetMapping("/login")
    public String displayLoginForm(Model model) {
        model.addAttribute(new LoginFormDTO());
        model.addAttribute("title", "Log In");
        return "login";
    }

    /**
     *   Lines 111-114 => define handler method at the route "/login";
     *   takes a valid LoginFormDTO object, associated errors, and a Model;
     *   this method needs an HttpServletRequest object (represents the incoming request provided by Spring)
     */
    @PostMapping("/login")
    public String processLoginForm(@ModelAttribute @Valid LoginFormDTO loginFormDTO,
                                   Errors errors, HttpServletRequest request,
                                   Model model) {

        if (errors.hasErrors()) {
            model.addAttribute("title", "Log In");
            return "login";
        }

        // Retrieves the User object with the given password from the database
        User theUser = userRepository.findByUsername(loginFormDTO.getUsername());

        // If the user does not exist, register a custom error and return to the form
        if (theUser == null) {
            errors.rejectValue("username", "user.invalid", "The given username does not exist");
            model.addAttribute("title", "Log In");
            return "login";
        }

        // Retrieves the submitted password from the from DTO
        String password = loginFormDTO.getPassword();

        // If the password is incorrect, register a custom error and return to the form => User.isMatchingPassword(); handles details associated with checked hashed passwords
        if (!theUser.isMatchingPassword(password)) {
            errors.rejectValue("password", "password.invalid", "Invalid password");
            model.addAttribute("title", "Log In");
            return "login";
        }

        // If user exists, create new session for the user
        setUserInSession(request.getSession(), theUser);

        // Redirects user to the home page
        return "redirect:";
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request) {
        request.getSession().invalidate();
        return "redirect:/login";
    }
}
