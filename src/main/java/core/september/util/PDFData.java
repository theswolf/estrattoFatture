package core.september.util;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

@Data
@AllArgsConstructor
public class PDFData {

    private static final AtomicInteger counter = new AtomicInteger(1);

    public static void resetCounter() {
        counter.set(1);
    }

    public static final String[] columns = new String[]{
            "PROG",
            "TOTALE",
            "IMPORTO",
            "DATA",
            "CLIENTE"
    };

    public static String[][] emptySet(String[] columns) {
        String[][] res = new String[1][columns.length];
        return res;
    }

    private Double amount;
    private Double importo;
    private Date date;

    private String cliente;

    public String[] toArray() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        return new String[] {
                String.valueOf(counter.getAndIncrement()),
                String.format(Locale.ITALIAN, "%,.2f", amount),
                String.format(Locale.ITALIAN, "%,.2f", importo),
                sdf.format(date),
                cliente
        };
    }


}
