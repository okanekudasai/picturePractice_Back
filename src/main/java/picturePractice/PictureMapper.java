package picturePractice;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PictureMapper {

	void uploadOneFile(PictureDto dto);

	List<PictureDto> downloadAllPicture();

	int getTotalVolume();

	String getadm();

	void clearAllProcess();

}
