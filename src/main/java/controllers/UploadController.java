package webencoder.controllers;

import java.io.IOException;
import java.util.stream.Collectors;

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



@Controller
public class UploadController {

    private final StorageService storageService;

    @Autowired
    public UploadController(StorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping("/")
    public String initialAccess() {
        return "redirect:/upload";
    }

    @GetMapping("/upload")
    public String uploadFile() {
        return "upload";
    }

    @GetMapping("/input/{filename:.+}")
    public RedirectView redirectToInputFile(@PathVariable String filename) {
        RedirectView redirectView = new RedirectView();
        redirectView.setUrl(this.storageService.path("input/" + filename));
        return redirectView;
    }

    @PostMapping("/input")
    public String handleFileUpload(@RequestParam("file") MultipartFile file,
            RedirectAttributes redirectAttributes) {

        storageService.store(file, "input");
        redirectAttributes.addFlashAttribute("message",
                "You successfully uploaded " + file.getOriginalFilename() + "!");

        return "redirect:/";
    }

    @GetMapping("/output/{filename:.+}")
    public RedirectView redirectToOutputFile(@PathVariable String filename) {
        RedirectView redirectView = new RedirectView();
        redirectView.setUrl(this.storageService.path("output/" + filename));
        return redirectView;
    }

    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
        return ResponseEntity.notFound().build();
    }

}
