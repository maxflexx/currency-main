package com.currency.currencymonitor.response;

import com.currency.currencymonitor.model.Currency;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public class CurrencyPriceResponse {
    @Getter @Setter
    private Double previousPrice;

    @Getter @Setter
    private Double currentPrice;

    @Getter @Setter
    private long currentPriceUpdateTimestamp;
}
