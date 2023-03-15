package core.september.util;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.vandeseer.easytable.TableDrawer;
import org.vandeseer.easytable.structure.Row;
import org.vandeseer.easytable.structure.Table;
import org.vandeseer.easytable.structure.cell.TextCell;

import javax.swing.table.TableModel;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.List;

import static org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA;
import static org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA_BOLD;
import static org.vandeseer.easytable.settings.HorizontalAlignment.*;

public class PdfHandler implements Runnable{

    private  final float PADDING;
    private final TableModel model;
    private final String[] columns;

    private final String destName;


    public PdfHandler(String destName, TableModel model, String[] columns) {
        this.model = model;
        this.columns = columns;
        this.destName = destName;

        this.PADDING = getPadding();
    }
    @Override
    public void run() {

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        PDDocument doc = new PDDocument();
        Table dataTable = createTable();

        String fileName = String.format("statoFattureExport%s.pdf",sdf.format(new Date()));
        try {
            createAndSaveDocumentWithTables(doc,fileName,dataTable);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private float getPadding() {
        double fullColsize = Arrays.asList(ArrayUtils.toObject(colsSizes(columns))).stream().mapToDouble(Float::doubleValue).sum();
        return (PDRectangle.A4.getWidth() - (float) fullColsize) / 2;
    }

    private  void createAndSaveDocumentWithTables(PDDocument document, String outputFileName, Table... tables) throws IOException {

        final PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        float startY = page.getMediaBox().getHeight() - PADDING;

        try (final PDPageContentStream contentStream = new PDPageContentStream(document, page)) {

            for (final Table table : tables) {

                TableDrawer.builder()
                        .page(page)
                        .contentStream(contentStream)
                        .table(table)
                        .startX(PADDING)
                        .startY(startY)
                        .endY(PADDING)
                        .build()
                        .draw(() -> document, () -> new PDPage(PDRectangle.A4), PADDING);

                startY -= (table.getHeight() + PADDING);
            }

        }

        document.save(destName + "/" + outputFileName);
        document.close();
        Desktop.getDesktop().open(new File(destName + "/" + outputFileName));

    }

    private Row.RowBuilder addCols(Row.RowBuilder builder, String[] cols) {
        Row.RowBuilder ret = null;
        for(int i = 0; i < cols.length; i++) {
            ret = builder.add(TextCell.builder().text(cols[i]).borderWidth(1).build());
        }
        return ret;
    }

    private float[] colsSizes(String[] cols) {
        float[] sizes = new float[cols.length];
        for(int i = 0; i < cols.length; i++) {
            sizes[i] = i == 0 ? 50f : i == cols.length -1 ? 200f : 100f;
        }
        return sizes;
    }

    private Table createTable() {
        final Table.TableBuilder tableBuilder = Table.builder()
                .addColumnsOfWidth(colsSizes(columns))
                .fontSize(8)
                .font(HELVETICA)
                .borderColor(Color.WHITE);


        tableBuilder.addRow(addCols(Row.builder(),columns)
                .backgroundColor(Color.BLUE)
                .textColor(Color.WHITE)
                .font(HELVETICA_BOLD)
                .fontSize(9)
                .horizontalAlignment(CENTER)
                .build());


        IntStream.range(0, model.getRowCount()).forEach(row -> {
            List<String> dataRow = IntStream.range(0, model.getColumnCount())
                    .mapToObj(column -> model.getValueAt(row,column).toString()).collect(Collectors.toList());

            String[] rowString = dataRow.toArray(new String[dataRow.size()]);
            tableBuilder.addRow(addCols(Row.builder(),rowString)
                    .backgroundColor(row == model.getRowCount() -1 ? Color.YELLOW : row % 2 == 0 ? Color.LIGHT_GRAY : Color.WHITE)
                    .horizontalAlignment(RIGHT)
                    .build());
        });


        return tableBuilder.build();

    }
}
