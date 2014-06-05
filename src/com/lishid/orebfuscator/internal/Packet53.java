package com.lishid.orebfuscator.internal;

import net.minecraft.server.v1_6_R3.Packet;
import net.minecraft.server.v1_6_R3.Packet53BlockChange;

public class Packet53 {
	private Packet53BlockChange packet;

	public Packet53(Packet packet) {
		this.packet = (Packet53BlockChange) packet;
	}

	public int getBlockX() {
		return packet.a;
	}

	public int getBlockY() {
		return packet.b;
	}

	public int getBlockZ() {
		return packet.c;
	}

	public int getNewMaterial() {
		return packet.material;
	}

}
