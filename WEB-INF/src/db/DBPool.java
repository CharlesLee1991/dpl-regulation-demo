package db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.servlet.ServletContext;
import java.sql.Connection;

/**
 * HikariCP 커넥션풀 싱글턴
 * 초기화: DBPoolListener (web.xml listener)
 * 사용: DBPool.getConnection()
 */
public class DBPool {

    private static HikariDataSource ds;

    public static void init(ServletContext ctx) {
        String url  = ctx.getInitParameter("DB_URL");
        String user = ctx.getInitParameter("DB_USER");
        // 비밀번호: 환경변수 DB_PASS 우선, 없으면 context-param
        String pass = System.getProperty("DB_PASS",
                      System.getenv("DB_PASS") != null
                        ? System.getenv("DB_PASS")
                        : ctx.getInitParameter("DB_PASS"));
        int poolSize = Integer.parseInt(
                ctx.getInitParameter("DB_POOL_SIZE") != null
                  ? ctx.getInitParameter("DB_POOL_SIZE") : "10");

        HikariConfig cfg = new HikariConfig();
        cfg.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        cfg.setJdbcUrl(url);
        cfg.setUsername(user);
        cfg.setPassword(pass);
        cfg.setMaximumPoolSize(poolSize);
        cfg.setConnectionTimeout(30_000);
        cfg.setIdleTimeout(600_000);
        cfg.setMaxLifetime(1_800_000);
        cfg.setPoolName("DPL-Pool");
        cfg.setInitializationFailTimeout(-1); // 부팅 시 연결 실패해도 컨텍스트 유지, 지연 재시도

        ds = new HikariDataSource(cfg);
    }

    public static Connection getConnection() throws Exception {
        if (ds == null) throw new IllegalStateException("DBPool not initialized");
        return ds.getConnection();
    }

    public static void close() {
        if (ds != null && !ds.isClosed()) ds.close();
    }
}
