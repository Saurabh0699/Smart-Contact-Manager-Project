package com.web.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.aspectj.bridge.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.web.dao.ContactRepository;
import com.web.dao.UserRepository;
import com.web.entity.Contact;
import com.web.entity.User;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ContactRepository contactRepository;
	//method for adding comman method
	
	@ModelAttribute
	public void addCommonData(Model model,Principal principal)
	{
		String userName = principal.getName();
		System.out.println("USERNAME "+userName);
		
		//get ther user using username
		
		User user = userRepository.getUserByUserName(userName);
		
		System.out.println("USER "+user);
		
		model.addAttribute("user", user);
	}
	
	//dashbash home
	
	@RequestMapping("/index")
	public String dashboard(Model model,Principal principal )
	{ 
		model.addAttribute("title","User Dashboard");
	    return "normal/user_dashboard";	
	}
	
	// open  add form handler
	
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model) 
	{
		model.addAttribute("title","Add Contact");
		model.addAttribute("contact",new Contact());
		
		return "normal/add_contact_form";
	}
	
	//Procesing add contact form
	
	@PostMapping("/process-contact")
	public String processContact(
			@ModelAttribute Contact contact,
			@RequestParam("profileImage") MultipartFile file,
			Principal principal,HttpSession session)
	{
		try {
		String name = principal.getName();
		User user = this.userRepository.getUserByUserName(name);
		
		//processing  and uploading file
		
		if(file.isEmpty())
		{
			//if file is empty then try our message
			System.out.println("File is empty");
			contact.setImage("contact.png");
			
			
		}
		else {
			//file the file to folder and update the name to contact
		contact.setImage(file.getOriginalFilename());
		
		File saveFile=new ClassPathResource("static/image").getFile();
	
		Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
		
		Files.copy(file.getInputStream(),path,StandardCopyOption.REPLACE_EXISTING);
		
		System.out.println("Image is uploaded");
		}
		user.getContacts().add(contact);
		contact.setUser(user);
		
		
		this.userRepository.save(user);
		
		System.out.println("Data"+contact);
		
		System.out.println("Added to Database");
		//success message
		session.setAttribute("message", new com.web.helper.Message("Your contact is added !! Add more..","success"));
		
		}catch (Exception e) {
			// TODO: handle exception
			System.out.println("ERROR"+e.getMessage());
	        e.printStackTrace();		
		//message error
		
		session.setAttribute("message", new com.web.helper.Message("Something went wrong  !! Try again..","danger"));
		}
		return "normal/add_contact_form";
	}
	// show contact handler
	
	
	@GetMapping("/show_contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page ,Model m ,Principal principal) {
		m.addAttribute("title","Show Contacts");
		// Send List of Contact List
		
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		
		//per page = 5[n]
				//Current page =0 (page)
				Pageable pageable = PageRequest.of(page, 4);
				Page<Contact> contacts = this.contactRepository.findContactsByUser(user.getId(),pageable);
				
				m.addAttribute("contacts",contacts);
				m.addAttribute("currentPage",page);
				m.addAttribute("totalPage", contacts.getTotalPages());
		
		return "normal/show_contacts";
	}
	
	//showing particular contact details
	
	@RequestMapping("/{cId}/contact")
	public String showContactDetail(@PathVariable("cId") Integer cId,Model model,Principal principal)
	{
	    System.out.println("CID "+cId);	
	    
	    Optional<Contact> contactOptional = this.contactRepository.findById(cId);
	    Contact contact = contactOptional.get();
	    
	    String userName = principal.getName();
	    User user = this.userRepository.getUserByUserName(userName);
	    
	    if(user.getId()==contact.getUser().getId())
	    {
	    	model.addAttribute("contact", contact);
	    	model.addAttribute("title",contact.getName());
	    }
	    
		return "normal/contact_detail";
	}
	
	//delete contact handler
	
	@GetMapping("/delete/{cid}")
	public String deleteContact(@PathVariable("cid") Integer cId,Model model,HttpSession session,Principal principal)
	{

		System.out.println("CID "+ cId );
		/*
		 * Optional<Contact> contactOptional = this.contactRepository.findById(cId);
		 * Contact contact = contactOptional.get();
		 */
		
		Contact contact = this.contactRepository.findById(cId).get();
		
		//check.....Assignment
		// if(user.getId()==contact.getUser().getId())
		//System.out.println("Contact "+contact.getcId());
		
		//contact.setUser(null);
		//this.contactRepository.delete(contact);
		
		User user = this.userRepository.getUserByUserName(principal.getName());
		
		user.getContacts().remove(contact);
		
		this.userRepository.save(user);
		
		System.out.println("DELETED");
		session.setAttribute("message",new com.web.helper.Message("Contact deleted successfully...", "success"));		
		
		return "redirect:/user/show_contacts/0";
	}
	
	//open update form handler
	
	@PostMapping("/update-contact/{cid}")
	public String updateForm(@PathVariable("cid") Integer cid,Model m)
	{
		
		m.addAttribute("title","Update Contact");
		
		Contact contact = this.contactRepository.findById(cid).get();
		m.addAttribute("contact", contact);
		return "normal/update_form";
	}
	
	//update contact handler
	
	@RequestMapping(value = "/process-update",method = RequestMethod.POST)
	public String updateHandler(@ModelAttribute Contact contact,@RequestParam("profileImage") MultipartFile file,
			Model m,HttpSession session,Principal principal)
	{
		
		try {
			//old contact details
			
			Contact OldContactDetail = this.contactRepository.findById(contact.getcId()).get();
			
			//image..
			if(!file.isEmpty())
			{
				//file work..
				//rewrite
				
				//delete old photo
				
				File deleteFile=new ClassPathResource("static/image").getFile();
				File file1=new File(deleteFile, OldContactDetail.getImage());
				file1.delete();

				
				//upload new photo
				

				File saveFile=new ClassPathResource("static/image").getFile();
			
				Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				
				Files.copy(file.getInputStream(),path,StandardCopyOption.REPLACE_EXISTING);
				
				contact.setImage(file.getOriginalFilename());
				
			} else
			{
				contact.setImage(OldContactDetail.getImage());
			}
			
			User user = this.userRepository.getUserByUserName(principal.getName());
			
			contact.setUser(user);
			
			this.contactRepository.save(contact);
			
			session.setAttribute("message", new com.web.helper.Message("Your contact is updated...","success"));
			
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		
		System.out.println("Contact Name "+contact.getName());
		System.out.println("Contact Name "+contact.getcId());
		
		return "redirect:/user/"+contact.getcId()+"/contact";
	}
}
