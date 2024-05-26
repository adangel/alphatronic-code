package org.adangel.alphatronic.serialclock;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import com.fazecast.jSerialComm.SerialPort;

public class JavaSerialClock2 {
    public static void main(String[] args) throws InterruptedException {
        SerialPort commPort = SerialPort.getCommPort("/dev/ttyUSB0");
        System.out.println("commPort = " + commPort + " (" + commPort.getSystemPortPath() + ")");

        //int baudrate = 4800 * 16 * 2;
        int baudrate = 4800 * 2;
        boolean set = commPort.setBaudRate(baudrate);
        System.out.println("set = " + set);
        commPort.setParity(SerialPort.NO_PARITY);
        commPort.setNumDataBits(8);
        commPort.setNumStopBits(SerialPort.ONE_STOP_BIT);
        commPort.setFlowControl(SerialPort.FLOW_CONTROL_DISABLED);
        commPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, (int) TimeUnit.SECONDS.toMillis(30), 0);
        commPort.openPort();

        System.out.println("Starting clock with " + (baudrate / 2) + "Hz");
        byte[] buffer = new byte[20];
        Arrays.fill(buffer, (byte) 0x55);
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < TimeUnit.SECONDS.toMillis(300)) {
            commPort.writeBytes(buffer, buffer.length);
        }
        System.out.println("Clock done");
    }
}
