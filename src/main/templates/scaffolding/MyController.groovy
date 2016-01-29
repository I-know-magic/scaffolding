<%=packageName ? "package ${packageName}" : ''%>

import com.saas.common.ResultJSON
import com.saas.common.exception.ServiceException
import com.saas.common.util.LogUtil

//TODO 修改类注释
/**
 * ${classMemo}Controller
 * @author CodeGen
 * @generate at ${timestamp}
 */
class ${className}Controller {
//    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    ${className}Service ${propertyName}Service

    /**
     * 显示功能页面
     */
    def index() {
        render(view: "/${propertyName}/view")
    }

    /**
     * 查询
     */
    def list() {
        ResultJSON result
        try {
            result = ${propertyName}Service.query${className}List(params)
        } catch (ServiceException se) {
            result = new ResultJSON(se.getCodeMessage(), false)
            LogUtil.logError(se, params)
        } catch (Exception e){
            result = new ResultJSON(e)
            LogUtil.logError(e, params)
        }
        render result
    }

    /**
     * 新增
     */
    def create() {
        ResultJSON result
        try {
            result = ${propertyName}Service.create()
        } catch (ServiceException se) {
            result = new ResultJSON(se.getCodeMessage(), false)
            LogUtil.logError(se, params)
        } catch (Exception e){
            result = new ResultJSON(e)
            LogUtil.logError(e, params)
        }
        render result
    }

    /**
     * 修改
     */
    def edit() {
        ResultJSON result
        try {
            String id = params.id;
            result = ${propertyName}Service.edit(id)
        } catch (ServiceException se) {
            result = new ResultJSON(se.getCodeMessage(), false)
            LogUtil.logError(se, params)
        } catch (Exception e){
            result = new ResultJSON(e)
            LogUtil.logError(e, params)
        }
        render result
    }

    /**
     * 保存
     */
    def save() {
        ResultJSON result
        try {
            ${className} ${propertyName} = new ${className}(params)
            result = ${propertyName}Service.save(${propertyName})
        } catch (ServiceException se) {
            result = new ResultJSON(se.getCodeMessage(), false)
            LogUtil.logError(se, params)
        } catch (Exception e){
            result = new ResultJSON(e)
            LogUtil.logError(e, params)
        }
        render result
    }

    /**
     * 更新
     */
    def update() {
        ResultJSON result
        try {
            ${className} ${propertyName} = new ${className}(params)
            result = ${propertyName}Service.save(${propertyName})
        } catch (ServiceException se) {
            result = new ResultJSON(se.getCodeMessage(), false)
            LogUtil.logError(se, params)
        } catch (Exception e){
            result = new ResultJSON(e)
            LogUtil.logError(e, params)
        }
        render result
    }

    /**
     * 删除
     */
    def delete() {
        ResultJSON result
        try {
            result = ${propertyName}Service.delete(params)
        } catch (ServiceException se) {
            result = new ResultJSON(se.getCodeMessage(), false)
            LogUtil.logError(se, params)
        } catch (Exception e){
            result = new ResultJSON(e)
            LogUtil.logError(e, params)
        }
        render result
    }

}
