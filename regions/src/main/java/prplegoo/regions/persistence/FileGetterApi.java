package prplegoo.regions.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import snake2d.LOG;
import snake2d.util.file.FileGetter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class FileGetterApi implements IFileLoad {
    private HashMap<String, JsonNode> loadedData;

    @Override
    public <T> T get(IDataPersistence<T> dataPersistence) {
        JsonNode json = loadedData.get(dataPersistence.getKey());

        String jsonString = json != null ? json.toString() : "";

        try {
            return ObjectMapperFactory.build().readValue(jsonString, dataPersistence.getDataClass());
        } catch (Exception e) {
            LOG.err("Failed to deserialize json with key: " + dataPersistence.getKey() + ", value: " + jsonString);
            return null;
        }
    }

    public void onGameLoaded(FileGetter fileGetter) throws IOException {
        if (fileGetter == null) {
            return;
        }

        int length = fileGetter.i();
        byte[] jsonBytes = new byte[length];
        fileGetter.bs(jsonBytes);

        String json = new String(jsonBytes, StandardCharsets.UTF_8);

        loadedData = ObjectMapperFactory.build().readValue(json, new TypeReference<HashMap<String, JsonNode>>() {
        });
    }
}
