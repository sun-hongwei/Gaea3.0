// 加箱数
function addXs(e) {
	var selRow = e.record;
	var count = mini.get("sl").value;
	if (count == NaN || count < 0 || ""==count)
		count = 0;
	count = parseInt(count) +parseFloat(selRow.YSDDDR_PJ_XS);
	var sl = mini.get("sl");
	sl.setValue(count);
}
// 减箱数
function subXs(e) {
	
	var selRow = e.record;
	var count = mini.get("sl").value;
	count = parseInt(count) -parseFloat(selRow.YSDDDR_PJ_XS);
	var sl = mini.get("sl");
	if (count == NaN || count < 0 || ""==count)
		count = 0;
	sl.setValue(count);
}