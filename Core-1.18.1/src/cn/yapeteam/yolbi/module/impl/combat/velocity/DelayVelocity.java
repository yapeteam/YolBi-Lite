package cn.yapeteam.yolbi.module.impl.combat.velocity;

import cn.yapeteam.yolbi.event.Listener;
import cn.yapeteam.yolbi.event.impl.player.EventUpdate;
import com.alan.clients.component.impl.player.PingSpoofComponent;
import com.alan.clients.module.impl.combat.Velocity;
import com.alan.clients.value.Mode;
import com.alan.clients.value.impl.BooleanValue;
import com.alan.clients.value.impl.NumberValue;

public final class DelayVelocity extends Mode<Velocity> {

    private final NumberValue delay = new NumberValue("Delay", this, 10, 1, 50, 1);
    private final BooleanValue pingSpoof = new BooleanValue("Ping Spoof", this, true);

    public DelayVelocity(String name, Velocity parent) {
        super(name, parent);
    }

    @Listener
    public void EventPreMotion(EventUpdate event) {
        PingSpoofComponent.spoof(delay.getValue().intValue() * 50, pingSpoof.getValue(), true, pingSpoof.getValue(), false);
    }

}