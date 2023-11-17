package com.dietrich.formReader.controller;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import com.dietrich.formReader.entity.FileInfo;
import com.dietrich.formReader.entity.Greeting;
import com.dietrich.formReader.entity.ResponseMessage;
import com.dietrich.formReader.entity.UserList;
import com.dietrich.formReader.entity.Users;
import com.dietrich.formReader.service.FileStorage;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

@Controller
public class FormReaderController {

	@GetMapping(value = "/")
	public String HomePage(@ModelAttribute Greeting greeting, Model model) {
		model.addAttribute("greeting", greeting);

//		ModelAndView mv = new ModelAndView();
//		mv.
		return "upload";
	}

	@PostMapping(value = "/")
	public String Home(@ModelAttribute Greeting greeting, Model model) {
		model.addAttribute("greeting", greeting);

		System.out.println(greeting.getContent());
//		ModelAndView mv = new ModelAndView();
//		mv.
		return "upload";
	}

	@Autowired
	FileStorage storageService;

	@PostMapping("/upload")
	public ResponseEntity<ResponseMessage> uploadFile(@RequestParam("file") MultipartFile file) {
		String message = "";
		try {
			storageService.save(file);

			message = "Uploaded the file successfully: " + file.getOriginalFilename();
			return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage(message));
		} catch (Exception e) {
			message = "Could not upload the file: " + file.getOriginalFilename() + ". Error: " + e.getMessage();
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ResponseMessage(message));
		}
	}

	@GetMapping("/files")
	public ResponseEntity<List<FileInfo>> getListFiles() {
		List<FileInfo> fileInfos = storageService.loadAll().map(path -> {
			String filename = path.getFileName().toString();
			String url = MvcUriComponentsBuilder
					.fromMethodName(FormReaderController.class, "getFile", path.getFileName().toString()).build()
					.toString();

			return new FileInfo(filename, url);
		}).collect(Collectors.toList());

		return ResponseEntity.status(HttpStatus.OK).body(fileInfos);
	}

	@GetMapping("/files/{filename:.+}")
	@ResponseBody
	public ResponseEntity<Resource> getFile(@PathVariable String filename) {
		Resource file = storageService.load(filename);
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
				.body(file);
	}

	private UserList uLi;
	@GetMapping("/read/{filename:.+}")
	@ResponseBody
	public void readFile(@PathVariable String filename, Class<?> clazz) {
		Resource file = storageService.load(filename);
		if (filename.endsWith("txt") || filename.endsWith("csv")) {
			try {
				FileReader fr = new FileReader(file.getFile());
				BufferedReader br = new BufferedReader(fr); // creates a buffering character input stream
				StringBuffer sb = new StringBuffer(); // constructs a string buffer with no characters
				String line;
				while ((line = br.readLine()) != null) {
					sb.append(line); // appends line to string buffer
					sb.append("\n"); // line feed
				}
				fr.close(); // closes the stream and release the resources
				System.out.println("Contents of File: ");
				System.out.println(sb.toString()); // returns a string that textually represents the object
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if(filename.endsWith("xml")) {
	        // Create an XmlMapper
			JacksonXmlModule module = new JacksonXmlModule();
			module.setDefaultUseWrapper(false);
			XmlMapper xmlMapper = new XmlMapper(module);
	        // Deserialize XML from the file into a Java object
	        UserList person2;
			try {
				person2 = xmlMapper.readValue(file.getFile(), UserList.class);
				for (Users user : person2.getUsers()) {					
					System.out.println(user.toString());
				}
			} catch (StreamReadException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (DatabindException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else {
			System.out.println(filename + " does not end with txt or csv.");
		}
	}
}
