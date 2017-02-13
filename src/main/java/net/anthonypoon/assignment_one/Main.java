/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.anthonypoon.assignment_one;

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
import net.anthonypoon.assignment_one.DataObject.Stock;
import net.anthonypoon.assignment_one.DataObject.StockPriceEntry;
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
    private static int minYear = 2015;
    private static int maxYear = 2017;
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
    private static Map<Integer, Stock> stockArray = new TreeMap<>();

    public static void main(String args[]) throws Exception {
        
        parseArgument(args);     
                
        FileOutputStream outFile = new FileOutputStream("output_" + minYear +"_" + maxYear + ".txt");
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
            for (int col = 1; col <= PRESET_STOCK_CODE.length; col++ ) {
                if (!stockArray.containsKey(PRESET_STOCK_CODE[col - 1])) {
                    stockArray.put(PRESET_STOCK_CODE[col - 1], new Stock(PRESET_STOCK_CODE[col - 1], PRESET_STOCK_NAME[col - 1]));
                }
                Stock currentStock = stockArray.get(PRESET_STOCK_CODE[col - 1]);
                currentStock.addEntry(date, Double.valueOf(strArray[col]));
            }
        }
        
        System.out.println("Output from " + minYear + " to " + maxYear);
        for (Map.Entry<Integer, Stock> pair : stockArray.entrySet()) {
            Stock stock = pair.getValue();
            System.out.println("Stock #" + pair.getKey() + " Std = " + decimalFormatter.format(stock.getStdByYear(minYear, maxYear)));
        }
         
        System.out.println();
        System.out.println("Covariance Matrix:");
        for (Stock stock1 : stockArray.values()) {
            for (Stock stock2 : stockArray.values()) {
                System.out.print(decimalFormatter.format(getCovariance(stock1, stock2)) + "\t");
            }
            System.out.println();
        }
        System.out.println();
        System.out.println("Coefficient Matrix:");
        for (Stock stock1 : stockArray.values()) {
            for (Stock stock2 : stockArray.values()) {
                System.out.print(decimalFormatter.format(getCoefficient(stock1, stock2)) + "\t");
            }
            System.out.println();
        }
    }
    
    private static void parseArgument(String args[]) throws ParseException {
        Options options = new Options();
        options.addOption("min", true, "Min year. Default = 2015");
        options.addOption("max", true, "Max year. Default = 2017");
        CommandLineParser parser = new DefaultParser();
        CommandLine cli = parser.parse(options, args);
        if (cli.hasOption("min")) {
            minYear = Integer.valueOf(cli.getOptionValue("min"));
        }
        
        if (cli.hasOption("max")) {
            maxYear = Integer.valueOf(cli.getOptionValue("max"));
        }
        
        inputPath = cli.getArgList().get(0);
    }
    
    private static Double getCovariance(Stock stock1, Stock stock2) {
        Double stock1Avg = stock1.getUAverageByYear(minYear, maxYear);
        Double stock2Avg = stock2.getUAverageByYear(minYear, maxYear);
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
    
    private static Double getCoefficient(Stock stock1, Stock stock2) {
        Double covar = getCovariance(stock1, stock2);
        return covar / (stock1.getStdByYear(minYear, maxYear) * stock2.getStdByYear(minYear, maxYear));
    }
}
