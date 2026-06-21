package db;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.sql.*;

/**
 * 앱 기동 시 regulation·legal·notify 모듈용 SP 자동 생성
 * 실 LSAFE DB에서는 실 SP 사용 — 여기서는 데모 DPL_DEMO DB용
 */
@WebListener
public class RegulationSetupListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("[DPL] RegulationSetupListener: SP 생성 시작");
        try (Connection conn = DBPool.getConnection()) {
            createRegulationSPs(conn);
            seedRegulationData(conn);
            createNotifySPs(conn);
            seedNotifyData(conn);
            System.out.println("[DPL] RegulationSetupListener: 완료");
        } catch (Exception e) {
            System.err.println("[DPL] RegulationSetupListener 실패: " + e.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════════
    // REGULATION + LEGAL SP (기존)
    // ══════════════════════════════════════════════════════════
    private void createRegulationSPs(Connection conn) throws Exception {
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
        }

        try (Statement st = conn.createStatement()) {
            st.execute("IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME='dpl_regulation_legal' AND COLUMN_NAME='LL_DEPT') ALTER TABLE dpl_regulation_legal ADD LL_DEPT NVARCHAR(100) DEFAULT ''");
            st.execute("IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME='dpl_regulation_legal' AND COLUMN_NAME='REG_USER') ALTER TABLE dpl_regulation_legal ADD REG_USER NVARCHAR(50) DEFAULT ''");
        }

        try (Statement st = conn.createStatement()) {
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
                    SELECT COUNT(*) AS TOTAL FROM dpl_regulation r
                    WHERE 1=1
                      AND (@qLL   = 0 OR r.LL_IDX = @qLL)
                      AND (@qWord = '' OR r.LR_TITLE LIKE '%' + @qWord + '%');
                END
            """);
        }

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

        try (Statement st = conn.createStatement()) {
            st.execute("""
                CREATE PROCEDURE uspLawRegulation_GetInfo
                    @IDX INT
                AS
                BEGIN
                    SET NOCOUNT ON;
                    SELECT
                        r.LR_IDX, r.LR_TITLE, r.LL_IDX,
                        ISNULL(l.LL_TITLE, '') AS LL_TITLE,
                        r.LR_CONDITION, r.LR_CERTIFY_GUIDE, r.LR_PENALTY,
                        r.LR_IS_USE,
                        r.REG_USER AS LR_REG_USER,
                        CONVERT(NVARCHAR(19), r.REG_DATE, 120) AS LR_REG_DATE,
                        r.UPD_USER AS LR_UPD_USER,
                        CONVERT(NVARCHAR(19), r.UPD_DATE, 120) AS LR_UPD_DATE
                    FROM dpl_regulation r
                    LEFT JOIN dpl_regulation_legal l ON l.LL_IDX = r.LL_IDX
                    WHERE r.LR_IDX = @IDX;
                END
            """);
        }

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
                    SET @RTN_MSG = ''; SET @RTN_IDX = @IDX;
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
                            LR_TITLE=@TITLE, LL_IDX=@LL_IDX, LR_CONDITION=@CONDITION,
                            LR_CERTIFY_GUIDE=@CERTIFY_GUIDE, LR_PENALTY=@PENALTY,
                            LR_IS_USE=@IS_USE, UPD_USER=@REG_USER, UPD_DATE=GETDATE()
                        WHERE LR_IDX = @IDX;
                    END
                    ELSE IF @ACTION = 'DEL'
                    BEGIN
                        DELETE FROM dpl_regulation WHERE LR_IDX = @IDX;
                    END
                END
            """);
        }

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

    private void seedRegulationData(Connection conn) throws Exception {
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

        // Legal SP 3종
        try (Statement st = conn.createStatement()) {
            st.execute("IF OBJECT_ID('uspLawRegulationLegal_GetListPaging','P') IS NOT NULL DROP PROCEDURE uspLawRegulationLegal_GetListPaging");
        }
        try (Statement st = conn.createStatement()) {
            st.execute("""
                CREATE PROCEDURE uspLawRegulationLegal_GetListPaging
                    @Page INT=1, @ListSize INT=20,
                    @qKey NVARCHAR(50)='', @qWord NVARCHAR(200)='', @qSort NVARCHAR(50)=''
                AS BEGIN
                    SET NOCOUNT ON;
                    DECLARE @Offset INT = (@Page-1)*@ListSize;
                    SELECT LL_IDX, LL_TITLE, ISNULL(LL_DEPT,'') AS LL_DEPT,
                           LL_IS_USE, CONVERT(NVARCHAR(10),REG_DATE,120) AS LL_REG_DATE,
                           REG_USER AS LL_REG_USER
                    FROM dpl_regulation_legal
                    WHERE (@qWord='' OR (
                           (@qKey='TITLE' AND LL_TITLE LIKE '%'+@qWord+'%') OR
                           (@qKey='DEPT'  AND LL_DEPT  LIKE '%'+@qWord+'%') OR
                           (@qKey=''      AND (LL_TITLE LIKE '%'+@qWord+'%' OR LL_DEPT LIKE '%'+@qWord+'%'))))
                    ORDER BY LL_IDX DESC OFFSET @Offset ROWS FETCH NEXT @ListSize ROWS ONLY;
                    SELECT COUNT(*) AS TOTAL FROM dpl_regulation_legal
                    WHERE (@qWord='' OR (
                           (@qKey='TITLE' AND LL_TITLE LIKE '%'+@qWord+'%') OR
                           (@qKey='DEPT'  AND LL_DEPT  LIKE '%'+@qWord+'%') OR
                           (@qKey=''      AND (LL_TITLE LIKE '%'+@qWord+'%' OR LL_DEPT LIKE '%'+@qWord+'%'))));
                END
            """);
        }
        try (Statement st = conn.createStatement()) {
            st.execute("IF OBJECT_ID('uspLawRegulationLegal_GetInfo','P') IS NOT NULL DROP PROCEDURE uspLawRegulationLegal_GetInfo");
        }
        try (Statement st = conn.createStatement()) {
            st.execute("""
                CREATE PROCEDURE uspLawRegulationLegal_GetInfo @IDX INT
                AS BEGIN
                    SET NOCOUNT ON;
                    SELECT LL_IDX, LL_TITLE, ISNULL(LL_DEPT,'') AS LL_DEPT, LL_IS_USE,
                           REG_USER AS LL_REG_USER,
                           CONVERT(NVARCHAR(19),REG_DATE,120) AS LL_REG_DATE
                    FROM dpl_regulation_legal WHERE LL_IDX=@IDX;
                END
            """);
        }
        try (Statement st = conn.createStatement()) {
            st.execute("IF OBJECT_ID('uspLawRegulationLegal_PutInfo','P') IS NOT NULL DROP PROCEDURE uspLawRegulationLegal_PutInfo");
        }
        try (Statement st = conn.createStatement()) {
            st.execute("""
                CREATE PROCEDURE uspLawRegulationLegal_PutInfo
                    @ACTION NVARCHAR(10), @IDX INT=0, @TITLE NVARCHAR(200),
                    @DEPT NVARCHAR(100)='', @IS_USE CHAR(1)='Y',
                    @REG_USER NVARCHAR(50)='', @REG_IP NVARCHAR(40)='',
                    @RTN_MSG NVARCHAR(500) OUTPUT
                AS BEGIN
                    SET NOCOUNT ON; SET @RTN_MSG='';
                    IF @ACTION='ADD'
                    BEGIN
                        IF @TITLE='' BEGIN SET @RTN_MSG='법규명을 입력하세요.'; RETURN; END
                        IF @DEPT ='' BEGIN SET @RTN_MSG='관리부처를 입력하세요.'; RETURN; END
                        INSERT INTO dpl_regulation_legal (LL_TITLE,LL_DEPT,LL_IS_USE,LL_SORT,REG_DATE)
                        VALUES (@TITLE,@DEPT,@IS_USE,0,GETDATE());
                    END
                    ELSE IF @ACTION='MOD'
                        UPDATE dpl_regulation_legal SET LL_TITLE=@TITLE,LL_DEPT=@DEPT,LL_IS_USE=@IS_USE WHERE LL_IDX=@IDX;
                    ELSE IF @ACTION='DEL'
                        DELETE FROM dpl_regulation_legal WHERE LL_IDX=@IDX;
                END
            """);
        }

        try (Statement st = conn.createStatement()) {
            ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM dpl_regulation");
            rs.next();
            if (rs.getInt(1) == 0) {
                st.execute("""
                    INSERT INTO dpl_regulation
                        (LR_TITLE, LL_IDX, LR_CONDITION, LR_CERTIFY_GUIDE, LR_PENALTY, LR_IS_USE, REG_USER, REG_DATE)
                    VALUES
                        (N'안전보건관리체계 구축', 1, N'상시근로자 50인 이상 사업장', N'안전보건관리규정 작성 및 비치', N'1,000만원 이하 과태료', 'Y', 'admin', GETDATE()),
                        (N'화학물질 취급시설 설치기준', 2, N'유해화학물질 취급량 기준 이상', N'취급시설 설치검사 수검', N'5,000만원 이하 벌금', 'Y', 'admin', GETDATE()),
                        (N'중대재해 발생 보고', 3, N'중대산업재해 발생 시', N'즉시 고용노동부 보고', N'1억원 이하 과태료', 'Y', 'admin', GETDATE()),
                        (N'위험물 저장소 설치 허가', 4, N'지정수량 이상 위험물 저장', N'소방서 허가 취득', N'3,000만원 이하 벌금', 'Y', 'admin', GETDATE()),
                        (N'소방시설 자체점검', 5, N'특정소방대상물', N'연 1회 이상 자체점검 실시', N'300만원 이하 과태료', 'Y', 'admin', GETDATE())
                """);
                System.out.println("[DPL] regulation 시드 5건 완료");
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // NOTIFY SP (신규)
    // ══════════════════════════════════════════════════════════
    private void createNotifySPs(Connection conn) throws Exception {
        // 테이블 생성
        try (Statement st = conn.createStatement()) {
            st.execute("""
                IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME='dpl_notify')
                CREATE TABLE dpl_notify (
                    LN_IDX       INT IDENTITY(1,1) PRIMARY KEY,
                    LN_TITLE     NVARCHAR(200) NOT NULL,
                    LL_IDX       INT NOT NULL DEFAULT 0,
                    LR_IDX       INT NOT NULL DEFAULT 0,
                    LN_HISTORY   NVARCHAR(MAX) NULL,
                    LN_IS_USE    CHAR(1) NOT NULL DEFAULT 'Y',
                    LN_REG_USER  NVARCHAR(50) NULL,
                    LN_REG_IP    NVARCHAR(40) NULL,
                    LN_REG_DATE  DATETIME DEFAULT GETDATE(),
                    LN_UPD_USER  NVARCHAR(50) NULL,
                    LN_UPD_IP    NVARCHAR(40) NULL,
                    LN_UPD_DATE  DATETIME NULL
                )
            """);
        }

        // SP DROP
        for (String sp : new String[]{
                "uspLawNotify_GetListPaging","uspLawNotify_GetList",
                "uspLawNotify_GetInfo","uspLawNotify_PutInfo"}) {
            try (Statement st = conn.createStatement()) {
                st.execute("IF OBJECT_ID('" + sp + "','P') IS NOT NULL DROP PROCEDURE " + sp);
            }
        }

        // GetListPaging
        try (Statement st = conn.createStatement()) {
            st.execute("""
                CREATE PROCEDURE uspLawNotify_GetListPaging
                    @Page     INT = 1,
                    @ListSize INT = 20,
                    @qKey     NVARCHAR(50)  = '',
                    @qWord    NVARCHAR(200) = '',
                    @qLL      INT = 0,
                    @qLR      INT = 0,
                    @qSort    NVARCHAR(50)  = ''
                AS
                BEGIN
                    SET NOCOUNT ON;
                    DECLARE @Offset INT = (@Page - 1) * @ListSize;
                    SELECT
                        n.LN_IDX,
                        n.LN_TITLE,
                        n.LN_TITLE     AS LN_NOTIFY,
                        ISNULL(l.LL_TITLE, '') AS LL_TITLE,
                        ISNULL(r.LR_TITLE, '') AS LR_TITLE,
                        n.LN_IS_USE,
                        CONVERT(NVARCHAR(10), n.LN_REG_DATE, 120) AS LN_REG_DATE
                    FROM dpl_notify n
                    LEFT JOIN dpl_regulation_legal l ON l.LL_IDX = n.LL_IDX
                    LEFT JOIN dpl_regulation       r ON r.LR_IDX = n.LR_IDX
                    WHERE 1=1
                      AND (@qLL = 0 OR n.LL_IDX = @qLL)
                      AND (@qLR = 0 OR n.LR_IDX = @qLR)
                      AND (@qWord = '' OR (
                            @qKey = 'TITLE' AND n.LN_TITLE LIKE '%' + @qWord + '%') OR
                            (@qKey = ''     AND n.LN_TITLE LIKE '%' + @qWord + '%'))
                    ORDER BY n.LN_IDX DESC
                    OFFSET @Offset ROWS FETCH NEXT @ListSize ROWS ONLY;
                    SELECT COUNT(1) AS TOTAL
                    FROM dpl_notify n
                    WHERE 1=1
                      AND (@qLL = 0 OR n.LL_IDX = @qLL)
                      AND (@qLR = 0 OR n.LR_IDX = @qLR)
                      AND (@qWord = '' OR n.LN_TITLE LIKE '%' + @qWord + '%');
                END
            """);
        }

        // GetList
        try (Statement st = conn.createStatement()) {
            st.execute("""
                CREATE PROCEDURE uspLawNotify_GetList
                    @qIsUse  CHAR(1) = 'Y',
                    @qParent INT = 0,
                    @qIdx    INT = 0
                AS
                BEGIN
                    SET NOCOUNT ON;
                    SELECT LN_IDX, LN_TITLE FROM dpl_notify
                    WHERE (@qIsUse='' OR LN_IS_USE=@qIsUse)
                      AND (@qParent=0 OR LR_IDX=@qParent)
                      AND (@qIdx=0    OR LN_IDX=@qIdx)
                    ORDER BY LN_TITLE;
                END
            """);
        }

        // GetInfo
        try (Statement st = conn.createStatement()) {
            st.execute("""
                CREATE PROCEDURE uspLawNotify_GetInfo
                    @IDX INT
                AS
                BEGIN
                    SET NOCOUNT ON;
                    SELECT
                        n.LN_IDX, n.LN_TITLE, n.LN_TITLE AS LN_NOTIFY,
                        n.LL_IDX, n.LR_IDX, n.LN_HISTORY, n.LN_IS_USE,
                        n.LN_REG_USER,
                        CONVERT(NVARCHAR(19), n.LN_REG_DATE, 120) AS LN_REG_DATE,
                        n.LN_UPD_USER,
                        CONVERT(NVARCHAR(19), n.LN_UPD_DATE, 120) AS LN_UPD_DATE,
                        ISNULL(l.LL_TITLE,'') AS LL_TITLE,
                        ISNULL(r.LR_TITLE,'') AS LR_TITLE
                    FROM dpl_notify n
                    LEFT JOIN dpl_regulation_legal l ON l.LL_IDX = n.LL_IDX
                    LEFT JOIN dpl_regulation       r ON r.LR_IDX = n.LR_IDX
                    WHERE n.LN_IDX = @IDX;
                END
            """);
        }

        // PutInfo
        try (Statement st = conn.createStatement()) {
            st.execute("""
                CREATE PROCEDURE uspLawNotify_PutInfo
                    @ACTION     NVARCHAR(10),
                    @IDX        INT = 0,
                    @NOTIFY     NVARCHAR(200),
                    @LR_IDX     INT = 0,
                    @HISTORY    NVARCHAR(MAX) = '',
                    @IS_USE     CHAR(1) = 'Y',
                    @ATTA_FILES NVARCHAR(MAX) = '',
                    @REG_USER   NVARCHAR(50)  = '',
                    @REG_IP     NVARCHAR(40)  = '',
                    @RTN_MSG    NVARCHAR(200) = '' OUTPUT,
                    @RTN_IDX    INT = 0 OUTPUT
                AS
                BEGIN
                    SET NOCOUNT ON;
                    SET @RTN_MSG = ''; SET @RTN_IDX = 0;
                    DECLARE @LL_IDX INT = 0;
                    SELECT @LL_IDX = ISNULL(LL_IDX,0) FROM dpl_regulation WHERE LR_IDX = @LR_IDX;
                    IF @ACTION = 'ADD'
                    BEGIN
                        IF @NOTIFY = '' BEGIN SET @RTN_MSG = '정부고시명을 입력해주세요.'; RETURN; END
                        INSERT INTO dpl_notify
                            (LN_TITLE, LL_IDX, LR_IDX, LN_HISTORY, LN_IS_USE, LN_REG_USER, LN_REG_IP, LN_REG_DATE)
                        VALUES
                            (@NOTIFY, @LL_IDX, @LR_IDX, @HISTORY, @IS_USE, @REG_USER, @REG_IP, GETDATE());
                        SET @RTN_IDX = SCOPE_IDENTITY();
                    END
                    ELSE IF @ACTION = 'MOD'
                    BEGIN
                        UPDATE dpl_notify SET
                            LN_TITLE=@NOTIFY, LL_IDX=@LL_IDX, LR_IDX=@LR_IDX,
                            LN_HISTORY=@HISTORY, LN_IS_USE=@IS_USE,
                            LN_UPD_USER=@REG_USER, LN_UPD_IP=@REG_IP, LN_UPD_DATE=GETDATE()
                        WHERE LN_IDX=@IDX;
                        SET @RTN_IDX=@IDX;
                    END
                    ELSE IF @ACTION = 'DEL'
                    BEGIN
                        DELETE FROM dpl_notify WHERE LN_IDX=@IDX;
                        SET @RTN_IDX=@IDX;
                    END
                    ELSE SET @RTN_MSG='잘못된 ACTION입니다.';
                END
            """);
        }

        System.out.println("[DPL] notify SP 4종 생성 완료");
    }

    private void seedNotifyData(Connection conn) throws Exception {
        try (Statement st = conn.createStatement()) {
            ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM dpl_notify");
            rs.next();
            if (rs.getInt(1) == 0) {
                st.execute("""
                    INSERT INTO dpl_notify (LN_TITLE, LL_IDX, LR_IDX, LN_HISTORY, LN_IS_USE, LN_REG_USER, LN_REG_DATE)
                    VALUES
                        (N'고용노동부 고시 제2023-19호 (안전보건관리체계)', 1, 1, N'2023-04-01 최초 제정', 'Y', 'admin', GETDATE()),
                        (N'환경부 고시 제2022-241호 (화학물질 취급시설 기준)', 2, 2, N'2022-12-01 개정', 'Y', 'admin', GETDATE()),
                        (N'고용노동부 고시 제2023-55호 (중대재해 보고절차)', 3, 3, N'2023-09-01 시행', 'Y', 'admin', GETDATE()),
                        (N'소방청 고시 제2023-11호 (위험물 저장기준)', 4, 4, N'2023-06-01 개정', 'Y', 'admin', GETDATE()),
                        (N'소방청 고시 제2022-33호 (소방시설 점검방법)', 5, 5, N'2022-12-01 최초 제정', 'Y', 'admin', GETDATE())
                """);
                System.out.println("[DPL] notify 시드 5건 완료");
            }
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {}
}
