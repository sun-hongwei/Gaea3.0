function TreeControl() {
    this.tree = undefined;

    this.Event = {
        OnNodeClick: undefined,
        OnGetData: undefined
    }

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

    this.addNode = function () {

    }

    this.getHtml = function (data, divWidth, divHeight) {
        var style = 'width:100%;height:100%;';
        if (data.border || data.border == "true") {
            style += "border:1px solid #ddd";
        } else {
            style += "border:0";
        }

        var classname = "mini-tree";
        if (!Tools.isNull(data.class))
            classname = data.class;

        var headerString = '<div' +
            this.setField('style', style) +
            this.setField('class', Tools.insertString(" ", data.styleClass, classname)) +
            this.setField('id', data.id) +
            this.setField('name', data.name) +
            this.setField('textField', data.textField) +
            this.setField('parentField', data.parentField) +
            // this.setField('width', Tools.convetPX(divWidth, data.width)) +
            // this.setField('height', Tools.convetPX(divHeight, data.height)) +
            this.setField('idField', data.idField);

        if (Tools.isNull(data.class) || data.class == "mini-tree") {
            headerString += this.setField('showTreeIcon', data.showTreeIcon, "true") +
                this.setField('checkedField', data.checkedField) +
                // this.setField('value', data.value) +
                this.setField('resultAsTree', data.resultAsTree, "false") +
                this.setField('url', data.url) +
                this.setField('allowSelect', data.allowSelect, "true") +
                this.setField('showCheckBox', data.showCheckBox, "true") +
                this.setField('showTreeLines', data.showTreeLines, "true") +
                this.setField('expandOnLoad', data.expandOnLoad, "true") +
                this.setField('showFolderCheckBox', data.showFolderCheckBox, "false") +
                this.setField('showExpandButtons', data.showExpandButtons, "true") +
                this.setField('enableHotTrack', data.enableHotTrack, "true") +
                this.setField('expandOnDblClick', data.expandOnDblClick, "false") +
                this.setField('expandOnNodeClick', data.expandOnNodeClick, "true") +
                this.setField('checkRecursive', data.checkRecursive, "true") +
                this.setField('autoCheckParent', data.autoCheckParent, "true") +
                this.setField('allowLeafDropIn', data.allowLeafDropIn, "true") +
                this.setField('allowDrag', data.allowDrag, "false") +
                this.setField('allowDrop', data.allowDrop, "false") +
                this.setField('frozenStartColumn', data.frozenStartColumn) +
                this.setField('frozenEndColumn', data.frozenEndColumn) +
                this.setField('showFilterRow', data.showFilterRow) +
                this.setField('lazy', data.lazy, "true");
        }
        headerString += '>' +
            '</div>';
        return headerString;
    }

    this.init = function (data) {
        mini.parse();

        this.tree = mini.getByName(data.name, data.tabParent);
        if (this.tree == undefined)
            return false;

        this.tree.on("nodeclick", this.OnNodeClick);

        this.id = data.id;
        if (Tools.isNull(data.class) || data.class == "mini-tree") {
            this.type = this.TREE;
        } else
            this.type = this.OUTLOOKTREE;
        this.tree.options = {};
        this.tree.options["treecontrol"] = this;
        this.tree.options.frameobj = this;
        this.selectid = undefined;

        if (!Tools.isNull(data.value)) {
            var treeData;
            if (typeof (data.value) == "object")
                treeData = data.value;
            else
                treeData = JSON.parse(data.value);
            this.loadFromData(treeData);
        }
    }

    this.write = function (dw, data, divWidth, divHeight) {
        var headerString = this.getHtml(data, divWidth, divHeight);

        dw.write(data, headerString);

        this.init(data);
    }

    this.TREE = 0;
    this.OUTLOOKTREE = 1;

    this.treeData = undefined;
    this.id = undefined;
    this.selectid = undefined;
    this.type = undefined;

    this.addNode = function (data) {
        var sel = this.tree.getSelectedNode();
        for (i = 0; i < data.length; i++) {
            item = data[i];
            if (!Tools.isNull(item)) {
                this.tree.addNode(item, "add", sel);
            }
        }
        if (!Tools.isNull(sel)) {
            this.tree.selectNode(sel);
            this.tree.expandNode(sel);
        }
    }

    this.loadFromOnGetLoadParam = function () {
        if (Tools.isNull(this.tree))
            return false;
        var data = undefined;

        if (Tools.isNull(this.Event.OnGetData)) {
            return;
        } else {
            data = this.Event.OnGetData(this.tree, this.id, this.selectid);
        }

        if (!Tools.isNull(data) && data.length > 0) {
            if (Tools.isNull(data[0].name)) {
                this.tree.textField = "text";
            } else
                this.tree.textField = "name";
        }
        return this.loadFromData(data);
    }

    this.loadFromData = function (data) {
        if (Tools.isNull(this.tree))
            return false;

        if (Tools.isNull(data))
            return false;

        // if (Tools.isNull(this.treeData))
        //     this.treeData = [];

        // var i;
        // for (i = 0; i < data.length; i++) {
        //     item = data[i];
        //     if (!Tools.isNull(item)) {
        //         if (Tools.isNull(item[this.tree.parentField])) {
        //             eval("delete item." + this.tree.parentField);
        //         }
        //         this.treeData.push(item);
        //     }
        // }

        this.tree.loadList(data, this.tree.idField, this.tree.parentField);
        // if (this.type == this.OUTLOOKTREE) {
        //     // json转对象
        //     // var obj = $.parseJSON(json);

        //     // 对象转json
        //     // var json = JSON.stringify(obj); 
        //     //var json = JSON.stringify(this.treeData); 
        // } else
        //     this.addNode(data);
        return true;

    }

    this.load = function () {
        if (this.tree == undefined)
            return false;

        this.selectid = undefined;
        return this.loadFromOnGetLoadParam();
    }

    this.getCascadeSelectValues = function (parent) {
        var result = [];
        this.tree.cascadeChild(parent, function (node) {
            if (!this.tree.isCheckedNode(node))
                return true;
            var idField = this.tree.getIdField();
            result.push(node[idaFIeld]);
            return true;
        })
        return result;
    }

    this.findNode = function (id) {
        var nodes = this.findNodes(id);
        if (Tools.isNull(nodes))
            return undefined;
        else
            return nodes[0];
    }

    this.findNodes = function (id) {
        var nodes = tree.findNodes(function (node) {
            var idField = this.tree.getIdField();
            if (node[idaFIeld] == id) return true;
        });

        if (!Tools.isNull(nodes) || nodes.length == 0)
            return undefined;
        else
            return nodes;
    }

    this.getRoot = function () {
        return this.tree.getRootNode();
    }

    this.getSelectValues = function (parent) {
        var result = [];
        this.tree.eachChild(parent, function (node) {
            if (!this.tree.isCheckedNode(node))
                return true;
            var idField = this.tree.getIdField();
            result.push(node[idaFIeld]);
            return true;
        })
        return result;
    }

    this.getValues = function () {
        var result = [];
        var root = this.tree.getRootNode();
        this.tree.cascadeChild(root, function (node) {
            var idField = this.tree.getIdField();
            result.push(node[idaFIeld]);
            return true;
        })
        return result;
    }

    this.OnNodeClick = function (e) {
        this.selectid = undefined;
        var tree = e.sender;
        var tc = tree.options["treecontrol"];
        if (e.isLeaf) {
            tc.selectid = e.node.id;
            if (this.type == this.TREE) {
                tc.internalLoad();
                if (tree.isLeaf(e.node) && !Tools.isNull(e.node.options) && !e.node.options["inited"]) {
                    e.node.options = {};
                    e.node.options["inited"] = true;
                }
            }
        }

        if (!Tools.isNull(tc.Event.OnNodeClick))
            tc.Event.OnNodeClick(tc, tree, e.node, e.isLeaf);
    }

}