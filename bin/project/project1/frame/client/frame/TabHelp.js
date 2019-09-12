function createGridTab(tabName){
    tn = "tab" + tabName;
    tabInfo = globalModel.global_splitter.addTab({
            name: tn,
            title:"表格" + tabName,
            showCloseButton:"true"
            // url:"./client/frame/div.html",
            // onload : function(e){
            // }
    });

    gridTab = tabInfo.tab;

    if (!tabInfo.exists){
        bodyEl = globalModel.global_splitter.getBodyForTab(gridTab);
        globalModel.global_dgc = new DataGridControl();
        globalModel.global_dgc.Event.OnSumCell = function (dgc, grid, result, field, value){
            if (Tools.isNull(value))
                return;
            return "最大值：" + value;
        };

        var gid = "grid" + tn;
        griddata = {
                tabParent : bodyEl,
                id:gid,
                idField:"idx",
                showSummaryRow:"true",
                showFilterRow:"true",
                header:[
                    {type:globalModel.global_dgc.INDEX_TYPE, title:"索引", headerAlign:"center"},
                    {type:globalModel.global_dgc.CHECK_TYPE},
                    {type:globalModel.global_dgc.TEXTBOX_TYPE, field:"id", width:100, align:"center", allowsort:"true", title:"编号", readOnly:"true", 
                        summaryType:"max", filter:{name:"idfilter" + tn, onvaluechanged:"onidfilter(e)"}},
                    {type:globalModel.global_dgc.COMBOBOX_TYPE, field:"name", title:"姓名", onclick : "onNameHeaderClick()", 
                        name:"cbb" + tn, textField : "nameselect", valueField : "idselect"},
                    {type:globalModel.global_dgc.SUB_TYPE, title:"多级表头", 
                    subs:[
                            {type:globalModel.global_dgc.TEXTBOX_TYPE, field:"id", width:100, align:"center", allowsort:"true", title:"编号"},
                            {type:globalModel.global_dgc.COMBOBOX_TYPE, field:"name", title:"姓名", onclick : "onNameHeaderClick()",
                                name:"cbb" + tn, textField : "nameselect", valueField : "idselect"}
                        ]
                    },
                ]
            };

        globalModel.global_dgc.write(dw, griddata);		
        globalModel.global_dgc.init("grid sample test", "sample", {id:[1,2,3,4,5]});
    }
}

function createScrollTab(tabName){
    tn = "tab" + tabName;
    tabInfo = globalModel.global_splitter.addTab({
            name: tn,
            title:"表格" + tabName,
            showCloseButton:"true"
            // url:"./client/frame/div.html",
            // onload : function(e){
            // }
    });

    tab = tabInfo.tab;

    if (!tabInfo.exists){
        bodyEl = globalModel.global_splitter.getBodyForTab(tab);
        sbc = new ScrollBarControl();

        var tid = "scrollbar" + tn;
        scrolldata = {
                tabParent : bodyEl,
                id:tid,
                tag:0,
                init:true,
                onGetData:function(scrollBarControlObj, inited){
                    data = {
                        command:"sample",
                        hint:"惰性加载例子",
                        postdata:{start:scrollBarControlObj.tag, size:(inited || scrollBarControlObj.tag == 0? 80 : 20)}
                    };
                    scrollBarControlObj.tag++;
                    return data;
                }
            };

        sbc.write(dw, scrolldata);
        sbc.load();		
    }
}

function onquery(e){
    miniButton = e.sender;
    textbox = mini.get("queryinput");
    alert(miniButton.text + ":" + textbox.value);
}

var selectNameHeaderData = undefined;

function onNameHeaderClick(e){
    if (selectNameHeaderData != undefined)
        return selectNameHeaderData;

    data = DBOperation.QueryEx("sampleselect", {id : [1,2,3]});
    selectNameHeaderData = data;
    //global_dgc.setComboboxValue("nameEditor", data);
    return data;
}

function onidfilter(e){
    if (Tools.isNull(e))
        return;

    value = e.value;
    if (Tools.isNull(value))
        globalModel.global_dgc.init("grid sample test", "sample", {id:[1,2,3,4,5]});
    else{
        globalModel.global_dgc.init("grid sample", "sample", {id:[value]});
    }
}

function dosample(){
    RequestInstance.submit("sample1", "command", {key:"key1",value:"value1"},
                            function(griddatatxt) {
                                var data = mini.decode(griddatatxt);
                                alert(griddatatxt);
                            },
                            function(msg){
                                alert(msg);
                            }		
                        );
}

function onMainActiveChanged(tabName){
    grid = mini.get("grid" + tabName);
    if (Tools.isNull(grid) || Tools.isNull(globalModel.global_dgc))
        return;
    globalModel.global_dgc = grid.options[GridOptionsKey];
}