package com.github.yinyee.locator.quickbooks;

import com.google.api.client.util.DateTime;
import com.google.api.client.util.Key;

public class Common {
    public static class ModificationMetaData {
        @Key("CreateTime") public DateTime createTime;
        @Key("LastUpdatedTime") public DateTime lastUpdatedTime;
    }

    public static class ReferenceType {
        @Key public String value;
        @Key public String name;
    }

    public static class DepartmentRefType extends ReferenceType {}

    public static class CurrencyRefType extends ReferenceType {}
}
