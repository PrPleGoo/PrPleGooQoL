package prplegoo.regions.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.HashMap;

@Data
@Builder
@EqualsAndHashCode
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@SuppressWarnings("InnerClassMayBeStatic")
public class RDFoodConsumptionData {
    @SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
    public HashMap<Integer, HashMap<Integer, Boolean>> data;

    public RDFoodConsumptionData(HashMap<Integer, HashMap<Integer, Boolean>> data) {
        this.data = data;
    }
}
