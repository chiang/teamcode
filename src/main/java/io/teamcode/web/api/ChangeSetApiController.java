package io.teamcode.web.api;

import io.teamcode.domain.api.ChangeSet;
import io.teamcode.service.visang.VisangRmsCustomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

/**
 * http://localhost:8080/api/v1/changeset?123
 *
 */
@RestController
@RequestMapping("/api/v1/changeset")
public class ChangeSetApiController {

    @Autowired
    VisangRmsCustomService visangRmsCustomService;

    /**
     *
     *
     * @param rmsId
     * @return
     */
    @GetMapping(params = {"rms_id"})
    public ChangeSet search(@RequestParam("rms_id") long rmsId, HttpServletResponse response) {
        response.addHeader("Access-Control-Allow-Origin", "*");

        /*ChangeSet changeSet = new ChangeSet();
        changeSet.add(ChangedFile.builder().author("shimwb").lastModifiedAt(new Date()).name("bootstrap.js").build());
        changeSet.add(ChangedFile.builder().author("kooja").lastModifiedAt(new Date()).name("login.asp").build());
        changeSet.add(ChangedFile.builder().author("kooja").lastModifiedAt(new Date()).name("index.jsp").build());

        return changeSet;*/

        return visangRmsCustomService.getByRmsId(rmsId);
    }
}
