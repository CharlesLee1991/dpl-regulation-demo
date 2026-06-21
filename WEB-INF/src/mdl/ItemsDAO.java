package mdl;

import java.sql.*;
import java.util.*;
import db.DBPool;

/**
 * MODULE: items (품목정보 마스터 + 세부품목 디테일)
 * 원본: _system/mdl/m.items.asp + m.items_detail.asp
 * SP: uspLawItems_* (마스터) + uspLawItemsDetail_* (디테일)
 */
public class ItemsDAO {

    // ══ 마스터: 품목정보 (LI_IDX) ═══════════════════════════════════

    public Map<String, Object> getListPaging(
            int page, int listSize,
            String qKey, String qWord,
            int qLL, int qLR, int qLN, String qSort) throws Exception {

        Map<String, Object> result = new LinkedHashMap<>();
        List<Map<String, Object>> rows = new ArrayList<>();
        int total = 0;

        try (Connection conn = DBPool.getConnection();
             CallableStatement cs = conn.prepareCall(
                     "{call uspLawItems_GetListPaging(?,?,?,?,?,?,?,?)}")) {
            cs.setInt(1, page); cs.setInt(2, listSize);
            cs.setString(3, qKey); cs.setString(4, qWord);
            cs.setInt(5, qLL); cs.setInt(6, qLR); cs.setInt(7, qLN);
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
        result.put("rows", rows); result.put("total", total);
        return result;
    }

    public List<Map<String, Object>> getItemsList(String isUse, int lnIdx, int liIdx) throws Exception {
        List<Map<String, Object>> rows = new ArrayList<>();
        try (Connection conn = DBPool.getConnection();
             CallableStatement cs = conn.prepareCall("{call uspLawItems_GetList(?,?,?)}")) {
            cs.setString(1, isUse); cs.setInt(2, lnIdx); cs.setInt(3, liIdx);
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
             CallableStatement cs = conn.prepareCall("{call uspLawItems_GetInfo(?)}")) {
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

    // PutInfo: @ACTION,@IDX,@LEGAL_NAME,@LN_IDX,@SCOPE,@EXCEPTION,@LO_IDX,@LG_IDX,@IS_USE,@LS_IDXS,@REG_USER,@REG_IP,@RTN_MSG(OUT),@RTN_IDX(OUT)
    public Map<String, Object> putInfo(
            String action, int idx, String legalName, int lnIdx,
            String scope, String exception, int loIdx, int lgIdx,
            String isUse, String lsIdxs, String regUser, String regIp) throws Exception {

        Map<String, Object> rtn = new LinkedHashMap<>();
        try (Connection conn = DBPool.getConnection();
             CallableStatement cs = conn.prepareCall(
                     "{call uspLawItems_PutInfo(?,?,?,?,?,?,?,?,?,?,?,?,?,?)}")) {
            cs.setString(1, action); cs.setInt(2, idx);
            cs.setString(3, legalName); cs.setInt(4, lnIdx);
            cs.setString(5, scope); cs.setString(6, exception);
            cs.setInt(7, loIdx); cs.setInt(8, lgIdx);
            cs.setString(9, isUse); cs.setString(10, lsIdxs);
            cs.setString(11, regUser); cs.setString(12, regIp);
            cs.registerOutParameter(13, Types.NVARCHAR);
            cs.registerOutParameter(14, Types.INTEGER);
            cs.execute();
            String msg = cs.getString(13);
            rtn.put("rtn_msg", msg != null ? msg : "");
            rtn.put("rtn_idx", cs.getInt(14));
        }
        return rtn;
    }

    // ══ 디테일: 세부품목 (LD_IDX) ════════════════════════════════════

    public Map<String, Object> getDetailListPaging(
            int page, int listSize,
            String qKey, String qWord, String qSort,
            int qLL, int qLR, int qLN) throws Exception {

        Map<String, Object> result = new LinkedHashMap<>();
        List<Map<String, Object>> rows = new ArrayList<>();
        int total = 0;

        try (Connection conn = DBPool.getConnection();
             CallableStatement cs = conn.prepareCall(
                     "{call uspLawItemsDetail_GetListPaging(?,?,?,?,?,?,?,?,?,?,?,?,?)}")) {
            cs.setInt(1, page); cs.setInt(2, listSize);
            cs.setString(3, "admin");  // @qSite
            cs.setString(4, qKey); cs.setString(5, qWord);
            cs.setString(6, qSort.isEmpty() ? "" : qSort.toUpperCase());
            cs.setInt(7, 0);  // @qCate
            cs.setInt(8, 0);  // @qCate2
            cs.setInt(9, 0);  // @qCate3
            cs.setInt(10, qLL); cs.setInt(11, qLR); cs.setInt(12, qLN);
            cs.setString(13, "");  // @KeywordList

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
        result.put("rows", rows); result.put("total", total);
        return result;
    }

    public Map<String, Object> getDetailInfo(int idx) throws Exception {
        Map<String, Object> row = new LinkedHashMap<>();
        try (Connection conn = DBPool.getConnection();
             CallableStatement cs = conn.prepareCall("{call uspLawItemsDetail_GetInfo(?,?)}")) {
            cs.setString(1, "admin"); cs.setInt(2, idx);
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

    // PutInfo: @ACTION,@IDX,@ITEM_NAME,@LI_IDX,@USE_AGE,@MATERIAL,@LC_IDX,@SAMPLE_ATTA_PATH,@IS_USE,@REG_USER,@REG_IP,@LDK_KEYWORDS,@LC_CATEGORIES,@RTN_MSG(OUT),@RTN_IDX(OUT)
    public Map<String, Object> putDetailInfo(
            String action, int idx, String itemName, int liIdx,
            String useAge, String material, String isUse,
            String regUser, String regIp) throws Exception {

        Map<String, Object> rtn = new LinkedHashMap<>();
        try (Connection conn = DBPool.getConnection();
             CallableStatement cs = conn.prepareCall(
                     "{call uspLawItemsDetail_PutInfo(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}")) {
            cs.setString(1, action); cs.setInt(2, idx);
            cs.setString(3, itemName); cs.setInt(4, liIdx);
            cs.setString(5, useAge); cs.setString(6, material);
            cs.setInt(7, 0);      // @LC_IDX
            cs.setString(8, ""); // @SAMPLE_ATTA_PATH (데모: 파일업로드 생략)
            cs.setString(9, isUse);
            cs.setString(10, regUser); cs.setString(11, regIp);
            cs.setString(12, ""); // @LDK_KEYWORDS
            cs.setString(13, ""); // @LC_CATEGORIES
            cs.registerOutParameter(14, Types.NVARCHAR);
            cs.registerOutParameter(15, Types.INTEGER);
            cs.execute();
            String msg = cs.getString(14);
            rtn.put("rtn_msg", msg != null ? msg : "");
            rtn.put("rtn_idx", cs.getInt(15));
        }
        return rtn;
    }

    // ══ 공통 드롭다운 헬퍼 ════════════════════════════════════════════
    public List<Map<String, Object>> getLegalList(String isUse) throws Exception {
        List<Map<String, Object>> rows = new ArrayList<>();
        try (Connection conn = DBPool.getConnection();
             CallableStatement cs = conn.prepareCall("{call uspLawRegulationLegal_GetList(?,?,?)}")) {
            cs.setString(1, isUse); cs.setInt(2, 0); cs.setInt(3, 0);
            try (ResultSet rs = cs.executeQuery()) {
                ResultSetMetaData meta = rs.getMetaData(); int colCnt = meta.getColumnCount();
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    for (int i = 1; i <= colCnt; i++) row.put(meta.getColumnName(i).toLowerCase(), rs.getObject(i));
                    rows.add(row);
                }
            }
        }
        return rows;
    }

    public List<Map<String, Object>> getRegulationList(String isUse, int llIdx) throws Exception {
        List<Map<String, Object>> rows = new ArrayList<>();
        try (Connection conn = DBPool.getConnection();
             CallableStatement cs = conn.prepareCall("{call uspLawRegulation_GetList(?,?,?)}")) {
            cs.setString(1, isUse); cs.setInt(2, llIdx); cs.setInt(3, 0);
            try (ResultSet rs = cs.executeQuery()) {
                ResultSetMetaData meta = rs.getMetaData(); int colCnt = meta.getColumnCount();
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    for (int i = 1; i <= colCnt; i++) row.put(meta.getColumnName(i).toLowerCase(), rs.getObject(i));
                    rows.add(row);
                }
            }
        }
        return rows;
    }

    public List<Map<String, Object>> getNotifyList(String isUse, int lrIdx) throws Exception {
        List<Map<String, Object>> rows = new ArrayList<>();
        try (Connection conn = DBPool.getConnection();
             CallableStatement cs = conn.prepareCall("{call uspLawNotify_GetList(?,?,?)}")) {
            cs.setString(1, isUse); cs.setInt(2, lrIdx); cs.setInt(3, 0);
            try (ResultSet rs = cs.executeQuery()) {
                ResultSetMetaData meta = rs.getMetaData(); int colCnt = meta.getColumnCount();
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    for (int i = 1; i <= colCnt; i++) row.put(meta.getColumnName(i).toLowerCase(), rs.getObject(i));
                    rows.add(row);
                }
            }
        }
        return rows;
    }
}
