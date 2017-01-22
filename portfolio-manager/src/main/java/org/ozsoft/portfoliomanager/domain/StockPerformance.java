// This file is part of the 'portfolio-manager' (Portfolio Manager)
// project, an open source stock portfolio manager application
// written in Java.
//
// Copyright 2015 Oscar Stigter
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.ozsoft.portfoliomanager.domain;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.ozsoft.portfoliomanager.util.MathUtils;

/**
 * Stock performance during a specific time range.
 *
 * @author Oscar Stigter
 */
public class StockPerformance {

    private final List<ClosingPrice> prices;

    private final int duration;

    private BigDecimal startPrice;

    private BigDecimal endPrice;

    private BigDecimal lowPrice;

    private BigDecimal highPrice;

    private BigDecimal change;

    private BigDecimal changePerc;

    private BigDecimal volatility;

    public StockPerformance(List<ClosingPrice> prices, TimeRange dateFilter) {
        this.prices = new ArrayList<ClosingPrice>();
        this.duration = dateFilter.getDuration();

        Date fromDate = dateFilter.getFromDate();
        for (ClosingPrice price : prices) {
            if (price.getDate().after(fromDate)) {
                this.prices.add(price);
            }
        }

        Collections.sort(this.prices);

        int count = this.prices.size();
        startPrice = this.prices.get(0).getValue();
        endPrice = this.prices.get(count - 1).getValue();
        lowPrice = new BigDecimal(99999);
        highPrice = BigDecimal.ZERO;
        change = endPrice.subtract(startPrice);
        changePerc = MathUtils.perc(change, startPrice);
        BigDecimal slope = change.divide(new BigDecimal(count), MathContext.DECIMAL64);

        for (int i = 0; i < count; i++) {
            BigDecimal p = this.prices.get(i).getValue();
            if (p.compareTo(lowPrice) < 0) {
                lowPrice = p;
            }
            if (p.compareTo(highPrice) > 0) {
                highPrice = p;
            }
            BigDecimal avg = startPrice.add(new BigDecimal(i).multiply(slope));

            volatility = volatility.add(MathUtils.abs(p, avg).divide(p, MathContext.DECIMAL64).multiply(MathUtils.HUNDRED));
        }
        volatility = volatility.divide(new BigDecimal(count), MathContext.DECIMAL64);
    }

    public List<ClosingPrice> getPrices() {
        return Collections.unmodifiableList(prices);
    }

    public double getStartPrice() {
        return startPrice.doubleValue();
    }

    public double getEndPrice() {
        return endPrice.doubleValue();
    }

    public double getLowPrice() {
        return lowPrice.doubleValue();
    }

    public double getHighPrice() {
        return highPrice.doubleValue();
    }

    public double getChange() {
        return change.doubleValue();
    }

    public double getChangePerc() {
        return changePerc.doubleValue();
    }

    public double getVolatility() {
        return volatility.doubleValue();
    }

    public double getCagr() {
        if (duration < 1) {
            return changePerc.doubleValue();
        } else {
            return (Math.pow(endPrice.divide(startPrice, MathContext.DECIMAL64).doubleValue(), 1.0 / duration) - 1.0) * 100.0;
        }
    }

    public double getDiscount() {
        BigDecimal discount = MathUtils.perc(highPrice.subtract(endPrice, MathContext.DECIMAL64),
                highPrice.subtract(lowPrice, MathContext.DECIMAL64));
        if (discount.signum() < 0) {
            return 0.0;
        } else {
            return discount.doubleValue();
        }
    }
}
