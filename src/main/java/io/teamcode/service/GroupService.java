package io.teamcode.service;

import io.teamcode.domain.entity.Group;
import io.teamcode.domain.entity.GroupType;
import io.teamcode.repository.GroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by chiang on 2017. 2. 6..
 */
@Service
@Transactional(readOnly = true)
public class GroupService {

    @Autowired
    UserService userService;

    @Autowired
    GroupRepository groupRepository;

    @Transactional
    public Group create(Group group) {
        if (groupRepository.countByPath(group.getPath()) > 0)
            throw new IllegalArgumentException("");//TODO custom error?

        //TODO form validation name?
        //처음에는 경로 정보를 받아서 저장한다. Name 은 나중에 식별하기 좋게 수정하새 사용하므로 수정 페이지에서 직접 Name 변경을 한다.
        group.setName(group.getPath());

        //TODO 소유자를 지정해서 받을 수도 있는디..
        if (group.getOwner() == null)
            group.setOwner(userService.getCurrentUser());

        Group savedGroup = groupRepository.save(group);
        return savedGroup;
    }

    public Group getByPath(final String path) {
        Group group = groupRepository.findByPath(path);
        if(group == null)
            throw new ResourceNotFoundException("");

        return group;
    }

    public List<Group> getGroups() {

        return groupRepository.findByType(GroupType.CUSTOM);
    }
}
