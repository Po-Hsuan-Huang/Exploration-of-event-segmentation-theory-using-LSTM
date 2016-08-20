package de.jannlab.core;

import de.jannlab.Net;

public class BadHack {
	
	public static double[] getInputBuffer(final Net net, final int frameidx) {
		NetBase base = (NetBase)net;
		return base.data.input[frameidx];
	}
	
	
}