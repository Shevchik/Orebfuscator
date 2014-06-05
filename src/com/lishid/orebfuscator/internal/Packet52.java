package com.lishid.orebfuscator.internal;

import net.minecraft.server.v1_6_R3.Packet;
import net.minecraft.server.v1_6_R3.Packet52MultiBlockChange;

public class Packet52 {
	private Packet52MultiBlockChange packet;

	public Packet52(Packet packet) {
		this.packet = (Packet52MultiBlockChange) packet;
	}

	public int getChunkX() {
		return packet.a;
	}

	public int getChunkZ() {
		return packet.b;
	}

	public int getRecordsCount() {
		return packet.d;
	}

	public byte[] getRecords() {
		return packet.c;
	}

}
