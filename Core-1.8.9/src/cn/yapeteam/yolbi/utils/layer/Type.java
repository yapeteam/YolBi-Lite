package cn.yapeteam.yolbi.utils.layer;


import cn.yapeteam.yolbi.utils.render.shader.impl.BloomShader;
import cn.yapeteam.yolbi.utils.render.shader.impl.BlurShader;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Type {
    REGULAR(null),
    BLOOM(BloomShader.class),
    BLUR(BlurShader.class);

    private final Class<?> shader;
}