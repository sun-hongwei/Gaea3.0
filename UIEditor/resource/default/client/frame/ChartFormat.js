var chartFormat={
    getPieCharts:function(dataSourceId, rowIndex){
    	var data = GlobalDataSources.getRow(dataSourceId, rowIndex);
    	return this.getJsonValueText(data);
	},
	getPieChartsCol:function(dataSourceId, rowIndex){
    	var data = GlobalDataSources.getCol(dataSourceId, rowIndex);
    	var newdata = [];
		for(var k in data){
			var jsonstr = {};
			jsonstr.value = data[k];
			jsonstr.name = rowIndex;
			newdata.push(jsonstr);
		}
		return newdata;
	},
	getPieChartsValue:function(dataSourceId, rowIndex,field){
		var data = GlobalDataSources.getValue(dataSourceId, rowIndex,field);
		return [{value:data,name:field}]
	},
	getJsonValueText:function(data){
		var newdata = [];
		for(var k in data){
			var jsonstr = {};
			jsonstr.value = data[k];
			jsonstr.name = k;
			newdata.push(jsonstr);
		}
		return newdata;
	}
}