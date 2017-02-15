package net.anthonypoon.fintech.assignment.one;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class Portfolio {
    private List<Stock> stockList = new ArrayList();
    private List<Double> weightList = new ArrayList();
    public Portfolio(List<Stock> stockList) {
        int i = 0;
        this.stockList = stockList;
    }
    
    public Stock getStock(Integer stockCode) {
        for (Stock stock : stockList) {
            if (stock.getCode() == stockCode) {
                return stock;
            }
        }
        return null;
    }
    
    
    public Double getWeightedReturn() {
        Double sum = 0.0;
        // Weight might not add up to one when not normalized
        Double weightSum = 0.0;
        for (int i = 0; i < stockList.size(); i++) {
            Double weight = weightList.get(i);
            sum = sum + stockList.get(i).getExpectedReturn() * weight;
            weightSum = weightSum + weight; 
        }
        return sum / weightSum;
    }
    
    public Double getVariance() {
        Double stdSum = 0.0;
        Double covarSum = 0.0;
        for (int i = 0; i < stockList.size(); i++) {
            Double weight = weightList.get(i);
            stdSum = stdSum + Math.pow(weight, 2) * Math.pow(stockList.get(i).getStd(), 2);
        }
        
        for (int x = 0; x < stockList.size(); x++) {
            for (int y = 0; y < stockList.size(); y++) {
                Double covar = Stock.getCovariance(stockList.get(x), stockList.get(y));
                covarSum = covarSum + weightList.get(x) * weightList.get(y) * covar;
            }
        }
        return stdSum + covarSum;
    }
    
    public Double getStd() {
        return Math.sqrt(getVariance());
    }
    public Integer size() {
        return stockList.size();
    }
    
    public void setWeight(List<Double> weightList) {
        this.weightList = weightList;
    }
    
    public void setWeight(double[] weightArray) {
        this.weightList = new ArrayList();
        for (int i = 0; i < weightArray.length; i ++) {
            this.weightList.add(weightArray[i]);
        }
    }
}
