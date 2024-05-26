# JavaSerialClock

Simple Java program using [jSerialComm](https://fazecast.github.io/jSerialComm/) to use the serial
port from within Java.

## JavaSerialClock
The program tries to communicate with the serial port of Alphatronic P2, including to generate a clock
signal.

See [Using the serial port - part 1.5 - bitbanging](https://adangel.org/2024/01/28/serial-8251A-part-1.5/)
for details.

## JavaSerialClock2
This program continuously sends "U" (0x55) to generate a clock at the serial port.

* See [Using the serial port - part 1.5 - bitbanging - followup](https://adangel.org/2024/02/25/serial-8251A-part-1.5-followup/).
* See [Using the serial port - part 2](https://adangel.org/2024/05/26/serial-8251A-part-2/).
