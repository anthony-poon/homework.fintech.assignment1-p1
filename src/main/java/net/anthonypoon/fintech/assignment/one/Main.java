/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.anthonypoon.fintech.assignment.one;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.output.TeeOutputStream;

/**
 *
 * @author ypoon
 */
public class Main {
    private static Date minYear;
    private static Date maxYear;
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
    private static Map<Integer, Stock> stockList = new TreeMap<>();

    public static void main(String args[]) throws Exception {
        
        parseArgument(args);
        String minYearStr = new SimpleDateFormat("y").format(minYear);
        String maxYearStr = new SimpleDateFormat("y").format(maxYear);
        FileOutputStream outFile = new FileOutputStream("output_" + minYearStr +"_" + maxYearStr + ".txt");
        TeeOutputStream teeStream = new TeeOutputStream(System.out, outFile);
        PrintStream ps = new PrintStream(teeStream);
        System.setOut(ps);        
        DecimalFormat decimalFormatter = new DecimalFormat("0.000");
        
        BufferedReader reader = new BufferedReader(new FileReader(inputPath));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] strArray = line.split(",");
            SimpleDateFormat dateFormatter = new SimpleDateFormat("d/M/y");
            Date date = dateFormatter.parse(strArray[0]);
            if (date.after(minYear) && date.before(maxYear)) {
                for (int col = 1; col <= PRESET_STOCK_CODE.length; col++ ) {
                    if (!stockList.containsKey(PRESET_STOCK_CODE[col - 1])) {
                        stockList.put(PRESET_STOCK_CODE[col - 1], new Stock(PRESET_STOCK_CODE[col - 1], PRESET_STOCK_NAME[col - 1]));
                    }
                    Stock currentStock = stockList.get(PRESET_STOCK_CODE[col - 1]);
                    currentStock.addEntry(date, Double.valueOf(strArray[col]));
                }
            }
        }
        
        System.out.println("Output from " + minYearStr + " to " + maxYearStr);
        for (Map.Entry<Integer, Stock> pair : stockList.entrySet()) {
            Stock stock = pair.getValue();
            System.out.println("Stock #" + pair.getKey() + " Std = " + decimalFormatter.format(stock.getStd()));
        }
         
        System.out.println();
        System.out.println("Covariance Matrix:");
        for (Stock stock1 : stockList.values()) {
            for (Stock stock2 : stockList.values()) {
                System.out.print(decimalFormatter.format(Stock.getCovariance(stock1, stock2)) + "\t");
            }
            System.out.println();
        }
        System.out.println();
        System.out.println("Coefficient Matrix:");
        for (Stock stock1 : stockList.values()) {
            for (Stock stock2 : stockList.values()) {
                System.out.print(decimalFormatter.format(Stock.getCoefficient(stock1, stock2)) + "\t");
            }
            System.out.println();
        }
    }
    
    private static void parseArgument(String args[]) throws Exception {
        Options options = new Options();
        options.addOption("min", true, "Min year. Default = 2015");
        options.addOption("max", true, "Max year. Default = 2017");
        CommandLineParser parser = new DefaultParser();
        CommandLine cli = parser.parse(options, args);
        if (cli.hasOption("min")) {
            minYear = new SimpleDateFormat("d/M/y").parse("01/01/" + cli.getOptionValue("min"));
        } else {
            minYear = new SimpleDateFormat("d/M/y").parse("01/01/2015");
        }
        
        if (cli.hasOption("max")) {
            maxYear = new SimpleDateFormat("d/M/y").parse("31/12/" + cli.getOptionValue("max"));
        } else {
            maxYear = new SimpleDateFormat("d/M/y").parse("31/12/" + cli.hasOption("max"));
        }
        
        inputPath = cli.getArgList().get(0);
    }    
}
