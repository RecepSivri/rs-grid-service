package com.rs.gridservice.service;

import com.rs.gridservice.dto.PageCreateRequest;
import com.rs.gridservice.dto.PageResponse;
import com.rs.gridservice.dto.PageUpdateRequest;

import java.util.List;

public interface PageService {

    PageResponse addPage(PageCreateRequest request);

    PageResponse getPage(String pageId);

    /** projectId'ye ve userId'ye ait page kayitlarini listeler. */
    List<PageResponse> getAllPages(String projectId, String userId, int first, int max, String search);

    /** Sadece kaydin sahibi (userId) guncelleyebilir; baskasina aitse "bulunamadi" hatasi doner. */
    PageResponse editPage(String pageId, String userId, PageUpdateRequest request);

    /** Sadece kaydin sahibi (userId) silebilir; baskasina aitse "bulunamadi" hatasi doner. */
    void deletePage(String pageId, String userId);
}
