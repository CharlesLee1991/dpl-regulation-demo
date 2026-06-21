package mdl;

import java.sql.*;
import java.util.*;
import db.DBPool;

/**
 * MODULE: regulation_legal (규제법률)
 * 원본: _system/mdl/m.regulation_legal.asp
 * SP: uspLawRegulationLegal_GetListPaging / GetInfo / PutInfo
 */
public class LegalDAO {

    // ── 목록 (페이징) — 패턴 A ──────────────────────────────────────
    public Map<String, Object> getListPaging(
            int page, int listSize,
            String qKey, String qWord, String qSort) throws Exception {

        Map<String, Object> result = new LinkedHashMap<>();
        List<Map<String, Object>> rows = new ArrayList<>();
        int total = 0;

        try (Connection conn = DBPool.getConnection();
             CallableStatement cs = conn.prepareCall(
                     "{call uspLawRegulationLegal_GetListPaging(?,?,?,?,?)}")) {

            cs.setInt(1, page);
            cs.setInt(2, listSize);
            cs.setString(3, qKey);
            cs.setString(4, qWord);
            cs.setString(5, qSort.isEmpty() ? "" : qSort.toUpperCase());

            boolean hasRS = cs.execute();
            if (hasRS) {
                try (ResultSet rs = cs.getResultSet()) {
                    ResultSetMetaData meta = rs.getMetaData();
                    int colCnt = meta.getColumnCount();
                    while (rs.next()) {
                        Map<String, Object> row = new LinkedHashMap<>();
                        for (int i = 1; i <= colCnt; i++)
                            row.put(meta.getColumnName(i).toLowerCase(), rs.getObject(i));
                        rows.add(row);
                    }
                }
                if (cs.getMoreResults()) {
                    try (ResultSet rs2 = cs.getResultSet()) {
                        if (rs2 != null && rs2.next()) total = rs2.getInt(1);
                    }
                }
            }
        }
        result.put("rows", rows);
        result.put("total", total);
        return result;
    }

    // ── 단건 상세 — 패턴 A ─────────────────────────────────────────
    public Map<String, Object> getInfo(int llIdx) throws Exception {
        Map<String, Object> row = new LinkedHashMap<>();
        try (Connection conn = DBPool.getConnection();
             CallableStatement cs = conn.prepareCall(
                     "{call uspLawRegulationLegal_GetInfo(?)}")) {

            cs.setInt(1, llIdx);
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) {
                    ResultSetMetaData meta = rs.getMetaData();
                    for (int i = 1; i <= meta.getColumnCount(); i++)
                        row.put(meta.getColumnName(i).toLowerCase(), rs.getObject(i));
                }
            }
        }
        return row;
    }

    // ── ADD/MOD/DEL — 패턴 C (OUTPUT 파라미터) ─────────────────────
    public String[] putInfo(String action, int llIdx,
                            String llTitle, String llDept, String llIsUse,
                            String regUser, String regIp) throws Exception {

        try (Connection conn = DBPool.getConnection();
             CallableStatement cs = conn.prepareCall(
                     "{call uspLawRegulationLegal_PutInfo(?,?,?,?,?,?,?,?)}")) {

            cs.setString(1, action);
            cs.setInt(2, llIdx);
            cs.setString(3, llTitle);
            cs.setString(4, llDept);
            cs.setString(5, llIsUse);
            cs.setString(6, regUser);
            cs.setString(7, regIp);
            cs.registerOutParameter(8, Types.NVARCHAR);  // RTN_MSG

            cs.execute();
            String rtnMsg = cs.getString(8);
            return new String[]{ rtnMsg == null ? "" : rtnMsg };
        }
    }

    public List<Map<String, Object>> getListAll(String isUse) throws Exception {
        List<Map<String, Object>> rows = new ArrayList<>();
        try (Connection conn = DBPool.getConnection();
             CallableStatement cs = conn.prepareCall("{call uspLawRegulationLegal_GetList(?,?,?)}")) {
            cs.setString(1, isUse); cs.setInt(2, 0); cs.setInt(3, 0);
            try (ResultSet rs = cs.executeQuery()) {
                ResultSetMetaData meta = rs.getMetaData(); int colCnt = meta.getColumnCount();
                while (rs.next()) {
                    Map<String, Object> row = new java.util.LinkedHashMap<>();
                    for (int i = 1; i <= colCnt; i++) row.put(meta.getColumnName(i).toLowerCase(), rs.getObject(i));
                    rows.add(row);
                }
            }
        }
        return rows;
    }
}
