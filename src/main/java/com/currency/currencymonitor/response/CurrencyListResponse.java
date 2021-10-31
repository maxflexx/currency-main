package com.currency.currencymonitor.response;

import com.currency.currencymonitor.model.Currency;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;


public class CurrencyListResponse {
    @Getter
    @Setter
    private List<Currency> items;

    @Getter @Setter
    private long total_count;
}