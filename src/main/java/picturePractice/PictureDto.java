package picturePractice;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PictureDto {
	String savedName;
	String ext;
	String originalName;
	String savedPath;
	long volume;
}
