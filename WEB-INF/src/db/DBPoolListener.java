package db;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.sql.Connection;

@WebListener
public class DBPoolListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        DBPool.init(sce.getServletContext());
        System.out.println("[DPL] DBPool initialized");
        // 프론트엔드 테이블 초기화
        try (Connection conn = DBPool.getConnection()) {
            FrontSetupExtension.init(conn);
        } catch (Exception e) {
            System.err.println("[DPL] FrontSetup 실패: " + e.getMessage());
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        DBPool.close();
        System.out.println("[DPL] DBPool closed");
    }
}
