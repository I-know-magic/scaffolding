<%=packageName ? "package ${packageName}" : ''%>

${imports}

/**
 * ${tableComment}
 */
${annotation}
class ${className} {

    static constraints = {
${constraints}
    }
    static mapping = {
${mapping}
    }

${fields}
}
