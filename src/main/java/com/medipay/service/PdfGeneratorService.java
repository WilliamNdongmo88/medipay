package com.medipay.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.medipay.entity.Transaction;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Date;

@Service
public class PdfGeneratorService {

    public byte[] generateUserTransactionsPdf(List<Transaction> transactions) throws DocumentException{
        // Utilisation d'un bloc try-catch-finally pour garantir la fermeture des ressources
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4.rotate());

        try {
            PdfWriter writer = PdfWriter.getInstance(document, out);

            document.open();

            // Titre
            Font fontHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Paragraph title = new Paragraph("Rapport des Transactions Utilisateurs", fontHeader);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // Date
            Font dateFont = new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            Paragraph date = new Paragraph("Généré le : " + sdf.format(new Date()), dateFont);
            date.setAlignment(Element.ALIGN_CENTER);
            document.add(date);

            document.add(new Paragraph("\n"));

            // Infos
            Font infoFont = new Font(Font.FontFamily.HELVETICA, 11);
            Paragraph info = new Paragraph("Nombre total de transactions : " + transactions.size(), infoFont);
            document.add(info);

            document.add(new Paragraph("\n"));

            // Table
            PdfPTable table = new PdfPTable(8);
            table.setWidthPercentage(100);
            addTableHeader(table);

            for (Transaction transaction : transactions) {
                table.addCell(safe(transaction.getId()));
                table.addCell(transaction.getSenderWallet() != null ? safe(transaction.getSenderWallet().getId()) : "N/A");
                table.addCell(transaction.getReceiverWallet()!= null ? safe(transaction.getReceiverWallet().getId()) : "N/A");
                table.addCell(safe(transaction.getAmount()));
                table.addCell(safe(transaction.getType()));
                table.addCell(safe(transaction.getStatus()));
                table.addCell(safe(transaction.getDescription()));
                table.addCell(transaction.getTimestamp() != null
                        ? transaction.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                        : "N/A");
            }

            document.add(table);

            // 1. On ferme le document d'abord (écrit le trailer PDF)
            document.close();

            // 2. On s'assure que le writer a fini de vider son buffer vers le flux de sortie
            writer.close();

            // 3. On récupère les octets
            byte[] result = out.toByteArray();

            // DEBUG : Vérifier si le tableau d'octets commence bien par le header PDF "%PDF-"
            if (result.length > 0 && result[0] == 0x25 && result[1] == 0x50 && result[2] == 0x44 && result[3] == 0x46) {
                System.out.println("✅ PDF généré avec succès (Header valide)");
            } else {
                System.err.println("⚠️ Attention : Le PDF généré semble invalide ou vide");
            }

            return result;

        } catch (Exception e) {
            if (document.isOpen()) {
                document.close();
            }
            throw new RuntimeException("Erreur lors de la génération du PDF", e);
        }
    }

    private String safe(Object value) {
        return value != null ? String.valueOf(value) : "N/A";
    }

    private void addTableHeader(PdfPTable table) {
        String[] headers = {"ID", "Sender Wallet ID", "Receiver Wallet ID", "Amount", "Type", "Status", "Description", "Date Transaction"};
        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.WHITE);

        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, font));
            cell.setBackgroundColor(BaseColor.DARK_GRAY);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setPadding(5);
            table.addCell(cell);
        }
    }
}
