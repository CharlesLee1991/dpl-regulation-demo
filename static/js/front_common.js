//HTML5 ?쒓렇 block ?좎뼵
document.createElement('header');
document.createElement('footer');
document.createElement('section');
document.createElement('aside');
document.createElement('nav');
document.createElement('article');

//select ui
(function($){
	$.fn.extend({
		customStyle : function(options) {
			if(!$.browser.msie || ($.browser.msie&&$.browser.version>6)){
				return this.each(function() {
					var currentSelected = $(this).find(':selected');
						$(this).after('<span class="customStyleSelectBox"><span class="customStyleSelectBoxInner">'+currentSelected.text()+'</span>&nbsp;</span>').css({position:'absolute', opacity:0,fontSize:$(this).next().css('font-size')});
					var selectBoxSpan = $(this).next();
					var selectBoxWidth = parseInt($(this).width()) - parseInt(selectBoxSpan.css('padding-left')) -parseInt(selectBoxSpan.css('padding-right'));
					var selectBoxSpanInner = selectBoxSpan.find(':first-child');
						selectBoxSpan.css({display:'inline-block'});
						selectBoxSpanInner.css({width:selectBoxWidth, display:'inline-block'});
					var selectBoxHeight = parseInt(selectBoxSpan.height()) + parseInt(selectBoxSpan.css('padding-top')) + parseInt(selectBoxSpan.css('padding-bottom'));
						$(this).height(selectBoxHeight).change(function(){
						selectBoxSpanInner.text($(this).find(':selected').text()).parent().addClass('changed');
					});
				});
			}
		}
	});
})(jQuery);

$(function(){
	/*
	// create datepicker
	var opts = {
		inline: true
		, showMonthAfterYear : true
		, yearSuffix: ""
		, changeMonth: true
		, changeYear: true
		, monthNames : [ "1월", "2월", "3월", "4월", "5월", "6월", "7월", "8월", "9월", "10월", "11월", "12월" ]
		, dayNamesMin : [ "일", "월", "화", "수", "목", "금", "토"]
		, dateFormat: "yy-mm-dd"
	};
	$( ".datepicker" ).datepicker(
		$.extend('', opts)
	);
	// clear date
	$(".eraser_box a").bind("click",function(){
		$(this).prev().val("");
	});
	*/

	$('.select_ui').customStyle();

	/** header */
	$('#header nav>ul').hover(function(){
		$('#header').addClass('ov').find('.gnb_2dep').show();
	},function(){
		$('#header').removeClass('ov').find('.gnb_2dep').hide();
	});


	// 숫자 카운터
	/*
	이동해야하는 최대값(변수) : max = 826 -> 단계
	시작값 begin = 현재값 +- max

	var max = 0;
	$('.counter').each(function(){
		var d = parseInt($(this).text());
		if (max < d) {
			max = d;
		}
	});
	//console.log('max : ' + max);
	var begin = 0;
	$('.counter').each(function(){
		var d = parseInt($(this).text());
		if (d >= max) {
			begin = d - max;
		} else {
			begin = d + max;
		}
		//console.log('begin : ' + begin + ' , to : ' + d);
		$(this).counterUp({
			delay: 50,
			time: 5000,
			offset: 1,
			beginAt: begin,
			formatter: function (n) {
				return n.replace(/,/g, '.');
			}
		});
	});
	*/

	//counter_ui — 숫자는 서버에서 콤마포맷 정적 렌더 (rollingCounter 플러그인 제거)
	// (rollingCounter 호출 제거: 플러그인 미로드로 TypeError 발생하여 이후 JS 중단됨)
})