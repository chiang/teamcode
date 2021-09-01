package io.teamcode.service;

import io.teamcode.common.SystemComponentHelper;
import io.teamcode.common.Tuple;
import io.teamcode.common.vcs.svn.SvnClientFactory;
import io.teamcode.domain.entity.UserState;
import io.teamcode.web.ui.view.AdminOverviewView;
import org.apache.subversion.javahl.ISVNClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

/**
 * Admin General Service
 */
@Service
public class AdminService {

    @Autowired
    SvnClientFactory svnClientFactory;

    @Autowired
    EntityManager entityManager;

    @Autowired
    ProjectService projectService;

    @Autowired
    UserService userService;

    @Autowired
    AttachmentService attachmentService;

    public AdminOverviewView getAdminOverviewView() {
        AdminOverviewView adminOverviewView = new AdminOverviewView();

        ISVNClient client = svnClientFactory.createLocalClient();
        adminOverviewView.setSubversionVersion(client.getVersion().toString());

        adminOverviewView.setPythonVersion(SystemComponentHelper.getPythonVersion());
        adminOverviewView.setHttpdVersion(SystemComponentHelper.getHttpdVersion());

        try {
            Tuple<String, String> tuple = getDatabaseInfo();
            adminOverviewView.setDatabaseProductName(tuple.getA());
            adminOverviewView.setDatabaseVersion(tuple.getB());
        } catch (SQLException e) {
            //TODO
            //e.printStackTrace();

            adminOverviewView.setDatabaseVersion("N/A");
        }

        buildFromDatabase(adminOverviewView);

        return adminOverviewView;
    }

    @Transactional(readOnly = true)
    public void buildFromDatabase(AdminOverviewView adminOverviewView) {
        adminOverviewView.setProjectsCount(projectService.countAll());
        adminOverviewView.setUsersCount(userService.countAll());
        adminOverviewView.setAttachmentsCount(attachmentService.countAll());
        adminOverviewView.setActiveUsersCount(userService.countByState(UserState.ACTIVE));
    }

    @Transactional(readOnly = true)
    public Tuple<String, String> getDatabaseInfo() throws SQLException {
        org.hibernate.engine.spi.SessionImplementor sessionImp =
                (org.hibernate.engine.spi.SessionImplementor) entityManager.getDelegate();
        DatabaseMetaData metadata = sessionImp.connection().getMetaData();

        Tuple<String, String> tuple = new Tuple<>();
        tuple.setA(metadata.getDatabaseProductName());
        tuple.setB(metadata.getDatabaseProductVersion());

        return tuple;
    }
}
