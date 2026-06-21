<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<% String uri = request.getRequestURI(); %>
<!DOCTYPE html><html lang="ko"><head>
<meta charset="UTF-8"><meta http-equiv="X-UA-Compatible" content="IE=edge">
<title>고시(부속서) — DPL 법규정보 관리시스템</title>
<link rel="stylesheet" href="/static/css/admin_common.css">
<link rel="stylesheet" href="/static/css/jquery-ui.min.css">
<script src="/static/js/jquery-1.11.1.min.js"></script>
<script src="/static/js/jquery-ui.min.js"></script>
<script src="/static/js/utils.js"></script>
<script src="/static/js/admin_common.js"></script>
<script>
function jfCreate(idx){location.href="?mode=form&ln_idx="+idx+"&qLL=${qLL}&qLR=${qLR}&qKey=${fn:escapeXml(qKey)}&qWord=${fn:escapeXml(qWord)}&page=${page}";}
function jfSearch(){$("#frmSearch").attr("action","?mode=list");return true;}
function jfSearchReset(){$("#frmSearch input[type=text],#frmSearch select").val("");$("#frmSearch").attr("action","?mode=list").submit();}
$(function(){
  $("th[data-act='ls-sort']").click(function(){var s=$(this).data("sort");var c=$("#qSort").val();var d=(c===("A|"+s))?"D":"A";$("#qSort").val(d+"|"+s);$("#frmSearch").attr("action","?mode=list").submit();});
  $("#qLL").change(function(){
    var idx=$(this).val(); $("#qLR option:gt(0)").remove();
    if(idx&&idx!="0") fetch("/ajax/?type=regulation&qLL="+idx).then(r=>r.json()).then(d=>{(d.rows||[]).forEach(row=>{$("#qLR").append('<option value="'+row.lr_idx+'">'+row.lr_title+'</option>');});});
  });
});
</script>
</head><body>
<div id="header">
  <div class="logo"><a href="/"><img src="/static/img_admin/common/logo.png" alt="롯데중앙연구소 안전센터"></a></div>
  <div class="side_menu"><div class="side_link my_info">
    <a href="#none"><strong>관리자</strong>DPL</a>
    <div class="my_info_view"><p><strong>관리자</strong><span>DPL 관리자</span></p>
      <div class="btn_c"><a href="#">개인정보 수정</a><a href="#">로그아웃</a></div></div>
  </div></div>
  <div class="gnb">
    <strong class="menu_1depth_01 menu_1depth on"><a href="/legal/?mode=list">법규정보DB 관리</a></strong>
    <div class="menu_2depth_01 menu_2depth"><ul>
      <li><p class="<%= uri.contains("/legal/") ? "on":"" %>"><a href="/legal/?mode=list">규제법률</a></p></li>
      <li><p class="<%= uri.contains("/regulation/") ? "on":"" %>"><a href="/regulation/?mode=list">규제사항</a></p></li>
      <li><p class="<%= uri.contains("/notify/") ? "on":"" %>"><a href="/notify/?mode=list">고시(부속서)</a></p></li>
      <li><p class="<%= uri.contains("/safety/") ? "on":"" %>"><a href="/safety/?mode=list">안전요건</a></p></li>
      <li><p><a href="#">표시사항</a></p></li>
      <li><p class="<%= uri.contains("items_def") ? "on":"" %>"><a href="/items_def/?mode=list">품목정보</a></p></li>
      <li><p class="<%= uri.contains("items_detail") ? "on":"" %>"><a href="/items_detail/?mode=list">세부품목정보</a></p></li>
      <li><p><a href="#">온라인표시사항</a></p></li>
      <li><p><a href="#">법규 제·개정 사항</a></p></li>
    </ul></div>
    <strong class="menu_1depth_02 menu_1depth"><a href="#">안전정보DB 관리</a></strong>
    <div class="menu_2depth_02 menu_2depth"><ul>
      <li><p><a href="#">제품안전 뉴스</a></p></li>
      <li><p><a href="#">위해정보DB</a></p></li>
      <li><p><a href="#">롯데스탠다드(품질기준서)</a></p></li>
    </ul></div>
    <strong class="menu_1depth_03 menu_1depth"><a href="#">셀프러닝 관리</a></strong>
    <div class="menu_2depth_03 menu_2depth"><ul>
      <li><p><a href="#">숏클래스</a></p></li>
      <li><p><a href="#">유용한 정보</a></p></li>
      <li><p><a href="#">동영상 정보</a></p></li>
      <li><p><a href="#">안전센터정보</a></p></li>
    </ul></div>
    <strong class="menu_1depth_04 menu_1depth"><a href="#">카테고리 관리</a></strong>
    <div class="menu_2depth_04 menu_2depth"><ul>
      <li><p><a href="#">중분류 관리</a></p></li>
      <li><p><a href="#">소분류 관리</a></p></li>
    </ul></div>
    <strong class="menu_1depth_05 menu_1depth"><a href="#">배너 관리</a></strong>
    <div class="menu_2depth_05 menu_2depth"><ul>
      <li><p><a href="#">메인상단띠배너</a></p></li>
    </ul></div>
  </div>
</div>
<div id="container">
  <div class="title title_navi">
    <div class="title_text">고시(부속서)</div>
    <p><span>Home</span><span>법규정보DB 관리</span><span>고시(부속서)</span></p>
  </div>
  <form name="frmSearch" id="frmSearch" method="get" action="?mode=list" onsubmit="return jfSearch()">
    <input type="hidden" name="mode" value="list">
    <input type="hidden" name="qSort" id="qSort" value="${fn:escapeXml(qSort)}">
    <div class="table_type_02"><table border="0" cellspacing="0" cellpadding="0">
      <colgroup><col width="118"><col width="*"></colgroup>
      <tbody>
        <tr>
          <th>규제법률</th><td>
            <select name="qLL" id="qLL">
              <option value="0">-- 선택 --</option>
              <c:forEach var="ll" items="${legalList}">
                <option value="${ll.ll_idx}" <c:if test="${ll.ll_idx==qLL}">selected</c:if>>${fn:escapeXml(ll.ll_title)}</option>
              </c:forEach>
            </select>
            <select name="qLR" id="qLR">
              <option value="0">-- 선택 --</option>
              <c:forEach var="lr" items="${regulationList}">
                <option value="${lr.lr_idx}" <c:if test="${lr.lr_idx==qLR}">selected</c:if>>${fn:escapeXml(lr.lr_title)}</option>
              </c:forEach>
            </select>
          </td>
        </tr>
        <tr>
          <th>검색어</th><td>
            <select name="qKey">
              <option value="" <c:if test="${qKey==''}">selected</c:if>>전체</option>
              <option value="TITLE" <c:if test="${qKey=='TITLE'}">selected</c:if>>정부고시</option>
            </select>
            <input type="text" name="qWord" class="inp" value="${fn:escapeXml(qWord)}">
            <input type="submit" class="btn_type_03" value="검색">
            <input type="button" class="btn_type_03" value="초기화" onclick="jfSearchReset()">
          </td>
        </tr>
      </tbody>
    </table></div>
  </form>
  <div class="info_text_type_01"><b><span id="div_cnt">${total}</span></b>건이 조회되었습니다.
    <div class="btn_r"><a class="btn_type_03" href="javascript:jfCreate(0);">등록</a></div></div>
  <div class="table_type_03"><table border="0" cellspacing="0" cellpadding="0">
    <colgroup><col style="width:80px"><col><col style="width:160px"><col style="width:160px"><col style="width:70px"><col style="width:110px"></colgroup>
    <thead><tr>
      <th>No.</th>
      <th data-act="ls-sort" data-sort="TITLE" style="cursor:pointer">정부고시(부속서)</th>
      <th data-act="ls-sort" data-sort="LEGAL" style="cursor:pointer">규제법규</th>
      <th data-act="ls-sort" data-sort="REGUL" style="cursor:pointer">규제제도</th>
      <th data-act="ls-sort" data-sort="DISP" style="cursor:pointer">노출</th>
      <th data-act="ls-sort" data-sort="RDATE" style="cursor:pointer">등록일</th>
    </tr></thead>
    <tbody>
      <c:choose>
        <c:when test="${empty list}"><tr><td colspan="6" style="text-align:center;padding:30px">검색된 내용이 없습니다.</td></tr></c:when>
        <c:otherwise>
          <c:forEach var="row" items="${list}" varStatus="st">
            <tr>
              <td style="text-align:center">${total-(page-1)*listSize-st.index}</td>
              <td class="_tit"><a href="javascript:jfCreate('${row.ln_idx}');">${fn:escapeXml(row.ln_title != null ? row.ln_title : '')}</a></td>
              <td style="text-align:center">${fn:escapeXml(row.ll_title != null ? row.ll_title : '')}</td>
              <td style="text-align:center">${fn:escapeXml(row.lr_title != null ? row.lr_title : '')}</td>
              <td style="text-align:center">${row.ln_is_use eq 'Y' ? '사용' : '미사용'}</td>
              <td style="text-align:center">${row.ln_reg_date}</td>
            </tr>
          </c:forEach>
        </c:otherwise>
      </c:choose>
    </tbody>
  </table></div>
  <div class="important_text"><p></p>
    <div class="btn_r"><a class="btn_type_03" href="javascript:jfCreate(0);">등록</a></div></div>
<div class="paginate_ui">
    <c:if test="${page>1}">
      <a class="pre" href="?mode=list&page=1&qLL=${qLL}&qLR=${qLR}&qKey=${fn:escapeXml(qKey)}&qWord=${fn:escapeXml(qWord)}&qSort=${fn:escapeXml(qSort)}"><img src="/static/img_admin/btn/btn_paginate_ui_prev_02.gif" alt="처음으로"></a>
      <a class="pre" href="?mode=list&page=${page-1}&qLL=${qLL}&qLR=${qLR}&qKey=${fn:escapeXml(qKey)}&qWord=${fn:escapeXml(qWord)}&qSort=${fn:escapeXml(qSort)}"><img src="/static/img_admin/btn/btn_paginate_ui_prev_01.gif" alt="이전"></a>
    </c:if>
    <c:forEach begin="1" end="${pageCnt}" var="p">
      <c:if test="${p>=page-4 && p<=page+4}">
        <c:choose>
          <c:when test="${p==page}"><strong>${p}</strong></c:when>
          <c:otherwise><a href="?mode=list&page=${p}&qLL=${qLL}&qLR=${qLR}&qKey=${fn:escapeXml(qKey)}&qWord=${fn:escapeXml(qWord)}&qSort=${fn:escapeXml(qSort)}">${p}</a></c:otherwise>
        </c:choose>
      </c:if>
    </c:forEach>
    <c:if test="${page<pageCnt}">
      <a class="next" href="?mode=list&page=${page+1}&qLL=${qLL}&qLR=${qLR}&qKey=${fn:escapeXml(qKey)}&qWord=${fn:escapeXml(qWord)}&qSort=${fn:escapeXml(qSort)}"><img src="/static/img_admin/btn/btn_paginate_ui_next_01.gif" alt="다음"></a>
      <a class="next" href="?mode=list&page=${pageCnt}&qLL=${qLL}&qLR=${qLR}&qKey=${fn:escapeXml(qKey)}&qWord=${fn:escapeXml(qWord)}&qSort=${fn:escapeXml(qSort)}"><img src="/static/img_admin/btn/btn_paginate_ui_next_02.gif" alt="마지막으로"></a>
    </c:if>
  </div>
</div>
<div id="footer">
  <div class="footer_logo"><a href="#"><img src="/static/img_admin/common/footer_logo.png" alt="롯데중앙연구소 안전센터"></a></div>
  <div class="copy">서울특별시 강서구 마곡중앙로 201 (마곡동) <span>|</span> TEL 02.6309.3233 <span>|</span> FAX 02.6309.3099<br>
    copyright © 2017 <strong>LOTTE R&amp;D CENTER SAFETY CENTER Co., Ltd.</strong> All rights reserved.</div>
</div></body></html>