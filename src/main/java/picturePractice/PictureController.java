package picturePractice;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@CrossOrigin
public class PictureController {
	@Autowired
	PictureMapper mapper;
	
	// 총 용량을 구하는 메서드에요
	public int totalVolume() {
		return mapper.getTotalVolume();
	}
	
	// 총 용량을 구해와요
	@GetMapping("/getTotalVolume")
	public int getTotalVolume() {
		return totalVolume();
	}
	
	// 뷰에서 파일을 받아서 파일정보를 데이터베이스에, 파일자체를 로컬에 저장해요
	@PostMapping("/uploadOneFile")
	public int uploadOneFile(@Value("${file.path}") String filePath, @RequestParam("file") MultipartFile file) throws Exception {
		int totalVolume = totalVolume();
		//만약 총 사진 용량이 1,000,000보다 크면 더이상 사진을 저장하지 않아요
		if ((long)totalVolume + file.getSize() > (long)(1024*1024*1024)) {
			return 0;
		}
		//파일의 원래 이름을 얻어요
		String originalName = file.getOriginalFilename();
		//파일의 확장자 정보를 얻어요
		String ext = originalName.substring(originalName.lastIndexOf(".") + 1);
		//파일 정보를 데이터베이스에 넣어요
		PictureDto dto = new PictureDto(UUID.randomUUID().toString(), ext, originalName, filePath, file.getSize());
		mapper.uploadOneFile(dto);
		//실제로 파일을 로컬에 저장해요
		file.transferTo(new File(filePath, dto.getSavedName() + "." + ext));
		return 1;
	}
	
	//모든 파일을 불러와요
	@GetMapping("/downloadAllPicture")
	public List<HashMap<String, String>> downloadAllPicture() throws IOException {		
		//데이터베이스에서 모든 파일의 정보를 불러와요
		List<PictureDto> list = mapper.downloadAllPicture();
		
		//파일정보를 스트링으로 변환해서 담을 리스트를 만들어요
		List<HashMap<String, String>> fileContentList = new ArrayList<>();
		for (PictureDto bean : list) {
			//파일 정보를 바이트스트림으로 변환해요
			byte[] fileContent = FileUtils.readFileToByteArray(new File(bean.getSavedPath() + "\\" + bean.getSavedName() + "." + bean.getExt()));
			//바이트스트림을 스트링으로 변환해서 리스트에 담아요
			HashMap<String, String> map = new HashMap<>();
			map.put("content", Base64.getEncoder().encodeToString(fileContent));
			map.put("ext", bean.getExt());
			map.put("fileName", bean.getOriginalName());
			fileContentList.add(map);
		}
		return fileContentList;
	}
	
}
