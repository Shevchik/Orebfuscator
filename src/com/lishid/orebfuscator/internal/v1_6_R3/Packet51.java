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

package com.lishid.orebfuscator.internal.v1_6_R3;

import java.util.zip.Deflater;

import net.minecraft.server.v1_6_R3.Packet51MapChunk;

import com.lishid.orebfuscator.utils.ReflectionHelper;

public class Packet51 {

	Packet51MapChunk packet;

	private byte[] inflatedBuffer;
	private byte[] buildBuffer;

	public void setPacket(Object packet) {
		if (packet instanceof Packet51MapChunk) {
			this.packet = (Packet51MapChunk) packet;
			inflatedBuffer = (byte[]) ReflectionHelper.getPrivateField(packet, "field_73596_g");
		}
		buildBuffer = new byte[inflatedBuffer.length];
		System.arraycopy(inflatedBuffer, 0, buildBuffer, 0, inflatedBuffer.length);
	}

	public int getX() {
		return packet.a;
	}

	public int getZ() {
		return packet.b;
	}

	public int getChunkMask() {
		return packet.c;
	}

	public int getExtraMask() {
		return packet.d;
	}

	public byte[] getInflatedBuffer() {
		return inflatedBuffer;
	}

	public byte[] getBuildBuffer() {
		return buildBuffer;
	}

	public void compress() {
		Deflater deflater = new Deflater(Deflater.NO_COMPRESSION);
		deflater.setInput(buildBuffer);
		deflater.finish();

		byte[] outputBuffer = (byte[]) ReflectionHelper.getPrivateField(packet, "field_73595_f");
		ReflectionHelper.setPrivateField(packet, "field_73602_h", deflater.deflate(outputBuffer));
	}

}
