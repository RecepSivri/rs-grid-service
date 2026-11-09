package com.rs.gridservice.service.impl;

import com.rs.gridservice.dto.ProjectAuthResponse;
import com.rs.gridservice.dto.ProjectAuthUpsertRequest;
import com.rs.gridservice.entity.ProjectAuthEntity;
import com.rs.gridservice.entity.ProjectEntity;
import com.rs.gridservice.exception.ResourceNotFoundException;
import com.rs.gridservice.repository.ProjectAuthRepository;
import com.rs.gridservice.repository.ProjectRepository;
import com.rs.gridservice.service.ProjectAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Bir Project'in Yetkilendirme (Authorization) bilgisini kendi veritabanimizdaki
 * ("project_auth" tablosu) verilere karsi gerceklestiren servis implementasyonu.
 * Proje basina tek kayit oldugu icin PageFieldService'teki (liste) replaceFields yerine
 * dogrudan upsert/delete var.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JpaProjectAuthServiceImpl implements ProjectAuthService {

    private final ProjectAuthRepository projectAuthRepository;
    private final ProjectRepository projectRepository;

    @Override
    public ProjectAuthResponse getAuth(String projectId, String userId) {
        findOwnedProject(projectId, userId);
        return projectAuthRepository.findById(projectId)
                .map(this::toResponse)
                .orElseGet(this::emptyAuth);
    }

    @Override
    public ProjectAuthResponse saveAuth(String projectId, String userId, ProjectAuthUpsertRequest request) {
        findOwnedProject(projectId, userId);

        ProjectAuthEntity entity = ProjectAuthEntity.builder()
                .projectId(projectId)
                .accessToken(request.getAccessToken())
                .refreshToken(request.getRefreshToken())
                .refreshUrl(request.getRefreshUrl())
                .expiresAt(request.getExpiresAt())
                .build();
        ProjectAuthEntity saved = projectAuthRepository.save(entity);
        log.info("Proje yetkilendirmesi kaydedildi: projectId={}", projectId);
        return toResponse(saved);
    }

    @Override
    public void clearAuth(String projectId, String userId) {
        findOwnedProject(projectId, userId);
        projectAuthRepository.deleteById(projectId);
        log.info("Proje yetkilendirmesi temizlendi: projectId={}", projectId);
    }

    /** Projeyi bulur ve gercekten userId'ye ait oldugunu dogrular; degilse "bulunamadi" ile aynen davranir.
     * JpaProjectServiceImpl'deki ayni yardimcinin bir kopyasi -- bu kodebase'te servisler birbirini degil,
     * dogrudan diger entity'nin repository'sini enjekte eder (bkz. JpaPageFieldServiceImpl -> PageRepository). */
    private ProjectEntity findOwnedProject(String projectId, String userId) {
        return projectRepository.findById(projectId)
                .filter(entity -> entity.getUserId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Proje bulunamadi: id=" + projectId));
    }

    private ProjectAuthResponse toResponse(ProjectAuthEntity entity) {
        return ProjectAuthResponse.builder()
                .accessToken(entity.getAccessToken())
                .refreshToken(entity.getRefreshToken())
                .refreshUrl(entity.getRefreshUrl())
                .expiresAt(entity.getExpiresAt())
                .build();
    }

    /** Henuz hic kaydedilmemis (PageFieldService'teki "bos liste" ile ayni ruhta) bir Project icin
     * donulecek varsayilan -- 404 yerine tum alanlari bos bir yanit. */
    private ProjectAuthResponse emptyAuth() {
        return ProjectAuthResponse.builder().accessToken("").refreshToken("").refreshUrl("").expiresAt(null).build();
    }
}
