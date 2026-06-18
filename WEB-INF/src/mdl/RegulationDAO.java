package mdl;

import java.sql.*;
import java.util.*;
import db.DBPool;

/**
 * MODULE: regulation (규제사항)
 * 원본: _system/mdl/m.regulation.asp
 * SP: uspLawRegulation_GetListPaging / GetList / GetInfo / PutInfo
 */
public class RegulationDAO {

    // ── 목록 (페이징) — 패턴 A ──────────────────────────────────────
    public Map<String, Object> getListPaging(
            int page, int listSize,
            String qKey, String qWord, int qLL, String qSort) throws Exception {

        Map<String, Object> result = new LinkedHashMap<>();
        List<Map<String, Object>> rows = new ArrayList<>();
        int total = 0;

        try (Connection conn = DBPool.getConnection();
             CallableStatement cs = conn.prepareCall(
                     "{call uspLawRegulation_GetListPaging(?,?,?,?,?,?)}")) {

            cs.setInt(1, page);
            cs.setInt(2, listSize);
            cs.setString(3, qKey);
            cs.setString(4, qWord);
            cs.setInt(5, qLL);
            cs.setString(6, qSort.isEmpty() ? "" : qSort.toUpperCase());

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
                // 두 번째 RS: total count
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

    // ── 전체 목록 (드롭다운용) — 패턴 A ────────────────────────────
    public List<Map<String, Object>> getListAll(
            String isUse, int parent, int idx) throws Exception {

        List<Map<String, Object>> rows = new ArrayList<>();
        try (Connection conn = DBPool.getConnection();
             CallableStatement cs = conn.prepareCall(
                     "{call uspLawRegulation_GetList(?,?,?)}")) {

            cs.setString(1, isUse);
            cs.setInt(2, parent);
            cs.setInt(3, idx);

            try (ResultSet rs = cs.executeQuery()) {
                ResultSetMetaData meta = rs.getMetaData();
                int colCnt = meta.getColumnCount();
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    for (int i = 1; i <= colCnt; i++)
                        row.put(meta.getColumnName(i).toLowerCase(), rs.getObject(i));
                    rows.add(row);
                }
            }
        }
        return rows;
    }

    // ── 상세 — 패턴 A ───────────────────────────────────────────────
    public Map<String, Object> getInfo(int idx) throws Exception {
        Map<String, Object> row = new LinkedHashMap<>();
        try (Connection conn = DBPool.getConnection();
             CallableStatement cs = conn.prepareCall(
                     "{call uspLawRegulation_GetInfo(?)}")) {

            cs.setInt(1, idx);
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

    // ── 규제법률 목록 (드롭다운용) ───────────────────────────────────
    public List<Map<String, Object>> getLegalList(String isUse) throws Exception {
        List<Map<String, Object>> rows = new ArrayList<>();
        try (Connection conn = DBPool.getConnection();
             CallableStatement cs = conn.prepareCall(
                     "{call uspLawRegulationLegal_GetList(?,?,?)}")) {
            cs.setString(1, isUse);
            cs.setInt(2, 0);
            cs.setInt(3, 0);
            try (ResultSet rs = cs.executeQuery()) {
                ResultSetMetaData meta = rs.getMetaData();
                int colCnt = meta.getColumnCount();
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    for (int i = 1; i <= colCnt; i++)
                        row.put(meta.getColumnName(i).toLowerCase(), rs.getObject(i));
                    rows.add(row);
                }
            }
        }
        return rows;
    }

    // ── 저장 (ADD/MOD/DEL) — 패턴 C (OUTPUT 파라미터) ──────────────
    public Map<String, Object> putInfo(
            String action, int idx, String title, int llIdx,
            String condition, String certifyGuide, String penalty,
            String isUse, String regUser, String regIp) throws Exception {

        Map<String, Object> rtn = new LinkedHashMap<>();
        // SP 파라미터 순서: ACTION, IDX, TITLE, LL_IDX, CONDITION,
        //   CERTIFY_GUIDE, PENALTY, IS_USE, REG_USER, REG_IP, RTN_MSG(OUT), RTN_IDX(OUT)
        try (Connection conn = DBPool.getConnection();
             CallableStatement cs = conn.prepareCall(
                     "{call uspLawRegulation_PutInfo(?,?,?,?,?,?,?,?,?,?,?,?)}")) {

            cs.setString(1, action);
            cs.setInt(2, idx);
            cs.setString(3, title);
            cs.setInt(4, llIdx);
            cs.setString(5, condition);
            cs.setString(6, certifyGuide);
            cs.setString(7, penalty);
            cs.setString(8, isUse);
            cs.setString(9, regUser);
            cs.setString(10, regIp);
            cs.registerOutParameter(11, Types.NVARCHAR);  // @RTN_MSG
            cs.registerOutParameter(12, Types.INTEGER);   // @RTN_IDX

            cs.execute();

            String msg = cs.getString(11);
            rtn.put("rtn_msg", msg != null ? msg : "");
            rtn.put("rtn_idx", cs.getInt(12));
        }
        return rtn;
    }
}
