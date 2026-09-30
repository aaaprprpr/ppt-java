package model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = PPTText.class,  name = "text"),
        @JsonSubTypes.Type(value = PPTRect.class,  name = "rect"),
        @JsonSubTypes.Type(value = PPTLine.class,  name = "line"),
        @JsonSubTypes.Type(value = PPTImage.class, name = "image"),
        @JsonSubTypes.Type(value = PPTCircle.class,name = "circle")
})

@Data
@NoArgsConstructor
public abstract class PPTObject {
    protected String id;         // 每个对象唯一ID
    protected String type;       // 对象类型：text/rect/ellipse/line/image
    protected double x;         // 对象左上角坐标
    protected double y;         // 对象左上角坐标
    protected double width;     // 宽高
    protected double height;
    protected double rotation = 0.0;// 旋转角度
    protected int zIndex = 0;// z-index（以后要做置顶/置底用）
}
