<!DOCTYPE>
<html>
<head>
    <meta name="layout" content="taglibs">
    <title>${tableComment}</title>
    <script type="text/javascript">
        var dgMain;
        \$(function () {
            dgMain = new EasyUIExt(\$("#mainGrid"), "<g:createLink controller="${propertyName}" action="list"  />");
            dgMain.singleSelect = true;
            dgMain.window = \$("#editWindow");
            dgMain.form = \$("#editForm");
            dgMain.pagination = true;
            dgMain.mainEasyUIJs();
        });

        function myAdd() {
            dgMain.mainAdd("<g:createLink controller="${propertyName}" action="create"/>");
            dgMain.formAction = "<g:createLink controller="${propertyName}" action="save"  />";
        }
        function edit() {
            dgMain.mainEdit("<g:createLink controller="${propertyName}" action="edit"  />");
            dgMain.formAction = "<g:createLink controller="${propertyName}" action="update"  />";
        }
        function del() {
            dgMain.mainDel("<g:createLink controller="${propertyName}" action="delete"  />");
        }
        function doSearch() {
        \$("#mainGrid").datagrid({
                queryParams: {
                    codeName: \$("#queryStr").textbox("getText")
                }
            });
        }
        function clearSearch(){
            \$("#queryStr").textbox("clear");
        }
    </script>
</head>

<body>
<div data-options="region:'north'" style="width: 100%;height:8%;margin-bottom:15px;">
    <div id="tb" style="width: 100%;height:100%">
        <span style="font-size:20Px;float: left">${tableComment}</span>
        <span style="margin-left:35%;margin-top: 1%;float: left">
            <input class="easyui-textbox"  data-options="prompt:'输入编码或名称查询',icons:[{iconCls:'icon-clear',handler:function(e){clearSearch()}}],buttonText:'查询',buttonIcon:'icon-search',width: 250,height:28,onClickButton:function(){doSearch()}" id="queryStr" name="queryStr">
        </span>
        <div style="margin-top:1%; text-align: right;width:20%;height: auto;float: right">
            <a href="javascript:void(0)" class="easyui-linkbutton" iconCls="icon-add" plain="true" onclick="myAdd()">增加</a>
            <a href="javascript:void(0)" class="easyui-linkbutton" iconCls="icon-edit" plain="true" onclick="edit()">修改</a>
            <a href="javascript:void(0)" class="easyui-linkbutton" iconCls="icon-remove" plain="true" onclick="del()">删除</a>
        </div>
    </div>
</div>
<div data-options="region:'center',split:true" style="width: 100%;height: 90%">
    <table id="mainGrid"  data-options="fit:true, fitColumns:false, idField : 'id',frozenColumns:[[{
	    field:'id',
	    checkbox:true
	}]]">
        <thead>
${dgTitles}
        </thead>
    </table>

</div>

<div id="editWindow" class="easyui-dialog" data-options="modal:true,closed:true,iconCls:'icon-save'" buttons="#infoWindow-buttons" style="width:400px;height:400px;">
    <form id="editForm" method="post">
        <table cellpadding="5">
            <input class="easyui-validatebox" type="hidden" name="id"/>
${editForm}
        </table>
    </form>
</div>

<div id="infoWindow-buttons">
    <a href="javascript:void(0)" id="sub" class="easyui-linkbutton" iconCls="icon-ok" onclick="dgMain.mainSave()">保存</a>
    <a href="javascript:void(0)" class="easyui-linkbutton" iconCls="icon-cancel" onclick="dgMain.mainClose()">取消</a>
</div>
</body>
</html>

