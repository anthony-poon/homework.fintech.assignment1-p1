/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.anthonypoon.fintech.assignment.one;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author ypoon
 */
public class Stock {
    private int stockCode;
    private List<StockPriceEntry> entryArray = new ArrayList();
    private String name;
    private Double expectedReturn;
    public Stock(int stockCode, String name, Double expectedReturn) {
        this.stockCode = stockCode;
        this.name = name;
        this.expectedReturn = expectedReturn;
    }
    
    public void addEntry(Date date, Double price) {
        if (!entryArray.isEmpty()) {
            // Get last entry
            StockPriceEntry lastEntry = entryArray.get(entryArray.size() - 1);
            Double uValue = Math.log(price / lastEntry.getPrice());
            entryArray.add(new StockPriceEntry(date, price, uValue));
        } else {
            entryArray.add(new StockPriceEntry(date, price));
        }
        
    }
    
    public Integer getCode () {
        return stockCode;
    }
    public Double getUAverage() {
        int count = 0;
        double sum = 0.0;
        for (StockPriceEntry entry : entryArray) {
            if (entry.getU() != null) {
                sum = sum + entry.getU();
                count = count + 1;
            }
        }
        return sum / count;
    }
    
    public Double getStd() {
        Double uAvg = this.getUAverage();
        int count = 0;
        double sum = 0;
        for (StockPriceEntry entry : entryArray) {
            if (entry.getU() != null) {
                sum = sum + Math.pow((entry.getU() - uAvg), 2);
                count ++;
            }
        }
        return Math.sqrt(sum / (count - 1)) * Math.sqrt(252);
    }
    
    public Double getExpectedReturn() {
        return expectedReturn;
    }
    
    public List<StockPriceEntry> getAllEntry() {
        return entryArray;
    }
    
    public String toString() {
        return "Code = " + String.valueOf(stockCode) + ";\t Name = " + name;
    }
    
    public static Double getCovariance(Stock stock1, Stock stock2) {
        Double stock1Avg = stock1.getUAverage();
        Double stock2Avg = stock2.getUAverage();
        List<Double> stock1UValueArray = new ArrayList<>();
        List<Double> stock2UValueArray = new ArrayList<>();
        for (StockPriceEntry entry : stock1.getAllEntry()) {
            if (entry.getU() != null) {
                stock1UValueArray.add(entry.getU());
            }
        }
        for (StockPriceEntry entry : stock2.getAllEntry()) {
            if (entry.getU() != null) {
                stock2UValueArray.add(entry.getU());
            }
        }
        Double sum = 0.0;
        int count = 0;
        for (int i = 0; i < stock1UValueArray.size(); i ++) {
            count ++;
            sum = sum + (stock1UValueArray.get(i) - stock1Avg) * (stock2UValueArray.get(i) - stock2Avg);
        }
        return sum * 252 / (count - 1);
    }
    
    public static Double getCoefficient(Stock stock1, Stock stock2) {
        Double covar = getCovariance(stock1, stock2);
        return covar / (stock1.getStd() * stock2.getStd());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Stock) {
            Stock stock = (Stock) obj;
            return this.getCode().equals(stock.getCode());
        } else {
            return false;
        }
        
    }
    
    
}
