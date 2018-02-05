package webencoder.controllers;

import java.io.IOException;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import webencoder.storage.StorageFileNotFoundException;
import webencoder.storage.StorageService;
import webencoder.encoding.EncodingService;

import org.json.JSONObject;
import org.json.JSONArray;

@Controller
public class UploadController {

    private final StorageService storageService;
    private final EncodingService encoding_service;

    @Autowired
    public UploadController(StorageService storageService, EncodingService encoding_service) {
        this.storageService = storageService;
        this.encoding_service = encoding_service;
    }

    @GetMapping("/")
    public String initialAccess() {
        return "redirect:/upload";
    }

    @GetMapping("/upload")
    public String uploadFile() {
        return "upload";
    }

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

    @GetMapping("/watch/{filename:.+}")
    public String watchFile(@PathVariable String filename, Model model) {
        String link = storageService.path(filename, "output");
        model.addAttribute("source", link);
        return "watch";
    }

    @GetMapping("/input/{filename:.+}")
    public RedirectView redirectToInputFile(@PathVariable String filename) {
        RedirectView redirectView = new RedirectView();
        redirectView.setUrl(this.storageService.path(filename, "input"));
        return redirectView;
    }

    @GetMapping("/output/{filename:.+}")
    public RedirectView redirectToOutputFile(@PathVariable String filename) {
        RedirectView redirectView = new RedirectView();
        redirectView.setUrl(this.storageService.path(filename, "output"));
        return redirectView;
    }

    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
        return ResponseEntity.notFound().build();
    }

}
