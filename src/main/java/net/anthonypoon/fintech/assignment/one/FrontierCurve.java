/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.anthonypoon.fintech.assignment.one;

import java.awt.geom.Point2D;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author ypoon
 */
public class FrontierCurve{
    private String name = "EFT Curve";
    private List<Point2D.Double> xyPoints = new ArrayList();
    private List<Map<Integer, Double>> xyPtWeightList = new ArrayList();
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void addPt(double x, double y, Map<Integer, Double> weightMap) {
        xyPoints.add(new Point2D.Double(x, y));
        xyPtWeightList.add(weightMap);
    }
    
    public int size() {
        return xyPoints.size();
    }
    
    public Point2D.Double getPt(int i) {
        return xyPoints.get(i);
    }
    
    public Map<Integer, Double> getWeight(int i) {
        return xyPtWeightList.get(i);
    }
    
    public String getName() {
        return name;        
    }
    
    public void printWeight(int i) {
        DecimalFormat df = new DecimalFormat("0.00");
        System.out.print("pt(" + df.format(xyPoints.get(i).getX()) + ", " + df.format(xyPoints.get(i).getY())+ "): ");
        for (Map.Entry<Integer, Double> pair : xyPtWeightList.get(i).entrySet()) {
            //System.out.printf("%-5s", pair.getKey());
            System.out.printf("%-10s", df.format(pair.getValue()));
        }
        System.out.println();
    }
}
