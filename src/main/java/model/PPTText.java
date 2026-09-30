package model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class PPTText extends PPTObject {
    private String text;      // 文本内容
    private int fontSize;     // 字号
    private String fontColor = "#000000";// 字体颜色
    private String fontFamily = "SansSerif";// 字体
    public PPTText() {
        this.type = "text";
        this.text = "";
        this.fontSize = 20;
    }
}
