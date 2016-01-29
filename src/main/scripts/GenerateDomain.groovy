import com.saas.common.ResultJSON
import com.saas.common.codegen.ColInfo
import com.saas.common.codegen.DbHelper
import grails.util.GrailsNameUtils
import org.grails.cli.interactive.completers.DomainClassCompleter
import org.grails.config.CodeGenConfig

import java.text.SimpleDateFormat

description("根据表结构生成DomainClass") {
    usage "grails gen-domain [TABLE NAME]"
    completer DomainClassCompleter
    flag name: 'force', description: "Whether to overwrite existing files"
}

if (args) {
    def tableNames = args

    //读取配置文件application.yml
    CodeGenConfig codeGenConfig = new CodeGenConfig()
    codeGenConfig.loadYml(file("/grails-app/conf/application.yml"))
//    codeGenConfig.loadYml(new File(System.getProperty("user.dir") + "/grails-app/conf/application.yml"))
    String pkgName = codeGenConfig.getProperty("grails.codegen.defaultPackage")

    //TODO 扩展到支持多表生成
//  if(args[0] == '*') {
//    tableNames = resources("file:grails-app/domain/**/*.groovy")
//                    .collect { className(it) }
//  }
    for (arg in tableNames) {
        def overwrite = flag('force') ? true : false
        //读取表和字段信息
        DbHelper dbHelper = new DbHelper()
        ResultJSON ret
        try {
            ret = dbHelper.queryTableInfo(arg)
        } catch (Exception e) {
            addStatus "如果报错找不到mysql驱动文件，请将驱动jar包复制到目录:%JAVA_HOME%\\jre\\lib\\ext"
            error e.message
            return
        }
        if(ret.success.equals("false")) {
            addStatus ret.msg
            return
        }
        dbHelper.closeConn()
        String tableComment = ret.jsonMap.tableComment
        System.out.print(tableComment)
        ArrayList<ColInfo> columns = ret.jsonMap.colInfo
        Map md = [:]

        //生成DomainClass文件
        //表名转类名
        String className = GrailsNameUtils.getClassName(GrailsNameUtils.getPropertyNameForLowerCaseHyphenSeparatedName(arg.replace('_', '-')));
        if(arg.toLowerCase().startsWith("s_")) {
            className = "Sys" + className.substring(1)
        } else if(arg.toLowerCase().startsWith("v_")) {
            className = "View" + className.substring(1)
        }

        md.put("pkgName", pkgName)
        md.put("packagePath", pkgName.replace('.', '/'))
        md.put("tableComment", tableComment)
        md.put("className", className)
        md.put("propertyName", GrailsNameUtils.getPropertyName(className))
        md.put("constraints", "")
        md.put("mapping", "")
        md.put("fields", "")
        md.put("annotation", "")
        md.put("import", "")

        //生成字段列表
        StringBuffer sbImport = new StringBuffer()      //import信息
        StringBuffer sbAnnotation = new StringBuffer()  //类注解
        StringBuffer sbCol = new StringBuffer()         //字段列表
        StringBuffer sbFix = new StringBuffer()         //通用字段
        StringBuffer sbCons = new StringBuffer()        //约束
        StringBuffer sbMap = new StringBuffer()         //映射
        StringBuffer sbHeader = new StringBuffer()      //表格列标题（用于生成view）
        StringBuffer sbEdit = new StringBuffer()        //编辑框内容
        columns.each {
            ColInfo colInfo = it
            if(colInfo.attrName.equals("tenantId")) {
                sbAnnotation.append("@TenantFilter(column = \"tenant_id\")")
                sbImport.append("import com.saas.common.annotation.TenantFilter")
                sbFix.append("    BigInteger tenantId\n")
            } else if (colInfo.attrName.equals("id")) {
                sbFix.append("    BigInteger id\n")
            } else if (colInfo.attrName.equals("dateCreated")) {
                sbFix.append("    Date dateCreated\n")
                if(!colInfo.colName.equals("date_created"))
                    sbMap.append("        dateCreated column: '${colInfo.colName}'\n")
            } else if (colInfo.attrName.equals("createAt")) {
                sbFix.append("    Date createAt\n")
            } else if (colInfo.attrName.equals("createBy")) {
                sbFix.append("    String createBy\n")
            } else if (colInfo.attrName.equals("lastUpdated")) {
                sbFix.append("    Date lastUpdated\n")
                if(!colInfo.colName.equals("last_updated"))
                    sbMap.append("        lastUpdated column: '${colInfo.colName}'\n")
            } else if (colInfo.attrName.equals("lastUpdateAt")) {
                sbFix.append("    Date lastUpdateAt\n")
            } else if (colInfo.attrName.equals("lastUpdateBy")) {
                sbFix.append("    String lastUpdateBy\n")
            } else if (colInfo.attrName.equals("isDelete")) {
                sbFix.append("    boolean isDelete\n")
            } else if (colInfo.attrName.equals("isDeleted")) {
                sbFix.append("    boolean isDeleted\n")
            } else {
                sbCol.append("    /**\n")
                sbCol.append("     * ${colInfo.comment}\n")
                sbCol.append("     */\n")
                sbCol.append("    ${colInfo.attrType} ${colInfo.attrName}\n")
                //唯一约束
                if(colInfo.key.equals("uni"))
                    sbCons.append("        ${colInfo.attrName}(unique: true)\n")
                //表格列标题
                sbHeader.append("    <th data-options=\"field:'${colInfo.attrName}'\">${colInfo.comment}</th>\n")
                //编辑框
                sbEdit.append("            <tr>\n")
                sbEdit.append("                <td class=\"title\">${colInfo.comment}:</td>\n")
                sbEdit.append("                <td><input class=\"easyui-textbox\" type=\"text\" name=\"${colInfo.attrName}\" data-options=\"required:true\"/></td>\n")
                sbEdit.append("            </tr>\n")
            }
        }
        //处理特殊表名
        if(arg.startsWith("s_") || arg.startsWith("v_")) {
            sbMap.append("        table '${arg}'\n")
        }
        sbCol.append("\n")
        sbCol.append(sbFix)
        md.fields = sbCol.toString()
        md.constraints = sbCons.toString()
        md.mapping = sbMap.toString()
        md.annotation = sbAnnotation.toString()
        md.import = sbImport.toString()

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        def timestamp = format.format(new Date())

        //生成Domain类
        def fileName = "grails-app/domain/${md.packagePath}/${md.className}.groovy"
        render template: template('scaffolding/Domain.groovy'),
                destination: file(fileName),
                model: [packageName: md.pkgName, tableComment: md.tableComment, className: md.className, constraints: md.constraints, mapping: md.mapping, fields: md.fields, annotation: md.annotation, imports: md.import],
                overwrite: overwrite

        //生成View
        fileName = "grails-app/views/${md.propertyName}/view.gsp"
        render template: template('scaffolding/index1.gsp'),//easyuiView
                destination: file(fileName),
                model: [packageName: md.pkgName, tableComment: md.tableComment, className: md.className, propertyName: md.propertyName, timestamp: timestamp, dgTitles: sbHeader.toString(), editForm: sbEdit.toString()],
                overwrite: overwrite

        addStatus "Scaffolding completed for ${md.className}.groovy"
    }
} else {
    error "No table specified"
}
