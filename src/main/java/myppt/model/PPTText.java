package myppt.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PPTText extends PPTObject {
    private String text = "";
    private int fontSize = 20;
    private String fontColor = "#000000";
    private String fontFamily = "SansSerif";
}
