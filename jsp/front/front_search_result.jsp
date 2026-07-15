<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html><html lang="ko"><head>
<meta charset="UTF-8"><meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>통합검색 결과 — 디지털 제품안전 라이브러리</title>
<link rel="stylesheet" href="/static/css/front_common.css">
<link rel="stylesheet" href="/static/css/swiper-bundle.min.css">
<script src="/static/js/jquery-1.8.3.min.js"></script>
<script src="/static/js/swiper-bundle.min.js"></script>
<script src="/static/js/front_common.js"></script>
<script type="text/javascript">
function jfSearch(){
  if ($('#qSearchWord').val() == '') { alert('검색어를 입력해주세요'); return false; }
  return true;
}
$(document).ready(function(){ $("#frmSearch").submit(jfSearch); });
</script>
</head><body>
<div id="wrap" class="sub search_list">
<div id="header"><header>
  <div class="logo"><a href="/front/"><img src="/static/img_front/images/common/logo.png" alt="디지털 제품안전 라이브러리"></a></div>
  <nav><ul><li><a class="gnb_1dep" href="/front/legal/">법규정보</a><div class="gnb_2dep"><ul><li><a href="/front/legal/">법규정보DB</a></li><li><a href="/front/legal/?tab=revise">법규 제·개정 정보</a></li></ul></div></li>
<li><a class="gnb_1dep" href="/front/safety/">안전정보</a><div class="gnb_2dep"><ul><li><a href="/front/safety/">위해정보DB</a></li><li><a href="/front/safety/?tab=news">제품안전 뉴스</a></li></ul></div></li>
<li><a class="gnb_1dep" href="/front/standard/">롯데 스탠다드</a><div class="gnb_2dep"><ul><li><a href="/front/standard/">품질관리기준</a></li></ul></div></li>
<li><a class="gnb_1dep" href="/front/support/">셀프러닝</a><div class="gnb_2dep"><ul><li><a href="/front/support/">숏클래스</a></li><li><a href="/front/support/?tab=info">유용한 정보</a></li><li><a href="/front/support/?tab=video">동영상 정보</a></li><li><a href="/front/support/?tab=safety">안전센터 정보</a></li></ul></div></li>
</ul></nav>
  <div class="right_link"><p class="btn_admin"><a href="/legal/?mode=list">관리자</a></p></div>
</header><div class="bg_nav"></div></div>

<div id="sub_visual">
  <div class="visual_title" data-target="gnb-text">통합 검색</div>
  <p>제품 안전정보를 통합 검색하세요</p>
</div>
<div id="container">
  <div id="aside" style="display:none"></div>
  <div id="contents" style="width:100%;max-width:1000px;margin:0 auto">
    <div id="title_content">
      <div id="title"><span>통합 검색결과</span></div>
      <p class="bread"><span>Home</span><span>통합검색</span><span>${fn:escapeXml(qWord)}</span></p>
    </div>

    <form name="frmSearch" id="frmSearch" method="get" action="/front/search/">
      <div class="search_table"><table border="0" cellspacing="0" cellpadding="0">
        <caption>조건검색</caption>
        <colgroup><col width="149"><col width="*"></colgroup>
        <tbody>
          <tr>
            <th>키워드</th>
            <td>
              <input type="text" name="qWord" id="qSearchWord" value="${fn:escapeXml(qWord)}" style="width:400px">
              <button type="submit" class="btn btn_style_01">검색</button>
              <button type="reset" class="btn btn_style_02">초기화</button>
            </td>
          </tr>
        </tbody>
      </table></div>
    </form>

    <!-- ============ 법규정보 ============ -->
    <div class="search_list_wrap legal">
      <div class="title-wrap">
        <h2>법규정보 (<span class="div_cnt">${cntLegal}</span>건)</h2>
        <c:if test="${cntLegal > 5}"><a href="/front/legal/?qWord=${fn:escapeXml(qWord)}">더보기</a></c:if>
      </div>
      <ul class="list legal_list">
        <c:choose>
          <c:when test="${empty legalResult}">
            <li><div style="text-align:center;">검색된 내용이 없습니다</div></li>
          </c:when>
          <c:otherwise>
            <c:forEach var="row" items="${legalResult}">
              <li>
                <a href="/front/legal/view?lr_idx=${row.lr_idx}" class="item">
                  <div class="data-wrap">
                    <div class="category"><span class="legal">규제법률</span>${fn:escapeXml(row.ll_title)}</div>
                    <div class="subject"><c:if test="${not empty row.li_legal_name}">${fn:escapeXml(row.li_legal_name)} <i class="bar"></i> </c:if><strong>${fn:escapeXml(row.ld_item_name)}</strong></div>
                    <ul class="info_list">
                      <li><em>고시(부속서)</em><i class="bar"></i>${fn:escapeXml(row.ln_title)}</li>
                      <li><em>관리제도</em><i class="bar"></i>${fn:escapeXml(row.lr_title)}</li>
                      <li><em>게시일</em><i class="bar"></i>${row.ld_reg_date}</li>
                    </ul>
                  </div>
                </a>
              </li>
            </c:forEach>
          </c:otherwise>
        </c:choose>
      </ul>
      <div class="bottom"><c:if test="${cntLegal > 5}"><a href="/front/legal/?qWord=${fn:escapeXml(qWord)}">더보기</a></c:if></div>
    </div>

    <!-- ============ 위해정보 ============ -->
    <div class="search_list_wrap riskdb">
      <div class="title-wrap">
        <h2>위해정보 (<span class="div_cnt">${cntRiskdb}</span>건)</h2>
        <c:if test="${cntRiskdb > 5}"><a href="/front/safety/?qWord=${fn:escapeXml(qWord)}">더보기</a></c:if>
      </div>
      <ul class="list riskdb_list">
        <c:choose>
          <c:when test="${empty riskdbResult}">
            <li><div style="text-align:center;">검색된 내용이 없습니다</div></li>
          </c:when>
          <c:otherwise>
            <c:forEach var="row" items="${riskdbResult}">
              <li>
                <a href="/front/safety/view?rd_idx=${row.rd_idx}" class="item">
                  <div class="data-wrap">
                    <div class="category">
                      <span class="risk-type">${fn:escapeXml(row.rd_type)}</span>
                      <span class="small">${fn:escapeXml(row.rd_factor)}</span>
                    </div>
                    <div class="subject">${fn:escapeXml(row.rd_title)}</div>
                    <ul class="info_list">
                      <li><em>이슈일자</em><i class="bar"></i>${row.rd_reg_date}</li>
                      <li><em>정보출처</em><i class="bar"></i>${fn:escapeXml(row.rd_source)}</li>
                      <li><em>링크</em><i class="bar"></i><span class="url">${fn:escapeXml(row.rd_link)}</span></li>
                    </ul>
                  </div>
                </a>
              </li>
            </c:forEach>
          </c:otherwise>
        </c:choose>
      </ul>
      <div class="bottom"><c:if test="${cntRiskdb > 5}"><a href="/front/safety/?qWord=${fn:escapeXml(qWord)}">더보기</a></c:if></div>
    </div>

    <!-- ============ 롯데 스탠다드 ============ -->
    <div class="search_list_wrap standard">
      <div class="title-wrap">
        <h2>롯데 스탠다드 (<span class="div_cnt">${cntStandard}</span>건)</h2>
        <c:if test="${cntStandard > 5}"><a href="/front/standard/?qWord=${fn:escapeXml(qWord)}">더보기</a></c:if>
      </div>
      <ul class="list standard_list">
        <c:choose>
          <c:when test="${empty standardResult}">
            <li><div style="text-align:center;">검색된 내용이 없습니다</div></li>
          </c:when>
          <c:otherwise>
            <c:forEach var="row" items="${standardResult}">
              <li>
                <a href="/front/standard/view?st_idx=${row.st_idx}" class="item">
                  <div class="data-wrap">
                    <div class="category">
                      <span class="standard-type">${fn:escapeXml(row.st_div)}</span>
                      <strong class="standard-name">${fn:escapeXml(row.st_title)}</strong>
                    </div>
                    <ul class="info_list">
                      <li><em>기준서번호</em><i class="bar"></i>${fn:escapeXml(row.st_code)}</li>
                      <li><em>최신개정일</em><i class="bar"></i>${fn:escapeXml(row.st_ver_date)}</li>
                      <li><em>적용품목군</em><i class="bar"></i>${fn:escapeXml(row.st_items)}</li>
                    </ul>
                  </div>
                  <i class="icon-view">PDF보기</i>
                </a>
              </li>
            </c:forEach>
          </c:otherwise>
        </c:choose>
      </ul>
      <div class="bottom"><c:if test="${cntStandard > 5}"><a href="/front/standard/?qWord=${fn:escapeXml(qWord)}">더보기</a></c:if></div>
    </div>

    <!-- ============ 숏클래스 ============ -->
    <div class="search_list_wrap shortclass">
      <div class="title-wrap">
        <h2>숏클래스 (<span class="div_cnt">${cntShortclass}</span>건)</h2>
        <c:if test="${cntShortclass > 5}"><a href="/front/support/?qWord=${fn:escapeXml(qWord)}">더보기</a></c:if>
      </div>
      <ul class="list shortclass_list">
        <c:choose>
          <c:when test="${empty shortclassResult}">
            <li><div style="text-align:center;">검색된 내용이 없습니다</div></li>
          </c:when>
          <c:otherwise>
            <c:forEach var="row" items="${shortclassResult}">
              <li>
                <a href="/front/support/view?sc_idx=${row.sc_idx}" class="item">
                  <c:if test="${not empty row.sc_thumb_url}"><div class="img-wrap"><img src="${fn:escapeXml(row.sc_thumb_url)}" alt="${fn:escapeXml(row.sc_title)}" onerror="this.closest('.img-wrap').style.display='none'"></div></c:if>
                  <div class="data-wrap">
                    <div class="category"><span class="legal">규제법률</span>${fn:escapeXml(row.ll_title)}</div>
                    <div class="subject"><strong class="shortclass-title">${fn:escapeXml(row.sc_title)}</strong></div>
                    <div class="shortclass-contents">${fn:escapeXml(row.sc_desc)}</div>
                    <ul class="info_list">
                      <li><em>교육분야</em><i class="bar"></i>${fn:escapeXml(row.sc_type)}</li>
                      <li><em>게시일</em><i class="bar"></i>${row.sc_reg_date}</li>
                    </ul>
                  </div>
                </a>
              </li>
            </c:forEach>
          </c:otherwise>
        </c:choose>
      </ul>
      <div class="bottom"><c:if test="${cntShortclass > 5}"><a href="/front/support/?qWord=${fn:escapeXml(qWord)}">더보기</a></c:if></div>
    </div>

  </div>
</div>
</div></div>
<div id="footer"><footer>
  <div class="footer_logo"><img src="/static/img_front/images/common/footer_logo.png" alt="롯데중앙연구소 안전센터"></div>
  <address>서울특별시 강서구 마곡중앙로 201 (마곡동) <i></i> TEL 02.6309.3244(식품) 02.6309.3581(비식품) <i></i> FAX 02.6309.3099</address>
  <p class="copy">copyright © 2022 LOTTE R&amp;D CENTER SAFETY CENTER Co., Ltd. All rights reserved.</p>
</footer></div>
</body></html>
