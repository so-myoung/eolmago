package kr.eolmago.service.deal;

import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import kr.eolmago.domain.entity.deal.Deal;
import kr.eolmago.repository.deal.DealRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Deal PDF 생성 Service
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DealPdfService {

    private final DealRepository dealRepository;

    private static final DateTimeFormatter DATE_FORMAT = 
        DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH시 mm분");
    
    private static final NumberFormat CURRENCY_FORMAT = 
        NumberFormat.getCurrencyInstance(Locale.KOREA);

    /**
     * 거래확정서 PDF 생성
     */
    public byte[] generateDealConfirmationPdf(Long dealId) {
        Deal deal = dealRepository.findById(dealId)
                .orElseThrow(() -> new IllegalArgumentException("거래를 찾을 수 없습니다"));

        if (!deal.getStatus().name().equals("COMPLETED")) {
            throw new IllegalStateException("완료된 거래만 PDF를 생성할 수 있습니다");
        }

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // 한글 폰트 설정 (기본 폰트 사용)
            PdfFont font = PdfFontFactory.createFont("Helvetica");

            // 제목
            Paragraph title = new Paragraph("Deal Confirmation Document")
                    .setFont(font)
                    .setFontSize(20)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(title);

            document.add(new Paragraph("\n"));

            // 거래 정보 테이블
            Table table = new Table(UnitValue.createPercentArray(new float[]{30, 70}))
                    .useAllAvailableWidth();

            addTableRow(table, font, "Deal ID", String.valueOf(deal.getDealId()));
            addTableRow(table, font, "Auction", deal.getAuction().getTitle());
            addTableRow(table, font, "Seller ID", deal.getSeller().getUserId().toString());
            addTableRow(table, font, "Buyer ID", deal.getBuyer().getUserId().toString());
            addTableRow(table, font, "Final Price", CURRENCY_FORMAT.format(deal.getFinalPrice()));
            addTableRow(table, font, "Status", deal.getStatus().name());
            
            if (deal.getSellerConfirmedAt() != null) {
                addTableRow(table, font, "Seller Confirmed At", 
                    deal.getSellerConfirmedAt().plusHours(9).format(DATE_FORMAT));
            }
            
            if (deal.getBuyerConfirmedAt() != null) {
                addTableRow(table, font, "Buyer Confirmed At", 
                    deal.getBuyerConfirmedAt().plusHours(9).format(DATE_FORMAT));
            }
            
            if (deal.getConfirmedAt() != null) {
                addTableRow(table, font, "Confirmed At", 
                    deal.getConfirmedAt().plusHours(9).format(DATE_FORMAT));
            }
            
            if (deal.getCompletedAt() != null) {
                addTableRow(table, font, "Completed At", 
                    deal.getCompletedAt().plusHours(9).format(DATE_FORMAT));
            }
            
            if (deal.getShippingNumber() != null) {
                addTableRow(table, font, "Shipping Number", deal.getShippingNumber());
            }
            
            if (deal.getShippingCarrierCode() != null) {
                addTableRow(table, font, "Carrier Code", deal.getShippingCarrierCode());
            }

            addTableRow(table, font, "Created At", 
                deal.getCreatedAt().plusHours(9).format(DATE_FORMAT));

            document.add(table);

            // 하단 서명란
            document.add(new Paragraph("\n\n"));
            document.add(new Paragraph("This document certifies that the deal has been completed successfully.")
                    .setFont(font)
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph("\n"));
            document.add(new Paragraph("Generated at: " + 
                java.time.OffsetDateTime.now().plusHours(9).format(DATE_FORMAT))
                    .setFont(font)
                    .setFontSize(8)
                    .setTextAlignment(TextAlignment.RIGHT));

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("PDF 생성 중 오류가 발생했습니다", e);
        }
    }

    private void addTableRow(Table table, PdfFont font, String key, String value) {
        table.addCell(new Paragraph(key).setFont(font).setBold());
        table.addCell(new Paragraph(value).setFont(font));
    }
}
