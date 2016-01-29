import com.saas.common.codegen.DbHelper
import com.saas.common.codegen.DbParams
import grails.util.GrailsNameUtils
import org.grails.cli.interactive.completers.DomainClassCompleter
import org.grails.config.CodeGenConfig

description("同步系统资源信息") {
    usage "grails sync-res"
    completer DomainClassCompleter
    flag name: 'force', description: "Whether to overwrite existing resources"
}

DbParams dbParams = new DbParams()
dbParams.dbDriverName = "com.mysql.jdbc.Driver"
dbParams.dbUrl = "jdbc:mysql://192.168.0.79:3306/saas"
dbParams.dbSchema = "saas"
dbParams.dbUser = "root"
dbParams.dbPass = "root"
addStatus "Connect to database: " + dbParams.dbUrl
DbHelper dbHelper = new DbHelper(dbParams)

String sqlInsertRes = "INSERT INTO s_res(package_name, res_name, controller_name, parent_id, res_status) VALUES ('%s', '%s', '%s', %d, 0)"
String sqlInsertOp = "INSERT INTO s_operate(op_name, action_name) VALUES ('%s', '%s')"
String sqlInsertPriv = "INSERT INTO s_privilege(res_id, op_id, is_free) VALUES (%d, %d, %d)"
String sqlResID = "SELECT id FROM s_res WHERE package_name = '%s' AND controller_name = '%s'"   //不允许在不同包下存在同名controller
String sqlOpID = "SELECT id FROM s_operate WHERE op_name = '%s' AND action_name = '%s'"
String sqlPrivID = "SELECT id FROM s_privilege WHERE res_id = %d AND op_id = %d"

//读取配置文件application.yml
CodeGenConfig codeGenConfig = new CodeGenConfig()
codeGenConfig.loadYml(file("/grails-app/conf/application.yml"))
//    codeGenConfig.loadYml(new File(System.getProperty("user.dir") + "/grails-app/conf/application.yml"))
String pkgName = codeGenConfig.getProperty("grails.codegen.defaultPackage")
addStatus pkgName

def classNames = resources("file:grails-app/controllers/**/*.groovy")
        .collect { className(it) }

for (arg in classNames) {
    if(!arg.contains("Controller")) continue

    def sourceClass = source(arg)

    def model = model(sourceClass)
    String packagePath = model.getPackagePath()
    packagePath = packagePath.replaceAll("\\\\", ".")
    addStatus "getPackagePath: " + packagePath
    addStatus "getPath: " + sourceClass.getFile().getPath()

    //创建子包资源节点
    BigInteger parentId = 0
    if(!packagePath.equals(pkgName)) {
        String subPkgName = packagePath.substring(pkgName.length() + 1)
        //查找对应ID，如果不存在就新增记录
        parentId = dbHelper.fetch(String.format(sqlResID, pkgName, subPkgName))
        if(parentId == null) {
            dbHelper.executeSql(String.format(sqlInsertRes, pkgName, subPkgName, subPkgName, 0))
            parentId = dbHelper.fetch(String.format(sqlResID, pkgName, subPkgName))
        }
    }

    //通过文本分析，获取controller类信息
    BigInteger resId = -1
    FileInputStream inputStream = new FileInputStream(sourceClass.getFile())
    InputStreamReader fileStream = new InputStreamReader(inputStream, "utf-8")
    String comment = ""
    Boolean isInComment = false
    Boolean isAfterComment = false
    fileStream.eachLine {
        String line = it.trim()
        if(line.isEmpty()) return

        if (line.startsWith("/**")) {
            isInComment = true
            comment = ""
        } else if (line.endsWith("*/")) {
            isInComment = false
            isAfterComment = true
        } else if (isInComment && comment.isEmpty() && line.startsWith("*")) {
            if (!line.equals("*")) {
                comment = line.substring("*".length()).trim()
            }
        } else if (isAfterComment && line.startsWith("class")) {
            isAfterComment = false
            String controllerName
            int n = line.indexOf("{")
            if(n < 0) {
                controllerName = line.substring("class ".length())
            } else {
                controllerName = line.substring("class ".length(), n)
            }
            controllerName = GrailsNameUtils.getPropertyName(controllerName.replace("Controller", ""))
            addStatus controllerName + " " + comment
            //创建controller资源节点
//            String controllerName = model.getClassName()
            //查找对应ID，如果不存在就新增记录
            resId = dbHelper.fetch(String.format(sqlResID, pkgName, controllerName))
            if(resId == null) {
                dbHelper.executeSql(String.format(sqlInsertRes, pkgName, comment, controllerName, parentId))
                resId = dbHelper.fetch(String.format(sqlResID, pkgName, controllerName))
            }
        } else if (resId > 0 && isAfterComment && line.startsWith("def")) {    //controller的方法必须全部使用“def 方法名”的格式来定义，不带返回值类型
            isAfterComment = false
            int n = line.indexOf("(")
            if(n < 0) return

            String methodName = line.substring("def ".length(), n)
            addStatus methodName + " " + comment

            Boolean isFree = methodName.startsWith("get") || methodName.startsWith("query") || methodName.startsWith("is")
            if(methodName.equals("index")) {
                isFree = true
            }

            //添加操作
            BigInteger opId = dbHelper.fetch(String.format(sqlOpID, comment, methodName))
            if(opId == null) {
                dbHelper.executeSql(String.format(sqlInsertOp, comment, methodName))
                opId = dbHelper.fetch(String.format(sqlOpID, comment, methodName))
            }

            //添加权限
            BigInteger privId = dbHelper.fetch(String.format(sqlPrivID, resId, opId))
            if(privId == null) {
                dbHelper.executeSql(String.format(sqlInsertPriv, resId, opId, isFree ? 1 : 0))
            }
        }
    }
    fileStream.close()
    inputStream.close()
}
dbHelper.closeConn()
addStatus "Syncronize resources completed."
