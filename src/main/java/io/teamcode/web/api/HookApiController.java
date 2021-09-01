package io.teamcode.web.api;

import io.teamcode.service.vcs.svn.RepositoryHookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * Created by chiang on 2017. 3. 23..
 */
@RestController
@RequestMapping("/api/v1/hooks")
public class HookApiController {

    @Autowired
    RepositoryHookService repositoryHookService;

    @PostMapping(value = "/post-commit", params = {"path", "rev"})
    @ResponseStatus(HttpStatus.OK)
    public void onPostCommit(@RequestParam String path, @RequestParam("rev") final long revisionNumber) {
        repositoryHookService.onPostCommit(path, revisionNumber);
    }
}
