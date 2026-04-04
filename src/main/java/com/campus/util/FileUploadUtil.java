package com.campus.util;

import com.campus.common.BusinessException;
import com.campus.common.ResultCode;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.UUID;

public class FileUploadUtil {

    private static final long MAX_SIZE = 5L * 1024 * 1024; // 5MB

    private FileUploadUtil() {
    }

    public static String saveImage(MultipartFile file, String uploadDirPath) {
        validateImage(file);

        String originalFilename = file.getOriginalFilename();
        String ext = getExt(originalFilename);
        String filename = UUID.randomUUID().toString().replace("-", "") + "." + ext;

        File uploadDir = new File(uploadDirPath);
        if (!uploadDir.exists() && !uploadDir.mkdirs()) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR.getCode(), "创建上传目录失败");
        }

        File target = new File(uploadDir, filename);
        try {
            file.transferTo(target);
        } catch (IOException e) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR.getCode(), "保存图片失败");
        }

        return filename;
    }

    private static void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "请选择要上传的图片");
        }

        if (file.getSize() > MAX_SIZE) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "图片大小不能超过5MB");
        }

        String ext = getExt(file.getOriginalFilename());
        if (!("jpg".equals(ext) || "jpeg".equals(ext) || "png".equals(ext) || "webp".equals(ext))) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "仅支持 jpg/jpeg/png/webp 格式图片");
        }
    }

    private static String getExt(String filename) {
        if (filename == null || !filename.contains(".")) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "文件格式错误");
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }
}
