package bwfdm.sara.api;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController()
public class REST {
	@RequestMapping(path = "/api/branches", method = RequestMethod.GET)
	public List<String> getBranches() {
		return Arrays.asList("foo", "bar", "baz", "master", "test", "spring");
	}

	@RequestMapping(path = "/api/actions", method = RequestMethod.GET)
	public Map<String, String> getActions() {
		final Map<String, String> res = new HashMap<>();
		res.put("spring", "abbrev");
		res.put("foo", "latest");
		res.put("bar", "archive");
		return res;
	}
}
