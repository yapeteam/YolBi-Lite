package cn.yapeteam.yolbi.event.impl.game;

import cn.yapeteam.yolbi.event.type.CancellableEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EventKey extends CancellableEvent {
    private int key;
}