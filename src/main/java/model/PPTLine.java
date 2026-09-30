package model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class PPTLine extends PPTObject {

    private double x2;     // 直线的终点
    private double y2;
    private String strokeColor = "#000000";// 线条颜色

    public PPTLine() {
        this.type = "line";
    }

    public PPTLine(double x1, double y1, double x2, double y2) {
        this.type = "line";
        this.x = x1;
        this.y = y1;
        this.x2 = x2;
        this.y2 = y2;

        this.width = 0;
        this.height = 0;
    }

    @Override
    public double getWidth() {
        return 0;
    }

    @Override
    public double getHeight() {
        return 0;
    }
}
