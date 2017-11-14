package bwfdm.sara.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequestMapping("/api/publication")
public class Publication {
	
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

}
