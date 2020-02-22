package com.example.demo.Controller;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@RestController
public class secondController
{
    @GetMapping("/test1/{id}")
    public ResponseEntity<FileInputStream> ok(@PathVariable String id) throws IOException {
        String content_type= Files.probeContentType(Path.of("/home/mrcoderider/IdeaProjects/demo (1)/demo/videos/"+id));
        File file=new File("/home/mrcoderider/IdeaProjects/demo (1)/demo/videos/"+id);
        Resource urlResource=new UrlResource(file.toURI());
        System.out.println(MediaTypeFactory.getMediaType(urlResource));
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        HttpHeaders httpHeaders=new HttpHeaders();

        ResourceRegion resourceRegion=new ResourceRegion(urlResource, 0, file.length());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .contentLength(file.length())
                .body(new FileInputStream(file));
    }
}
