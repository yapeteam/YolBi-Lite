package cn.yapeteam.yolbi.server.utils;

import cn.yapeteam.yolbi.module.setting.impl.ModeSetting;
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;

public class ValueUtil {
    public static JsonArray getAllSubValuesAsJson(ModeSetting modeValue) {
        JsonArray jsonArray = new JsonArray();
        for (String mode : modeValue.getOptions())
            jsonArray.add(new JsonPrimitive(mode));
        return jsonArray;
    }
}
