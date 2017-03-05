package com.github.yinyee.locator.quickbooks;

import com.google.api.client.util.DateTime;
import com.google.api.client.util.Key;

import java.math.BigDecimal;
import java.util.List;

public class Invoice {
    public static class Wrapper {
        @Key("Invoice") public Invoice invoice;
    }

    public static class QueryResponseWrapper {
        @Key("QueryResponse") public QueryResponse queryResponse;
    }

    public static class QueryResponse {
        @Key("Invoice") public List<Invoice> invoices;
    }

    public static class CustomField {
        @Key("DefinitionId") public String definitionId;
        @Key("Name") public String name;
        // TODO @Key("Type") public CustomFieldTypeEnum type;
        @Key("StringValue") public String stringValue;
    }

    public static class LinkedTxn {
        @Key("TxnId") public String txnId;
        @Key("TxnType") public String txnType;
        @Key("TxnLineId") public String txnLineId;
    }

    public static class Line {
        public static class SalesItemLineDetail {
            public static class MarkupInfo {
                @Key("PercentBased") public Boolean percentBased;
                @Key("Value") public BigDecimal value;
                @Key("Percent") public BigDecimal percent;
                @Key("PriceLevelRef") public Common.ReferenceType priceLevelRef;
            }

            @Key("ItemRef") public Common.ReferenceType itemRef;
            @Key("ClassRef") public Common.ReferenceType classRef;
            @Key("UnitPrice") public BigDecimal unitPrice;
            @Key("MarkupInfo") public MarkupInfo markupInfo;
            @Key("Qty") public BigDecimal quantity;
            @Key("TaxCodeRef") public Common.ReferenceType taxCodeRef;
            @Key("ServiceDate") public DateTime serviceDate;
            @Key("TaxInclusiveAmt") public BigDecimal taxInclusiveAmt;
        }

        public static class GroupLineDetail {
            @Key("GroupItemRef") public Common.ReferenceType groupItemRef;
            @Key("Quantity") public BigDecimal quantity;
            @Key("Line") public List<Line> lines;
        }

        public static class DescriptionLineDetail {
            @Key("ServiceDate") public DateTime serviceDate;
            @Key("TaxCodeRef") public Common.ReferenceType taxCodeRef;
        }

        public static class DiscountLineDetail {
            @Key("PercentBased") public Boolean percentBased;
            @Key("DiscountPercent") public BigDecimal discountPercent;
            @Key("DiscountAccountRef") public Common.ReferenceType discountAccountRef;
            @Key("ClassRef") public Common.ReferenceType classRef;
            @Key("TaxCodeRef") public Common.ReferenceType taxCodeRef;
        }

        public static class SubtotalLineDetail {
            @Key("ItemRef") public Common.ReferenceType itemRef;
        }

        @Key("Id") public String id;
        @Key("LineNum") public Integer lineNum;
        @Key("Description") public String description;
        @Key("Amount") public BigDecimal amount;
        @Key("DetailType") public String detailType; // TODO handle this through subclasses
        @Key("SalesItemLineDetail") public SalesItemLineDetail salesItemLineDetail;
        @Key("GroupLineDetail") public GroupLineDetail groupLineDetail;
        @Key("DescriptionLineDetail") public DescriptionLineDetail descriptionLineDetail;
        @Key("DiscountLineDetail") public DiscountLineDetail discountLineDetail;
        @Key("SubtotalLineDetail") public SubtotalLineDetail subtotalLineDetail;
    }

    public static class TxnTaxDetail {
        public static class Line {
            public static class TaxLineDetail {
                @Key("PercentBased") public Boolean percentBased;
                @Key("NetAmountTaxable") public BigDecimal netAmountTaxable;
                @Key("TaxInclusiveAmount") public BigDecimal taxInclusiveAmount;
                @Key("OverrideDeltaAmount") public BigDecimal overrideDeltaAmount;
                @Key("TaxPercent") public BigDecimal taxPercent;
                @Key("TaxRateRef") public Common.ReferenceType taxRateRef;
            }

            @Key("Amount") public BigDecimal amount;
            @Key("DetailType") public String detailType; // TODO handle this through subclasses
            @Key("TaxLineDetail") public TaxLineDetail taxLineDetail;
        }

        @Key("TxnTaxCodeRef") public Common.ReferenceType txnTaxCodeRef;
        @Key("TotalTax") public BigDecimal totalTax;
        @Key("TaxLine") public List<Line> taxLines;
    }

    public static class MemoRef {
        @Key public String value;
    }

    public static class PhysicalAddress {
        @Key("Id") public String id;
        @Key("Line1") public String line1;
        @Key("Line2") public String line2;
        @Key("Line3") public String line3;
        @Key("Line4") public String line4;
        @Key("Line5") public String line5;
        @Key("City") public String city;
        @Key("Country") public String country;
        @Key("CountrySubDivisionCode") public String countrySubDivisionCode;
        @Key("PostalCode") public String postalCode;
        @Key("Lat") public String lat;
        @Key("Long") public String lon;
    }

    public static class EmailAddress {
        @Key("Address") public String address;
    }

    public static class DeliveryInfo {
        @Key("DeliveryType") public String deliveryType; // TODO handle this through subclasses
        @Key("DeliveryTime") public DateTime deliveryTime;
    }

    @Key("Id") public String id;
    @Key("SyncToken") public String syncToken;
    @Key("MetaData") public Common.ModificationMetaData metaData;
    @Key("CustomField") public List<CustomField> customFields;
    @Key("DocNumber") public String docNumber;
    @Key("TxnDate") public DateTime txnDate;
    @Key("DepartmentRef") public Common.DepartmentRefType departmentRef;
    @Key("CurrencyRef") public Common.CurrencyRefType currencyRef;
    @Key("ExchangeRate") public Integer exchangeRate;
    @Key("PrivateNote") public String privateNote;
    @Key("LinkedTxn") public List<LinkedTxn> linkedTxns;
    @Key("Line") public List<Line> lines;
    @Key("TxnTaxDetail") public TxnTaxDetail txnTaxDetail;
    @Key("CustomerRef") public Common.ReferenceType customerRef;
    @Key("CustomerMemo") public MemoRef customerMemo;
    @Key("BillAddr") public PhysicalAddress billAddr;
    @Key("ShipAddr") public PhysicalAddress shipAddr;
    @Key("ClassRef") public Common.ReferenceType classRef;
    @Key("SalesTermRef") public Common.ReferenceType salesTermRef;
    @Key("DueDate") public DateTime dueDate;
    @Key("GlobalTaxCalculation") public String globalTaxCalculation; // TODO handle as enum
    @Key("ShipMethodRef") public Common.ReferenceType shipMethodRef;
    @Key("ShipDate") public DateTime shipDate;
    @Key("TrackingNum") public String trackingNum;
    @Key("TotalAmt") public BigDecimal totalAmt;
    @Key("HomeTotalAmt") public BigDecimal homeTotalAmt;
    @Key("ApplyTaxAfterDiscount") public Boolean applyTaxAfterDiscount;
    @Key("PrintStatus") public String printStatus;
    @Key("EmailStatus") public String emailStatus;
    @Key("BillEmail") public EmailAddress billEmail;
    @Key("BillEmailCc") public EmailAddress billEmailCc;
    @Key("BillEmailBcc") public EmailAddress billEmailBcc;
    @Key("DeliveryInfo") public DeliveryInfo deliveryInfo;
    @Key("Balance") public BigDecimal balance;
    @Key("HomeBalance") public BigDecimal homeBalance;
    @Key("TxnSource") public String txnSource;
    @Key("Deposit") public BigDecimal deposit;
    @Key("TransactionLocationType") public String transactionLocationType;
    @Key public Boolean sparse;
}
