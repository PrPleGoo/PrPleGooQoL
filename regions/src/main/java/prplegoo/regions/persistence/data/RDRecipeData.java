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
public class RDRecipeData {
    public byte[][][] enabledRecipeIndex;

    public RDRecipeData(byte[][][] enabledRecipeIndex) {
        this.enabledRecipeIndex = enabledRecipeIndex;
    }
}

