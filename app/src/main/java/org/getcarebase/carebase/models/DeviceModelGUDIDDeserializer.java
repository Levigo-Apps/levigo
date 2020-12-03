package org.getcarebase.carebase.models;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

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
        // get device production information
        JsonObject udiObject = jsonObject.getAsJsonObject("udi");
        deviceProduction.setUniqueDeviceIdentifier(udiObject.getAsJsonPrimitive("udi").getAsString());
        // TODO use `expirationDateFormat` to format expiration date
        deviceProduction.setExpirationDate(udiObject.getAsJsonPrimitive("expirationDate").getAsString());
        deviceProduction.setLotNumber(udiObject.getAsJsonPrimitive("lotNumber").getAsString());


        JsonObject gudidObject = jsonObject.getAsJsonObject("gudid");
        JsonObject deviceObject = gudidObject.getAsJsonObject("device");

        // get device model information
        deviceModel.setDeviceIdentifier(udiObject.getAsJsonPrimitive("di").getAsString());
        deviceModel.setCompany(deviceObject.getAsJsonPrimitive("companyName").getAsString());
        deviceModel.setCompany(deviceObject.getAsJsonPrimitive("companyName").getAsString());
        deviceModel.setName(deviceObject.getAsJsonObject("gmdnTerms").getAsJsonArray("gmdn").get(0).getAsJsonObject().getAsJsonPrimitive("gmdnPTName").getAsString());
        deviceModel.setDescription(deviceObject.getAsJsonPrimitive("deviceDescription").getAsString());
        deviceProduction.setReferenceNumber(deviceObject.getAsJsonPrimitive("catalogNumber").getAsString());
        // Not really sure about this but fine for now
        deviceProduction.setQuantity(deviceObject.getAsJsonPrimitive("deviceCount").getAsInt());

        // add device sizes to specifications
        if (!deviceObject.get("deviceSizes").isJsonNull()) {
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


        deviceModel.addDeviceProduction(deviceProduction);
        return deviceModel;
    }
}
