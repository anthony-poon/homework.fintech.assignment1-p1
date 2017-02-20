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
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.io.output.TeeOutputStream;
import org.jfree.ui.RefineryUtilities;
/**
 *
 * @author ypoon
 */
public class Main {
    private static final double riskFreeR = 0.0009;
    private static final double maxBound = 0.25;
    private static DecimalFormat ddf = new DecimalFormat("0.00000");
    private static double etfLowbound = 0.1;
    private static double etfUpbound = 0.3;
    private static double etfInterval = 0.01;
    private static boolean skipBounded = false;
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
    private static final Double[] PRESET_ALT_EXPECTED_RETURN = {
        0.089,
        0.163,
        0.190,
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
        TeeOutputStream teeStream = new TeeOutputStream(System.out, new FileOutputStream("output.txt"));
        PrintStream ps = new PrintStream(teeStream, true);
        System.setOut(ps); 
        for (String path : inputPaths) {
              
            List<FrontierCurve> allSeries = new ArrayList();
            Map<Integer, Stock> stockMap = processFile(path);
            
            System.out.println("Output for: " + path);
            printStockMap(stockMap);

            EfficientFrontier eft = new EfficientFrontier(stockMap.values());
            FrontierCurve eftCurve = eft.getETFByIncrement(etfLowbound, etfUpbound, etfInterval);
            eftCurve.setName("Unconstrained");
            allSeries.add(eftCurve);
            System.out.println("Unconstrained EFT");
            for (int i = 0; i < eftCurve.size(); i++) {
                eftCurve.printWeight(i);
            }
            System.out.println();

            // Positive only frontier
            EfficientFrontier eftPOS = new EfficientFrontier(stockMap.values());
            eftPOS.setPostiveOnly(true);
            FrontierCurve eftPOSCurve =  eftPOS.getETFByIncrement(etfLowbound, etfUpbound, etfInterval);            
            allSeries.add(eftPOSCurve);
            eftPOSCurve.setName("POS Only");
            System.out.println("POS Only EFT");
            for (int i = 0; i < eftPOSCurve.size(); i++) {
                eftPOSCurve.printWeight(i);
            }

            // Positive only + max value bounded frontier
            if (!skipBounded) {
                System.out.println();
                EfficientFrontier eftBounded = new EfficientFrontier(stockMap.values());
                eftBounded.setMaxBound(maxBound);
                eftBounded.setPostiveOnly(true);
                FrontierCurve eftBoundedCurve =  eftBounded.getETFByIncrement(etfLowbound, etfUpbound, etfInterval);            
                eftBoundedCurve.setName("POS + Bounded");
                allSeries.add(eftBoundedCurve);
                System.out.println("POS + Bounded");
                for (int i = 0; i < eftBoundedCurve.size(); i++) {
                    eftBoundedCurve.printWeight(i);
                }
            }

            Plotter plotter = new Plotter(path);
            for (FrontierCurve series : allSeries) {
                plotter.addCurve(series);
            }
            plotter.render();
            plotter.pack();
            RefineryUtilities.centerFrameOnScreen(plotter);
            plotter.setVisible(true);
            
            getCal(stockMap, path + " CAL Graph");
            System.out.println();
        }        
        
    }
    
    private static void getCal(Map<Integer, Stock> stockMap, String graphName) throws Exception {
        Double bestSlope1 = Double.MIN_VALUE;
        int bestIndex1 = 0;
        EfficientFrontier eftPOS1 = new EfficientFrontier(stockMap.values());
        eftPOS1.setPostiveOnly(true);
        FrontierCurve eftPOSCurve1 =  eftPOS1.getETFByIncrement(etfLowbound, etfUpbound, etfInterval);            
        eftPOSCurve1.setName("Rate 1");
        for (int i = 0; i < eftPOSCurve1.size(); i ++) {
            Double slope = (eftPOSCurve1.getPt(i).getY() - riskFreeR) / eftPOSCurve1.getPt(i).getX();
            if (slope > bestSlope1) {
                bestSlope1 = slope;
                bestIndex1 = i;
            }
        }
        System.out.println();
        System.out.println("Best Weight for rate 1");
        eftPOSCurve1.printWeight(bestIndex1);
        
        Double bestSlope2 = Double.MIN_VALUE;
        int bestIndex2 = 0;
        List<Stock> altStock = new ArrayList(stockMap.values());
        for (int i = 0; i < altStock.size(); i ++) {
            altStock.get(i).setRate(PRESET_ALT_EXPECTED_RETURN[i]);
        }
        
        EfficientFrontier eftPOS2 = new EfficientFrontier(altStock);
        eftPOS2.setPostiveOnly(true);
        FrontierCurve eftPOSCurve2 =  eftPOS2.getETFByIncrement(etfLowbound, etfUpbound, etfInterval);            
        eftPOSCurve2.setName("Rate 2");
        for (int i = 0; i < eftPOSCurve2.size(); i ++) {
            Double slope = (eftPOSCurve2.getPt(i).getY() - riskFreeR) / eftPOSCurve2.getPt(i).getX();
            if (slope > bestSlope2) {
                bestSlope2 = slope;
                bestIndex2 = i;
            }
        }
        System.out.println();
        System.out.println("Best Weight for rate 2");
        eftPOSCurve2.printWeight(bestIndex2);
        
        Plotter plotter = new Plotter(graphName);
        plotter.addCurve(eftPOSCurve1);
        plotter.addCurve(eftPOSCurve2);
        plotter.addXYPoint("CAL for rate 1", 0.0, riskFreeR);
        plotter.addXYPoint("CAL for rate 1", eftPOSCurve1.getPt(bestIndex1).getX(), eftPOSCurve1.getPt(bestIndex1).getY());
        plotter.addXYPoint("CAL for rate 2", 0.0, riskFreeR);
        plotter.addXYPoint("CAL for rate 2", eftPOSCurve2.getPt(bestIndex2).getX(), eftPOSCurve2.getPt(bestIndex2).getY());        
        plotter.render();
        plotter.pack();
        RefineryUtilities.centerFrameOnScreen(plotter);
        plotter.setVisible(true);
    }

    private static void parseArgument(String args[]) throws Exception {
        Options options = new Options();
        options.addOption("s", "skip-bounded", false, "skip bounded constrain calculation");
        CommandLineParser parser = new DefaultParser();
        CommandLine cli = parser.parse(options, args);
        skipBounded = cli.hasOption("s");
        for (String path : cli.getArgList()) {
            inputPaths.add(path);
        }
    }
    
    private static Map<Integer, Stock> processFile(String path) throws Exception {
        Map<Integer, Stock> stockMap = new TreeMap<>(); 
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
