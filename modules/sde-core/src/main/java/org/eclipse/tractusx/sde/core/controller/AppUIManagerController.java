package org.eclipse.tractusx.sde.core.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class AppUIManagerController {

	@GetMapping("/index")
	public String sayHello() {
		return "index";
	}

	@GetMapping("/add-app")
	public String app() {
		return "app";
	}

	@GetMapping("/submodel")
	public String component() {
		return "submodel";
	}
	
}
