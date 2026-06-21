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
function jfSubmit(){
  if(!$("#ln_notify").val().trim()){alert("정부고시명을 입력해주세요.");$("#ln_notify").focus();return;}
  if($("#lr_idx").val()=="0"){alert("규제사항을 선택해주세요.");$("#lr_idx").focus();return;}
  if(!$("input[name='ln_is_use']:checked").val()){alert("사용여부를 선택해주세요.");return;}
  $("#frmInfo").submit();
}
function jfList(){location.href="?mode=list&page=${page}&qLL=${qLL}&qLR=${qLR}&qKey=${fn:escapeXml(qKey)}&qWord=${fn:escapeXml(qWord)}";}
function jfDelete(){if(confirm("삭제하시겠습니까?")){$("#action").val("DEL");$("#frmInfo").submit();}}
$(function(){
  $("#ll_idx").change(function(){
    var idx=$(this).val(); $("#lr_idx option:gt(0)").remove();
    if(idx&&idx!="0") fetch("/ajax/?type=regulation&qLL="+idx).then(r=>r.json()).then(d=>{
      (d.rows||[]).forEach(row=>{$("#lr_idx").append('<option value="'+row.lr_idx+'">'+row.lr_title+'</option>');});
    });
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
      <li><p><a href="#">제품안전 뉴스</a></p></li><li><p><a href="#">위해정보DB</a></p></li>
      <li><p><a href="#">롯데스탠다드(품질기준서)</a></p></li>
    </ul></div>
    <strong class="menu_1depth_03 menu_1depth"><a href="#">셀프러닝 관리</a></strong>
    <div class="menu_2depth_03 menu_2depth"><ul>
      <li><p><a href="#">숏클래스</a></p></li><li><p><a href="#">유용한 정보</a></p></li>
      <li><p><a href="#">동영상 정보</a></p></li><li><p><a href="#">안전센터정보</a></p></li>
    </ul></div>
    <strong class="menu_1depth_04 menu_1depth"><a href="#">카테고리 관리</a></strong>
    <div class="menu_2depth_04 menu_2depth"><ul>
      <li><p><a href="#">중분류 관리</a></p></li><li><p><a href="#">소분류 관리</a></p></li>
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
  <form id="frmInfo" name="frmInfo" method="post" action="?mode=proc">
    <input type="hidden" name="mode" value="proc">
    <input type="hidden" name="action" id="action" value="${action}">
    <input type="hidden" name="ln_idx" value="${lnIdx}">
    <input type="hidden" name="page" value="${page}">
    <input type="hidden" name="qLL" value="${qLL}">
    <input type="hidden" name="qLR" value="${qLR}">
    <input type="hidden" name="qKey" value="${fn:escapeXml(qKey)}">
    <input type="hidden" name="qWord" value="${fn:escapeXml(qWord)}">

    <div class="table_type_01">
      <table border="0" cellspacing="0" cellpadding="0">
        <colgroup><col width="15%"><col width="*"></colgroup>
        <tbody>
          <tr>
            <th><i class="import_i">*</i>정부고시(부속서)</th>
            <td><input type="text" name="ln_notify" id="ln_notify" class="inp _w_full"
                value="${fn:escapeXml(info.ln_title != null ? info.ln_title : '')}" maxlength="200"></td>
          </tr>
          <tr>
            <th><i class="import_i">*</i>규제법률/사항</th>
            <td>
              <select name="ll_idx" id="ll_idx">
                <option value="0">-- 선택 --</option>
                <c:forEach var="ll" items="${legalList}">
                  <option value="${ll.ll_idx}" <c:if test="${ll.ll_idx==info.ll_idx}">selected</c:if>>${fn:escapeXml(ll.ll_title)}</option>
                </c:forEach>
              </select>
              <select name="lr_idx" id="lr_idx">
                <option value="0">-- 선택 --</option>
                <c:forEach var="lr" items="${regulationList}">
                  <option value="${lr.lr_idx}" <c:if test="${lr.lr_idx==info.lr_idx}">selected</c:if>>${fn:escapeXml(lr.lr_title)}</option>
                </c:forEach>
              </select>
            </td>
          </tr>
          <tr>
            <th>제·개정이력</th>
            <td><textarea name="ln_history" class="inp _w_full" rows="4" style="resize:vertical">${fn:escapeXml(info.ln_history != null ? info.ln_history : '')}</textarea></td>
          </tr>
          <tr>
            <th><i class="import_i">*</i>사용여부</th>
            <td>
              <input type="radio" name="ln_is_use" id="ln_is_use_y" value="Y"
                <c:if test="${empty info.ln_is_use || info.ln_is_use eq 'Y'}">checked</c:if>>
              <label for="ln_is_use_y" class="radio_label">사용</label>
              <input type="radio" name="ln_is_use" id="ln_is_use_n" value="N"
                <c:if test="${info.ln_is_use eq 'N'}">checked</c:if>>
              <label for="ln_is_use_n" class="radio_label">미사용</label>
            </td>
          </tr>
          <c:if test="${action eq 'MOD'}">
            <tr><th>등록일</th><td>${info.ln_reg_date} (${fn:escapeXml(info.ln_reg_user != null ? info.ln_reg_user : '')})</td></tr>
          </c:if>
        </tbody>
      </table>
    </div>
    <div class="btn_content"><div class="btn_r">
      <a href="javascript:jfSubmit();" class="btn_type_01">저장</a>
      <c:if test="${action eq 'MOD'}"><a href="javascript:jfDelete();" class="btn_type_03">삭제</a></c:if>
      <a href="javascript:jfList();" class="btn_type_03">목록</a>
    </div></div>
  </form>

</div>
<div id="footer">
  <div class="footer_logo"><a href="#"><img src="/static/img_admin/common/footer_logo.png" alt="롯데중앙연구소 안전센터"></a></div>
  <div class="copy">서울특별시 강서구 마곡중앙로 201 (마곡동) <span>|</span> TEL 02.6309.3233(식품) 02.6309.3581(비식품) <span>|</span> FAX 02.6309.3099<br>
    copyright © 2017 <strong>LOTTE R&amp;D CENTER SAFETY CENTER Co., Ltd.</strong> All rights reserved.</div>
</div></body></html>