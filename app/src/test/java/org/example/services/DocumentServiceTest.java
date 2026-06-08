package org.example.services;

import org.example.dtos.request.GenerateDocumentRequest;
import org.example.enums.DocumentFormat;
import org.example.enums.DocumentType;
import org.example.enums.UserRole;
import org.example.exceptions.InvalidCredentialsExceptions;
import org.example.models.Associate;
import org.example.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private AssociateService associateService;

    @Mock
    private UserService userService;

    @InjectMocks
    private DocumentService documentService;

    private User internUser;
    private User coordinatorUser;
    private Associate associate;

    @BeforeEach
    void setUp() {
        coordinatorUser = User.builder()
                .id(2L)
                .name("Doutor Coordenador")
                .role(UserRole.COORDINATOR)
                .build();

        internUser = User.builder()
                .id(1L)
                .name("Estagiario")
                .role(UserRole.INTERN)
                .coordinator(coordinatorUser)
                .build();

        associate = Associate.builder()
                .id(1L)
                .name("Maria Souza")
                .cpf("12345678900")
                .address("Rua das Flores, 123")
                .intern(internUser)
                .build();
    }

    @Test
    @DisplayName("Deve gerar PDF da Procuração com sucesso")
    void generateShouldReturnPdfBytesForPowerOfAttorney() {

        GenerateDocumentRequest request = new GenerateDocumentRequest(DocumentType.POWER_OF_ATTORNEY, DocumentFormat.PDF, 1L, null);
        when(associateService.findAssociateById(1L)).thenReturn(associate);

        byte[] result = documentService.generate(request, internUser);

        assertNotNull(result);
        assertTrue(result.length > 0); 
        verify(associateService, times(1)).findAssociateById(1L);
    }

    @Test
    @DisplayName("Deve gerar DOCX da Declaração com sucesso")
    void generateShouldReturnDocxBytesForDeclaration() {

        GenerateDocumentRequest request = new GenerateDocumentRequest(DocumentType.DECLARATION_OF_INSUFFICIENCY_OF_RESOURCES, DocumentFormat.DOCX, 1L, null);
        when(associateService.findAssociateById(1L)).thenReturn(associate);

        byte[] result = documentService.generate(request, internUser);

        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar gerar documento sem coordenador válido")
    void generateShouldThrowExceptionWhenCoordinatorCannotBeResolved() {

        User internWithoutCoordinator = User.builder().id(3L).role(UserRole.INTERN).build();
        Associate associateWithoutInternCoordinator = Associate.builder()
                .id(2L).name("Jose").cpf("000").address("Rua").intern(internWithoutCoordinator).build();
        
        GenerateDocumentRequest request = new GenerateDocumentRequest(DocumentType.POWER_OF_ATTORNEY, DocumentFormat.PDF, 2L, null);
        
        when(associateService.findAssociateById(2L)).thenReturn(associateWithoutInternCoordinator);

        InvalidCredentialsExceptions exception = assertThrows(InvalidCredentialsExceptions.class, () -> {
            documentService.generate(request, internWithoutCoordinator);
        });

        assertTrue(exception.getMessage().contains("Não foi possível determinar o coordinator"));
    }

    @Test
    @DisplayName("Deve retornar o content type correto para cada formato")
    void getContentTypeShouldReturnCorrectString() {
        assertEquals("application/pdf", documentService.getContentType(DocumentFormat.PDF));
        assertEquals("application/vnd.openxmlformats-officedocument.wordprocessingml.document", 
                     documentService.getContentType(DocumentFormat.DOCX));
    }

    @Test
    @DisplayName("Deve formatar o nome do arquivo corretamente")
    void getFilenameShouldFormatCorrectly() {
        String pdfFilename = documentService.getFilename(DocumentType.POWER_OF_ATTORNEY, DocumentFormat.PDF, "Maria Souza");
        assertEquals("procuracao_maria_souza.pdf", pdfFilename);

        String docxFilename = documentService.getFilename(DocumentType.DECLARATION_OF_INSUFFICIENCY_OF_RESOURCES, DocumentFormat.DOCX, "João da Silva");
        assertEquals("declaracao_hipossuficiencia_joão_da_silva.docx", docxFilename);
    }
}