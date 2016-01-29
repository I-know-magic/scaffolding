<%=packageName ? "package ${packageName}" : ''%>

import com.saas.common.ResultJSON
import com.saas.common.exception.ServiceException
import grails.transaction.Transactional

//TODO 修改类注释
/**
 * ${classMemo}Service
 * @author CodeGen
 * @generate at ${timestamp}
 */
@Transactional
class ${className}Service {

    /**
     * 查询${classMemo}列表
     * @param params 参数Map，至少应包含rows,page两个参数
     */
    @Transactional(readOnly = true)
    def query${className}List(Map params) throws ServiceException {
        try {
            ResultJSON result = new ResultJSON()
            def map = new HashMap<String, Object>()

            StringBuffer query = new StringBuffer("from ${className} t")
            StringBuffer queryCount = new StringBuffer("select count(t) from ${className} t")
            def queryParams = new HashMap()
            queryParams.max = params.rows
            queryParams.offset = (Integer.parseInt(params.page) - 1) * Integer.parseInt(params.rows)
            def namedParams = new HashMap()
            //TODO 处理查询条件

            def list = ${className}.executeQuery(query.toString(), namedParams, queryParams)
            def count = ${className}.executeQuery(queryCount.toString(), namedParams)
            map.put("total", count.size() > 0 ? count[0] : 0)
            map.put("rows", list)
            if (list.size() == 0) {
                result.setMsg("无数据")
            }

            result.jsonMap = map
            return result
        } catch (Exception e) {
            ServiceException se = new ServiceException("1001", "查询失败", e.message)
            throw se
        }
    }

    /**
     * 新增或更新${classMemo}
     * @param ${propertyName}
     * @return
     */
    def save(${className} ${propertyName}) throws ServiceException {
        try {
            ResultJSON result = new ResultJSON()

            if (${propertyName}.id) {
                if (${propertyName}.hasErrors()) {
                    throw new Exception("数据校验失败!")
                }
                def o${className} = ${className}.findById(${propertyName}.id)
                //TODO 复制对象属性值

                o${className}.save flush: true
                result.setMsg("编辑成功")
            } else {
                ${propertyName}.save flush: true
                result.setMsg("添加成功")
            }

            return result
        } catch (Exception e) {
            ServiceException se = new ServiceException("1002", "保存数据失败", e.message)
            throw se
        }
    }

    /**
     * 新增${classMemo}
     * @param id ${classMemo}主键id
     * @return
     */
    @Transactional(readOnly = true)
    def create() throws ServiceException {
        try {
            ResultJSON result = new ResultJSON()
            ${className} ${propertyName} = new ${className}()
            result.object = ${propertyName}

            return result
        } catch (Exception e) {
            ServiceException se = new ServiceException("1003", "添加失败", e.message)
            throw se
        }
    }

    /**
     * 编辑${classMemo}
     * @param id ${classMemo}主键id
     * @return
     */
    @Transactional(readOnly = true)
    def edit(String id) throws ServiceException {
        try {
            ResultJSON result = new ResultJSON()
            if (id) {
                ${className} ${propertyName} = ${className}.findById(Integer.parseInt(id))
                result.object = ${propertyName}
            } else {
                throw new Exception("无效的id")
            }

            return result
        } catch (Exception e) {
            ServiceException se = new ServiceException("1004", "编辑失败", e.message)
            throw se
        }
    }

    /**
     * 删除${classMemo}，支持批量删除
     * @param params
     * @return
     */
    def delete(def params) throws ServiceException {
        try {
            ResultJSON result = new ResultJSON()

            if (params.ids) {
                for (String id : params.ids.split(",")) {
                    ${className} ${propertyName} = ${className}.findById(Integer.parseInt(id))
                    ${propertyName}.isDeleted = true
                    ${propertyName}.save flush: true
                }
            }
            result.setMsg("删除成功!")

            return result
        } catch (Exception e) {
            ServiceException se = new ServiceException("1005", "删除数据失败", e.message)
            throw se
        }
    }
}
