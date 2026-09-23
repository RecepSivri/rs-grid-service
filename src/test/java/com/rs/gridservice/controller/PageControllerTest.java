package com.rs.gridservice.controller;

import com.rs.gridservice.dto.PageCreateRequest;
import com.rs.gridservice.dto.PageResponse;
import com.rs.gridservice.dto.PageUpdateRequest;
import com.rs.gridservice.service.PageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PageControllerTest {

    @Mock
    private PageService pageService;

    @InjectMocks
    private PageController pageController;

    @Test
    void addPageReturnsCreatedWithLocation() {
        PageCreateRequest request = new PageCreateRequest();
        request.setProjectId("project-1");
        request.setUserId("user-1");
        request.setName("Kullanicilar");
        PageResponse created = PageResponse.builder().id("page-1").projectId("project-1")
                .userId("user-1").name("Kullanicilar").build();
        when(pageService.addPage(request)).thenReturn(created);

        ResponseEntity<PageResponse> response = pageController.addPage(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getHeaders().getLocation()).hasToString("/api/v1/pages/page-1");
        assertThat(response.getBody()).isSameAs(created);
    }

    @Test
    void getPageReturnsPageFromService() {
        PageResponse page = PageResponse.builder().id("page-1").build();
        when(pageService.getPage("page-1")).thenReturn(page);

        ResponseEntity<PageResponse> response = pageController.getPage("page-1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(page);
    }

    @Test
    void getAllPagesDelegatesProjectIdUserIdPagingAndSearchToService() {
        List<PageResponse> pages = List.of(PageResponse.builder().id("page-1").build());
        when(pageService.getAllPages("project-1", "user-1", 0, 50, "kullan")).thenReturn(pages);

        ResponseEntity<List<PageResponse>> response = pageController.getAllPages("project-1", "user-1", 0, 50, "kullan");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(pages);
    }

    @Test
    void editPageReturnsUpdatedPage() {
        PageUpdateRequest request = new PageUpdateRequest();
        request.setName("Kullanicilar v2");
        PageResponse updated = PageResponse.builder().id("page-1").name("Kullanicilar v2").build();
        when(pageService.editPage("page-1", "user-1", request)).thenReturn(updated);

        ResponseEntity<PageResponse> response = pageController.editPage("page-1", "user-1", request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(updated);
    }

    @Test
    void deletePageReturnsNoContentAndDelegatesToService() {
        ResponseEntity<Void> response = pageController.deletePage("page-1", "user-1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(pageService).deletePage("page-1", "user-1");
    }
}
