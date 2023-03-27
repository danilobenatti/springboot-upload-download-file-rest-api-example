package net.guides.springboot.springbootfileupload.service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import net.guides.springboot.springbootfileupload.exception.FileNotFoundException;
import net.guides.springboot.springbootfileupload.exception.FileStorageException;
import net.guides.springboot.springbootfileupload.property.FileStorageProperties;

@Service
public class FileStorageService {
	
	private final Path fileStorageLocation;
	
	public FileStorageService(FileStorageProperties fileStorageProperties) {
		
		this.fileStorageLocation = Paths
				.get(fileStorageProperties.getUploadDir()).toAbsolutePath()
				.normalize();
		try {
			Files.createDirectories(this.fileStorageLocation);
		} catch (IOException ex) {
			throw new FileStorageException(
					"Could not create the directory where the uploaded files will be stored.",
					ex);
		}
	}
	
	public String storeFile(MultipartFile file) {
		
		String fileName = StringUtils.cleanPath(file.getOriginalFilename());
		
		try {
			if (fileName.contains("..")) {
				throw new FileStorageException(String.format(
						"Filename contains invalid path sequence %s",
						fileName));
			}
			Path targetLocation = this.fileStorageLocation.resolve(fileName);
			Files.copy(file.getInputStream(), targetLocation,
					StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException ex) {
			throw new FileStorageException(String.format(
					"Could not store file %s. Try again!", fileName), ex);
		}
		return fileName;
	}
	
	public Resource loadFileAsResource(String fileName) {
		
		Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
		
		String message = String.format("File not found %s", fileName);
		try {
			Resource resource = new UrlResource(filePath.toUri());
			if (resource.exists()) {
				return resource;
			} else {
				throw new FileNotFoundException(message);
			}
		} catch (MalformedURLException ex) {
			throw new FileNotFoundException(message, ex);
		}
	}
	
}
