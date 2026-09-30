package model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
@Data
@NoArgsConstructor
public class Document {
    private String title = "未命名";// 文档标题
    private List<Page> pages = new ArrayList<>();// 页面列表
    private String id;// 文档ID
}
