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
            // 순서 보장: 규제 스키마/SP/시드 먼저, 그 다음 프론트 확장
            new RegulationSetupListener().runSetup(conn);
            FrontSetupExtension.init(conn);
        } catch (Exception e) {
            System.err.println("[DPL] Setup 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        DBPool.close();
        System.out.println("[DPL] DBPool closed");
    }
}
