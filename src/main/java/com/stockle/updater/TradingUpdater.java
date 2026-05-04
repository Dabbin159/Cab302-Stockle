package com.stockle.updater;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import com.stockle.api.data.BarData;
import com.stockle.api.service.MarketDataService;

public class TradingUpdater {

	public interface Listener {
		void onPriceUpdate(String symbol, BarData bar);
		void onError(String symbol, Exception exception);
	}

	private final MarketDataService marketDataService;
	private final Supplier<String> symbolSupplier;
	private final Listener listener;
	private final String feed;
	private final long intervalSeconds;

	private volatile boolean running = false;
	private volatile ScheduledExecutorService scheduler;

	public TradingUpdater(
			MarketDataService marketDataService,
			Supplier<String> symbolSupplier,
			Listener listener,
			String feed,
			long intervalSeconds
	) {
		this.marketDataService = Objects.requireNonNull(marketDataService, "marketDataService");
		this.symbolSupplier = Objects.requireNonNull(symbolSupplier, "symbolSupplier");
		this.listener = Objects.requireNonNull(listener, "listener");
		this.feed = (feed == null || feed.isBlank()) ? "iex" : feed;
		this.intervalSeconds = Math.max(1L, intervalSeconds);
	}

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

	public void refreshNow() {
		ScheduledExecutorService current = scheduler;
		if (!running || current == null || current.isShutdown()) {
			return;
		}
		current.execute(this::pollOnce);
	}

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
