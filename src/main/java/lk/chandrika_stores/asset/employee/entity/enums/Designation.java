package lk.chandrika_stores.asset.employee.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Designation {
  ADMIN("Admin"),
  MANAGER("Manager"),
  PROCUREMENT_MANAGER("Stock Keeper"),
  HR_MANAGER("HR Manager"),
  ACCOUNT_MANAGER("Accountant"),
  CASHIER("Cashier");

  private final String designation;
}
