package cn.yapeteam.yolbi.server.handlers.modules;

import cn.yapeteam.loader.logger.Logger;
import cn.yapeteam.yolbi.YolBi;
import cn.yapeteam.yolbi.module.Module;
import cn.yapeteam.yolbi.utils.web.URLUtil;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;

public class ModulesHttpHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {

        try{
            // Extract the category from the request URL
            String category = URLUtil.getValues(httpExchange)[0];

            // Create a JSON object to store the response
            JsonObject response = new JsonObject();

            // Iterate through modules and add relevant information to the response
            for (Module module : YolBi.instance.getModuleManager().getAll()) {
                if (module.getModuleInfo().category().toString().equalsIgnoreCase(category)){
                    JsonObject moduleJson = new JsonObject();
                    moduleJson.addProperty("name", module.getModuleInfo().aliases()[0]);
                    moduleJson.addProperty("description", module.getModuleInfo().description());
                    moduleJson.addProperty("Enabled",module.isEnabled());
                    Logger.info("Module found" + module.getModuleInfo().aliases()[0]);
                    response.add(module.getModuleInfo().aliases()[0], moduleJson);
                }
            }


            // Send the JSON response with CORS headers
            sendJsonResponse(httpExchange, 200, response);
        }catch (Exception e){
            Logger.exception(e);
        }

    }

    private void sendJsonResponse(HttpExchange httpExchange, int statusCode, JsonObject response) throws IOException {
        // Convert JSON to string
        String jsonResponse = response.toString();
        byte[] jsonResponseBytes = jsonResponse.getBytes();

        // Set CORS headers
        httpExchange.getResponseHeaders().set("Content-Type", "application/json");
        httpExchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");  // Allow requests from any origin
        httpExchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, OPTIONS");
        httpExchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Origin, Content-Type, Accept, Authorization");

        // Send the response status and length
        httpExchange.sendResponseHeaders(statusCode, jsonResponseBytes.length);

        // Write the JSON response to the output stream
        try (OutputStream outputStream = httpExchange.getResponseBody()) {
            outputStream.write(jsonResponseBytes);
        }
    }
}
