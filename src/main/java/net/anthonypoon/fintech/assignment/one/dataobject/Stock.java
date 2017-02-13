/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.anthonypoon.fintech.assignment.one.dataobject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author ypoon
 */
public class Stock {
    private int stockCode;
    private List<StockPriceEntry> entryArray = new ArrayList();
    private String name;
    public Stock(int stockCode, String name) {
        this.stockCode = stockCode;
        this.name = name;
    }
    
    public void addEntry(Date date, Double price) {
        if (entryArray.size() != 0) {
            // Get last entry
            StockPriceEntry lastEntry = entryArray.get(entryArray.size() - 1);
            Double uValue = Math.log(price / lastEntry.getPrice());
            entryArray.add(new StockPriceEntry(date, price, uValue));
        } else {
            entryArray.add(new StockPriceEntry(date, price));
        }
        
    }
    
    public Double getUAverageByYear(Integer minYear, Integer maxYear) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy");
        int count = 0;
        double sum = 0.0;
        for (StockPriceEntry entry : entryArray) {
            Integer entryYearInt = Integer.valueOf(formatter.format(entry.getDate()));
            if (entry.getU() != null && minYear <= entryYearInt && entryYearInt <= maxYear) {
                sum = sum + entry.getU();
                count = count + 1;
            }
        }
        return sum / count;
    }
    
    public Double getStdByYear(Integer minYear, Integer maxYear) {
        Double uAvg = this.getUAverageByYear(minYear, maxYear);
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
    
    public List<StockPriceEntry> getAllEntry() {
        return entryArray;
    }
    
    public String toString() {
        return "Code = " + String.valueOf(stockCode) + ";\t Name = " + name;
    }
}
