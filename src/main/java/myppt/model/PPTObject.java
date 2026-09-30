package myppt.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.Setter;
import java.util.UUID;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = PPTText.class, name = "text"),
    @JsonSubTypes.Type(value = PPTRect.class, name = "rect"),
    @JsonSubTypes.Type(value = PPTCircle.class, name = "circle"),
    @JsonSubTypes.Type(value = PPTLine.class, name = "line"),
    @JsonSubTypes.Type(value = PPTImage.class, name = "image")
})
@Getter
@Setter
public abstract class PPTObject {
    private String id = UUID.randomUUID().toString();
    private double x;
    private double y;
    private double width;
    private double height;
}
