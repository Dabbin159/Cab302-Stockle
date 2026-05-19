package com.stockle.updater;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import com.stockle.api.data.NewsArticle;
import com.stockle.api.service.NewsService;

public class NewsUpdater {

	public interface Listener {
		void onNewsUpdate(String symbol, List<NewsArticle> articles);
		void onError(String symbol, Exception exception);
	}

	private final NewsService newsService;
	private final Supplier<String> symbolSupplier;
	private final Listener listener;
	private final int newsLimit;
	private final long intervalSeconds;

	private volatile boolean running = false;
	private volatile ScheduledExecutorService scheduler;

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
			NewsService.NewsPage page = newsService.getNewsPage(newsLimit, null, symbol);
			listener.onNewsUpdate(symbol, page.articles());
		} catch (Exception e) {
			listener.onError(symbol, e);
		}
	}
}
