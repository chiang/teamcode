package io.teamcode.common.vcs.svn;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Iterator;
import java.util.List;

/**
 *
 * Subversion History 를 Pagination 합니다.
 * <strong>Revision 이 순차적으로 기록되었음을 전제로 합니다.</strong> 또한 정렬이 revision number 내림 차순임을 전제로 합니다.
 */
public class CommitsPageImpl implements Page<Commit> {

    private List<Commit> repositoryHistories;

    private int size;

    private long headRevisionNumber;

    public CommitsPageImpl(List<Commit> repositoryHistories, int size, long headRevisionNumber) {
        this.repositoryHistories = repositoryHistories;
        this.size = size;
        this.headRevisionNumber = headRevisionNumber;
    }

    @Override
    public int getTotalPages() {
        return 0;
    }

    @Override
    public long getTotalElements() {
        return (int)headRevisionNumber;
    }

    @Override
    public int getNumber() {
        return 0;
    }

    @Override
    public int getSize() {
        return this.size;
    }

    @Override
    public int getNumberOfElements() {
        return this.repositoryHistories.size();
    }

    @Override
    public List<Commit> getContent() {
        return this.repositoryHistories;
    }

    @Override
    public boolean hasContent() {
        return repositoryHistories != null && this.repositoryHistories.size() > 0;
    }

    @Override
    public Sort getSort() {
        return null;
    }

    @Override
    public boolean isFirst() {
        return this.repositoryHistories == null || this.repositoryHistories.stream().anyMatch(r -> r.getRevision() == headRevisionNumber);
    }

    @Override
    public boolean isLast() {
        return this.repositoryHistories == null || this.repositoryHistories.stream().anyMatch(r -> r.getRevision() == 1l);
    }

    /**
     *
     *
     * @return
     */
    @Override
    public boolean hasNext() {
        return this.repositoryHistories == null || this.repositoryHistories.stream().anyMatch(r -> r.getRevision() == 1l);
    }

    @Override
    public boolean hasPrevious() {
        return false;
    }

    @Override
    public Pageable nextPageable() {
        return null;
    }

    @Override
    public Pageable previousPageable() {
        return null;
    }

    @Override
    public <S> Page<S> map(Converter<? super Commit, ? extends S> converter) {
        return null;
    }

    @Override
    public Iterator<Commit> iterator() {
        return this.repositoryHistories.iterator();
    }
}
