package myppt.model;

import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class Document {
    private String title = "未命名";
    private List<Page> pages = new ArrayList<>();
    private String id = UUID.randomUUID().toString();
}
