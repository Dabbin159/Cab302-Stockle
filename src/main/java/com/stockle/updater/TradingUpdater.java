package com.stockle.updater;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import com.stockle.api.data.BarData;
import com.stockle.api.service.MarketDataService;

/**
 * Updates trading data automatically
 * Uses market data API endpoint
 */
public class TradingUpdater {

	/**
	 * Listener for trading updates and errors
	 */
	public interface Listener {
		/**
		 * Called when price data is updated
		 * @param symbol stock symbol that was updated
		 * @param bar latest bar data for the symbol
		 */
		void onPriceUpdate(String symbol, BarData bar);
		/**
		 * Called when an error occurs during trading update
		 * @param symbol stock symbol that was being updated when the error occurred
		 * @param exception the exception that was thrown
		 */
		void onError(String symbol, Exception exception);
	}

	private final MarketDataService marketDataService;
	private final Supplier<String> symbolSupplier;
	private final Listener listener;
	private final String feed;
	private final long intervalSeconds;

	private volatile boolean running = false;
	private volatile ScheduledExecutorService scheduler;

	/**
	 * Creates a new TradingUpdater
	 * @param marketDataService market data service to fetch price data from
	 * @param symbolSupplier supplier for obtaining stock symbols
	 * @param listener listener for handling trading updates and errors
	 * @param feed market data feed to use
	 * @param intervalSeconds interval in seconds between trading updates
	 */
	public TradingUpdater(
			MarketDataService marketDataService,
			Supplier<String> symbolSupplier,
			Listener listener,
			String feed,
			long intervalSeconds
	) {
		this.marketDataService = marketDataService;
		this.symbolSupplier = symbolSupplier;
		this.listener = listener;
		this.feed = feed;
		this.intervalSeconds = Math.max(1L, intervalSeconds);
	}

	/**
	 * Starts the trading updater
	 */
	public synchronized void start() {
		if (running) {
			return;
		}

		running = true;
		scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
			Thread t = new Thread(r, "TradingUpdaterThread");
			t.setDaemon(true);
			return t;
		});

		scheduler.scheduleAtFixedRate(this::pollOnce, intervalSeconds, intervalSeconds, TimeUnit.SECONDS);
	}

	/**
	 * Stops the trading updater
	 */
	public synchronized void stop() {
		if (!running) {
			return;
		}

		running = false;
		if (scheduler != null) {
			scheduler.shutdownNow();
			scheduler = null;
		}
	}

	/**
	 * Refreshes trading data immediately
	 */
	public void refreshNow() {
		ScheduledExecutorService current = scheduler;
		if (!running || current == null || current.isShutdown()) {
			return;
		}
		current.execute(this::pollOnce);
	}

	/**
	 * Polls trading data for current symbol once and notifies listener
	 */
	private void pollOnce() {
		String symbol = symbolSupplier.get();
		if (symbol == null || symbol.isBlank()) {
			return;
		}

		try {
			Map<String, BarData> bars = marketDataService.getLatestBars(List.of(symbol), feed);
			BarData bar = bars.get(symbol);
			if (bar == null) {
				return;
			}
			listener.onPriceUpdate(symbol, bar);
		} catch (Exception e) {
			listener.onError(symbol, e);
		}
	}
}
