package com.judgingmoloch.ftdiweb.compiler;

import java.util.Scanner;

public class FTDICompiler {
    private static Scanner s;

    public static void main(String... args) {
        // Can use this class to interpret and uninterpret commands
//		interpreter();
//		uninterpreter();
    }

    public static byte[] toBytes(String x) {
        String[] s = x.split(" |\n|,");
        byte[] r = new byte[s.length];

        for (int i = 0; i < s.length; i++) {
            try {
                r[i] = (byte) Integer.parseInt(s[i].toLowerCase(), 16);
            } catch (Exception e) {
                r[i] = (byte) 0x00;
            }
        }

        return r;
    }

    /**
     * toHex: Print the hex representation of a byte
     */
    public static String toHex(byte n) {
        return String.format("0x%02x", n);
    }

    /**
     * uncompile: Given a binary command, figure out what it is in as assembly language
     */
    public static String uncompile(byte output) {
        String action;

        if ((output & 0x80) == 0) {
            action = "RD";
        } else {
            action = "WR";
        }

        switch(output & 0x7F) {
            case (0x00 | 0x00): return "CPU_ID_LO " + action;
            case (0x00 | 0x01): return "CPU_ID_HI " + action;
            case (0x40 | 0x02): return "CPU_CTL " + action;
            case (0x40 | 0x03): return "CPU_STAT " + action;
            case (0x40 | 0x04): return "MEM_CTL " + action;
            case (0x00 | 0x05): return "MEM_ADDR " + action;
            case (0x00 | 0x06): return "MEM_DATA " + action;
            case (0x00 | 0x07): return "MEM_CNT " + action;
            case (0x40 | 0x08): return "BRK0_CTL " + action;
            case (0x40 | 0x09): return "BRK0_STAT " + action;
            case (0x00 | 0x0A): return "BRK0_ADDR0 " + action;
            case (0x00 | 0x0B): return "BRK0_ADDR1 " + action;
            case (0x40 | 0x0C): return "BRK1_CTL " + action;
            case (0x40 | 0x0D): return "BRK1_STAT " + action;
            case (0x00 | 0x0E): return "BRK1_ADDR0 " + action;
            case (0x00 | 0x0F): return "BRK1_ADDR1 " + action;
            case (0x40 | 0x10): return "BRK2_CTL " + action;
            case (0x40 | 0x11): return "BRK2_STAT " + action;
            case (0x00 | 0x12): return "BRK2_ADDR0 " + action;
            case (0x00 | 0x13): return "BRK2_ADDR1 " + action;
            case (0x40 | 0x14): return "BRK3_CTL " + action;
            case (0x40 | 0x15): return "BRK3_STAT " + action;
            case (0x00 | 0x16): return "BRK3_ADDR0 " + action;
            case (0x00 | 0x17): return "BRK3_ADDR1 " + action;
            case (0x00 | 0x18): return "CPU_NR " + action;
            default:			return toHex(output);
        }
    }

    /**
     * compile: Turn a command and an action into a bit representation
     */
    public static byte compile(String command, String action) {
        byte rd_wr;

        switch (action.toUpperCase()) {
            case "RD":				rd_wr = (byte) 0x00; break;
            case "WR":				rd_wr = (byte) 0x80; break;
            default:				rd_wr = (byte) 0x00; break;
        }

        switch (command.toUpperCase()) {
            case "CPU_ID_LO":		return (byte) (rd_wr | 0x00 | 0x00);
            case "CPU_ID_HI":		return (byte) (rd_wr | 0x00 | 0x01);
            case "CPU_CTL":			return (byte) (rd_wr | 0x40 | 0x02);
            case "CPU_STAT":		return (byte) (rd_wr | 0x40 | 0x03);
            case "MEM_CTL":			return (byte) (rd_wr | 0x40 | 0x04);
            case "MEM_ADDR":		return (byte) (rd_wr | 0x00 | 0x05);
            case "MEM_DATA":		return (byte) (rd_wr | 0x00 | 0x06);
            case "MEM_CNT":			return (byte) (rd_wr | 0x00 | 0x07);
            case "BRK0_CTL":		return (byte) (rd_wr | 0x40 | 0x08);
            case "BRK0_STAT":		return (byte) (rd_wr | 0x40 | 0x09);
            case "BRK0_ADDR0":		return (byte) (rd_wr | 0x00 | 0x0A);
            case "BRK0_ADDR1":		return (byte) (rd_wr | 0x00 | 0x0B);
            case "BRK1_CTL":		return (byte) (rd_wr | 0x40 | 0x0C);
            case "BRK1_STAT":		return (byte) (rd_wr | 0x40 | 0x0D);
            case "BRK1_ADDR0":		return (byte) (rd_wr | 0x00 | 0x0E);
            case "BRK1_ADDR1":		return (byte) (rd_wr | 0x00 | 0x0F);
            case "BRK2_CTL":		return (byte) (rd_wr | 0x40 | 0x10);
            case "BRK2_STAT":		return (byte) (rd_wr | 0x40 | 0x11);
            case "BRK2_ADDR0":		return (byte) (rd_wr | 0x00 | 0x12);
            case "BRK2_ADDR1":		return (byte) (rd_wr | 0x00 | 0x13);
            case "BRK3_CTL":		return (byte) (rd_wr | 0x40 | 0x14);
            case "BRK3_STAT":		return (byte) (rd_wr | 0x40 | 0x15);
            case "BRK3_ADDR0":		return (byte) (rd_wr | 0x00 | 0x16);
            case "BRK3_ADDR1":		return (byte) (rd_wr | 0x00 | 0x17);
            case "CPU_NR":			return (byte) (rd_wr | 0x00 | 0x18);
            default:				return (0x00);
        }
    }

    public static final String[] ADDRESSES = {
            "CPU_ID_LO",	"CPU_ID_HI",	"CPU_CTL",
            "CPU_STAT",		"MEM_CTL",		"MEM_ADDR",
            "MEM_DATA",		"MEM_CNT",		"BRK0_CTL",
            "BRK0_STAT",	"BRK0_ADDR0",	"BRK0_ADDR1",
            "BRK1_CTL",		"BRK1_STAT",	"BRK1_ADDR0",
            "BRK1_ADDR1",	"BRK2_CTL",		"BRK2_STAT",
            "BRK2_ADDR0",	"BRK2_ADDR1",	"BRK3_CTL",
            "BRK3_STAT",	"BRK3_ADDR0",	"BRK3_ADDR1",
            "CPU_NR",
    };

	/* ABOUT
	 * First bit (i.e. 0x80 or 0b10000000): Set to write, otherwise read
	 * Second bit (i.e. 0x40 or 0b01000000): Set for 8 bit, otherwise 16 bit
	 * Rest: Address to particular register
	 */

    /**
     * Uncompiles string
     */
    public static String uncompile(String input) {
        String[] toRead = input.split(" |\n");
        String r = "";
        int n;
        for (String s : toRead) {
            try {
                n = Integer.parseInt(s, 16);
            } catch (Exception e) {
                r += s + "\n";
                continue;
            }
            r += uncompile((byte) n) + "\n";
        }
        return r;
    }
}
