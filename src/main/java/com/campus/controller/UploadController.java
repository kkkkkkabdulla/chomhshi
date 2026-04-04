package com.campus.controller;

import com.campus.common.Result;
import com.campus.dto.response.UploadImageResp;
import com.campus.util.FileUploadUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@RestController
@RequestMapping("/api/upload")
public class UploadController {

    @Value("${file.upload.path}")
    private String uploadPath;

    @Value("${file.access.prefix}")
    private String accessPrefix;

    @PostMapping("/image")
    public Result<UploadImageResp> uploadImage(@RequestParam("file") MultipartFile file) {
        String filename = FileUploadUtil.saveImage(file, uploadPath);
        String url = buildAccessUrl(filename);
        return Result.success(new UploadImageResp(url));
    }

    @GetMapping("/file/{filename:.+}")
    public ResponseEntity<Resource> getFile(@PathVariable("filename") String filename) {
        File target = new File(uploadPath, filename);
        if (!target.exists() || !target.isFile()) {
            return ResponseEntity.notFound().build();
        }

        FileSystemResource resource = new FileSystemResource(target);
        MediaType mediaType = resolveMediaType(filename);

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CACHE_CONTROL, "max-age=86400")
                .body(resource);
    }

    private String buildAccessUrl(String filename) {
        String prefix = accessPrefix.endsWith("/") ? accessPrefix : accessPrefix + "/";
        return prefix + filename;
    }

    private MediaType resolveMediaType(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".png")) {
            return MediaType.IMAGE_PNG;
        }
        if (lower.endsWith(".webp")) {
            return MediaType.parseMediaType("image/webp");
        }
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            return MediaType.IMAGE_JPEG;
        }
        return MediaType.APPLICATION_OCTET_STREAM;
    }
}
