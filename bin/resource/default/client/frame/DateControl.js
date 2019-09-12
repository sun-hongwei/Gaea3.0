function DateControl() {

    this.header;
    this.uiInfo;

    this.obj;

    this.selectDates = {};
    this.selectDays = {};

    this.getValue = function (value, defaultValue) {
        if (Tools.isNull(value))
            if (Tools.isNull(defaultValue))
                return "";
            else
                return defaultValue
        else
            return value;
    }

    this.setField = function (fieldname, value, defaultValue) {
        return (Tools.isNull(value) ? (defaultValue == undefined ? '' : ' ' + fieldname + '="' + defaultValue + '"') : ' ' + fieldname + '="' + value + '"');
    }

    this.write = function (dw, header, uiInfo, divWidth, divHeight, inputStyle, borderStyle, textFontStyle) {
        var typename = "mini-datepicker";
        if (header.viewType == "日历") {
            typename = "mini-calendar";
        }
        var Vtype = header.vtype
        if (Vtype) {
            var vtype = header.vtype.split("compare");
            var max = header.vtype.split("compareMax");
            if (max.length >1) {
                vtype = max;
                max = true;
            }
            if (vtype.length > 1) {
                var k = vtype[1].indexOf(";");
                vtype = vtype[1].substr(1, k - 1);
                var vtypeJson = {};
                vtypeJson.compare = vtype;
                vtypeJson.name = "compare-" +vtype;
                vtypeJson.type = "date";
                CustomVTypeExecutor.setSpecialData(vtypeJson);
            }
        }
        var headerString = '<input' +
            this.setField('value', header.value) +
            this.setField("emptyText", header.emptyText) +
            this.setField('id', header.id) +
            this.setField('name', header.name) +
            this.setField('typecode', header.type) +
            this.setField('typename', header.typename) +
            this.setField('required', header.required) +
            this.setField('enabled', header.enabled) +
            this.setField('class', Tools.insertString(" ", header.styleClass, typename)) +
            this.setField('style', Tools.addString(header.style, ";width:100%;height:100%;" + textFontStyle)) +
            this.setField('format', header.format, "yyyy-MM-dd") +
            this.setField('timeFormat', header.timeFormat, "H:mm") +
            this.setField('showTime', header.showTime, false) +
            this.setField('showHeader', header.showHeader, true) +
            this.setField('showFooter', header.showFooter, true) +
            this.setField('showWeekNumber', header.showWeekNumber, true) +
            this.setField('showDaysHeader', header.showDaysHeader, true) +
            this.setField('showMonthButtons', header.showMonthButtons, true) +
            this.setField('showYearButtons', header.showYearButtons, true) +
            this.setField('showTodayButton', header.showTodayButton, true) +
            this.setField('showClearButton', header.showClearButton, true) +
            this.setField('inputStyle', inputStyle) +
            this.setField('borderStyle', borderStyle);
            if (vtypeJson) {
                if (max) {
                    headerString +=this.setField('onvalidation',  "compareMax_fun(e,'"+vtypeJson.name+"')");
                }else{
                    headerString +=this.setField('onvalidation',  "compare_fun(e,'"+vtypeJson.name+"')");
                }
            }
            headerString +='>'+
            '</input>';
        dw.write(header, headerString);
        this.header = header;
        this.uiInfo = uiInfo;
        mini.parse();

        this.obj = mini.getByName(header.name, this.header.tabParent);
        this.obj.options = {};
        this.obj.options.frameobj = this;

        this.obj.on("drawdate", this.onDrawDate);

        // var clickEvent = new MouseEvent('click',{
        //     //altKey:true // 模拟alt键按下
        // });
        // this.obj.todayButtonEl.dispatchEvent(clickEvent);
    }

    this.getDateKey = function(date){
        return date.getFullYear() + date.getMonth() + date.getDate();
    }

    /**
     * 设置选择日期，每次一个日期对象，相同对象将被覆盖
     * @param {date} date 要选择的日期对象
     */
    this.setSelectDates = function(date){
        var dateStr = this.getDateKey(date);
        this.selectDates[dateStr] = date;
    }

    /**
     * 清除所有选择日期设置
     */
    this.clearSelectDates = function(){
        this.selectDates = {};
    }

    /**
     * 设置选择天（每个月中的天），每次一个天，相同天将被覆盖
     * @param {int} day 要选择的天
     */
    this.setSelectDays = function(day){
        this.selectDays[day] = day;
    }

    /**
     * 清除所有选择天设置
     */
    this.clearSelectDays = function(){
        this.selectDays = {};
    }
    
    this.onDrawDate = function(e){
        //sender, date, dateCls, dateStyle, dateHtml, allowSelect
        var day = e.date.getDate();
        var control = e.sender.options.frameobj;
        var dateStr = control.getDateKey(e.date);
        var isok = !Tools.isNull(control.selectDates[dateStr]);
        if (!isok){
            isok = !Tools.isNull(control.selectDays[day]);
        }
        if (isok){
            e.dateHtml = "<div style='width:100%;height:100%; display:table;background:#ff9955'><span style='display:table-cell;vertical-align:middle'>" + day + "</span></div>"
        }
    }

}