package ctrl;

import mdl.RegulationDAO;
import mdl.NotifyDAO;
import mdl.ItemsDAO;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;

/**
 * AJAX JSON 엔드포인트 — 드롭다운 연동
 * URL: /ajax/?type=regulation&qLL=1
 *      /ajax/?type=notify&qLR=1
 *      /ajax/?type=items&qLN=1
 *      /ajax/?type=counts   ← 메인 대시보드 건수
 */
public class AjaxServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json; charset=UTF-8");

        String type = nvl(req.getParameter("type"), "");
        PrintWriter out = resp.getWriter();

        try {
            switch (type) {
                // ── 규제사항 목록 (qLL 기준) ─────────────────────────
                case "regulation": {
                    int qLL = toInt(req.getParameter("qLL"), 0);
                    RegulationDAO dao = new RegulationDAO();
                    List<Map<String, Object>> rows = dao.getListAll("Y", qLL, 0);
                    out.print(toJson(rows, "lr_idx", "lr_title"));
                    break;
                }
                // ── 고시 목록 (qLR 기준) ─────────────────────────────
                case "notify": {
                    int qLR = toInt(req.getParameter("qLR"), 0);
                    NotifyDAO dao = new NotifyDAO();
                    List<Map<String, Object>> rows = dao.getListAll("Y", qLR, 0);
                    out.print(toJson(rows, "ln_idx", "ln_title"));
                    break;
                }
                // ── 품목정보(법정) 목록 (qLN 기준) ───────────────────
                case "items": {
                    int qLN = toInt(req.getParameter("qLN"), 0);
                    ItemsDAO dao = new ItemsDAO();
                    List<Map<String, Object>> rows = dao.getItemsList("Y", qLN, 0);
                    out.print(toJson(rows, "li_idx", "li_legal_name"));
                    break;
                }
                // ── 메인 대시보드 건수 ────────────────────────────────
                case "counts": {
                    out.print(getCounts());
                    break;
                }

                // ── 실 MSSQL 연결 테스트 ─────────────────────────────
                case "db_test": {
                    long t0 = System.currentTimeMillis();
                    try {
                        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                        // 접속정보는 컨텍스트/환경변수에서 조회 (평문 하드코딩 금지)
                        String url = getServletContext().getInitParameter("DB_URL");
                        String u = getServletContext().getInitParameter("DB_USER");
                        String p = System.getenv("DB_PASS");
                        if (p == null || p.isEmpty()) p = getServletContext().getInitParameter("DB_PASS");
                        java.sql.Connection conn = java.sql.DriverManager.getConnection(url, u, p);
                        java.sql.Statement st = conn.createStatement();
                        java.sql.ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM LAW_REGULATION_LEGAL WHERE LL_IS_USE='Y'");
                        int cnt = rs.next() ? rs.getInt(1) : -1;
                        conn.close();
                        long ms = System.currentTimeMillis() - t0;
                        out.print("{\"status\":\"OK\",\"db\":\"LSAFE\",\"host\":\"121.78.147.103,16868\",\"table\":\"LAW_REGULATION_LEGAL\",\"count\":"+cnt+",\"ms\":"+ms+"}");
                    } catch(Exception ex) {
                        long ms = System.currentTimeMillis() - t0;
                        out.print("{\"status\":\"FAIL\",\"error\":\""+escape(ex.getMessage())+"\",\"ms\":"+ms+"}");
                    }
                    break;
                }

                // ── 데이터 초기화 (실제 DPL 데이터로 교체) ──────────
                case "mirror_reload": {
                    // 실 LAW_* 원본 → dpl_* 미러 재적재 (LAW_*는 READ only, dpl_*만 write)
                    // 컬럼 적응형: dpl_x ∩ LAW_X 공통 컬럼만 INSERT ... SELECT (스키마 불일치 방어)
                    String[][] pairs = {
                        {"dpl_regulation_legal","LAW_REGULATION_LEGAL"},
                        {"dpl_regulation","LAW_REGULATION"},
                        {"dpl_notify","LAW_NOTIFY"},
                        {"dpl_safety","LAW_SAFETY"},
                        {"dpl_items","LAW_ITEMS"},
                        {"dpl_items_detail","LAW_ITEMS_DETAIL"}
                    };
                    // 자식→부모 역순 DELETE (FK), 부모→자식 순서 INSERT
                    String[] delOrder = {"dpl_items_detail","dpl_items","dpl_safety","dpl_notify","dpl_regulation","dpl_regulation_legal"};
                    StringBuilder rpt = new StringBuilder("{\"status\":\"OK\",\"tables\":{");
                    try (java.sql.Connection conn = db.DBPool.getConnection()) {
                        // 1) 미러 비우기 (자식부터)
                        try (java.sql.Statement st = conn.createStatement()) {
                            for (String t : delOrder) { try { st.execute("DELETE FROM "+t); } catch(Exception ignored){} }
                        }
                        // 2) 부모→자식 순서로 실 LAW_ → dpl_ 적재
                        boolean first = true;
                        for (String[] p : pairs) {
                            String dpl = p[0], law = p[1];
                            String res;
                            try (java.sql.Statement st = conn.createStatement()) {
                                // 공통 컬럼 추출
                                java.util.List<String> cols = new java.util.ArrayList<>();
                                try (java.sql.ResultSet rs = st.executeQuery(
                                        "SELECT c1.COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS c1 "
                                      + "JOIN INFORMATION_SCHEMA.COLUMNS c2 ON c1.COLUMN_NAME=c2.COLUMN_NAME "
                                      + "WHERE c1.TABLE_NAME='"+dpl+"' AND c2.TABLE_NAME='"+law+"'")) {
                                    while (rs.next()) cols.add("["+rs.getString(1)+"]");
                                }
                                if (cols.isEmpty()) { res="{\"ok\":false,\"err\":\"no common columns\"}"; }
                                else {
                                    String colList = String.join(",", cols);
                                    boolean hasId = colList.toUpperCase().contains("_IDX]");
                                    try (java.sql.Statement st2 = conn.createStatement()) {
                                        if (hasId) { try { st2.execute("SET IDENTITY_INSERT "+dpl+" ON"); } catch(Exception ignored){} }
                                        int n = st2.executeUpdate("INSERT INTO "+dpl+" ("+colList+") SELECT "+colList+" FROM "+law);
                                        if (hasId) { try { st2.execute("SET IDENTITY_INSERT "+dpl+" OFF"); } catch(Exception ignored){} }
                                        res = "{\"ok\":true,\"rows\":"+n+",\"cols\":"+cols.size()+"}";
                                    }
                                }
                            } catch (Exception te) {
                                res = "{\"ok\":false,\"err\":\""+escape(te.getMessage())+"\"}";
                            }
                            if (!first) rpt.append(",");
                            rpt.append("\"").append(dpl).append("\":").append(res);
                            first = false;
                        }
                        rpt.append("}}");
                        out.print(rpt.toString());
                    } catch (Exception ex) {
                        resp.setStatus(500);
                        out.print("{\"status\":\"FAIL\",\"error\":\"" + escape(ex.getMessage()) + "\"}");
                    }
                    break;
                }
                case "data_reset": {
                    try (java.sql.Connection conn = db.DBPool.getConnection()) {
                        try (java.sql.Statement st = conn.createStatement()) {
                            st.execute("DELETE FROM dpl_items_detail");
                            st.execute("DELETE FROM dpl_items");
                            st.execute("DELETE FROM dpl_safety");
                            st.execute("DELETE FROM dpl_notify");
                            st.execute("DELETE FROM dpl_regulation");
                            st.execute("DELETE FROM dpl_regulation_legal");
                            // IDENTITY 리셋
                            for (String tbl : new String[]{"dpl_regulation_legal","dpl_regulation","dpl_notify","dpl_safety","dpl_items","dpl_items_detail"}) {
                                try { st.execute("DBCC CHECKIDENT('"+tbl+"', RESEED, 0)"); } catch(Exception ignored){}
                            }
                        }
                        // 실제 DPL 데이터 삽입
                        db.RegulationSetupListener listener = new db.RegulationSetupListener();
                        listener.insertRealData(conn);
                        db.FrontSetupExtension.init(conn);
                        out.print("{\"status\":\"OK\",\"message\":\"실제 DPL 데이터로 초기화 완료\"}");
                    } catch (Exception ex) {
                        resp.setStatus(500);
                        out.print("{\"status\":\"FAIL\",\"error\":\"" + escape(ex.getMessage()) + "\"}");
                    }
                    break;
                }
                default:
                    resp.setStatus(400);
                    out.print("{\"error\":\"unknown type\"}");
            }
        } catch (Exception e) {
            resp.setStatus(500);
            out.print("{\"error\":\"" + escape(e.getMessage()) + "\"}");
        }
    }

    // ── JSON 직렬화 (외부 라이브러리 없이) ───────────────────────────
    private String toJson(List<Map<String, Object>> rows, String idKey, String nameKey) {
        StringBuilder sb = new StringBuilder("{\"rows\":[");
        boolean first = true;
        for (Map<String, Object> row : rows) {
            if (!first) sb.append(",");
            Object id   = row.get(idKey);
            Object name = row.get(nameKey);
            if (name == null) name = row.get("ln_notify"); // notify 호환
            sb.append("{\"").append(idKey).append("\":").append(id != null ? id : 0)
              .append(",\"").append(nameKey).append("\":\"").append(escape(name != null ? name.toString() : "")).append("\"}");
            first = false;
        }
        sb.append("]}");
        return sb.toString();
    }

    private String getCounts() throws Exception {
        // 각 모듈 카운트를 단일 쿼리로 집계
        StringBuilder sb = new StringBuilder("{");
        String[] tables = {
            "legal",        "dpl_regulation_legal",
            "regulation",   "dpl_regulation",
            "notify",       "dpl_notify",
            "safety",       "dpl_safety",
            "items_def",    "dpl_items",
            "items_detail", "dpl_items_detail",
        };
        try (java.sql.Connection conn = db.DBPool.getConnection()) {
            boolean first = true;
            for (int i = 0; i < tables.length; i += 2) {
                String key   = tables[i];
                String table = tables[i + 1];
                int cnt = 0;
                try (java.sql.Statement st = conn.createStatement();
                     java.sql.ResultSet rs = st.executeQuery(
                         "SELECT COUNT(*) FROM " + table + " WHERE " +
                         (table.equals("dpl_regulation_legal") ? "LL_IS_USE='Y'" :
                          table.equals("dpl_regulation") ? "LR_IS_USE='Y'" :
                          table.equals("dpl_notify") ? "LN_IS_USE='Y'" :
                          table.equals("dpl_safety") ? "LS_IS_USE='Y'" :
                          table.equals("dpl_items") ? "LI_IS_USE='Y'" :
                          "LD_IS_USE='Y'"))) {
                    if (rs.next()) cnt = rs.getInt(1);
                }
                if (!first) sb.append(",");
                sb.append("\"").append(key).append("\":").append(cnt);
                first = false;
            }
        }
        sb.append("}");
        return sb.toString();
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }

    private String nvl(String v, String def) { return (v == null || v.isEmpty()) ? def : v; }
    private int toInt(String v, int def) {
        if (v == null) return def;
        try { return Integer.parseInt(v.trim()); } catch (NumberFormatException e) { return def; }
    }
}
