package model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class PPTCircle extends PPTObject {
    private String fillColor = "#DDDDDD";//填充颜色
    private String strokeColor = "#000000";//边框颜色
    private boolean fillEnabled = false;//是否填充
    public PPTCircle() {
        this.type = "circle";
    }
}
