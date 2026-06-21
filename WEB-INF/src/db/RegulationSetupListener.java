package db;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.sql.*;

/**
 * 앱 기동 시 regulation·legal·notify·safety·items 모듈 SP 자동 생성
 */
@WebListener
public class RegulationSetupListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("[DPL] SetupListener: SP 생성 시작");
        try (Connection conn = DBPool.getConnection()) {
            createRegulationSPs(conn);
            seedRegulationData(conn);
            createNotifySPs(conn);
            seedNotifyData(conn);
            createSafetySPs(conn);
            seedSafetyData(conn);
            createItemsSPs(conn);
            seedItemsData(conn);
            System.out.println("[DPL] SetupListener: 모든 SP 생성 완료");
        } catch (Exception e) {
            System.err.println("[DPL] SetupListener 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ══ REGULATION + LEGAL ══════════════════════════════════════════
    private void createRegulationSPs(Connection conn) throws Exception {
        String[] drops = {
            "IF OBJECT_ID('uspLawRegulation_GetListPaging','P') IS NOT NULL DROP PROCEDURE uspLawRegulation_GetListPaging",
            "IF OBJECT_ID('uspLawRegulation_GetList','P') IS NOT NULL DROP PROCEDURE uspLawRegulation_GetList",
            "IF OBJECT_ID('uspLawRegulation_GetInfo','P') IS NOT NULL DROP PROCEDURE uspLawRegulation_GetInfo",
            "IF OBJECT_ID('uspLawRegulation_PutInfo','P') IS NOT NULL DROP PROCEDURE uspLawRegulation_PutInfo",
            "IF OBJECT_ID('uspLawRegulationLegal_GetList','P') IS NOT NULL DROP PROCEDURE uspLawRegulationLegal_GetList",
        };
        for (String sql : drops) exec(conn, sql);

        exec(conn, """
            IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME='dpl_regulation_legal')
            CREATE TABLE dpl_regulation_legal (
                LL_IDX   INT IDENTITY(1,1) PRIMARY KEY,
                LL_TITLE NVARCHAR(200) NOT NULL,
                LL_IS_USE CHAR(1) DEFAULT 'Y',
                LL_SORT  INT DEFAULT 0,
                REG_DATE DATETIME DEFAULT GETDATE()
            )
        """);
        exec(conn, "IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME='dpl_regulation_legal' AND COLUMN_NAME='LL_DEPT') ALTER TABLE dpl_regulation_legal ADD LL_DEPT NVARCHAR(100) DEFAULT ''");
        exec(conn, "IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME='dpl_regulation_legal' AND COLUMN_NAME='REG_USER') ALTER TABLE dpl_regulation_legal ADD REG_USER NVARCHAR(50) DEFAULT ''");
        exec(conn, """
            IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME='dpl_regulation')
            CREATE TABLE dpl_regulation (
                LR_IDX INT IDENTITY(1,1) PRIMARY KEY, LR_TITLE NVARCHAR(300) NOT NULL,
                LL_IDX INT NOT NULL DEFAULT 0, LR_CONDITION NVARCHAR(MAX),
                LR_CERTIFY_GUIDE NVARCHAR(MAX), LR_PENALTY NVARCHAR(MAX),
                LR_IS_USE CHAR(1) DEFAULT 'Y', REG_USER NVARCHAR(50), REG_IP NVARCHAR(40),
                REG_DATE DATETIME DEFAULT GETDATE(), UPD_USER NVARCHAR(50), UPD_DATE DATETIME
            )
        """);
        exec(conn, """
            CREATE PROCEDURE uspLawRegulation_GetListPaging
                @Page INT=1,@ListSize INT=20,@qKey NVARCHAR(50)='',@qWord NVARCHAR(200)='',@qLL INT=0,@qSort NVARCHAR(50)=''
            AS BEGIN
                SET NOCOUNT ON; DECLARE @Offset INT=(@Page-1)*@ListSize;
                SELECT r.LR_IDX,r.LR_TITLE,r.LL_IDX,ISNULL(l.LL_TITLE,'') AS LL_TITLE,r.LR_IS_USE,
                    CONVERT(NVARCHAR(10),r.REG_DATE,120) AS LR_REG_DATE,r.REG_USER AS LR_REG_USER,
                    CONVERT(NVARCHAR(10),r.UPD_DATE,120) AS LR_UPD_DATE,r.UPD_USER AS LR_UPD_USER
                FROM dpl_regulation r LEFT JOIN dpl_regulation_legal l ON l.LL_IDX=r.LL_IDX
                WHERE (@qLL=0 OR r.LL_IDX=@qLL)
                  AND (@qWord='' OR (@qKey='TITLE' AND r.LR_TITLE LIKE '%'+@qWord+'%') OR (@qKey='' AND r.LR_TITLE LIKE '%'+@qWord+'%'))
                ORDER BY r.LR_IDX DESC OFFSET @Offset ROWS FETCH NEXT @ListSize ROWS ONLY;
                SELECT COUNT(*) AS TOTAL FROM dpl_regulation r
                WHERE (@qLL=0 OR r.LL_IDX=@qLL) AND (@qWord='' OR r.LR_TITLE LIKE '%'+@qWord+'%');
            END
        """);
        exec(conn, """
            CREATE PROCEDURE uspLawRegulation_GetList @qIsUse CHAR(1)='Y',@qParent INT=0,@qIdx INT=0
            AS BEGIN SET NOCOUNT ON;
                SELECT LR_IDX,LR_TITLE,LL_IDX,LR_IS_USE FROM dpl_regulation
                WHERE (@qIsUse='' OR LR_IS_USE=@qIsUse) ORDER BY LR_TITLE;
            END
        """);
        exec(conn, """
            CREATE PROCEDURE uspLawRegulation_GetInfo @IDX INT AS BEGIN SET NOCOUNT ON;
                SELECT r.LR_IDX,r.LR_TITLE,r.LL_IDX,ISNULL(l.LL_TITLE,'') AS LL_TITLE,
                    r.LR_CONDITION,r.LR_CERTIFY_GUIDE,r.LR_PENALTY,r.LR_IS_USE,
                    r.REG_USER AS LR_REG_USER,CONVERT(NVARCHAR(19),r.REG_DATE,120) AS LR_REG_DATE,
                    r.UPD_USER AS LR_UPD_USER,CONVERT(NVARCHAR(19),r.UPD_DATE,120) AS LR_UPD_DATE
                FROM dpl_regulation r LEFT JOIN dpl_regulation_legal l ON l.LL_IDX=r.LL_IDX
                WHERE r.LR_IDX=@IDX;
            END
        """);
        exec(conn, """
            CREATE PROCEDURE uspLawRegulation_PutInfo
                @ACTION NVARCHAR(10),@IDX INT=0,@TITLE NVARCHAR(300),@LL_IDX INT=0,
                @CONDITION NVARCHAR(MAX)='',@CERTIFY_GUIDE NVARCHAR(MAX)='',@PENALTY NVARCHAR(MAX)='',
                @IS_USE CHAR(1)='Y',@REG_USER NVARCHAR(50)='',@REG_IP NVARCHAR(40)='',
                @RTN_MSG NVARCHAR(500) OUTPUT,@RTN_IDX INT OUTPUT
            AS BEGIN SET NOCOUNT ON; SET @RTN_MSG=''; SET @RTN_IDX=@IDX;
                IF @ACTION='ADD' BEGIN
                    IF @TITLE='' BEGIN SET @RTN_MSG='제목을 입력하세요.'; RETURN; END
                    INSERT INTO dpl_regulation (LR_TITLE,LL_IDX,LR_CONDITION,LR_CERTIFY_GUIDE,LR_PENALTY,LR_IS_USE,REG_USER,REG_IP,REG_DATE)
                    VALUES (@TITLE,@LL_IDX,@CONDITION,@CERTIFY_GUIDE,@PENALTY,@IS_USE,@REG_USER,@REG_IP,GETDATE());
                    SET @RTN_IDX=SCOPE_IDENTITY();
                END ELSE IF @ACTION='MOD'
                    UPDATE dpl_regulation SET LR_TITLE=@TITLE,LL_IDX=@LL_IDX,LR_CONDITION=@CONDITION,
                    LR_CERTIFY_GUIDE=@CERTIFY_GUIDE,LR_PENALTY=@PENALTY,LR_IS_USE=@IS_USE,UPD_USER=@REG_USER,UPD_DATE=GETDATE()
                    WHERE LR_IDX=@IDX;
                ELSE IF @ACTION='DEL' DELETE FROM dpl_regulation WHERE LR_IDX=@IDX;
            END
        """);
        exec(conn, """
            CREATE PROCEDURE uspLawRegulationLegal_GetList @qIsUse CHAR(1)='Y',@qParent INT=0,@qIdx INT=0
            AS BEGIN SET NOCOUNT ON;
                SELECT LL_IDX,LL_TITLE,LL_IS_USE FROM dpl_regulation_legal WHERE LL_IS_USE=@qIsUse ORDER BY LL_SORT,LL_IDX;
            END
        """);
        System.out.println("[DPL] regulation SP 완료");
    }

    private void seedRegulationData(Connection conn) throws Exception {
        try (Statement st = conn.createStatement()) {
            ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM dpl_regulation_legal"); rs.next();
            if (rs.getInt(1)==0) {
                st.execute("INSERT INTO dpl_regulation_legal (LL_TITLE,LL_IS_USE,LL_SORT) VALUES (N'산업안전보건법','Y',1),(N'화학물질관리법','Y',2),(N'중대재해처벌법','Y',3),(N'위험물안전관리법','Y',4),(N'소방시설법','Y',5)");
            }
        }
        dropAndCreate(conn, "uspLawRegulationLegal_GetListPaging", """
            CREATE PROCEDURE uspLawRegulationLegal_GetListPaging
                @Page INT=1,@ListSize INT=20,@qKey NVARCHAR(50)='',@qWord NVARCHAR(200)='',@qSort NVARCHAR(50)=''
            AS BEGIN SET NOCOUNT ON; DECLARE @Offset INT=(@Page-1)*@ListSize;
                SELECT LL_IDX,LL_TITLE,ISNULL(LL_DEPT,'') AS LL_DEPT,LL_IS_USE,
                    CONVERT(NVARCHAR(10),REG_DATE,120) AS LL_REG_DATE,REG_USER AS LL_REG_USER
                FROM dpl_regulation_legal
                WHERE (@qWord='' OR (@qKey='TITLE' AND LL_TITLE LIKE '%'+@qWord+'%') OR (@qKey='DEPT' AND LL_DEPT LIKE '%'+@qWord+'%') OR (@qKey='' AND (LL_TITLE LIKE '%'+@qWord+'%' OR LL_DEPT LIKE '%'+@qWord+'%')))
                ORDER BY LL_IDX DESC OFFSET @Offset ROWS FETCH NEXT @ListSize ROWS ONLY;
                SELECT COUNT(*) AS TOTAL FROM dpl_regulation_legal WHERE (@qWord='' OR LL_TITLE LIKE '%'+@qWord+'%' OR LL_DEPT LIKE '%'+@qWord+'%');
            END
        """);
        dropAndCreate(conn, "uspLawRegulationLegal_GetInfo", """
            CREATE PROCEDURE uspLawRegulationLegal_GetInfo @IDX INT AS BEGIN SET NOCOUNT ON;
                SELECT LL_IDX,LL_TITLE,ISNULL(LL_DEPT,'') AS LL_DEPT,LL_IS_USE,
                    REG_USER AS LL_REG_USER,CONVERT(NVARCHAR(19),REG_DATE,120) AS LL_REG_DATE
                FROM dpl_regulation_legal WHERE LL_IDX=@IDX;
            END
        """);
        dropAndCreate(conn, "uspLawRegulationLegal_PutInfo", """
            CREATE PROCEDURE uspLawRegulationLegal_PutInfo
                @ACTION NVARCHAR(10),@IDX INT=0,@TITLE NVARCHAR(200),@DEPT NVARCHAR(100)='',
                @IS_USE CHAR(1)='Y',@REG_USER NVARCHAR(50)='',@REG_IP NVARCHAR(40)='',@RTN_MSG NVARCHAR(500) OUTPUT
            AS BEGIN SET NOCOUNT ON; SET @RTN_MSG='';
                IF @ACTION='ADD' BEGIN
                    IF @TITLE='' BEGIN SET @RTN_MSG='법규명을 입력하세요.'; RETURN; END
                    IF @DEPT='' BEGIN SET @RTN_MSG='관리부처를 입력하세요.'; RETURN; END
                    INSERT INTO dpl_regulation_legal (LL_TITLE,LL_DEPT,LL_IS_USE,LL_SORT,REG_DATE) VALUES (@TITLE,@DEPT,@IS_USE,0,GETDATE());
                END ELSE IF @ACTION='MOD'
                    UPDATE dpl_regulation_legal SET LL_TITLE=@TITLE,LL_DEPT=@DEPT,LL_IS_USE=@IS_USE WHERE LL_IDX=@IDX;
                ELSE IF @ACTION='DEL' DELETE FROM dpl_regulation_legal WHERE LL_IDX=@IDX;
            END
        """);
        try (Statement st = conn.createStatement()) {
            ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM dpl_regulation"); rs.next();
            if (rs.getInt(1)==0) {
                st.execute("INSERT INTO dpl_regulation (LR_TITLE,LL_IDX,LR_CONDITION,LR_CERTIFY_GUIDE,LR_PENALTY,LR_IS_USE,REG_USER,REG_DATE) VALUES (N'안전보건관리체계 구축',1,N'상시근로자 50인 이상',N'안전보건관리규정 작성',N'1,000만원 이하 과태료','Y','admin',GETDATE()),(N'화학물질 취급시설 기준',2,N'유해화학물질 취급량 기준 이상',N'취급시설 설치검사 수검',N'5,000만원 이하 벌금','Y','admin',GETDATE()),(N'중대재해 발생 보고',3,N'중대산업재해 발생 시',N'즉시 고용노동부 보고',N'1억원 이하 과태료','Y','admin',GETDATE()),(N'위험물 저장소 설치 허가',4,N'지정수량 이상 위험물 저장',N'소방서 허가 취득',N'3,000만원 이하 벌금','Y','admin',GETDATE()),(N'소방시설 자체점검',5,N'특정소방대상물',N'연 1회 이상 자체점검 실시',N'300만원 이하 과태료','Y','admin',GETDATE())");
            }
        }
    }

    // ══ NOTIFY ══════════════════════════════════════════════════════
    private void createNotifySPs(Connection conn) throws Exception {
        exec(conn, """
            IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME='dpl_notify')
            CREATE TABLE dpl_notify (
                LN_IDX INT IDENTITY(1,1) PRIMARY KEY, LN_TITLE NVARCHAR(200) NOT NULL,
                LL_IDX INT NOT NULL DEFAULT 0, LR_IDX INT NOT NULL DEFAULT 0,
                LN_HISTORY NVARCHAR(MAX), LN_IS_USE CHAR(1) NOT NULL DEFAULT 'Y',
                LN_REG_USER NVARCHAR(50), LN_REG_IP NVARCHAR(40),
                LN_REG_DATE DATETIME DEFAULT GETDATE(), LN_UPD_USER NVARCHAR(50),
                LN_UPD_IP NVARCHAR(40), LN_UPD_DATE DATETIME
            )
        """);
        dropAndCreate(conn, "uspLawNotify_GetListPaging", """
            CREATE PROCEDURE uspLawNotify_GetListPaging
                @Page INT=1,@ListSize INT=20,@qKey NVARCHAR(50)='',@qWord NVARCHAR(200)='',@qLL INT=0,@qLR INT=0,@qSort NVARCHAR(50)=''
            AS BEGIN SET NOCOUNT ON; DECLARE @Offset INT=(@Page-1)*@ListSize;
                SELECT n.LN_IDX,n.LN_TITLE,n.LN_TITLE AS LN_NOTIFY,
                    ISNULL(l.LL_TITLE,'') AS LL_TITLE,ISNULL(r.LR_TITLE,'') AS LR_TITLE,
                    n.LN_IS_USE,CONVERT(NVARCHAR(10),n.LN_REG_DATE,120) AS LN_REG_DATE
                FROM dpl_notify n
                LEFT JOIN dpl_regulation_legal l ON l.LL_IDX=n.LL_IDX
                LEFT JOIN dpl_regulation r ON r.LR_IDX=n.LR_IDX
                WHERE (@qLL=0 OR n.LL_IDX=@qLL) AND (@qLR=0 OR n.LR_IDX=@qLR)
                  AND (@qWord='' OR (@qKey='TITLE' AND n.LN_TITLE LIKE '%'+@qWord+'%') OR (@qKey='' AND n.LN_TITLE LIKE '%'+@qWord+'%'))
                ORDER BY n.LN_IDX DESC OFFSET @Offset ROWS FETCH NEXT @ListSize ROWS ONLY;
                SELECT COUNT(1) AS TOTAL FROM dpl_notify n
                WHERE (@qLL=0 OR n.LL_IDX=@qLL) AND (@qLR=0 OR n.LR_IDX=@qLR) AND (@qWord='' OR n.LN_TITLE LIKE '%'+@qWord+'%');
            END
        """);
        dropAndCreate(conn, "uspLawNotify_GetList", """
            CREATE PROCEDURE uspLawNotify_GetList @qIsUse CHAR(1)='Y',@qParent INT=0,@qIdx INT=0
            AS BEGIN SET NOCOUNT ON;
                SELECT LN_IDX,LN_TITLE FROM dpl_notify
                WHERE (@qIsUse='' OR LN_IS_USE=@qIsUse) AND (@qParent=0 OR LR_IDX=@qParent) AND (@qIdx=0 OR LN_IDX=@qIdx)
                ORDER BY LN_TITLE;
            END
        """);
        dropAndCreate(conn, "uspLawNotify_GetInfo", """
            CREATE PROCEDURE uspLawNotify_GetInfo @IDX INT AS BEGIN SET NOCOUNT ON;
                SELECT n.LN_IDX,n.LN_TITLE,n.LN_TITLE AS LN_NOTIFY,n.LL_IDX,n.LR_IDX,n.LN_HISTORY,n.LN_IS_USE,
                    n.LN_REG_USER,CONVERT(NVARCHAR(19),n.LN_REG_DATE,120) AS LN_REG_DATE,
                    n.LN_UPD_USER,CONVERT(NVARCHAR(19),n.LN_UPD_DATE,120) AS LN_UPD_DATE,
                    ISNULL(l.LL_TITLE,'') AS LL_TITLE,ISNULL(r.LR_TITLE,'') AS LR_TITLE
                FROM dpl_notify n LEFT JOIN dpl_regulation_legal l ON l.LL_IDX=n.LL_IDX
                LEFT JOIN dpl_regulation r ON r.LR_IDX=n.LR_IDX WHERE n.LN_IDX=@IDX;
            END
        """);
        dropAndCreate(conn, "uspLawNotify_PutInfo", """
            CREATE PROCEDURE uspLawNotify_PutInfo
                @ACTION NVARCHAR(10),@IDX INT=0,@NOTIFY NVARCHAR(200),@LR_IDX INT=0,
                @HISTORY NVARCHAR(MAX)='',@IS_USE CHAR(1)='Y',@ATTA_FILES NVARCHAR(MAX)='',
                @REG_USER NVARCHAR(50)='',@REG_IP NVARCHAR(40)='',
                @RTN_MSG NVARCHAR(200)='' OUTPUT,@RTN_IDX INT=0 OUTPUT
            AS BEGIN SET NOCOUNT ON; SET @RTN_MSG=''; SET @RTN_IDX=0;
                DECLARE @LL_IDX INT=0;
                SELECT @LL_IDX=ISNULL(LL_IDX,0) FROM dpl_regulation WHERE LR_IDX=@LR_IDX;
                IF @ACTION='ADD' BEGIN
                    IF @NOTIFY='' BEGIN SET @RTN_MSG='정부고시명을 입력해주세요.'; RETURN; END
                    INSERT INTO dpl_notify (LN_TITLE,LL_IDX,LR_IDX,LN_HISTORY,LN_IS_USE,LN_REG_USER,LN_REG_IP,LN_REG_DATE)
                    VALUES (@NOTIFY,@LL_IDX,@LR_IDX,@HISTORY,@IS_USE,@REG_USER,@REG_IP,GETDATE());
                    SET @RTN_IDX=SCOPE_IDENTITY();
                END ELSE IF @ACTION='MOD' BEGIN
                    UPDATE dpl_notify SET LN_TITLE=@NOTIFY,LL_IDX=@LL_IDX,LR_IDX=@LR_IDX,
                    LN_HISTORY=@HISTORY,LN_IS_USE=@IS_USE,LN_UPD_USER=@REG_USER,LN_UPD_IP=@REG_IP,LN_UPD_DATE=GETDATE()
                    WHERE LN_IDX=@IDX; SET @RTN_IDX=@IDX;
                END ELSE IF @ACTION='DEL' BEGIN DELETE FROM dpl_notify WHERE LN_IDX=@IDX; SET @RTN_IDX=@IDX; END
                ELSE SET @RTN_MSG='잘못된 ACTION입니다.';
            END
        """);
    }

    private void seedNotifyData(Connection conn) throws Exception {
        try (Statement st = conn.createStatement()) {
            ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM dpl_notify"); rs.next();
            if (rs.getInt(1)==0)
                st.execute("INSERT INTO dpl_notify (LN_TITLE,LL_IDX,LR_IDX,LN_HISTORY,LN_IS_USE,LN_REG_USER,LN_REG_DATE) VALUES (N'고용노동부 고시 제2023-19호',1,1,N'2023-04-01 최초 제정','Y','admin',GETDATE()),(N'환경부 고시 제2022-241호',2,2,N'2022-12-01 개정','Y','admin',GETDATE()),(N'고용노동부 고시 제2023-55호',3,3,N'2023-09-01 시행','Y','admin',GETDATE()),(N'소방청 고시 제2023-11호',4,4,N'2023-06-01 개정','Y','admin',GETDATE()),(N'소방청 고시 제2022-33호',5,5,N'2022-12-01 최초 제정','Y','admin',GETDATE())");
        }
    }

    // ══ SAFETY ══════════════════════════════════════════════════════
    private void createSafetySPs(Connection conn) throws Exception {
        exec(conn, """
            IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME='dpl_safety')
            CREATE TABLE dpl_safety (
                LS_IDX INT IDENTITY(1,1) PRIMARY KEY,
                LN_IDX INT NOT NULL DEFAULT 0,
                LL_IDX INT NOT NULL DEFAULT 0,
                LR_IDX INT NOT NULL DEFAULT 0,
                LS_TITLE NVARCHAR(200) NOT NULL,
                LS_CONTENT NVARCHAR(MAX),
                LS_IS_USE CHAR(1) NOT NULL DEFAULT 'Y',
                LS_REG_USER NVARCHAR(50), LS_REG_IP NVARCHAR(40),
                LS_REG_DATE DATETIME DEFAULT GETDATE(),
                LS_UPD_USER NVARCHAR(50), LS_UPD_IP NVARCHAR(40), LS_UPD_DATE DATETIME
            )
        """);
        dropAndCreate(conn, "uspLawSafety_GetListPaging", """
            CREATE PROCEDURE uspLawSafety_GetListPaging
                @Page INT=1,@ListSize INT=20,@qKey NVARCHAR(50)='',@qWord NVARCHAR(200)='',
                @qLL INT=0,@qLR INT=0,@qLN INT=0,@qSort NVARCHAR(50)=''
            AS BEGIN SET NOCOUNT ON; DECLARE @Offset INT=(@Page-1)*@ListSize;
                SELECT s.LS_IDX,s.LS_TITLE,
                    ISNULL(n.LN_TITLE,'') AS LN_TITLE,ISNULL(n.LN_TITLE,'') AS LN_NOTIFY,
                    ISNULL(r.LR_TITLE,'') AS LR_TITLE,ISNULL(l.LL_TITLE,'') AS LL_TITLE,
                    s.LS_IS_USE,CONVERT(NVARCHAR(10),s.LS_REG_DATE,120) AS LS_REG_DATE,
                    s.LS_REG_USER,s.LL_IDX,s.LR_IDX,s.LN_IDX
                FROM dpl_safety s
                LEFT JOIN dpl_regulation_legal l ON l.LL_IDX=s.LL_IDX
                LEFT JOIN dpl_regulation r ON r.LR_IDX=s.LR_IDX
                LEFT JOIN dpl_notify n ON n.LN_IDX=s.LN_IDX
                WHERE (@qLL=0 OR s.LL_IDX=@qLL) AND (@qLR=0 OR s.LR_IDX=@qLR) AND (@qLN=0 OR s.LN_IDX=@qLN)
                  AND (@qWord='' OR (@qKey='TITLE' AND s.LS_TITLE LIKE '%'+@qWord+'%') OR (@qKey='' AND s.LS_TITLE LIKE '%'+@qWord+'%'))
                ORDER BY s.LS_IDX DESC OFFSET @Offset ROWS FETCH NEXT @ListSize ROWS ONLY;
                SELECT COUNT(1) AS TOTAL FROM dpl_safety s
                WHERE (@qLL=0 OR s.LL_IDX=@qLL) AND (@qLR=0 OR s.LR_IDX=@qLR) AND (@qLN=0 OR s.LN_IDX=@qLN)
                  AND (@qWord='' OR s.LS_TITLE LIKE '%'+@qWord+'%');
            END
        """);
        dropAndCreate(conn, "uspLawSafety_GetList", """
            CREATE PROCEDURE uspLawSafety_GetList @qFlag NVARCHAR(10)='',@qIsUse CHAR(1)='Y',@qParent INT=0,@qIdx INT=0
            AS BEGIN SET NOCOUNT ON;
                SELECT LS_IDX,LS_TITLE FROM dpl_safety
                WHERE (@qIsUse='' OR LS_IS_USE=@qIsUse) AND (@qParent=0 OR LN_IDX=@qParent) AND (@qIdx=0 OR LS_IDX=@qIdx)
                ORDER BY LS_TITLE;
            END
        """);
        dropAndCreate(conn, "uspLawSafety_GetInfo", """
            CREATE PROCEDURE uspLawSafety_GetInfo @IDX INT AS BEGIN SET NOCOUNT ON;
                SELECT s.LS_IDX,s.LN_IDX,s.LL_IDX,s.LR_IDX,s.LS_TITLE,s.LS_CONTENT,s.LS_IS_USE,
                    s.LS_REG_USER,CONVERT(NVARCHAR(19),s.LS_REG_DATE,120) AS LS_REG_DATE,
                    s.LS_UPD_USER,CONVERT(NVARCHAR(19),s.LS_UPD_DATE,120) AS LS_UPD_DATE,
                    ISNULL(l.LL_TITLE,'') AS LL_TITLE,ISNULL(r.LR_TITLE,'') AS LR_TITLE,
                    ISNULL(n.LN_TITLE,'') AS LN_TITLE
                FROM dpl_safety s LEFT JOIN dpl_regulation_legal l ON l.LL_IDX=s.LL_IDX
                LEFT JOIN dpl_regulation r ON r.LR_IDX=s.LR_IDX LEFT JOIN dpl_notify n ON n.LN_IDX=s.LN_IDX
                WHERE s.LS_IDX=@IDX;
            END
        """);
        dropAndCreate(conn, "uspLawSafety_PutInfo", """
            CREATE PROCEDURE uspLawSafety_PutInfo
                @ACTION NVARCHAR(10),@IDX INT=0,@LN_IDX INT=0,@TITLE NVARCHAR(200),
                @CONTENT NVARCHAR(MAX)='',@IS_USE CHAR(1)='Y',
                @REG_USER NVARCHAR(50)='',@REG_IP NVARCHAR(40)='',
                @RTN_MSG NVARCHAR(200)='' OUTPUT,@RTN_IDX INT=0 OUTPUT
            AS BEGIN SET NOCOUNT ON; SET @RTN_MSG=''; SET @RTN_IDX=0;
                DECLARE @LL_IDX INT=0,@LR_IDX INT=0;
                SELECT @LR_IDX=ISNULL(LR_IDX,0),@LL_IDX=ISNULL(LL_IDX,0) FROM dpl_notify WHERE LN_IDX=@LN_IDX;
                IF @ACTION='ADD' BEGIN
                    IF @TITLE='' BEGIN SET @RTN_MSG='안전요건명을 입력해주세요.'; RETURN; END
                    INSERT INTO dpl_safety (LN_IDX,LL_IDX,LR_IDX,LS_TITLE,LS_CONTENT,LS_IS_USE,LS_REG_USER,LS_REG_IP,LS_REG_DATE)
                    VALUES (@LN_IDX,@LL_IDX,@LR_IDX,@TITLE,@CONTENT,@IS_USE,@REG_USER,@REG_IP,GETDATE());
                    SET @RTN_IDX=SCOPE_IDENTITY();
                END ELSE IF @ACTION='MOD' BEGIN
                    UPDATE dpl_safety SET LN_IDX=@LN_IDX,LL_IDX=@LL_IDX,LR_IDX=@LR_IDX,
                    LS_TITLE=@TITLE,LS_CONTENT=@CONTENT,LS_IS_USE=@IS_USE,
                    LS_UPD_USER=@REG_USER,LS_UPD_IP=@REG_IP,LS_UPD_DATE=GETDATE()
                    WHERE LS_IDX=@IDX; SET @RTN_IDX=@IDX;
                END ELSE IF @ACTION='DEL' BEGIN DELETE FROM dpl_safety WHERE LS_IDX=@IDX; SET @RTN_IDX=@IDX; END
                ELSE SET @RTN_MSG='잘못된 ACTION입니다.';
            END
        """);
    }

    private void seedSafetyData(Connection conn) throws Exception {
        try (Statement st = conn.createStatement()) {
            ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM dpl_safety"); rs.next();
            if (rs.getInt(1)==0)
                st.execute("INSERT INTO dpl_safety (LN_IDX,LL_IDX,LR_IDX,LS_TITLE,LS_CONTENT,LS_IS_USE,LS_REG_USER,LS_REG_DATE) VALUES (1,1,1,N'안전보건관리책임자 선임',N'상시근로자 100인 이상 사업장은 안전보건관리책임자를 선임해야 합니다.','Y','admin',GETDATE()),(2,2,2,N'화학물질 취급 안전교육',N'연 1회 이상 화학물질 취급 관련 안전교육을 실시해야 합니다.','Y','admin',GETDATE()),(3,3,3,N'중대재해 재발방지 대책 수립',N'중대재해 발생 시 재발방지 대책을 30일 이내 수립해야 합니다.','Y','admin',GETDATE()),(4,4,4,N'위험물 안전관리자 선임',N'위험물 취급소에는 위험물 안전관리자를 선임해야 합니다.','Y','admin',GETDATE()),(5,5,5,N'소방시설 유지·관리',N'소방시설을 정기적으로 점검하고 적합하게 유지·관리해야 합니다.','Y','admin',GETDATE())");
        }
    }

    // ══ ITEMS (마스터 + 디테일) ══════════════════════════════════════
    private void createItemsSPs(Connection conn) throws Exception {
        exec(conn, """
            IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME='dpl_items')
            CREATE TABLE dpl_items (
                LI_IDX INT IDENTITY(1,1) PRIMARY KEY,
                LN_IDX INT NOT NULL DEFAULT 0, LL_IDX INT NOT NULL DEFAULT 0, LR_IDX INT NOT NULL DEFAULT 0,
                LI_LEGAL_NAME NVARCHAR(200) NOT NULL,
                LI_SCOPE NVARCHAR(MAX), LI_EXCEPTION NVARCHAR(MAX),
                LO_IDX INT DEFAULT 0, LG_IDX INT DEFAULT 0,
                LI_IS_USE CHAR(1) DEFAULT 'Y',
                LI_REG_USER NVARCHAR(50), LI_REG_IP NVARCHAR(40),
                LI_REG_DATE DATETIME DEFAULT GETDATE(),
                LI_UPD_USER NVARCHAR(50), LI_UPD_DATE DATETIME
            )
        """);
        exec(conn, """
            IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME='dpl_items_detail')
            CREATE TABLE dpl_items_detail (
                LD_IDX INT IDENTITY(1,1) PRIMARY KEY,
                LI_IDX INT NOT NULL DEFAULT 0, LN_IDX INT NOT NULL DEFAULT 0,
                LL_IDX INT NOT NULL DEFAULT 0, LR_IDX INT NOT NULL DEFAULT 0,
                LD_ITEM_NAME NVARCHAR(200) NOT NULL,
                LD_USE_AGE NVARCHAR(100), LD_MATERIAL NVARCHAR(100),
                LD_SAMPLE_ATTA_PATH NVARCHAR(500),
                LD_IS_USE CHAR(1) DEFAULT 'Y',
                LD_REG_USER NVARCHAR(50), LD_REG_IP NVARCHAR(40),
                LD_REG_DATE DATETIME DEFAULT GETDATE(),
                LD_UPD_USER NVARCHAR(50), LD_UPD_DATE DATETIME
            )
        """);
        dropAndCreate(conn, "uspLawItems_GetListPaging", """
            CREATE PROCEDURE uspLawItems_GetListPaging
                @Page INT=1,@ListSize INT=20,@qKey NVARCHAR(50)='',@qWord NVARCHAR(200)='',@qLL INT=0,@qLR INT=0,@qLN INT=0,@qSort NVARCHAR(50)=''
            AS BEGIN SET NOCOUNT ON; DECLARE @Offset INT=(@Page-1)*@ListSize;
                SELECT i.LI_IDX,i.LI_LEGAL_NAME,
                    ISNULL(n.LN_TITLE,'') AS LN_TITLE,ISNULL(n.LN_TITLE,'') AS LN_NOTIFY,
                    ISNULL(r.LR_TITLE,'') AS LR_TITLE,ISNULL(l.LL_TITLE,'') AS LL_TITLE,
                    i.LI_IS_USE,CONVERT(NVARCHAR(10),i.LI_REG_DATE,120) AS LI_REG_DATE,i.LL_IDX,i.LR_IDX,i.LN_IDX
                FROM dpl_items i
                LEFT JOIN dpl_regulation_legal l ON l.LL_IDX=i.LL_IDX
                LEFT JOIN dpl_regulation r ON r.LR_IDX=i.LR_IDX
                LEFT JOIN dpl_notify n ON n.LN_IDX=i.LN_IDX
                WHERE (@qLL=0 OR i.LL_IDX=@qLL) AND (@qLR=0 OR i.LR_IDX=@qLR) AND (@qLN=0 OR i.LN_IDX=@qLN)
                  AND (@qWord='' OR (@qKey='TITLE' AND i.LI_LEGAL_NAME LIKE '%'+@qWord+'%') OR (@qKey='' AND i.LI_LEGAL_NAME LIKE '%'+@qWord+'%'))
                ORDER BY i.LI_IDX DESC OFFSET @Offset ROWS FETCH NEXT @ListSize ROWS ONLY;
                SELECT COUNT(1) AS TOTAL FROM dpl_items i
                WHERE (@qLL=0 OR i.LL_IDX=@qLL) AND (@qLR=0 OR i.LR_IDX=@qLR) AND (@qLN=0 OR i.LN_IDX=@qLN)
                  AND (@qWord='' OR i.LI_LEGAL_NAME LIKE '%'+@qWord+'%');
            END
        """);
        dropAndCreate(conn, "uspLawItems_GetList", """
            CREATE PROCEDURE uspLawItems_GetList @qIsUse CHAR(1)='Y',@qParent INT=0,@qIdx INT=0
            AS BEGIN SET NOCOUNT ON;
                SELECT LI_IDX,LI_LEGAL_NAME FROM dpl_items
                WHERE (@qIsUse='' OR LI_IS_USE=@qIsUse) AND (@qParent=0 OR LN_IDX=@qParent) AND (@qIdx=0 OR LI_IDX=@qIdx)
                ORDER BY LI_LEGAL_NAME;
            END
        """);
        dropAndCreate(conn, "uspLawItems_GetInfo", """
            CREATE PROCEDURE uspLawItems_GetInfo @IDX INT AS BEGIN SET NOCOUNT ON;
                SELECT i.LI_IDX,i.LI_LEGAL_NAME,i.LN_IDX,i.LL_IDX,i.LR_IDX,i.LI_SCOPE,i.LI_EXCEPTION,i.LI_IS_USE,
                    CONVERT(NVARCHAR(19),i.LI_REG_DATE,120) AS LI_REG_DATE,i.LI_REG_USER,
                    ISNULL(l.LL_TITLE,'') AS LL_TITLE,ISNULL(r.LR_TITLE,'') AS LR_TITLE,ISNULL(n.LN_TITLE,'') AS LN_TITLE
                FROM dpl_items i LEFT JOIN dpl_regulation_legal l ON l.LL_IDX=i.LL_IDX
                LEFT JOIN dpl_regulation r ON r.LR_IDX=i.LR_IDX LEFT JOIN dpl_notify n ON n.LN_IDX=i.LN_IDX
                WHERE i.LI_IDX=@IDX;
            END
        """);
        dropAndCreate(conn, "uspLawItems_PutInfo", """
            CREATE PROCEDURE uspLawItems_PutInfo
                @ACTION NVARCHAR(10),@IDX INT=0,@LEGAL_NAME NVARCHAR(200),@LN_IDX INT=0,
                @SCOPE NVARCHAR(MAX)='',@EXCEPTION NVARCHAR(MAX)='',@LO_IDX INT=0,@LG_IDX INT=0,
                @IS_USE CHAR(1)='Y',@LS_IDXS NVARCHAR(MAX)='',@REG_USER NVARCHAR(50)='',@REG_IP NVARCHAR(40)='',
                @RTN_MSG NVARCHAR(200)='' OUTPUT,@RTN_IDX INT=0 OUTPUT
            AS BEGIN SET NOCOUNT ON; SET @RTN_MSG=''; SET @RTN_IDX=0;
                DECLARE @LL_IDX INT=0,@LR_IDX INT=0;
                SELECT @LR_IDX=ISNULL(LR_IDX,0),@LL_IDX=ISNULL(LL_IDX,0) FROM dpl_notify WHERE LN_IDX=@LN_IDX;
                IF @ACTION='ADD' BEGIN
                    IF @LEGAL_NAME='' BEGIN SET @RTN_MSG='법정 품목명을 입력해주세요.'; RETURN; END
                    INSERT INTO dpl_items (LN_IDX,LL_IDX,LR_IDX,LI_LEGAL_NAME,LI_SCOPE,LI_EXCEPTION,LO_IDX,LG_IDX,LI_IS_USE,LI_REG_USER,LI_REG_IP,LI_REG_DATE)
                    VALUES (@LN_IDX,@LL_IDX,@LR_IDX,@LEGAL_NAME,@SCOPE,@EXCEPTION,@LO_IDX,@LG_IDX,@IS_USE,@REG_USER,@REG_IP,GETDATE());
                    SET @RTN_IDX=SCOPE_IDENTITY();
                END ELSE IF @ACTION='MOD' BEGIN
                    UPDATE dpl_items SET LN_IDX=@LN_IDX,LL_IDX=@LL_IDX,LR_IDX=@LR_IDX,
                    LI_LEGAL_NAME=@LEGAL_NAME,LI_SCOPE=@SCOPE,LI_EXCEPTION=@EXCEPTION,LI_IS_USE=@IS_USE,
                    LI_UPD_USER=@REG_USER,LI_UPD_DATE=GETDATE() WHERE LI_IDX=@IDX; SET @RTN_IDX=@IDX;
                END ELSE IF @ACTION='DEL' BEGIN DELETE FROM dpl_items WHERE LI_IDX=@IDX; SET @RTN_IDX=@IDX; END
                ELSE SET @RTN_MSG='잘못된 ACTION입니다.';
            END
        """);
        // Items Detail SP
        dropAndCreate(conn, "uspLawItemsDetail_GetListPaging", """
            CREATE PROCEDURE uspLawItemsDetail_GetListPaging
                @Page INT=1,@ListSize INT=20,@qSite NVARCHAR(20)='admin',
                @qKey NVARCHAR(50)='',@qWord NVARCHAR(200)='',@qSort NVARCHAR(50)='',
                @qCate INT=0,@qCate2 INT=0,@qCate3 INT=0,@qLL INT=0,@qLR INT=0,@qLN INT=0,@KeywordList NVARCHAR(MAX)=''
            AS BEGIN SET NOCOUNT ON; DECLARE @Offset INT=(@Page-1)*@ListSize;
                SELECT d.LD_IDX,d.LD_ITEM_NAME,d.LD_USE_AGE,d.LD_MATERIAL,
                    ISNULL(l.LL_TITLE,'') AS LL_TITLE,ISNULL(r.LR_TITLE,'') AS LR_TITLE,
                    ISNULL(n.LN_TITLE,'') AS LN_TITLE,ISNULL(n.LN_TITLE,'') AS LN_NOTIFY,
                    d.LD_IS_USE,CONVERT(NVARCHAR(10),d.LD_REG_DATE,120) AS LD_REG_DATE,
                    d.LL_IDX,d.LR_IDX,d.LN_IDX,d.LI_IDX
                FROM dpl_items_detail d
                LEFT JOIN dpl_regulation_legal l ON l.LL_IDX=d.LL_IDX
                LEFT JOIN dpl_regulation r ON r.LR_IDX=d.LR_IDX
                LEFT JOIN dpl_notify n ON n.LN_IDX=d.LN_IDX
                WHERE (@qLL=0 OR d.LL_IDX=@qLL) AND (@qLR=0 OR d.LR_IDX=@qLR) AND (@qLN=0 OR d.LN_IDX=@qLN)
                  AND (@qWord='' OR d.LD_ITEM_NAME LIKE '%'+@qWord+'%')
                ORDER BY d.LD_IDX DESC OFFSET @Offset ROWS FETCH NEXT @ListSize ROWS ONLY;
                SELECT COUNT(1) AS TOTAL FROM dpl_items_detail d
                WHERE (@qLL=0 OR d.LL_IDX=@qLL) AND (@qLR=0 OR d.LR_IDX=@qLR) AND (@qLN=0 OR d.LN_IDX=@qLN)
                  AND (@qWord='' OR d.LD_ITEM_NAME LIKE '%'+@qWord+'%');
            END
        """);
        dropAndCreate(conn, "uspLawItemsDetail_GetList", """
            CREATE PROCEDURE uspLawItemsDetail_GetList @qIsUse CHAR(1)='Y',@qIdx INT=0
            AS BEGIN SET NOCOUNT ON;
                SELECT LD_IDX,LD_ITEM_NAME FROM dpl_items_detail
                WHERE (@qIsUse='' OR LD_IS_USE=@qIsUse) AND (@qIdx=0 OR LD_IDX=@qIdx)
                ORDER BY LD_ITEM_NAME;
            END
        """);
        dropAndCreate(conn, "uspLawItemsDetail_GetInfo", """
            CREATE PROCEDURE uspLawItemsDetail_GetInfo @qSite NVARCHAR(20)='admin',@IDX INT
            AS BEGIN SET NOCOUNT ON;
                SELECT d.LD_IDX,d.LD_ITEM_NAME,d.LI_IDX,d.LN_IDX,d.LL_IDX,d.LR_IDX,
                    d.LD_USE_AGE,d.LD_MATERIAL,d.LD_IS_USE,
                    CONVERT(NVARCHAR(19),d.LD_REG_DATE,120) AS LD_REG_DATE,d.LD_REG_USER,
                    ISNULL(l.LL_TITLE,'') AS LL_TITLE,ISNULL(r.LR_TITLE,'') AS LR_TITLE,
                    ISNULL(n.LN_TITLE,'') AS LN_TITLE,ISNULL(i.LI_LEGAL_NAME,'') AS LI_LEGAL_NAME
                FROM dpl_items_detail d LEFT JOIN dpl_regulation_legal l ON l.LL_IDX=d.LL_IDX
                LEFT JOIN dpl_regulation r ON r.LR_IDX=d.LR_IDX LEFT JOIN dpl_notify n ON n.LN_IDX=d.LN_IDX
                LEFT JOIN dpl_items i ON i.LI_IDX=d.LI_IDX WHERE d.LD_IDX=@IDX;
            END
        """);
        dropAndCreate(conn, "uspLawItemsDetail_PutInfo", """
            CREATE PROCEDURE uspLawItemsDetail_PutInfo
                @ACTION NVARCHAR(10),@IDX INT=0,@ITEM_NAME NVARCHAR(200),@LI_IDX INT=0,
                @USE_AGE NVARCHAR(100)='',@MATERIAL NVARCHAR(100)='',@LC_IDX INT=0,
                @SAMPLE_ATTA_PATH NVARCHAR(500)='',@IS_USE CHAR(1)='Y',
                @REG_USER NVARCHAR(50)='',@REG_IP NVARCHAR(40)='',
                @LDK_KEYWORDS NVARCHAR(MAX)='',@LC_CATEGORIES NVARCHAR(MAX)='',
                @RTN_MSG NVARCHAR(200)='' OUTPUT,@RTN_IDX INT=0 OUTPUT
            AS BEGIN SET NOCOUNT ON; SET @RTN_MSG=''; SET @RTN_IDX=0;
                DECLARE @LN_IDX INT=0,@LL_IDX INT=0,@LR_IDX INT=0;
                SELECT @LN_IDX=ISNULL(LN_IDX,0),@LL_IDX=ISNULL(LL_IDX,0),@LR_IDX=ISNULL(LR_IDX,0) FROM dpl_items WHERE LI_IDX=@LI_IDX;
                IF @ACTION='ADD' BEGIN
                    IF @ITEM_NAME='' BEGIN SET @RTN_MSG='일반 품목명을 입력해주세요.'; RETURN; END
                    INSERT INTO dpl_items_detail (LI_IDX,LN_IDX,LL_IDX,LR_IDX,LD_ITEM_NAME,LD_USE_AGE,LD_MATERIAL,LD_IS_USE,LD_REG_USER,LD_REG_IP,LD_REG_DATE)
                    VALUES (@LI_IDX,@LN_IDX,@LL_IDX,@LR_IDX,@ITEM_NAME,@USE_AGE,@MATERIAL,@IS_USE,@REG_USER,@REG_IP,GETDATE());
                    SET @RTN_IDX=SCOPE_IDENTITY();
                END ELSE IF @ACTION='MOD' BEGIN
                    UPDATE dpl_items_detail SET LI_IDX=@LI_IDX,LN_IDX=@LN_IDX,LL_IDX=@LL_IDX,LR_IDX=@LR_IDX,
                    LD_ITEM_NAME=@ITEM_NAME,LD_USE_AGE=@USE_AGE,LD_MATERIAL=@MATERIAL,LD_IS_USE=@IS_USE,
                    LD_UPD_USER=@REG_USER,LD_UPD_DATE=GETDATE() WHERE LD_IDX=@IDX; SET @RTN_IDX=@IDX;
                END ELSE IF @ACTION='DEL' BEGIN DELETE FROM dpl_items_detail WHERE LD_IDX=@IDX; SET @RTN_IDX=@IDX; END
                ELSE SET @RTN_MSG='잘못된 ACTION입니다.';
            END
        """);
        System.out.println("[DPL] items SP 완료");
    }

    private void seedItemsData(Connection conn) throws Exception {
        try (Statement st = conn.createStatement()) {
            ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM dpl_items"); rs.next();
            if (rs.getInt(1)==0)
                st.execute("INSERT INTO dpl_items (LN_IDX,LL_IDX,LR_IDX,LI_LEGAL_NAME,LI_SCOPE,LI_IS_USE,LI_REG_USER,LI_REG_DATE) VALUES (1,1,1,N'안전모',N'모든 산업현장 근무자용','Y','admin',GETDATE()),(2,2,2,N'방독면',N'유해가스 발생 작업장용','Y','admin',GETDATE()),(3,3,3,N'안전화',N'중량물 취급 작업장용','Y','admin',GETDATE()),(4,4,4,N'소화기',N'사무실 및 작업장 비치용','Y','admin',GETDATE()),(5,5,5,N'구명조끼',N'수상 작업 및 선박 탑승자용','Y','admin',GETDATE())");
        }
        try (Statement st = conn.createStatement()) {
            ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM dpl_items_detail"); rs.next();
            if (rs.getInt(1)==0)
                st.execute("INSERT INTO dpl_items_detail (LI_IDX,LN_IDX,LL_IDX,LR_IDX,LD_ITEM_NAME,LD_USE_AGE,LD_MATERIAL,LD_IS_USE,LD_REG_USER,LD_REG_DATE) VALUES (1,1,1,1,N'일반 안전모',N'성인용',N'ABS 수지','Y','admin',GETDATE()),(2,2,2,2,N'방독 마스크',N'18세 이상',N'실리콘+활성탄 필터','Y','admin',GETDATE()),(3,3,3,3,N'중작업용 안전화',N'성인용',N'강화 가죽+철심','Y','admin',GETDATE()),(4,4,4,4,N'ABC 분말 소화기',N'성인용',N'금속 용기+분말약제','Y','admin',GETDATE()),(5,5,5,5,N'성인용 구명조끼',N'40~110kg',N'발포 폼+나일론','Y','admin',GETDATE())");
        }
    }

    // ── 유틸 ─────────────────────────────────────────────────────────
    private void exec(Connection conn, String sql) throws Exception {
        try (Statement st = conn.createStatement()) { st.execute(sql.strip()); }
    }

    private void dropAndCreate(Connection conn, String spName, String createSql) throws Exception {
        exec(conn, "IF OBJECT_ID('" + spName + "','P') IS NOT NULL DROP PROCEDURE " + spName);
        exec(conn, createSql);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {}
}
