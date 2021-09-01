package io.teamcode.domain.api;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
@Data
public class ChangeSet {

    private List<ChangedFile> changedFiles = new ArrayList<>();

    public void add(ChangedFile changedFile) {
        this.changedFiles.add(changedFile);
    }

}
