package cn.yapeteam.yolbi.module.impl.ghost;

import cn.yapeteam.yolbi.event.Listener;
import cn.yapeteam.yolbi.event.impl.player.EventMouseOver;
import cn.yapeteam.yolbi.module.Module;
import cn.yapeteam.yolbi.module.api.Category;
import cn.yapeteam.yolbi.module.api.ModuleInfo;
import cn.yapeteam.yolbi.module.api.value.impl.BoundsNumberValue;
import cn.yapeteam.yolbi.utils.math.MathUtils;

import java.util.Random;


@ModuleInfo(aliases = {"module.ghost.reach.name", "Long Reach", "Extra Reach", "Reach"}, description = "module.ghost.reach.description", category = Category.GHOST)
public class Reach extends Module {
    BoundsNumberValue reach = new BoundsNumberValue("Reach", this, 3.2,3.5,3.0,6.0,1);

    BoundsNumberValue reachpossiblity = new BoundsNumberValue("Reach Possibility", this, 80,90,10,100,10);

    Random random = new Random();

    @Listener
    public void onEventMouseOver(EventMouseOver event) {
        if(random.nextInt(100) > MathUtils.getRandom(reachpossiblity.getValue().doubleValue(), reachpossiblity.getSecondValue().doubleValue()))
            return;
        event.setReach((float) MathUtils.getRandom(reach.getValue().doubleValue(), reach.getSecondValue().doubleValue()));
    }
}
