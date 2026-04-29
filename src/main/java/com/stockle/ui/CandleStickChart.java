package com.stockle.ui;

import com.stockle.model.CandleData;

import javafx.beans.NamedArg;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

public class CandleStickChart extends XYChart<String, Number> {
    private static final Color BULL_FILL   = Color.web("#16a34a");
    private static final Color BULL_STROKE = Color.web("#15803d");
    private static final Color BEAR_FILL   = Color.web("#dc2626");
    private static final Color BEAR_STROKE = Color.web("#b91c1c");
    private static final Color WICK_COLOR  = Color.web("#6b7280");

    public CandleStickChart(@NamedArg("xAxis") CategoryAxis xAxis, @NamedArg("yAxis") NumberAxis yAxis) {
        super(xAxis, yAxis);
        setAnimated(false);
        setLegendVisible(false);
    }

    // XYChart methods
    @Override
    protected void dataItemAdded(Series<String, Number> series, int itemIndex, Data<String, Number> item) {
        Node candle = createCandleNode();
        item.setNode(candle);
        getPlotChildren().add(candle);
    }

    @Override
    protected void dataItemRemoved(Data<String, Number> item, Series<String, Number> series) {
        getPlotChildren().remove(item.getNode());
    }

    @Override
    protected void dataItemChanged(Data<String, Number> item) {}

    @Override
    protected void seriesAdded(Series<String, Number> series, int seriesIndex) {
        for (int i = 0; i < series.getData().size(); i++) {
            dataItemAdded(series, i, series.getData().get(i));
        }
    }

    @Override
    protected void seriesRemoved(Series<String, Number> series) {
        for (Data<String, Number> item : series.getData()) {
            getPlotChildren().remove(item.getNode());
        }
    }

    // Layout

    @Override
    protected void layoutPlotChildren() {
        if (getData().isEmpty()) return;

        CategoryAxis xAxis = (CategoryAxis) getXAxis();
        NumberAxis   yAxis = (NumberAxis)   getYAxis();

        // Candle body width = 60% of the space each category gets
        double candleWidth = Math.max(2, xAxis.getCategorySpacing() * 0.6);

        for (Series<String, Number> series : getData()) {
            for (Data<String, Number> item : series.getData()) {
                CandleData cd = (CandleData) item.getExtraValue();
                if (cd == null || !(item.getNode() instanceof Group)) continue;

                double x = xAxis.getDisplayPosition(item.getXValue());
                double yOpen = yAxis.getDisplayPosition(cd.open);
                double yHigh = yAxis.getDisplayPosition(cd.high);
                double yLow = yAxis.getDisplayPosition(cd.low);
                double yClose = yAxis.getDisplayPosition(cd.close);

                Group group = (Group)item.getNode();
                Line wick  = (Line)group.getChildren().get(0);
                Rectangle body = (Rectangle)group.getChildren().get(1);

                // Wick: full high to low range
                wick.setStartX(x); 
                wick.setEndX(x);
                wick.setStartY(yHigh); 
                wick.setEndY(yLow);

                // Body: open to close range
                double bodyTop = Math.min(yOpen, yClose);
                double bodyHeight = Math.max(1, Math.abs(yClose - yOpen));

                body.setX(x - candleWidth / 2);
                body.setY(bodyTop);
                body.setWidth(candleWidth);
                body.setHeight(bodyHeight);

                boolean bullish = cd.close >= cd.open;
                body.setFill(bullish ? BULL_FILL : BEAR_FILL);
                body.setStroke(bullish ? BULL_STROKE : BEAR_STROKE);
            }
        }
    }

    // Helpers
    private Node createCandleNode() {
        Line wick = new Line();
        wick.setStroke(WICK_COLOR);
        wick.setStrokeWidth(1.5);
        Rectangle body = new Rectangle();
        body.setStrokeWidth(1);
        return new Group(wick, body);
    }

    // Load from a list of CandleData
    public void setCandles(java.util.List<CandleData> candles) {
        Series<String, Number> series = new Series<>();
        double min = Double.MAX_VALUE, max = Double.MIN_VALUE;
        for (CandleData cd : candles) {
            Data<String, Number> point = new Data<>(cd.time, cd.close);
            point.setExtraValue(cd);
            series.getData().add(point);
            if (cd.low  < min) min = cd.low;
            if (cd.high > max) max = cd.high;
        }
        setData(javafx.collections.FXCollections.observableArrayList(series));

        // Zoom the Y axis to the data range with a small 1% padding
        double padding = (max - min) * 0.1;
        NumberAxis yAxis = (NumberAxis) getYAxis();
        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(min - padding);
        yAxis.setUpperBound(max + padding);
        yAxis.setTickUnit((max - min) / 5.0);
    }
}
