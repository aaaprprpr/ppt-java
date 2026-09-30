package model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class PPTRect extends PPTObject {
    private String fillColor = "#DDDDDD";// 填充颜色
    private String strokeColor = "#000000";// 描边颜色
    private boolean fillEnabled = false;// 是否填充
    public PPTRect() {
        this.type = "rect";
    }
}
