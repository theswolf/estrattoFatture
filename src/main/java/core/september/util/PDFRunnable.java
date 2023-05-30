package core.september.util;

import lombok.Getter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import javax.swing.*;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PDFRunnable implements Runnable{
    private final JTable table;
    private final File file;
    AtomicInteger counter = new AtomicInteger(0);



    public PDFRunnable(File file,JTable table) {
        super();
        this.file = file;
        this.table = table;
    }

    private void process(List<PDFData> chunks) {
        PDFData.resetCounter();
        String[][] data;
        data = new String[chunks.size()][];
        chunks
                .stream()
                .sorted(Comparator.comparing(PDFData::getCliente))
                .sorted(Comparator.comparing(PDFData::getDate))
                .forEach(ch -> {
            data[counter.getAndIncrement()] = ch.toArray();
        });

        table.setModel(new DefaultTableModel(data,PDFData.columns));
    }



    @Override
    public void run() {

        List<File> fileList = Arrays.asList(file.listFiles())
                .stream()
                .filter(f -> f.getName().endsWith("pdf")).collect(Collectors.toList());

        List<PDFData> pdfDataList = fileList.stream()
                .map(file -> {
                    PDDocument document = null;
                    try {
                        document = PDDocument.load(file);
                        PDFTextStripper stripper = new PDFTextStripper();
                        String text = stripper.getText(document);
                        document.close();

                        String regex = "^(((0[1-9])|([12][0-9])|(3[01]))\\.((0[0-9])|(1[012]))\\.((20[012]\\d|19\\d\\d)|(1\\d|2[0123])))";
                        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);



                        List<String>  fullText = Arrays.asList(text.split("\\n"));
                        AtomicReference<String> cliente = new AtomicReference();
                        AtomicReference<Date> data = new AtomicReference();
                        AtomicReference<Number> totale = new AtomicReference();
                        AtomicReference<Number> importo = new AtomicReference();

                        fullText.stream().forEach(s -> {
                            if(s.startsWith("Cessionario")) {
                                cliente.set(fullText.get(fullText.indexOf(s) + 1));
                            }

                            if(pattern.matcher(s).find()) {
                                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
                                try {
                                    data.set(sdf.parse(s));
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }

                            if(s.startsWith("TOTALE")) {
                                try {
                                    totale.set(NumberFormat.getNumberInstance(Locale.ITALIAN).parse(s.split(" ")[1]));
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }

                            if(s.startsWith("22,00")) {
                                try {
                                    importo.set(NumberFormat.getNumberInstance(Locale.ITALIAN).parse(s.split(" ")[1]));
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                        PDFData pdfData = new PDFData(totale.get().doubleValue(),
                                importo.get().doubleValue(),
                                data.get(),cliente.get().split("- C")[0]);

                        System.out.println("----------------------------------");
                        System.out.println(pdfData);
                        //System.out.println(text);
                        System.out.println("----------------------------------");
                        return pdfData;
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                }).collect(Collectors.toList());

        Double totalAmount = pdfDataList.stream().mapToDouble(PDFData::getAmount).sum();
        Double totalImporto = pdfDataList.stream().mapToDouble(PDFData::getImporto).sum();

        PDFData totali = new PDFData(totalAmount,totalImporto,new Date(),"TOTALE");

        pdfDataList.add(totali);

        process(pdfDataList);

    }
}
