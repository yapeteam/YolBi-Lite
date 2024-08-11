package cn.yapeteam.yolbi.server.handlers.modules;

import cn.yapeteam.yolbi.YolBi;
import cn.yapeteam.yolbi.module.Module;
import cn.yapeteam.yolbi.module.setting.Setting;
import cn.yapeteam.yolbi.module.setting.impl.ButtonSetting;
import cn.yapeteam.yolbi.module.setting.impl.ModeSetting;
import cn.yapeteam.yolbi.module.setting.impl.SliderSetting;
import cn.yapeteam.yolbi.server.utils.ValueUtil;
import cn.yapeteam.yolbi.utils.web.URLUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class ModuleSettingsHttpHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        String moduleName = URLUtil.getValues(httpExchange)[0];

        JsonObject jsonObject = new JsonObject();
        boolean isFound = false;

        for (Module module : YolBi.instance.getModuleManager().getModules()) {
            if (module.getName().equalsIgnoreCase(moduleName)) {
                JsonArray moduleJsonArray = new JsonArray();
                isFound = true;
                for (final Setting setting : module.getSettings()) {
                    if (!setting.visibleCheck.get()) continue;
                    JsonObject moduleSet = new JsonObject();
                    /*if (setting instanceof StringValue) {
                        moduleSet.addProperty("name", setting.getName());
                        moduleSet.addProperty("type", "input");
                        moduleSet.addProperty("value", ((StringValue) setting).getValue());
                    } else*/
                    if (setting instanceof SliderSetting) {
                        moduleSet.addProperty("name", setting.getName());
                        moduleSet.addProperty("type", "slider");
                        SliderSetting sliderSetting = (SliderSetting) setting;
                        moduleSet.addProperty("min", sliderSetting.getMin());
                        moduleSet.addProperty("max", sliderSetting.getMax());
                        moduleSet.addProperty("step", sliderSetting.getIntervals());
                        moduleSet.addProperty("value", sliderSetting.getInput());
                        moduleSet.addProperty("suffix", /*((NumberValue<?>) setting).getSuffix()*/"");
                    } else if (setting instanceof ModeSetting) {
                        moduleSet.addProperty("name", setting.getName());
                        moduleSet.addProperty("type", "mode");
                        JsonArray values = new JsonArray();
                        ModeSetting modeSetting = (ModeSetting) setting;
                        values.addAll(ValueUtil.getAllSubValuesAsJson(modeSetting));
                        moduleSet.add("values", values);
                        moduleSet.addProperty("value", URLUtil.encode(modeSetting.getOptions()[(int) modeSetting.getInput()]));
                    } /*else if (setting instanceof ListValue) {
                        moduleSet.addProperty("name", setting.getName());
                        moduleSet.addProperty("type", "radio");
                        moduleSet.addProperty("value", setting.getValue().toString());
                        JsonArray values = new JsonArray();
                        values.addAll(((ListValue<?>) setting).getSubValuesAsJson());
                        moduleSet.add("values", values);
                    } */ else if (setting instanceof ButtonSetting) {
                        moduleSet.addProperty("name", setting.getName());
                        moduleSet.addProperty("type", "checkbox");
                        moduleSet.addProperty("value", ((ButtonSetting) setting).isToggled());
                    } /*else if (setting instanceof BoundsNumberValue) {
                        moduleSet.addProperty("name", setting.getName());
                        moduleSet.addProperty("type", "range_slider");
                        moduleSet.addProperty("min", ((BoundsNumberValue) setting).getMin().doubleValue());
                        moduleSet.addProperty("max", ((BoundsNumberValue) setting).getMax().doubleValue());
                        moduleSet.addProperty("step", 1);
                        moduleSet.addProperty("minvalue", ((BoundsNumberValue) setting).getValue().doubleValue());
                        moduleSet.addProperty("maxvalue", ((BoundsNumberValue) setting).getSecondValue().doubleValue());
                        moduleSet.addProperty("suffix", ((BoundsNumberValue) setting).getSuffix());
                    }*/// else if (setting instanceof ColorValue) {
                    // moduleSet.addProperty("name", setting.getName());
                    // moduleSet.addProperty("type", "color");
                    // JsonArray Color = new JsonArray();
                    // Color.add(new JsonPrimitive(((ColorValue) setting).getValue().getRed()));
                    // Color.add(new JsonPrimitive(((ColorValue) setting).getValue().getGreen()));
                    // Color.add(new JsonPrimitive(((ColorValue) setting).getValue().getBlue()));
                    // Color.add(new JsonPrimitive(((ColorValue) setting).getValue().getAlpha()));
                    // moduleSet.add("value", Color);
                    //}
                    moduleJsonArray.add(moduleSet);
                }
                jsonObject.add("result", moduleJsonArray);
            }
        }

        jsonObject.addProperty("success", isFound);

        if (!isFound) jsonObject.addProperty("reason", "Can't find module");

        byte[] response = jsonObject.toString().getBytes(StandardCharsets.UTF_8);

        // Set CORS headers
        httpExchange.getResponseHeaders().set("Content-Type", "application/json");
        httpExchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");  // Allow requests from any origin
        httpExchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, OPTIONS");
        httpExchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Origin, Content-Type, Accept, Authorization");

        httpExchange.sendResponseHeaders(200, response.length);

        OutputStream out = httpExchange.getResponseBody();
        out.write(response);
        out.flush();
        out.close();
    }
}
