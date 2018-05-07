package bwfdm.sara.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import bwfdm.sara.Config;
import bwfdm.sara.publication.Repository;
import java.util.UUID;

@RestController
@RequestMapping("/api/publication")
public class Publication {
	@Autowired
	private Config config;
	
	@GetMapping("test")
    public RedirectView getTestPublicationPage() {
        return new RedirectView("/publication-test.html");
    }
		
	@PostMapping("publish-something")
	public String publishSomething(@RequestParam("j_title") String title) {
	    //do something
		String str = title;		
		return "will be published soon...: " + str;
	}
	/*
	@GetMapping("set-ir")
	public void setIR(@RequestParam("uuid") String id) {
	}*/
}
