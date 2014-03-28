package com.lishid.orebfuscator.obfuscation;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.lishid.orebfuscator.OrebfuscatorConfig;

public class ProcessingThreads {

	public static ProcessingThreads instance;
	public static void initialize() {
		instance = new ProcessingThreads();
	}

	private ExecutorService chunkProcessingService = null;
	private ExecutorService blockUpdateProcessingService = null;
	public void startThreads() {
		chunkProcessingService = Executors.newFixedThreadPool(OrebfuscatorConfig.ProcessingThreads);
		blockUpdateProcessingService = Executors.newSingleThreadExecutor();
	}
	public void stopThreads() {
		if (chunkProcessingService != null) {
			chunkProcessingService.shutdownNow();
		}
		if (blockUpdateProcessingService != null) {
			blockUpdateProcessingService.shutdownNow();
		}
	}

	public void submitChunkObfuscate(Runnable run) {
		chunkProcessingService.submit(run);
	}

	public void submitBlockUpdate(Runnable run) {
		blockUpdateProcessingService.submit(run);
	}

}
