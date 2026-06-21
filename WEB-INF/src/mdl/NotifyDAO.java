package mdl;

import java.sql.*;
import java.util.*;
import db.DBPool;

/**
 * MODULE: notify (고시/부속서)
 * 원본: _system/mdl/m.notify.asp
 * SP: uspLawNotify_GetListPaging / GetList / GetInfo / PutInfo
 */
public class NotifyDAO {

    // ── 목록 (페이징) ─────────────────────────────────────────────────
    public Map<String, Object> getListPaging(
            int page, int listSize,
            String qKey, String qWord,
            int qLL, int qLR, String qSort) throws Exception {

        Map<String, Object> result = new LinkedHashMap<>();
        List<Map<String, Object>> rows = new ArrayList<>();
        int total = 0;

        try (Connection conn = DBPool.getConnection();
             CallableStatement cs = conn.prepareCall(
                     "{call uspLawNotify_GetListPaging(?,?,?,?,?,?,?)}")) {

            cs.setInt(1, page);
            cs.setInt(2, listSize);
            cs.setString(3, qKey);
            cs.setString(4, qWord);
            cs.setInt(5, qLL);
            cs.setInt(6, qLR);
            cs.setString(7, qSort.isEmpty() ? "" : qSort.toUpperCase());

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

    // ── 전체 목록 (드롭다운용) ────────────────────────────────────────
    public List<Map<String, Object>> getListAll(
            String isUse, int parent, int idx) throws Exception {

        List<Map<String, Object>> rows = new ArrayList<>();
        try (Connection conn = DBPool.getConnection();
             CallableStatement cs = conn.prepareCall(
                     "{call uspLawNotify_GetList(?,?,?)}")) {

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

    // ── 상세 ─────────────────────────────────────────────────────────
    public Map<String, Object> getInfo(int idx) throws Exception {
        Map<String, Object> row = new LinkedHashMap<>();
        try (Connection conn = DBPool.getConnection();
             CallableStatement cs = conn.prepareCall(
                     "{call uspLawNotify_GetInfo(?)}")) {

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

    // ── 규제법률 목록 (1단계 드롭다운) ──────────────────────────────
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

    // ── 규제사항 목록 (2단계 드롭다운 — llIdx 기준) ─────────────────
    public List<Map<String, Object>> getRegulationList(String isUse, int llIdx) throws Exception {
        List<Map<String, Object>> rows = new ArrayList<>();
        try (Connection conn = DBPool.getConnection();
             CallableStatement cs = conn.prepareCall(
                     "{call uspLawRegulation_GetList(?,?,?)}")) {
            cs.setString(1, isUse);
            cs.setInt(2, llIdx);
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

    // ── 저장 (ADD/MOD/DEL) ──────────────────────────────────────────
    // SP 파라미터: @ACTION, @IDX, @NOTIFY, @LR_IDX, @HISTORY, @IS_USE,
    //              @ATTA_FILES, @REG_USER, @REG_IP, @RTN_MSG(OUT), @RTN_IDX(OUT)
    public Map<String, Object> putInfo(
            String action, int idx, String notify, int lrIdx,
            String history, String isUse,
            String attaFiles, String regUser, String regIp) throws Exception {

        Map<String, Object> rtn = new LinkedHashMap<>();
        try (Connection conn = DBPool.getConnection();
             CallableStatement cs = conn.prepareCall(
                     "{call uspLawNotify_PutInfo(?,?,?,?,?,?,?,?,?,?,?)}")) {

            cs.setString(1, action);
            cs.setInt(2, idx);
            cs.setString(3, notify);
            cs.setInt(4, lrIdx);
            cs.setString(5, history);
            cs.setString(6, isUse);
            cs.setString(7, attaFiles);
            cs.setString(8, regUser);
            cs.setString(9, regIp);
            cs.registerOutParameter(10, Types.NVARCHAR);  // @RTN_MSG
            cs.registerOutParameter(11, Types.INTEGER);   // @RTN_IDX

            cs.execute();

            String msg = cs.getString(10);
            rtn.put("rtn_msg", msg != null ? msg : "");
            rtn.put("rtn_idx", cs.getInt(11));
        }
        return rtn;
    }
}
