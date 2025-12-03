
package utils;

import java.io.File;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;

/**
 * Simple facture PDF generator using PDFBox.
 */
public class PDFUtils {
    public static File generateInvoice(String client, String content, String filename) {
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage();
            doc.addPage(page);
            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                cs.beginText();
                // setFont is omitted to avoid compile errors with different PDFBox versions;
                // if your PDFBox provides PDType1Font.HELVETICA_BOLD use:
                //     cs.setFont(PDType1Font.HELVETICA_BOLD, 18);
                // or load an explicit font via PDType0Font.load(doc, fontFile)
                cs.newLineAtOffset(50, 700);
                cs.showText("Facture PharmaPays");
                cs.endText();

                cs.beginText();
                // cs.setFont(PDType1Font.HELVETICA, 12);
                cs.newLineAtOffset(50, 650);
                cs.showText("Client: " + client);
                cs.newLineAtOffset(0, -20);
                cs.showText(content);
                cs.endText();
            }
            File out = new File(filename);
            doc.save(out);
            return out;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
