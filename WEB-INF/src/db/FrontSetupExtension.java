package db;

import java.sql.*;

/**
 * 프론트엔드 전용 테이블 (위해정보/스탠다드/셀프러닝) 초기화
 */
public class FrontSetupExtension {

    public static void init(Connection conn) throws Exception {
        createTables(conn);
        seedData(conn);   // v5.3 no-op (미러가 실데이터 공급)
        // ── 카테고리 (원본 LAW_CATEGORY 컬럼 미러 — 중분류 depth1 / 소분류 depth2) ──
        // v5.3: LC_ROOT_IDX는 원본에 없는 자체 컬럼이었음 → 제거하고 원본 11컬럼으로 정정
        dropIfOldSchema(conn, "dpl_law_category", "LC_ROOT_IDX");
        exec(conn, """
            IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME='dpl_law_category')
            CREATE TABLE dpl_law_category (
                LC_IDX INT IDENTITY(1,1) PRIMARY KEY,
                LC_DEPTH INT DEFAULT 1,
                LC_CATEGORY NVARCHAR(MAX),
                LC_PARENT_IDX INT DEFAULT 0,
                LC_IS_USE NVARCHAR(10) DEFAULT 'Y',
                LC_SORT INT DEFAULT 0,
                LC_REG_USER NVARCHAR(MAX),
                LC_REG_DATE DATETIME DEFAULT GETDATE(),
                LC_UPD_USER NVARCHAR(MAX),
                LC_UPD_DATE DATETIME,
                LC_ICODE NVARCHAR(MAX)
            )""");
        // v5.3: 카테고리 시드 제거 — 원본 LAW_CATEGORY(67행) 미러로 대체
    }

    private static void createTables(Connection conn) throws Exception {
        // ── v5.3 마이그레이션: 구 스키마(자체 컬럼명 RD_*/ST_*/SC_*) 테이블 제거 ──
        // 사유: 원본 LAW_RISKDB=LR_* / LAW_STANDARD=LS_* / LAW_SHORTCLASS=LS_* 이므로
        //       구 컬럼명은 mirror_reload 교집합이 0 → 미러 자체가 불가능.
        //       "신규테이블은 원본 컬럼명 그대로 미러" 원칙 복원. 시드 데이터는 폐기 대상.
        dropIfOldSchema(conn, "dpl_riskdb",     "RD_IDX");
        dropIfOldSchema(conn, "dpl_standard",   "ST_IDX");
        dropIfOldSchema(conn, "dpl_shortclass", "SC_IDX");
        // 원본 LAW_RISKDB 컬럼 미러
        exec(conn, """
            IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME='dpl_riskdb')
            CREATE TABLE dpl_riskdb (
                LR_IDX INT IDENTITY(1,1) PRIMARY KEY,
                LS_IDX INT DEFAULT 0,
                LR_TITLE NVARCHAR(MAX),
                LR_TYPE NVARCHAR(MAX),
                LR_FACTOR NVARCHAR(MAX),
                LR_CONTENTS NVARCHAR(MAX),
                LR_LEVEL INT DEFAULT 1,
                LR_URL NVARCHAR(MAX),
                LR_SOURCE NVARCHAR(MAX),
                LR_REG_DATE DATETIME DEFAULT GETDATE(),
                LR_REG_USER NVARCHAR(MAX),
                LR_REG_IP NVARCHAR(MAX),
                LR_UPD_DATE DATETIME,
                LR_UPD_USER NVARCHAR(MAX),
                LR_UPD_IP NVARCHAR(MAX),
                LS_REG_DATE DATETIME
            )""");
        // 원본 LAW_STANDARD 컬럼 미러
        exec(conn, """
            IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME='dpl_standard')
            CREATE TABLE dpl_standard (
                LS_IDX INT IDENTITY(1,1) PRIMARY KEY,
                LS_CODE NVARCHAR(MAX),
                LS_DIV NVARCHAR(MAX),
                LS_REV_DATE NVARCHAR(MAX),
                LS_TITLE NVARCHAR(MAX),
                LS_ITEMS NVARCHAR(MAX),
                LS_ATTA NVARCHAR(MAX),
                LS_ATTA_PATH NVARCHAR(MAX),
                LS_REG_DATE DATETIME DEFAULT GETDATE(),
                LS_REG_USER NVARCHAR(MAX),
                LS_REG_IP NVARCHAR(MAX),
                LS_UPD_DATE DATETIME,
                LS_UPD_USER NVARCHAR(MAX),
                LS_UPD_IP NVARCHAR(MAX)
            )""");
        // 원본 LAW_SHORTCLASS 컬럼 미러
        exec(conn, """
            IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME='dpl_shortclass')
            CREATE TABLE dpl_shortclass (
                LS_IDX INT IDENTITY(1,1) PRIMARY KEY,
                LS_DIV NVARCHAR(MAX),
                LS_TYPE NVARCHAR(MAX),
                LS_TITLE NVARCHAR(MAX),
                LS_CONTENTS NVARCHAR(MAX),
                LS_THUMB NVARCHAR(MAX),
                LS_THUMB_PATH NVARCHAR(MAX),
                LS_ATTA NVARCHAR(MAX),
                LS_ATTA_PATH NVARCHAR(MAX),
                LS_RECYN NVARCHAR(10) DEFAULT 'N',
                LS_SHOWYN NVARCHAR(10) DEFAULT 'Y',
                LS_REG_DATE DATETIME DEFAULT GETDATE(),
                LS_REG_USER NVARCHAR(MAX),
                LS_REG_IP NVARCHAR(MAX),
                LS_UPD_DATE DATETIME,
                LS_UPD_USER NVARCHAR(MAX),
                LS_UPD_IP NVARCHAR(MAX),
                LS_HIT INT DEFAULT 0,
                LN_IDX INT DEFAULT 0
            )""");
        // 원본 LAW_GRADEMARK 컬럼 미러 (메인 카운트 5중 조인에 필요 — v5.3 신규)
        exec(conn, """
            IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME='dpl_grademark')
            CREATE TABLE dpl_grademark (
                LG_IDX INT IDENTITY(1,1) PRIMARY KEY,
                LG_IS_USE NVARCHAR(10) DEFAULT 'Y'
            )""");
        // ── v5.3: dpl_law_safety 폐기 ──────────────────────────────────
        // 원본 제품안전뉴스 = ctrlBoard(BBS_LEGAL_SAFETY=2) → LAW_BOARD BD_CODE=2.
        // 원본 LAW_SAFETY는 LN_IDX를 가진 고시(NOTIFY) 종속 테이블로 뉴스와 무관 —
        // v4.3에서 테이블명만 보고 오인해 자체 신설했던 것. 제거하고 dpl_law_board로 통합.
        exec(conn, """
            IF EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME='dpl_law_safety')
            DROP TABLE dpl_law_safety""");
        // ── 게시판 (원본 law_board 컬럼 전체 미러) ──
        // BD_CODE: 1=법규 제·개정 / 2=제품안전뉴스(글로벌 안전정보) / 6=유용한정보 / 8=동영상 / 10=안전센터
        // v5.3: 컬럼 길이는 원본 실측 기준 넉넉히 (BD_SUBTITLE/BD_CONTENTS = 원본 text 형)
        dropIfOldSchema(conn, "dpl_law_board", "BD_ETC_COLS_4", true);  // 구 12컬럼 스키마면 DROP
        exec(conn, """
            IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME='dpl_law_board')
            CREATE TABLE dpl_law_board (
                BD_IDX INT IDENTITY(1,1) PRIMARY KEY,
                BD_CODE INT NOT NULL,
                BD_TITLE NVARCHAR(MAX),
                BD_HIT INT DEFAULT 0,
                BD_WRITER NVARCHAR(MAX) DEFAULT N'관리자',
                BD_IS_ANSWER NVARCHAR(MAX) DEFAULT 'N',
                BD_ETC_COLS_1 NVARCHAR(MAX),
                BD_ETC_COLS_2 NVARCHAR(MAX),
                BD_ETC_COLS_3 NVARCHAR(MAX),
                BD_ETC_COLS_4 NVARCHAR(MAX),
                BD_ETC_COLS_5 NVARCHAR(MAX),
                BD_ETC_COLS_6 NVARCHAR(MAX),
                BD_ETC_COLS_7 NVARCHAR(MAX),
                BD_ETC_COLS_8 NVARCHAR(MAX),
                BD_ETC_COLS_9 NVARCHAR(MAX),
                BD_ETC_COLS_10 NVARCHAR(MAX),
                BD_SUBTITLE NVARCHAR(MAX),
                BD_CONTENTS NVARCHAR(MAX),
                BD_REG_USER NVARCHAR(MAX),
                BD_REG_DATE DATETIME DEFAULT GETDATE(),
                BD_REG_IP NVARCHAR(MAX),
                BD_UPD_USER NVARCHAR(MAX),
                BD_UPD_DATE DATETIME,
                BD_UPD_IP NVARCHAR(MAX)
            )""");
        System.out.println("[DPL] 프론트 테이블 생성 완료");
    }

    /** 구 스키마 감지 시 DROP (해당 컬럼이 존재하면 구 스키마로 판단) */
    private static void dropIfOldSchema(Connection conn, String table, String oldCol) throws Exception {
        dropIfOldSchema(conn, table, oldCol, false);
    }
    private static void dropIfOldSchema(Connection conn, String table, String col, boolean dropWhenMissing) throws Exception {
        String cond = dropWhenMissing ? "NOT EXISTS" : "EXISTS";
        exec(conn, "IF EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME='"+table+"') "
                 + "AND "+cond+" (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME='"+table+"' AND COLUMN_NAME='"+col+"') "
                 + "DROP TABLE "+table);
    }

    // ── v5.3: 시드 전량 폐기 ────────────────────────────────────────
    // 프론트 전 화면이 mirror_reload로 실 LAW_* 데이터를 서빙하므로
    // 하드코딩 시드는 불필요할 뿐 아니라 실데이터와 혼재해 오염을 유발.
    // (v5.2까지 F4/F5/F6/게시판/뉴스가 시드를 보고 있던 것이 버그의 원인)
    private static void seedData(Connection conn) throws Exception { /* no-op */ }

    private static void exec(Connection conn, String sql) throws Exception {
        try (Statement st = conn.createStatement()) { st.execute(sql.strip()); }
    }
}
