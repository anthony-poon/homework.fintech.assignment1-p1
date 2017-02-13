/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.anthonypoon.fintech.assignment.one;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

/**
 *
 * @author ypoon
 */
public class EfficientFrontier {
    //private Portfolio portfolio;
    private Double porfolioReturn;
    private List<Stock> stockList = new ArrayList();
    private List<Double> bestFitWeight = new ArrayList();
    private Double lamda1;
    private Double lamda2;
    private final Double stepSize = 0.02;
    public EfficientFrontier(List<Stock> stockList, Double porfolioReturn) {
        for (Stock stock : stockList) {
            bestFitWeight.add(1.0 / stockList.size());
            //Random r = new Random();
            //bestFitWeight.add(r.nextDouble());
        }
        this.porfolioReturn = porfolioReturn;
        this.stockList = stockList;
        lamda1 = 1.0;
        lamda2 = 1.0;
    }
    
    public List<Double> gradientDescentOptimiztion() {
        int iterCount = 0;
        Portfolio portfolio = new Portfolio(stockList, bestFitWeight);
        System.out.println("Starting return = " + portfolio.getWeightedReturn());
        while (iterCount < 100) {
            for (int i = 0; i < stockList.size(); i ++) {
                Double newWeight = bestFitWeight.get(i) - getGradient(i);
                bestFitWeight.set(i, newWeight);
            }
            portfolio.setWeight(bestFitWeight);
            lamda1 = lamda1 - getLamda1Gradient();
            lamda2 = lamda2 - getLamda2Gradient();
            DecimalFormat df = new DecimalFormat("#.0000");
            System.out.println(iterCount + ":\tR = " + df.format(portfolio.getWeightedReturn()) + "\tstd = " + df.format(portfolio.getVariance()) + "\tlam1 = " + df.format(lamda1) + "\tlam2 = " + df.format(lamda2));
            
            //System.out.println();
            iterCount++;
        }
        return bestFitWeight;
    }
    
    private Double getGradient(Integer index) {
        Double weight = bestFitWeight.get(index);
        Double expReturn = stockList.get(index).getExpectedReturn();
        Double gradient = weight * Math.pow(stockList.get(index).getStd(), 2);
        gradient = gradient - 2 * lamda1 * (weight * expReturn - porfolioReturn) - 2 * lamda2 * (weight - 1);
        return gradient * stepSize;
    }
    
    private Double getLamda1Gradient() {
        Double sum = 0.0;
        Double weightTotal = 0.0;
        for (int i = 0; i < stockList.size(); i ++) {
            weightTotal = weightTotal + bestFitWeight.get(i);
            sum = sum + bestFitWeight.get(i) * stockList.get(i).getExpectedReturn();
        }
        return Math.pow((sum / weightTotal  - porfolioReturn), 2) * stepSize;
    }
    
    private Double getLamda2Gradient() {
        Double sum = 0.0;
        for (int i = 0; i < bestFitWeight.size(); i ++) {
            sum = sum + bestFitWeight.get(i);
        }
        return Math.pow((sum - 1), 2) * stepSize;
    }
}
