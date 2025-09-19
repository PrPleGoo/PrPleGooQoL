package prplegoo.regions.persistence.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@SuppressWarnings("InnerClassMayBeStatic")
public class SellingData {
    public double[][] sold;

    public SellingData(double[][] sold) {
        this.sold = sold;
    }
}

