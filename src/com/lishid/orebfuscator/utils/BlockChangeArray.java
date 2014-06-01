package com.lishid.orebfuscator.utils;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

/**
 * Utility class for creating arrays of block changes.
 * <p>
 * See also {@link Packet34MultiBlockChange}.
 * 
 * @author Kristian
 */
public class BlockChangeArray {
	/**
	 * Represents a single block change.
	 * <p>
	 * Retrieved by {@link BlockChangeArray#getBlockChange(int)}.
	 * 
	 * @author Kristian
	 */
	public class BlockChange {
		// Index of the block change entry that we may change
		private final int index;

		private BlockChange(int index) {
			this.index = index;
		}

		/**
		 * Retrieve the relative x-axis position of the current block change.
		 * 
		 * @return X-axis position of the block change.
		 */
		public int getRelativeX() {
			return getValue(28, 0xF0000000);
		}

		/**
		 * Retrieve the relative z-axis position of the current block change.
		 * 
		 * @return Z-axis position of the block change.
		 */
		public byte getRelativeZ() {
			return (byte) getValue(24, 0xF000000);
		}

		/**
		 * Retrieve the absolute y-axis position of the current block change.
		 * 
		 * @return Y-axis position of the block change.
		 */
		public int getAbsoluteY() {
			return getValue(16, 0xFF0000);
		}

		/**
		 * Retrieve the block ID of the current block change.
		 * 
		 * @return The block ID that the block will change into.
		 */
		public int getBlockID() {
			return getValue(4, 0xFFF0);
		}

		/**
		 * Retrieve the index of the current block change.
		 * 
		 * @return Index of the current block change.
		 */
		public int getIndex() {
			return index;
		}

		private int getValue(int rightShift, int updateMask) {
			return (data[index] & updateMask) >> rightShift;
		}
	}

	/**
	 * Single of a single block change record in bytes.
	 */
	private static final int RECORD_SIZE = 4;

	/**
	 * The internally backed array.
	 */
	private int[] data;

	/**
	 * Construct a new block change array from the copy of a given data array.
	 * 
	 * @param data
	 *            - the data array to store internally.
	 */
	public BlockChangeArray(byte[] input) {
		IntBuffer source = ByteBuffer.wrap(input).asIntBuffer();
		IntBuffer destination = IntBuffer.allocate(input.length / RECORD_SIZE);
		destination.put(source);

		// Get the copied array
		data = destination.array();
	}

	/**
	 * Retrieve a view of the block change entry at the given index.
	 * <p>
	 * Any modification to this view will be stored in the block change array
	 * itself.
	 * 
	 * @param index
	 *            - index of the block change to retrieve.
	 * @return A view of the block change entry.
	 */
	public BlockChange getBlockChange(int index) {
		if (index < 0 || index >= getSize())
			throw new IllegalArgumentException("Index is out of bounds.");
		return new BlockChange(index);
	}

	/**
	 * Retrieve the number of block changes.
	 * 
	 * @return The number of block changes.
	 */
	public int getSize() {
		return data.length;
	}

}
