package picturePractice;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@RestController
@CrossOrigin
public class PictureController {
	@Autowired
	PictureMapper mapper;
	
	String fileSeparator = File.separator;
	
	// 총 용량을 구하는 메서드에요
	public int totalVolume() {
		// 데이터베이스에 값이 없으면 에러가 발생해요 int는 null일 수 없거든요
		try {
			return mapper.getTotalVolume();
		} catch(Exception e) {
			return 0;
		}
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
			byte[] fileContent = FileUtils.readFileToByteArray(new File(bean.getSavedPath() + fileSeparator + bean.getSavedName() + "." + bean.getExt()));
			//바이트스트림을 스트링으로 변환해서 리스트에 담아요
			HashMap<String, String> map = new HashMap<>();
			map.put("content", Base64.getEncoder().encodeToString(fileContent));
			map.put("ext", bean.getExt());
			map.put("fileName", bean.getOriginalName());
			fileContentList.add(map);
		}
		return fileContentList;
	}
	
	@PostMapping("/admConfirm")
	public int admConfirm(@RequestBody String adm) {
		String confirm = mapper.getadm();
		if (adm.equals(confirm)) return 1;
		else return 0;
	}
	
	@GetMapping("/getToken")
	public String getToken(@Value("${secret.key}") String secretKey) {
		Date now = new Date();
		Date expiryDate = new Date(now.getTime() + 86400000);
		return Jwts.builder()
				.setSubject("access_key")
				.setIssuedAt(new Date())
				.setExpiration(expiryDate)
				//만약 claim을 넣고 싶다면
				//.claim("user", "admin")
				.signWith(SignatureAlgorithm.HS512, secretKey)
				.compact();
	}
	
	@GetMapping("/clearAllProcess")
	public void clearAllProcess(@Value("${file.path}") String filePath) {
		// 이제 폴더안에 있는 파일과 데이터베이스의 파일 정보를 모두 지워요/
		// 데이터베이스의 파일정보를 지워요
		mapper.clearAllProcess();
		// 폴더안의 내용을 지워요
		File folder = new File(filePath);
		File[] files = folder.listFiles();
		for (File file : files) {
			file.delete();
		}
	}
	
	@PostMapping("/tokenConfirm")
	public int tokenConfirm(@Value("${secret.key}") String secretKey, @RequestBody String token) {
		try {
			Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
			return 1;
		} catch(Exception e) {
			return 0;
		}
		//만약 claim을 알고 싶다면
		//Claim claim = Jwts.parser().setSigningKey(@Value(${secret.key})).parseClaimsJws(token).getBody();
		//System.out.println(claim.getUser());
	}
	
}
