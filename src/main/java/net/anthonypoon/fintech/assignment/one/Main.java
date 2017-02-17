/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.anthonypoon.fintech.assignment.one;

import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.io.output.TeeOutputStream;
import org.jfree.ui.RefineryUtilities;
import org.paukov.combinatorics3.Generator;
/**
 *
 * @author ypoon
 */
public class Main {
    private static DecimalFormat ddf = new DecimalFormat("0.00000");
    private static double etfLowbound = 0.1;
    private static double etfUpbound = 0.3;
    private static double etfInterval = 0.01;
    private static boolean showGraph = false;
    private static List<String> inputPaths = new ArrayList();
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
    public static void main(String args[]) throws Exception {
        
        parseArgument(args);
        Map<String, List<Point2D.Double>> allSeries= new HashMap();
        for (String path : inputPaths) {
            Map<Integer, Stock> stockMap = processFile(path);
            System.out.println("Output for: " + path);
            printStockMap(stockMap);            
            /**
            EfficientFrontier eftPOSBounded = new EfficientFrontier(stockMap.values());
            eftPOSBounded.setPostiveOnly(true);
            eftPOSBounded.setMaxBound(0.3);
            eftPOSBounded.debugMode(true);
            Map<Point2D.Double, Map<Integer, Double>> testResult = eftPOSBounded.getETFByIncrement(etfLowbound, etfUpbound, etfInterval);
            **/
            EfficientFrontier eft = new EfficientFrontier(stockMap.values());
            Map<Point2D.Double, Map<Integer, Double>> result = eft.getETFByIncrement(etfLowbound, etfUpbound, etfInterval);
            List<Point2D.Double> xyPts = new ArrayList(result.keySet());
            allSeries.put(path + " - Unconstrained", xyPts);
            System.out.println("Unconstrained EFT");
            List<Map<Integer, Double>> weightList = new ArrayList(result.values());
            DecimalFormat df = new DecimalFormat("#.000");
            for (Integer i = 0; i < weightList.size(); i ++) {
                System.out.printf("Pt#%-3s", i.toString());
                for (Double weight : weightList.get(i).values()) {
                    System.out.printf("%-5s ", df.format(weight));
                }
                System.out.println();
            }            
            System.out.println();
            
            
            
            
            EfficientFrontier eftPOS = new EfficientFrontier(stockMap.values());
            eftPOS.setPostiveOnly(true);
            result = eftPOS.getETFByIncrement(etfLowbound, etfUpbound, etfInterval);
            xyPts = new ArrayList(result.keySet());
            allSeries.put(path + " - POS Only", xyPts);
            System.out.println("Postive only EFT");
            weightList = new ArrayList(result.values());
            for (Integer i = 0; i < weightList.size(); i ++) {
                System.out.printf("Pt#%-3s", i.toString());
                for (Double weight : weightList.get(i).values()) {
                    System.out.printf("%-5s ", df.format(weight));
                }
                System.out.println();
            }
            System.out.println();
            
            
        }

        if (showGraph) {
            Plotter plotter = new Plotter("ETF");
            for (Map.Entry<String, List<Point2D.Double>> series : allSeries.entrySet()) {
                plotter.addSeries(series.getKey(), series.getValue());
            }            
            plotter.render();
            plotter.pack();
            RefineryUtilities.centerFrameOnScreen(plotter);
            plotter.setVisible(true);
        }
        
        
    }
    
    private static void parseArgument(String args[]) throws Exception {
        Options options = new Options();
        options.addOption("graph", false, "Display efficient frontier graph");
        CommandLineParser parser = new DefaultParser();
        CommandLine cli = parser.parse(options, args);
        showGraph = cli.hasOption("graph");
        for (String path : cli.getArgList()) {
            inputPaths.add(path);
        }
    }
    
    private static Map<Integer, Stock> processFile(String path) throws Exception {
        Map<Integer, Stock> stockMap = new TreeMap<>();
        FileOutputStream outFile = new FileOutputStream("output.txt");
        TeeOutputStream teeStream = new TeeOutputStream(System.out, outFile);
        PrintStream ps = new PrintStream(teeStream, true, "UTF-8");
        //PrintStream ps = new PrintStream(System.out, true, "UTF-16");
        System.setOut(ps);        
               
        BufferedReader reader = new BufferedReader(new FileReader(path));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] strArray = line.split(",");
            SimpleDateFormat dateFormatter = new SimpleDateFormat("d/M/y");
            Date date = dateFormatter.parse(strArray[0]);
            for (int col = 1; col <= PRESET_STOCK_CODE.length; col++ ) {
                if (!stockMap.containsKey(PRESET_STOCK_CODE[col - 1])) {
                    stockMap.put(PRESET_STOCK_CODE[col - 1], new Stock(PRESET_STOCK_CODE[col - 1], PRESET_STOCK_NAME[col - 1], PRESET_EXPECTED_RETURN[col - 1]));
                }
                Stock currentStock = stockMap.get(PRESET_STOCK_CODE[col - 1]);
                currentStock.addEntry(date, Double.valueOf(strArray[col]));
            }           
        }

        return stockMap; 
    }
    
    private static void printStockMap(Map<Integer, Stock> stockMap) {
        for (Map.Entry<Integer, Stock> pair : stockMap.entrySet()) {
            Stock stock = pair.getValue();
            System.out.println("Stock #" + pair.getKey() + " Std = " + ddf.format(stock.getStd()));
        }
        
        for (Map.Entry<Integer, Stock> pair : stockMap.entrySet()) {
            Stock stock = pair.getValue();
            System.out.println("Stock #" + pair.getKey() + " E(r) = " + ddf.format(stock.getExpectedReturn()));
        }
        for (Map.Entry<Integer, Stock> pair : stockMap.entrySet()) {
            Stock stock = pair.getValue();
            System.out.println("Stock #" + pair.getKey() + " E(r) = " + ddf.format(stock.getExpectedReturn()));
        }
        
        System.out.println();
        System.out.println("Covariance Matrix:");
        for (Stock stock1 : stockMap.values()) {
            for (Stock stock2 : stockMap.values()) {
                System.out.print(ddf.format(Stock.getCovariance(stock1, stock2)) + "\t");
            }
            System.out.println();
        }
        System.out.println();
        System.out.println("Coefficient Matrix:");
        for (Stock stock1 : stockMap.values()) {
            for (Stock stock2 : stockMap.values()) {
                System.out.print(ddf.format(Stock.getCoefficient(stock1, stock2)) + "\t");
            }
            System.out.println();
        }
        System.out.println();
    }
    
}
