package com.github.yuqingliu.economy.api.managers;

public interface ConfigurationManager {
    <T> T setConstant(String key, T value, Class<T> clazz);
    int getDailyVendorBuyLimit();
    int getDailyVendorResetDurationHrs();
}
