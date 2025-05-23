package com.example.labadaptor.model.values;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FreeT4Value {

    @NotNull(message = "Value cannot be null")
    private Double value;

    @NotBlank(message = "Unit cannot be blank")
    private String unit;

    @NotNull(message = "Reference range cannot be null")
    @Valid
    private ReferenceRange referenceRange;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReferenceRange {
        @NotNull(message = "Min value cannot be null")
        private Double min;
        @NotNull(message = "Max value cannot be null")
        private Double max;
    }
}
