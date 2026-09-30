package myppt.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PPTLine extends PPTObject {
    private double x2;
    private double y2;
    private String strokeColor = "#000000";
}
