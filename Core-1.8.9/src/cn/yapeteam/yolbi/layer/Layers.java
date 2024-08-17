package cn.yapeteam.yolbi.layer;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Layers {
    BLOOM(Type.BLOOM),
    BLUR(Type.BLUR),
    REGULAR(Type.REGULAR);

    final Type type;
}
