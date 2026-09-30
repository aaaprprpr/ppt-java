package myppt.model;

import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Page {
    private int id;
    private String name;
    private List<PPTObject> objects = new ArrayList<>();
    private String backgroundColor = "white";
}
