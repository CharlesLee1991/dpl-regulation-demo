//HTML5 태그 block 선언
document.createElement('header');
document.createElement('footer');
document.createElement('section');
document.createElement('aside');
document.createElement('nav');
document.createElement('article');

// Array.isArray for ie8
Array.isArray = function (obj) {
	return Object.prototype.toString.call(obj) === "[object Array]";
};

if(!window.jQuery)
{
	/*
	var script = document.createElement('script');
	script.type = "text/javascript";
	script.src = "/lims_0911/js/jquery-1.7.2.min.js";
	document.getElementsByTagName('head')[0].insertBefore(script, document.getElementsByTagName('script')[0]);
	*/
} else {
	$(document).ready(function(){
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

			// input numeric
			$("input[data-type='int'], input[data-type='float']").on("keydown",function(){
				var key;
				var keychar;
				var type = $(this).attr("data-type");

				if (window.event) {
					key = window.event.keyCode;
				} else if (e) {
					key = e.which;
				} else {
					return true;
				}

				keychar = String.fromCharCode(key);

				if ((key >= 96 && key <= 105) || key == 8 || key == 9) { // number key or backspace or tab
					return true;
				} else if ((("0123456789.").indexOf(keychar) > -1)) {
					return true;
				} else if (type == "float" && (($(this).val()).indexOf(".") == -1) && ( key == 110 || key == 190)) { // .은 한번만 들어가게
					return true;
				} else
					return false;
			});
	});
}
