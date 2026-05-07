package com.stockle.ui;

import com.stockle.model.CandleData;

import javafx.beans.NamedArg;
import javafx.util.StringConverter;
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

    /**
     * Constructs a CandleStickChart with the given axes.
     * The @NamedArg annotations allow FXMLLoader to instantiate this chart from FXML.
     *
     * @param xAxis the category axis used for time labels
     * @param yAxis the number axis used for price values
     */
    public CandleStickChart(@NamedArg("xAxis") CategoryAxis xAxis, @NamedArg("yAxis") NumberAxis yAxis) {
        super(xAxis, yAxis);
        setAnimated(false);
        setLegendVisible(false);
    }

    /**
     * Creates a candle node and adds it to the plot when a data item is added.
     *
     * @param series the series the item belongs to
     * @param itemIndex the index of the new item within the series
     * @param item the data item that was added
     */
    @Override
    protected void dataItemAdded(Series<String, Number> series, int itemIndex, Data<String, Number> item) {
        Node candle = createCandleNode();
        item.setNode(candle);
        getPlotChildren().add(candle);
    }

    /**
     * Removes the candle node from the plot when a data item is removed.
     *
     * @param item the data item that was removed
     * @param series the series the item belonged to
     */
    @Override
    protected void dataItemRemoved(Data<String, Number> item, Series<String, Number> series) {
        getPlotChildren().remove(item.getNode());
    }

    /** No-op — candle appearance is driven entirely by layoutPlotChildren. */
    @Override
    protected void dataItemChanged(Data<String, Number> item) {}

    /**
     * Creates candle nodes for every item when a series is added.
     *
     * @param series the series that was added
     * @param seriesIndex the index of the series within the chart
     */
    @Override
    protected void seriesAdded(Series<String, Number> series, int seriesIndex) {
        for (int i = 0; i < series.getData().size(); i++) {
            dataItemAdded(series, i, series.getData().get(i));
        }
    }

    /**
     * Removes all candle nodes belonging to a series when it is removed.
     *
     * @param series the series that was removed
     */
    @Override
    protected void seriesRemoved(Series<String, Number> series) {
        for (Data<String, Number> item : series.getData()) {
            getPlotChildren().remove(item.getNode());
        }
    }

    /**
     * Positions and sizes every candle on each layout pass.
     * Candle width is 60% of the per-category spacing.
     */
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

    /**
     * Creates a blank candle node (wick Line + body Rectangle inside a Group).
     * Coordinates are set later in layoutPlotChildren.
     *
     * @return a new Group representing a single candle
     */
    private Node createCandleNode() {
        Line wick = new Line();
        wick.setStroke(WICK_COLOR);
        wick.setStrokeWidth(1.5);
        Rectangle body = new Rectangle();
        body.setStrokeWidth(1);
        return new Group(wick, body);
    }

    /**
     * Replaces the chart data with the given candles and auto-scales the y-axis.
     * Adds 10% padding above and below the price range. Tick labels are 2 decimal places.
     *
     * @param candles the list of CandleData to display
     */
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

        double padding = (max - min) * 0.1;
        NumberAxis yAxis = (NumberAxis) getYAxis();
        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(min - padding);
        yAxis.setUpperBound(max + padding);
        yAxis.setTickUnit((max - min) / 5.0);
        yAxis.setTickLabelFormatter(new StringConverter<Number>() {
            @Override public String toString(Number n) { return String.format("%.2f", n.doubleValue()); }
            @Override public Number fromString(String s) { return Double.parseDouble(s); }
        });
    }
}
