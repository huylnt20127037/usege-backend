package com.group_1.uploadFile.service;

import com.group_1.sharedDynamoDB.model.UserFile;
import com.group_1.sharedDynamoDB.model.UserInfo;
import com.group_1.sharedDynamoDB.repository.UserFileDbRepository;
import com.group_1.sharedDynamoDB.repository.UserRepository;
import com.group_1.sharedS3.repository.FileRepository;
import com.group_1.uploadFile.dto.UserFileDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * com.group_1.uploadFile.service
 * Created by NhatLinh - 19127652
 * Date 4/19/2023 - 12:39 PM
 * Description: ...
 */
@Service
@Slf4j
public class FileServiceImpl implements FileService {

    private final FileRepository fileRepository;
    private final UserRepository userRepository;

    private final UserFileDbRepository userFileDbRepository;
    @Value("${amazon.aws.s3-bucket}")
    private String bucket;

    public FileServiceImpl(FileRepository fileRepository, UserRepository userRepository, UserFileDbRepository userFileDbRepository) {
        this.fileRepository = fileRepository;
        this.userRepository = userRepository;
        this.userFileDbRepository = userFileDbRepository;
    }

    @Override
    public String userUploadFile(String userId, UserFileDto userFileDto, MultipartFile file) {
        log.info(String.format("UserId ----> %s", userId));
        log.info(String.format("----> %s", userFileDto));
        byte[] fileBytes;
        try {
            fileBytes = file.getBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String fileName = file.getOriginalFilename();
        String contentType = file.getContentType();
        long fileSize = file.getSize() / 1024;
        String fileUrl = String.format("http://localhost:4566/%s/%s/%s", bucket, userId, fileName);

        log.info(String.format("FILE-NAME ----> %s", fileName));
        log.info(String.format("CONTENT-TYPE ----> %s", contentType));
        log.info(String.format("FILE-SIZE ----> %s", fileSize));

        UserFile userFile = UserFile
                .builder()
                .userId(userId)
                .fileName(fileName)
                .contentType(contentType)
                .sizeInKb(fileSize)
                .updated(LocalDateTime.now().toString())
                .fileUrl(fileUrl)
                .tags(userFileDto.getTags())
                .date(userFileDto.getDate())
                .description(userFileDto.getDescription())
                .location(userFileDto.getLocation())
                .uriLocal(userFileDto.getUri())
                .build();

        UserInfo userInfo = userRepository.getRecordById(userId);
        if (userInfo.getUsedSpace() + fileSize >= userInfo.getMaxSpace())
            return null;

        userFileDbRepository.saveRecord(userFile);
        log.info(String.format("USER FILE ----> %s", userFile.toString()));
        // Tăng giá trị fileCount của imgCount
        userRepository.updateRecord(userId, u -> {
            u.setUsedSpace(u.getUsedSpace() + fileSize);
            u.setImgCount(u.getImgCount() + 1);
        });
        fileRepository.uploadFile(userId, fileName, contentType, fileBytes);
        return fileUrl;
    }

    @Override
    public void userDeleteFile(String userId, String fileId) {

    }

    @Override
    public void testUploadFile(MultipartFile file) {
//        fileRepository.createFolder("9fb8b578-3fd0-4794-bff0-557659afbcfc");
        try {
            fileRepository.uploadFile("9fb8b578-3fd0-4794-bff0-557659afbcfc", file.getName(), file.getContentType(), file.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
