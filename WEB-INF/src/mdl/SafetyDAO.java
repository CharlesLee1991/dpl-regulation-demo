package mdl;

import java.sql.*;
import java.util.*;
import db.DBPool;

/**
 * MODULE: safety (안전요건)
 * 원본: _system/mdl/m.safety.asp
 * SP: uspLawSafety_GetListPaging / GetList / GetInfo / PutInfo
 */
public class SafetyDAO {

    public Map<String, Object> getListPaging(
            int page, int listSize,
            String qKey, String qWord,
            int qLL, int qLR, int qLN, String qSort) throws Exception {

        Map<String, Object> result = new LinkedHashMap<>();
        List<Map<String, Object>> rows = new ArrayList<>();
        int total = 0;

        try (Connection conn = DBPool.getConnection();
             CallableStatement cs = conn.prepareCall(
                     "{call uspLawSafety_GetListPaging(?,?,?,?,?,?,?,?)}")) {
            cs.setInt(1, page);
            cs.setInt(2, listSize);
            cs.setString(3, qKey);
            cs.setString(4, qWord);
            cs.setInt(5, qLL);
            cs.setInt(6, qLR);
            cs.setInt(7, qLN);
            cs.setString(8, qSort.isEmpty() ? "" : qSort.toUpperCase());

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

    public List<Map<String, Object>> getListAll(
            String flag, String isUse, int parent, int idx) throws Exception {
        List<Map<String, Object>> rows = new ArrayList<>();
        try (Connection conn = DBPool.getConnection();
             CallableStatement cs = conn.prepareCall(
                     "{call uspLawSafety_GetList(?,?,?,?)}")) {
            cs.setString(1, flag);
            cs.setString(2, isUse);
            cs.setInt(3, parent);
            cs.setInt(4, idx);
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

    public Map<String, Object> getInfo(int idx) throws Exception {
        Map<String, Object> row = new LinkedHashMap<>();
        try (Connection conn = DBPool.getConnection();
             CallableStatement cs = conn.prepareCall(
                     "{call uspLawSafety_GetInfo(?)}")) {
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

    public List<Map<String, Object>> getLegalList(String isUse) throws Exception {
        List<Map<String, Object>> rows = new ArrayList<>();
        try (Connection conn = DBPool.getConnection();
             CallableStatement cs = conn.prepareCall(
                     "{call uspLawRegulationLegal_GetList(?,?,?)}")) {
            cs.setString(1, isUse); cs.setInt(2, 0); cs.setInt(3, 0);
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

    public List<Map<String, Object>> getRegulationList(String isUse, int llIdx) throws Exception {
        List<Map<String, Object>> rows = new ArrayList<>();
        try (Connection conn = DBPool.getConnection();
             CallableStatement cs = conn.prepareCall(
                     "{call uspLawRegulation_GetList(?,?,?)}")) {
            cs.setString(1, isUse); cs.setInt(2, llIdx); cs.setInt(3, 0);
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

    public List<Map<String, Object>> getNotifyList(String isUse, int lrIdx) throws Exception {
        List<Map<String, Object>> rows = new ArrayList<>();
        try (Connection conn = DBPool.getConnection();
             CallableStatement cs = conn.prepareCall(
                     "{call uspLawNotify_GetList(?,?,?)}")) {
            cs.setString(1, isUse); cs.setInt(2, lrIdx); cs.setInt(3, 0);
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

    // PutInfo: @ACTION, @IDX, @LN_IDX, @TITLE, @CONTENT, @IS_USE, @REG_USER, @REG_IP, @RTN_MSG(OUT), @RTN_IDX(OUT)
    public Map<String, Object> putInfo(
            String action, int idx, int lnIdx,
            String title, String content, String isUse,
            String regUser, String regIp) throws Exception {

        Map<String, Object> rtn = new LinkedHashMap<>();
        try (Connection conn = DBPool.getConnection();
             CallableStatement cs = conn.prepareCall(
                     "{call uspLawSafety_PutInfo(?,?,?,?,?,?,?,?,?,?)}")) {
            cs.setString(1, action);
            cs.setInt(2, idx);
            cs.setInt(3, lnIdx);
            cs.setString(4, title);
            cs.setString(5, content);
            cs.setString(6, isUse);
            cs.setString(7, regUser);
            cs.setString(8, regIp);
            cs.registerOutParameter(9, Types.NVARCHAR);
            cs.registerOutParameter(10, Types.INTEGER);
            cs.execute();
            String msg = cs.getString(9);
            rtn.put("rtn_msg", msg != null ? msg : "");
            rtn.put("rtn_idx", cs.getInt(10));
        }
        return rtn;
    }
}
