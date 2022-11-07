package com.example.uploadingfiles;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

import com.example.uploadingfiles.dao.RecordsRepository;
import com.example.uploadingfiles.model.RecordModel;
import com.example.uploadingfiles.storage.StorageFileNotFoundException;
import com.example.uploadingfiles.storage.StorageService;


@Controller
public class FileUploadController {

    @Autowired
    RecordsRepository recordsRepo;
	
	private final StorageService storageService;

	@Autowired
	public FileUploadController(StorageService storageService) {
		this.storageService = storageService;
	}

	@GetMapping("/upload")
	public String listUploadedFiles(Model model) throws IOException {

		model.addAttribute("files", storageService.loadAll().map(
				path -> MvcUriComponentsBuilder.fromMethodName(FileUploadController.class,
						"serveFile", path.getFileName().toString()).build().toUri().toString())
				.collect(Collectors.toList()));

		return "uploadForm";
	}

	@GetMapping("/files/{filename:.+}")
	@ResponseBody
	public ResponseEntity<Resource> serveFile(@PathVariable String filename) {

		Resource file = storageService.loadAsResource(filename);
		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
				"attachment; filename=\"" + file.getFilename() + "\"").body(file);
	}

	@PostMapping("/upload")
	public String handleFileUpload(@RequestParam("file") MultipartFile file,
			RedirectAttributes redirectAttributes) {
		storageService.store(file);
		redirectAttributes.addFlashAttribute("message",
				"You successfully uploaded " + file.getOriginalFilename() + "!");
				RecordModel model = new RecordModel();
				
				// create event type map and document list to use for uploading to database
				
				Map<String, String> eventTypeMap = model.readAllLinesArray(file);
				List<List<String>> documentList = model.splitDocuments(file);

				// can use a for loop in the even we want to define more documents within text file 
				// however, since the text file only contains two columns of documents

				Map<String,String> refinedDocumentList1 = model.readRecord(documentList.get(0));
				Map<String,String> refinedDocumentList2 = model.readRecord(documentList.get(1));

				// now we have a list of lists which contain the documents in each column. Along with their proper names
				// We use each refined document along with mapping to create a record object to save to the  database 
	
				ArrayList<RecordModel> listOfRecordsColumn1 = model.publishRecords(refinedDocumentList1, eventTypeMap);
				for(RecordModel recordToUpload : listOfRecordsColumn1){
					recordsRepo.save(recordToUpload);
				}

				ArrayList<RecordModel> listOfRecordsColumn2 = model.publishRecords(refinedDocumentList2, eventTypeMap);
				for(RecordModel recordToUpload : listOfRecordsColumn2){
					recordsRepo.save(recordToUpload);
				}
				
				return "redirect:upload";
	}





	@ExceptionHandler(StorageFileNotFoundException.class)
	public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
		return ResponseEntity.notFound().build();
	}

}
