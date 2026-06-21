package db;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.sql.*;

/**
 * 앱 기동 시 전체 SP 생성 + 실제 DPL 데이터 초기화
 */
@WebListener
public class RegulationSetupListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("[DPL] SetupListener 시작");
        try (Connection conn = DBPool.getConnection()) {
            createAllTables(conn);
            createAllSPs(conn);
            insertRealDataIfEmpty(conn);
            System.out.println("[DPL] SetupListener 완료");
        } catch (Exception e) {
            System.err.println("[DPL] SetupListener 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ══ 테이블 생성 ══════════════════════════════════════════════════
    private void createAllTables(Connection conn) throws Exception {
        exec(conn, "IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME='dpl_regulation_legal') CREATE TABLE dpl_regulation_legal (LL_IDX INT IDENTITY(1,1) PRIMARY KEY, LL_TITLE NVARCHAR(200) NOT NULL, LL_DEPT NVARCHAR(200) DEFAULT '', LL_IS_USE CHAR(1) DEFAULT 'Y', LL_SORT INT DEFAULT 0, REG_USER NVARCHAR(50), REG_DATE DATETIME DEFAULT GETDATE())");
        exec(conn, "IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME='dpl_regulation_legal' AND COLUMN_NAME='LL_DEPT') ALTER TABLE dpl_regulation_legal ADD LL_DEPT NVARCHAR(200) DEFAULT ''");
        exec(conn, "IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME='dpl_regulation') CREATE TABLE dpl_regulation (LR_IDX INT IDENTITY(1,1) PRIMARY KEY, LR_TITLE NVARCHAR(300) NOT NULL, LL_IDX INT DEFAULT 0, LR_CONDITION NVARCHAR(MAX), LR_CERTIFY_GUIDE NVARCHAR(MAX), LR_PENALTY NVARCHAR(MAX), LR_IS_USE CHAR(1) DEFAULT 'Y', REG_USER NVARCHAR(50), REG_IP NVARCHAR(40), REG_DATE DATETIME DEFAULT GETDATE(), UPD_USER NVARCHAR(50), UPD_DATE DATETIME)");
        exec(conn, "IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME='dpl_notify') CREATE TABLE dpl_notify (LN_IDX INT IDENTITY(1,1) PRIMARY KEY, LN_TITLE NVARCHAR(200) NOT NULL, LL_IDX INT DEFAULT 0, LR_IDX INT DEFAULT 0, LN_HISTORY NVARCHAR(MAX), LN_IS_USE CHAR(1) DEFAULT 'Y', LN_REG_USER NVARCHAR(50), LN_REG_IP NVARCHAR(40), LN_REG_DATE DATETIME DEFAULT GETDATE(), LN_UPD_USER NVARCHAR(50), LN_UPD_IP NVARCHAR(40), LN_UPD_DATE DATETIME)");
        exec(conn, "IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME='dpl_safety') CREATE TABLE dpl_safety (LS_IDX INT IDENTITY(1,1) PRIMARY KEY, LN_IDX INT DEFAULT 0, LL_IDX INT DEFAULT 0, LR_IDX INT DEFAULT 0, LS_TITLE NVARCHAR(200) NOT NULL, LS_CONTENT NVARCHAR(MAX), LS_IS_USE CHAR(1) DEFAULT 'Y', LS_REG_USER NVARCHAR(50), LS_REG_IP NVARCHAR(40), LS_REG_DATE DATETIME DEFAULT GETDATE(), LS_UPD_USER NVARCHAR(50), LS_UPD_IP NVARCHAR(40), LS_UPD_DATE DATETIME)");
        exec(conn, "IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME='dpl_items') CREATE TABLE dpl_items (LI_IDX INT IDENTITY(1,1) PRIMARY KEY, LN_IDX INT DEFAULT 0, LL_IDX INT DEFAULT 0, LR_IDX INT DEFAULT 0, LI_LEGAL_NAME NVARCHAR(200) NOT NULL, LI_SCOPE NVARCHAR(MAX), LI_EXCEPTION NVARCHAR(MAX), LO_IDX INT DEFAULT 0, LG_IDX INT DEFAULT 0, LI_IS_USE CHAR(1) DEFAULT 'Y', LI_REG_USER NVARCHAR(50), LI_REG_IP NVARCHAR(40), LI_REG_DATE DATETIME DEFAULT GETDATE(), LI_UPD_USER NVARCHAR(50), LI_UPD_DATE DATETIME)");
        exec(conn, "IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME='dpl_items_detail') CREATE TABLE dpl_items_detail (LD_IDX INT IDENTITY(1,1) PRIMARY KEY, LI_IDX INT DEFAULT 0, LN_IDX INT DEFAULT 0, LL_IDX INT DEFAULT 0, LR_IDX INT DEFAULT 0, LD_ITEM_NAME NVARCHAR(200) NOT NULL, LD_USE_AGE NVARCHAR(100), LD_MATERIAL NVARCHAR(100), LD_SAMPLE_ATTA_PATH NVARCHAR(500), LD_IS_USE CHAR(1) DEFAULT 'Y', LD_REG_USER NVARCHAR(50), LD_REG_IP NVARCHAR(40), LD_REG_DATE DATETIME DEFAULT GETDATE(), LD_UPD_USER NVARCHAR(50), LD_UPD_DATE DATETIME)");
        System.out.println("[DPL] 테이블 생성 완료");
    }

    // ══ 실제 DPL 데이터 초기화 ══════════════════════════════════════

    private void insertRealDataIfEmpty(Connection conn) throws Exception {
        try (Statement st = conn.createStatement()) {
            ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM dpl_regulation_legal");
            rs.next();
            if (rs.getInt(1) > 0) { System.out.println("[DPL] 데이터 이미 존재 — 스킵"); return; }
        }
        insertRealData(conn);
    }

        public void insertRealData(Connection conn) throws Exception {
        System.out.println("[DPL] 실제 DPL 데이터 삽입 시작");

        // ── 규제법률 (실제 DPL 개발서버와 동일) ──
        exec(conn, "SET IDENTITY_INSERT dpl_regulation_legal ON");
        exec(conn, "INSERT INTO dpl_regulation_legal (LL_IDX,LL_TITLE,LL_DEPT,LL_IS_USE,LL_SORT,REG_DATE) VALUES " +
            "(1,N'전기용품 및 생활용품안전관리법',N'산업통상자원부 / 국가기술표준원','Y',1,GETDATE())," +
            "(2,N'어린이제품안전특별법',N'산업통상자원부 / 국가기술표준원','Y',2,GETDATE())," +
            "(3,N'생활화학제품 및 살생물제의 안전관리에 관한 법률',N'환경부 / 화학물질안전원','Y',3,GETDATE())," +
            "(4,N'식품위생법',N'식품의약품안전처','Y',4,GETDATE())," +
            "(5,N'약사법',N'식품의약품안전처','Y',5,GETDATE())," +
            "(6,N'화장품법',N'식품의약품안전처','Y',6,GETDATE())," +
            "(7,N'위생용품관리법',N'식품의약품안전처','Y',7,GETDATE())," +
            "(8,N'대외무역법',N'산업통상자원부 / 무역위원회','Y',8,GETDATE())," +
            "(9,N'표시·광고법',N'공정거래위원회','Y',9,GETDATE())," +
            "(10,N'전자상거래법',N'공정거래위원회','Y',10,GETDATE())");
        exec(conn, "SET IDENTITY_INSERT dpl_regulation_legal OFF");

        // ── 규제사항 ──
        exec(conn, "SET IDENTITY_INSERT dpl_regulation ON");
        exec(conn, "INSERT INTO dpl_regulation (LR_IDX,LR_TITLE,LL_IDX,LR_CONDITION,LR_CERTIFY_GUIDE,LR_PENALTY,LR_IS_USE,REG_DATE) VALUES " +
            "(1,N'안전확인',1,N'안전확인대상 전기용품을 제조·수입하는 자',N'지정시험기관 시험 → KC마크 취득 → 안전확인 신고',N'3년 이하 징역 또는 3천만원 이하 벌금','Y',GETDATE())," +
            "(2,N'공급자적합성확인(SDoC)',1,N'공급자적합성확인 생활용품을 제조·수입하는 자',N'자체시험 또는 시험의뢰 → 공급자적합성 확인',N'1년 이하 징역 또는 1천만원 이하 벌금','Y',GETDATE())," +
            "(3,N'안전기준준수',1,N'안전기준준수 대상 생활용품을 제조·수입하는 자',N'안전기준 준수 → 별도 신고 불필요',N'500만원 이하 과태료','Y',GETDATE())," +
            "(4,N'안전인증',2,N'어린이제품안전특별법 제17조에 따른 안전인증대상 어린이제품',N'KC마크 인증 취득 후 제조·수입',N'3년 이하 징역 또는 3천만원 이하 벌금','Y',GETDATE())," +
            "(5,N'어린이제품 공급자적합성확인',2,N'어린이제품안전특별법 제22조에 따른 대상 제품',N'시험기관 시험성적서 확보 → 적합성확인 신고',N'1년 이하 징역 또는 1천만원 이하 벌금','Y',GETDATE())," +
            "(6,N'안전기준준수(어린이)',2,N'어린이제품 안전기준 준수대상 제품',N'안전기준 준수 → 제품에 안전기준 준수 확인 표시',N'500만원 이하 과태료','Y',GETDATE())," +
            "(7,N'위해우려제품 신고',3,N'위해우려제품 제조·수입업자',N'제품안전정보원에 신고 → 승인 → 판매',N'5년 이하 징역 또는 5천만원 이하 벌금','Y',GETDATE())," +
            "(8,N'식품 등의 표시 기준',4,N'식품, 식품첨가물, 기구 또는 용기·포장',N'식약처 고시 표시기준 준수',N'3년 이하 징역 또는 3천만원 이하 벌금','Y',GETDATE())," +
            "(9,N'원산지 표시',8,N'수출입 물품 원산지 표시 의무',N'원산지 표시규정 준수 → 수입신고 시 확인',N'3년 이하 징역 또는 3천만원 이하 벌금','Y',GETDATE())," +
            "(10,N'표시·광고의 공정화',9,N'사업자의 상품 표시·광고',N'허위·과장 광고 금지 / 사실에 근거한 표시·광고',N'2년 이하 징역 또는 1억5천만원 이하 벌금','Y',GETDATE())");
        exec(conn, "SET IDENTITY_INSERT dpl_regulation OFF");

        // ── 고시(부속서) ──
        exec(conn, "SET IDENTITY_INSERT dpl_notify ON");
        exec(conn, "INSERT INTO dpl_notify (LN_IDX,LN_TITLE,LL_IDX,LR_IDX,LN_HISTORY,LN_IS_USE,LN_REG_DATE) VALUES " +
            "(1,N'전기용품 안전기준 (부속서 1) — 가정용전기기기',1,1,N'2024-01-01 개정','Y',GETDATE())," +
            "(2,N'전기용품 안전기준 (부속서 2) — 전동기기계기구',1,1,N'2024-01-01 개정','Y',GETDATE())," +
            "(3,N'생활용품 안전기준 — 생활용 섬유제품',1,2,N'2023-06-01 개정','Y',GETDATE())," +
            "(4,N'어린이제품 공통안전기준',2,4,N'2024-03-01 개정','Y',GETDATE())," +
            "(5,N'어린이용 완구 안전기준',2,4,N'2023-09-01 개정','Y',GETDATE())," +
            "(6,N'어린이용 의류 안전기준',2,5,N'2023-12-01 개정','Y',GETDATE())," +
            "(7,N'살생물제 안전기준 — 항균제품',3,7,N'2023-07-01 최초 제정','Y',GETDATE())," +
            "(8,N'식품 등의 표시기준 — 영양표시',4,8,N'2024-01-01 개정','Y',GETDATE())," +
            "(9,N'원산지 표시요령 (산업통상자원부 고시)',8,9,N'2023-11-01 개정','Y',GETDATE())," +
            "(10,N'표시·광고 내용의 실증에 관한 운영고시',9,10,N'2023-04-01 개정','Y',GETDATE())");
        exec(conn, "SET IDENTITY_INSERT dpl_notify OFF");

        // ── 안전요건 ──
        exec(conn, "SET IDENTITY_INSERT dpl_safety ON");
        exec(conn, "INSERT INTO dpl_safety (LS_IDX,LN_IDX,LL_IDX,LR_IDX,LS_TITLE,LS_CONTENT,LS_IS_USE,LS_REG_DATE) VALUES " +
            "(1,1,1,1,N'과전류 및 단락보호 요건',N'<p>과전류 및 단락보호장치를 필히 구비하여야 하며, IEC 60335-1 기준에 따라 시험을 통과하여야 합니다.</p>','Y',GETDATE())," +
            "(2,1,1,1,N'절연내력 시험 요건',N'<p>제품의 절연체는 IEC 60068 환경시험 후 절연내력 2,000V 이상을 견뎌야 합니다.</p>','Y',GETDATE())," +
            "(3,4,2,4,N'물리적·기계적 특성 안전요건',N'<p>어린이 제품에 사용되는 부품은 질식·삼킴 위험이 없어야 하며, KS C IEC 62115 기준을 준수합니다.</p>','Y',GETDATE())," +
            "(4,4,2,4,N'유해물질 함유 기준',N'<p>납(Pb) 90mg/kg 이하, 카드뮴 75mg/kg 이하, 프탈레이트계 가소제 각 0.1% 이하를 준수합니다.</p>','Y',GETDATE())," +
            "(5,5,2,4,N'완구 전기·기계 안전요건',N'<p>3세 미만 대상 완구의 최소 부품 크기: 직경 31.7mm, 길이 57.1mm 이상이어야 합니다.</p>','Y',GETDATE())," +
            "(6,8,4,8,N'영양성분 표시 요건',N'<p>열량, 나트륨, 탄수화물, 당류, 지방, 트랜스지방, 포화지방, 콜레스테롤, 단백질을 의무 표시합니다.</p>','Y',GETDATE())," +
            "(7,9,8,9,N'원산지 표시 방법 요건',N'<p>원산지 표시는 소비자가 쉽게 알아볼 수 있는 곳에 한글로 표시하여야 합니다. (8pt 이상)</p>','Y',GETDATE())," +
            "(8,3,1,2,N'내구성 및 세탁 안전요건',N'<p>섬유제품은 가정세탁 50회 후에도 기능 유지, 치수변화율 ±3% 이내이어야 합니다.</p>','Y',GETDATE())");
        exec(conn, "SET IDENTITY_INSERT dpl_safety OFF");

        // ── 품목정보(법정) ──
        exec(conn, "SET IDENTITY_INSERT dpl_items ON");
        exec(conn, "INSERT INTO dpl_items (LI_IDX,LN_IDX,LL_IDX,LR_IDX,LI_LEGAL_NAME,LI_SCOPE,LI_IS_USE,LI_REG_DATE) VALUES " +
            "(1,1,1,1,N'전기냉장고',N'가정용 전기냉장고 및 냉동고 (IEC 60335-2-24 적용)','Y',GETDATE())," +
            "(2,1,1,1,N'전기세탁기',N'가정용 전기세탁기 및 탈수기 (IEC 60335-2-7 적용)','Y',GETDATE())," +
            "(3,2,1,1,N'전기드릴',N'전동공구류 중 전기드릴 (IEC 60745 적용)','Y',GETDATE())," +
            "(4,3,1,2,N'면섬유 제품',N'면 함유량 80% 이상 섬유제품','Y',GETDATE())," +
            "(5,4,2,4,N'어린이용 장난감 (일반)',N'36개월 이상 어린이 대상 완구류','Y',GETDATE())," +
            "(6,5,2,4,N'어린이용 봉제완구',N'천 소재 기반의 인형·봉제 완구류','Y',GETDATE())," +
            "(7,6,2,5,N'어린이용 의류',N'14세 이하 어린이 대상 의류 및 잡화','Y',GETDATE())," +
            "(8,8,4,8,N'특수영양식품',N'영유아식, 성장기용 조제식, 영양보충용 식품','Y',GETDATE())," +
            "(9,9,8,9,N'해외직구 가전제품',N'해외 직접구매 대상 가전제품류','Y',GETDATE())," +
            "(10,10,9,10,N'화장품',N'기초·색조·기능성 화장품 전 품목','Y',GETDATE())");
        exec(conn, "SET IDENTITY_INSERT dpl_items OFF");

        // ── 세부품목정보 ──
        exec(conn, "SET IDENTITY_INSERT dpl_items_detail ON");
        exec(conn, "INSERT INTO dpl_items_detail (LD_IDX,LI_IDX,LN_IDX,LL_IDX,LR_IDX,LD_ITEM_NAME,LD_USE_AGE,LD_MATERIAL,LD_IS_USE,LD_REG_DATE) VALUES " +
            "(1,1,1,1,1,N'일반형 냉장고 (450L 이하)',N'성인용',N'ABS / 스테인리스','Y',GETDATE())," +
            "(2,1,1,1,1,N'김치냉장고',N'성인용',N'ABS / 스테인리스','Y',GETDATE())," +
            "(3,2,1,1,1,N'드럼세탁기',N'성인용',N'스테인리스 / ABS','Y',GETDATE())," +
            "(4,2,1,1,1,N'통돌이 세탁기',N'성인용',N'PP / ABS','Y',GETDATE())," +
            "(5,5,4,2,4,N'블록 완구',N'36개월 이상',N'ABS 무독성 수지','Y',GETDATE())," +
            "(6,5,4,2,4,N'모형자동차 (배터리 구동)',N'36개월 이상',N'ABS / PP','Y',GETDATE())," +
            "(7,6,5,2,4,N'봉제인형 (소)',N'0세 이상',N'폴리에스터 면 / PP솜','Y',GETDATE())," +
            "(8,7,6,2,5,N'아동용 티셔츠',N'2~14세',N'면(Cotton) 100%','Y',GETDATE())," +
            "(9,8,8,4,8,N'분유 (영아용 조제분유)',N'0~12개월',N'분말유(우유 원료)','Y',GETDATE())," +
            "(10,10,10,9,10,N'기초화장품 (스킨·로션)',N'성인용',N'수성 기반 복합 원료','Y',GETDATE())");
        exec(conn, "SET IDENTITY_INSERT dpl_items_detail OFF");

        System.out.println("[DPL] 실제 DPL 데이터 삽입 완료 (법률 10건, 규제사항 10건, 고시 10건, 안전요건 8건, 품목 10건, 세부품목 10건)");
    }

    // ══ SP 생성 (기존과 동일 — 함축) ═════════════════════════════════
    private void createAllSPs(Connection conn) throws Exception {
        // Legal SP
        dropAndCreate(conn, "uspLawRegulationLegal_GetListPaging",
            "CREATE PROCEDURE uspLawRegulationLegal_GetListPaging @Page INT=1,@ListSize INT=20,@qKey NVARCHAR(50)='',@qWord NVARCHAR(200)='',@qSort NVARCHAR(50)='' AS BEGIN SET NOCOUNT ON; DECLARE @Offset INT=(@Page-1)*@ListSize; SELECT LL_IDX,LL_TITLE,ISNULL(LL_DEPT,'') AS LL_DEPT,LL_IS_USE,CONVERT(NVARCHAR(10),REG_DATE,120) AS LL_REG_DATE FROM dpl_regulation_legal WHERE (@qWord='' OR (@qKey='TITLE' AND LL_TITLE LIKE '%'+@qWord+'%') OR (@qKey='DEPT' AND LL_DEPT LIKE '%'+@qWord+'%') OR (@qKey='' AND (LL_TITLE LIKE '%'+@qWord+'%' OR LL_DEPT LIKE '%'+@qWord+'%'))) ORDER BY LL_SORT,LL_IDX OFFSET @Offset ROWS FETCH NEXT @ListSize ROWS ONLY; SELECT COUNT(*) AS TOTAL FROM dpl_regulation_legal WHERE (@qWord='' OR LL_TITLE LIKE '%'+@qWord+'%' OR LL_DEPT LIKE '%'+@qWord+'%'); END");
        dropAndCreate(conn, "uspLawRegulationLegal_GetList",
            "CREATE PROCEDURE uspLawRegulationLegal_GetList @qIsUse CHAR(1)='Y',@qParent INT=0,@qIdx INT=0 AS BEGIN SET NOCOUNT ON; SELECT LL_IDX,LL_TITLE,ISNULL(LL_DEPT,'') AS LL_DEPT,LL_IS_USE FROM dpl_regulation_legal WHERE LL_IS_USE=@qIsUse ORDER BY LL_SORT,LL_IDX; END");
        dropAndCreate(conn, "uspLawRegulationLegal_GetInfo",
            "CREATE PROCEDURE uspLawRegulationLegal_GetInfo @IDX INT AS BEGIN SET NOCOUNT ON; SELECT LL_IDX,LL_TITLE,ISNULL(LL_DEPT,'') AS LL_DEPT,LL_IS_USE,REG_USER AS LL_REG_USER,CONVERT(NVARCHAR(19),REG_DATE,120) AS LL_REG_DATE FROM dpl_regulation_legal WHERE LL_IDX=@IDX; END");
        dropAndCreate(conn, "uspLawRegulationLegal_PutInfo",
            "CREATE PROCEDURE uspLawRegulationLegal_PutInfo @ACTION NVARCHAR(10),@IDX INT=0,@TITLE NVARCHAR(200),@DEPT NVARCHAR(200)='',@IS_USE CHAR(1)='Y',@REG_USER NVARCHAR(50)='',@REG_IP NVARCHAR(40)='',@RTN_MSG NVARCHAR(500)='' OUTPUT AS BEGIN SET NOCOUNT ON; SET @RTN_MSG=''; IF @ACTION='ADD' BEGIN IF @TITLE='' BEGIN SET @RTN_MSG='법규명을 입력하세요.'; RETURN; END INSERT INTO dpl_regulation_legal (LL_TITLE,LL_DEPT,LL_IS_USE,LL_SORT,REG_DATE) VALUES (@TITLE,@DEPT,@IS_USE,0,GETDATE()); END ELSE IF @ACTION='MOD' UPDATE dpl_regulation_legal SET LL_TITLE=@TITLE,LL_DEPT=@DEPT,LL_IS_USE=@IS_USE WHERE LL_IDX=@IDX; ELSE IF @ACTION='DEL' DELETE FROM dpl_regulation_legal WHERE LL_IDX=@IDX; END");

        // Regulation SP
        dropAndCreate(conn, "uspLawRegulation_GetListPaging",
            "CREATE PROCEDURE uspLawRegulation_GetListPaging @Page INT=1,@ListSize INT=20,@qKey NVARCHAR(50)='',@qWord NVARCHAR(200)='',@qLL INT=0,@qSort NVARCHAR(50)='' AS BEGIN SET NOCOUNT ON; DECLARE @Offset INT=(@Page-1)*@ListSize; SELECT r.LR_IDX,r.LR_TITLE,r.LL_IDX,ISNULL(l.LL_TITLE,'') AS LL_TITLE,r.LR_IS_USE,CONVERT(NVARCHAR(10),r.REG_DATE,120) AS LR_REG_DATE,r.REG_USER AS LR_REG_USER,CONVERT(NVARCHAR(10),r.UPD_DATE,120) AS LR_UPD_DATE,r.UPD_USER AS LR_UPD_USER FROM dpl_regulation r LEFT JOIN dpl_regulation_legal l ON l.LL_IDX=r.LL_IDX WHERE (@qLL=0 OR r.LL_IDX=@qLL) AND (@qWord='' OR (@qKey='TITLE' AND r.LR_TITLE LIKE '%'+@qWord+'%') OR (@qKey='' AND r.LR_TITLE LIKE '%'+@qWord+'%')) ORDER BY r.LR_IDX DESC OFFSET @Offset ROWS FETCH NEXT @ListSize ROWS ONLY; SELECT COUNT(*) AS TOTAL FROM dpl_regulation r WHERE (@qLL=0 OR r.LL_IDX=@qLL) AND (@qWord='' OR r.LR_TITLE LIKE '%'+@qWord+'%'); END");
        dropAndCreate(conn, "uspLawRegulation_GetList",
            "CREATE PROCEDURE uspLawRegulation_GetList @qIsUse CHAR(1)='Y',@qParent INT=0,@qIdx INT=0 AS BEGIN SET NOCOUNT ON; SELECT LR_IDX,LR_TITLE,LL_IDX,LR_IS_USE FROM dpl_regulation WHERE (@qIsUse='' OR LR_IS_USE=@qIsUse) AND (@qParent=0 OR LL_IDX=@qParent) ORDER BY LR_TITLE; END");
        dropAndCreate(conn, "uspLawRegulation_GetInfo",
            "CREATE PROCEDURE uspLawRegulation_GetInfo @IDX INT AS BEGIN SET NOCOUNT ON; SELECT r.LR_IDX,r.LR_TITLE,r.LL_IDX,ISNULL(l.LL_TITLE,'') AS LL_TITLE,r.LR_CONDITION,r.LR_CERTIFY_GUIDE,r.LR_PENALTY,r.LR_IS_USE,r.REG_USER AS LR_REG_USER,CONVERT(NVARCHAR(19),r.REG_DATE,120) AS LR_REG_DATE,r.UPD_USER AS LR_UPD_USER,CONVERT(NVARCHAR(19),r.UPD_DATE,120) AS LR_UPD_DATE FROM dpl_regulation r LEFT JOIN dpl_regulation_legal l ON l.LL_IDX=r.LL_IDX WHERE r.LR_IDX=@IDX; END");
        dropAndCreate(conn, "uspLawRegulation_PutInfo",
            "CREATE PROCEDURE uspLawRegulation_PutInfo @ACTION NVARCHAR(10),@IDX INT=0,@TITLE NVARCHAR(300),@LL_IDX INT=0,@CONDITION NVARCHAR(MAX)='',@CERTIFY_GUIDE NVARCHAR(MAX)='',@PENALTY NVARCHAR(MAX)='',@IS_USE CHAR(1)='Y',@REG_USER NVARCHAR(50)='',@REG_IP NVARCHAR(40)='',@RTN_MSG NVARCHAR(500)='' OUTPUT,@RTN_IDX INT=0 OUTPUT AS BEGIN SET NOCOUNT ON; SET @RTN_MSG=''; SET @RTN_IDX=@IDX; IF @ACTION='ADD' BEGIN IF @TITLE='' BEGIN SET @RTN_MSG='제목을 입력하세요.'; RETURN; END INSERT INTO dpl_regulation (LR_TITLE,LL_IDX,LR_CONDITION,LR_CERTIFY_GUIDE,LR_PENALTY,LR_IS_USE,REG_USER,REG_IP,REG_DATE) VALUES (@TITLE,@LL_IDX,@CONDITION,@CERTIFY_GUIDE,@PENALTY,@IS_USE,@REG_USER,@REG_IP,GETDATE()); SET @RTN_IDX=SCOPE_IDENTITY(); END ELSE IF @ACTION='MOD' UPDATE dpl_regulation SET LR_TITLE=@TITLE,LL_IDX=@LL_IDX,LR_CONDITION=@CONDITION,LR_CERTIFY_GUIDE=@CERTIFY_GUIDE,LR_PENALTY=@PENALTY,LR_IS_USE=@IS_USE,UPD_USER=@REG_USER,UPD_DATE=GETDATE() WHERE LR_IDX=@IDX; ELSE IF @ACTION='DEL' DELETE FROM dpl_regulation WHERE LR_IDX=@IDX; END");

        // Notify SP
        dropAndCreate(conn, "uspLawNotify_GetListPaging",
            "CREATE PROCEDURE uspLawNotify_GetListPaging @Page INT=1,@ListSize INT=20,@qKey NVARCHAR(50)='',@qWord NVARCHAR(200)='',@qLL INT=0,@qLR INT=0,@qSort NVARCHAR(50)='' AS BEGIN SET NOCOUNT ON; DECLARE @Offset INT=(@Page-1)*@ListSize; SELECT n.LN_IDX,n.LN_TITLE,n.LN_TITLE AS LN_NOTIFY,ISNULL(l.LL_TITLE,'') AS LL_TITLE,ISNULL(r.LR_TITLE,'') AS LR_TITLE,n.LN_IS_USE,CONVERT(NVARCHAR(10),n.LN_REG_DATE,120) AS LN_REG_DATE FROM dpl_notify n LEFT JOIN dpl_regulation_legal l ON l.LL_IDX=n.LL_IDX LEFT JOIN dpl_regulation r ON r.LR_IDX=n.LR_IDX WHERE (@qLL=0 OR n.LL_IDX=@qLL) AND (@qLR=0 OR n.LR_IDX=@qLR) AND (@qWord='' OR n.LN_TITLE LIKE '%'+@qWord+'%') ORDER BY n.LN_IDX DESC OFFSET @Offset ROWS FETCH NEXT @ListSize ROWS ONLY; SELECT COUNT(1) AS TOTAL FROM dpl_notify n WHERE (@qLL=0 OR n.LL_IDX=@qLL) AND (@qLR=0 OR n.LR_IDX=@qLR) AND (@qWord='' OR n.LN_TITLE LIKE '%'+@qWord+'%'); END");
        dropAndCreate(conn, "uspLawNotify_GetList",
            "CREATE PROCEDURE uspLawNotify_GetList @qIsUse CHAR(1)='Y',@qParent INT=0,@qIdx INT=0 AS BEGIN SET NOCOUNT ON; SELECT LN_IDX,LN_TITLE,LN_TITLE AS LN_NOTIFY FROM dpl_notify WHERE (@qIsUse='' OR LN_IS_USE=@qIsUse) AND (@qParent=0 OR LR_IDX=@qParent) AND (@qIdx=0 OR LN_IDX=@qIdx) ORDER BY LN_TITLE; END");
        dropAndCreate(conn, "uspLawNotify_GetInfo",
            "CREATE PROCEDURE uspLawNotify_GetInfo @IDX INT AS BEGIN SET NOCOUNT ON; SELECT n.LN_IDX,n.LN_TITLE,n.LN_TITLE AS LN_NOTIFY,n.LL_IDX,n.LR_IDX,n.LN_HISTORY,n.LN_IS_USE,n.LN_REG_USER,CONVERT(NVARCHAR(19),n.LN_REG_DATE,120) AS LN_REG_DATE,n.LN_UPD_USER,CONVERT(NVARCHAR(19),n.LN_UPD_DATE,120) AS LN_UPD_DATE,ISNULL(l.LL_TITLE,'') AS LL_TITLE,ISNULL(r.LR_TITLE,'') AS LR_TITLE FROM dpl_notify n LEFT JOIN dpl_regulation_legal l ON l.LL_IDX=n.LL_IDX LEFT JOIN dpl_regulation r ON r.LR_IDX=n.LR_IDX WHERE n.LN_IDX=@IDX; END");
        dropAndCreate(conn, "uspLawNotify_PutInfo",
            "CREATE PROCEDURE uspLawNotify_PutInfo @ACTION NVARCHAR(10),@IDX INT=0,@NOTIFY NVARCHAR(200),@LR_IDX INT=0,@HISTORY NVARCHAR(MAX)='',@IS_USE CHAR(1)='Y',@ATTA_FILES NVARCHAR(MAX)='',@REG_USER NVARCHAR(50)='',@REG_IP NVARCHAR(40)='',@RTN_MSG NVARCHAR(200)='' OUTPUT,@RTN_IDX INT=0 OUTPUT AS BEGIN SET NOCOUNT ON; SET @RTN_MSG=''; SET @RTN_IDX=0; DECLARE @LL_IDX INT=0; SELECT @LL_IDX=ISNULL(LL_IDX,0) FROM dpl_regulation WHERE LR_IDX=@LR_IDX; IF @ACTION='ADD' BEGIN IF @NOTIFY='' BEGIN SET @RTN_MSG='정부고시명을 입력해주세요.'; RETURN; END INSERT INTO dpl_notify (LN_TITLE,LL_IDX,LR_IDX,LN_HISTORY,LN_IS_USE,LN_REG_USER,LN_REG_IP,LN_REG_DATE) VALUES (@NOTIFY,@LL_IDX,@LR_IDX,@HISTORY,@IS_USE,@REG_USER,@REG_IP,GETDATE()); SET @RTN_IDX=SCOPE_IDENTITY(); END ELSE IF @ACTION='MOD' BEGIN UPDATE dpl_notify SET LN_TITLE=@NOTIFY,LL_IDX=@LL_IDX,LR_IDX=@LR_IDX,LN_HISTORY=@HISTORY,LN_IS_USE=@IS_USE,LN_UPD_USER=@REG_USER,LN_UPD_IP=@REG_IP,LN_UPD_DATE=GETDATE() WHERE LN_IDX=@IDX; SET @RTN_IDX=@IDX; END ELSE IF @ACTION='DEL' BEGIN DELETE FROM dpl_notify WHERE LN_IDX=@IDX; SET @RTN_IDX=@IDX; END END");

        // Safety SP
        dropAndCreate(conn, "uspLawSafety_GetListPaging",
            "CREATE PROCEDURE uspLawSafety_GetListPaging @Page INT=1,@ListSize INT=20,@qKey NVARCHAR(50)='',@qWord NVARCHAR(200)='',@qLL INT=0,@qLR INT=0,@qLN INT=0,@qSort NVARCHAR(50)='' AS BEGIN SET NOCOUNT ON; DECLARE @Offset INT=(@Page-1)*@ListSize; SELECT s.LS_IDX,s.LS_TITLE,ISNULL(n.LN_TITLE,'') AS LN_TITLE,ISNULL(r.LR_TITLE,'') AS LR_TITLE,ISNULL(l.LL_TITLE,'') AS LL_TITLE,s.LS_IS_USE,CONVERT(NVARCHAR(10),s.LS_REG_DATE,120) AS LS_REG_DATE,s.LL_IDX,s.LR_IDX,s.LN_IDX FROM dpl_safety s LEFT JOIN dpl_regulation_legal l ON l.LL_IDX=s.LL_IDX LEFT JOIN dpl_regulation r ON r.LR_IDX=s.LR_IDX LEFT JOIN dpl_notify n ON n.LN_IDX=s.LN_IDX WHERE (@qLL=0 OR s.LL_IDX=@qLL) AND (@qLR=0 OR s.LR_IDX=@qLR) AND (@qLN=0 OR s.LN_IDX=@qLN) AND (@qWord='' OR s.LS_TITLE LIKE '%'+@qWord+'%') ORDER BY s.LS_IDX DESC OFFSET @Offset ROWS FETCH NEXT @ListSize ROWS ONLY; SELECT COUNT(1) AS TOTAL FROM dpl_safety s WHERE (@qLL=0 OR s.LL_IDX=@qLL) AND (@qLR=0 OR s.LR_IDX=@qLR) AND (@qLN=0 OR s.LN_IDX=@qLN) AND (@qWord='' OR s.LS_TITLE LIKE '%'+@qWord+'%'); END");
        dropAndCreate(conn, "uspLawSafety_GetList",
            "CREATE PROCEDURE uspLawSafety_GetList @qFlag NVARCHAR(10)='',@qIsUse CHAR(1)='Y',@qParent INT=0,@qIdx INT=0 AS BEGIN SET NOCOUNT ON; SELECT LS_IDX,LS_TITLE FROM dpl_safety WHERE (@qIsUse='' OR LS_IS_USE=@qIsUse) AND (@qParent=0 OR LN_IDX=@qParent) AND (@qIdx=0 OR LS_IDX=@qIdx) ORDER BY LS_TITLE; END");
        dropAndCreate(conn, "uspLawSafety_GetInfo",
            "CREATE PROCEDURE uspLawSafety_GetInfo @IDX INT AS BEGIN SET NOCOUNT ON; SELECT s.LS_IDX,s.LN_IDX,s.LL_IDX,s.LR_IDX,s.LS_TITLE,s.LS_CONTENT,s.LS_IS_USE,s.LS_REG_USER,CONVERT(NVARCHAR(19),s.LS_REG_DATE,120) AS LS_REG_DATE,s.LS_UPD_USER,CONVERT(NVARCHAR(19),s.LS_UPD_DATE,120) AS LS_UPD_DATE,ISNULL(l.LL_TITLE,'') AS LL_TITLE,ISNULL(r.LR_TITLE,'') AS LR_TITLE,ISNULL(n.LN_TITLE,'') AS LN_TITLE FROM dpl_safety s LEFT JOIN dpl_regulation_legal l ON l.LL_IDX=s.LL_IDX LEFT JOIN dpl_regulation r ON r.LR_IDX=s.LR_IDX LEFT JOIN dpl_notify n ON n.LN_IDX=s.LN_IDX WHERE s.LS_IDX=@IDX; END");
        dropAndCreate(conn, "uspLawSafety_PutInfo",
            "CREATE PROCEDURE uspLawSafety_PutInfo @ACTION NVARCHAR(10),@IDX INT=0,@LN_IDX INT=0,@TITLE NVARCHAR(200),@CONTENT NVARCHAR(MAX)='',@IS_USE CHAR(1)='Y',@REG_USER NVARCHAR(50)='',@REG_IP NVARCHAR(40)='',@RTN_MSG NVARCHAR(200)='' OUTPUT,@RTN_IDX INT=0 OUTPUT AS BEGIN SET NOCOUNT ON; SET @RTN_MSG=''; SET @RTN_IDX=0; DECLARE @LL_IDX INT=0,@LR_IDX INT=0; SELECT @LR_IDX=ISNULL(LR_IDX,0),@LL_IDX=ISNULL(LL_IDX,0) FROM dpl_notify WHERE LN_IDX=@LN_IDX; IF @ACTION='ADD' BEGIN IF @TITLE='' BEGIN SET @RTN_MSG='안전요건명을 입력해주세요.'; RETURN; END INSERT INTO dpl_safety (LN_IDX,LL_IDX,LR_IDX,LS_TITLE,LS_CONTENT,LS_IS_USE,LS_REG_USER,LS_REG_IP,LS_REG_DATE) VALUES (@LN_IDX,@LL_IDX,@LR_IDX,@TITLE,@CONTENT,@IS_USE,@REG_USER,@REG_IP,GETDATE()); SET @RTN_IDX=SCOPE_IDENTITY(); END ELSE IF @ACTION='MOD' BEGIN UPDATE dpl_safety SET LN_IDX=@LN_IDX,LL_IDX=@LL_IDX,LR_IDX=@LR_IDX,LS_TITLE=@TITLE,LS_CONTENT=@CONTENT,LS_IS_USE=@IS_USE,LS_UPD_USER=@REG_USER,LS_UPD_IP=@REG_IP,LS_UPD_DATE=GETDATE() WHERE LS_IDX=@IDX; SET @RTN_IDX=@IDX; END ELSE IF @ACTION='DEL' BEGIN DELETE FROM dpl_safety WHERE LS_IDX=@IDX; SET @RTN_IDX=@IDX; END END");

        // Items SP
        dropAndCreate(conn, "uspLawItems_GetListPaging",
            "CREATE PROCEDURE uspLawItems_GetListPaging @Page INT=1,@ListSize INT=20,@qKey NVARCHAR(50)='',@qWord NVARCHAR(200)='',@qLL INT=0,@qLR INT=0,@qLN INT=0,@qSort NVARCHAR(50)='' AS BEGIN SET NOCOUNT ON; DECLARE @Offset INT=(@Page-1)*@ListSize; SELECT i.LI_IDX,i.LI_LEGAL_NAME,ISNULL(n.LN_TITLE,'') AS LN_TITLE,ISNULL(n.LN_TITLE,'') AS LN_NOTIFY,ISNULL(r.LR_TITLE,'') AS LR_TITLE,ISNULL(l.LL_TITLE,'') AS LL_TITLE,i.LI_IS_USE,CONVERT(NVARCHAR(10),i.LI_REG_DATE,120) AS LI_REG_DATE,i.LL_IDX,i.LR_IDX,i.LN_IDX FROM dpl_items i LEFT JOIN dpl_regulation_legal l ON l.LL_IDX=i.LL_IDX LEFT JOIN dpl_regulation r ON r.LR_IDX=i.LR_IDX LEFT JOIN dpl_notify n ON n.LN_IDX=i.LN_IDX WHERE (@qLL=0 OR i.LL_IDX=@qLL) AND (@qLR=0 OR i.LR_IDX=@qLR) AND (@qLN=0 OR i.LN_IDX=@qLN) AND (@qWord='' OR i.LI_LEGAL_NAME LIKE '%'+@qWord+'%') ORDER BY i.LI_IDX DESC OFFSET @Offset ROWS FETCH NEXT @ListSize ROWS ONLY; SELECT COUNT(1) AS TOTAL FROM dpl_items i WHERE (@qLL=0 OR i.LL_IDX=@qLL) AND (@qLR=0 OR i.LR_IDX=@qLR) AND (@qLN=0 OR i.LN_IDX=@qLN) AND (@qWord='' OR i.LI_LEGAL_NAME LIKE '%'+@qWord+'%'); END");
        dropAndCreate(conn, "uspLawItems_GetList",
            "CREATE PROCEDURE uspLawItems_GetList @qIsUse CHAR(1)='Y',@qParent INT=0,@qIdx INT=0 AS BEGIN SET NOCOUNT ON; SELECT LI_IDX,LI_LEGAL_NAME FROM dpl_items WHERE (@qIsUse='' OR LI_IS_USE=@qIsUse) AND (@qParent=0 OR LN_IDX=@qParent) AND (@qIdx=0 OR LI_IDX=@qIdx) ORDER BY LI_LEGAL_NAME; END");
        dropAndCreate(conn, "uspLawItems_GetInfo",
            "CREATE PROCEDURE uspLawItems_GetInfo @IDX INT AS BEGIN SET NOCOUNT ON; SELECT i.LI_IDX,i.LI_LEGAL_NAME,i.LN_IDX,i.LL_IDX,i.LR_IDX,i.LI_SCOPE,i.LI_EXCEPTION,i.LI_IS_USE,CONVERT(NVARCHAR(19),i.LI_REG_DATE,120) AS LI_REG_DATE,i.LI_REG_USER,ISNULL(l.LL_TITLE,'') AS LL_TITLE,ISNULL(r.LR_TITLE,'') AS LR_TITLE,ISNULL(n.LN_TITLE,'') AS LN_TITLE FROM dpl_items i LEFT JOIN dpl_regulation_legal l ON l.LL_IDX=i.LL_IDX LEFT JOIN dpl_regulation r ON r.LR_IDX=i.LR_IDX LEFT JOIN dpl_notify n ON n.LN_IDX=i.LN_IDX WHERE i.LI_IDX=@IDX; END");
        dropAndCreate(conn, "uspLawItems_PutInfo",
            "CREATE PROCEDURE uspLawItems_PutInfo @ACTION NVARCHAR(10),@IDX INT=0,@LEGAL_NAME NVARCHAR(200),@LN_IDX INT=0,@SCOPE NVARCHAR(MAX)='',@EXCEPTION NVARCHAR(MAX)='',@LO_IDX INT=0,@LG_IDX INT=0,@IS_USE CHAR(1)='Y',@LS_IDXS NVARCHAR(MAX)='',@REG_USER NVARCHAR(50)='',@REG_IP NVARCHAR(40)='',@RTN_MSG NVARCHAR(200)='' OUTPUT,@RTN_IDX INT=0 OUTPUT AS BEGIN SET NOCOUNT ON; SET @RTN_MSG=''; SET @RTN_IDX=0; DECLARE @LL_IDX INT=0,@LR_IDX INT=0; SELECT @LR_IDX=ISNULL(LR_IDX,0),@LL_IDX=ISNULL(LL_IDX,0) FROM dpl_notify WHERE LN_IDX=@LN_IDX; IF @ACTION='ADD' BEGIN IF @LEGAL_NAME='' BEGIN SET @RTN_MSG='법정 품목명을 입력해주세요.'; RETURN; END INSERT INTO dpl_items (LN_IDX,LL_IDX,LR_IDX,LI_LEGAL_NAME,LI_SCOPE,LI_EXCEPTION,LO_IDX,LG_IDX,LI_IS_USE,LI_REG_USER,LI_REG_IP,LI_REG_DATE) VALUES (@LN_IDX,@LL_IDX,@LR_IDX,@LEGAL_NAME,@SCOPE,@EXCEPTION,@LO_IDX,@LG_IDX,@IS_USE,@REG_USER,@REG_IP,GETDATE()); SET @RTN_IDX=SCOPE_IDENTITY(); END ELSE IF @ACTION='MOD' BEGIN UPDATE dpl_items SET LN_IDX=@LN_IDX,LL_IDX=@LL_IDX,LR_IDX=@LR_IDX,LI_LEGAL_NAME=@LEGAL_NAME,LI_SCOPE=@SCOPE,LI_EXCEPTION=@EXCEPTION,LI_IS_USE=@IS_USE,LI_UPD_USER=@REG_USER,LI_UPD_DATE=GETDATE() WHERE LI_IDX=@IDX; SET @RTN_IDX=@IDX; END ELSE IF @ACTION='DEL' BEGIN DELETE FROM dpl_items WHERE LI_IDX=@IDX; SET @RTN_IDX=@IDX; END END");
        dropAndCreate(conn, "uspLawItemsDetail_GetListPaging",
            "CREATE PROCEDURE uspLawItemsDetail_GetListPaging @Page INT=1,@ListSize INT=20,@qSite NVARCHAR(20)='admin',@qKey NVARCHAR(50)='',@qWord NVARCHAR(200)='',@qSort NVARCHAR(50)='',@qCate INT=0,@qCate2 INT=0,@qCate3 INT=0,@qLL INT=0,@qLR INT=0,@qLN INT=0,@KeywordList NVARCHAR(MAX)='' AS BEGIN SET NOCOUNT ON; DECLARE @Offset INT=(@Page-1)*@ListSize; SELECT d.LD_IDX,d.LD_ITEM_NAME,d.LD_USE_AGE,d.LD_MATERIAL,ISNULL(l.LL_TITLE,'') AS LL_TITLE,ISNULL(r.LR_TITLE,'') AS LR_TITLE,ISNULL(n.LN_TITLE,'') AS LN_TITLE,ISNULL(n.LN_TITLE,'') AS LN_NOTIFY,d.LD_IS_USE,CONVERT(NVARCHAR(10),d.LD_REG_DATE,120) AS LD_REG_DATE,d.LL_IDX,d.LR_IDX,d.LN_IDX,d.LI_IDX FROM dpl_items_detail d LEFT JOIN dpl_regulation_legal l ON l.LL_IDX=d.LL_IDX LEFT JOIN dpl_regulation r ON r.LR_IDX=d.LR_IDX LEFT JOIN dpl_notify n ON n.LN_IDX=d.LN_IDX WHERE (@qLL=0 OR d.LL_IDX=@qLL) AND (@qLR=0 OR d.LR_IDX=@qLR) AND (@qLN=0 OR d.LN_IDX=@qLN) AND (@qWord='' OR d.LD_ITEM_NAME LIKE '%'+@qWord+'%') ORDER BY d.LD_IDX DESC OFFSET @Offset ROWS FETCH NEXT @ListSize ROWS ONLY; SELECT COUNT(1) AS TOTAL FROM dpl_items_detail d WHERE (@qLL=0 OR d.LL_IDX=@qLL) AND (@qLR=0 OR d.LR_IDX=@qLR) AND (@qLN=0 OR d.LN_IDX=@qLN) AND (@qWord='' OR d.LD_ITEM_NAME LIKE '%'+@qWord+'%'); END");
        dropAndCreate(conn, "uspLawItemsDetail_GetList",
            "CREATE PROCEDURE uspLawItemsDetail_GetList @qIsUse CHAR(1)='Y',@qIdx INT=0 AS BEGIN SET NOCOUNT ON; SELECT LD_IDX,LD_ITEM_NAME FROM dpl_items_detail WHERE (@qIsUse='' OR LD_IS_USE=@qIsUse) AND (@qIdx=0 OR LD_IDX=@qIdx) ORDER BY LD_ITEM_NAME; END");
        dropAndCreate(conn, "uspLawItemsDetail_GetInfo",
            "CREATE PROCEDURE uspLawItemsDetail_GetInfo @qSite NVARCHAR(20)='admin',@IDX INT AS BEGIN SET NOCOUNT ON; SELECT d.LD_IDX,d.LD_ITEM_NAME,d.LI_IDX,d.LN_IDX,d.LL_IDX,d.LR_IDX,d.LD_USE_AGE,d.LD_MATERIAL,d.LD_IS_USE,CONVERT(NVARCHAR(19),d.LD_REG_DATE,120) AS LD_REG_DATE,d.LD_REG_USER,ISNULL(l.LL_TITLE,'') AS LL_TITLE,ISNULL(r.LR_TITLE,'') AS LR_TITLE,ISNULL(n.LN_TITLE,'') AS LN_TITLE,ISNULL(i.LI_LEGAL_NAME,'') AS LI_LEGAL_NAME FROM dpl_items_detail d LEFT JOIN dpl_regulation_legal l ON l.LL_IDX=d.LL_IDX LEFT JOIN dpl_regulation r ON r.LR_IDX=d.LR_IDX LEFT JOIN dpl_notify n ON n.LN_IDX=d.LN_IDX LEFT JOIN dpl_items i ON i.LI_IDX=d.LI_IDX WHERE d.LD_IDX=@IDX; END");
        dropAndCreate(conn, "uspLawItemsDetail_PutInfo",
            "CREATE PROCEDURE uspLawItemsDetail_PutInfo @ACTION NVARCHAR(10),@IDX INT=0,@ITEM_NAME NVARCHAR(200),@LI_IDX INT=0,@USE_AGE NVARCHAR(100)='',@MATERIAL NVARCHAR(100)='',@LC_IDX INT=0,@SAMPLE_ATTA_PATH NVARCHAR(500)='',@IS_USE CHAR(1)='Y',@REG_USER NVARCHAR(50)='',@REG_IP NVARCHAR(40)='',@LDK_KEYWORDS NVARCHAR(MAX)='',@LC_CATEGORIES NVARCHAR(MAX)='',@RTN_MSG NVARCHAR(200)='' OUTPUT,@RTN_IDX INT=0 OUTPUT AS BEGIN SET NOCOUNT ON; SET @RTN_MSG=''; SET @RTN_IDX=0; DECLARE @LN_IDX INT=0,@LL_IDX INT=0,@LR_IDX INT=0; SELECT @LN_IDX=ISNULL(LN_IDX,0),@LL_IDX=ISNULL(LL_IDX,0),@LR_IDX=ISNULL(LR_IDX,0) FROM dpl_items WHERE LI_IDX=@LI_IDX; IF @ACTION='ADD' BEGIN IF @ITEM_NAME='' BEGIN SET @RTN_MSG='일반 품목명을 입력해주세요.'; RETURN; END INSERT INTO dpl_items_detail (LI_IDX,LN_IDX,LL_IDX,LR_IDX,LD_ITEM_NAME,LD_USE_AGE,LD_MATERIAL,LD_IS_USE,LD_REG_USER,LD_REG_IP,LD_REG_DATE) VALUES (@LI_IDX,@LN_IDX,@LL_IDX,@LR_IDX,@ITEM_NAME,@USE_AGE,@MATERIAL,@IS_USE,@REG_USER,@REG_IP,GETDATE()); SET @RTN_IDX=SCOPE_IDENTITY(); END ELSE IF @ACTION='MOD' BEGIN UPDATE dpl_items_detail SET LI_IDX=@LI_IDX,LN_IDX=@LN_IDX,LL_IDX=@LL_IDX,LR_IDX=@LR_IDX,LD_ITEM_NAME=@ITEM_NAME,LD_USE_AGE=@USE_AGE,LD_MATERIAL=@MATERIAL,LD_IS_USE=@IS_USE,LD_UPD_USER=@REG_USER,LD_UPD_DATE=GETDATE() WHERE LD_IDX=@IDX; SET @RTN_IDX=@IDX; END ELSE IF @ACTION='DEL' BEGIN DELETE FROM dpl_items_detail WHERE LD_IDX=@IDX; SET @RTN_IDX=@IDX; END END");

        // AJAX용 카운트 SP (AjaxServlet이 직접 쿼리 사용하므로 불필요, 생략)
        System.out.println("[DPL] 전체 SP 생성 완료");
    }

    private void exec(Connection conn, String sql) throws Exception {
        try (Statement st = conn.createStatement()) { st.execute(sql.strip()); }
    }
    private void dropAndCreate(Connection conn, String spName, String createSql) throws Exception {
        exec(conn, "IF OBJECT_ID('" + spName + "','P') IS NOT NULL DROP PROCEDURE " + spName);
        exec(conn, createSql);
    }
    @Override public void contextDestroyed(ServletContextEvent sce) {}
}
