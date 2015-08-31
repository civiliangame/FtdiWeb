package com.judgingmoloch.ftdiweb.driver;

import com.judgingmoloch.ftdiweb.compiler.FTDICompiler;
import com.judgingmoloch.ftdiweb.utils.Utils;

public abstract class GenericDriver {

    // Need to override these two methods to use this class
    // The idea is that regardless of the driver being used to connect to the openMSP430, it
    // can still be controlled normally
    protected abstract boolean write(byte... b);
    protected abstract byte[] read(int length);

    public boolean write(int... data) {
        byte[] b = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            b[i] = (byte) data[i];
        }
        return write(b);
    }

    public void printAllRegisters() {
        for (String reg : FTDICompiler.ADDRESSES) {
            System.out.println(reg + ": " + Utils.join(readRegister(reg)));
        }
    }

    public byte[] readRegister(int registerNumber) {
        writeRegister("MEM_CNT", 0x0000);
        writeRegister("MEM_ADDR", registerNumber);
        writeRegister("MEM_CTL", 0x0005);
        return readRegister("MEM_DATA");
    }

    public byte[] readRegister(String registerName) {
        byte cmd = FTDICompiler.compile(registerName, "RD");
        this.write(cmd);

        if ((cmd & 0x40) == 0) { // 16 bit
            return this.read(2);
        } else { // 8 bit
            return this.read(1);
        }
    }

    public boolean writeRegister(int registerNumber, byte... data) {
        writeRegister("MEM_ADDR", registerNumber);
        writeRegister("MEM_DATA", data);
        return writeRegister("MEM_CTL", 0x07);
    }

    public boolean writeRegister(String registerName, int... data) {
        return writeRegister(registerName, Utils.toByte(data));
    }

    public boolean writeRegister(String registerName, byte... data) {
        byte cmd = FTDICompiler.compile(registerName, "WR");
        this.write(cmd);

        if ((cmd & 0x40) == 0) { // 16 bit
            if (data.length > 1) {
                return this.write(data[0]) && this.write(data[1]);
            } else {
                return this.write(data[0]) && this.write((byte) 0x00);
            }
        } else { // 8 bit
            return this.write(data[0]);
        }
    }

    public boolean writeSingle(int startAddress, int data) {
        return writeSingle(startAddress, (char) data);
    }

    public boolean writeSingle(int startAddress, char data) {
        return writeSingle(startAddress, Utils.toByte(data));
    }

    public boolean writeSingle(int startAddress, byte... data) {
        writeRegister("MEM_ADDR", startAddress);			// Put register to write to in MEM_ADDR

        if (data.length == 1) {
            writeRegister("MEM_DATA", data[0]);				// Put data to write in MEM_DATA
            return writeRegister("MEM_CTL", 0x0b);			// Initiate write command
        } else {
            writeRegister("MEM_DATA", data[0], data[1]);	// Put data to write in MEM_DATA
            return writeRegister("MEM_CTL", 0x03);			// Initiate write command
        }
    }

    public boolean writeBurst(int startAddress, int... data) {
        return writeBurst(startAddress, Utils.toByte(data));
    }

    public boolean writeBurst(int startAddress, char... data) {
        return writeBurst(startAddress, Utils.toByte(data));
    }

    public boolean writeBurst(int startAddress, byte... data) {
        // Make sure our data consists of 16 bit units only
        if (data.length % 2 != 0) {
            byte[] n_data = new byte[data.length - 1];
            for (int i = 0; i < data.length - 1; i++) {
                n_data[i] = data[i];
            }
            data = n_data;
        }
        writeRegister("MEM_CNT", data.length / 2 - 1);
        writeRegister("MEM_ADDR", startAddress);
        writeRegister("MEM_CTL", 0x03);
        return this.write(data);
    }

    public byte[] readSingle(int startAddress, boolean memAccess) {
        writeRegister("MEM_CNT", 0x00, 0x00);
        writeRegister("MEM_ADDR", startAddress);	// Put register to read from in MEM_ADDR
        if (memAccess) {
            writeRegister("MEM_CTL", 0x05);
        } else {
            writeRegister("MEM_CTL", 0x01);				// Initiate read command
        }
        return readRegister("MEM_DATA");
    }

    public byte[] readBurst(int startAddress, int length) {
        writeRegister("MEM_CNT", length - 1);		// This sets burst mode
        writeRegister("MEM_ADDR", startAddress);	// Write start address to MEM_ADDR
        writeRegister("MEM_CTL", 0x01);				// Initiate read command
        return this.read(length * 2);				// Read the data
    }

	/* -----------------------------------
	 * The code below this line corresponds to dbg_functions.tcl
	 * ----------------------------------- */

    public boolean getDevice() {
        return writeRegister("CPU_CTL", 0x18); // Enable auto-freeze and software breakpoints
    }

    public boolean releaseDevice(int addr) {
        int r = 0;
        if (addr == 0xfffe) {
            r++;
            if (executePor()) r++;
            if (releaseCpu()) r++;
        } else {
            if (haltCpu()) r++;
            if (setPc(addr)) r++;
            if (releaseCpu()) r++;
        }

        return r == 3;
    }

    public boolean releaseDeviceCadsp() {
        return releaseDevice(getPc());
    }

    public boolean executePor() {
        int cpuCtlOrg = Utils.toInt(readRegister("CPU_CTL"));

        // Set PUC
        writeRegister("CPU_CTL", cpuCtlOrg | 0x40);

        // Remove PUC, clear break after reset
        cpuCtlOrg &= 0x5f;
        writeRegister("CPU_CTL", cpuCtlOrg);

        // Check status: Make sure a PUC occured
        int cpuStatVal = Utils.toInt(readRegister("CPU_STAT"));
        if ((0x04 & cpuStatVal) != 4) return false;

        // Clear PUC pending flag
        writeRegister("CPU_STAT", 0x04);

        return true;
    }

    // TODO Make sure this works
    public boolean setPc(int addr) {
        return writeRegister(0, Utils.toByte(addr));
    }

    public int getPc() {
        return Utils.toInt(readRegister(0));
    }

    public boolean haltCpu() {
        int cpuCtlOrg = Utils.toInt(readRegister("CPU_CTL"));

        // Stop CPU
        writeRegister("CPU_CTL", 0x01 | cpuCtlOrg);

        // Check status: Make sure the CPU halted
        int cpuStatVal = Utils.toInt(readRegister("CPU_STAT"));
        return (0x01 & cpuStatVal) == 4;
    }

    public boolean releaseCpu() {
        int cpuCtlOrg = Utils.toInt(readRegister("CPU_CTL"));

        // Start CPU
        writeRegister("CPU_CTL", 0x02 | cpuCtlOrg);

        // Check status: Make sure the CPU runs
        int cpuStatVal = Utils.toInt(readRegister("CPU_STAT"));
        return (0x01 & cpuStatVal) == 0;
    }

    public boolean verifyMemory(int startAddress, byte[] data) {
        byte[] readData = readBurst(startAddress, data.length / 2);
        if (readData.length != data.length) return false;
        for (int i = 0; i < readData.length; i++) {
            if (readData[i] != data[i]) return false;
        }
        return true;
    }

    public boolean executePorHalt() {
        int cpuCtlOrg = Utils.toInt(readRegister("CPU_CTL"));

        // Perform PUC
        writeRegister("CPU_CTL", 0x60 | cpuCtlOrg);
        try { Thread.sleep(100); } catch (InterruptedException e) { e.printStackTrace(); }
        writeRegister("CPU_CTL", cpuCtlOrg);

        // Check status: Make sure a PUC occurred and that the CPU is halted
        int cpuStatVal = Utils.toInt(readRegister("CPU_STAT"));
        if ((0x05 & cpuStatVal) != 5) return false;

        // Clear PUC pending flag
        writeRegister("CPU_STAT", 0x04);

        return true;
    }

    public int getCpuId() {
        int cpuIdLo = Utils.toInt(readRegister("CPU_ID_LO"));
        int cpuIdHi = Utils.toInt(readRegister("CPU_ID_HI"));
        int cpuNr = Utils.toInt(readRegister("CPU_NR"));

        return (cpuIdHi << 8) + cpuIdLo;
    }

    public boolean verifyCpuId() {
        return getCpuId() != 0;
    }

    public boolean stepCpu() {
        int cpuCtlOrg = Utils.toInt(readRegister("CPU_CTL"));
        writeRegister("CPU_CTL", cpuCtlOrg | 0x04);
        return true;
    }

    public int initBreakUnits() {
        int numBrkUnits = 0;
        for (int i = 0; i < 4; i++) {
            String regName = "BRK" + i + "_ADDR0";
            writeRegister(regName, 0x1234);
            int newVal = Utils.toInt(readRegister(regName));
            if (newVal == 0x1234) {
                numBrkUnits++;
                writeRegister("BRK" + i + "_CTL", 0x00);
                writeRegister("BRK" + i + "_STAT", 0xff);
                writeRegister("BRK" + i + "_ADDR0", 0x0000);
                writeRegister("BRK" + i + "_ADDR1", 0x0000);
            }
        }
        return numBrkUnits;
    }

    // TODO SetHWBreak
    // TODO ClearHWBreak

    public boolean isHalted() {
        int cpuStatVal = Utils.toInt(readRegister("CPU_STAT"));
        return cpuStatVal != 0;
    }

    public boolean clearStatus() {
        return writeRegister("CPU_STAT", 0xff) &
                writeRegister("BRK0_STAT", 0xff) &
                writeRegister("BRK1_STAT", 0xff) &
                writeRegister("BRK2_STAT", 0xff) &
                writeRegister("BRK3_STAT", 0xff);
    }

	/* -----------------------------------
	 * End of code corresponding to dbg_functions.tcl
	 * ----------------------------------- */

    // Generic exception type for alerting the user that something went wrong with the connection
    class DeviceConnectionError extends Exception {
        private static final long serialVersionUID = 1L;

        public DeviceConnectionError() { }
        public DeviceConnectionError(String message) { super(message); }
        public DeviceConnectionError(Throwable cause) { super(cause); }
        public DeviceConnectionError(String message, Throwable cause) { super(message, cause); }
    }

	/* Notes:
	 * The amount of "Program Memory" is stored in the register CPU_ID_HI
	 * The amount of "Data Memory" is stored in the register CPU_ID_LO
	 * I don't know if these are actually true... That's just what the Tcl scripts seemed to suggest
	 */
}
