package text2slides.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.json.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.*;

import text2slides.code_exec.CodeManager;
import text2slides.slides.SlidesProcessing;
import text2slides.utils.FileHelper;

/**
 * This class matches the URL request from the browser with the required handler
 * method.
 */

@SpringBootApplication
@RestController
@ComponentScan(basePackages = { "text2slides.slides", "text2slides.server", "text2slides.code_exec" })
public class SpringServer {

	private SlidesProcessing sproc;

	private CodeManager codeManager;

	public void startServer(String[] args) {
		SpringApplication.run(SpringServer.class, args);
	}

	@Autowired
	public void addSlidesProcessing(SlidesProcessing sproc) {
		this.sproc = sproc;
	}

	@Autowired
	public void addCodeManager(CodeManager cmdMan) {
		this.codeManager = cmdMan;
	}

	@GetMapping("/index")
	@ResponseBody
	@Cacheable("index")
	public String indexPage(@RequestParam(name = "page", defaultValue = "1") String activePageNumber) {

		String filePath = "./src/main/resources/templates/index.html";
		String document = FileHelper.readFile(filePath);

		document = document.replace("//CURRENTPAGE", activePageNumber);

		return document;
	}

	@PostMapping("/code")
	public ResponseEntity<?> receiveData(@RequestBody String input) {
		String receivedText = input;
		// System.out.println(receivedText);

		JSONObject jsonObj = new JSONObject(input);
		String code = jsonObj.getString("text");

		String result = this.codeManager.execCode(code);
		// String responseText = receivedText.toUpperCase();

		return ResponseEntity.ok().body(Map.of("response", result));
	}

	// Markdown syntax: ![Alt Text](/images?imageName=image_file "Title") (Title is
	// not nece ssary)
	// Example: ![image](/images?imageName=pizza.png)
	// Can be any image types(e.g. png, jpg, gif etc.)
	@GetMapping("/images")
	@ResponseBody
	public ResponseEntity<byte[]> getImage(@RequestParam("imageName") String imageName) {
		try {
			Resource resource = new ClassPathResource("pic/" + imageName);
			InputStream inputStream = resource.getInputStream();

			byte[] imageBytes = StreamUtils.copyToByteArray(inputStream);

			HttpHeaders headers = new HttpHeaders();
			headers.setCacheControl("max-age=60");
			headers.setContentType(MediaType.IMAGE_JPEG);
			return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
		} catch (IOException e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	// External videos: [Link Text](URL "Title") (Title is not necessary)
	// Example: [Video](https://www.youtube-nocookie.com/embed/hfTjs6K0AMY "Mr. Bean
	// Video link")

	// local videos: [Text](/videos?videoName=video_file.mp4 "Title")
	// Example: [Video](/videos?videoName=seaWave.webm "Sea wave Video path")

	// display the video on the same webpage
	// Embed an video file with controls
	// <video width="320" height="240" controls>
	// <source src="/videos?videoName=seaWave.mp4" type="video/mp4">
	// </video>
	@GetMapping("/videos")
	@ResponseBody
	public ResponseEntity<byte[]> getVideo(@RequestParam("videoName") String videoName) {
		try {
			Resource resource = new ClassPathResource("video/" + videoName);
			InputStream inputStream = resource.getInputStream();

			byte[] videoBytes = StreamUtils.copyToByteArray(inputStream);

			HttpHeaders headers = new HttpHeaders();

			// Determine content type based on file extension
			String contentType = getVideoContentType(videoName);
			headers.setContentType(MediaType.parseMediaType(contentType));

			headers.setContentLength(videoBytes.length);
			headers.setContentDisposition(ContentDisposition.inline().filename(videoName).build());
			headers.setCacheControl("max-age=60");

			return new ResponseEntity<>(videoBytes, headers, HttpStatus.OK);
		} catch (IOException e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// Helper method: determine content type based on file extension
	private String getVideoContentType(String fileName) {
		String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase(); // Extract the file
																							// extension from the
																							// filename

		switch (extension) {
		case "mp4":
			return "video/mp4";
		case "webm":
			return "video/webm";
		case "mov":
			return "video/mov";
		case "avi":
			return "video/avi";
		default:
			return "application/octet-stream"; // Default type
		}
	}

	// Embed an audio file with controls
	// <audio controls>
	// <source src="/audio?audioName=success.mp3" type="audio/mp3">
	// </audio>
	@GetMapping("/audio")
	@ResponseBody
	public ResponseEntity<byte[]> getAudio(@RequestParam("audioName") String audioName) {
		try {
			Resource resource = new ClassPathResource("audio/" + audioName);
			InputStream inputStream = resource.getInputStream();

			byte[] audioBytes = StreamUtils.copyToByteArray(inputStream);

			HttpHeaders headers = new HttpHeaders();

			// Determine content type based on file extension
			String contentType = getAudioContentType(audioName);
			headers.setContentType(MediaType.parseMediaType(contentType));
			headers.setCacheControl("max-age=60");
			headers.setContentLength(audioBytes.length);
			headers.setContentDisposition(ContentDisposition.inline().filename(audioName).build());

			return new ResponseEntity<>(audioBytes, headers, HttpStatus.OK);
		} catch (IOException e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// Helper method: determine content type based on file extension
	private String getAudioContentType(String fileName) {
		String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase(); // Extract the file
																							// extension from the
																							// filename

		switch (extension) {
		case "mp3":
			return "audio/mp3";
		case "ogg":
			return "audio/ogg";
		case "wav":
			return "audio/wav";
		default:
			return "application/octet-stream"; // Default type
		}
	}
}
