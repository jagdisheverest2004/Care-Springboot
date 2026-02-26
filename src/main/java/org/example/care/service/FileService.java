package org.example.care.service;

import java.io.IOException;
import org.example.care.model.MedicalRecord;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileService {

    public void storeFile(MedicalRecord medicalRecord, MultipartFile file) {
        try {
            medicalRecord.setFileData(file.getBytes());
        } catch (IOException ex) {
            throw new IllegalArgumentException("Failed to read file data");
        }
    }

    public byte[] readFile(MedicalRecord medicalRecord) {
        if (medicalRecord.getFileData() == null || medicalRecord.getFileData().length == 0) {
            throw new IllegalArgumentException("File data not found");
        }
        return medicalRecord.getFileData();
    }
}
