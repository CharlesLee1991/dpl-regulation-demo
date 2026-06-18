package db;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class DBPoolListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        DBPool.init(sce.getServletContext());
        System.out.println("[DPL] DBPool initialized");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        DBPool.close();
        System.out.println("[DPL] DBPool closed");
    }
}
