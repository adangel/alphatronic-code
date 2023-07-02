package org.adangel.alphatronic.serialclock;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.fazecast.jSerialComm.SerialPort;

public class JavaSerialClock {
    public static void main(String[] args) throws InterruptedException {
        SerialPort commPort = SerialPort.getCommPort("/dev/ttyUSB0");
        System.out.println("commPort = " + commPort + " (" + commPort.getSystemPortPath() + ")");

        // actually wanted 8n1@4800, but 4800 baud is too fast for java bit banging
        // staying at 75 baud seems to be working ok.
        int baudrate = 75;
        commPort.setBaudRate(baudrate);
        commPort.setParity(SerialPort.NO_PARITY);
        commPort.setNumDataBits(8);
        commPort.setNumStopBits(SerialPort.ONE_STOP_BIT);
        commPort.setFlowControl(SerialPort.FLOW_CONTROL_DISABLED);
        commPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, (int) TimeUnit.SECONDS.toMillis(30), 0);
        commPort.clearDTR();
        commPort.openPort();

        double frequency = baudrate * 16;
        int halfPeriodNanos = (int) (0.5 * 1.0 / frequency * TimeUnit.SECONDS.toNanos(1));
        System.out.println("halfPeriodNanos = " + halfPeriodNanos);

        Runnable clock = () -> {
            long start = System.currentTimeMillis();
            System.out.println("Toggling DTR...");
            while (System.currentTimeMillis() - start < TimeUnit.SECONDS.toMillis(100)) {
                commPort.setDTR();
                busyWait(halfPeriodNanos);
                commPort.clearDTR();
                busyWait(halfPeriodNanos);
            }
            System.out.println("Done");
        };

        Runnable sender = () -> {
            int bitLengthNanos = (int) (1.0 / baudrate * TimeUnit.SECONDS.toNanos(1));
            System.out.println("bitLengthNanos = " + bitLengthNanos);
            commPort.clearDTR(); // no data
            busyWait(TimeUnit.MILLISECONDS.toNanos(500));
            System.out.println("Sending...");
            sendString(commPort, "**01234567", bitLengthNanos);
            System.out.println("Done sending");
        };

        Thread clockThread = new Thread(clock);
        clockThread.start();
        // Thread senderThread = new Thread(sender);
        // senderThread.start();

        byte[] buffer = new byte[10];
        int readBytes = commPort.readBytes(buffer, buffer.length);

        System.out.println("readBytes = " + readBytes);
        String hexString = IntStream.range(0, buffer.length).map(i -> buffer[i] & 0xff)
                .boxed()
                .map(Integer::toHexString)
                .map(s -> "0x" + s)
                .collect(Collectors.joining(", "));
        System.out.println("hexString = " + hexString);
        System.out.println("buffer = \"" + new String(buffer, StandardCharsets.ISO_8859_1) + "\"");

        commPort.closePort();
    }

    private static void sendString(SerialPort commPort, String s, long bitLengthNanos) {
        for (int i = 0; i < s.length(); i++) {
            sendByte(commPort, s.charAt(i), bitLengthNanos);
        }
    }

    private static void sendByte(SerialPort commPort, int c, long bitLengthNanos) {
        // Send start bit
        sendBit(commPort, false, bitLengthNanos);
        for (int i = 0; i < 8; i++) {
            int mask = 1 << i;
            sendBit(commPort, (c & mask) == mask, bitLengthNanos);
        }
        // Send stop bit
        sendBit(commPort, true, bitLengthNanos); // stop bit

        // Idle
        commPort.clearDTR(); // no data
        busyWait(TimeUnit.MILLISECONDS.toNanos(500));
    }

    private static void sendBit(SerialPort commPort, boolean bit, long bitLengthNanos) {
        if (!bit) {
            commPort.setDTR();
        } else {
            commPort.clearDTR();
        }
        busyWait(bitLengthNanos);
    }

    private static void busyWait(long nanos) {
        long start = System.nanoTime();
        start -= 0.2*nanos; // remove overhead
        long end = 0;
        while (end - start < nanos) {
            end = System.nanoTime();
        }
    }
}
