package cn.yapeteam.yolbi.event.impl.client;

import cn.yapeteam.yolbi.event.type.CancellableEvent;
import cn.yapeteam.yolbi.module.Module;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EventModuleToggle extends CancellableEvent {
    Module module;
}
