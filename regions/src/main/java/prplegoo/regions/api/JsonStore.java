package prplegoo.regions.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Data
@Builder
@EqualsAndHashCode
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@SuppressWarnings("InnerClassMayBeStatic")
public class JsonStore {
    @SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
    public int[][] data;

    public JsonStore(int[][] data) {
        this.data = data;
    }
}
