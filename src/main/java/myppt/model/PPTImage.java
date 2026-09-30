package myppt.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PPTImage extends PPTObject {
    private String imagePath;
    private String src;
}
