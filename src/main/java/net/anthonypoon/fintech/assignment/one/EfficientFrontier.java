/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.anthonypoon.fintech.assignment.one;

import Jama.Matrix;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author ypoon
 */
public class EfficientFrontier {
    //private Portfolio portfolio;
    private Matrix a;
    private Matrix b;
    private List<Stock> stockList = new ArrayList();
    private boolean[] zeroWeight;
    private boolean isDebug = false;
    public EfficientFrontier(List<Stock> stockList) {
        this.stockList = stockList;
        zeroWeight = new boolean[stockList.size()];
        for (int i = 0; i < stockList.size(); i ++) {
            zeroWeight[i] = false;
        }
    }
    
    public Map<Integer, Double> solveWeight(Double expReturn) {
        // Matrix n+2 x n+2; n = number of stock;
        // w1, w2, w3 .... wn, lamda1, lamda2
        // Create a matrix size num of stock + 2
        List<Stock> stockList = new ArrayList();
        for (int i = 0; i < zeroWeight.length; i++) {
            if (!zeroWeight[i]) {
                stockList.add(this.stockList.get(i));
            }
        }
        double[][] array = new double[stockList.size() + 2][stockList.size() + 2];
        for (int row = 0; row < array.length; row ++) {
            
            for (int col = 0; col < array[row].length; col ++) {
                Double coefficient = 0.0;
                // w.row * stockStd^2 - lamda1 * r1 - lamda2 = ...
                // if < n && row !== col, coeff = covar(row,col) * w.col
                // if < n && row == col, coeff = std^2 
                // else if = n, coeff = - ri
                // else if = n + 1, coeff = - 1
                // else, coeff = 0
                if (row < stockList.size()) {                  
                    if (col < stockList.size() && row == col) {
                        coefficient = Math.pow(stockList.get(col).getStd(), 2);
                    }
                    if (col < stockList.size() && row != col) {
                        coefficient = Stock.getCovariance(stockList.get(row), stockList.get(col));
                    }
                    if (col == stockList.size()) {
                        // Lamda 1
                        coefficient = - stockList.get(row).getExpectedReturn();
                    }
                    if (col == stockList.size() + 1) {
                        // Lamda 2
                        coefficient = - 1.0;
                    }
                }
                // sigma[i:1..n](wi * ri) = expectedReturn;
                // if < n, coeff = ri 
                // else, coeff = 0
                if (row == stockList.size()) {                    
                    if (col < stockList.size()) {
                        coefficient = stockList.get(col).getExpectedReturn();
                    }
                }
                
                // sigma[i:1..n](wi) = 1;
                // if < n, coeff = 1 
                // else, coeff = 0
                if (row == stockList.size() + 1) {                    
                    if (col < stockList.size()) {
                        coefficient = 1.0;
                    }
                }
                array[row][col] = coefficient;
            }
        }
        
        a = new Matrix(array);
        
        array = new double[stockList.size() + 2][1];
        for (int row = 0; row < array.length; row ++) {
            Double coefficient = 0.0;
            // i = row
            // if < n, coeff = 0.0
            // else if = n, coeff = expPReturn;
            // else if = n + 1, coeff = expPReturn;
            if (row >= stockList.size()) {
                if (row == stockList.size()) {
                    coefficient = expReturn;
                }
                if (row == stockList.size() + 1) {
                    coefficient = 1.0;
                }
            }
            array[row][0] = coefficient;
        }
        
        b = new Matrix(array);
        if (isDebug) {
            a.print(new DecimalFormat("0.00"), 5);
            b.print(new DecimalFormat("0.00"), 5);
        }
        Matrix x = a.solve(b);
        //x.print(new DecimalFormat("#.000"), 5);
        //System.out.println();
        double[] weightWithLamda = x.getColumnPackedCopy();
        Map<Integer, Double> returnMap = new TreeMap();
        for (Stock stock : this.stockList) {
            if (stockList.contains(stock)) {
                returnMap.put(stock.getCode(), weightWithLamda[stockList.indexOf(stock)]);
            } else {
                returnMap.put(stock.getCode(), 0.0);
            }
        }
        return returnMap;
    }
    
    public void getKuhnTuckerWithPostiveOnlyConstrain() {
        DecimalFormat df = new DecimalFormat("0.000");
        for (int row = 0; row < stockList.size(); row ++) {
            String outputStr = "";
            for (int col = 0; col < stockList.size(); col ++) {
                if (col != 0) {
                    outputStr += " + ";
                }
                if (row == col) {
                    outputStr += df.format(Math.pow(stockList.get(col).getStd(), 2)) + "W" + (col + 1);
                } else {
                    outputStr += df.format(Stock.getCovariance(stockList.get(row), stockList.get(col))) + "W" + (col + 1);
                }
            }
            outputStr += "+ " + df.format(stockList.get(row).getExpectedReturn()) + "lamda1 + lamda2 <= 0";
            System.out.println("W" + row + "(" + outputStr + ")");
        }       
    }
    
    public void setZeroWeight(int index) {
        zeroWeight[index] = true;
    }
    
    public void unsetConstrain() {
        for (int i = 0; i < stockList.size(); i ++) {
            zeroWeight[i] = false;
        }
    }
    
    public void debugMode(boolean isDebug) {
        this.isDebug = isDebug;
    }
}
