package cn.yapeteam.yolbi.module.impl.render;

import cn.yapeteam.yolbi.module.Module;
import cn.yapeteam.yolbi.module.api.Category;
import cn.yapeteam.yolbi.module.api.ModuleInfo;
import cn.yapeteam.yolbi.value.impl.*;

import java.awt.*;


@ModuleInfo(name = "Test", description = "Test", category = Category.EXPLOIT)
public class Test extends Module {

    private final BooleanValue testboolean = new BooleanValue("Test", this, true);

    private final BoundsNumberValue testBounds = new BoundsNumberValue("TestBounds", this, 50, 100, 0, 500, 20);

    private final NumberValue testnumber = new NumberValue("TestNumber", this, 50, 0, 100, 1);

    private final StringValue testString = new StringValue("TestString", this, "Test");

    private final ColorValue testColor = new ColorValue("TestColor", this, new Color(255, 255, 255, 255));



}


