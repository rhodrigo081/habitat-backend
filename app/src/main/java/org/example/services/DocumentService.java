package org.example.services;

import com.itextpdf.text.*;
import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.*;
import org.example.dtos.request.GenerateDocumentRequest;
import org.example.enums.DocumentFormat;
import org.example.enums.DocumentType;
import org.example.enums.UserRole;
import org.example.exceptions.InvalidArgumentException;
import org.example.exceptions.InvalidCredentialsExceptions;
import org.example.models.Associate;
import org.example.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class DocumentService {

    @Autowired
    private AssociateService associateService;
    @Autowired
    private UserService userService;
    @Autowired
    private static DateTimeFormatter FORMATTER_BR =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public byte[] generate(GenerateDocumentRequest request, User requester) {
        Associate associate = associateService.findAssociateById(request.associateId());

        User coordinator = coordinatorResolve(request, requester, associate);

        try {
            return request.format() == DocumentFormat.PDF
                    ? generatePdf(request.type(), associate, coordinator)
                    : generateDocx(request.type(), associate, coordinator);
        } catch (Exception e) {
            log.error("Erro ao gerar documento: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao gerar documento: " + e.getMessage());
        }
    }


    private byte[] generatePdf(DocumentType type, Associate associate, User coordinator)
            throws Exception {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 72, 72, 72, 72);
        PdfWriter.getInstance(document, byteArrayOutputStream);
        document.open();

        Font titleFont = new Font(Font.FontFamily.TIMES_ROMAN, 14, Font.BOLD);
        Font bodyFont = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.NORMAL);
        Font boldFont = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD);

        switch (type) {
            case POWER_OF_ATTORNEY ->
                    addPowerOfAttorneyPdf(document, associate, coordinator, titleFont, bodyFont, boldFont);
            case DECLARATION_OF_INSUFFICIENCY_OF_RESOURCES ->
                    addDeclarationPdf(document, associate, titleFont, bodyFont, boldFont);
            default -> throw new InvalidArgumentException("Tipo de documento inválido!");
        }


        document.close();
        return byteArrayOutputStream.toByteArray();
    }

    private void addPowerOfAttorneyPdf(Document doc, Associate associate, User coordinator,
                                       Font title, Font normal, Font bold) throws DocumentException {
        String today = LocalDate.now().format(FORMATTER_BR);

        doc.add(new Paragraph("PROCURAÇÃO AD JUDICIA ET EXTRA", title) {{
            setAlignment(Element.ALIGN_CENTER);
            setSpacingAfter(20);
        }});

        doc.add(new Paragraph(
                associate.getName().toUpperCase() + ", portador(a) do CPF nº " + associate.getCpf() +
                        ", residente e domiciliado(a) em " + associate.getAddress() +
                        ", pelo presente instrumento particular, nameia e constitui como seu(sua) bastante " +
                        "procurador(a) o(a) Dr(a). " + coordinator.getName().toUpperCase() +
                        ", advogado(a) devidamente inscrito(a) na Ordem dos Advogados do Brasil, " +
                        "a quem confere amplos poderes para representá-lo(a) em Juízo ou fora dele, " +
                        "podendo praticar todos os atos necessários ao fiel desempenho deste mandato, " +
                        "incluindo receber citações, confessar, desistir, transigir, firmar compromissos " +
                        "e substabelecer com ou sem reservas.",
                normal) {{
            setAlignment(Element.ALIGN_JUSTIFIED);
            setSpacingAfter(30);
        }});

        doc.add(new Paragraph(
                associate.getAddress() + ", " + today + ".", normal) {{
            setAlignment(Element.ALIGN_RIGHT);
            setSpacingAfter(50);
        }});

        doc.add(new Paragraph("_______________________________________", normal) {{
            setAlignment(Element.ALIGN_CENTER);
        }});
        doc.add(new Paragraph(associate.getName().toUpperCase(), bold) {{
            setAlignment(Element.ALIGN_CENTER);
        }});
        doc.add(new Paragraph("CPF: " + associate.getCpf(), normal) {{
            setAlignment(Element.ALIGN_CENTER);
        }});
    }

    private void addDeclarationPdf(Document doc, Associate associate,
                                   Font title, Font normal, Font bold) throws DocumentException {
        String today = LocalDate.now().format(FORMATTER_BR);

        doc.add(new Paragraph("DECLARAÇÃO DE HIPOSSUFICIÊNCIA", title) {{
            setAlignment(Element.ALIGN_CENTER);
            setSpacingAfter(20);
        }});

        doc.add(new Paragraph(
                "Eu, " + associate.getName().toUpperCase() + ", portador(a) do CPF nº " +
                        associate.getCpf() + ", residente e domiciliado(a) em " + associate.getAddress() +
                        ", DECLARO, para os devidos fins de direito, sob as penas da lei, que não possuo " +
                        "condições financeiras de arcar com as custas do processo e os honorários advocatícios " +
                        "sem prejuízo do próprio sustento e de minha família, razão pela qual requeiro os " +
                        "benefícios da JUSTIÇA GRATUITA, nos termos do art. 98 do Código de Processo Civil.",
                normal) {{
            setAlignment(Element.ALIGN_JUSTIFIED);
            setSpacingAfter(20);
        }});

        doc.add(new Paragraph(
                "Por ser expressão da verdade, firmo a presente declaração.", normal) {{
            setAlignment(Element.ALIGN_JUSTIFIED);
            setSpacingAfter(30);
        }});

        doc.add(new Paragraph(
                associate.getAddress() + ", " + today + ".", normal) {{
            setAlignment(Element.ALIGN_RIGHT);
            setSpacingAfter(50);
        }});

        doc.add(new Paragraph("_______________________________________", normal) {{
            setAlignment(Element.ALIGN_CENTER);
        }});
        doc.add(new Paragraph(associate.getName().toUpperCase(), bold) {{
            setAlignment(Element.ALIGN_CENTER);
        }});
        doc.add(new Paragraph("CPF: " + associate.getCpf(), normal) {{
            setAlignment(Element.ALIGN_CENTER);
        }});
    }

    private byte[] generateDocx(DocumentType type, Associate associate, User coordinator)
            throws Exception {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        try (XWPFDocument doc = new XWPFDocument()) {
            documentConfigure(doc);
            switch (type) {
                case POWER_OF_ATTORNEY -> addPowerOfAttorneyDocx(doc, associate, coordinator);
                case DECLARATION_OF_INSUFFICIENCY_OF_RESOURCES -> addDeclarationDocx(doc, associate);
                default -> throw new InvalidArgumentException("Tipo de documento inválido!");
            }
            doc.write(byteArrayOutputStream);
        }

        return byteArrayOutputStream.toByteArray();
    }

    private void documentConfigure(XWPFDocument doc) {
        doc.createStyles();
    }

    private void addPowerOfAttorneyDocx(XWPFDocument doc, Associate associate, User coordinator) {
        String today = LocalDate.now().format(FORMATTER_BR);

        XWPFParagraph title = doc.createParagraph();
        title.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun runTitle = title.createRun();
        runTitle.setText("PROCURAÇÃO AD JUDICIA ET EXTRA");
        runTitle.setBold(true);
        runTitle.setFontSize(14);
        runTitle.setFontFamily("Times New Roman");
        runTitle.addBreak();

        XWPFParagraph corpo = doc.createParagraph();
        corpo.setAlignment(ParagraphAlignment.BOTH);
        XWPFRun runCorpo = corpo.createRun();
        runCorpo.setFontFamily("Times New Roman");
        runCorpo.setFontSize(12);
        runCorpo.setText(
                associate.getName().toUpperCase() + ", portador(a) do CPF nº " + associate.getCpf() +
                        ", residente e domiciliado(a) em " + associate.getAddress() +
                        ", pelo presente instrumento particular, nameia e constitui como seu(sua) bastante " +
                        "procurador(a) o(a) Dr(a). " + coordinator.getName().toUpperCase() +
                        ", advogado(a) devidamente inscrito(a) na Ordem dos Advogados do Brasil, " +
                        "a quem confere amplos poderes para representá-lo(a) em Juízo ou fora dele, " +
                        "podendo praticar todos os atos necessários ao fiel desempenho deste mandato, " +
                        "incluindo receber citações, confessar, desistir, transigir, firmar compromissos " +
                        "e substabelecer com ou sem reservas."
        );

        XWPFParagraph localDate = doc.createParagraph();
        localDate.setAlignment(ParagraphAlignment.RIGHT);
        XWPFRun runDate = localDate.createRun();
        runDate.setFontFamily("Times New Roman");
        runDate.setFontSize(12);
        runDate.addBreak();
        runDate.setText(associate.getAddress() + ", " + today + ".");

        addSignature(doc, associate.getName(), associate.getCpf());
    }

    private void addDeclarationDocx(XWPFDocument doc, Associate associate) {
        String today = LocalDate.now().format(FORMATTER_BR);

        XWPFParagraph title = doc.createParagraph();
        title.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun runTitle = title.createRun();
        runTitle.setText("DECLARAÇÃO DE HIPOSSUFICIÊNCIA");
        runTitle.setBold(true);
        runTitle.setFontSize(14);
        runTitle.setFontFamily("Times New Roman");
        runTitle.addBreak();

        XWPFParagraph corpo = doc.createParagraph();
        corpo.setAlignment(ParagraphAlignment.BOTH);
        XWPFRun runCorpo = corpo.createRun();
        runCorpo.setFontFamily("Times New Roman");
        runCorpo.setFontSize(12);
        runCorpo.setText(
                "Eu, " + associate.getName().toUpperCase() + ", portador(a) do CPF nº " +
                        associate.getCpf() + ", residente e domiciliado(a) em " + associate.getAddress() +
                        ", DECLARO, para os devidos fins de direito, sob as penas da lei, que não possuo " +
                        "condições financeiras de arcar com as custas do processo e os honorários advocatícios " +
                        "sem prejuízo do próprio sustento e de minha família, razão pela qual requeiro os " +
                        "benefícios da JUSTIÇA GRATUITA, nos termos do art. 98 do Código de Processo Civil."
        );

        XWPFParagraph clasp = doc.createParagraph();
        clasp.setAlignment(ParagraphAlignment.BOTH);
        XWPFRun runClasp = clasp.createRun();
        runClasp.setFontFamily("Times New Roman");
        runClasp.setFontSize(12);
        runClasp.addBreak();
        runClasp.setText("Por ser expressão da verdade, firmo a presente declaração.");

        XWPFParagraph localDate = doc.createParagraph();
        localDate.setAlignment(ParagraphAlignment.RIGHT);
        XWPFRun runDate = localDate.createRun();
        runDate.setFontFamily("Times New Roman");
        runDate.setFontSize(12);
        runDate.addBreak();
        runDate.setText(associate.getAddress() + ", " + today + ".");

        addSignature(doc, associate.getName(), associate.getCpf());
    }

    private void addSignature(XWPFDocument doc, String name, String cpf) {
        for (int i = 0; i < 3; i++) doc.createParagraph().createRun().addBreak();

        XWPFParagraph linha = doc.createParagraph();
        linha.setAlignment(ParagraphAlignment.CENTER);
        linha.createRun().setText("_______________________________________");

        XWPFParagraph paragraph = doc.createParagraph();
        paragraph.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun runNome = paragraph.createRun();
        runNome.setBold(true);
        runNome.setFontFamily("Times New Roman");
        runNome.setFontSize(12);
        runNome.setText(name.toUpperCase());

        XWPFParagraph cpfP = doc.createParagraph();
        cpfP.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun runCpf = cpfP.createRun();
        runCpf.setFontFamily("Times New Roman");
        runCpf.setFontSize(12);
        runCpf.setText("CPF: " + cpf);
    }

    private User coordinatorResolve(GenerateDocumentRequest request,
                                    User requester,
                                    Associate associate) {
        if (request.coordinatorId() != null) {
            return userService.findById(request.coordinatorId());
        }

        if (requester.getRole() == UserRole.COORDINATOR) {
            return requester;
        }

        if (requester.getCoordinator() != null) {
            return requester.getCoordinator();
        }
        User estagiarioDoAssociate = associate.getIntern();
        if (estagiarioDoAssociate != null && estagiarioDoAssociate.getCoordinator() != null) {
            return estagiarioDoAssociate.getCoordinator();
        }

        throw new InvalidCredentialsExceptions(
                "Não foi possível determinar o coordinator para o documento. " +
                        "Informe o coordinatorId na requisição.");
    }

    public String getContentType(DocumentFormat format) {
        return format == DocumentFormat.PDF
                ? "application/pdf"
                : "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    }

    public String getFilename(DocumentType type, DocumentFormat format, String nameAssociate) {
        String base = type == DocumentType.POWER_OF_ATTORNEY ? "procuracao" : "declaracao_hipossuficiencia";
        String ext = format == DocumentFormat.PDF ? ".pdf" : ".docx";
        String name = nameAssociate.toLowerCase().replaceAll("\\s+", "_");
        return base + "_" + name + ext;
    }
}
