package webencoder.controllers;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.view.RedirectView;
import webencoder.storage.StorageFileNotFoundException;
import webencoder.storage.StorageService;
import webencoder.encoding.EncodingService;
import org.json.JSONObject;

/*
Main controller that handles the routing for the application.

It provides the functionality of uploading through a POST request a
file into the storage service, it initiates an encoding job with
the encoding service and allows the user to watch a video from a url
from the storage service.
*/
@Controller
public class MainController {

    private final StorageService storageService;
    private final EncodingService encoding_service;

    @Autowired
    public MainController(StorageService storageService, EncodingService encoding_service) {
        this.storageService = storageService;
        this.encoding_service = encoding_service;
    }

    /*
    Root mapping that diverges to /upload.
    */
    @GetMapping("/")
    public String initialAccess() {
        return "redirect:/upload";
    }

    /*
    Initial view.
    */
    @GetMapping("/upload")
    public String uploadFile() {
        return "upload";
    }

    /*
    Store the file received from POST request, then initiate a encoding job.

    This function returns the encoding view passing to it all data necessary
    for keeping track of the encoding job. It redirect to upload again if no
    file is received.
    */
    @PostMapping("/encode")
    public String handleFileUpload(
        @RequestParam("file") MultipartFile file,
        Model model)
    {
        try {
            // Store file to S3.
            storageService.store(file, "input");
            // Process file names
            String input_filename = file.getOriginalFilename();
            String output_filename =
              FilenameUtils.removeExtension(input_filename) + ".webm";

            // Encode file and save it to S3.
            String response = encoding_service.encode(
              input_filename, "input",
              output_filename, "output"
            );

            // Add response attributes to model.
            JSONObject response_JSON = new JSONObject(response);
            model.addAttribute("input_id", response_JSON.get("input_id"));
            model.addAttribute("output_id", response_JSON.get("output_id"));
            model.addAttribute("output_link", "/watch/" + output_filename);
            model.addAttribute("zencoder_key", response_JSON.get("zencoder_key"));

            return "encoding";
        } catch(Exception e) {
            return "upload";
        }
    }

    /*
    Allows the user to watch an encoded video.
    */
    @GetMapping("/watch/{filename:.+}")
    public String watchFile(@PathVariable String filename, Model model) {
        String link = storageService.path(filename, "output");
        model.addAttribute("source", link);
        return "watch";
    }

}
