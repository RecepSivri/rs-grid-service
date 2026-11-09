package com.rs.gridservice.service.impl;

import com.rs.gridservice.dto.PageFieldResponse;
import com.rs.gridservice.dto.PageFieldUpsertRequest;
import com.rs.gridservice.entity.PageEntity;
import com.rs.gridservice.entity.PageFieldEntity;
import com.rs.gridservice.exception.ResourceNotFoundException;
import com.rs.gridservice.repository.PageFieldRepository;
import com.rs.gridservice.repository.PageRepository;
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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JpaPageFieldServiceImplTest {

    @Mock
    private PageFieldRepository pageFieldRepository;
    @Mock
    private PageRepository pageRepository;

    private JpaPageFieldServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new JpaPageFieldServiceImpl(pageFieldRepository, pageRepository);
    }

    private PageEntity sampleOwnedPage() {
        return PageEntity.builder().id("page-1").projectId("project-1").userId("user-1").name("Patients").build();
    }

    private PageFieldEntity sampleField(String id, String dataField, int order) {
        return PageFieldEntity.builder().id(id).pageId("page-1").dataField(dataField).caption("Ad")
                .inputType("text").visible(true).readonly(false).required(true).sortOrder(order).build();
    }

    private PageFieldUpsertRequest upsert(String dataField) {
        PageFieldUpsertRequest request = new PageFieldUpsertRequest();
        request.setDataField(dataField);
        request.setCaption("Ad");
        request.setInputType("text");
        request.setVisible(true);
        request.setReadonly(false);
        request.setRequired(true);
        return request;
    }

    @Test
    void getFieldsReturnsOrderedMappedResponsesWhenPageOwnedByUser() {
        when(pageRepository.findById("page-1")).thenReturn(Optional.of(sampleOwnedPage()));
        when(pageFieldRepository.findByPageIdOrderBySortOrderAsc("page-1"))
                .thenReturn(List.of(sampleField("field-1", "name", 0), sampleField("field-2", "email", 1)));

        List<PageFieldResponse> response = service.getFields("page-1", "user-1");

        assertThat(response).hasSize(2);
        assertThat(response.get(0).getDataField()).isEqualTo("name");
        assertThat(response.get(0).isVisible()).isTrue();
        assertThat(response.get(0).isRequired()).isTrue();
        assertThat(response.get(1).getDataField()).isEqualTo("email");
    }

    @Test
    void getFieldsThrowsNotFoundWhenPageMissing() {
        when(pageRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getFields("missing", "user-1"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getFieldsThrowsNotFoundWhenPageOwnedByDifferentUser() {
        when(pageRepository.findById("page-1")).thenReturn(Optional.of(sampleOwnedPage()));

        assertThatThrownBy(() -> service.getFields("page-1", "someone-else"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void replaceFieldsDeletesExistingAndPersistsNewOrderedEntitiesWithFreshIds() {
        when(pageRepository.findById("page-1")).thenReturn(Optional.of(sampleOwnedPage()));
        ArgumentCaptor<List<PageFieldEntity>> captor = ArgumentCaptor.forClass(List.class);
        when(pageFieldRepository.saveAll(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        List<PageFieldResponse> response = service.replaceFields("page-1", "user-1", List.of(upsert("name"), upsert("email")));

        verify(pageFieldRepository).deleteByPageId("page-1");
        List<PageFieldEntity> saved = captor.getValue();
        assertThat(saved).hasSize(2);
        assertThat(saved.get(0).getId()).isNotBlank();
        assertThat(saved.get(0).getPageId()).isEqualTo("page-1");
        assertThat(saved.get(0).getDataField()).isEqualTo("name");
        assertThat(saved.get(0).getSortOrder()).isEqualTo(0);
        assertThat(saved.get(1).getDataField()).isEqualTo("email");
        assertThat(saved.get(1).getSortOrder()).isEqualTo(1);
        assertThat(saved.get(0).getId()).isNotEqualTo(saved.get(1).getId());

        assertThat(response).hasSize(2);
        assertThat(response.get(1).getSortOrder()).isEqualTo(1);
    }

    @Test
    void replaceFieldsWithEmptyListClearsTheFields() {
        when(pageRepository.findById("page-1")).thenReturn(Optional.of(sampleOwnedPage()));
        when(pageFieldRepository.saveAll(anyList())).thenReturn(List.of());

        List<PageFieldResponse> response = service.replaceFields("page-1", "user-1", List.of());

        verify(pageFieldRepository).deleteByPageId("page-1");
        assertThat(response).isEmpty();
    }

    @Test
    void replaceFieldsThrowsNotFoundWhenPageMissing() {
        when(pageRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.replaceFields("missing", "user-1", List.of()))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(pageFieldRepository, never()).deleteByPageId(anyString());
        verify(pageFieldRepository, never()).saveAll(anyList());
    }

    @Test
    void replaceFieldsThrowsNotFoundWhenPageOwnedByDifferentUser() {
        when(pageRepository.findById("page-1")).thenReturn(Optional.of(sampleOwnedPage()));

        assertThatThrownBy(() -> service.replaceFields("page-1", "someone-else", List.of()))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(pageFieldRepository, never()).saveAll(anyList());
    }
}
