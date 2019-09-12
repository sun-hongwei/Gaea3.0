function ChartControl() {
    this.chart = null;
    this.data;
    this.div;
    this.curTheme;
    this.uiInfo;
    this.autoLoad = false;
    this.getValue = function (value, defaultValue) {
        if (Tools.isNull(value))
            if (Tools.isNull(defaultValue))
                return "";
            else
                return defaultValue
        else
            return value;
    }

    this.refresh = function (args) {
        var myChart = this.chart;
        if (myChart && myChart.dispose) {
            myChart.dispose();
        }

        var domMain = $(this.div).find("#" + this.data.id)[0];

        myChart = echarts.init(domMain, this.curTheme);
        eval("var " + this.data.name + " = myChart");

        window.onresize = myChart.resize;
        // var theString = this.data.chartData;//.replace(/\n/g, " ").replace(/  /g, " ");

        var getOptions;
        var functionParams = [];

        var args = [];
        var functionStr = this.data.chartData + "\n" + "return " + this.data.resultName + ";";
        if (!Tools.isNull(this.data.functionParams)) {
            var params = this.data.functionParams.split(",");
            for (var index = 0; index < params.length; index++) {
                var param = params[index];
                functionParams.push(param);
                var paramVar = eval(param);
                args.push(paramVar);
            }
        }

        functionParams.push("myChart");
        functionParams.push(this.data.name);
        args.push(myChart);
        args.push(myChart);

        if (Tools.isNull(functionParams))
            getOptions = new Function(functionStr);
        else
            getOptions = new Function(functionParams, functionStr);


        var options = Tools.dynamicCallFunction(getOptions, args);
        //eval(this.data.chartData);
        myChart.setOption(options, true);
        this.chart = myChart;
        this.chart.options = {};
        this.chart.options["control"] = this;
        if (Tools.isNull(domMain.options))
            domMain.options = {};
        domMain.options["chart"] = this.chart;
        domMain.options["control"] = this;
        domMain.options.frameobj = this;

    }

    this.setField = function (fieldname, value, defaultValue) {
        return (Tools.isNull(value) ? (defaultValue == undefined ? '' : ' ' + fieldname + '="' + defaultValue + '"') : ' ' + fieldname + '="' + value + '"');
    }

    this.switchToImage = function () {
        var imageselector = $(this.div).find("img");
        var divselector = $(this.div).find("#" + this.data.id);
        imageselector[0].src = this.chart.getDataURL({
            pixelRatio: 2,
            backgroundColor: '#fff'
        });
        divselector.hide();
        imageselector.show();
    }

    this.getImageUrl = function () {
        return this.chart.getDataURL({
            pixelRatio: 2,
            backgroundColor: '#fff'
        });
    }

    this.switchToChart = function () {
        var imageselector = $(this.div).find("img");
        var divselector = $(this.div).find("#" + this.data.id);
        imageselector.hide();
        divselector.show();
    }

    this.getHtml = function (header, divWidth, divHeight) {
        this.autoLoad = Tools.isNull(header.autoLoad) ? false : header.autoLoad;
        this.data = header;
        var headerString = '<div style="width:100%' +
            // Tools.convetPX(divWidth, header.width) + 
            ';height:100%"' +
            // Tools.convetPX(divHeight, header.height) + '"' +
            this.setField('id', header.id) +
            this.setField('name', header.name) +
            '>';
        headerString += '</div>';
        headerString += '<img style="width:100%' +
            // Tools.convetPX(divWidth, header.width) + 
            ';height:100%' +
            // Tools.convetPX(divHeight, header.height) + ';display:none"' +
            this.setField('id', header.id + "image") +
            this.setField('name', header.name + "image") +
            '/>';

        return headerString;
    }

    this.init = function (uiInfo, div, header, autoload) {
        this.div = $(div).parent()[0];
        this.uiInfo = uiInfo;
        div.options = {};
        div.options["control"] = this;
        div.options["frameobj"] = this;
        if (Tools.isNull(this.div.options))
            this.div.options = {};
        this.div.options["control"] = this;
        this.div.options["frameobj"] = this;
        this.data = header;
        this.autoLoad = Tools.isNull(header.autoLoad) ? false : header.autoLoad;
        if (!Tools.isNull(autoload) && autoload) {
            this.refresh();
        }
    }

    this.write = function (dw, header, divWidth, divHeight, uiInfo) {
        var headerString = this.getHtml(header, divWidth, divHeight);
        dw.write(header, headerString);
        var div = $(header.tabParent).find("#" + header.id)[0];
        this.init(uiInfo, div, header);
    }
}