package lk.chandrika_stores.asset.common_asset.model;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ParameterCount {
    private String name;
    private Integer count;
    private BigDecimal sellPrice;
    private BigDecimal price;

}
