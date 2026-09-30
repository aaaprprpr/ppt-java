package model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class PPTImage extends PPTObject {
    private String imagePath;// 图片路径
    private String src;// 图片源
    public PPTImage() {
        this.type = "image";
    }
}
