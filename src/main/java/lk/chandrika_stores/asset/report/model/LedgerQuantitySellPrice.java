package lk.chandrika_stores.asset.report.model;

import lk.chandrika_stores.asset.ledger.entity.Ledger;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LedgerQuantitySellPrice {
  private Ledger ledger;
  private Integer counter;
  private BigDecimal amount;
}
