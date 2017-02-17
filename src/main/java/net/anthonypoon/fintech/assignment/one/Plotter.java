/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.anthonypoon.fintech.assignment.one;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.util.List;
import javax.swing.JFrame;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.FastScatterPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.UnknownKeyException;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;

/**
 *
 * @author ypoon
 */
public class Plotter extends ApplicationFrame {
    private XYSeriesCollection dataObj = new XYSeriesCollection();
    private String title;
    public Plotter(String title) {
        super(title);       
        this.title = title;
    }
    
    public void addXYPoint(String seriesName, Double x, Double y) {
        XYSeries dataSeries = null;
        try {
            dataSeries = dataObj.getSeries(seriesName);
        } catch (UnknownKeyException ex) {
            dataSeries = new XYSeries(seriesName, false);
            dataObj.addSeries(dataSeries);
        }
        dataSeries.add(x, y);
    }
    
    public void addCurve(FrontierCurve curve) {
        XYSeries dataSeries = new XYSeries(curve.getName(), false);
        for (int i = 0; i < curve.size(); i++) {
            dataSeries.add(curve.getPt(i).getX(), curve.getPt(i).getY());
        }
        dataObj.addSeries(dataSeries);
    }
    
    public void render() {
        JFreeChart chart = ChartFactory.createXYLineChart(
                this.title,
                "std",
                "Return",
                dataObj,
                PlotOrientation.VERTICAL ,
                true,
                true,
                false
            );
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(560 , 367));
        XYPlot plot = chart.getXYPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer( );
        renderer.setSeriesPaint(0, Color.RED);
        renderer.setSeriesStroke(0, new BasicStroke( 4.0f ) );
        plot.setRenderer(renderer);        
        setContentPane(chartPanel);
    }

    void addList(String seriesName, List<Point2D.Double> ptList) {
        XYSeries dataSeries = new XYSeries(seriesName, false);
        for (Point2D.Double pt : ptList) {
            dataSeries.add(pt.getX(), pt.getY());
        }
        dataObj.addSeries(dataSeries);
    }
}
