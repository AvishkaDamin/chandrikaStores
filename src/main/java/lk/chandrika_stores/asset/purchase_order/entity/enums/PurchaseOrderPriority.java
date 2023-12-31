package lk.chandrika_stores.asset.purchase_order.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PurchaseOrderPriority {
    HIGH("Immediate"),
    MEDIUM("Medium"),
    NORMAL("Normal");
    private final String purchaseOrderPriority;
}
