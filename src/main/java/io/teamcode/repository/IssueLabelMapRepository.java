package io.teamcode.repository;

import io.teamcode.domain.entity.*;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

/**
 *
 */
public interface IssueLabelMapRepository extends CrudRepository<IssueLabelMap, IssueLabelMapId> {

    //아래와 같이 하면 실제로 실행할 때 Issue 와 IssueLabelMap 을 Cartesian Product 로 처리하고 그 다음은 조건들을 때리게 된다.
    //따라서 이것은 매우 많은 데이터가 있을 때 느리지 않을까? 하는 고려가 필요하다.
    //일단 키 기반으로만 조회하니까 Index 는 잘 탈 테고 (state 는?) 기관 내부 용도로만 사용하는 것은 큰 문제가 없겠다.
    @Query("SELECT COUNT(ilm) FROM IssueLabelMap ilm WHERE project = :project AND issueLabel = :issueLabel AND issue.state = :state")
    Long countByProjectAndIssueLabel(@Param("project") final Project project, @Param("issueLabel") final IssueLabel issueLabel, @Param("state") final IssueState state);

}
