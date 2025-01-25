package prplegoo.regions.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import snake2d.LOG;
import snake2d.util.file.FilePutter;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class FilePutterApi implements IFileSave {
    private final HashMap<String, Object> dataToWrite;

    public FilePutterApi() {
        dataToWrite = new HashMap<>();
    }

    @Override
    public void put(String key, Object data) {
        dataToWrite.put(key, data);
    }

    public void onGameSaved(FilePutter filePutter) {
        if(filePutter == null){
            return;
        }

        try {
            byte[] json = ObjectMapperFactory.build()
                    .writeValueAsString(dataToWrite)
                    .getBytes(StandardCharsets.UTF_8);
            filePutter.bsE(json);
        }
        catch(JsonProcessingException e){
            LOG.err("Can't serialize save data: " + e.getMessage());
        }
    }
}

