package com.example.fitnesscoach.controller;

import com.example.fitnesscoach.service.AIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Controller
public class AudioController {

    @Autowired
    private AIService aiService;

    @PostMapping("/processAudio")
    public String handleAudioUpload(@RequestParam("audio") MultipartFile audioFile, Model model) {
        try {
            File tempAudio = File.createTempFile("user_audio_", ".wav");
            audioFile.transferTo(tempAudio);

            File responseAudio = aiService.processUserAudio(tempAudio);


            String audioFilename = responseAudio.getName();
            model.addAttribute("responseAudioFile", audioFilename);

            return "resultPage"; // thymeleaf template to play audio
        } catch (IOException e) {
            e.printStackTrace();
            model.addAttribute("error", "Failed to process audio: " + e.getMessage());
            return "errorPage";
        }
    }


    @GetMapping("/audio/{filename}")
    @ResponseBody
    public ResponseEntity<FileSystemResource> downloadAudio(@PathVariable String filename) {
        File audioFile = new File(filename);
        if (!audioFile.exists()) {
            return ResponseEntity.notFound().build();
        }
        FileSystemResource resource = new FileSystemResource(audioFile);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .contentType(MediaType.valueOf("audio/mpeg"))
                .body(resource);
    }
}
