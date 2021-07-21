package org.getcarebase.carebase.models;

import android.text.InputType;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import org.json.JSONArray;

import java.lang.reflect.Type;

/**
 * converts a DeviceModel in json format into a DeviceModel Java object
 */
public class DeviceModelGUDIDDeserializer implements JsonDeserializer<DeviceModel> {

    @Override
    public DeviceModel deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        DeviceModel deviceModel = new DeviceModel();
        DeviceProduction deviceProduction = new DeviceProduction();

        JsonObject jsonObject = json.getAsJsonObject();
        JsonObject gudidObject = jsonObject.getAsJsonObject("gudid");
        JsonObject deviceObject = gudidObject.getAsJsonObject("device");

        if (jsonObject.has("udi")) {
            // get device production information
            JsonObject udiObject = jsonObject.getAsJsonObject("udi");
            deviceModel.setDeviceIdentifier(udiObject.getAsJsonPrimitive("di").getAsString());
            deviceProduction.setUniqueDeviceIdentifier(udiObject.getAsJsonPrimitive("udi").getAsString());
            // TODO use `expirationDateFormat` to format expiration date
            if (udiObject.get("expirationDate") != null && !udiObject.get("expirationDate").isJsonNull()) {
                deviceProduction.setExpirationDate(udiObject.getAsJsonPrimitive("expirationDate").getAsString());
            }
            if (udiObject.get("lotNumber") != null && !udiObject.get("lotNumber").isJsonNull()) {
                deviceProduction.setLotNumber(udiObject.getAsJsonPrimitive("lotNumber").getAsString());
            }
            if (deviceObject.get("catalogNumber") != null && !deviceObject.get("catalogNumber").isJsonNull()) {
                deviceProduction.setReferenceNumber(deviceObject.getAsJsonPrimitive("catalogNumber").getAsString());
            }
            deviceProduction.setQuantity(deviceObject.getAsJsonPrimitive("deviceCount").getAsInt());

            deviceModel.addDeviceProduction(deviceProduction);
        }

        // get device model information
        deviceModel.setCompany(deviceObject.getAsJsonPrimitive("companyName").getAsString());
        deviceModel.setName(deviceObject.getAsJsonObject("gmdnTerms").getAsJsonArray("gmdn").get(0).getAsJsonObject().getAsJsonPrimitive("gmdnPTName").getAsString());
        if (deviceObject.has("deviceDescription") && !deviceObject.get("deviceDescription").isJsonNull()) {
            deviceModel.setDescription(deviceObject.getAsJsonPrimitive("deviceDescription").getAsString());
        }
        deviceModel.setProductCode(deviceObject.getAsJsonObject("productCodes").getAsJsonArray("fdaProductCode").get(0).getAsJsonObject().get("productCode").getAsString());

        // add device sizes to specifications
        if (deviceObject.has("deviceSizes") && !deviceObject.get("deviceSizes").isJsonNull()) {
            JsonArray deviceSizeArray = deviceObject.getAsJsonObject("deviceSizes").getAsJsonArray("deviceSize");
            for (int i = 0; i < deviceSizeArray.size(); ++i) {
                int colonIndex;
                String v;
                JsonObject currentSizeObject = deviceSizeArray.get(i).getAsJsonObject();
                String specification = currentSizeObject.getAsJsonPrimitive("sizeType").getAsString();
                if (specification.equals("Device Size Text, specify")) {
                    String customSizeText = currentSizeObject.getAsJsonPrimitive("sizeText").getAsString();
                    // Key, Value usually separated by colon
                    colonIndex = customSizeText.indexOf(":");
                    if (colonIndex == -1) {
                        // If no colon, save whole field as "value"
                        specification = "Custom Key";
                        v = customSizeText;
                    } else {
                        specification = customSizeText.substring(0, colonIndex);
                        v = customSizeText.substring(colonIndex + 1).trim();
                    }
                } else {
                    v = currentSizeObject.getAsJsonObject("size").getAsJsonPrimitive("value").getAsString()
                            + " "
                            + currentSizeObject.getAsJsonObject("size").getAsJsonPrimitive("unit").getAsString();
                }
                deviceModel.addSpecification(specification,v);
            }
        }

        return deviceModel;
    }
}
