function ControlCommon() {
    this.CHECKBOX_TYPE = 0;
    this.LABEL_TYPE = 1;
    this.DATE_TYPE = 2;
    this.TEXTBOX_TYPE = 3;
    this.COMBOBOX_TYPE = 4;
    this.BUTTON_TYPE = 5;
    this.SEPARATOR_TYPE = 6;
    this.DATE_TIME = 7;
    this.COMBOBOXTREE_TYPE = 8;
    this.TEXTAREA_TYPE = 9;
    this.RADIOBUTTONS_TYPE = 10;
    this.LISTBOX_TYPE = 11;
    this.PASSWORD_TYPE = 12;
    this.GRID_TYPE = 13;
    this.TREE_TYPE = 14;
    this.SCROLLBAR_TYPE = 15;
    this.IMAGE_TYPE = 16;
    this.TIME_TYPE = 17;
    this.REPORT_TYPE = 18;
    this.Chart_Type = 19;
    this.MainMenu_Type = 20;
    this.MainTree_Type = 21;
    this.ListView_Type = 22;
    this.ProgressBar_Type = 23;
    this.Upload_Type = 24;
    this.Div_Type = 25;
    this.Sub_Type = 26;
    this.Toolbar_Type = 27;
    this.Timer_Type = 28;
    this.UNKNOWN_TYPE = -1;

    this.Label_Name = "标签";
    this.TextBox_Name = "文本框";
    this.Image_Name = "图片";
    this.ComboBox_Name = "下拉列表";
    this.Spinner_Name = "数字选择框";
    this.ComboTreeBox_Name = "下拉树列表";
    this.RadioBox_Name = "单选框";
    this.CheckBox_Name = "多选框";
    this.DateBox_Name = "日期";
    this.TimeBox_Name = "时间";
    this.Tree_Name = "树列表";
    this.Grid_Name = "表格";
    this.Button_Name = "按钮";
    this.Password_Name = "密码框";
    this.TextArea_Name = "多行文本框";
    this.ListBox_Name = "列表框";
    this.ScrollBar_Name = "滚动列表框";
    this.Report_Name = "报表";
    this.Separator_Name = "分隔符";
    this.Chart_Name = "图表";
    this.MainMenu_Name = "主菜单";
    this.MainTree_Name = "主导航树";
    this.ListView_Name = "视图列表";
    this.ProgressBar_Name = "进度条";
    this.Upload_Name = "上传框";
    this.Div_Name = "占位框";
    this.Sub_Name = "子界面";
    this.Toolbar_Name = "工具栏";
    this.Timer_Name = "定时器";

    this.convertType = function(typename) {
        switch (typename) {
            case this.Toolbar_Name:
                return this.Toolbar_Type;

            case this.Upload_Name:
                return this.Upload_Type;

            case this.Report_Name:
                return this.REPORT_TYPE;

            case this.Label_Name:
                return this.LABEL_TYPE;

            case this.TextBox_Name:
                return this.TEXTBOX_TYPE;

            case this.TextArea_Name:
                return this.TEXTAREA_TYPE;

            case this.ListBox_Name:
                return this.LISTBOX_TYPE;

            case this.ScrollBar_Name:
                return this.SCROLLBAR_TYPE;

            case this.Password_Name:
                return this.PASSWORD_TYPE;

            case this.Spinner_Name:
                return this.INT_TYPE;

            case this.Image_Name:
                return this.IMAGE_TYPE;

            case this.Div_Name:
                return this.Div_Type;

            case this.Sub_Name:
                return this.Sub_Type;

            case this.ComboBox_Name:
                return this.COMBOBOX_TYPE;

            case this.ComboTreeBox_Name:
                return this.COMBOBOXTREE_TYPE;

            case this.RadioBox_Name:
                return this.RADIOBUTTONS_TYPE;

            case this.CheckBox_Name:
                return this.CHECKBOX_TYPE;

            case this.DateBox_Name:
                return this.DATE_TYPE;

            case this.TimeBox_Name:
                return this.TIME_TYPE;

            case this.Tree_Name:
                return this.TREE_TYPE;

            case this.Grid_Name:
                return this.GRID_TYPE;

            case this.Button_Name:
                return this.BUTTON_TYPE;

            case this.Separator_Name:
                return this.SEPARATOR_TYPE;

            case this.Chart_Name:
                return this.Chart_Type;

            case this.MainTree_Name:
                return this.MainTree_Type;

            case this.ListView_Name:
                return this.ListView_Type;

            case this.MainMenu_Name:
                return this.MainMenu_Type;

            case this.ProgressBar_Name:
                return this.ProgressBar_Type;

            default:
                return this.UNKNOWN_TYPE;
        }

    }

}