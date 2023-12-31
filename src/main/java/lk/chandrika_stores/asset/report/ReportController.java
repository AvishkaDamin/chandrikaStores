package lk.chandrika_stores.asset.report;

import lk.chandrika_stores.asset.common_asset.model.NameCount;
import lk.chandrika_stores.asset.common_asset.model.ParameterCount;
import lk.chandrika_stores.asset.common_asset.model.TwoDate;
import lk.chandrika_stores.asset.common_asset.model.enums.LiveDead;
import lk.chandrika_stores.asset.employee.entity.Employee;
import lk.chandrika_stores.asset.invoice.entity.Invoice;
import lk.chandrika_stores.asset.invoice.entity.enums.PaymentMethod;
import lk.chandrika_stores.asset.invoice.service.InvoiceService;
import lk.chandrika_stores.asset.invoice_ledger.entity.InvoiceLedger;
import lk.chandrika_stores.asset.invoice_ledger.service.InvoiceLedgerService;
import lk.chandrika_stores.asset.item.entity.Item;
import lk.chandrika_stores.asset.item.service.ItemService;
import lk.chandrika_stores.asset.ledger.entity.Ledger;
import lk.chandrika_stores.asset.ledger.service.LedgerService;
import lk.chandrika_stores.asset.payment.entity.Payment;
import lk.chandrika_stores.asset.payment.service.PaymentService;
import lk.chandrika_stores.asset.purchase_order.service.PurchaseOrderService;
import lk.chandrika_stores.asset.purchase_order_item.service.PurchaseOrderItemService;
import lk.chandrika_stores.asset.report.model.ItemSellPriceQuantityBuyingPrice;
import lk.chandrika_stores.asset.report.model.LedgerQuantitySellPrice;
import lk.chandrika_stores.asset.user_management.user.service.UserService;
import lk.chandrika_stores.util.service.DateTimeAgeService;
import lk.chandrika_stores.util.service.OperatorService;
import lombok.val;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/report")
public class ReportController {
  private final PaymentService paymentService;
  private final InvoiceService invoiceService;
  private final OperatorService operatorService;
  private final DateTimeAgeService dateTimeAgeService;
  private final UserService userService;
  private final InvoiceLedgerService invoiceLedgerService;
  private final PurchaseOrderService purchaseOrderService;
  private final ItemService itemService;
  private final LedgerService ledgerService;
  private final PurchaseOrderItemService purchaseOrderItemService;

  public ReportController(PaymentService paymentService, InvoiceService invoiceService, OperatorService operatorService,
      DateTimeAgeService dateTimeAgeService, UserService userService, InvoiceLedgerService invoiceLedgerService,
      PurchaseOrderService purchaseOrderService, ItemService itemService, LedgerService ledgerService,
      PurchaseOrderItemService purchaseOrderItemService) {
    this.paymentService = paymentService;
    this.invoiceService = invoiceService;
    this.operatorService = operatorService;
    this.dateTimeAgeService = dateTimeAgeService;
    this.userService = userService;
    this.invoiceLedgerService = invoiceLedgerService;
    this.purchaseOrderService = purchaseOrderService;
    this.itemService = itemService;
    this.ledgerService = ledgerService;
    this.purchaseOrderItemService = purchaseOrderItemService;
  }

  private String commonAll(List<Payment> payments, List<Invoice> invoices, Model model, String message,
      LocalDateTime startDateTime, LocalDateTime endDateTime) {
    // according to payment type -> invoice
    commonInvoices(invoices, model);
    // according to payment type -> payment
    commonPayment(payments, model);
    // invoice count by cashier
    commonPerCashier(invoices, model);
    // payment count by account department
    commonPerAccountUser(payments, model);
    // item count according to item
    commonPerItem(startDateTime, endDateTime, model);

    model.addAttribute("message", message);
    return "report/paymentAndIncomeReport";
  }

  @GetMapping("/manager")
  public String getAllInvoiceAndPayment(Model model) {
    LocalDate localDate = LocalDate.now();
    String message = "This report is belongs to " + localDate.toString();
    LocalDateTime startDateTime = dateTimeAgeService.dateTimeToLocalDateStartInDay(localDate);
    LocalDateTime endDateTime = dateTimeAgeService.dateTimeToLocalDateEndInDay(localDate);

    return commonAll(paymentService.findByCreatedAtIsBetween(startDateTime, endDateTime),
        invoiceService.findByCreatedAtIsBetween(startDateTime, endDateTime), model, message,
        startDateTime, endDateTime);

  }

  @PostMapping("/manager/search")
  public String getAllInvoiceAndPaymentBetweenTwoDate(@ModelAttribute("twoDate") TwoDate twoDate, Model model) {
    String message = "This report is between from " + twoDate.getStartDate().toString() + " to "
        + twoDate.getEndDate().toString();
    LocalDateTime startDateTime = dateTimeAgeService.dateTimeToLocalDateStartInDay(twoDate.getStartDate());
    LocalDateTime endDateTime = dateTimeAgeService.dateTimeToLocalDateEndInDay(twoDate.getEndDate());
    return commonAll(paymentService.findByCreatedAtIsBetween(startDateTime, endDateTime),
        invoiceService.findByCreatedAtIsBetween(startDateTime, endDateTime), model, message,
        startDateTime, endDateTime);
  }

  private void commonInvoices(List<Invoice> invoices, Model model) {
    // invoice count
    int invoiceTotalCount = invoices.size();
    model.addAttribute("invoiceTotalCount", invoiceTotalCount);
    // |-> card
    List<Invoice> invoiceCards = invoices.stream().filter(x -> x.getPaymentMethod().equals(PaymentMethod.CREDIT))
        .collect(Collectors.toList());
    int invoiceCardCount = invoiceCards.size();
    AtomicReference<BigDecimal> invoiceCardAmount = new AtomicReference<>(BigDecimal.ZERO);
    invoiceCards.forEach(x -> {
      BigDecimal addAmount = operatorService.addition(invoiceCardAmount.get(), x.getTotalAmount());
      invoiceCardAmount.set(addAmount);
    });
    model.addAttribute("invoiceCardCount", invoiceCardCount);
    model.addAttribute("invoiceCardAmount", invoiceCardAmount.get());
    // |-> cash
    List<Invoice> invoiceCash = invoices.stream().filter(x -> x.getPaymentMethod().equals(PaymentMethod.CASH))
        .collect(Collectors.toList());
    int invoiceCashCount = invoiceCash.size();
    AtomicReference<BigDecimal> invoiceCashAmount = new AtomicReference<>(BigDecimal.ZERO);
    invoiceCash.forEach(x -> {
      BigDecimal addAmount = operatorService.addition(invoiceCashAmount.get(), x.getTotalAmount());
      invoiceCashAmount.set(addAmount);
    });
    model.addAttribute("invoiceCashCount", invoiceCashCount);
    model.addAttribute("invoiceCashAmount", invoiceCashAmount.get());

  }

  @GetMapping("/cashier")
  public String getCashierToday(Model model) {
    LocalDate localDate = LocalDate.now();
    String message = "This report is belongs to " + localDate.toString() + " and \n congratulation all are done by " +
        "you.";
    LocalDateTime startDateTime = dateTimeAgeService.dateTimeToLocalDateStartInDay(localDate);
    LocalDateTime endDateTime = dateTimeAgeService.dateTimeToLocalDateEndInDay(localDate);
    commonInvoices(invoiceService.findByCreatedAtIsBetweenAndCreatedBy(startDateTime, endDateTime,
        SecurityContextHolder.getContext().getAuthentication().getName()), model);
    model.addAttribute("message", message);
    return "report/cashierReport";
  }

  @PostMapping("/cashier/search")
  public String getCashierSearch(@ModelAttribute("twoDate") TwoDate twoDate, Model model) {
    String message = "This report is between from " + twoDate.getStartDate().toString() + " to "
        + twoDate.getEndDate().toString() + " and \n congratulation all are done by you.";
    LocalDateTime startDateTime = dateTimeAgeService.dateTimeToLocalDateStartInDay(twoDate.getStartDate());
    LocalDateTime endDateTime = dateTimeAgeService.dateTimeToLocalDateEndInDay(twoDate.getEndDate());
    commonInvoices(invoiceService.findByCreatedAtIsBetweenAndCreatedBy(startDateTime, endDateTime,
        SecurityContextHolder.getContext().getAuthentication().getName()), model);
    model.addAttribute("message", message);
    return "report/cashierReport";
  }

  private void commonPayment(List<Payment> payments, Model model) {
    // payment count
    int paymentTotalCount = payments.size();
    model.addAttribute("paymentTotalCount", paymentTotalCount);
    // |-> card
    List<Payment> paymentCards = payments.stream().filter(x -> x.getPaymentMethod().equals(PaymentMethod.CREDIT))
        .collect(Collectors.toList());
    int paymentCardCount = paymentCards.size();
    AtomicReference<BigDecimal> paymentCardAmount = new AtomicReference<>(BigDecimal.ZERO);
    paymentCards.forEach(x -> {
      BigDecimal addAmount = operatorService.addition(paymentCardAmount.get(), x.getAmount());
      paymentCardAmount.set(addAmount);
    });
    model.addAttribute("paymentCardCount", paymentCardCount);
    model.addAttribute("paymentCardAmount", paymentCardAmount.get());
    // |-> cash
    List<Payment> paymentCash = payments.stream().filter(x -> x.getPaymentMethod().equals(PaymentMethod.CASH))
        .collect(Collectors.toList());
    int paymentCashCount = paymentCash.size();
    AtomicReference<BigDecimal> paymentCashAmount = new AtomicReference<>(BigDecimal.ZERO);
    paymentCash.forEach(x -> {
      BigDecimal addAmount = operatorService.addition(paymentCashAmount.get(), x.getAmount());
      paymentCashAmount.set(addAmount);
    });
    model.addAttribute("paymentCashCount", paymentCashCount);
    model.addAttribute("paymentCardAmount", paymentCashAmount.get());

  }

  @GetMapping("/payment")
  public String getPaymentToday(Model model) {
    LocalDate localDate = LocalDate.now();
    String message = "This report is belongs to " + localDate.toString() + " and \n congratulation all are done by " +
        "you.";
    LocalDateTime startDateTime = dateTimeAgeService.dateTimeToLocalDateStartInDay(localDate);
    LocalDateTime endDateTime = dateTimeAgeService.dateTimeToLocalDateEndInDay(localDate);
    commonPayment(paymentService.findByCreatedAtIsBetweenAndCreatedBy(startDateTime, endDateTime,
        SecurityContextHolder.getContext().getAuthentication().getName()), model);
    model.addAttribute("message", message);
    return "report/paymentReport";
  }

  @PostMapping("/payment/search")
  public String getPaymentSearch(@ModelAttribute("twoDate") TwoDate twoDate, Model model) {
    String message = "This report is between from " + twoDate.getStartDate().toString() + " to "
        + twoDate.getEndDate().toString() + " and \n congratulation all are done by you.";
    LocalDateTime startDateTime = dateTimeAgeService.dateTimeToLocalDateStartInDay(twoDate.getStartDate());
    LocalDateTime endDateTime = dateTimeAgeService.dateTimeToLocalDateEndInDay(twoDate.getEndDate());
    commonPayment(paymentService.findByCreatedAtIsBetweenAndCreatedBy(startDateTime, endDateTime,
        SecurityContextHolder.getContext().getAuthentication().getName()), model);
    model.addAttribute("message", message);
    return "report/paymentReport";
  }

  private void commonPerCashier(List<Invoice> invoices, Model model) {
    List<NameCount> invoiceByCashierAndTotalAmount = new ArrayList<>();
    // name, count, total
    HashSet<String> createdByAll = new HashSet<>();
    invoices.forEach(x -> createdByAll.add(x.getCreatedBy()));

    createdByAll.forEach(x -> {
      NameCount nameCount = new NameCount();
      Employee employee = userService.findByUserName(x).getEmployee();
      nameCount.setName(employee.getTitle().getTitle() + " " + employee.getName());
      AtomicReference<BigDecimal> cashierTotalCount = new AtomicReference<>(BigDecimal.ZERO);
      List<Invoice> cashierInvoice = invoices.stream().filter(a -> a.getCreatedBy().equals(x))
          .collect(Collectors.toList());
      nameCount.setCount(cashierInvoice.size());
      cashierInvoice.forEach(a -> {
        BigDecimal addAmount = operatorService.addition(cashierTotalCount.get(), a.getTotalAmount());
        cashierTotalCount.set(addAmount);
      });
      nameCount.setTotal(cashierTotalCount.get());
      invoiceByCashierAndTotalAmount.add(nameCount);
    });
    model.addAttribute("invoiceByCashierAndTotalAmount", invoiceByCashierAndTotalAmount);
  }

  @GetMapping("/perCashier")
  public String perCashierToday(Model model) {
    LocalDate localDate = LocalDate.now();
    String message = "This report is belongs to " + localDate.toString();
    LocalDateTime startDateTime = dateTimeAgeService.dateTimeToLocalDateStartInDay(localDate);
    LocalDateTime endDateTime = dateTimeAgeService.dateTimeToLocalDateEndInDay(localDate);
    commonPerCashier(invoiceService.findByCreatedAtIsBetween(startDateTime, endDateTime), model);
    model.addAttribute("message", message);
    return "report/perCashierReport";
  }

  @PostMapping("/perCashier/search")
  public String getPerCashierSearch(@ModelAttribute("twoDate") TwoDate twoDate, Model model) {
    String message = "This report is between from " + twoDate.getStartDate().toString() + " to "
        + twoDate.getEndDate().toString() + " and \n congratulation all are done by you.";
    LocalDateTime startDateTime = dateTimeAgeService.dateTimeToLocalDateStartInDay(twoDate.getStartDate());
    LocalDateTime endDateTime = dateTimeAgeService.dateTimeToLocalDateEndInDay(twoDate.getEndDate());
    commonPerCashier(invoiceService.findByCreatedAtIsBetween(startDateTime, endDateTime), model);
    model.addAttribute("message", message);
    return "report/perCashierReport";
  }

  private void commonPerAccountUser(List<Payment> payments, Model model) {
    List<NameCount> paymentByUserAndTotalAmount = new ArrayList<>();
    // name, count, total
    HashSet<String> createdByAllPayment = new HashSet<>();
    payments.forEach(x -> createdByAllPayment.add(x.getCreatedBy()));

    createdByAllPayment.forEach(x -> {
      NameCount nameCount = new NameCount();
      Employee employee = userService.findByUserName(x).getEmployee();
      nameCount.setName(employee.getTitle().getTitle() + " " + employee.getName());
      List<BigDecimal> userTotalCount = new ArrayList<>();
      List<Payment> paymentUser = payments.stream().filter(a -> a.getCreatedBy().equals(x))
          .collect(Collectors.toList());
      nameCount.setCount(paymentUser.size());
      paymentUser.forEach(a -> {
        userTotalCount.add(a.getAmount());
      });
      nameCount.setTotal(userTotalCount.stream().reduce(BigDecimal.ZERO, BigDecimal::add));
      paymentByUserAndTotalAmount.add(nameCount);
    });

    model.addAttribute("paymentByUserAndTotalAmount", paymentByUserAndTotalAmount);

  }

  @GetMapping("/perAccount")
  public String perAccountToday(Model model) {
    LocalDate localDate = LocalDate.now();
    String message = "This report is belongs to " + localDate.toString();
    LocalDateTime startDateTime = dateTimeAgeService.dateTimeToLocalDateStartInDay(localDate);
    LocalDateTime endDateTime = dateTimeAgeService.dateTimeToLocalDateEndInDay(localDate);
    commonPerAccountUser(paymentService.findByCreatedAtIsBetween(startDateTime, endDateTime), model);
    model.addAttribute("message", message);
    return "report/perAccountReport";
  }

  @PostMapping("/perAccount/search")
  public String getPerAccountSearch(@ModelAttribute("twoDate") TwoDate twoDate, Model model) {
    String message = "This report is between from " + twoDate.getStartDate().toString() + " to "
        + twoDate.getEndDate().toString() + " and \n congratulation all are done by you.";
    LocalDateTime startDateTime = dateTimeAgeService.dateTimeToLocalDateStartInDay(twoDate.getStartDate());
    LocalDateTime endDateTime = dateTimeAgeService.dateTimeToLocalDateEndInDay(twoDate.getEndDate());
    commonPerAccountUser(paymentService.findByCreatedAtIsBetween(startDateTime, endDateTime), model);
    model.addAttribute("message", message);
    return "report/perAccountReport";
  }

  private void commonPerItem(LocalDateTime startDateTime, LocalDateTime endDateTime, Model model) {
    HashSet<Item> invoiceItems = new HashSet<>();

    List<ParameterCount> itemNameAndItemCount = new ArrayList<>();

    List<InvoiceLedger> invoiceLedgers = invoiceLedgerService.findByCreatedAtIsBetween(startDateTime, endDateTime);
    invoiceLedgers.forEach(x -> invoiceItems.add(x.getLedger().getItem()));

    invoiceItems.forEach(x -> {
      ParameterCount parameterCount = new ParameterCount();
      parameterCount.setName(x.getName());
      parameterCount.setSellPrice(x.getSellPrice());
      int count = (int) invoiceLedgers
          .stream()
          .filter(a -> a.getLedger().getItem().equals(x))
          .count();

      parameterCount.setCount(count);
      // parameterCount.setPrice(x.getSellPrice().multiply(BigDecimal.valueOf(count)));
      parameterCount.setPrice(sellPrice(x.getSellPrice(), count));
      itemNameAndItemCount.add(parameterCount);

    });
    model.addAttribute("itemNameAndItemCount", itemNameAndItemCount);

  }

  private BigDecimal sellPrice(BigDecimal sellPrice, int itemCount) {
    System.out.println(sellPrice.multiply(BigDecimal.valueOf(itemCount)));
    return sellPrice.multiply(BigDecimal.valueOf(itemCount));

  }

  @GetMapping("/perItem")
  public String perItemToday(Model model) {
    LocalDate localDate = LocalDate.now();
    String message = "This report is belongs to " + localDate.toString();
    LocalDateTime startDateTime = dateTimeAgeService.dateTimeToLocalDateStartInDay(localDate);
    LocalDateTime endDateTime = dateTimeAgeService.dateTimeToLocalDateEndInDay(localDate);
    commonPerItem(startDateTime, endDateTime, model);
    model.addAttribute("message", message);
    return "report/perItemReport";
  }

  @PostMapping("/perItem/search")
  public String getPerItemSearch(@ModelAttribute("twoDate") TwoDate twoDate, Model model) {
    String message = "This report is between from " + twoDate.getStartDate().toString() + " to "
        + twoDate.getEndDate().toString() + " and \n congratulation all are done by you.";
    LocalDateTime startDateTime = dateTimeAgeService.dateTimeToLocalDateStartInDay(twoDate.getStartDate());
    LocalDateTime endDateTime = dateTimeAgeService.dateTimeToLocalDateEndInDay(twoDate.getEndDate());
    commonPerItem(startDateTime, endDateTime, model);
    model.addAttribute("message", message);
    return "report/perItemReport";
  }

  @GetMapping("/incomeItem")
  public String incomeItemToday(Model model) {
    LocalDate localDate = LocalDate.now();
    String message = "This report is belongs to " + localDate.toString();
    LocalDateTime startDateTime = dateTimeAgeService.dateTimeToLocalDateStartInDay(localDate);
    LocalDateTime endDateTime = dateTimeAgeService.dateTimeToLocalDateEndInDay(localDate);

    // purchase order list
    // List< PurchaseOrder > purchaseOrders =
    // purchaseOrderService.findByUpdatedAtIsBetween(startDateTime, endDateTime)
    // .stream()
    // .filter(x ->
    // !x.getPurchaseOrderStatus().equals(PurchaseOrderStatus.NOT_COMPLETED))
    // .collect(Collectors.toList());

    return commonIncomeItem(startDateTime, endDateTime, model, message);
  }

  @PostMapping("/incomeItem")
  public String incomeItemToday(@ModelAttribute TwoDate twoDate, Model model) {

    String message = "This report is belongs to " + twoDate.getStartDate() + "  to " + twoDate.getEndDate();
    LocalDateTime startDateTime = dateTimeAgeService.dateTimeToLocalDateStartInDay(twoDate.getStartDate());
    LocalDateTime endDateTime = dateTimeAgeService.dateTimeToLocalDateEndInDay(twoDate.getEndDate());

    // purchase order list
    // List< PurchaseOrder > purchaseOrders =
    // purchaseOrderService.findByUpdatedAtIsBetween(startDateTime, endDateTime)
    // .stream()
    // .filter(x ->
    // !x.getPurchaseOrderStatus().equals(PurchaseOrderStatus.NOT_COMPLETED))
    // .collect(Collectors.toList());

    return commonIncomeItem(startDateTime, endDateTime, model, message);
  }

  private String commonIncomeItem(LocalDateTime startDateTime, LocalDateTime endDateTime, Model model, String message) {
    List<ItemSellPriceQuantityBuyingPrice> itemSellPriceQuantityBuyingPrices = new ArrayList<>();
    // given date invoices
    List<LedgerQuantitySellPrice> ledgerQuantitySellPrices = new ArrayList<>();

    List<Invoice> invoices = invoiceService.findByCreatedAtIsBetween(startDateTime, endDateTime).stream()
        .filter(x -> x.getLiveDead().equals(LiveDead.ACTIVE)).collect(Collectors.toList());
    //
    for (Invoice invoice : invoices) {
      for (InvoiceLedger invoiceLedger : invoice.getInvoiceLedgers()) {
        LedgerQuantitySellPrice ledgerQuantitySellPrice = new LedgerQuantitySellPrice();
        ledgerQuantitySellPrice.setLedger(ledgerService.findById(invoiceLedger.getLedger().getId()));
        ledgerQuantitySellPrice.setAmount(invoiceLedger.getSellPrice());
        ledgerQuantitySellPrice.setCounter(Integer.parseInt(invoiceLedger.getQuantity()));
        ledgerQuantitySellPrices.add(ledgerQuantitySellPrice);
      }
    }

    List<Ledger> ledgers = new ArrayList<>();
    for (LedgerQuantitySellPrice ledgerQuantitySellPrice : ledgerQuantitySellPrices) {
      ledgers.add(ledgerQuantitySellPrice.getLedger());
    }
    // duplicate removed
    List<Ledger> duplicateRemovedLedgers = ledgers.stream().distinct().collect(Collectors.toList());
    for (Ledger duplicateRemovedLedger : duplicateRemovedLedgers) {
      ItemSellPriceQuantityBuyingPrice itemSellPriceQuantityBuyingPrice = new ItemSellPriceQuantityBuyingPrice();
      int counter = 0;
      for (LedgerQuantitySellPrice ledgerQuantitySellPrice : ledgerQuantitySellPrices) {
        if (duplicateRemovedLedger.equals(ledgerQuantitySellPrice.getLedger())) {
          counter = counter + ledgerQuantitySellPrice.getCounter();
        }
      }
      itemSellPriceQuantityBuyingPrice.setItem(itemService.findById(duplicateRemovedLedger.getItem().getId()));
      itemSellPriceQuantityBuyingPrice.setSellPrice(duplicateRemovedLedger.getSellPrice());
      itemSellPriceQuantityBuyingPrice.setItemCounter(counter);
      itemSellPriceQuantityBuyingPrice.setSellPrice(duplicateRemovedLedger.getSellPrice());
      itemSellPriceQuantityBuyingPrice
          .setSellTotalPrice(duplicateRemovedLedger.getSellPrice().multiply(new BigDecimal(counter)));
      BigDecimal buyingPrices = purchaseOrderItemService
          .findByPurchaseOrderAndItem(duplicateRemovedLedger.getGoodReceivedNote().getPurchaseOrder(),
              duplicateRemovedLedger.getItem())
          .getBuyingPrice();
      itemSellPriceQuantityBuyingPrice.setBuyingPrice(buyingPrices);
      itemSellPriceQuantityBuyingPrice.setBuyingTotalPrice(buyingPrices.multiply(new BigDecimal(counter)));

      itemSellPriceQuantityBuyingPrices.add(itemSellPriceQuantityBuyingPrice);
    }
    model.addAttribute("itemSellPriceQuantityBuyingPrices", itemSellPriceQuantityBuyingPrices);

    model.addAttribute("message", message);
    return "report/incomeItem";
  }

}
