package db;

import java.sql.*;

/**
 * 프론트엔드 전용 테이블 (위해정보/스탠다드/셀프러닝) 초기화
 */
public class FrontSetupExtension {

    public static void init(Connection conn) throws Exception {
        createTables(conn);
        seedData(conn);
    }

    private static void createTables(Connection conn) throws Exception {
        exec(conn, """
            IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME='dpl_riskdb')
            CREATE TABLE dpl_riskdb (
                RD_IDX INT IDENTITY(1,1) PRIMARY KEY,
                RD_TITLE NVARCHAR(300) NOT NULL,
                RD_TYPE NVARCHAR(100),
                RD_FACTOR NVARCHAR(200),
                RD_LEVEL INT DEFAULT 1,
                RD_SOURCE NVARCHAR(200),
                RD_LINK NVARCHAR(500),
                RD_CONTENT NVARCHAR(MAX),
                RD_CATE INT DEFAULT 0,
                RD_IS_USE CHAR(1) DEFAULT 'Y',
                RD_REG_DATE DATETIME DEFAULT GETDATE()
            )""");
        exec(conn, """
            IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME='dpl_standard')
            CREATE TABLE dpl_standard (
                ST_IDX INT IDENTITY(1,1) PRIMARY KEY,
                ST_DIV NVARCHAR(50),
                ST_CODE NVARCHAR(100),
                ST_TITLE NVARCHAR(300) NOT NULL,
                ST_ITEMS NVARCHAR(500),
                ST_VER_DATE NVARCHAR(20),
                ST_CONTENT NVARCHAR(MAX),
                ST_CATE INT DEFAULT 0,
                ST_IS_USE CHAR(1) DEFAULT 'Y',
                ST_REG_DATE DATETIME DEFAULT GETDATE()
            )""");
        exec(conn, """
            IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME='dpl_shortclass')
            CREATE TABLE dpl_shortclass (
                SC_IDX INT IDENTITY(1,1) PRIMARY KEY,
                SC_TITLE NVARCHAR(300) NOT NULL,
                SC_DESC NVARCHAR(MAX),
                SC_TYPE NVARCHAR(50) DEFAULT '법규',
                SC_CATE INT DEFAULT 0,
                SC_LL_IDX INT DEFAULT 0,
                SC_THUMB_URL NVARCHAR(500),
                SC_VIDEO_URL NVARCHAR(500),
                SC_IS_USE CHAR(1) DEFAULT 'Y',
                SC_REG_DATE DATETIME DEFAULT GETDATE()
            )""");
        System.out.println("[DPL] 프론트 테이블 생성 완료");
    }

    private static void seedData(Connection conn) throws Exception {
        // 빈 경우만 삽입
        try (Statement st = conn.createStatement()) {
            ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM dpl_riskdb"); rs.next();
            if (rs.getInt(1) > 0) { System.out.println("[DPL] 프론트 데이터 이미 존재"); return; }
        }

        // ── 위해정보 (10건) ──
        exec(conn, "INSERT INTO dpl_riskdb (RD_TITLE,RD_TYPE,RD_FACTOR,RD_LEVEL,RD_SOURCE,RD_CONTENT,RD_CATE,RD_IS_USE,RD_REG_DATE) VALUES " +
            "(N'[어린이용 완구] 납 함유량 초과 저가 장난감',N'화학적결함',N'납(Pb) 기준 초과',3,N'소비자원',N'<p>납 함유량이 기준치(90mg/kg)를 초과한 어린이용 완구가 적발되었습니다. 장기 노출 시 신경계 손상 우려가 있으며 즉각 판매중단 및 회수 조치되었습니다.</p>',5,'Y',GETDATE())," +
            "(N'[전기용품] USB 충전기 과열로 인한 화재 위험',N'전기적결함',N'과열/화재',3,N'메디컬투데이',N'<p>특정 USB 충전기 제품에서 과열 현상이 발생하여 화재 위험이 확인되었습니다. 해당 제품 사용 즉시 중단 권고.</p>',9,'Y',GETDATE())," +
            "(N'[화장품] 기준 초과 방부제 검출',N'화학적결함',N'파라벤 기준 초과',2,N'식품의약품안전처',N'<p>일부 수입 화장품에서 방부제(파라벤) 성분이 기준치를 초과하여 검출되었습니다.</p>',6,'Y',GETDATE())," +
            "(N'[어린이 가구] 강화유리 파손 부상 위험',N'물리적결함',N'파손/절상',2,N'소비자24',N'<p>어린이용 책상 강화유리 상판이 충격 없이 자연 파손되어 부상 사례가 보고되었습니다.</p>',5,'Y',GETDATE())," +
            "(N'[주방용품] 테플론 코팅 냄비 유해물질 논란',N'화학적결함',N'PFOA 검출',1,N'환경부',N'<p>고온 조리 시 테플론(불소수지) 코팅에서 유해물질이 방출될 수 있다는 연구결과가 발표되었습니다.</p>',3,'Y',GETDATE())," +
            "(N'[섬유제품] 형광증백제 함유 어린이 의류 적발',N'화학적결함',N'형광증백제 초과',2,N'국가기술표준원',N'<p>어린이 내의류에서 형광증백제가 기준치를 초과하여 검출되었으며 피부 자극 우려가 있습니다.</p>',1,'Y',GETDATE())," +
            "(N'[생활화학제품] 방향제 VOC 기준 초과',N'화학적결함',N'휘발성유기화합물',2,N'화학물질안전원',N'<p>차량용 방향제 제품에서 VOC(휘발성유기화합물) 함량이 기준을 초과하였습니다.</p>',8,'Y',GETDATE())," +
            "(N'[완구] 소형 부품 질식 위험 미개선 제품',N'물리적결함',N'질식/삼킴 위험',3,N'소비자원',N'<p>36개월 미만 영아용으로 판매된 완구에서 소형 부품이 탈락되어 질식 사고가 발생할 수 있습니다.</p>',5,'Y',GETDATE())," +
            "(N'[전기매트] 전자파 기준 초과 제품',N'전기적결함',N'전자파 초과',1,N'전자파연구원',N'<p>일부 전기매트에서 전자파 방출량이 기준을 초과하였으며 장기 사용 시 인체 영향 가능성이 있습니다.</p>',8,'Y',GETDATE())," +
            "(N'[유아용품] 유모차 브레이크 결함 리콜',N'물리적결함',N'제동장치 결함',3,N'국토교통부',N'<p>특정 브랜드 유모차 브레이크 장치 결함으로 인해 경사로에서 자동 이탈되는 사고가 보고되었습니다.</p>',5,'Y',GETDATE())");

        // ── 롯데스탠다드 (8건) ──
        exec(conn, "INSERT INTO dpl_standard (ST_DIV,ST_CODE,ST_TITLE,ST_ITEMS,ST_VER_DATE,ST_CONTENT,ST_IS_USE,ST_REG_DATE) VALUES " +
            "(N'품질기준',N'LS-2024-001',N'어린이 완구류 안전 품질기준',N'완구, 교구, 인형류',N'2024.03.01',N'<p>만 14세 미만 어린이 대상 완구류에 적용되는 롯데 내부 품질 안전기준입니다. 화학물질, 물리적 특성, 전기안전 등을 포함합니다.</p>','Y',GETDATE())," +
            "(N'공통',N'ADV-2023-012',N'제품 표시·광고 내부 심의기준',N'전 상품 공통',N'2023.09.01',N'<p>롯데그룹 유통 전 제품에 적용되는 표시·광고 심의 기준으로, 공정거래법 및 표시·광고법 준수를 위한 내부 가이드라인입니다.</p>','Y',GETDATE())," +
            "(N'품질기준',N'LS-2024-003',N'화장품류 성분 품질기준',N'기초화장품, 색조화장품, 기능성화장품',N'2024.01.15',N'<p>화장품법에 따른 기준 외 롯데 내부적으로 강화된 성분 관리 기준을 규정합니다. 방부제, 착향제, 색소 등의 허용 기준을 포함합니다.</p>','Y',GETDATE())," +
            "(N'품질기준',N'LS-2023-008',N'식품용 기구·용기 포장 기준',N'주방용품, 식품포장재',N'2023.06.30',N'<p>식품위생법 기준에 준하여 식품 접촉 기구 및 포장재의 용출 시험 기준을 정의합니다.</p>','Y',GETDATE())," +
            "(N'공통',N'ENV-2024-002',N'친환경 포장재 적용 기준',N'전 상품 포장재',N'2024.02.01',N'<p>탄소중립 목표 달성을 위한 포장재 경량화, 재활용 가능 소재 비율 기준을 규정합니다.</p>','Y',GETDATE())," +
            "(N'품질기준',N'LS-2024-005',N'섬유제품 유해물질 기준',N'의류, 침구류, 인테리어 섬유',N'2024.04.01',N'<p>KC 인증 기준 대비 강화된 유해물질 기준으로 포름알데히드, 아조염료, 중금속 등을 규제합니다.</p>','Y',GETDATE())," +
            "(N'안전기준',N'SF-2023-007',N'전기·전자제품 EMC 기준',N'가전제품, 전자기기',N'2023.12.01',N'<p>전기용품 안전관리법 기준에 더하여 전자파 적합성(EMC) 내부 기준을 강화하여 운영합니다.</p>','Y',GETDATE())," +
            "(N'안전기준',N'SF-2024-001',N'배터리 내장 제품 안전기준',N'충전기, 보조배터리, 스마트기기',N'2024.05.01',N'<p>리튬이온 배터리 내장 제품의 과충전 보호, 단락 방지, 발화 방지 기준을 규정합니다.</p>','Y',GETDATE())");

        // ── 숏클래스 (8건) ──
        exec(conn, "INSERT INTO dpl_shortclass (SC_TITLE,SC_DESC,SC_TYPE,SC_LL_IDX,SC_IS_USE,SC_REG_DATE) VALUES " +
            "(N'KC 인증 완전정복 — 전기용품 편',N'전기용품 안전관리법에 따른 KC 인증 취득 절차와 핵심 요건을 알기 쉽게 정리했습니다.',N'법규',1,'Y',GETDATE())," +
            "(N'어린이제품 공급자적합성확인 SDoC 가이드',N'SDoC 제도의 개념부터 실무 적용까지, 바이어와 MD가 꼭 알아야 할 핵심 내용을 담았습니다.',N'법규',2,'Y',GETDATE())," +
            "(N'화장품법 표시·기재 기준 완벽 정리',N'화장품 전성분 표시부터 기능성 화장품 광고 규정까지 실무에서 바로 활용할 수 있는 내용입니다.',N'법규',6,'Y',GETDATE())," +
            "(N'어린이제품 유해물질 — 프탈레이트계 가소제 심층 분석',N'어린이제품에서 자주 검출되는 프탈레이트계 가소제의 규제 기준과 시험 방법을 상세히 설명합니다.',N'법규',2,'Y',GETDATE())," +
            "(N'롯데 표준 품질기준 이해하기',N'롯데 내부 품질기준(Lotte Standard)의 체계와 적용 방법을 설명합니다.',N'공통',0,'Y',GETDATE())," +
            "(N'원산지 표시 완전 가이드 — 수입 제품 편',N'대외무역법에 따른 원산지 표시 의무와 위반 사례, 올바른 표시 방법을 안내합니다.',N'법규',8,'Y',GETDATE())," +
            "(N'전자상거래 표시·광고 규정 실무',N'온라인 플랫폼에서의 제품 표시·광고 규정 준수 방법과 주요 위반 사례를 소개합니다.',N'법규',10,'Y',GETDATE())," +
            "(N'위해제품 리콜 대응 매뉴얼',N'제품 위해 발생 시 대응 절차, 소비자 통보, 리콜 신고 방법을 단계별로 안내합니다.',N'공통',0,'Y',GETDATE())");

        System.out.println("[DPL] 프론트 데이터 삽입 완료 (위해정보 10건, 스탠다드 8건, 숏클래스 8건)");
    }

    private static void exec(Connection conn, String sql) throws Exception {
        try (Statement st = conn.createStatement()) { st.execute(sql.strip()); }
    }
}
