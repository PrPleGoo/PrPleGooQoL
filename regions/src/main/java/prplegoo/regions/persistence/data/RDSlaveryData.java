package prplegoo.regions.persistence.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.HashMap;

@Data
@EqualsAndHashCode
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RDSlaveryData {
    public HashMap<Integer, HashMap<Integer, Boolean>> data;
    public double[][] slaveDelivery;

    public RDSlaveryData(HashMap<Integer, HashMap<Integer, Boolean>> data,
                         double[][] slaveDelivery) {
        this.data = data;
        this.slaveDelivery = slaveDelivery;
    }
}
