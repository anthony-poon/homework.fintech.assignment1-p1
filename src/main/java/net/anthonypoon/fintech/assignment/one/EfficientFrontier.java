/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.anthonypoon.fintech.assignment.one;

import Jama.Matrix;
import java.awt.geom.Point2D;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.paukov.combinatorics3.Generator;

/**
 *
 * @author ypoon
 */
public class EfficientFrontier {
    //private Portfolio portfolio;
    private enum Constrain {
        NONE,
        POS,
        MAX
    };
    private Matrix a;
    private Matrix b;
    private List<Stock> stockList = new ArrayList();
    private boolean isPostiveOnly;
    private boolean isMaxBounded;
    private Double maxValue = 0.0;
    private Double lamda1;
    private Double lamda2;
    private List<Integer> zeroLamda = new ArrayList();
    private Map<Integer, Constrain> constrainMap = new TreeMap();
    //private List<Integer> zeroWeight = new ArrayList();
    private boolean isDebug = false;
    

    public EfficientFrontier(Collection<Stock> stockList) {
        this.stockList = new ArrayList(stockList);
        for (Stock stock : stockList) {
            constrainMap.put(stock.getCode(), Constrain.NONE);
        }
    }
    private Map<Integer, Double> solveWeight(Double expReturn) throws Exception {
        // Matrix n+2 x n+2; n = number of stock;
        // w1, w2, w3 .... wn, lamda1, lamda2
        // Create a matrix size num of stock + 2
        List<Stock> unconstStocks = new ArrayList();
        List<Stock> zeroWStocks = new ArrayList(); 
        List<Stock> maxWStocks = new ArrayList(); 
        for (Stock stock : stockList) {
            switch (constrainMap.get(stock.getCode())) {
                case NONE:
                    unconstStocks.add(stock);
                    break;
                case MAX:
                    maxWStocks.add(stock);
                    break;
                case POS:
                    zeroWStocks.add(stock);
                    break;
            }
        }
        double[][] array = new double[unconstStocks.size() + 2][unconstStocks.size() + 2];
        for (int row = 0; row < array.length; row ++) {
            for (int col = 0; col < array[row].length; col ++) {
                Double coefficient = 0.0;
                // w.row * stockStd^2 - lamda1 * r1 - lamda2 = ...
                // if < n && row !== col, coeff = covar(row,col) * w.col
                // if < n && row == col, coeff = std^2 
                // else if = n, coeff = - ri
                // else if = n + 1, coeff = - 1
                // else, coeff = 0
                if (row < unconstStocks.size()) {                  
                    if (col < unconstStocks.size() && row == col) {
                        coefficient = Math.pow(unconstStocks.get(col).getStd(), 2);
                    }
                    if (col < unconstStocks.size() && row != col) {
                        coefficient = Stock.getCovariance(unconstStocks.get(row), unconstStocks.get(col));
                    }
                    if (col == unconstStocks.size()) {
                        // Lamda 1
                        coefficient = - unconstStocks.get(row).getExpectedReturn();
                    }
                    if (col == unconstStocks.size() + 1) {
                        // Lamda 2
                        coefficient = - 1.0;
                    }
                }
                // sigma[i:1..n](wi * ri) = expectedReturn;
                // if < n, coeff = ri 
                // else, coeff = 0
                if (row == unconstStocks.size()) {                    
                    if (col < unconstStocks.size()) {
                        coefficient = unconstStocks.get(col).getExpectedReturn();
                    }
                }
                
                // sum[i:1..n](wi) = 1;
                // if < n, coeff = 1 
                // else, coeff = 0
                if (row == unconstStocks.size() + 1) {                    
                    if (col < unconstStocks.size()) {
                        coefficient = 1.0;
                    }
                }
                array[row][col] = coefficient;
            }
        }
        
        a = new Matrix(array);
        //a.print(new DecimalFormat("0.000"), 5);
        array = new double[unconstStocks.size() + 2][1];
        for (int row = 0; row < array.length; row ++) {
            Double coefficient = 0.0;
            // i = row
            // if < n, coeff = 0.0
            // else if = n, coeff = expPReturn;
            // else if = n + 1, coeff = expPReturn;
            if (row >= unconstStocks.size() && row <= unconstStocks.size() + 1) {
                if (row < unconstStocks.size()) {
                    for (Stock stock : maxWStocks) {
                        if (unconstStocks.get(row).getCode() == stock.getCode()) {
                            throw new Exception("THIS SHOULD NOT HAPPEN");
                        }
                        coefficient = coefficient - Stock.getCovariance(unconstStocks.get(row), stock) * maxValue;
                    }
                    
                } 
                if (row == unconstStocks.size()) {
                    Double sum = 0.0;
                    for (Stock stock : maxWStocks) {
                        sum += maxValue * stock.getExpectedReturn();
                    }
                    coefficient = expReturn - sum;
                }
                if (row == unconstStocks.size() + 1) {
                    Double sum = 0.0;
                    for (Stock stock : maxWStocks) {
                        sum += maxValue;
                    }
                    coefficient = 1.0 - sum;
                }
            }
            array[row][0] = coefficient;
        }
        
        b = new Matrix(array);
        //b.print(new DecimalFormat("0.000"), 5);
        Matrix x = a.solve(b);
        //x.print(new DecimalFormat("#.000"), 5);
        //System.out.println();
        double[] weightWithLamda = x.getColumnPackedCopy();
        lamda1 = weightWithLamda[weightWithLamda.length - 2];
        lamda2 = weightWithLamda[weightWithLamda.length - 1];
        Map<Integer, Double> returnMap = new TreeMap();
        for (Stock stock : stockList) {
            switch (constrainMap.get(stock.getCode())) {
                case NONE:
                    returnMap.put(stock.getCode(), weightWithLamda[unconstStocks.indexOf(stock)]);
                    break;
                case POS:
                    returnMap.put(stock.getCode(), 0.0);
                    break;
                case MAX:
                    returnMap.put(stock.getCode(), maxValue);
                    break;
            }
        }
        return returnMap;
    }
    
    private Map<Integer, Double> solveWeight(List<Stock> subList, Double expReturn) throws Exception {
        // Matrix n+2 x n+2; n = number of stock;
        // w1, w2, w3 .... wn, lamda1, lamda2
        // Create a matrix size num of stock + 2
        
        double[][] array = new double[subList.size() + 2][subList.size() + 2];
        for (int row = 0; row < array.length; row ++) {
            
            for (int col = 0; col < array[row].length; col ++) {
                Double coefficient = 0.0;
                // w.row * stockStd^2 - lamda1 * r1 - lamda2 = ...
                // if < n && row !== col, coeff = covar(row,col) * w.col
                // if < n && row == col, coeff = std^2 
                // else if = n, coeff = - ri
                // else if = n + 1, coeff = - 1
                // else, coeff = 0
                if (row < subList.size()) {                  
                    if (col < subList.size() && row == col) {
                        coefficient = Math.pow(subList.get(col).getStd(), 2);
                    }
                    if (col < subList.size() && row != col) {
                        coefficient = Stock.getCovariance(subList.get(row), subList.get(col));
                    }
                    if (col == subList.size()) {
                        // Lamda 1
                        coefficient = - subList.get(row).getExpectedReturn();
                    }
                    if (col == subList.size() + 1) {
                        // Lamda 2
                        coefficient = - 1.0;
                    }
                }
                // sigma[i:1..n](wi * ri) = expectedReturn;
                // if < n, coeff = ri 
                // else, coeff = 0
                if (row == subList.size()) {                    
                    if (col < subList.size()) {
                        coefficient = subList.get(col).getExpectedReturn();
                    }
                }
                
                // sum[i:1..n](wi) = 1;
                // if < n, coeff = 1 
                // else, coeff = 0
                if (row == subList.size() + 1) {                    
                    if (col < subList.size()) {
                        coefficient = 1.0;
                    }
                }
                array[row][col] = coefficient;
            }
        }
        
        a = new Matrix(array);
        // a.print(new DecimalFormat("0.000"), 5);
        array = new double[subList.size() + 2][1];
        for (int row = 0; row < array.length; row ++) {
            Double coefficient = 0.0;
            // i = row
            // if < n, coeff = 0.0
            // else if = n, coeff = expPReturn;
            // else if = n + 1, coeff = expPReturn;
            if (row >= subList.size() && row <= subList.size() + 1) {
                if (row == subList.size()) {
                    coefficient = expReturn;
                }
                if (row == subList.size() + 1) {
                    coefficient = 1.0;
                }
            }
            if (row >= subList.size() + 2) {
                coefficient = maxValue;
            }
            array[row][0] = coefficient;
        }
        
        b = new Matrix(array);
        //b.print(new DecimalFormat("0.000"), 5);
        Matrix x = a.solve(b);
        //x.print(new DecimalFormat("#.000"), 5);
        //System.out.println();
        double[] weightWithLamda = x.getColumnPackedCopy();
        lamda1 = weightWithLamda[weightWithLamda.length - 2];
        lamda2 = weightWithLamda[weightWithLamda.length - 1];
        Map<Integer, Double> returnMap = new TreeMap();
        for (Stock stock : subList) {
            returnMap.put(stock.getCode(), weightWithLamda[subList.indexOf(stock)]);
        }
        return returnMap;
    }
    
    public Portfolio getOptimalPortfolio(Double expReturn) throws Exception {
        Portfolio returnP = new Portfolio(stockList);
        // Need to fix the condition later
        if (!isPostiveOnly) {
            returnP.setWeight(solveWeight(expReturn).values());
        } else if (isMaxBounded && isPostiveOnly) {
            returnP.setWeight(solveWeightWithPOSConstrain(expReturn).values());
        } else {
            
            returnP.setWeight(solveWeightWithPOSConstrain(expReturn).values());
        }        
        return returnP;
    }
    
    public void debugMode(boolean isDebug) {
        this.isDebug = isDebug;
    }
    
    public void setPostiveOnly(boolean mode) {
        isPostiveOnly = mode;
    }
    
    public void setMaxBound(Double val) {
        isMaxBounded = true;
        maxValue = val;
    }
    
    public Map<Point2D.Double, Map<Integer, Double>> getETFByIncrement(double startExpReturn, double endExpReturn, double increment) throws Exception {
        if (startExpReturn + increment > endExpReturn) {
            throw new IllegalArgumentException("Invalid range and increment");
        }
        Map<Point2D.Double, Map<Integer, Double>> returnMap = new LinkedHashMap();
        for (double i = startExpReturn; i < endExpReturn; i = i + increment) {
            try {
                Portfolio p = getOptimalPortfolio(i);
                Point2D.Double xyPt = new Point2D.Double(p.getStd(), p.getWeightedReturn());
                Map<Integer, Double> weight = p.getWeight();
                returnMap.put(xyPt, weight);
            } catch (NullPointerException ex) {
                if (isDebug) {
                    System.out.println("No solve at E(r) = " + i);
                }
            }
        }
        return returnMap;
    }
    
    public Map<Integer, Double> solveWeightWithPOSConstrain(Double expReturn) throws Exception {
        List<Constrain> activeConstrain = new ArrayList();
        activeConstrain.add(Constrain.NONE);
        if (isPostiveOnly) {
            activeConstrain.add(Constrain.POS);
        }
        if (isMaxBounded) {
            activeConstrain.add(Constrain.MAX);
        }        
        List<List<Constrain>> allSubset = Generator
                .permutation(activeConstrain)
                .withRepetitions(stockList.size())
                .stream()
                .collect(Collectors.<List<Constrain>>toList());
        Portfolio bestP = null;
        Double bestVar = Double.MAX_VALUE;
        // Permuate different weight set to Wi = 0
        for (List<Constrain> subset : allSubset) {
            try {
                Portfolio p = new Portfolio(stockList);
                for (int i = 0; i < stockList.size(); i++) {
                    constrainMap.put(stockList.get(i).getCode(), subset.get(i));
                }
                Map<Integer, Double> weightMap = solveWeight(expReturn);
                // becuse solution do not contain zero value Stock
                if (verifyNecessaryCondition(expReturn, new ArrayList(weightMap.values()))) {
                    if (isDebug) {
                        System.out.println("Probable Solve:");
                        for (Map.Entry<Integer, Double> pair : weightMap.entrySet()) {
                            System.out.println(pair.getKey() + "\t" + pair.getValue());
                        }
                        System.out.println();
                    }
                    p.setWeight(new ArrayList(weightMap.values()));
                    if (p.getVariance() < bestVar) {
                        bestVar = p.getVariance();
                        bestP = p;
                    }
                }
            } catch (RuntimeException ex) {
                if (isDebug) {
                    //System.out.println("Permuation " + subset.toString() + " is Singular");
                }
            }            
        }
        if (bestP != null) {
            return bestP.getWeight();
        } else {
            return null;
        }
    }
    
    public Map<Integer, Double> solveWithBothConstrain(Double expReturn) throws Exception {
        // Given both inequality must match, Wi (The weight of stock) must be in one of thise state:
        // 1. W = 0
        // 2. 0 < W < maxValue
        // 3. W = maxValue
        // When 1 or 3 is met, value of W is known and can be removed from the equaltion after the following
        //      Wx = weight of the constrained value (Wx can only be 0 or maxValue)
        //      Wi = weight of the unconstrained value (Wi can only be 0 < W < maxValue)
        //      n = number of unconstrained stock, also n + 2 = number of condition
        //      m = number of constrained stock
        //      min(Wi * sigma + sum[j:1..n](covar(i, j) * Wj) - lamda1 * Ri - lamda2) + sum[k:1..m](covar(i,k) * Wk) = 0
        //      Wi * Ri - pR + sum(Wx * R) = 0
        //      sum[i:1..n][x:1..m](Wi + Wx - 1) = 0
        List<Integer> unbindingIndex = new ArrayList();
        // Omitt lamda 1 and 2 because they have to bind.
        Integer numberOfMultiplier = stockList.size() * 2;
        for (int i = 0; i < numberOfMultiplier; i ++) {
            unbindingIndex.add(i);
        }
        List<List<Integer>> allSubset = Generator
                .subset(unbindingIndex)
                .simple()
                .stream()
                .collect(Collectors.<List<Integer>>toList());
        Portfolio bestP = null;
        Double bestVar = Double.MAX_VALUE;
        for (List<Integer> subset : allSubset) {
            //zeroWeight = new ArrayList();
            zeroLamda = new ArrayList();
            for (Integer index : subset) {
                if (index < stockList.size()) {
                    //zeroWeight.add(index);
                    // If weight is zero, lamda must be zero
                    zeroLamda.add(index);
                } else {
                    zeroLamda.add(index - stockList.size());
                }
            }
            Portfolio p = new Portfolio(stockList);
            try {
                Map<Integer, Double> weightMap = solveWeight(expReturn);
                if (verifyNecessaryCondition(expReturn, new ArrayList(weightMap.values()))) {
                    p.setWeight(solveWeight(expReturn).values());
                    if (p.getVariance() < bestVar) {
                        bestVar = p.getVariance();
                        bestP = p;
                    }
                }
            } catch (RuntimeException ex) {
                if (isDebug) {
                    System.out.println("Permuation " + subset.toString() + " is Singular");
                }
            } 
        }
        //zeroWeight = new ArrayList();
        zeroLamda = new ArrayList();
        if (bestP != null) {
            return bestP.getWeight();
        } else {
            return null;
        }
    }
    
    private boolean verifyNecessaryCondition(Double expReturn, List<Double> weightList) {
        if (isPostiveOnly) {
            for (Double weight : weightList) {
                if (weight < 0) {
                    return false;
                }
            }
        }
        if (isMaxBounded) {
            for (Double weight : weightList) {
                if (weight > maxValue) {
                    return false;
                }
            }
        }
        Double sumR = 0.0;
        Double sumW = 0.0;
        for (int i = 0; i < weightList.size(); i ++) {
            sumW += weightList.get(i);
            sumR += stockList.get(i).getExpectedReturn() * weightList.get(i);
        }
        if ((Math.abs(sumR - expReturn) > 0.01) || (Math.abs(sumW - 1) > 0.01)) {
            return false;
        }
        
        return true;
    }
}
