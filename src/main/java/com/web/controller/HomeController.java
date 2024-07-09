package com.web.controller;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.web.dao.UserRepository;
import com.web.entity.User;
import com.web.helper.Message;

@Controller
public class HomeController {
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	@Autowired
	private UserRepository userRepository;
	
	@RequestMapping("/")
	public String home(Model model) 
	{
		model.addAttribute("title", "Home - Smart Contact Manager");
		return "home";
	}
	

	@RequestMapping("/about")
	public String about(Model model) 
	{
		model.addAttribute("title", "About - Smart Contact Manager");
		return "about";
	}
	

	@RequestMapping("/signup")
	public String signup(Model model) 
	{
		model.addAttribute("title", "Register - Smart Contact Manager");
		model.addAttribute("user", new User());
		return "signup";
	}
	
	//this handler for registration
	@PostMapping("/do_register")
     public String registerUser(@Valid @ModelAttribute("user") User user,BindingResult result1,
    	   @RequestParam (value = "agreement",defaultValue = "false") boolean agreement,
    	   Model model, HttpSession session ) 
     {
		try {
			
			if(!agreement)
			{
			System.out.println("You have not agreed terns and conditions ..");
			throw new Exception("You have not agreed terns and conditions ..");	
			}
			
			if(result1.hasErrors())
			{
				System.out.println("ERROR "+result1.toString());
				model.addAttribute("user",user);
				return "signup";
			}
			
			
			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setImageUrl("default.png");
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			
			System.out.println("Agreement "+agreement);
			System.out.println("USER "+user);
			
			User result = this.userRepository.save(user);
		    model.addAttribute("user",new User());
		    
		    session.setAttribute("message", new Message("SuccessFully Registered !!","alert-success"));
			 return "signup";
			
		} catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("user",user);
			session.setAttribute("message", new Message("Something went wrong !!"+e.getMessage(),"alert-danger"));
			 return "signup";
		}
      	 
     }

	// handler for custom login
	@GetMapping("/login1")
	public String customLogin(Model model)
	{
		model.addAttribute("title", "Login page");
		return "login";
	}
	
	@RequestMapping("/login_fail")
	public String failed()
	{
		return "error_page";
	}
}
