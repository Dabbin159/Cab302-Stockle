package com.stockle.updater;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import com.stockle.api.data.NewsArticle;
import com.stockle.api.service.NewsService;

/**
 * Updates news articles automatically
 * Uses news API endpoint
 */
public class NewsUpdater {

	/**
	 * Listener for news updates and errors
	 */
	public interface Listener {
		/**
		 * Called when news articles are updated
		 * @param symbol stock symbol that was updated
		 * @param articles list of news articles for the symbol
		 */
		void onNewsUpdate(String symbol, List<NewsArticle> articles);

		/**
		 * Called when an error occurs during news update
		 * @param symbol stock symbol that was being updated when the error occurred
		 * @param exception the exception that was thrown
		 */
		void onError(String symbol, Exception exception);
	}

	private final NewsService newsService;
	private final Supplier<String> symbolSupplier;
	private final Listener listener;
	private final int newsLimit;
	private final long intervalSeconds;

	private volatile boolean running = false;
	private volatile ScheduledExecutorService scheduler;

	/**
	 * Creates a new NewsUpdater
	 * @param newsService news service to fetch news articles from
	 * @param symbolSupplier supplier for obtaining stock symbols
	 * @param listener listener for handling news updates and errors
	 * @param newsLimit maximum number of news articles to fetch
	 * @param intervalSeconds interval in seconds between news updates
	 */
	public NewsUpdater(
			NewsService newsService,
			Supplier<String> symbolSupplier,
			Listener listener,
			int newsLimit,
			long intervalSeconds
	) {
		this.newsService = newsService;
		this.symbolSupplier = symbolSupplier;
		this.listener = listener;
		this.newsLimit = Math.max(1, newsLimit);
		this.intervalSeconds = Math.max(1L, intervalSeconds);
	}

	/**
	 * Starts the news updater
	 */
	public synchronized void start() {
		if (running) {
			return;
		}

		running = true;
		scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
			Thread t = new Thread(r, "NewsUpdaterThread");
			t.setDaemon(true);
			return t;
		});

		scheduler.scheduleAtFixedRate(this::pollOnce, intervalSeconds, intervalSeconds, TimeUnit.SECONDS);
	}

	/**
	 * Stops the news updater
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
	 * Refreshes news immediately
	 */
	public void refreshNow() {
		ScheduledExecutorService current = scheduler;
		if (!running || current == null || current.isShutdown()) {
			return;
		}
		current.execute(this::pollOnce);
	}

	/**
	 * Polls news for current symbol once and notifies listener
	 */
	private void pollOnce() {
		String symbol = symbolSupplier.get();
		if (symbol == null || symbol.isBlank()) {
			return;
		}

		try {
			NewsService.NewsPage page = newsService.getNewsPage(newsLimit, null, symbol);
			listener.onNewsUpdate(symbol, page.articles());
		} catch (Exception e) {
			listener.onError(symbol, e);
		}
	}
}
