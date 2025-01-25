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
public class RDSlaveryData {
    @SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
    public HashMap<Integer, HashMap<Integer, Boolean>> data;
    @SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
    public double[][] slaveDelivery;

    public RDSlaveryData(HashMap<Integer, HashMap<Integer, Boolean>> data,
                         double[][] slaveDelivery) {
        this.data = data;
        this.slaveDelivery = slaveDelivery;
    }
}
