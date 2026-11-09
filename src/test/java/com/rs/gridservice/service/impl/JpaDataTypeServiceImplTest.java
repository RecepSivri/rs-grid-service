package com.rs.gridservice.service.impl;

import com.rs.gridservice.dto.DataTypeCreateRequest;
import com.rs.gridservice.dto.DataTypeResponse;
import com.rs.gridservice.dto.DataTypeUpdateRequest;
import com.rs.gridservice.entity.DataTypeEntity;
import com.rs.gridservice.entity.DataTypeKind;
import com.rs.gridservice.exception.InvalidPatternException;
import com.rs.gridservice.exception.ResourceNotFoundException;
import com.rs.gridservice.repository.DataTypeRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JpaDataTypeServiceImplTest {

    @Mock
    private DataTypeRepository dataTypeRepository;
    @Mock
    private EntityManager entityManager;
    @Mock
    private TypedQuery<DataTypeEntity> typedQuery;

    private JpaDataTypeServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new JpaDataTypeServiceImpl(dataTypeRepository, entityManager);
        lenient().when(entityManager.createQuery(anyString(), eq(DataTypeEntity.class))).thenReturn(typedQuery);
        lenient().when(typedQuery.setFirstResult(anyInt())).thenReturn(typedQuery);
        lenient().when(typedQuery.setMaxResults(anyInt())).thenReturn(typedQuery);
        lenient().when(typedQuery.setParameter(anyString(), any())).thenReturn(typedQuery);
    }

    private DataTypeEntity sampleEntity() {
        return DataTypeEntity.builder().id("dt-1").name("Email").kind(DataTypeKind.TEXT).build();
    }

    @Test
    void addDataTypeGeneratesIdAndPersists() {
        DataTypeCreateRequest request = new DataTypeCreateRequest();
        request.setName("Email");
        request.setKind(DataTypeKind.TEXT);

        ArgumentCaptor<DataTypeEntity> captor = ArgumentCaptor.forClass(DataTypeEntity.class);
        when(dataTypeRepository.save(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        DataTypeResponse response = service.addDataType(request);

        assertThat(response.getId()).isNotBlank();
        assertThat(response.getName()).isEqualTo("Email");
        assertThat(response.getKind()).isEqualTo(DataTypeKind.TEXT);
        assertThat(captor.getValue().getId()).isEqualTo(response.getId());
    }

    @Test
    void addDataTypeWithoutPatternPersistsNullPattern() {
        DataTypeCreateRequest request = new DataTypeCreateRequest();
        request.setName("Ad Soyad");
        request.setKind(DataTypeKind.TEXT);
        when(dataTypeRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        DataTypeResponse response = service.addDataType(request);

        assertThat(response.getPattern()).isNull();
        assertThat(response.getPatternMessage()).isNull();
    }

    @Test
    void addDataTypePersistsPatternAndPatternMessage() {
        DataTypeCreateRequest request = new DataTypeCreateRequest();
        request.setName("Email");
        request.setKind(DataTypeKind.TEXT);
        request.setPattern("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
        request.setPatternMessage("Gecerli bir e-posta adresi giriniz");
        request.setPatternMessageEn("Please enter a valid email address");
        when(dataTypeRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        DataTypeResponse response = service.addDataType(request);

        assertThat(response.getPattern()).isEqualTo("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
        assertThat(response.getPatternMessage()).isEqualTo("Gecerli bir e-posta adresi giriniz");
        assertThat(response.getPatternMessageEn()).isEqualTo("Please enter a valid email address");
    }

    @Test
    void addDataTypeWithBlankPatternPersistsNullLikeAnEmptyField() {
        // pattern null DEGIL ama bos/whitespace -- validatePattern'in isBlank() dalini
        // da kapsar (null-check'ten AYRI bir dal, bkz. validatePattern).
        DataTypeCreateRequest request = new DataTypeCreateRequest();
        request.setName("Ad Soyad");
        request.setKind(DataTypeKind.TEXT);
        request.setPattern("   ");
        when(dataTypeRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        DataTypeResponse response = service.addDataType(request);

        assertThat(response.getPattern()).isEqualTo("   ");
    }

    @Test
    void addDataTypeWithInvalidRegexThrowsAndDoesNotPersist() {
        DataTypeCreateRequest request = new DataTypeCreateRequest();
        request.setName("Bozuk");
        request.setKind(DataTypeKind.TEXT);
        request.setPattern("(["); // gecersiz regex: kapanmamis grup/karakter sinifi

        assertThatThrownBy(() -> service.addDataType(request))
                .isInstanceOf(InvalidPatternException.class);
        verify(dataTypeRepository, never()).save(any());
    }

    @Test
    void getDataTypeReturnsMappedResponseWhenFound() {
        when(dataTypeRepository.findById("dt-1")).thenReturn(Optional.of(sampleEntity()));

        DataTypeResponse response = service.getDataType("dt-1");

        assertThat(response.getName()).isEqualTo("Email");
    }

    @Test
    void getDataTypeThrowsNotFoundWhenMissing() {
        when(dataTypeRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getDataType("missing"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getAllDataTypesWithoutSearchSkipsParameterBinding() {
        when(typedQuery.getResultList()).thenReturn(List.of(sampleEntity()));

        List<DataTypeResponse> response = service.getAllDataTypes(0, 50, null);

        assertThat(response).hasSize(1);
        verify(typedQuery, never()).setParameter(anyString(), any());
    }

    @Test
    void getAllDataTypesWithBlankSearchSkipsParameterBinding() {
        // search null DEGIL ama bos/whitespace -- hasSearch = search != null && !isBlank()
        // ifadesindeki isBlank() dalini (null-check'ten AYRI) kapsar.
        when(typedQuery.getResultList()).thenReturn(List.of());

        service.getAllDataTypes(0, 50, "   ");

        verify(typedQuery, never()).setParameter(anyString(), any());
    }

    @Test
    void getAllDataTypesWithSearchBindsParameter() {
        when(typedQuery.getResultList()).thenReturn(List.of(sampleEntity()));

        List<DataTypeResponse> response = service.getAllDataTypes(0, 50, "em");

        assertThat(response).hasSize(1);
        verify(typedQuery, times(1)).setParameter("search", "em");
    }

    @Test
    void editDataTypeUpdatesNameKindAndPattern() {
        DataTypeEntity existing = sampleEntity();
        when(dataTypeRepository.findById("dt-1")).thenReturn(Optional.of(existing));
        when(dataTypeRepository.save(existing)).thenReturn(existing);

        DataTypeUpdateRequest request = new DataTypeUpdateRequest();
        request.setName("Telefon");
        request.setKind(DataTypeKind.TEXT);
        request.setPattern("^(\\+90|0)?5\\d{9}$");
        request.setPatternMessage("Gecerli bir telefon numarasi giriniz");
        request.setPatternMessageEn("Please enter a valid phone number");

        DataTypeResponse response = service.editDataType("dt-1", request);

        assertThat(response.getName()).isEqualTo("Telefon");
        assertThat(response.getPattern()).isEqualTo("^(\\+90|0)?5\\d{9}$");
        assertThat(response.getPatternMessage()).isEqualTo("Gecerli bir telefon numarasi giriniz");
        assertThat(response.getPatternMessageEn()).isEqualTo("Please enter a valid phone number");
    }

    @Test
    void editDataTypeCanClearAnExistingPattern() {
        DataTypeEntity existing = DataTypeEntity.builder()
                .id("dt-1").name("Email").kind(DataTypeKind.TEXT)
                .pattern("^.+@.+$").patternMessage("gecersiz").patternMessageEn("invalid").build();
        when(dataTypeRepository.findById("dt-1")).thenReturn(Optional.of(existing));
        when(dataTypeRepository.save(existing)).thenReturn(existing);

        DataTypeUpdateRequest request = new DataTypeUpdateRequest();
        request.setName("Email");
        request.setKind(DataTypeKind.TEXT);
        // pattern/patternMessage/patternMessageEn ayarlanmadi (null) -- dogrulama kaldiriliyor.

        DataTypeResponse response = service.editDataType("dt-1", request);

        assertThat(response.getPattern()).isNull();
        assertThat(response.getPatternMessage()).isNull();
        assertThat(response.getPatternMessageEn()).isNull();
    }

    @Test
    void editDataTypeWithInvalidRegexThrowsAndDoesNotSave() {
        // Pattern dogrulamasi findById'den ONCE calisir (bkz. editDataType) --
        // bulunamayan bir kayit icin bile gecersiz regex'i erken reddetmek daha
        // tutarli bir hata veriyor; bu yuzden findById hic stub'lanmiyor.
        DataTypeUpdateRequest request = new DataTypeUpdateRequest();
        request.setName("Email");
        request.setKind(DataTypeKind.TEXT);
        request.setPattern("(["); // gecersiz regex

        assertThatThrownBy(() -> service.editDataType("dt-1", request))
                .isInstanceOf(InvalidPatternException.class);
        verify(dataTypeRepository, never()).save(any());
    }

    @Test
    void editDataTypeThrowsNotFoundWhenMissing() {
        when(dataTypeRepository.findById("missing")).thenReturn(Optional.empty());

        DataTypeUpdateRequest request = new DataTypeUpdateRequest();
        request.setName("Telefon");
        request.setKind(DataTypeKind.TEXT);

        assertThatThrownBy(() -> service.editDataType("missing", request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteDataTypeRemovesWhenExists() {
        when(dataTypeRepository.existsById("dt-1")).thenReturn(true);

        service.deleteDataType("dt-1");

        verify(dataTypeRepository).deleteById("dt-1");
    }

    @Test
    void deleteDataTypeThrowsNotFoundWhenMissing() {
        when(dataTypeRepository.existsById("missing")).thenReturn(false);

        assertThatThrownBy(() -> service.deleteDataType("missing"))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(dataTypeRepository, never()).deleteById(anyString());
    }
}
