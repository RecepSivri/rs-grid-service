package com.rs.gridservice.repository;

import com.rs.gridservice.entity.ProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProjectRepository extends JpaRepository<ProjectEntity, String> {

    /** TestRunService.getRunsForUser icin: tek bir kullanicinin SAHIP OLDUGU tum
     * projelerin id'lerini bulmak -- Dashboard'un "test durumu" ozeti HER projeyi
     * TEK cagrida gostermesi gerekiyor (bkz. TestRunController). */
    List<ProjectEntity> findByUserId(String userId);

    /**
     * Project.technology bir foreign key degil, Technology.name'in yazma aninda
     * kopyalanmis bir kopyasi -- bu yuzden bir Technology yeniden adlandirildiginda
     * (bkz. JpaTechnologyServiceImpl.editTechnology) onu zaten kullanan projelerin
     * de ayni isimle guncellenmesi gerekir, yoksa eski isimde takili kalirlar.
     */
    @Modifying
    @Query("UPDATE ProjectEntity p SET p.technology = :newName WHERE p.technology = :oldName")
    int renameTechnologyReferences(@Param("oldName") String oldName, @Param("newName") String newName);

    /** Ayni sekilde Project.type de ProjectType.name'in bir kopyasi -- bkz. JpaProjectTypeServiceImpl.editProjectType. */
    @Modifying
    @Query("UPDATE ProjectEntity p SET p.type = :newName WHERE p.type = :oldName")
    int renameTypeReferences(@Param("oldName") String oldName, @Param("newName") String newName);
}
