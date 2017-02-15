/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.anthonypoon.fintech.assignment.one;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.collections4.iterators.PermutationIterator;
import org.apache.commons.io.output.TeeOutputStream;
import org.jfree.ui.RefineryUtilities;
import org.paukov.combinatorics3.Generator;
/**
 *
 * @author ypoon
 */
public class Main {
    private static double frontierLowbound = 0.1;
    private static double frontierUpbound = 0.3;
    private static double frontierInterval = 0.01;
    private static boolean showGraph = false;
    private static Date set1MinYear;
    private static Date set1MaxYear;
    private static Date set2MinYear;
    private static Date set2MaxYear;
    private static String inputPath;
    private static final int[] PRESET_STOCK_CODE = {
        6,
        17,
        489,
        669,
        883,
        1211,
        1299,
        2318,
        2388,
        6837
    };
    private static final String[] PRESET_STOCK_NAME = {
        "Power Assets Holdings Ltd",
        "New World Development Co Ltd",
        "Dongfeng Motors",
        "Techtronic Industries",
        "CNOOC Ltd",
        "BYD",
        "AIA",
        "Ping An Insurance",
        "BOC Hong Kong",
        "Haitong Securities"
    };
    private static final Double[] PRESET_EXPECTED_RETURN = {
        0.089,
        0.163,
        0.251,
        0.181,
        0.185,
        0.241,
        0.154,
        0.217,
        0.126,
        0.232,
    };
    private static Map<Integer, Stock> Set1Map = new TreeMap<>();
    private static Map<Integer, Stock> Set2Map = new TreeMap<>();
    public static void main(String args[]) throws Exception {
        
        parseArgument(args);
        FileOutputStream outFile = new FileOutputStream("output.txt");
        TeeOutputStream teeStream = new TeeOutputStream(System.out, outFile);
        PrintStream ps = new PrintStream(teeStream, true, "UTF-8");
        //PrintStream ps = new PrintStream(System.out, true, "UTF-16");
        System.setOut(ps);        
               
        BufferedReader reader = new BufferedReader(new FileReader(inputPath));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] strArray = line.split(",");
            SimpleDateFormat dateFormatter = new SimpleDateFormat("d/M/y");
            Date date = dateFormatter.parse(strArray[0]);
            if (date.after(set1MinYear) && date.before(set1MaxYear)) {
                for (int col = 1; col <= PRESET_STOCK_CODE.length; col++ ) {
                    if (!Set1Map.containsKey(PRESET_STOCK_CODE[col - 1])) {
                        Set1Map.put(PRESET_STOCK_CODE[col - 1], new Stock(PRESET_STOCK_CODE[col - 1], PRESET_STOCK_NAME[col - 1], PRESET_EXPECTED_RETURN[col - 1]));
                    }
                    Stock currentStock = Set1Map.get(PRESET_STOCK_CODE[col - 1]);
                    currentStock.addEntry(date, Double.valueOf(strArray[col]));
                }
            }
            if (date.after(set2MinYear) && date.before(set2MaxYear)) {
                for (int col = 1; col <= PRESET_STOCK_CODE.length; col++ ) {
                    if (!Set2Map.containsKey(PRESET_STOCK_CODE[col - 1])) {
                        Set2Map.put(PRESET_STOCK_CODE[col - 1], new Stock(PRESET_STOCK_CODE[col - 1], PRESET_STOCK_NAME[col - 1], PRESET_EXPECTED_RETURN[col - 1]));
                    }
                    Stock currentStock = Set2Map.get(PRESET_STOCK_CODE[col - 1]);
                    currentStock.addEntry(date, Double.valueOf(strArray[col]));
                }
            }            
        }
        /**
        SimpleDateFormat yearFormat = new SimpleDateFormat("y");
        System.out.println("Output from " + yearFormat.format(set1MinYear) + " to " + yearFormat.format(set1MaxYear));
        processStockMap(Set1Map);
        
        System.out.println("Output from " + yearFormat.format(set2MinYear) + " to " + yearFormat.format(set2MaxYear));
        processStockMap(Set2Map);
        
        Plotter plotter = new Plotter("Plotter");
        Portfolio portf1 = new Portfolio(new ArrayList(Set1Map.values()));
        Portfolio portf2 = new Portfolio(new ArrayList(Set2Map.values()));
        EfficientFrontier frontier1 = new EfficientFrontier(new ArrayList(Set1Map.values()));
        EfficientFrontier frontier2 = new EfficientFrontier(new ArrayList(Set2Map.values()));
        for (double i = frontierLowbound; i < frontierUpbound; i = i + frontierInterval){
            double[] weightArray = frontier1.solveWeight(i);
            portf1.setWeight(weightArray);
            weightArray = frontier2.solveWeight(i);
            portf2.setWeight(weightArray);
            if (showGraph) {
                plotter.addXYPoint("Frontier 1", portf1.getStd(), portf1.getWeightedReturn());
                plotter.addXYPoint("Frontier 2", portf2.getStd(), portf2.getWeightedReturn());
            }
        }
        
        if (showGraph) {
            plotter.render();
            plotter.pack();
            RefineryUtilities.centerFrameOnScreen(plotter);
            plotter.setVisible(true);
        }
        **/
        //frontier1.getKuhnTuckerWithPostiveOnlyConstrain();
        Double expReturn = 0.1;
        EfficientFrontier trialFrontier = new EfficientFrontier(new ArrayList(Set1Map.values()));
        
        List<Double> weightArray = new ArrayList(trialFrontier.solveWeight(expReturn).values());
        System.out.println("Original weight: " + weightArray);
        List<Integer> indexArray = new ArrayList();        
        for (int i = 0; i < weightArray.size(); i ++) {
            //if (weightArray[i] < 0) {
                indexArray.add(i);
            //}
        }
        
        System.out.println("Stock index with negative weight: " + indexArray);
        List<List<Integer>> subset = Generator
                .subset(indexArray)
                .simple()
                .stream()
                .collect(Collectors.<List<Integer>>toList());
        //trialFrontier.debugMode(true);
        List<Map<Integer, Double>> positiveWeight = new ArrayList();
        for (int i = 0; i < subset.size(); i++) {
            System.out.printf("%-50s", "Permutation: " + subset.get(i).toString());
            if (subset.get(i).size() > 0) {
                try {
                    DecimalFormat df = new DecimalFormat("0.00");                    
                    trialFrontier.unsetConstrain();
                    for (Integer index : subset.get(i)) {
                        trialFrontier.setZeroWeight(index);
                    }
                    boolean allPositive = true;
                    Map<Integer, Double>resolvedWeight = trialFrontier.solveWeight(expReturn);
                    for (Double weight : resolvedWeight.values()) {
                        if (weight < 0.0) {
                            allPositive = false;
                        }
                        System.out.print(df.format(weight) + "\t");
                    }
                    if (allPositive) {
                        positiveWeight.add(resolvedWeight);
                    }
                    System.out.println();
                } catch (RuntimeException ex) {
                    System.out.println("Singular Matrix");
                }
            }
        }
        System.out.println();
        System.out.println("Possible weight: ");
        Double bestVar = Double.MAX_VALUE;
        Map<Integer, Double> bestCombination = new TreeMap();
        for (Map<Integer, Double> combination : positiveWeight) {
            DecimalFormat df = new DecimalFormat("0.000000");
            for (Map.Entry<Integer, Double> pair : combination.entrySet()) {                
                System.out.printf("%-7s", pair.getKey().toString());
                System.out.println(df.format(pair.getValue()));                
            }
            Portfolio p = new Portfolio(new ArrayList(Set1Map.values()));
            p.setWeight(new ArrayList(combination.values()));
            System.out.println("R: " + df.format(p.getWeightedReturn()) + " Var: " + df.format(p.getVariance()));
            System.out.println();
            if (p.getVariance() < bestVar) {
                bestVar = p.getVariance();
                bestCombination = combination;
            }
        }
        
        System.out.println("Best combination:");
        for (Map.Entry<Integer, Double> pair : bestCombination.entrySet()) {
            DecimalFormat df = new DecimalFormat("0.000000");
            System.out.printf("%-7s", pair.getKey().toString());
            System.out.println(df.format(pair.getValue()));  
        }
        System.out.println("Var: " + bestVar);
    }
    
    private static void parseArgument(String args[]) throws Exception {
        Options options = new Options();
        options.addOption("graph", false, "Display efficient frontier graph");
        CommandLineParser parser = new DefaultParser();
        CommandLine cli = parser.parse(options, args);
        showGraph = cli.hasOption("graph");
        set1MinYear = new SimpleDateFormat("d/M/y").parse("01/01/2015");  
        set1MaxYear = new SimpleDateFormat("d/M/y").parse("31/12/2017");
        set2MinYear = new SimpleDateFormat("d/M/y").parse("01/01/2016");  
        set2MaxYear = new SimpleDateFormat("d/M/y").parse("31/12/2017");
        inputPath = cli.getArgList().get(0);
    }
    
    private static void processStockMap(Map<Integer, Stock> stockMap) {
        DecimalFormat decimalFormatter = new DecimalFormat("0.00000000"); 
        for (Map.Entry<Integer, Stock> pair : stockMap.entrySet()) {
            Stock stock = pair.getValue();
            System.out.println("Stock #" + pair.getKey() + " Std = " + decimalFormatter.format(stock.getStd()));
        }
        
        for (Map.Entry<Integer, Stock> pair : stockMap.entrySet()) {
            Stock stock = pair.getValue();
            System.out.println("Stock #" + pair.getKey() + " E(r) = " + decimalFormatter.format(stock.getExpectedReturn()));
        }
        for (Map.Entry<Integer, Stock> pair : stockMap.entrySet()) {
            Stock stock = pair.getValue();
            System.out.println("Stock #" + pair.getKey() + " E(r) = " + decimalFormatter.format(stock.getExpectedReturn()));
        }
        
        System.out.println();
        System.out.println("Covariance Matrix:");
        for (Stock stock1 : stockMap.values()) {
            for (Stock stock2 : stockMap.values()) {
                System.out.print(decimalFormatter.format(Stock.getCovariance(stock1, stock2)) + "\t");
            }
            System.out.println();
        }
        System.out.println();
        System.out.println("Coefficient Matrix:");
        for (Stock stock1 : stockMap.values()) {
            for (Stock stock2 : stockMap.values()) {
                System.out.print(decimalFormatter.format(Stock.getCoefficient(stock1, stock2)) + "\t");
            }
            System.out.println();
        }
        System.out.println();
    }
}
