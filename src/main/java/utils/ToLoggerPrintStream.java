package utils;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class ToLoggerPrintStream {
    /**
     * Logger for this class
     */
    private PrintStream myPrintStream;
    private ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    /**
     * @return printStream
     */
    public PrintStream getPrintStream() {
        if (myPrintStream == null) {
            OutputStream output = new OutputStream() {

                @Override
                public void write(int b) {
                    buffer.write(b);
                }

                /**
                 * @see java.io.OutputStream#flush()
                 */
                @Override
                public void flush() {
                    //User for clean up request/response logging
                    List<String> blacklist = Arrays.asList("Content-Length", "Date", "Server", "Proxy", "charset=UTF-8", "Cookies", "Multiparts", "Form", "Path", "Query", "Request params");
                    try {
                        Arrays.stream(buffer.toString("UTF-8").split("\n"))
                                .filter(str -> blacklist.stream().noneMatch(str::startsWith)

                                        & !str.isEmpty())
                                .forEach(e -> System.out.println("[" + new SimpleDateFormat("dd.MM.yyyy_HH:mm:ss,SSS").format(Calendar.getInstance().getTime()) + "] - " + e));
                    } catch (UnsupportedEncodingException e) {
                        throw new UnsupportedOperationException();
                    }
                    buffer.reset();
                    System.gc();
                }
            };
            myPrintStream = new PrintStream(output, true);  // true: autoflush must be set!
        }
        return myPrintStream;
    }

    public ToLoggerPrintStream() {
        super();
    }
}
