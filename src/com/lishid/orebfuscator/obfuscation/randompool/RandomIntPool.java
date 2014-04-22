/**
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 */

package com.lishid.orebfuscator.obfuscation.randompool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.lishid.orebfuscator.OrebfuscatorConfig;

public class RandomIntPool {

	private static RandomIntPool instance;
	public static RandomIntPool getInstance() {
		return instance;
	}
	public static void initialize() {
		instance = new RandomIntPool();
	}

	private volatile boolean generatorrunning = true;
	public void start() {
		generatorrunning = true;
		ExecutorService executor = Executors.newSingleThreadExecutor();
		executor.submit(
			new Runnable() {
				@Override
				public void run() {
					while (generatorrunning) {
						randomnormalqueue.put(OrebfuscatorConfig.getRandomBlockID(false));
						randomnetherqueue.put(OrebfuscatorConfig.getRandomBlockID(true));
						try {Thread.sleep(0);} catch (InterruptedException e) {}
					}
				}
			}
		);
		executor.shutdown();
	}

	public void stop() {
		generatorrunning = false;
	}

	private PrimitiveIntLinkedBlockingQueue randomnormalqueue = new PrimitiveIntLinkedBlockingQueue(30*1024*1024);
	private PrimitiveIntLinkedBlockingQueue randomnetherqueue = new PrimitiveIntLinkedBlockingQueue(30*1024*1024);

	public static int getRandomBlockID(boolean nether) {
		if (nether) {
			return instance.randomnetherqueue.take();
		}
		return instance.randomnormalqueue.take();
	}

}
