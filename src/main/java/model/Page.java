package model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
@Data
@NoArgsConstructor
public class Page {
    private int id;// 页码
    private String name;// 页名
    private List<PPTObject> objects = new ArrayList<>();// 对象列表
    private String backgroundColor = "white";// 背景色
    private boolean dirty = true;// 页面是否被修改
}
