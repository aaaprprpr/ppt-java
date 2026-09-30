package myppt.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class PPTShape extends PPTObject {
    private String fillColor = "#DDDDDD";
    private String strokeColor = "#000000";
    private boolean fillEnabled;
}
