package com.github.yinyee.locator.quickbooks;

import com.google.api.client.util.DateTime;
import com.google.api.client.util.Key;

import java.math.BigDecimal;
import java.util.List;

public class Item {
    public static class Wrapper {
        @Key("Item") public Item item;
    }

    public static class QueryResponseWrapper {
        @Key("QueryResponse") public QueryResponse queryResponse;
    }

    public static class QueryResponse {
        @Key("Item") public List<Item> items;
    }

    @Key("Id") public String id;
    @Key("SyncToken") public String syncToken;
    @Key("MetaData") public Common.ModificationMetaData metaData;
    @Key("Name") public String name;
    @Key("Sku") public String sku;
    @Key("Description") public String description;
    @Key("Active") public Boolean active;
    @Key("Subitem") public Boolean subitem;
    @Key("ParentRef") public Common.ReferenceType parentRef;
    @Key("Level") public Integer level;
    @Key("FullyQualifiedName") public String fullyQualifiedName;
    @Key("Taxable") public Boolean taxable;
    @Key("SalesTaxIncluded") public Boolean salesTaxIncluded;
    @Key("UnitPrice") public BigDecimal unitPrice;
    @Key("Type") public String type; // TODO handle enum as subclasses
    @Key("ItemCategoryType") public String itemCategoryType;
    @Key("IncomeAccountRef") public Common.ReferenceType incomeAccountRef;
    @Key("ExpenseAccountRef") public Common.ReferenceType expenseAccountRef;
    @Key("PurchaseDesc") public String purchaseDesc;
    @Key("PurchaseTaxIncluded") public Boolean purchaseTaxIncluded;
    @Key("PurchaseCost") public BigDecimal purchaseCost;
    @Key("AssetAccountRef") public Common.ReferenceType assetAccountRef;
    @Key("TrackQtyOnHand") public Boolean trackQtyOnHand;
    @Key("InvStartData") public DateTime invStartDate;
    @Key("QtyOnHand") public BigDecimal qtyOnHand;
    @Key("SalesTaxCodeRef") public Common.ReferenceType salesTaxCodeRef;
    @Key("PurchaseTaxCodeRef") public Common.ReferenceType purchaseTaxCodeRef;
    @Key("AbatementRate") public BigDecimal abatementRate;
    @Key("ReverseChargeRate") public BigDecimal reverseChargeRate;
    @Key("ServiceType") public String serviceType;
    @Key public Boolean sparse;
}
