package db;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.sql.*;

/**
 * 앱 기동 시 regulation 모듈용 SP 더미 자동 생성
 * 실 LSAFE DB에서는 실 SP 사용 — 여기서는 데모 DPL_DEMO DB용
 */
@WebListener
public class RegulationSetupListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("[DPL] RegulationSetupListener: SP 생성 시작");
        try (Connection conn = DBPool.getConnection()) {
            createSPs(conn);
            seedData(conn);
            System.out.println("[DPL] RegulationSetupListener: 완료");
        } catch (Exception e) {
            System.err.println("[DPL] RegulationSetupListener 실패: " + e.getMessage());
        }
    }

    private void createSPs(Connection conn) throws Exception {
        String[] drops = {
            "IF OBJECT_ID('uspLawRegulation_GetListPaging','P') IS NOT NULL DROP PROCEDURE uspLawRegulation_GetListPaging",
            "IF OBJECT_ID('uspLawRegulation_GetList','P') IS NOT NULL DROP PROCEDURE uspLawRegulation_GetList",
            "IF OBJECT_ID('uspLawRegulation_GetInfo','P') IS NOT NULL DROP PROCEDURE uspLawRegulation_GetInfo",
            "IF OBJECT_ID('uspLawRegulation_PutInfo','P') IS NOT NULL DROP PROCEDURE uspLawRegulation_PutInfo",
            "IF OBJECT_ID('uspLawRegulationLegal_GetList','P') IS NOT NULL DROP PROCEDURE uspLawRegulationLegal_GetList",
        };
        for (String sql : drops) {
            try (Statement st = conn.createStatement()) { st.execute(sql); }
        }

        // 테이블
        try (Statement st = conn.createStatement()) {
            st.execute("""
                IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME='dpl_regulation_legal')
                CREATE TABLE dpl_regulation_legal (
                    LL_IDX   INT IDENTITY(1,1) PRIMARY KEY,
                    LL_TITLE NVARCHAR(200) NOT NULL,
                    LL_IS_USE CHAR(1) DEFAULT 'Y',
                    LL_SORT  INT DEFAULT 0,
                    REG_DATE DATETIME DEFAULT GETDATE()
                )
            """);
            st.execute("""
                IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME='dpl_regulation')
                CREATE TABLE dpl_regulation (
                    LR_IDX           INT IDENTITY(1,1) PRIMARY KEY,
                    LR_TITLE         NVARCHAR(300) NOT NULL,
                    LL_IDX           INT NOT NULL DEFAULT 0,
                    LR_CONDITION     NVARCHAR(MAX),
                    LR_CERTIFY_GUIDE NVARCHAR(MAX),
                    LR_PENALTY       NVARCHAR(MAX),
                    LR_IS_USE        CHAR(1) DEFAULT 'Y',
                    REG_USER         NVARCHAR(50),
                    REG_IP           NVARCHAR(40),
                    REG_DATE         DATETIME DEFAULT GETDATE(),
                    UPD_USER         NVARCHAR(50),
                    UPD_DATE         DATETIME
                )
            """);
        }

        // SP: GetListPaging (패턴 A, 멀티RS: 목록 + total) — LL_TITLE JOIN 포함
        try (Statement st = conn.createStatement()) {
            st.execute("IF OBJECT_ID('uspLawRegulation_GetListPaging','P') IS NOT NULL DROP PROCEDURE uspLawRegulation_GetListPaging");
        }
        try (Statement st = conn.createStatement()) {
            st.execute("""
                CREATE PROCEDURE uspLawRegulation_GetListPaging
                    @Page     INT = 1,
                    @ListSize INT = 20,
                    @qKey     NVARCHAR(50) = '',
                    @qWord    NVARCHAR(200) = '',
                    @qLL      INT = 0,
                    @qSort    NVARCHAR(50) = ''
                AS
                BEGIN
                    SET NOCOUNT ON;
                    DECLARE @Offset INT = (@Page - 1) * @ListSize;

                    SELECT
                        r.LR_IDX, r.LR_TITLE, r.LL_IDX,
                        ISNULL(l.LL_TITLE, '') AS LL_TITLE,
                        r.LR_IS_USE,
                        CONVERT(NVARCHAR(10), r.REG_DATE, 120) AS LR_REG_DATE,
                        r.REG_USER AS LR_REG_USER,
                        CONVERT(NVARCHAR(10), r.UPD_DATE, 120) AS LR_UPD_DATE,
                        r.UPD_USER AS LR_UPD_USER
                    FROM dpl_regulation r
                    LEFT JOIN dpl_regulation_legal l ON l.LL_IDX = r.LL_IDX
                    WHERE 1=1
                      AND (@qLL   = 0 OR r.LL_IDX = @qLL)
                      AND (@qWord = '' OR (
                            (@qKey = 'TITLE' AND r.LR_TITLE LIKE '%' + @qWord + '%') OR
                            (@qKey = ''      AND r.LR_TITLE LIKE '%' + @qWord + '%')
                      ))
                    ORDER BY r.LR_IDX DESC
                    OFFSET @Offset ROWS FETCH NEXT @ListSize ROWS ONLY;

                    SELECT COUNT(*) AS TOTAL
                    FROM dpl_regulation r
                    WHERE 1=1
                      AND (@qLL   = 0 OR r.LL_IDX = @qLL)
                      AND (@qWord = '' OR r.LR_TITLE LIKE '%' + @qWord + '%');
                END
            """);
        }

        // SP: GetList (전체 목록, 드롭다운용)
        try (Statement st = conn.createStatement()) {
            st.execute("""
                CREATE PROCEDURE uspLawRegulation_GetList
                    @qIsUse  CHAR(1) = 'Y',
                    @qParent INT = 0,
                    @qIdx    INT = 0
                AS
                BEGIN
                    SET NOCOUNT ON;
                    SELECT LR_IDX, LR_TITLE, LL_IDX, LR_IS_USE
                    FROM dpl_regulation
                    WHERE (@qIsUse = '' OR LR_IS_USE = @qIsUse)
                    ORDER BY LR_TITLE;
                END
            """);
        }

        // SP: GetInfo (단건 상세)
        try (Statement st = conn.createStatement()) {
            st.execute("""
                CREATE PROCEDURE uspLawRegulation_GetInfo
                    @IDX INT
                AS
                BEGIN
                    SET NOCOUNT ON;
                    SELECT
                        LR_IDX, LR_TITLE, LL_IDX,
                        LR_CONDITION, LR_CERTIFY_GUIDE, LR_PENALTY,
                        LR_IS_USE,
                        REG_USER AS LR_REG_USER,
                        CONVERT(NVARCHAR(19), REG_DATE, 120) AS LR_REG_DATE,
                        UPD_USER AS LR_UPD_USER,
                        CONVERT(NVARCHAR(19), UPD_DATE, 120) AS LR_UPD_DATE
                    FROM dpl_regulation
                    WHERE LR_IDX = @IDX;
                END
            """);
        }

        // SP: PutInfo (ADD/MOD/DEL — 패턴 C, OUTPUT)
        try (Statement st = conn.createStatement()) {
            st.execute("""
                CREATE PROCEDURE uspLawRegulation_PutInfo
                    @ACTION          NVARCHAR(10),
                    @IDX             INT = 0,
                    @TITLE           NVARCHAR(300),
                    @LL_IDX          INT = 0,
                    @CONDITION       NVARCHAR(MAX) = '',
                    @CERTIFY_GUIDE   NVARCHAR(MAX) = '',
                    @PENALTY         NVARCHAR(MAX) = '',
                    @IS_USE          CHAR(1) = 'Y',
                    @REG_USER        NVARCHAR(50) = '',
                    @REG_IP          NVARCHAR(40) = '',
                    @RTN_MSG         NVARCHAR(500) OUTPUT,
                    @RTN_IDX         INT OUTPUT
                AS
                BEGIN
                    SET NOCOUNT ON;
                    SET @RTN_MSG = '';
                    SET @RTN_IDX = @IDX;

                    IF @ACTION = 'ADD'
                    BEGIN
                        IF @TITLE = '' BEGIN SET @RTN_MSG = '제목을 입력하세요.'; RETURN; END
                        INSERT INTO dpl_regulation
                            (LR_TITLE, LL_IDX, LR_CONDITION, LR_CERTIFY_GUIDE, LR_PENALTY,
                             LR_IS_USE, REG_USER, REG_IP, REG_DATE)
                        VALUES
                            (@TITLE, @LL_IDX, @CONDITION, @CERTIFY_GUIDE, @PENALTY,
                             @IS_USE, @REG_USER, @REG_IP, GETDATE());
                        SET @RTN_IDX = SCOPE_IDENTITY();
                    END
                    ELSE IF @ACTION = 'MOD'
                    BEGIN
                        UPDATE dpl_regulation SET
                            LR_TITLE         = @TITLE,
                            LL_IDX           = @LL_IDX,
                            LR_CONDITION     = @CONDITION,
                            LR_CERTIFY_GUIDE = @CERTIFY_GUIDE,
                            LR_PENALTY       = @PENALTY,
                            LR_IS_USE        = @IS_USE,
                            UPD_USER         = @REG_USER,
                            UPD_DATE         = GETDATE()
                        WHERE LR_IDX = @IDX;
                    END
                    ELSE IF @ACTION = 'DEL'
                    BEGIN
                        DELETE FROM dpl_regulation WHERE LR_IDX = @IDX;
                    END
                END
            """);
        }

        // SP: 규제법률 목록 (드롭다운용 — regulation_legal 모듈 일부)
        try (Statement st = conn.createStatement()) {
            st.execute("""
                CREATE PROCEDURE uspLawRegulationLegal_GetList
                    @qIsUse  CHAR(1) = 'Y',
                    @qParent INT = 0,
                    @qIdx    INT = 0
                AS
                BEGIN
                    SET NOCOUNT ON;
                    SELECT LL_IDX, LL_TITLE, LL_IS_USE
                    FROM dpl_regulation_legal
                    WHERE LL_IS_USE = @qIsUse
                    ORDER BY LL_SORT, LL_IDX;
                END
            """);
        }

        System.out.println("[DPL] regulation SP 5종 생성 완료");
    }

    private void seedData(Connection conn) throws Exception {
        // regulation_legal 시드 (드롭다운 데이터)
        try (Statement st = conn.createStatement()) {
            ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM dpl_regulation_legal");
            rs.next();
            if (rs.getInt(1) == 0) {
                st.execute("""
                    INSERT INTO dpl_regulation_legal (LL_TITLE, LL_IS_USE, LL_SORT) VALUES
                    (N'산업안전보건법', 'Y', 1),
                    (N'화학물질관리법', 'Y', 2),
                    (N'중대재해처벌법', 'Y', 3),
                    (N'위험물안전관리법', 'Y', 4),
                    (N'소방시설법', 'Y', 5)
                """);
                System.out.println("[DPL] regulation_legal 시드 5건 완료");
            }
        }

        // regulation 시드
        try (Statement st = conn.createStatement()) {
            ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM dpl_regulation");
            rs.next();
            if (rs.getInt(1) == 0) {
                st.execute("""
                    INSERT INTO dpl_regulation
                        (LR_TITLE, LL_IDX, LR_CONDITION, LR_CERTIFY_GUIDE, LR_PENALTY, LR_IS_USE, REG_USER, REG_DATE)
                    VALUES
                        (N'안전보건관리체계 구축', 1, N'상시근로자 50인 이상 사업장', N'안전보건관리규정 작성 및 비치', N'1,000만원 이하 과태료', 'Y', 'admin', GETDATE()),
                        (N'화학물질 취급시설 설치기준', 2, N'유해화학물질 취급량 기준 이상', N'취급시설 설치검사 수검', N'2년 이하 징역 또는 5,000만원 이하 벌금', 'Y', 'admin', GETDATE()),
                        (N'중대재해 발생 보고', 3, N'중대산업재해 발생 시', N'즉시 고용노동부 보고', N'1억원 이하 과태료', 'Y', 'admin', GETDATE()),
                        (N'위험물 저장소 설치 허가', 4, N'지정수량 이상 위험물 저장', N'소방서 허가 취득', N'3년 이하 징역 또는 3,000만원 이하 벌금', 'Y', 'admin', GETDATE()),
                        (N'소방시설 자체점검', 5, N'특정소방대상물', N'연 1회 이상 자체점검 실시', N'300만원 이하 과태료', 'Y', 'admin', GETDATE())
                """);
                System.out.println("[DPL] regulation 시드 5건 완료");
            }
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {}
}
