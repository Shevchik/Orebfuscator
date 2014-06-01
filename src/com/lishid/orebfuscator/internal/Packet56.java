/*
 * Copyright (C) 2011-2014 lishid.  All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation,  version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.lishid.orebfuscator.internal;

import java.util.zip.Deflater;

import net.minecraft.server.v1_6_R3.Packet56MapChunkBulk;

import com.lishid.orebfuscator.utils.ReflectionHelper;

public class Packet56 {
	Packet56MapChunkBulk packet;

	private byte[][] inflatedBuffers;

	private byte[] buildBuffer;

	public void setPacket(Object packet) {
		if (packet instanceof Packet56MapChunkBulk) {
			this.packet = (Packet56MapChunkBulk) packet;
			inflatedBuffers = (byte[][]) ReflectionHelper.getPrivateField(packet, Fields.Packet56Fields.getInflatedBuffersFieldName());
		}

		int bufferSize = 0;
		for (int i = 0; i < inflatedBuffers.length; i++) {
			bufferSize += inflatedBuffers[i].length;
		}

		buildBuffer = new byte[bufferSize];
		bufferSize = 0;
		for (int i = 0; i < inflatedBuffers.length; i++) {
			System.arraycopy(inflatedBuffers[i], 0, buildBuffer, bufferSize, inflatedBuffers[i].length);
			bufferSize += inflatedBuffers[i].length;
		}
	}

	public int getPacketChunkNumber() {
		return packet.d();
	}

	public int[] getX() {
		return (int[]) ReflectionHelper.getPrivateField(packet, Fields.Packet56Fields.getChunksXCoordsFieldName());
	}

	public int[] getZ() {
		return (int[]) ReflectionHelper.getPrivateField(packet, Fields.Packet56Fields.getChunksZCoordsFieldName());
	}

	public int[] getChunkMask() {
		return packet.a;
	}

	public int[] getExtraMask() {
		return packet.b;
	}

	public byte[][] getInflatedBuffers() {
		return inflatedBuffers;
	}

	public byte[] getBuildBuffer() {
		return buildBuffer;
	}

	public void compress() {
		Deflater deflater = new Deflater(Deflater.NO_COMPRESSION);
		deflater.setInput(buildBuffer);
		deflater.finish();

		byte[] outputBuffer = new byte[buildBuffer.length + 100];
		ReflectionHelper.setPrivateField(packet, Fields.Packet56Fields.getOutputBufferFieldName(), outputBuffer);
		ReflectionHelper.setPrivateField(packet, Fields.Packet56Fields.getCompressedSizeFieldName(), deflater.deflate(outputBuffer));
	}

}
