package com.lishid.orebfuscator.listeners;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.lishid.orebfuscator.OrebfuscatorConfig;

public class ProcessingThreads {

	public static ProcessingThreads instance;
	public static void initialize() {
		instance = new ProcessingThreads();
	}

	private ExecutorService blockUpdateProcessingService = null;
	public void startThreads() {
		blockUpdateProcessingService = Executors.newFixedThreadPool(OrebfuscatorConfig.ProcessingThreads);
	}
	public void stopThreads() {
		if (blockUpdateProcessingService != null) {
			blockUpdateProcessingService.shutdownNow();
		}
	}

	public void submitBlockUpdate(Runnable run) {
		blockUpdateProcessingService.submit(run);
	}

}
