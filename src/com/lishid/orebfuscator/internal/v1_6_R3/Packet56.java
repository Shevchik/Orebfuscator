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



//Volatile
import net.minecraft.server.v1_6_R3.Packet56MapChunkBulk;

import com.lishid.orebfuscator.OrebfuscatorConfig;
import com.lishid.orebfuscator.internal.IPacket56;
import com.lishid.orebfuscator.utils.ReflectionHelper;

public class Packet56 implements IPacket56 {
	Packet56MapChunkBulk packet;

	byte[][] inflatedBuffers;

	@Override
	public void setPacket(Object packet) {
		if (packet instanceof Packet56MapChunkBulk) {
			this.packet = (Packet56MapChunkBulk) packet;
			inflatedBuffers = (byte[][]) ReflectionHelper.getPrivateField(packet, "field_73584_f");
		}
	}

	@Override
	public int getPacketChunkNumber() {
		return packet.d();
	}

	@Override
	public int[] getX() {
		return (int[]) ReflectionHelper.getPrivateField(packet, "field_73589_c");
	}

	@Override
	public int[] getZ() {
		return (int[]) ReflectionHelper.getPrivateField(packet, "field_73586_d");
	}

	@Override
	public int[] getChunkMask() {
		return packet.a;
	}

	@Override
	public int[] getExtraMask() {
		return packet.b;
	}

	@Override
	public byte[][] getInflatedBuffers() {
		return inflatedBuffers;
	}

	@Override
	public void compress() {
		int finalBufferSize = 0;
		for (int i = 0; i < inflatedBuffers.length; i++) {
			finalBufferSize += inflatedBuffers[i].length;
		}

		byte[] finalbuffer = new byte[finalBufferSize + 100];
		finalBufferSize = 0;
		for (int i = 0; i < inflatedBuffers.length; i++) {
			System.arraycopy(inflatedBuffers[i], 0, finalbuffer, finalBufferSize, inflatedBuffers[i].length);
			finalBufferSize += inflatedBuffers[i].length;
		}
		ReflectionHelper.setPrivateField(packet, "field_73587_e", finalbuffer);
		
		Deflater deflater = new Deflater(OrebfuscatorConfig.CompressionLevel);
		deflater.setInput(finalbuffer);
		deflater.finish();

		ReflectionHelper.setPrivateField(packet, "field_73585_g", deflater.deflate(finalbuffer));
	}

}
